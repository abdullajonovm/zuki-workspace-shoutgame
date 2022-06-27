package tv.shout.snowyowl.common;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.meinc.identity.domain.Subscriber;
import com.meinc.trigger.service.ITriggerService;

import io.socket.client.Socket;
import tv.shout.sc.domain.MatchPlayer;
import tv.shout.sc.domain.RoundPlayer;
import tv.shout.snowyowl.service.ISnowyowlService;
import tv.shout.snowyowl.service.SnowyowlService;
import tv.shout.sync.domain.SyncMessage;
import tv.shout.sync.service.ISyncService;
import tv.shout.util.JsonUtil;

public interface SyncMessageSender
{
    default void enqueueSyncMessage(
        ObjectMapper jsonMapper, ISyncService syncService, Logger logger,
        String contextualId, String messageType, Map<String, Object> payload,
        Subscriber subscriber, Socket socketIoSocket, ITriggerService triggerService)
    {
        try {
            long subscriberId = subscriber.getSubscriberId();

            SyncMessage syncMessage = new SyncMessage(
                    subscriberId, contextualId, messageType,
                    ISnowyowlService.GAME_ENGINE, jsonMapper.writeValueAsString(payload));

            if (syncService != null) {
                if (SnowyowlService.SUB_EVENT_LOGGER.isDebugEnabled()) {
                    RoundPlayer rp;

                    String gameId = contextualId;
                    String otherId;
                    switch (messageType)
                    {
                        case ISnowyowlService.SYNC_MESSAGE_JOINED_ROUND:
                        case ISnowyowlService.SYNC_MESSAGE_ABANDONED_ROUND:
                            rp = (RoundPlayer) payload.get("roundPlayer");
                            otherId = "rid: " + rp.getRoundId().substring(0,4) + "...";
                            break;

                        case ISnowyowlService.SYNC_MESSAGE_MATCH_RESULT:
                            String roundId = (String) payload.get("roundId");
                            otherId = "rid: " + roundId.substring(0,4) + "...";
                            break;

                        case ISnowyowlService.SYNC_MESSAGE_USER_MATCHED:
                            @SuppressWarnings("unchecked") List<MatchPlayer> players = (List<MatchPlayer>) payload.get("players");

                            if (players == null || players.size() == 0) {
                                otherId = "rId: unknown";
                            } else {
                                MatchPlayer mp = players.get(0);
                                otherId = "rid: " + mp.getRoundId().substring(0,4) + "...";
                            }
                            break;

                        case ISnowyowlService.SYNC_MESSAGE_GAME_RESULT:
                            @SuppressWarnings("unchecked") List<RoundPlayer> roundPlayers = (List<RoundPlayer>) payload.get("roundPlayers");

                            if (roundPlayers == null || roundPlayers.size() == 0) {
                                otherId = "rId: unknown";
                            } else {
                                rp = roundPlayers.get(0);
                                otherId = "rid: " + rp.getRoundId().substring(0,4) + "...";
                            }
                            break;

                        default:
                            otherId = "";
                    }

                    SnowyowlService.SUB_EVENT_LOGGER.debug(MessageFormat.format(
                        "MSG: sId: {0,number,#}, {1}: type: {2}, gameId: {3}, otherId: {4}",
                        subscriberId, "syncMessage", messageType, gameId, otherId)
                    );
                }

                //store in the db
                syncService.addSyncMessageDirect(syncMessage);

                //send as a socket.io message
                if (socketIoSocket != null) {
                    Map<String, Object> msg = new HashMap<>();
                    msg.put("recipient", subscriber.getEmailSha256Hash());
                    msg.put("message", syncMessage);

                    ObjectMapper _jsonMapper = JsonUtil.getObjectMapper();
                    String message = _jsonMapper.writeValueAsString(msg);
                    SocketIoLogger.log(triggerService, subscriberId, "send_sync_message", message, "SENDING");
                    socketIoSocket.emit("send_sync_message", message);
                    SocketIoLogger.log(triggerService, subscriberId, "send_sync_message", message, "SENT");
                }

            } else {
                logger.warn("_syncService not initialized; not enqueueing SyncMessage");
            }

        } catch (JsonProcessingException e) {
            logger.error("failed to convert syncMessage to json string", e);
        }
    }
}
