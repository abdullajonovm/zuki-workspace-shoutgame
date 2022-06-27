package com.meinc.commons.postoffice.shout;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.amazonaws.util.StringInputStream;
import com.meinc.commons.postoffice.sender.IEmailSender;
import com.meinc.commons.postoffice.service.Attachment;
import com.meinc.commons.postoffice.service.Email;
import com.meinc.launcher.serverprops.ServerPropertyHolder;
import com.sun.mail.smtp.SMTPMessage;

public class ShoutHttpToSmtpSender implements IEmailSender {
    private static final Log _log = LogFactory.getLog(ShoutHttpToSmtpSender.class);

    private String _shoutHttpToSmtpHostname = ServerPropertyHolder.getProperty("shout.http.to.smtp.hostname");

    @Override
    public SendResult send(String emailString) {
        //_log.error("SES Sending: " + emailString);
        SendResult r;

        String from;
        String to;
        try {
            SMTPMessage smtpMessage = new SMTPMessage(Session.getDefaultInstance(new Properties()), new StringInputStream(emailString));
            Address[] fromAddresses = smtpMessage.getFrom();
            from = fromAddresses[0].toString();
            //StringBuffer fromBuffer = new StringBuffer();
            //for (Address fromAddress : fromAddresses)
            //    fromBuffer.append(fromAddress.toString()).append(",");
            //from = fromBuffer.substring(0, fromBuffer.length()-1);

            Address[] toAddresses = smtpMessage.getRecipients(RecipientType.TO);
            StringBuffer toBuffer = new StringBuffer();
            for (Address toAddress : toAddresses)
                toBuffer.append(toAddress.toString()).append(",");
            to = toBuffer.substring(0, toBuffer.length()-1);
        } catch (UnsupportedEncodingException e) {
            _log.error(e.getMessage(), e);
            return SendResult.ERROR;
        } catch (MessagingException e) {
            _log.error(e.getMessage(), e);
            return SendResult.ERROR;
        }

        //_log.info(">>HOST>>"+_shoutHttpToSmtpHostname);
        //_log.info(">>FROM>>"+from);
        //_log.info(">>TO>>"+to);
        //_log.info(">>MESSAGE>>"+emailString);

        Map<String,Object> params = new LinkedHashMap<String,Object>();
        params.put("FROM", from);
        params.put("TO", to);
        params.put("MESSAGE", emailString);

        // Build HTTP post body
        StringBuilder postData = new StringBuilder();
        for (Map.Entry<String,Object> param : params.entrySet()) {
            if (postData.length() != 0) postData.append('&');
            try {
                postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
                postData.append('=');
                postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                _log.error(e.getMessage(), e);
                r = SendResult.ERROR;
                r.error = e.getMessage();
                return r;
            }
        }
        byte[] postDataBytes;
        try {
            postDataBytes = postData.toString().getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            _log.error(e.getMessage(), e);
            r = SendResult.ERROR;
            r.error = e.getMessage();
            return r;
        }

        URL url;
        try {
            url = new URL("https://"+_shoutHttpToSmtpHostname+"/SMTP");
        } catch (MalformedURLException e) {
            _log.error("Invalid Shout-SMTP HTTP URL: " + _shoutHttpToSmtpHostname, e);
            r = SendResult.ERROR;
            r.error = e.getMessage();
            return r;
        }

        HttpsURLConnection conn;
        try {
            conn = (HttpsURLConnection) url.openConnection();
        } catch (IOException e) {
            _log.error(e.getMessage(), e);
            r = SendResult.ERROR;
            r.error = e.getMessage();
            return r;
        }

        SSLContext sc;
        try {
            sc = SSLContext.getInstance("TLS");
        } catch (NoSuchAlgorithmException e) {
            _log.error(e.getMessage(), e);
            r = SendResult.ERROR;
            r.error = e.getMessage();
            return r;
        }
        try {
            sc.init(null, null, new java.security.SecureRandom());
        } catch (KeyManagementException e) {
            _log.error(e.getMessage(), e);
            r = SendResult.ERROR;
            r.error = e.getMessage();
            return r;
        }
        conn.setSSLSocketFactory(sc.getSocketFactory());

        try {
            conn.setRequestMethod("POST");
        } catch (ProtocolException e) {
            _log.error(e.getMessage(), e);
            r = SendResult.ERROR;
            r.error = e.getMessage();
            return r;
        }
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
        conn.setRequestProperty("X-SMTP-KEY", "a48b15ac-f583-42e8-a2e1-fa5883e1914f");
        conn.setDoOutput(true);
        conn.setUseCaches(false);
        try {
            conn.getOutputStream().write(postDataBytes);
        } catch (IOException e) {
            _log.error(e.getMessage(), e);
            r = SendResult.ERROR;
            r.error = e.getMessage();
            return r;
        }

        try {
            char[] inChars = new char[1024];
            InputStreamReader connInReader = new InputStreamReader(conn.getInputStream(), "UTF-8");
            StringBuffer inBuff = new StringBuffer();
            int charsRead;
            while ((charsRead = connInReader.read(inChars)) != -1)
                inBuff.append(inChars, 0, charsRead);
            _log.info(">>RESPONSE>>"+inBuff.toString());
        } catch (UnsupportedEncodingException e) {
            _log.error(e.getMessage(), e);
            r = SendResult.ERROR;
            r.error = e.getMessage();
            return r;
        } catch (IOException e) {
            _log.error(e.getMessage(), e);
            r = SendResult.ERROR;
            r.error = e.getMessage();
            return r;
        }

        return SendResult.SUCCESS;
    }

    @Override
    public void sendEmailWithAttachment(Email email, Attachment attachment)
    {
        throw new UnsupportedOperationException();
    }
}