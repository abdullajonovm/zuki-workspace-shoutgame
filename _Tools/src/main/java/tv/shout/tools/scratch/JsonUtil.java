package tv.shout.tools.scratch;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonUtil
{
    private static ObjectMapper _objectMapper;

    /**
     * @return an ObjectMapper that will return dates in the this ISO8601 format: yyyy-MM-dd'T'hh:mm:ss.SSSX
     */
    public static ObjectMapper getObjectMapper()
    {
        if (_objectMapper == null) {
            _objectMapper = new ObjectMapper();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX");
            dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            _objectMapper.setDateFormat(dateFormat);
        }
        return _objectMapper;
    }
}
