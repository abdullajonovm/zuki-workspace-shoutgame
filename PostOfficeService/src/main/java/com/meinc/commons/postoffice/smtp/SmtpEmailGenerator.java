package com.meinc.commons.postoffice.smtp;

import static com.meinc.commons.postoffice.service.IPostOffice.POSTOFFICE_ENCRYPT_NAMESPACE;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.googlecode.htmlcompressor.compressor.HtmlCompressor;
import com.meinc.commons.postoffice.exception.EmailAddressException;
import com.meinc.commons.postoffice.exception.EmailException;
import com.meinc.commons.postoffice.exception.SignException;
import com.meinc.commons.postoffice.service.EmailAddress;
import com.meinc.commons.postoffice.service.EmailMessage;
import com.meinc.commons.postoffice.service.EmailPurpose;

import clientproxy.encryptionservice.EncryptionServiceClientProxy;
import de.agitos.dkim.DKIMSigner;
import de.agitos.dkim.SMTPDKIMMessage;

public class SmtpEmailGenerator {

  private static Log _log = LogFactory.getLog(SmtpEmailGenerator.class);
  
  private String _dkimDomain;
  private String _dkimSelector;
  private PrivateKey _dkimKey;
  private EncryptionServiceClientProxy _encryptService = new EncryptionServiceClientProxy();
  private String _serverHostname;

  public String createEmail(EmailPurpose purpose, EmailAddress sender, EmailAddress recipient, String subject, EmailMessage emailMessage)
  throws EmailException, EmailAddressException, SignException {
    Properties props = new Properties();
    Session session = Session.getDefaultInstance(props, null);
    
    MimeMessage messageObj = new MimeMessage(session);
    
    Multipart multiPart;
    try {
      multiPart = new MimeMultipart("alternative");
      messageObj.setContent(multiPart);
    } catch (MessagingException e) {
      throw new EmailException(e);
    }
    
    InternetAddress from;
    try {
      from = new InternetAddress(sender.address, sender.name);
    } catch (UnsupportedEncodingException e) {
      throw new EmailAddressException(e);
    }
    
    try {
      messageObj.setFrom(from);
    } catch (MessagingException e) {
      throw new EmailAddressException(e);
    }
    
    try {
      messageObj.setSentDate(new Date());
    } catch (MessagingException e) {
      throw new EmailException(e);
    }
    
    if (purpose == EmailPurpose.BULK) {
      try {
        // Gmail recommends using Precedence header, but AWS SES doesn't allow
        // it. So we use X-Precedence and hope Gmail is smart enough to figure
        // it out.
        messageObj.addHeader("X-Precedence", "bulk");
      } catch (MessagingException e) {
        throw new EmailException(e);
      }
    }
    if (purpose == EmailPurpose.BULK || purpose == EmailPurpose.PROMOTIONAL) {
      try {
        messageObj.addHeader("List-Unsubscribe", sender.address);
      } catch (MessagingException e) {
        throw new EmailException(e);
      }
    }

    subject = subject.replace(EmailMessage.EMAIL_REPLACE_CODE, recipient.address);
    subject = subject.replace(EmailMessage.NAME_REPLACE_CODE, (recipient.name == null ? "" : recipient.name));
    try {
      messageObj.setSubject(MimeUtility.encodeText(subject, "UTF-8", null));
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    } catch (MessagingException e) {
      throw new EmailException(e);
    }

    String message = emailMessage.getMessage();
    
    String unsubscribeId = null;
    if (purpose == EmailPurpose.BULK || purpose == EmailPurpose.PROMOTIONAL ||
        message.contains(EmailMessage.UNSUBSCRIBE_REPLACE_CODE)) {
      String emailAddressesString = sender.toSerialString() + recipient.toSerialString();
      unsubscribeId = _encryptService.getMungedValueFromOriginalValue(POSTOFFICE_ENCRYPT_NAMESPACE, emailAddressesString);
      if (unsubscribeId == null) {
        Calendar expires = Calendar.getInstance();
        expires.add(Calendar.MONTH, 6);
        unsubscribeId = _encryptService.encryptValue(POSTOFFICE_ENCRYPT_NAMESPACE, emailAddressesString, expires.getTime());
      }
      
      if (message.contains(EmailMessage.UNSUBSCRIBE_REPLACE_CODE)) {
        message = message.replaceAll(EmailMessage.UNSUBSCRIBE_REPLACE_CODE, String.format("http://%s/eps/unsubscribe?id=%s", _serverHostname, unsubscribeId));
        
      } else {
        int closeIndex = message.indexOf("</body>");
        if (closeIndex == -1)
          closeIndex = message.indexOf("</html>");
        if (closeIndex == -1) {
          message += String.format("\n\nTo stop receiving promotional emails from Shout, " +
                                   "unsubscribe by opening this address in a web browser: " +
                                   "http://%s/eps/unsubscribe?id=%s", _serverHostname, unsubscribeId);
          
        } else {
        
          String preText = message.substring(0, closeIndex);
          String postText = message.substring(closeIndex);
          
          message = preText +
                    "<p>To stop receiving promotional emails from Shout, " +
                    String.format("<a href=\"http://%s/eps/unsubscribe?id=%s\">Unsubscribe</a>.", _serverHostname, unsubscribeId) +
                    "</p>" +
                    postText;
        }
      }
    }

    message = message.replace(EmailMessage.EMAIL_REPLACE_CODE, recipient.address);
    if (message.indexOf("</html>") != -1) {
      if (emailMessage.isCompress()) {
        HtmlCompressor compressor = new HtmlCompressor();
        compressor.setCompressCss(true);
        compressor.setRemoveIntertagSpaces(true);
        message = compressor.compress(message);
      }
      
      MimeBodyPart textPart = new MimeBodyPart();
      MimeBodyPart htmlPart = new MimeBodyPart();
      
      try {
        // MIME spec says add parts in low-to-high fidelity order
        multiPart.addBodyPart(textPart);
        multiPart.addBodyPart(htmlPart);
      } catch (MessagingException e) {
        throw new EmailException(e);
      }

      String plainTextMessage = message.replaceAll("<br/?>", "\n")
                                       .replaceAll("</?p>",  "\n")
                                       .replaceAll("<.*?>", "");
      try {
        textPart.setContent(plainTextMessage, "text/plain");
      } catch (MessagingException e) {
        throw new EmailException(e);
      }
      
      try {
        htmlPart.setContent(message, "text/html; charset=utf-8");
      } catch (MessagingException e) {
        throw new EmailException(e);
      }
    } else {
      MimeBodyPart textPart = new MimeBodyPart();
      try {
        multiPart.addBodyPart(textPart);
      } catch (MessagingException e) {
        throw new EmailException(e);
      }
      try {
        textPart.setContent(message, "text/plain");
      } catch (MessagingException e) {
        throw new EmailException(e);
      }
    }

    InternetAddress toAddress;
    try {
      toAddress = new InternetAddress(recipient.address, recipient.name);
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
    
    try {
      messageObj.setRecipient(Message.RecipientType.TO, toAddress);
    } catch (MessagingException e) {
      throw new EmailAddressException(e);
    }
    
    try {
      messageObj.saveChanges();
    } catch (MessagingException e) {
      throw new EmailException(e);
    }
    
    if (from.getAddress().endsWith("@"+_dkimDomain)) {
      // We can sign this message
      try {
        // get DKIMSigner object
        DKIMSigner dkimSigner = new DKIMSigner(_dkimDomain, _dkimSelector, _dkimKey);
        dkimSigner.setIdentity(from.getAddress());
        
        // According to Amazon SES Documentation, these headers cannot be signed
        dkimSigner.removeHeaderToSign("Message-ID");
        dkimSigner.removeHeaderToSign("Date");
        // Bug in dkimSigner, these headers aren't signed anyhow
        //dkimSigner.removeHeaderToSign("Return-Path");
        //dkimSigner.removeHeaderToSign("Bounces-To");
  
        // construct the JavaMail message using the DKIM message type from DKIM for JavaMail
        messageObj = new SMTPDKIMMessage(messageObj, dkimSigner);
      } catch (Exception e) {
        throw new SignException(e);
      }
    }
    
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try {
      messageObj.writeTo(baos);
    } catch (IOException e) {
      throw new RuntimeException(e);
    } catch (MessagingException e) {
      throw new EmailException(e);
    }
    
    return baos.toString();
  }

  public void setDkimDomain(String dkimDomain) {
    _dkimDomain = dkimDomain;
  }

  public void setDkimSelector(String dkimSelector) {
    _dkimSelector = dkimSelector;
  }

  public void setDkimKey(String dkimKey) {
    byte[] dkimKeyBytes = Base64.decodeBase64(dkimKey.getBytes());
    
    PKCS8EncodedKeySpec pkcs8Spec = new PKCS8EncodedKeySpec(dkimKeyBytes);
    
    KeyFactory keyFactory;
    try {
      keyFactory = KeyFactory.getInstance("RSA");
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    }
    
    try {
      _dkimKey = (RSAPrivateKey) keyFactory.generatePrivate(pkcs8Spec);
    } catch (InvalidKeySpecException e) {
      throw new RuntimeException(e);
    }
  }

  public void setServerHostname(String serverHostname) {
    _serverHostname = serverHostname;
  }
}
