package tv.shout.snowyowl.engine;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.meinc.trigger.service.ITriggerService;
import com.meinc.webdatastore.domain.WebDataStoreObject;
import com.meinc.webdatastore.service.IWebDataStoreService;
import com.meinc.webdatastore.service.WebDataStoreException;

import io.socket.client.Socket;
import tv.shout.collector.BaseMessageHandler;
import tv.shout.sc.domain.Round;
import tv.shout.util.JsonUtil;
import tv.shout.util.MaxSizeHashMap;

public class MMECommon
{
    private static Logger _logger = Logger.getLogger(MMECommon.class);

    @Resource(name="webDataStoreService")
    private IWebDataStoreService _wdsService;

    private MaxSizeHashMap<String, Integer> _roundOutstandingBracketMatchesMap = new MaxSizeHashMap<String, Integer>().withMaxSize(32);

    //this must be wrapped in a transaction
    public int publishBracketOutstandingMatchCount(Round round, int numOutstandingMatches, Socket socketIoSocket, ITriggerService triggerService/*, IDaoMapper dao*/)
    {
        ObjectMapper jsonMapper = JsonUtil.getObjectMapper();

        //check the cache
        Integer previousNumOutstandingMatches = _roundOutstandingBracketMatchesMap.get(round.getId());
        if (previousNumOutstandingMatches == null) {
            previousNumOutstandingMatches = -1;
        }

        int totalMatchesForRound = round.getCurrentPlayerCount() / round.getMatchPlayerCount();

        //don't republish the doc if nothing has changed
        if (numOutstandingMatches == previousNumOutstandingMatches) {
            return totalMatchesForRound;
        }

        //cache for next time
        _roundOutstandingBracketMatchesMap.put(round.getId(), numOutstandingMatches);

        WebDataStoreObject object = new WebDataStoreObject();
        object.addResponseHeader("Content-Type", "application/json;charset=utf-8");
        object.setExpirationDate(new Date(new Date().getTime() + BaseMessageHandler.EXPIRES_1_HOUR_MS));
        object.setPath("/" + round.getGameId() + "/bracketplay_outstanding_matches.json");

        Map<String, Object> data = new HashMap<>();
        data.put("roundSequence", round.getRoundSequence());
        data.put("totalMatchesForRound", totalMatchesForRound);
        data.put("numOutstandingMatches", numOutstandingMatches);

        try {
            object.setData(jsonMapper.writeValueAsBytes(data));

            try {
                _wdsService.createOrUpdateObjectSync(object, 0);
            } catch (WebDataStoreException | InterruptedException e) {
                _logger.error("unable to publish bracketplay_outstanding_matches.json for game: " +  round.getGameId(), e);
            }

        } catch (JsonProcessingException e) {
            _logger.error("unable to convert data map to json", e);
        }

        return totalMatchesForRound;
    }

}
