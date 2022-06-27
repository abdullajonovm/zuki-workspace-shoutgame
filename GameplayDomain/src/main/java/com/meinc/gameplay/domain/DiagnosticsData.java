package com.meinc.gameplay.domain;

import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.map.annotate.JsonSerialize;

public class DiagnosticsData 
implements Serializable
{
    private static final long serialVersionUID = 1L;

    private int _ccId;
    private List<Tuple<Date>> _dayPassesForLast2Months;
    private List<Tuple<Date>> _weekPassesForLast2Months;
    private List<Tuple<Date>> _monthPassesForLast2Months;
    private List<SubscriberCC1on1Result> _subscriberCcResults;
    
    public int getCcId()
    {
        return _ccId;
    }
    public void setCcId(int ccId)
    {
        _ccId = ccId;
    }
    public List<SubscriberCC1on1Result> getSubscriberCcResults()
    {
        return _subscriberCcResults;
    }
    public void setSubscriberCcResults(List<SubscriberCC1on1Result> subscriberCcResults)
    {
        _subscriberCcResults = subscriberCcResults;
    }
    
    @JsonSerialize(using = TupleDateJacksonJsonSerializer.class)
    public List<Tuple<Date>> getDayPassesForLast2Months()
    {
        return _dayPassesForLast2Months;
    }
    
    public void setDayPassesForLast2Months(List<Tuple<Date>> dayPassesForLast2Months)
    {
        _dayPassesForLast2Months = dayPassesForLast2Months;
    }
    
    @JsonSerialize(using = TupleDateJacksonJsonSerializer.class)
    public List<Tuple<Date>> getWeekPassesForLast2Months()
    {
        return _weekPassesForLast2Months;
    }
    public void setWeekPassesForLast2Months(List<Tuple<Date>> weekPassesForLast2Months)
    {
        _weekPassesForLast2Months = weekPassesForLast2Months;
    }
    
    @JsonSerialize(using = TupleDateJacksonJsonSerializer.class)
    public List<Tuple<Date>> getMonthPassesForLast2Months()
    {
        return _monthPassesForLast2Months;
    }
    public void setMonthPassesForLast2Months(List<Tuple<Date>> monthPassesForLast2Months)
    {
        _monthPassesForLast2Months = monthPassesForLast2Months;
    }
    
    @Override
    public String toString()
    {
        StringBuilder buf = new StringBuilder();

        buf.append("ccId: ").append(_ccId);
        buf.append("\nEvents:");
        for (SubscriberCC1on1Result eventResult : _subscriberCcResults) {
            buf.append("\n\n").append(eventResult);
        }
        
        buf.append("\n\n1 Day Passes (over last 2 months):");
        for (Tuple<Date> tuple : _dayPassesForLast2Months) {
            buf.append("\n\t").append("purchaseDate: ").append(tuple.getKey()).append(", expireDate: ").append(tuple.getVal());
        }
        
        buf.append("\n\n1 Week Passes (over last 2 months):");
        for (Tuple<Date> tuple : _weekPassesForLast2Months) {
            buf.append("\n\t").append("purchaseDate: ").append(tuple.getKey()).append(", expireDate: ").append(tuple.getVal());
        }
        
        buf.append("\n\n1 Month Passes (over last 2 months):");
        for (Tuple<Date> tuple : _monthPassesForLast2Months) {
            buf.append("\n\t").append("purchaseDate: ").append(tuple.getKey()).append(", expireDate: ").append(tuple.getVal());
        }

        return buf.toString();
    }
    
    //TODO: i don't like this here ... it's duplicated
    private static final String ISO8601Str =  "yyyy-MM-dd'T'HH:mm:ssZ";
    public static String dateToIso8601(Date date)
    {
        if (date == null)
            return "";
        String result = new SimpleDateFormat(ISO8601Str).format(date);

        result = result.substring(0, result.length() - 2) + ":" + result.substring(result.length() - 2);

        return result;
    }
    
    static class TupleDateJacksonJsonSerializer extends JsonSerializer<List<Tuple<Date>>>
    {
        @Override
        public void serialize(List<Tuple<Date>> data, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) 
        throws IOException, JsonGenerationException
        {
            jsonGenerator.writeStartArray();
            
            for (Tuple<Date> tupleDate : data) {
                jsonGenerator.writeStartObject();
                jsonGenerator.writeFieldName("purchaseDate");
                jsonGenerator.writeString(dateToIso8601(tupleDate.getKey()));
                jsonGenerator.writeFieldName("expireDate");
                jsonGenerator.writeString(dateToIso8601(tupleDate.getVal()));
                jsonGenerator.writeEndObject();
            }
            
            jsonGenerator.writeEndArray();
        }
    }
}
