package com.meinc.commons.postoffice.service;

import static org.springframework.transaction.annotation.Propagation.NESTED;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.mail.MessagingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.transaction.annotation.Transactional;

import com.meinc.commons.postoffice.dao.IPostOfficeDaoMapper;
import com.meinc.commons.postoffice.directory.PostOfficeDirectory;
import com.meinc.commons.postoffice.exception.EmailAddressException;
import com.meinc.commons.postoffice.exception.EmailException;
import com.meinc.commons.postoffice.exception.PostOfficeException;
import com.meinc.commons.postoffice.exception.SignException;
import com.meinc.commons.postoffice.sender.EmailSenderThread;
import com.meinc.commons.postoffice.sender.IEmailSender;
import com.meinc.commons.postoffice.ses.AwsSesSender;
import com.meinc.commons.postoffice.smtp.SmtpEmailGenerator;
import com.meinc.http.domain.HttpRequest;
import com.meinc.http.domain.HttpResponse;
import com.meinc.launcher.serverprops.ServerPropertyHolder;
import com.meinc.mrsoa.service.ServiceEndpoint;
import com.meinc.mrsoa.service.ServiceMessage;
import com.meinc.mrsoa.service.annotation.OnService;
import com.meinc.mrsoa.service.annotation.OnStart;
import com.meinc.mrsoa.service.annotation.OnStop;
import com.meinc.mrsoa.service.annotation.Service;
import com.meinc.mrsoa.service.annotation.ServiceMethod;

import clientproxy.encryptionservice.EncryptionServiceClientProxy;
import clientproxy.epshttpconnectorservice.EpsHttpConnectorServiceClientProxy;

@Service(
    name=PostOfficeService.POSTOFFICE_SERVICE,
    interfaces=PostOfficeService.POSTOFFICE_INTERFACE,
    exposeAs=IPostOffice.class)
public class PostOfficeService
implements IPostOffice
{
    public static final String POSTOFFICE_SERVICE = "PostOfficeService";
    public static final String POSTOFFICE_INTERFACE = "IPostOfficeService";
    public static final int PREFS_EMAIL_SERVER = 17;

    private static Log _log = LogFactory.getLog(PostOfficeService.class);

    private IPostOfficeDaoMapper _dao;
    private SmtpEmailGenerator _emailGenerator;
    private PostOfficeDirectory _postOfficeDirectory;
    private EmailSenderThread _emailSenderThread;
    private String _emailServerHostname;
    private IEmailSender _awsSesSender;
    private IEmailSender _smtpSender;
    private IEmailSender _shoutHttpToSmtpSender;
    private EncryptionServiceClientProxy _encryptService = new EncryptionServiceClientProxy();
    private String _serverBaseUrl;
    private String _shoutHttpToSmtpHostname = ServerPropertyHolder.getProperty("shout.http.to.smtp.hostname");
    private String _prizesHostname = ServerPropertyHolder.getProperty("hostname.prizes");

    class EmailVerifiedCallback
    {
        ServiceEndpoint endpoint;
        String methodName;
    }
    private Map<String, EmailVerifiedCallback> _emailVerifiedCallbacks = new HashMap<String, PostOfficeService.EmailVerifiedCallback>();

    @Override
    @OnStart
    @ServiceMethod
    public void load()
    {
        if (AwsSesSender.AWS_SES_HOSTNAME.equals(_emailServerHostname))
            _emailSenderThread.setEmailSender(_awsSesSender);
        else if (_shoutHttpToSmtpHostname != null && !_shoutHttpToSmtpHostname.isEmpty())
            _emailSenderThread.setEmailSender(_shoutHttpToSmtpSender);
        else
            _emailSenderThread.setEmailSender(_smtpSender);

        _emailSenderThread.setDaemon(true);
        _emailSenderThread.start();

        ServiceEndpoint myEndpoint = new ServiceEndpoint();
        myEndpoint.setServiceName(POSTOFFICE_SERVICE);

        EpsHttpConnectorServiceClientProxy httpConnector = new EpsHttpConnectorServiceClientProxy();
        httpConnector.registerHttpCallback(myEndpoint, "doGet", "doPost", "/unsubscribe", null);
        httpConnector.registerHttpCallback(myEndpoint, "doGetVerifyEmail", "doPostVerifyEmail", "/verifyEmail", null);
    }

    @Override
    @ServiceMethod
    @OnStop(depends = @OnService(proxy = EpsHttpConnectorServiceClientProxy.class))
    public void unload()
    {
        _emailSenderThread.interrupt();
        EpsHttpConnectorServiceClientProxy httpConnector = new EpsHttpConnectorServiceClientProxy();
        httpConnector.unregisterHttpCallback("/unsubscribe");
        httpConnector.unregisterHttpCallback("/verifyEmail");
    }

    @Override
    @ServiceMethod
    public boolean registerEmailVerifiedCallback(ServiceEndpoint endpoint, String methodName)
    {
        EmailVerifiedCallback callback = new EmailVerifiedCallback();
        callback.endpoint = endpoint;
        callback.methodName = methodName;
        _emailVerifiedCallbacks.put(endpoint.getServiceName(), callback);
        return true;
    }

    @Override
    @ServiceMethod
    public boolean unregisterEmailVerifiedCallback(ServiceEndpoint endpoint)
    {
        if (endpoint == null) return false;
        EmailVerifiedCallback callback = _emailVerifiedCallbacks.remove(endpoint.getServiceName());
        return callback != null;
    }

    @Override
    @ServiceMethod
    public HttpResponse doGet(HttpRequest request)
    {
        return doPost(request);
    }

    @Override
    @ServiceMethod
    public HttpResponse doPost(HttpRequest request)
    {
        HttpResponse response = new HttpResponse();

        if (request.getFirstParameter("id") != null) {
        	// TODO: refactor me to not use the servlet session
            //request.getSession().put("id", request.getFirstParameter("id"));
            response.setRedirect("http://" + _serverBaseUrl + "/unsubscribe.html");

        } else if (request.getFirstParameter("unsubscribe") != null) {
        	// TODO: refactor me to not use the servlet session
            //String unsubscribeId = (String) request.getSession().get("id");
        	String unsubscribeId = null;
            if (unsubscribeId == null) {
                response.setRedirect("http://" + _serverBaseUrl + "/unsubscribe_problem.html");

            } else {
                String emailAddressesString = _encryptService.unencryptValue(POSTOFFICE_ENCRYPT_NAMESPACE, unsubscribeId);
                if (emailAddressesString == null) {
                    response.setRedirect("http://" + _serverBaseUrl + "/unsubscribe_problem.html");

                } else {
                    EmailAddress fromEmail = new EmailAddress();
                    EmailAddress toEmail = new EmailAddress();

                    int offset = 0;
                    offset = fromEmail.fromSerialString(emailAddressesString, offset);
                    offset = toEmail.fromSerialString(emailAddressesString, offset);

                    addEmailOptout(fromEmail.address, toEmail.address);

                    response.setRedirect("http://" + _serverBaseUrl + "/unsubscribed.html");
                }
            }
        }

        return response;
    }

    @Override
    @ServiceMethod
    public HttpResponse doGetVerifyEmail(HttpRequest request)
    {
        return doPostVerifyEmail(request);
    }

    @Override
    @ServiceMethod
    public HttpResponse doPostVerifyEmail(HttpRequest request)
    {
        if (_log.isDebugEnabled()) {
            _log.info("verifyEmail request received for: " + request.getRequestURL());
        }
        HttpResponse response = new HttpResponse();

        if (request.getFirstParameter("id") != null) {
//_log.info("id parameter: " + request.getFirstParameter("id"));
            String payload = _encryptService.unencryptValue(POSTOFFICE_ENCRYPT_NAMESPACE, request.getFirstParameter("id"));
            if (payload == null) {
                _log.warn("de-munge value is null for param: " + request.getFirstParameter("id"));
                response.setRedirect("http://" + _serverBaseUrl + "/verify_email_problem.html");
            } else {
//_log.info("payload: " + payload);
                String[] args = payload.split("\\|");
                if (args.length != 2) {
                    _log.warn("payload format invalid");
                    response.setRedirect("http://" + _serverBaseUrl + "/verify_email_problem.html");
                } else {
                    int subscriberId;
                    String email;
                    try {
                        subscriberId = Integer.parseInt(args[0]);
                        email = args[1];
                    } catch (Exception e) {
                        _log.warn("unable to parse args", e);
                        response.setRedirect("http://" + _serverBaseUrl + "/verify_email_problem.html");
                        return response;
                    }

                    if (_log.isDebugEnabled()) {
                        _log.debug(MessageFormat.format("email verified. sId: {0,number,#}, email: {1}", subscriberId, email));
                    }

                    //notify any registered callbacks
                    for (EmailVerifiedCallback callback : _emailVerifiedCallbacks.values()) {
//_log.info("sending email verified callback...");
                        ServiceMessage.send(callback.endpoint, callback.methodName, subscriberId, email);
                    }

                    response.setRedirect("http://" + _serverBaseUrl + "/verify_email_success.html");
                }
            }
        } else {
            _log.warn("id parameter not found on verifyEmail");
            response.setRedirect("http://" + _serverBaseUrl + "/verify_email_problem.html");
        }

        return response;
    }

    @Override
    @ServiceMethod
    @Transactional(propagation=NESTED)
    public void verifyEmail(int appId, long subscriberId, String emailAddress, String subject, String emailBody, String languageCode)
    throws PostOfficeException
    {
        if (_prizesHostname == null) {
            _log.error("*** hostname.prizes property not set. unable to verify email");
            throw new IllegalStateException("hostname.prizes property not set. unable to verify email");
        }

        if (_log.isDebugEnabled()) {
            _log.debug(MessageFormat.format(
                "verifyEmail. sId: {0,number,#}, emailAddress: {1}, language: {2}, appId: {3,number,#}",
                subscriberId, emailAddress, languageCode, appId));
        }

        Date oneHour = new Date(System.currentTimeMillis() + 60000 * 60);
        String payload = subscriberId + "|" + emailAddress;
        String munge = _encryptService.encryptValue(POSTOFFICE_ENCRYPT_NAMESPACE, payload, oneHour);

        TemplateEmail templatedEmail = new TemplateEmail(
            null,
            EmailPurpose.TRANSACTIONAL,
            new EmailAddress("info@shout.tv", "Shout TV"),
            new EmailAddress(emailAddress, emailAddress),
            subject);

        templatedEmail.disableSection("header");
        templatedEmail.disableSection("unsubscribe");
        templatedEmail.addVariable("htmlBody", emailBody);
        templatedEmail.addVariable("munge", munge);
        templatedEmail.addVariable("serverBaseUrl", _prizesHostname);

        templatedEmail.setTemplatePathPrefix("/email_templates");
        try {
            sendEmail(templatedEmail.getVerifyEmail(appId, languageCode));
        } catch (IOException e) {
            throw new PostOfficeException(e);
        }
    }

    @Override
    @ServiceMethod
    @Transactional(propagation=NESTED)
    public void sendEmail(Email email)
    throws PostOfficeException
    {
        EmailAddress sender = email.getFrom();
        String subject = email.getSubject();
        List<EmailAddress> recipients = email.getTo();
        EmailMessage message = email.getMessage();

        if (sender.address == null || sender.address.isEmpty())
            throw new IllegalArgumentException("Invalid sender address");

        //special case: a "throw-away" email
        if (recipients != null && recipients.size() == 1 && "nobody@nowhere.com".equals(recipients.get(0).address)) {
            _log.info("Received email for nobody@nowhere.com. Not sending. Here is the body of the email:\n\n" + message.getMessage());
            return;
        }

        Iterator<EmailAddress> recipientsIt = recipients.iterator();
        while (recipientsIt.hasNext()) {
            EmailAddress recipient = recipientsIt.next();

            if (recipient.address == null || recipient.address.isEmpty())
                throw new IllegalArgumentException("Invalid recipient address");

            if (_dao.isEmailOptedOut(sender.address, recipient.address))
                recipientsIt.remove();
        }

        if (_log.isInfoEnabled()) {
            StringBuffer recipientsString = new StringBuffer();
            for (int i = 0; i < recipients.size(); i++) {
                if (i > 0)
                    recipientsString.append(",");
                recipientsString.append(recipients.get(i).toString());
            }
            int messageLength = message.getMessage().length();
            String firstMessagePart = message.getMessage().substring(0, (messageLength < 30 ? messageLength : 30));
            _log.info(String.format("Sending email %s -> [%s] : %s : %s...", sender, recipientsString, subject, firstMessagePart));
        }

        for (EmailAddress recipient : recipients) {
            String rawEmail;
            try {
                rawEmail = _emailGenerator.createEmail(email.getPurpose(), sender, recipient, subject, message);
            } catch (EmailException e) {
                _log.error("Could not create email string: " + e.getMessage(), e);
                try {
                    _postOfficeDirectory.saveErrorEmail(e, sender, recipient, subject, message.getMessage());
                } catch (IOException e1) {
                    _log.error("Could not save error email: " + e1.getMessage(), e1);
                }
                continue;
            } catch (EmailAddressException e) {
                _log.error("Could not create email address: " + e.getMessage(), e);
                try {
                    _postOfficeDirectory.saveErrorEmail(e, sender, recipient, subject, message.getMessage());
                } catch (IOException e1) {
                    _log.error("Could not save error email: " + e1.getMessage(), e1);
                }
                continue;
            } catch (SignException e) {
                _log.error("Could not sign email: " + e.getMessage(), e);
                try {
                    _postOfficeDirectory.saveErrorEmail(e, sender, recipient, subject, message.getMessage());
                } catch (IOException e1) {
                    _log.error("Could not save error email: " + e1.getMessage(), e1);
                }
                continue;
            }

            try {
                _postOfficeDirectory.saveEmail(rawEmail);
            } catch (IOException e) {
                _log.error("Could not send email: " + e.getMessage(), e);
                try {
                    _postOfficeDirectory.saveErrorEmail(e, sender, recipient, subject, message.getMessage());
                } catch (IOException e1) {
                    _log.error("Could not save error email: " + e1.getMessage(), e1);
                }
            }
        }
    }

    @Override
    @ServiceMethod
    @Transactional(propagation=NESTED)
    public void sendTemplateEmail(TemplateEmail templateEmail, int appId, String languageCode)
    throws PostOfficeException
    {
        templateEmail.setTemplatePathPrefix("/email_templates");
        try {
            sendEmail(templateEmail.getEmail(appId, languageCode));
        } catch (IOException e) {
            throw new PostOfficeException(e);
        }
    }

    @Override
    @ServiceMethod
    @Transactional(propagation=NESTED)
    public void sendEmailWithAttachment(Email email, String attachmentData, String attachmentFilename)
    throws PostOfficeException
    {
        Attachment attachment = Attachment.fromStringSource(attachmentData, attachmentFilename);
        IEmailSender emailSender = _emailSenderThread.getEmailSender();
        try {
            emailSender.sendEmailWithAttachment(email, attachment);
        } catch (MessagingException | IOException e) {
            throw new PostOfficeException(e);
        }
    }

    @Override
    @ServiceMethod
    @Transactional(propagation = NESTED)
    public boolean isEmailOptedOut(String senderAddress, String recipientAddress)
    {
        return _dao.isEmailOptedOut(senderAddress, recipientAddress);
    }

    @Override
    @ServiceMethod
    @Transactional(propagation = NESTED)
    public boolean isEmailOptedOut(String recipientAddress)
    {
        return _dao.isEmailAnyOptedOut(recipientAddress);
    }

    @Override
    @ServiceMethod
    @Transactional(propagation = NESTED)
    public void addEmailOptout(String senderAddress, String recipientAddress)
    {
        _dao.addEmailOptout(senderAddress, recipientAddress);
    }

    @Override
    @ServiceMethod
    @Transactional(propagation = NESTED)
    public void removeEmailOptout(String senderAddress, String recipientAddress)
    {
        _dao.removeEmailOptout(senderAddress, recipientAddress);
    }

    @Override
    @ServiceMethod
    @Transactional(propagation = NESTED)
    public void removeEmailOptout(String recipientAddress)
    {
        _dao.removeAllEmailOptout(recipientAddress);
    }

    public void setDao(IPostOfficeDaoMapper dao)
    {
        _dao = dao;
    }

    public void setEmailGenerator(SmtpEmailGenerator emailGenerator)
    {
        _emailGenerator = emailGenerator;
    }

    public void setPostOfficeDirectory(PostOfficeDirectory postOfficeDirectory)
    {
        _postOfficeDirectory = postOfficeDirectory;
    }

    public void setEmailSenderThread(EmailSenderThread emailSenderThread)
    {
        _emailSenderThread = emailSenderThread;
    }

    public void setEmailServerHostname(String emailServerHostname)
    {
        _emailServerHostname = emailServerHostname;
    }

    public void setAwsSesSender(IEmailSender awsSesSender)
    {
        this._awsSesSender = awsSesSender;
    }

    public void setSmtpSender(IEmailSender smtpSender)
    {
        this._smtpSender = smtpSender;
    }

    public void setShoutHttpToSmtpSender(IEmailSender shoutHttpToSmtpSender)
    {
        this._shoutHttpToSmtpSender = shoutHttpToSmtpSender;
    }

    public void setServerBaseUrl(String serverBaseUrl)
    {
        this._serverBaseUrl = serverBaseUrl;
    }
}
