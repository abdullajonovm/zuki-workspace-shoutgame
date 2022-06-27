package com.meinc.commons.postoffice.smtp;

import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.security.GeneralSecurityException;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.amazonaws.util.StringInputStream;
import com.meinc.commons.postoffice.sender.IEmailSender;
import com.meinc.commons.postoffice.service.Attachment;
import com.meinc.commons.postoffice.service.Email;
import com.sun.mail.smtp.SMTPMessage;
import com.sun.mail.util.MailSSLSocketFactory;

public class SmtpSender implements IEmailSender
{
    private static final Log _log = LogFactory.getLog(SmtpSender.class);

    private String _emailServerHostname;
    private String _emailOriginHostname;
    private String _emailServerUsername;
    private String _emailServerPassword;
    private Session _session;

    @Override
    public SendResult send(String emailString)
    {
        // _log.error("SMTP Sending: " + emailString);

        SMTPMessage msg;
        SendResult r;

        try {
            msg = new SMTPMessage(_session, new StringInputStream(emailString));
        } catch (UnsupportedEncodingException e) {
            _log.error(e.getMessage(), e);
            r = SendResult.ERROR;
            r.error = e.getMessage();
            return r;
        } catch (MessagingException e) {
            _log.error(e.getMessage(), e);
            r = SendResult.ERROR;
            r.error = e.getMessage();
            return r;
        }
        try {
            Transport.send(msg);
        } catch (MessagingException e) {
            if (e.getCause() != null && e.getCause() instanceof ConnectException) {
                _log.warn("Could not connect to mail server - retrying: " + e.getMessage() + " | "
                        + e.getCause().getMessage());
                r = SendResult.TRY_AGAIN_LATER;
                r.error = e.getMessage();
            } else {
                _log.error(e.getMessage(), e);
                r = SendResult.ERROR;
                r.error = e.getMessage();
            }
            return r;
        }

        r = SendResult.SUCCESS;
        return r;
    }

    @Override
    public void sendEmailWithAttachment(Email email, Attachment attachment)
    {
        throw new UnsupportedOperationException();
    }

    public void setEmailServerHostname(String emailServerHostname)
    {
        _emailServerHostname = emailServerHostname;

        Properties props = new Properties();
        props.put("mail.smtp.host", _emailServerHostname);
        props.put("mail.smtp.localhost", _emailOriginHostname);

        if (_emailServerUsername != null && _emailServerPassword != null) {
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");

            try {
                MailSSLSocketFactory socketFactory = new MailSSLSocketFactory();
                socketFactory.setTrustedHosts(new String[] { _emailServerHostname });
                props.put("mail.smtp.ssl.socketFactory", socketFactory);
            } catch (GeneralSecurityException e) {
                _log.error(e);
            }

            Authenticator authenticator = new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication()
                {
                    return new PasswordAuthentication(_emailServerUsername, _emailServerPassword);
                }
            };

            _session = Session.getInstance(props, authenticator);
        } else {
            _session = Session.getInstance(props);
        }

        _session.setDebug(false);
    }

    public void setEmailOriginHostname(String emailOriginHostname)
    {
        _emailOriginHostname = emailOriginHostname;
    }

    public void setEmailServerUsername(String emailServerUsername)
    {
        _emailServerUsername = emailServerUsername;
    }

    public void setEmailServerPassword(String emailServerPassword)
    {
        _emailServerPassword = emailServerPassword;
    }
}
