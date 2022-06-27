package com.meinc.ergo.util;

import java.io.IOException;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;

import com.meinc.ergo.domain.Color;

//http://stackoverflow.com/questions/7766791/serializing-enums-with-jackson
public class ColorSerializer 
extends JsonSerializer<Color>
{
    @Override
    public void serialize(Color value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException
    {
//        jgen.writeStartObject();
//        jgen.writeFieldName("name");
//        jgen.writeString(value.getName());
//        jgen.writeFieldName("color");
//        jgen.writeNumber(value.getColor());
//        jgen.writeFieldName("hex");
//        jgen.writeString(value.getHex());
//        jgen.writeEndObject();
        
        jgen.writeNumber(value.getColor());
    }

}
