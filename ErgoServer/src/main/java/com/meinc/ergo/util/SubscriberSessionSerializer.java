package com.meinc.ergo.util;

import java.io.IOException;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;

import com.meinc.ergo.domain.Subscriber;
import com.meinc.ergo.domain.SubscriberSession;

public class SubscriberSessionSerializer 
extends JsonSerializer<SubscriberSession>
{
    @Override
    public void serialize(SubscriberSession ss, JsonGenerator jgen, SerializerProvider provider) 
    throws IOException, JsonProcessingException
    {
        Subscriber s = ss.getSubscriber();
        
        //jgen.writeString("{");
        jgen.writeStartObject();
        jgen.writeStringField("subscriberId", s.getUuid());
        jgen.writeStringField("sessionKey", ss.getSessionKey());
        jgen.writeStringField("email", s.getEmail());
        //jgen.writeStringField("fbId", s.getFbId());
        jgen.writeStringField("payment_status", s.getPaymentState().name());
        jgen.writeStringField("account_level", s.getState().name());
        if (s.getStateExpirationDate() != null)
            jgen.writeNumberField("account_level_expiration_date", s.getStateExpirationDate().getTime());
        jgen.writeBooleanField("featureTester", s.isFeatureTester());
        jgen.writeNumberField("createDate", s.getCreateDate().getTime());
        jgen.writeNumberField("lastUpdate", s.getLastUpdate().getTime());
        //jgen.writeString("}");
        jgen.writeEndObject();
    }

}
