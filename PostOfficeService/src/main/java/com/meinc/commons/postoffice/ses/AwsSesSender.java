package com.meinc.commons.postoffice.ses;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.Properties;
import java.util.stream.Collectors;

import javax.activation.DataHandler;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClient;
import com.amazonaws.services.simpleemail.model.RawMessage;
import com.amazonaws.services.simpleemail.model.SendRawEmailRequest;
import com.meinc.commons.postoffice.sender.IEmailSender;
import com.meinc.commons.postoffice.service.Attachment;
import com.meinc.commons.postoffice.service.Email;
import com.meinc.commons.postoffice.service.EmailAddress;

public class AwsSesSender implements IEmailSender
{
    //private static final Log _log = LogFactory.getLog(AwsSesSender.class);
    public static final String AWS_SES_HOSTNAME = "amazonses";

    private String _awsAccessKeyId;
    private String _awsAccessKey;
    private AmazonSimpleEmailServiceClient _awsSesClient;

    @Override
    public SendResult send(String emailString)
    {
        // _log.error("SES Sending: " + emailString);
        SendResult result;

        // Call Amazon SES to send the message
        try {
            RawMessage rm = new RawMessage(ByteBuffer.wrap(emailString.getBytes()));
            _awsSesClient.sendRawEmail(new SendRawEmailRequest().withRawMessage(rm));

        } catch (AmazonServiceException e) {
            String errCode = e.getErrorCode();
            if ("ServiceUnavailable".equals(errCode) || "Throttling".equals(errCode)) {
                result = SendResult.TRY_AGAIN_LATER;
                result.error = errCode;
                return result;
            } else {
                result = SendResult.ERROR;
                result.error = errCode;
                return result;
            }
        }

        result = SendResult.SUCCESS;
        return result;
    }

    @Override
    public void sendEmailWithAttachment(Email email, Attachment attachment)
    throws MessagingException, IOException
    {
        MimeMessage message = new MimeMessage(Session.getDefaultInstance(new Properties()));

        //output the data into the message
        message.setSubject(email.getSubject(), "UTF-8");
        message.setFrom(toInternetAddress(email.getFrom()));
        message.setRecipients(Message.RecipientType.TO,
            email.getTo().stream()
                .map(e ->toInternetAddress(e))
                .collect(Collectors.toList())
                .toArray(new InternetAddress[email.getTo().size()])
        );

        //set up any body parts (in this case, just one: a text body part. but could also have an html part, etc)
        MimeMultipart msg_body = new MimeMultipart("alternative");
        MimeBodyPart wrap = new MimeBodyPart();
        MimeBodyPart textPart = new MimeBodyPart();
        textPart.setContent(email.getMessage().getMessage(), "text/plain; charset=UTF-8");
        msg_body.addBodyPart(textPart);
        wrap.setContent(msg_body);

        //do the attachment
        MimeMultipart msg = new MimeMultipart("mixed");
        message.setContent(msg);
        msg.addBodyPart(wrap);

        MimeBodyPart attachmentPart = new MimeBodyPart();
        attachmentPart.setDataHandler(new DataHandler(attachment.getDatasource()));
        attachmentPart.setFileName(attachment.getFilename());
        msg.addBodyPart(attachmentPart);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        message.writeTo(outputStream);

        //send
        RawMessage rawMessage = new RawMessage(ByteBuffer.wrap(outputStream.toByteArray()));
        SendRawEmailRequest rawEmailRequest = new SendRawEmailRequest().withRawMessage(rawMessage);
        _awsSesClient.sendRawEmail(rawEmailRequest);
    }

    private InternetAddress toInternetAddress(EmailAddress emailAddress)
    {
        try {
            return new InternetAddress(emailAddress.address, emailAddress.name);
        } catch (UnsupportedEncodingException ignored) {
            return null;
        }
    }

    public void setAwsAccessKeyId(String awsAccessKeyId)
    {
        _awsAccessKeyId = awsAccessKeyId;
        configAwsClient();
    }

    public void setAwsAccessKey(String awsAccessKey)
    {
        _awsAccessKey = awsAccessKey;
        configAwsClient();
    }

    private void configAwsClient()
    {
        if (_awsAccessKey != null && _awsAccessKeyId != null)
            _awsSesClient = new AmazonSimpleEmailServiceClient(new BasicAWSCredentials(_awsAccessKeyId, _awsAccessKey));
    }
}
