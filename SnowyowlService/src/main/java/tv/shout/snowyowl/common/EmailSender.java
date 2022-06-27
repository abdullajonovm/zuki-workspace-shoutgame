package tv.shout.snowyowl.common;

import org.apache.log4j.Logger;

import com.meinc.commons.postoffice.exception.PostOfficeException;
import com.meinc.commons.postoffice.service.Email;
import com.meinc.commons.postoffice.service.EmailAddress;
import com.meinc.commons.postoffice.service.EmailMessage;
import com.meinc.commons.postoffice.service.EmailPurpose;
import com.meinc.commons.postoffice.service.IPostOffice;
import com.meinc.identity.domain.Subscriber;
import com.meinc.identity.service.IIdentityService;

import tv.shout.util.StringUtil;

public interface EmailSender
{
    default void sendEmail(
        Logger logger, long subscriberId, IIdentityService identityService, IPostOffice postOfficeService,
        EmailAddress fromEmail, String emailSubject, String emailMessage, String attachmentData, String attachmentFilename)
    {
        Subscriber subscriber = identityService.getSubscriberById(subscriberId);

        String fullname, firstname, lastname;
        if (StringUtil.isEmpty(subscriber.getFirstname()) || "NYI".equals(subscriber.getFirstname())) {
            firstname = subscriber.getNickname();
        } else {
            firstname = subscriber.getFirstname();
        }
        if (StringUtil.isEmpty(subscriber.getLastname()) || "NYI".equals(subscriber.getLastname())) {
            lastname = "";
        } else {
            lastname = subscriber.getLastname();
        }
        fullname = firstname + " " + lastname;

        if (fromEmail == null) {
            fromEmail = new EmailAddress("support@shout.tv", "");
        }

        EmailAddress toEmail = new EmailAddress(subscriber.getEmail(), fullname);

        sendEmail(logger, toEmail, identityService, postOfficeService, fromEmail, emailSubject, emailMessage, attachmentData, attachmentFilename);
    }

    default void sendEmail(
            Logger logger, EmailAddress toEmail, IIdentityService identityService, IPostOffice postOfficeService,
            EmailAddress fromEmail, String emailSubject, String emailMessage, String attachmentData, String attachmentFilename)
        {
            Email email = new Email(
                EmailPurpose.TRANSACTIONAL,
                fromEmail,
                toEmail,
                emailSubject,
                new EmailMessage(emailMessage)
            );

            try {
                if (attachmentData == null) {
                    postOfficeService.sendEmail(email);
                } else {
                    postOfficeService.sendEmailWithAttachment(email, attachmentData, attachmentFilename);
                }
            } catch (PostOfficeException e) {
                logger.error("unable to send email to: " + toEmail.name, e);
            }
        }

}
