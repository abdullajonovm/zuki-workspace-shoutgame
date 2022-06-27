package tv.shout.snowyowl.common;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.apache.log4j.Logger;

import com.meinc.identity.domain.Subscriber;
import com.meinc.identity.service.IIdentityService;
import com.twilio.sdk.TwilioRestClient;
import com.twilio.sdk.TwilioRestException;
import com.twilio.sdk.resource.factory.MessageFactory;
import com.twilio.sdk.resource.instance.Message;

public interface SmsSender
{
    default String sendSms(
            String from, String to, String body,
            String twilioAccountSid, String twilioAuthToken,
            Logger logger, long subscriberId, IIdentityService identityService, boolean doVerificationCheck)
    {
        if (doVerificationCheck) {
            //make sure subscriber's phone has been verified
            Subscriber s = identityService.getSubscriberById(subscriberId);
            if (!s.isPhoneVerified()) {
                logger.warn(MessageFormat.format("NOT sending SMS to subscriber {0,number,#}. Phone# not verified.", subscriberId));
                return null;
            }
        }

        TwilioRestClient client = new TwilioRestClient(twilioAccountSid, twilioAuthToken);
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("From", from));
        params.add(new BasicNameValuePair("To", to));
        params.add(new BasicNameValuePair("Body", body));
        MessageFactory messageFactory = client.getAccount().getMessageFactory();
        Message message;
        try {
            message = messageFactory.create(params);
        } catch (TwilioRestException e) {
            logger.error("unable to send SMS",e);
            return null;
        }

        return message.getSid();
    }
}
