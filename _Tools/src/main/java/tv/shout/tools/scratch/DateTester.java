package tv.shout.tools.scratch;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class DateTester
{
    private static ZoneOffset _currentZoneOffset;

    /**
     * Convert a string in ISO8601 format into a java.util.Date object.
     * <br/><br/>
     * Tested with the following date formats:
     * <ul>
     *   <li>2017-06-25T07:09:08.101-06:00</li>
     *   <li>2017-06-25T07:09:08-06:00</li>
     *   <li>2017-06-25T07:09-06:00</li>
     *   <li>2017-06-25T07:09:08.101-0600</li>
     *   <li>2017-06-25T07:09:08-0600</li>
     *   <li>2017-06-25T07:09-0600</li>
     *   <li>2017-06-25T07:09:08.101Z</li>
     *   <li>2017-06-25T07:09:08Z</li>
     *   <li>2017-06-25T07:09Z</li>
     * </ul>
     *
     * @return a java.util.Date
     * @throws java.time.format.DateTimeParseException
     */
    public static Date iso8601ToDate(String input)
    //throws ParseException
    {
        //get the current timezone offset (single initialization)
        if (_currentZoneOffset == null) {
            //https://stackoverflow.com/questions/32626733/is-there-any-way-to-convert-zoneid-to-zoneoffset-in-java-8
            //see answer by Stanislav Bashkyrtsev
            Instant instant = Instant.now(); //can be LocalDateTime
            ZoneId systemZone = ZoneId.systemDefault(); // my timezone
            _currentZoneOffset = systemZone.getRules().getOffset(instant);
        }

        //sanity check against obviously bad data
        if (input == null || input.trim().length() == 0 || input.trim().toLowerCase().equals("null") || input.length() < 6)
            return null;

        //if a time offset is provided, make sure it has a colon between hours and minutes
        if (!input.endsWith("Z")) {
            char c = input.charAt(input.length()-5);
            if (c == '+' || c == '-') {
                input = input.substring(0, input.length() - 2) + ":" + input.substring(input.length() - 2);
            }
        }

        DateTimeFormatter format = DateTimeFormatter.ISO_DATE_TIME;
        LocalDateTime date = LocalDateTime.parse(input, format);
        return Date.from(date.toInstant(input.endsWith("Z") ? ZoneOffset.UTC : _currentZoneOffset));
    }

    /**
     *
     * @param date the date to convert
     * @return an ISO8601 formatted string using the browser default format of:
     *  <pre>yyyy-MM-DD'T'hh:mm:ss.SSS'Z'</pre>
     */
    public static String dateToIso8601(Date date)
    {
        if (date == null) return null;

        ObjectMapper mapper = JsonUtil.getObjectMapper();
        String val;
        try {
            val = mapper.writeValueAsString(date);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return val.replace("\"", "");
    }

    public static void main(String[] args)
    {
        System.out.println(DateTester.iso8601ToDate("2017-06-25T07:09:08.101-06:00"));
        System.out.println(DateTester.iso8601ToDate("2017-06-25T07:09:08-06:00"));
        System.out.println(DateTester.iso8601ToDate("2017-06-25T07:09-06:00"));

        System.out.println(DateTester.iso8601ToDate("2017-06-25T07:09:08.101-0600"));
        System.out.println(DateTester.iso8601ToDate("2017-06-25T07:09:08-0600"));
        System.out.println(DateTester.iso8601ToDate("2017-06-25T07:09-0600"));

        System.out.println(DateTester.iso8601ToDate("2017-06-25T07:09:08.101Z"));
        System.out.println(DateTester.iso8601ToDate("2017-06-25T07:09:08Z"));
        System.out.println(DateTester.iso8601ToDate("2017-06-25T07:09Z"));

        System.out.println(DateTester.dateToIso8601(new Date()));
        System.out.println(DateTester.iso8601ToDate("2017-06-25T14:00:32.345Z"));

        System.out.println(DateTester.iso8601ToDate("2017-06-22T18:00:07.981Z"));
    }
}
