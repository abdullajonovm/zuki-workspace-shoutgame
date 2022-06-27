package com.meinc.commons.postoffice.sender;

import java.io.IOException;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.meinc.commons.postoffice.directory.EmailFile;
import com.meinc.commons.postoffice.directory.PostOfficeDirectory;
import com.meinc.commons.postoffice.sender.IEmailSender.SendResult;

public class EmailSenderThread extends Thread {
  private static final Log _log = LogFactory.getLog(EmailSenderThread.class);

  private int _maxEmailsPerBatch;
  private int _batchIntervalMs;
  private IEmailSender _emailSender;
  private PostOfficeDirectory _postOfficeDirectory;

  @Override
  public void run() {
    int sleepAdditive = 0;
    int tryAgainCount = 0;
    while (!isInterrupted()) {
      try {
        List<EmailFile> sendEmails = _postOfficeDirectory.getSendEmails(_maxEmailsPerBatch);
        if (sendEmails.size() > 0)
          _log.info("Attempting to send " + sendEmails.size() + " emails");

        int sentEmailsCount = 0;

        for (EmailFile sendEmail : sendEmails) {
          SendResult result = _emailSender.send(sendEmail.getEmailText());

          if (result == SendResult.SUCCESS) {
            //_log.error("Sent email: " + sendEmail);
            _postOfficeDirectory.deleteEmail(sendEmail);
            sentEmailsCount += 1;
            sleepAdditive = 0;

          } else if (result == SendResult.ERROR) {
            _log.error("Could not send email: " + result.error + "\n" + sendEmail);
            try {
              _postOfficeDirectory.saveErrorEmail(sendEmail.getEmailText(), result);
            } catch (IOException e) {
              _log.error("Could not save error email", e);
            }
            _postOfficeDirectory.deleteEmail(sendEmail);

          } else if (result == SendResult.TRY_AGAIN_LATER) {
            if (++tryAgainCount >= 3) {
              if (sleepAdditive < 60 * 60 * 1000) {
                if (sleepAdditive == 0)
                  sleepAdditive = _batchIntervalMs;
                else
                  sleepAdditive *= 2;
                _log.info("Increasing email send interval to " + (_batchIntervalMs+sleepAdditive) + "ms");
              }
              break;
            }
          }
        }

        if (!sendEmails.isEmpty())
          _log.info("Sent " + sentEmailsCount + " emails");

        if (sentEmailsCount > 0)
          tryAgainCount = 0;

        sleep(_batchIntervalMs + sleepAdditive);
        sleepAdditive -= sleepAdditive / 2;
      } catch (InterruptedException e) {
        interrupt();
      } catch (Throwable e) {
        _log.error(e);
      }
    }
  }

  public void setEmailSender(IEmailSender emailSender) {
    _emailSender = emailSender;
  }

  public IEmailSender getEmailSender() { return _emailSender; }

  public void setPostOfficeDirectory(PostOfficeDirectory postOfficeDirectory) {
    _postOfficeDirectory = postOfficeDirectory;
  }

  public void setMaxEmailsPerBatch(int maxEmailsPerBatch) {
    _maxEmailsPerBatch = maxEmailsPerBatch;
  }

  public void setBatchIntervalMs(int batchIntervalMs) {
    _batchIntervalMs = batchIntervalMs;
  }
}