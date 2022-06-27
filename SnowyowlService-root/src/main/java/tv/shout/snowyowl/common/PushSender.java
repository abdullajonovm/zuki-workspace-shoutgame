package tv.shout.snowyowl.common;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import com.meinc.push.exception.PayloadInvalidException;
import com.meinc.push.exception.PayloadTooLargeException;
import com.meinc.push.exception.PushNetworkIoException;
import com.meinc.push.service.IPushService;

import tv.shout.sc.domain.Game;
import tv.shout.snowyowl.dao.IDaoMapper;
import tv.shout.util.FastMap;

public interface PushSender
{
    Map<Integer, Set<String>> _appIdToBundleIdsMap = new HashMap<>();

    default void sendGamePush(
            PlatformTransactionManager transactionManager, IDaoMapper dao, IPushService pushService, Logger logger,
            long subscriberId, String languageCode, Game game, String apsCategory, String notificationTitle, String notificationBody, String type, Map<String, Object> extras)
    {
        //these are required by pushes for APNs
        Map<String, String> aps = new HashMap<>();
        aps.put("category", apsCategory);
        aps.put("alert", notificationBody);
        aps.put("sound", "default");

        final Map<String, Object> msgValues = new HashMap<>();
        msgValues.put("aps", aps);
        msgValues.put("gameId", game.getId());
        msgValues.put("type", type);
        if (extras != null) {
            msgValues.put("extras", extras);
        }

        //these are required for pushes by FCM
        Map<Long, String> subIdToLanguageCodeMap = new FastMap<>(subscriberId, languageCode);
        Map<String, String> languageCodeToNotificationTitleMap = new FastMap<>(languageCode, notificationTitle);
        Map<String, String> languageCodeToNotificationBodyMap = new FastMap<>(languageCode, notificationBody);

        game.getAllowableAppIds().stream().forEach(appId -> {
            Set<String> bundleIds = getAppBundleIds(appId, transactionManager, dao);
            try {
                pushService.pushNotificationToSubscriber(msgValues, subscriberId, bundleIds, subIdToLanguageCodeMap, languageCodeToNotificationTitleMap, languageCodeToNotificationBodyMap);
            } catch (PushNetworkIoException | PayloadTooLargeException | PayloadInvalidException e) {
                logger.error("unable to send push to {0,number,#}", e);
            }
        });
    }

    default void sendCustomPush(
        PlatformTransactionManager transactionManager, IDaoMapper dao, IPushService pushService, Logger logger,
        long subscriberId, String languageCode, int appId, String apsCategory, String notificationTitle, String notificationBody, String type, Map<String, Object> extras)
    {
        //these are required by pushes for APNs
        Map<String, String> aps = new HashMap<>();
        aps.put("category", apsCategory);
        aps.put("alert", notificationBody);
        aps.put("sound", "default");

        final Map<String, Object> msgValues = new HashMap<>();
        msgValues.put("aps", aps);
        msgValues.put("type", type);
        if (extras != null) {
            msgValues.put("extras", extras);
        }

        //these are required for pushes by FCM
        Map<Long, String> subIdToLanguageCodeMap = new FastMap<>(subscriberId, languageCode);
        Map<String, String> languageCodeToNotificationTitleMap = new FastMap<>(languageCode, notificationTitle);
        Map<String, String> languageCodeToNotificationBodyMap = new FastMap<>(languageCode, notificationBody);

        Set<String> bundleIds = getAppBundleIds(appId, transactionManager, dao);
        try {
//logger.info(">>> about to send push...");
            pushService.pushNotificationToSubscriber(msgValues, subscriberId, bundleIds, subIdToLanguageCodeMap, languageCodeToNotificationTitleMap, languageCodeToNotificationBodyMap);
        } catch (PushNetworkIoException | PayloadTooLargeException | PayloadInvalidException e) {
            logger.error("unable to send push to {0,number,#}", e);
        }
    }

    default Set<String> getAppBundleIds(int appId, PlatformTransactionManager transactionManager, IDaoMapper dao)
    {
        Set<String> appBundleIds = _appIdToBundleIdsMap.get(appId);
        if (appBundleIds == null) {
            appBundleIds = new HashSet<>();

            DefaultTransactionDefinition txDef = new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_NESTED);
            TransactionStatus txStatus = transactionManager.getTransaction(txDef);
            try {
                List<String> bundleIdsForApp = dao.getBundleIdsForApp(appId);
                for (String bundleId : bundleIdsForApp) {
                    if (bundleId != null) {
                        appBundleIds.add(bundleId);
                    }
                }

                transactionManager.commit(txStatus);
                txStatus = null;

            } finally {
                if (txStatus != null) {
                    transactionManager.rollback(txStatus);
                    txStatus = null;
                }
            }

            _appIdToBundleIdsMap.put(appId, appBundleIds);
        }

        return appBundleIds;
    }

}
