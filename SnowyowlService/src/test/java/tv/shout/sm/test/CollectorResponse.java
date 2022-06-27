package tv.shout.sm.test;

import java.io.IOException;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import tv.shout.util.JsonUtil;

public class CollectorResponse
{
    public String ticket; //contains a UUID which is the path needed to retrieve the WDS response
    public long estimatedWaitTime; //the number of milliseconds the server expects the async requests to take
    public String encryptKey; //usually null, but in the case of /account/login, will contain a one time use key needed to decrypt the WDS response

    public static CollectorResponse fromJsonString(String jsonResponse)
    throws IOException
    {
        //a convenient way to convert the JSON string into a Java object of a specific type by matching up field names
        jsonResponse = jsonResponse.replace("\\", "\\\\");
        ObjectMapper mapper = JsonUtil.getObjectMapper();
        CollectorResponse cr = mapper.readValue(jsonResponse, new TypeReference<CollectorResponse>() {});
        return cr;
    }

}
