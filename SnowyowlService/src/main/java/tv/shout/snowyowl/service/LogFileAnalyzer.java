package tv.shout.snowyowl.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import com.meinc.commons.postoffice.service.IPostOffice;
import com.meinc.identity.service.IIdentityService;

import tv.shout.snowyowl.common.EmailSender;

public class LogFileAnalyzer
implements EmailSender
{
    private static Logger _logger = Logger.getLogger(LogFileAnalyzer.class);
    private static final String SESSION_IDENTIFIER = "Starting MrSOA Server";

    @Value("${hostname.suffix}")
    private String _servername;

    @Value("${sm.logmonitor.logfile}")
    private String _logFileName;

    @Value("${sm.logmonitor.checkintervalMs}")
    private long _checkIntervalMs;

    @Value("${sm.logmonitor.dateformat}")
    private String _logDateFormat;

    @Value("${sm.logmonitor.emailrecipient.subscriberId}")
    private long _emailRecipientSubscriberId;

    @Value("#{'${sm.logmonitor.expressions}'.split(',')}")
    private List<String> _regEx;

    @Autowired
    private IIdentityService _identityService;

    @Autowired
    private IPostOffice _postOfficeService;

    @Autowired
    private PlatformTransactionManager _transactionManager;

    private SimpleDateFormat SDF_LOGDATE;
    private LogFileAnalyzerThread _thread;
    private List<Pattern> _regExPatterns = new ArrayList<>();

    private long _mostRecentLineNum = -1;
    private Date _mostRecentTimestamp = null;

    public void start()
    {
        SDF_LOGDATE = new SimpleDateFormat(_logDateFormat);

        if (_logger.isDebugEnabled()) {
            _logger.debug("adding logfile monitor regex patterns:");
        }
        _regEx.forEach(p -> {
            if (_logger.isDebugEnabled()) {
                _logger.debug(MessageFormat.format("\t{0}", p));
            }
            _regExPatterns.add(Pattern.compile(p, Pattern.DOTALL));
        });

        _thread = new LogFileAnalyzerThread();
        _thread.setDaemon(true);
        _thread.start();
    }

    public void stop()
    {
        _thread.interrupt();
    }

    private class LogFileAnalyzerThread
    extends Thread
    {
        @Override
        public void run()
        {
            try {
                while (!isInterrupted()) {
                    processLogFile();
                    Thread.sleep(_checkIntervalMs);
                }
            } catch (InterruptedException ignored) {
            }

            System.out.println("DONE");
        }
    }

    private long getMostRecentSessionLineNum()
    {
        final AtomicReference<Long> lineNumRef = new AtomicReference<Long>(-1L);
        final AtomicReference<Long> mostRecentSessionLineNumRef = new AtomicReference<Long>(-1L);

        try (Stream<String> stream = Files.lines(Paths.get(_logFileName))) {
            stream.forEach(line -> {
                lineNumRef.set(lineNumRef.get() + 1); //increment line #
                if (line.contains(SESSION_IDENTIFIER)) {
//_logger.info(">>> getMostRecentSessionLineNum::found a new session ref at line: " + lineNumRef.get());
                    mostRecentSessionLineNumRef.set(lineNumRef.get());
                }
            });
        } catch (IOException e) {
            new IllegalStateException("unable to read file");
        }

        return mostRecentSessionLineNumRef.get().longValue();
    }

    private void processLogFile()
    {
        //if (_logger.isDebugEnabled()) {
        //    _logger.debug(MessageFormat.format("mostRecnetLineNum: {0,number,#}, mostRecentTimestamp: {1}", _mostRecentLineNum, _mostRecentTimestamp));
        //}
//_logger.info(MessageFormat.format(">>> processLogFile::mostRecnetLineNum: {0,number,#}, mostRecentTimestamp: {1}",_mostRecentLineNum, _mostRecentTimestamp));

        String firstLineWithTimestamp = null;

        //calculate one hour ago (for later use)
        Calendar c = Calendar.getInstance();
        c.add(Calendar.HOUR, -1);
        Date tMinusOneHour = c.getTime();

        //open file and read first line that has a timestamp
        Path path = Paths.get(_logFileName);
        try (Stream<String> stream = Files.lines(path)) {
            //read the first line (skipping anything that doesn't start with a timestamp) - in order to get the timestamp of the first line
            firstLineWithTimestamp = stream.
                filter(line -> startsWithTimestamp(line))
                .findFirst().orElse("01/01 00:00:00:000 INFO no data");

        } catch (IOException e) {
            new IllegalStateException("unable to read file");
        }

        //if existing timestamp is null, this is the first time since being run. start at the most recent session (or 0 if not found)
        if (_mostRecentTimestamp == null) {
            _mostRecentLineNum = Math.max(getMostRecentSessionLineNum(), 0);

        } else {
            //else if file first line timestamp > existing timestamp, the log file has rolled - start at the top
            Date firstLineTimestamp = getLineTimestamp(firstLineWithTimestamp);
            if (firstLineTimestamp.after(_mostRecentTimestamp)) {
//_logger.info(MessageFormat.format(">>>processLogFile:starting at top. log has rolled. firstLineTimestamp: {0}", firstLineTimestamp));
                _mostRecentLineNum = 0;

            } // _mostRecentLineNum has been set. just use it
//else _logger.info(">>> processLogFile:starting from last read point");
        }

        List<String> noteworthyLines = new ArrayList<>();

        //open the file at the appropriate location and begin reading each line
        try (Stream<String> stream = Files.lines(path).skip(_mostRecentLineNum)) {
            stream.forEach(line -> {
                parseLine(line, tMinusOneHour, noteworthyLines);
            });
        } catch (IOException e) {
            new IllegalStateException("unable to read file");
        }

        //if there is anything noteworthy, send an email
        if (noteworthyLines.size() > 0) {
            StringBuilder buf = new StringBuilder();
            buf.append("SERVER: ").append(_servername).append("\n\n");
            buf.append("LOGFILE: ").append(_logFileName).append("\n\n");
            buf.append("\n\n");
            noteworthyLines.forEach(line -> buf.append(line).append("\n"));

            if (_postOfficeService != null) {
//_logger.info(">>>processLogFile:sending log file email...");
                DefaultTransactionDefinition txDef = new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_NESTED);
                TransactionStatus txStatus = _transactionManager.getTransaction(txDef);
                try {
                    String emailSubject = MessageFormat.format("Log File Bug Report for: {0} at {1,date,yyyy-MM-dd hh:mm:ss.SSS}", _servername, new Date());
                    sendEmail(_logger, _emailRecipientSubscriberId, _identityService, _postOfficeService, null, emailSubject, buf.toString(), null, null);

                    _transactionManager.commit(txStatus);
                    txStatus = null;

                } catch (RuntimeException e) {
                    _logger.error("uncaught runtime exception", e);
                    throw e;

                } finally {
                    if (txStatus != null) {
                        _transactionManager.rollback(txStatus);
                        txStatus = null;
                        throw new IllegalStateException("transaction failed");
                    }
                }
            }
        }
    }

    private boolean startsWithTimestamp(String line)
    {
        if (line == null || line.length() < _logDateFormat.length()+1 || !Character.isDigit(line.charAt(0))) return false;

        try {
            SDF_LOGDATE.parse(line.substring(0, _logDateFormat.length()));
        } catch (ParseException e) {
            return false;
        }

        return true;
    }

    private Date getLineTimestamp(String line)
    {
        if (line == null || line.length() < _logDateFormat.length()+1 || !Character.isDigit(line.charAt(0))) return null;

        try {
            return SDF_LOGDATE.parse(line.substring(0, _logDateFormat.length()));

        } catch (ParseException e) {
            return null;
        }
    }

    /**
     * This method operates via side effects. It is called for each line of the log file. It will do the following:
     * <ul>
     *   <li>update _mostRecentLineNum</li>
     *   <li>update _mostRecentTimestamp</li>
     *   <li>add items into the noteworthyLines list if it "noteworthy"</li>
     * </ul>
     * @param line the line to examine
     * @param tMinusOneHour one hour ago (to precent having to recalculate it each line)
     * @param noteworthyLines the struct to add this line to if it's worth capturing
     */
    private void parseLine(String line, Date tMinusOneHour, List<String> noteworthyLines)
    {
        Date lineTimestamp = getLineTimestamp(line);
        if (lineTimestamp != null) {
            _mostRecentTimestamp = lineTimestamp;
        }

        _mostRecentLineNum++;

        //if this line is older than one hour ago, ignore it
        Date timeToCompare = lineTimestamp != null ? lineTimestamp : _mostRecentTimestamp;
        if (timeToCompare != null && timeToCompare.before(tMinusOneHour)) return;

        //ignore any entries this file makes, since it is printing out the regular expressions, which match themselves
        if (line.contains("[LogFileAnalyzer]")) return;

        //see if the line matches any of the known patterns
        for (Pattern p : _regExPatterns) {
            if (p.matcher(line).matches()) {

                //prepend a timestamp if the line doesn't already have one
                if (lineTimestamp == null) {
                    line = MessageFormat.format("[AROUND {0,date,yyyy-MM-dd hh:mm:ss.SSS}] {1}", _mostRecentTimestamp, line);
                }

                noteworthyLines.add(line);
                break;
            }
        }
    }

//    public static void main(String[] args)
//    throws Exception
//    {
////        LogFileAnalyzer lfa = new LogFileAnalyzer();
////        lfa._logFileName = "/Users/shawker/temp/sample_sm.log";
////        lfa._checkIntervalMs = 20_000L;
////        lfa._logDateFormat = "MM/dd HH:mm:ss:SSS";
////        lfa._regEx = Arrays.asList(
////            ".* ERROR .*", ".*Exception:.*", ".*Error:.*", "(\\s+)at .*", ".*Exception$", ".*Detected SQL execution outside of transaction.*"
////        );
////        lfa.start();
////
////        //wait for user input
////        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
////        try {
////            System.out.println("press enter to exit> ");
////            br.readLine();
////        } catch (IOException e) {
////            e.printStackTrace();
////        }
////
////        lfa.stop();
//
//        Pattern p1 = Pattern.compile(".* ERROR .*", Pattern.DOTALL);
//        Pattern p2 = Pattern.compile(".*Exception:.*", Pattern.DOTALL);
//        Pattern p3 = Pattern.compile(".*Error:.*", Pattern.DOTALL);
//        Pattern p4 = Pattern.compile("(\\s+)at .*", Pattern.DOTALL);
//        Pattern p5 = Pattern.compile(".*Exception$", Pattern.DOTALL);
//        Pattern p6 = Pattern.compile(".*Detected SQL execution outside of transaction.*", Pattern.DOTALL);
//
//        List<Pattern> patterns = Arrays.asList(p1, p2, p3, p4, p5, p6);
//
//        //String line = "10/01 15:41:24:355 ERROR [BaseMessageHandler] Error processing message: Message id=f72d1854-6062-4c90-85a3-466a81fc4642 (STORE_HANDLER) ts=Mon Oct 01 15:41:24 GMT 2018 ip=172.17.0.1 props={__requestPath=/store/braintree/getPaymentMethods, HEADER_Referer=http://localhost:4200/user/wallet, HEADER_X-REST-APPLICATION-ID=SnowyOwl, HEADER_Pragma=no-cache, HEADER_Cache-Control=no-cache, HEADER_Accept=application/json, text/plain, */*, HEADER_X-REST-APPLICATION-VERSION=1.0, HEADER_User-Agent=Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/69.0.3497.81 Mobile Safari/537.36, HEADER_Origin=http://localhost:4200, HEADER_Connection=keep-alive, HEADER_X-REST-SESSION-KEY=22c62c1c-8208-47f6-9cb3-14fd8a5ea2c1, HEADER_X-REST-DEVICE-ID=07b35dd4-9219-4e93-ad7f-81e6cb87838e, HEADER_Content-Length=86, appId=snowyowl, HEADER_Accept-Language=en-US,en;q=0.9,la;q=0.8,es;q=0.7, HEADER_Accept-Encoding=gzip, deflate, br, HEADER_Host=snowl-collector--0--nc10-1.shoutgameplay.com, HEADER_Content-Type=application/x-www-form-urlencoded, toWds=https://snowl-wds-origin--0--nc10-1.shoutgameplay.com:443} parms=null with customerId is null\n";
//        //String line = "java.lang.IllegalArgumentException: customerId is null\n";
//        //String line = "        at com.meinc.store.processor.BraintreeProcessor2.getPaymentMethodsForCustomerProfile(BraintreeProcessor2.java:197)\n";
//        //String line = "Caused by: java.sql.SQLException: Connection is read-only. Queries leading to data modification are not allowed\n";
//        //String line = "java.lang.NullPointerException";
//        String line = "0/02 20:28:29:949 WARN  [SideEffectConnection] Detected SQL execution outside of transaction. See SQL log for details.";
//
//        boolean matched = false;
//        for (Pattern p : patterns) {
//            if (p.matcher(line).matches()) {
//                matched = true;
//                break;
//            }
//        }
//
//        System.out.println(matched);
//    }
}
