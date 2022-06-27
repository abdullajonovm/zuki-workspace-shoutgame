package tv.shout.sm.test;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import tv.shout.sm.test.CollectorToWdsResponse.DataReceiver;
import tv.shout.sm.test.CollectorToWdsResponse.ERROR_TYPE;
import tv.shout.sm.test.CollectorToWdsResponse.REQUEST_TYPE;

public class ConsoleOutputWithRunnableDataReceiver
implements DataReceiver
{
    private static Logger _logger = Logger.getLogger(ConsoleOutputWithRunnableDataReceiver.class);
    private JsonRunnable _runOnSuccess;

    public ConsoleOutputWithRunnableDataReceiver(JsonRunnable runOnSuccess)
    {
        _runOnSuccess = runOnSuccess;
    }

    @Override
    public void dataCallbackSuccess(REQUEST_TYPE requestType, JsonNode json)
    {
        try {
            _logger.debug("SUCCESS");

            if (_runOnSuccess.showRawResult) {
                ObjectMapper mapper = new ObjectMapper();
                JsonGenerator jsonGenerator = mapper.getFactory().createGenerator(System.out);
                jsonGenerator.setPrettyPrinter(new DefaultPrettyPrinter());
                mapper.writeTree(jsonGenerator, json);
            }

            _runOnSuccess.json = json;
            _runOnSuccess.run();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void dataCallbackFailure(
            REQUEST_TYPE requestType, ERROR_TYPE errorType, int httpResponseCode,
            String responseMessage, Map<String, List<String>> responseHeaders, String responseBody)
    {
        _logger.error(MessageFormat.format("FAILURE. type: {0}, http response code: {1}, message: {2}, body:\n{3}", errorType, httpResponseCode, responseMessage, responseBody));
        System.exit(1);
    }
}
