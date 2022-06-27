package test;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClient;
import com.amazonaws.services.simpleemail.model.RawMessage;
import com.amazonaws.services.simpleemail.model.SendRawEmailRequest;

public class AmazonSesSender
{
    private static final String AWS_ACCESS_KEY_ID = "AKIAJVMLF4GPLUMHTYOQ";
    private static final String AWS_ACCESSS_KEY = "WGW9KjBEBtxgD30FzhfzToIM5jMUlq8O9opi5eDn";

    public static void main(String[] args)
    throws Exception
    {
        String emailSubject = "this is the subject";
        String emailBody = "this is the body";
        String attachmentAsStr = "email,nickname,network_size,winnings\r\ndarlcmcbride@gmail.com,gobigorgohome,20,25.97\r\nshawker@me-inc.com,yarell,5,13.22\r\nbxgrant@shout.tv,bxgrant,2,5.91";
        String attachmentFilename = "affiliate_winnings-001.csv";

        //create the client sender
        AmazonSimpleEmailServiceClient client = new AmazonSimpleEmailServiceClient(new BasicAWSCredentials(AWS_ACCESS_KEY_ID, AWS_ACCESSS_KEY));
        MimeMessage message = new MimeMessage(Session.getDefaultInstance(new Properties()));

        //output the data into the message
        message.setSubject(emailSubject, "UTF-8");
        message.setFrom(new InternetAddress("info@shout.tv"));
        message.setRecipients(Message.RecipientType.TO, "shawker@shout.tv");

        //set up any body parts (in this case, just one: a text body part. but could also have an html part, etc)
        MimeMultipart msg_body = new MimeMultipart("alternative");
        MimeBodyPart wrap = new MimeBodyPart();
        MimeBodyPart textPart = new MimeBodyPart();
        textPart.setContent(emailBody, "text/plain; charset=UTF-8");
        msg_body.addBodyPart(textPart);
        wrap.setContent(msg_body);

        //do the attachment
        MimeMultipart msg = new MimeMultipart("mixed");
        message.setContent(msg);
        msg.addBodyPart(wrap);

        DataSource ds = new ByteArrayDataSource(attachmentAsStr.getBytes("UTF-8"), "application/octet-stream");
        MimeBodyPart attachmentPart = new MimeBodyPart();
        attachmentPart.setDataHandler(new DataHandler(ds));
        attachmentPart.setFileName(attachmentFilename);
        msg.addBodyPart(attachmentPart);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        message.writeTo(outputStream);

        //send
        RawMessage rawMessage = new RawMessage(ByteBuffer.wrap(outputStream.toByteArray()));
        SendRawEmailRequest rawEmailRequest = new SendRawEmailRequest().withRawMessage(rawMessage);
        client.sendRawEmail(rawEmailRequest);
    }

}
