package tv.shout.snowyowl.common;

import java.util.Date;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.meinc.webdatastore.domain.WebDataStoreObject;
import com.meinc.webdatastore.service.IWebDataStoreService;
import com.meinc.webdatastore.service.WebDataStoreException;

import tv.shout.util.JsonUtil;

public interface WdsPublisher
{
    /**
     * Publish a WDS document in json format.
     *
     * @param logger .
     * @param wdsService .
     * @param expireDate may be null
     * @param path where to put it
     * @param data what to publish
     */
    public default void publishJsonWdsDoc(Logger logger, IWebDataStoreService wdsService, Date expireDate, String path, Object data)
    {
        WebDataStoreObject object = new WebDataStoreObject();
        object.addResponseHeader("Content-Type", "application/json;charset=utf-8");
        if (expireDate != null) {
            object.setExpirationDate(expireDate);
        }
        object.setPath(path);
        try {
            object.setData(JsonUtil.getObjectMapper().writeValueAsBytes(data));

            try {
                wdsService.createOrUpdateObjectSync(object, 0);
            } catch (WebDataStoreException | InterruptedException e) {
                logger.error("unable to publish " + path, e);
            }

        } catch (JsonProcessingException e) {
            logger.error("unable to convert data to json", e);
        }

    }
}
