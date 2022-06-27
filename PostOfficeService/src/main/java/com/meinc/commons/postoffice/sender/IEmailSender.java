package com.meinc.commons.postoffice.sender;

import java.io.IOException;

import javax.mail.MessagingException;

import com.meinc.commons.postoffice.service.Attachment;
import com.meinc.commons.postoffice.service.Email;

public interface IEmailSender {

  public enum SendResult {
    SUCCESS, TRY_AGAIN_LATER, ERROR;
    public String error;
  }

  SendResult send(String emailString);
  void sendEmailWithAttachment(Email email, Attachment attachment) throws MessagingException, IOException;

}
