package com.meinc.commons.helper;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ConcurrentDateFormatHelper {
    
    public static IConcurrentDateFormat getFormatterForFormat(final String dateFormat) {
        return new IConcurrentDateFormat() {
            private ThreadLocal<DateFormat> df = new ThreadLocal<DateFormat> () {

                @Override
                public DateFormat get() {
                    return super.get();
                }

                @Override
                protected DateFormat initialValue() {
                    return new SimpleDateFormat(dateFormat);
                }

                @Override
                public void remove() {
                    super.remove();
                }

                @Override
                public void set(DateFormat value) {
                    super.set(value);
                }

            };


            @Override
            public Date fromString(String date) {
                Date d = null;
                try {
                    if (date != null) {
                        d = df.get().parse(date);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return d;
            }


            @Override
            public String toString(Date date) {
                if (date == null) {
                    return null;
                }
                return df.get().format(date);
            }
        };
    }
    
    public static IConcurrentDateFormat ISO_8601_FORMAT = getFormatterForFormat("yyyy-MM-dd'T'HH:mm:ssZ");

}
