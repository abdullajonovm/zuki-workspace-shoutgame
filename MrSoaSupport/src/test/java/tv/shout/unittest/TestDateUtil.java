package tv.shout.unittest;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.ZoneId;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.Test;

import tv.shout.util.DateUtil;

public class TestDateUtil
{
    @Test
    public void testGetAge()
    {
        //set date to be today (tminus 5 years)
        Calendar c = Calendar.getInstance();
        c.add(Calendar.YEAR, -5);
        Date today = c.getTime();

        //set date to be one day before today (tminus 5 years)
        Calendar c2 = Calendar.getInstance();
        c2.add(Calendar.YEAR, -5);
        c2.add(Calendar.DAY_OF_MONTH, -1);
        Date yesterday = c2.getTime();

        //set date to be one day after today (tminus 5 years)
        Calendar c3 = Calendar.getInstance();
        c3.add(Calendar.YEAR, -5);
        c3.add(Calendar.DAY_OF_MONTH, 1);
        Date tomorrow = c3.getTime();

        int ageYesterdayMinus5Years = DateUtil.getAge(yesterday);
        assertEquals(5, ageYesterdayMinus5Years);

        int ageTodayMinus5Years = DateUtil.getAge(today);
        assertEquals(5, ageTodayMinus5Years);

        int ageTomorrowMinus5Years = DateUtil.getAge(tomorrow);
        assertEquals(4, ageTomorrowMinus5Years);
    }

    @Test
    public void dateToIso8601()
    {
        Calendar c = Calendar.getInstance();
        c.set(2010, Calendar.MAY, 24, 16, 32, 56);
        c.set(Calendar.MILLISECOND, 101);

        Date specificDate = c.getTime();
        String isoDateStr = DateUtil.dateToIso8601(specificDate);
        assertEquals("2010-05-24T22:32:56.101Z", isoDateStr);
    }

    @Test
    public void testIso8601ToDate()
    {
        //need to set _currentZoneOffset so this will work in whatever time zone the test is run in
        DateUtil.setTimezoneId(ZoneId.of("America/Denver"));

        List<String> isoDateStrList = Arrays.asList(
            "2017-06-25T07:09:08.101-06:00",
            "2017-06-25T07:09:08-06:00",
            "2017-06-25T07:09-06:00",
            "2017-06-25T07:09:08.101-0600",
            "2017-06-25T07:09:08-0600",
            "2017-06-25T07:09-0600",
            "2017-06-25T07:09:08.101Z",
            "2017-06-25T07:09:08Z",
            "2017-06-25T07:09Z"
        );

        List<Long> expectedOutputs = Arrays.asList(
            1498399748101L,
            1498399748000L,
            1498399740000L,
            1498399748101L,
            1498399748000L,
            1498399740000L,
            1498374548101L,
            1498374548000L,
            1498374540000L
        );

        for (int i=0; i<isoDateStrList.size(); i++) {
            Date date = DateUtil.iso8601ToDate(isoDateStrList.get(i));
            assertEquals(expectedOutputs.get(i), date.getTime());
        }
    }
}
