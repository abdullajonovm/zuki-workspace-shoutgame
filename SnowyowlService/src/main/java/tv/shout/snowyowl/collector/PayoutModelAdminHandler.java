package tv.shout.snowyowl.collector;

import static com.meinc.webcollector.message.CollectorMessage.PARM_TO_WDS;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import com.fasterxml.jackson.core.type.TypeReference;
import com.meinc.identity.domain.Subscriber;
import com.meinc.webcollector.service.IWebCollectorService.CollectorEndpoint;
import com.meinc.webcollector.service.IWebCollectorService.CollectorEndpoint.ConnectionType;

import tv.shout.collector.CollectorEndpointHandler;
import tv.shout.collector.PublishResponseError;
import tv.shout.snowyowl.domain.PayoutModel;
import tv.shout.snowyowl.domain.PayoutModelRound;
import tv.shout.snowyowl.engine.PayoutManagerException;
import tv.shout.snowyowl.engine.PayoutManagerFixedRoundCommon;
import tv.shout.util.FastMap;

public class PayoutModelAdminHandler
extends BaseSmMessageHandler
{
    private static Logger _logger = Logger.getLogger(PayoutModelAdminHandler.class);

    // Valid form post param names
    private static final List<String> _validFormVars = Arrays.asList(
        "costToJoin", "payoutModel", "payoutModelId", "deactivationReason"
    );

    @Override
    protected List<String> getValidFormVars()
    {
        return _validFormVars;
    }

    @Override
    public String getHandlerMessageType()
    {
        return "SNOWN_PM_ADMIN";
    }

    @Override
    public CollectorEndpoint[] getCollectorEndpoints()
    {
        CollectorEndpointHandler[] handlers = new CollectorEndpointHandler[] {
                new CollectorEndpointHandler(new CollectorEndpoint("/snowladmin/payoutModel/create", ConnectionType.ANY))
                .withValidRoles(new HashSet<>(Arrays.asList(PAYOUT_MODEL)))
                .withMessageHandlerFunction(
                        (message, logMessageTag) ->
                        createPayoutModel(message.getProperties(), message.getMessageId())),

                new CollectorEndpointHandler(new CollectorEndpoint("/snowladmin/payoutModel/get", ConnectionType.ANY))
                .withValidRoles(new HashSet<>(Arrays.asList(SHOUTCASTER, PAYOUT_MODEL)))
                .withMessageHandlerFunction(
                        (message, logMessageTag) ->
                        getPayoutModels(message.getProperties(), message.getMessageId())),

                new CollectorEndpointHandler(new CollectorEndpoint("/snowladmin/payoutModel/update", ConnectionType.ANY))
                .withValidRoles(new HashSet<>(Arrays.asList(PAYOUT_MODEL)))
                .withMessageHandlerFunction(
                        (message, logMessageTag) ->
                        updatePayoutModel(message.getProperties(), message.getMessageId())),

                new CollectorEndpointHandler(new CollectorEndpoint("/snowladmin/payoutModel/deactivate", ConnectionType.ANY))
                .withValidRoles(new HashSet<>(Arrays.asList(PAYOUT_MODEL)))
                .withMessageHandlerFunction(
                        (message, logMessageTag) ->
                        deactivatePayoutModel(message.getProperties(), message.getMessageId())),

                new CollectorEndpointHandler(new CollectorEndpoint("/snowladmin/payoutModel/delete", ConnectionType.ANY))
                .withValidRoles(new HashSet<>(Arrays.asList(PAYOUT_MODEL)))
                .withMessageHandlerFunction(
                        (message, logMessageTag) ->
                        deletePayoutModel(message.getProperties(), message.getMessageId())),
        };

        for (CollectorEndpointHandler handler : handlers) {
            _collectorEndpointHandlerByPath.put(handler.getCollectorEndpoint().getPath(), handler);
        }

        return Arrays.stream(handlers)
                .map(CollectorEndpointHandler::getCollectorEndpoint)
                .toArray(CollectorEndpoint[]::new);
    }

    private Map<String, Object> createPayoutModel(Map<String, String> props, String messageId)
    throws PublishResponseError
    {
        String docType = "createPayoutModel";
        Subscriber subscriber = getSubscriber(props, messageId, docType);

        //read in the object, convert from json to java, and set default values
        TypeReference<PayoutModel> typeRef = new TypeReference<PayoutModel>() {};
        PayoutModel pm = getJsonObjectFromProps(props, messageId, docType, "payoutModel", true, typeRef);
        pm.setCreatorId(subscriber.getSubscriberId());
        pm.setCreateDate(new Date());
        pm.setActive(true);
        pm.setDeactivatedDate(null);
        pm.setDeactivationReason(null);

        try {
            checkPayoutModelForConsistency(pm);
        } catch (PayoutManagerException e) {
            throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, docType, false, "invalidPayoutModel", e.getErrorTypeCode());
        }

        DefaultTransactionDefinition txDef = new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_NESTED);
        TransactionStatus txStatus = _transactionManager.getTransaction(txDef);
        try {
            _dao.insertPayoutModel(pm);

            for (PayoutModelRound pmr : pm.getPayoutModelRounds()) {
                pmr.setPayoutModelId(pm.getPayoutModelId());
                _dao.insertPayoutModelRound(pmr);
            }

            _transactionManager.commit(txStatus);
            txStatus = null;
        } finally {
            if (txStatus != null) {
                _transactionManager.rollback(txStatus);
                txStatus = null;
            }
        }

        return new FastMap<>("payoutModel", pm);
    }

    private Map<String, Object> getPayoutModels(Map<String, String> props, String messageId)
    throws PublishResponseError
    {
        String docType = "getPayoutModels";

        Float costToJoin = getFloatParamFromProps(props, messageId, docType, "costToJoin", false);

        List<PayoutModel> payoutModels;

        DefaultTransactionDefinition txDef = new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_NESTED);
        TransactionStatus txStatus = _transactionManager.getTransaction(txDef);
        try {
            if (costToJoin == null) {
                payoutModels = _dao.getAllPayoutModels();
            } else {
                payoutModels = _dao.getPayoutModelsByEntranceFee(costToJoin);
            }
            _transactionManager.commit(txStatus);
            txStatus = null;
        } finally {
            if (txStatus != null) {
                _transactionManager.rollback(txStatus);
                txStatus = null;
            }
        }

        return new FastMap<>("payoutModels", payoutModels.stream().filter(PayoutModel::isActive).collect(Collectors.toList()));
    }

    private Map<String, Object> updatePayoutModel(Map<String, String> props, String messageId)
    throws PublishResponseError
    {
        String docType = "updatePayoutModel";

        TypeReference<PayoutModel> typeRef = new TypeReference<PayoutModel>() {};
        PayoutModel pm = getJsonObjectFromProps(props, messageId, docType, "payoutModel", true, typeRef);

        try {
            checkPayoutModelForConsistency(pm);
        } catch (PayoutManagerException e) {
            throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, docType, false, "invalidPayoutModel", e.getErrorTypeCode());
        }

        DefaultTransactionDefinition txDef = new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_NESTED);
        TransactionStatus txStatus = _transactionManager.getTransaction(txDef);
        try {
            //make sure no games are currently using it
            if (_dao.isPayoutModelInUse(pm.getPayoutModelId())) {
                throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, docType, false, "payoutModelInUse");
            }

            //update the object in the db
            _dao.updatePayoutModel(pm);

            //delete all the pmr rows and re-insert them from the new model
            _dao.deletePayoutModelRounds(pm.getPayoutModelId());
            for (PayoutModelRound pmr : pm.getPayoutModelRounds()) {
                pmr.setPayoutModelId(pm.getPayoutModelId());
                _dao.insertPayoutModelRound(pmr);
            }

            _transactionManager.commit(txStatus);
            txStatus = null;
        } finally {
            if (txStatus != null) {
                _transactionManager.rollback(txStatus);
                txStatus = null;
            }
        }

        return null;
    }

    private Map<String, Object> deactivatePayoutModel(Map<String, String> props, String messageId)
    throws PublishResponseError
    {
        String docType = "deactivatePayoutModel";
        Subscriber subscriber = getSubscriber(props, messageId, docType);
        int payoutModelId = getIntParamFromProps(props, messageId, docType, "payoutModelId", true);
        String deactivationReason = getParamFromProps(props, messageId, docType, "deactivationReason", true);

        DefaultTransactionDefinition txDef = new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_NESTED);
        TransactionStatus txStatus = _transactionManager.getTransaction(txDef);
        try {
            _dao.deactivatePayoutModel(payoutModelId, subscriber.getSubscriberId(), deactivationReason);

            _transactionManager.commit(txStatus);
            txStatus = null;
        } finally {
            if (txStatus != null) {
                _transactionManager.rollback(txStatus);
                txStatus = null;
            }
        }

        return null;
    }

    private Map<String, Object> deletePayoutModel(Map<String, String> props, String messageId)
    throws PublishResponseError
    {
        String docType = "deletePayoutModel";

        int payoutModelId = getIntParamFromProps(props, messageId, docType, "payoutModelId", true);

        DefaultTransactionDefinition txDef = new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_NESTED);
        TransactionStatus txStatus = _transactionManager.getTransaction(txDef);
        try {
            //make sure no games are currently using it
            if (_dao.isPayoutModelInUse(payoutModelId)) {
                throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, docType, false, "payoutModelInUse");
            }

            //delete all the pmr rows
            _dao.deletePayoutModelRounds(payoutModelId);

            //delete the pm row
            _dao.deletePayoutModel(payoutModelId);

            _transactionManager.commit(txStatus);
            txStatus = null;
        } finally {
            if (txStatus != null) {
                _transactionManager.rollback(txStatus);
                txStatus = null;
            }
        }

        return null;
    }

    private void checkPayoutModelForConsistency(PayoutModel pm)
    throws PayoutManagerException
    {
        int numPlayers = pm.getBasePlayerCount();
        List<PayoutModelRound> rounds = PayoutManagerFixedRoundCommon.getDefaultPayoutModelRounds(numPlayers);
        //Collections.reverse(rounds);
        _logger.info("SERVER ROUNDS:");
        rounds.stream().forEach(r -> _logger.info("\t" + r));

        _logger.info("CLIENT ROUNDS:");
        pm.getPayoutModelRounds().stream().forEach(r -> _logger.info("\t" + r));

        //make sure the number of rounds is the same
        if (rounds.size() != pm.getPayoutModelRounds().size()) {
            throw new PayoutManagerException("incorrect number of rounds", null);
        }

        int lastClientStartingPlayerCount = -1;

        //for each round, make sure that the starting/eliminated player count is the same
        for (int i=0; i<rounds.size(); i++) {
            PayoutModelRound generatedRound = rounds.get(i);
            PayoutModelRound clientRound = pm.getPayoutModelRounds().get(i);

            if (clientRound.getStartingPlayerCount() < lastClientStartingPlayerCount) {
                throw new PayoutManagerException(MessageFormat.format("starting player count for round {0}, is smaller than last round player count", i+1), null) ;
            }
            lastClientStartingPlayerCount = clientRound.getStartingPlayerCount();

            if (generatedRound.getStartingPlayerCount() != clientRound.getStartingPlayerCount()) {
                throw new PayoutManagerException(MessageFormat.format("starting player counts do not match for round {0}, should be {1,number,#}", i+1, generatedRound.getStartingPlayerCount()), null) ;
            }

            if (generatedRound.getEliminatedPlayerCount() != clientRound.getEliminatedPlayerCount()) {
                throw new PayoutManagerException(MessageFormat.format("eliminated player counts do not match for round {0}, should be {1,number,#}", i+1, generatedRound.getEliminatedPlayerCount()), null) ;
            }
        }
    }

}
