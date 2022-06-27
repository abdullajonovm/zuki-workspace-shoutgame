package tv.shout.sm.test;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;

/**
 * A simple HTTP library for GET/POST/MULTIPART. A production system should use a robust 3rd party networking library
 */
public class HttpLibrary
{
    /**
     * A simple wrapper around performing an HTTP GET operation. A production system should use a more robust 3rd party HTTP library.
     * <p/>
     * Since we are not doing header requests and the WebDataStore doesn't do redirects, we don't need to worry about 30x responses.
     * This call is about nothing more than grabbing the resulting document from the WebDataStore. In all instances the result should be HTTP 200 or HTTP 404.
     * Unless there's a larger unexpected server problem, in which case there might be a 50x.
     *
     * @param getUrl the endpoint. make sure that any name/value pairs (if any) on the url have been properly encoded before calling this method
     */
    public static String httpGet(String getUrl)
    throws IOException, NetworkException
    {
        URL obj = new URL(getUrl);

        //send the request
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("GET"); //not really necessary; GET is the default for HttpURLConnection
        int responseCode = con.getResponseCode();

        //read the response
        StringBuilder response = new StringBuilder();
        try {
	        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
	        String inputLine;
	        while ((inputLine = in.readLine()) != null) {
	            response.append(inputLine);
	        }
	        in.close();
        } catch (FileNotFoundException e) {
        	throw new NetworkException(404, con.getHeaderFields(), null);
        }

        if (responseCode == 200) {
            return response.toString();
        } else {
            throw new NetworkException(responseCode, con.getHeaderFields(), response.toString());
        }
    }

    /**
     * A simple wrapper around performing an HTTP POST operation. A production system should use a more robust 3rd party HTTP library.
     *
     * @param postUrl the endpoint
     * @param headers any headers to send along with the request. The Content-Type header will be added automatically
     * @param params the parameters to pass along on the request
     */
    public static String httpPost(String postUrl, Map<String, String> headers, Map<String, String> params)
    throws IOException, NetworkException
    {
        URL obj = new URL(postUrl);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection(); //NOTE: for production, this should be using https, and thus HttpsURLConnection
        con.setRequestMethod("POST");

        //add any headers
        if (headers != null) {
            for (Map.Entry<String, String> header : headers.entrySet()) {
                con.setRequestProperty(header.getKey(), header.getValue());
            }
        }

        //build up the form params
        //a string in this form:
        // param1=value&param2=value
        StringBuilder buf = new StringBuilder();
        if (params != null) {
            con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            boolean first = true;
            for (Map.Entry<String, String> param : params.entrySet()) {
                if (!first) {
                    buf.append("&");
                } else {
                    first = false;
                }

                buf.append(param.getKey()).append("=").append(encode(param.getValue()));
            }
        }

        //send post request
        con.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
        wr.writeBytes(buf.toString());
        wr.flush();
        wr.close();

        //get the response
        int responseCode = con.getResponseCode();

        //read the response
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        if (responseCode == 200) {
            return response.toString();
        } else {
            throw new NetworkException(responseCode, con.getHeaderFields(), response.toString());
        }
    }

    //https://stackoverflow.com/questions/11766878/sending-files-using-post-with-httpurlconnection
    public static String httpMultipartPost(String postUrl, String attachmentName, String attachmentFileName, byte[] data)
    throws IOException, NetworkException
    {
        String crlf = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";

        //set up the request
        HttpURLConnection httpUrlConnection = null;
        URL url = new URL(postUrl);
        httpUrlConnection = (HttpURLConnection) url.openConnection();
        httpUrlConnection.setUseCaches(false);
        httpUrlConnection.setDoOutput(true);
        httpUrlConnection.setRequestMethod("POST");
        httpUrlConnection.setRequestProperty("Connection", "Keep-Alive");
        httpUrlConnection.setRequestProperty("Cache-Control", "no-cache");
        httpUrlConnection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);

        //start the content wrapper
        DataOutputStream request = new DataOutputStream(httpUrlConnection.getOutputStream());
        request.writeBytes(twoHyphens + boundary + crlf);
        request.writeBytes("Content-Disposition: form-data; name=\"" +
                attachmentName + "\";filename=\"" +
                attachmentFileName + "\"" + crlf);
        request.writeBytes(crlf);

        //write the data
        request.write(data);

        //end the content wrapper
        request.writeBytes(crlf);
        request.writeBytes(twoHyphens + boundary + twoHyphens + crlf);

        //make sure it sends
        request.flush();
        request.close();

        int responseCode = httpUrlConnection.getResponseCode();

        //get the response
        InputStream responseStream = new BufferedInputStream(httpUrlConnection.getInputStream());
        BufferedReader responseStreamReader = new BufferedReader(new InputStreamReader(responseStream));
        String line = "";
        StringBuilder stringBuilder = new StringBuilder();
        while ((line = responseStreamReader.readLine()) != null) {
            stringBuilder.append(line).append("\n");
        }
        responseStreamReader.close();
        String response = stringBuilder.toString();
        responseStream.close();

        httpUrlConnection.disconnect();

        if (responseCode == 200) {
            return response.toString();
        } else {
            throw new NetworkException(responseCode, httpUrlConnection.getHeaderFields(), response);
        }
    }

    private static String encode(String value)
    {
        try {
            return URLEncoder.encode(value, "UTF-8");
        } catch (UnsupportedEncodingException impossible) {
            //if utf-8 isn't supported, there are bigger problems to deal with
            return value;
        }
    }

}
