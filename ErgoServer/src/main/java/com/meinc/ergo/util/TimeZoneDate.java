package com.meinc.ergo.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * In Java, a {@link Date} object represents, <u>in the UTC time zone</u>, the
 * number of milliseconds since the epoch. This class extends that concept by
 * representing, <u>in the specified time zone</u>, the number of milliseconds
 * since the epoch. The behavior of this class differs from {@link Calendar} in
 * that Calendar provides the illusion of changing time zones by providing
 * different views of the same underlying UTC-based Date object. This class
 * actually changes the value of the underlying Date object as the time zone
 * changes.
 * 
 * It also provides a method to convert the number of milliseconds since the
 * epoch to another time zone.
 * 
 * @author mpontius
 */
public class TimeZoneDate extends Date {
    private static final long serialVersionUID = 1L;
    
    private TimeZone timezone;
    
    /**
     * Copies another date instance, interpreting it to represent the number of
     * milliseconds since the epoch in the specified time zone. Note that the
     * specified Date <em>is not</em> changed into the specified time zone.
     * Rather the Date is interpreted to already exist in the specified time
     * zone.
     */
    public TimeZoneDate(Date date, TimeZone timezone) {
        super(date.getTime());
        if (timezone == null)
            throw new NullPointerException();
        this.timezone = timezone;
    }

    /**
     * Shifts this instance's date by the current time difference between this
     * instance's time zone and the provided time zone.
     * <p/>
     * For example shifting an instance with the state of 2:30 MST (GMT-7) to
     * UTC (GMT-0) results in 9:30 UTC.
     * 
     * @param source
     * @param timezoneFrom
     * @param toTimezone
     * @return
     */
    public void shiftTo(TimeZone toTimezone) {
        DateFormat dateFormatter = DateFormat.getDateTimeInstance();
        dateFormatter.setTimeZone(toTimezone);
        String sourceUtcString = dateFormatter.format(this);
        dateFormatter.setTimeZone(timezone);
        Date shifted;
        try {
            shifted = dateFormatter.parse(sourceUtcString);
        } catch (ParseException e) {
            throw new IllegalStateException(e);
        }
        setTime(shifted.getTime());
        timezone = toTimezone;
    }
    
    public void setDate(Date date) {
        setTime(date.getTime());
    }

    public TimeZone getTimezone() {
        return timezone;
    }

    public void setTimezone(TimeZone timezone) {
        if (timezone == null)
            throw new NullPointerException();
        this.timezone = timezone;
    }
}