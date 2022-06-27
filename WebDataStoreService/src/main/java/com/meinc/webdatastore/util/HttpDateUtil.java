//
//  ========================================================================
//  Copyright (c) 1995-2013 Mort Bay Consulting Pty. Ltd.
//  ------------------------------------------------------------------------
//  All rights reserved. This program and the accompanying materials
//  are made available under the terms of the Eclipse Public License v1.0
//  and Apache License v2.0 which accompanies this distribution.
//
//      The Eclipse Public License is available at
//      http://www.eclipse.org/legal/epl-v10.html
//
//      The Apache License v2.0 is available at
//      http://www.opensource.org/licenses/apache2.0.php
//
//  You may elect to redistribute this code under either of these licenses.
//  ========================================================================
//
// Source code ADAPTED from org.eclipse.jetty.http.HttpFields.DateGenerator
//
package com.meinc.webdatastore.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

public class HttpDateUtil {
    public static final TimeZone __GMT = TimeZone.getTimeZone("GMT");
    private static final String[] DAYS =
    { "Sat", "Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
    private static final String[] MONTHS =
    { "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec", "Jan"};

    /**
     * Format HTTP date "EEE, dd MMM yyyy HH:mm:ss 'GMT'" 
     */
    public static String formatRfc1123Date(long date)
    {
        StringBuilder buf = new StringBuilder(32);
        buf.setLength(0);

        GregorianCalendar gc = new GregorianCalendar(__GMT);
        gc.setTimeInMillis(date);
        
        int day_of_week = gc.get(Calendar.DAY_OF_WEEK);
        int day_of_month = gc.get(Calendar.DAY_OF_MONTH);
        int month = gc.get(Calendar.MONTH);
        int year = gc.get(Calendar.YEAR);
        int century = year / 100;
        year = year % 100;
        
        int hours = gc.get(Calendar.HOUR_OF_DAY);
        int minutes = gc.get(Calendar.MINUTE);
        int seconds = gc.get(Calendar.SECOND);

        buf.append(DAYS[day_of_week]);
        buf.append(',');
        buf.append(' ');
        append2digits(buf, day_of_month);

        buf.append(' ');
        buf.append(MONTHS[month]);
        buf.append(' ');
        append2digits(buf, century);
        append2digits(buf, year);
        
        buf.append(' ');
        append2digits(buf, hours);
        buf.append(':');
        append2digits(buf, minutes);
        buf.append(':');
        append2digits(buf, seconds);
        buf.append(" GMT");
        return buf.toString();
    }

    /* ------------------------------------------------------------ */
    /**
     * Format "EEE, dd-MMM-yy HH:mm:ss 'GMT'" for cookies
     */
    public static void formatCookieDate(StringBuilder buf, long date)
    {
        GregorianCalendar gc = new GregorianCalendar(__GMT);
        gc.setTimeInMillis(date);
        
        int day_of_week = gc.get(Calendar.DAY_OF_WEEK);
        int day_of_month = gc.get(Calendar.DAY_OF_MONTH);
        int month = gc.get(Calendar.MONTH);
        int year = gc.get(Calendar.YEAR);
        year = year % 10000;

        int epoch = (int) ((date / 1000) % (60 * 60 * 24));
        int seconds = epoch % 60;
        epoch = epoch / 60;
        int minutes = epoch % 60;
        int hours = epoch / 60;

        buf.append(DAYS[day_of_week]);
        buf.append(',');
        buf.append(' ');
        append2digits(buf, day_of_month);

        buf.append('-');
        buf.append(MONTHS[month]);
        buf.append('-');
        append2digits(buf, year/100);
        append2digits(buf, year%100);
        
        buf.append(' ');
        append2digits(buf, hours);
        buf.append(':');
        append2digits(buf, minutes);
        buf.append(':');
        append2digits(buf, seconds);
        buf.append(" GMT");
    }

    /* ------------------------------------------------------------ */
    private static void append2digits(StringBuilder buf,int i)
    {
        if (i<100)
        {
            buf.append((char)(i/10+'0'));
            buf.append((char)(i%10+'0'));
        }
    }

    private final static String httpDateFormatStrings[] =
    {
        "EEE, dd MMM yyyy HH:mm:ss zzz",
        "EEE, dd-MMM-yy HH:mm:ss",
        "EEE MMM dd HH:mm:ss yyyy",
        "EEE, dd MMM yyyy HH:mm:ss", "EEE dd MMM yyyy HH:mm:ss zzz",
        "EEE dd MMM yyyy HH:mm:ss", "EEE MMM dd yyyy HH:mm:ss zzz", "EEE MMM dd yyyy HH:mm:ss",
        "EEE MMM-dd-yyyy HH:mm:ss zzz", "EEE MMM-dd-yyyy HH:mm:ss", "dd MMM yyyy HH:mm:ss zzz",
        "dd MMM yyyy HH:mm:ss", "dd-MMM-yy HH:mm:ss zzz", "dd-MMM-yy HH:mm:ss", "MMM dd HH:mm:ss yyyy zzz",
        "MMM dd HH:mm:ss yyyy", "EEE MMM dd HH:mm:ss yyyy zzz",
        "EEE, MMM dd HH:mm:ss yyyy zzz", "EEE, MMM dd HH:mm:ss yyyy", "EEE, dd-MMM-yy HH:mm:ss zzz",
        "EEE dd-MMM-yy HH:mm:ss zzz", "EEE dd-MMM-yy HH:mm:ss",
    };
    
    private static SimpleDateFormat httpDateFormats[] = new SimpleDateFormat[httpDateFormatStrings.length];
    
    static {
        for (int i = 0; i < httpDateFormatStrings.length; i++) {
            httpDateFormats[i] = new SimpleDateFormat(httpDateFormatStrings[i], Locale.US);
            httpDateFormats[i].setTimeZone(__GMT);
        }
    }

    public static long parseWebDate(final String dateVal)
    {
        for (int i = 0; i < httpDateFormats.length; i++)
        {
            try
            {
                Date date = (Date) httpDateFormats[i].parseObject(dateVal);
                return date.getTime();
            }
            catch (java.lang.Exception e)
            {
                // LOG.ignore(e);
            }
        }
        if (dateVal.endsWith(" GMT"))
        {
            final String val = dateVal.substring(0, dateVal.length() - 4);
            for (SimpleDateFormat element : httpDateFormats)
            {
                try
                {
                    Date date = (Date) element.parseObject(val);
                    return date.getTime();
                }
                catch (java.lang.Exception e)
                {
                    // LOG.ignore(e);
                }
            }
        }
        return -1;
    }
}
