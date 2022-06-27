package tv.shout.ecommerce;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Properties;
import java.util.UUID;

import javax.net.ssl.HttpsURLConnection;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import tv.shout.sm.db.DbProvider;
import tv.shout.sm.test.NetworkException;

public class AuthorizeDotNetTester
{
    static
    {
        //initialize the local logging to go to the console
        System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.SimpleLog");
        System.setProperty("org.apache.commons.logging.simplelog.defaultlog", "debug");
        System.setProperty("org.apache.commons.logging.simplelog.defaultlog", "debug");
        System.setProperty("org.apache.commons.logging.simplelog.log.org.apache", "debug");

        System.setProperty("log4j.defaultInitOverride", "true");
        Properties log4jProperties = new Properties();
        log4jProperties.setProperty("log4j.rootLogger", "INFO, stdout");
        log4jProperties.setProperty("log4j.appender.stdout", "org.apache.log4j.ConsoleAppender");
        log4jProperties.setProperty("log4j.appender.stdout.layout", "org.apache.log4j.PatternLayout");
        log4jProperties.setProperty("log4j.appender.stdout.layout.ConversionPattern", "%d{MM/dd HH:mm:ss.SSS} %-5p [%c] %m%n");
        log4jProperties.setProperty("log4j.logger.org.apache.http.wire", "DEBUG");
        log4jProperties.setProperty("log4j.logger.org.apache.httpclient", "DEBUG");
        PropertyConfigurator.configure(log4jProperties);
    }

    private static Logger _logger = Logger.getLogger(AuthorizeDotNetTester.class);
    private static final String PROP_FILE = "/Volumes/Encrypted Data/ShoutMeinc/snowyowl.properties";
    private static final String KEY_CLIENT_KEY = "authorize.net.clientkey";
    private static final String KEY_API_LOGIN_ID = "authorize.net.apiloginid";
    private static final String KEY_TRANSACTION_KEY = "authorize.net.transactionkey";

    private String getNonce(String cardNumber, String expirationDate, String code)
    throws Exception
    {
        Properties props = Util.getProperties(PROP_FILE);
        String clientKey = props.getProperty(KEY_CLIENT_KEY);
        String apiLoginId = props.getProperty(KEY_API_LOGIN_ID);

        String tokenId = UUID.randomUUID().toString();

        String payload = String.format(
            "{ " +
            "    \"securePaymentContainerRequest\":{" +
            "       \"merchantAuthentication\":{" +
            "          \"name\":\"%s\"," +
            "          \"clientKey\":\"%s\"" +
            "       }," +
            "       \"data\":{" +
            "          \"type\":\"TOKEN\"," +
            "          \"id\":\"%s\"," +
            "          \"token\":{" +
            "             \"cardNumber\":\"%s\"," +
            "             \"expirationDate\":\"%s\"," +
            "             \"cardCode\":\"%s\"" +
            "          }" +
            "       }" +
            "    }" +
            "}",
            apiLoginId, clientKey, tokenId, cardNumber, expirationDate, code);

        //build the request
        URL obj = new URL("https://apitest.authorize.net/xml/v1/request.api");
        HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

        //send post request
        con.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
        wr.writeBytes(payload);
        wr.flush();
        wr.close();

        //get the response
        int responseCode = con.getResponseCode();
        _logger.info("response code: "+ responseCode);

        //read the response
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        _logger.info(response.toString());

        if (responseCode == 200) {
            String responseAsStr = response.toString();
            int beginIdx = responseAsStr.indexOf("\"dataValue\":")+13;
            int endIdx = responseAsStr.indexOf("\"", beginIdx+1);
            String nonce = responseAsStr.substring(beginIdx, endIdx);

            return nonce;

        } else {
            throw new NetworkException(responseCode, con.getHeaderFields(), response.toString());
        }
    }

    public static void main(String[] args)
    throws Exception
    {
        AuthorizeDotNetProcessor processor = new AuthorizeDotNetProcessor(DbProvider.DB.NC11_1);

//        AuthorizeDotNetTester tester = new AuthorizeDotNetTester();
//        String nonce = tester.getNonce("5424000000000015", "122020", "900");
//        String nonce = tester.getNonce("4111111111111111", "092019", "256");
//        String nonce = tester.getNonce("5424000000000015", "2020-12", "900");
//        String nonce = tester.getNonce("4111111111111111", "0919", "256");
//        _logger.info("NONCE: " + nonce);

        int subscriberId = 8;

        //purchase via nonce
//        String itemUuid = "e8056253-7c6c-11e7-970d-0242ac110004"; //$1 purchase
//        Receipt receipt = processor.purchaseViaNonce(subscriberId, itemUuid, nonce);
//        _logger.info("RECEIPT:\n" + receipt);

        //create profile
//        String customerProfileId = processor.createCustomerProfile(subscriberId, nonce);
//        _logger.info("customerProfileId: " + customerProfileId);

        //add payment method
//        processor.addPaymentMethodToCustomerProfile(subscriberId, nonce);

        //retrieve the profile
//        com.meinc.store.domain.CustomerProfile cp = processor.getCustomerProfile(subscriberId);
//        _logger.info("Customer Profile: " + cp);

        //purchase via payment profile
//        String itemUuid = "e8056253-7c6c-11e7-970d-0242ac110004"; //$1 purchase
//        String paymentProcessorId = "1502132741";
//        Receipt receipt = processor.purchaseViaCustomerProfile(subscriberId, itemUuid, paymentProcessorId);
//        _logger.info("RECEIPT:\n" + receipt);

        //delete the profile
        processor.deleteCustomerProfile(subscriberId);

        processor.stop();
    }

}
