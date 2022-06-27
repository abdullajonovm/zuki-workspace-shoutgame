package tv.shout.snowyowl.engine;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.meinc.trigger.service.ITriggerService;
import com.meinc.webdatastore.domain.WebDataStoreObject;
import com.meinc.webdatastore.service.IWebDataStoreService;
import com.meinc.webdatastore.service.WebDataStoreException;

import io.socket.client.Socket;
import tv.shout.collector.BaseMessageHandler;
import tv.shout.sc.domain.Round;
import tv.shout.snowyowl.common.FileHandler;
import tv.shout.snowyowl.common.MessageBus;
import tv.shout.snowyowl.common.MessageProcessor;
import tv.shout.snowyowl.domain.Message;
import tv.shout.snowyowl.service.SnowyowlService;
import tv.shout.util.JsonUtil;
import tv.shout.util.MaxSizeHashMap;

public class MMECommon
implements FileHandler, MessageProcessor
{
    private static Logger _logger = Logger.getLogger(MMECommon.class);

    @Value("${sm.engine.statedir}")
    protected String _stateDir;

    @Resource(name="webDataStoreService")
    private IWebDataStoreService _wdsService;

    private String _prefix;

    private Lock _lock = new ReentrantLock();
    private Set<MMEProcess> _processingIds = new HashSet<>();
    private MaxSizeHashMap<String, Integer> _roundOutstandingBracketMatchesMap = new MaxSizeHashMap<String, Integer>().withMaxSize(32);

    public void setPrefix(String prefix)
    {
        _prefix = prefix;

        //piggy back: register for messages
        MessageBus.unregister(this);
        MessageBus.register(this);
_logger.info("registering for message bus messages");
    }

    public Set<MMEProcess> getProcessingIds()
    {
        return _processingIds;
    }

    public Map<String, Integer> getRoundOutstandingBracketMatchesMap()
    {
        return _roundOutstandingBracketMatchesMap;
    }

    @Override
    public void handleMessage(Message message)
    {
        switch (message.type)
        {
            case Message.MESSAGE_TYPE_GAME_CANCELLED:
//_logger.info("received message bus message: game_cancelled");
                removeProcess((String) message.payload);
                break;
        }
    }

    public void killProcess(String id)
    {
        for (MMEProcess process : _processingIds) {
            if (process.id.equals(id)) {
                _lock.lock();
                try {
                    SnowyowlService.SUB_EVENT_LOGGER.debug("MME: FORCE STOP PROCESSING OF: " + process.id);
                    _processingIds.remove(new MMEProcess(process.id, null));
                    saveState();
                } finally {
                    _lock.unlock();
                }
                break;
            }
        }
    }

    private void removeProcess(String gameId)
    {
        for (MMEProcess process : _processingIds) {
            if (process.gameId.equals(gameId)) {
                _lock.lock();
                try {
                    SnowyowlService.SUB_EVENT_LOGGER.debug("MME: REMOVING PROCESSING OF: " + process.id);
//_logger.info("removing process: " + process.id);
                    _processingIds.remove(new MMEProcess(process.id, null));
                    saveState();
                } finally {
                    _lock.unlock();
                }
                break;
            }
        }
    }

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

//not necessary. this socket message is sent each time a match concludes so this seems redundant
//        //also send out a socket.io message with the number of players not yet eliminated
//        //this is making some assumptions (that are true now, but might not be in the future)
//        // 1) bots move forward when they win
//        // 2) always a winner at each match ending
//        // ... sure there are others, but i can't think of what they are right now
//        int totalPlayersAtRoundBeginning = round.getCurrentPlayerCount();
//        int matchPlayersRemaining = numOutstandingMatches * round.getMatchPlayerCount();
//        int totalPlayersLeft = totalPlayersAtRoundBeginning - ( ( totalPlayersAtRoundBeginning - matchPlayersRemaining ) / round.getMatchPlayerCount() );
//
//        //offset this by the number of saves remaining
//        GameStats gameStats = Optional.of(dao.getGameStats(round.getGameId())).orElse(new GameStats());
//        int numSavesRemaining = Optional.of(gameStats.getRemainingSavePlayerCount()).orElse(0);
//        totalPlayersLeft += numSavesRemaining;
//
//        if (socketIoSocket != null) {
////_logger.info(MessageFormat.format(">>> round: #{0}, numOutstandingMatches: #{1}, totalPlayersLeft: #{2}", round.getRoundSequence(), numOutstandingMatches, totalPlayersLeft));
//            Map<String, Object> msg = new HashMap<>();
//            msg.put("count", totalPlayersLeft);
//
//            try {
//                ObjectMapper _jsonMapper = JsonUtil.getObjectMapper();
//                String message = _jsonMapper.writeValueAsString(msg);
//                SocketIoLogger.log(triggerService, null, "send_playercount", message, "SENDING");
//                socketIoSocket.emit("send_playercount", message);
//                SocketIoLogger.log(triggerService, null, "send_playercount", message, "SENDT");
//            } catch (JsonProcessingException e) {
//                _logger.error("unable to convert map to json", e);
//            }
//        }

        return totalMatchesForRound;
    }

    public void saveState()
    {
        if (_prefix == null) {
            throw new IllegalStateException("prefix not set");
        }

        try {
            File setFile = new File(_stateDir, _prefix + "_processingIds.dat");
            writeToFile(setFile, _processingIds);
        } catch (IOException e) {
            _logger.error("unable to save state", e);
        }
    }

    @SuppressWarnings("unchecked")
    public void loadState(Consumer<MMEProcess> processMethod)
    {
        if (_prefix == null) {
            throw new IllegalStateException("prefix not set");
        }

        try {
            File setFile = new File(_stateDir, _prefix + "_processingIds.dat");
            if (!setFile.exists()) return;

            _processingIds = (Set<MMEProcess>) readFromFile(setFile);
            if (_processingIds.size() > 0) {
                processMethod.accept(null);
            }

        } catch (IOException | ClassNotFoundException e) {
            _logger.error("unable to load state", e);
        }
    }
}
