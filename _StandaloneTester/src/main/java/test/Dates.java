package test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Dates
{
    private static final SimpleDateFormat SDF_HTTP_HEADER_DATE = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);

    public static void main(String[] args) throws ParseException
    {
        String httpHeaderLastModifiedStr = "Tue, 09 May 2017 19:58:10 GMT";
        String localLastModifiedStr = "Tue, 09 May 2017 19:58:08 GMT+00:00";

        Date httpHeaderLastModified = SDF_HTTP_HEADER_DATE.parse(httpHeaderLastModifiedStr);
        Date localLastModified = SDF_HTTP_HEADER_DATE.parse(localLastModifiedStr);

        System.out.println("header date: " + httpHeaderLastModified);
        System.out.println("local date: " + localLastModified);
        System.out.println("http after local? " + httpHeaderLastModified.after(localLastModified));
    }

}
