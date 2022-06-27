package com.meinc.commons.postoffice.directory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.meinc.commons.postoffice.sender.IEmailSender.SendResult;
import com.meinc.commons.postoffice.service.EmailAddress;

public class PostOfficeDirectory {
  private static Log _log = LogFactory.getLog(PostOfficeDirectory.class);
  
  private File _directoryRootFile;
  private File _directorySendFile;
  private File _directoryErrorFile;
  private File _directoryTmpFile;
  
  public void saveEmail(String emailText) throws IOException {
    repairEmailDirectories();
    
    File sendFile = File.createTempFile("send_", ".email", _directoryTmpFile);
    
    try {
      FileWriter fw = new FileWriter(sendFile);
      fw.write(emailText);
      fw.close();
    } catch (IOException e) {
      throw new IOException("Error writing email to " + sendFile.getPath() + ":\n" + emailText, e);
    }
    
    File newSendFile = new File(_directorySendFile, sendFile.getName());
    sendFile.renameTo(newSendFile);
  }

  public void saveErrorEmail(Exception e, EmailAddress sender, EmailAddress recipient, String subject, String message)
  throws IOException {
    repairEmailDirectories();
    
    File errorFile = File.createTempFile("err_", ".log", _directoryTmpFile);
    
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);
    
    pw.format("%s -> %s%n", sender, recipient);
    pw.println(subject);
    pw.println(message);
    e.printStackTrace(pw);
    pw.close();
    
    try {
      FileWriter fw = new FileWriter(errorFile);
      fw.write(sw.toString());
      fw.close();
    } catch (IOException e1) {
      errorFile.deleteOnExit();
      throw new IOException("Error writing error email to " + errorFile.getPath() + ":\n" + sw.toString(), e1);
    }
    
    File newErrorFile = new File(_directoryErrorFile, errorFile.getName());
    errorFile.renameTo(newErrorFile);
  }

  public void saveErrorEmail(String emailText, SendResult sendResult) throws IOException {
    repairEmailDirectories();
    
    File errorFile = File.createTempFile("err_", ".email", _directoryTmpFile);
    
    try {
      FileWriter fw = new FileWriter(errorFile);
      fw.write(sendResult.error + "\n");
      fw.write(emailText);
      fw.close();
    } catch (IOException e) {
      throw new IOException("Error writing email to " + errorFile.getPath() + ":\n" + emailText, e);
    }
    
    File newErrorFile = new File(_directoryErrorFile, errorFile.getName());
    errorFile.renameTo(newErrorFile);
  }

  public List<EmailFile> getSendEmails(int limit) {
    repairEmailDirectories();
    
    File[] sendFiles = _directorySendFile.listFiles();
    if (sendFiles == null) {
        try {
            _log.warn("Email directory is unaccessible at " + _directorySendFile.getCanonicalPath());
        } catch (IOException e1) { }
        return new ArrayList<EmailFile>(0);
    }
    
    if (limit <= 0)
      limit = sendFiles.length;
    else if (limit > sendFiles.length)
      limit = sendFiles.length;
    
    List<EmailFile> sendEmails = new ArrayList<EmailFile>(limit);
    
    byte[] buffer = new byte[1024];
    ByteArrayOutputStream baos = new ByteArrayOutputStream(buffer.length);
    
    for (int i = 0; i < limit; i++) {
      File sendFile = sendFiles[i];
      
      FileInputStream fis = null;
      try {
        try {
          fis = new FileInputStream(sendFile);
        } catch (FileNotFoundException e) {
          _log.error(e.getMessage(), e);

          if (sendFiles.length > limit)
            limit += 1;

          continue;
        }

        baos.reset();
        int readCount = 0;

        try {
          while ((readCount = fis.read(buffer)) != -1) {
            baos.write(buffer, 0, readCount);
          }
        } catch (IOException e) {
          _log.error(e.getMessage(), e);

          if (sendFiles.length > limit)
            limit += 1;

          continue;
        }
      } finally {
        if (fis != null)
          try {
            fis.close();
          } catch (IOException e) { }
      }

      String sendEmail = baos.toString();
      EmailFile ef = new EmailFile();
      ef._file = sendFile;
      ef._emailText = sendEmail;
      
      sendEmails.add(ef);
    }
    
    return sendEmails;
  }

  public void deleteEmail(EmailFile sendEmail) {
    sendEmail._file.delete();
  }

  private void repairEmailDirectories() {
    if (!_directoryRootFile.exists() && !_directoryRootFile.mkdirs() || !_directoryRootFile.isDirectory()) {
      try {
        throw new IllegalStateException("PostOffice Root Directory does not exist and could not be created at " + _directoryRootFile.getCanonicalPath());
      } catch (IOException e) { }
    }

    if (!_directorySendFile.exists() && !_directorySendFile.mkdirs() || !_directorySendFile.isDirectory()) {
      try {
        throw new IllegalArgumentException("PostOffice Send Directory does not exist and could not be created at " + _directorySendFile.getCanonicalPath());
      } catch (IOException e) { }
    }

    if (!_directoryErrorFile.exists() && !_directoryErrorFile.mkdirs() || !_directoryErrorFile.isDirectory()) {
      try {
        throw new IllegalArgumentException("PostOffice Error Directory does not exist and could not be created at " + _directoryErrorFile.getCanonicalPath());
      } catch (IOException e) { }
    }

    if (!_directoryTmpFile.exists() && !_directoryTmpFile.mkdirs() || !_directoryTmpFile.isDirectory()) {
      try {
        throw new IllegalArgumentException("PostOffice Temporary Directory does not exist and could not be created at " + _directoryTmpFile.getCanonicalPath());
      } catch (IOException e) { }
    }
  }
  
  public void setDirectoryRootPath(String directoryRootPath) {
    _directoryRootFile = new File(directoryRootPath);
    _directorySendFile = new File(_directoryRootFile, "outbox");
    _directoryErrorFile = new File(_directoryRootFile, "error");
    _directoryTmpFile = new File(_directoryRootFile, "tmp");
    repairEmailDirectories();
  }
}
