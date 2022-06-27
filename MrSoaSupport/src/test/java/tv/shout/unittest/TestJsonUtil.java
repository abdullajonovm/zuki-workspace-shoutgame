package tv.shout.unittest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Calendar;
import java.util.Date;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import tv.shout.util.JsonUtil;

public class TestJsonUtil
{
    @Test
    public void testGetObjectMapper()
    {
        Calendar c = Calendar.getInstance();
        c.set(2010, Calendar.MAY, 24, 16, 32, 56);
        c.set(Calendar.MILLISECOND, 101);

        Date specificDate = c.getTime();

        ObjectMapper mapper = JsonUtil.getObjectMapper();
        assertNotNull(mapper);

        String jsonDateStr = null;
        try {
            jsonDateStr = mapper.writeValueAsString(specificDate);
        } catch (JsonProcessingException e) {
            fail(e);
        }
        assertEquals("\"2010-05-24T22:32:56.101Z\"", jsonDateStr);
    }
}
