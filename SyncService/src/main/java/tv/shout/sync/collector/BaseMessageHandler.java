package tv.shout.sync.collector;

import static com.meinc.jdbc.SQLError.isTransactionLost;
import static com.meinc.webcollector.message.CollectorMessage.PARM_TO_WDS;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.dao.PessimisticLockingFailureException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.meinc.webcollector.message.CollectorMessage;
import com.meinc.webcollector.message.IWebCollectorMessageBuffer;
import com.meinc.webcollector.message.handler.BadRequestException;
import com.meinc.webdatastore.domain.WebDataStoreObject;
import com.meinc.webdatastore.service.IWebDataStoreService;
import com.meinc.webdatastore.service.WebDataStoreException;

import clientproxy.webdatastoreservice.WebDataStoreServiceClientProxy;
import tv.shout.util.JsonUtil;

public abstract class BaseMessageHandler
implements ICollectorMessageHandler
{
    protected static final long EXPIRES_1_HOUR_MS = 1000L * 60L * 60L;
    protected static ObjectMapper jsonMapper = JsonUtil.getObjectMapper();

    private static Logger _logger = Logger.getLogger(BaseMessageHandler.class);

    protected IWebDataStoreService _wdsService;

    public BaseMessageHandler()
    {
        _wdsService = new WebDataStoreServiceClientProxy(false);
    }

    protected void logCreateMessage()
    {
        if (_logger.isDebugEnabled()) {
            _logger.debug(">>> createMessage: " + getHandlerMessageType());
        }
    }

    @Override
    public void handleMessages(List<CollectorMessage> messages, IWebCollectorMessageBuffer messageBuffer)
    throws BadRequestException
    {
        //boilerplate to process each message and handle exceptions/retries/transactions
        for (CollectorMessage message : messages) {
            //TxODO: handlers can have transaction annotations if necessary so is this manual transaction really necessary?
//            DefaultTransactionDefinition txDef = new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_NESTED);
//            TransactionStatus txStatus = _transactionManager.getTransaction(txDef);
            try {
                //pass off to the implementing class to handle the actual message
                handleMessage(message);

                messageBuffer.removeMessage(message, true);

//                _transactionManager.commit(txStatus);
//                txStatus = null;
            } catch (Throwable t) {
                boolean doPublishResponse = true;
                if (t instanceof SQLException && isTransactionLost((SQLException)t) || t instanceof PessimisticLockingFailureException) {
                    // Transaction was lost
                    _logger.error("Error processing message: " + message + " with " + t.getMessage(), t);
                    doPublishResponse = !messageBuffer.retryMessage(message, 1);
                    if (doPublishResponse)
                        messageBuffer.removeMessage(message, false);
                } else if (t instanceof PublishResponseError) {
                    doPublishResponse = false;
                    messageBuffer.removeMessage(message, false);
                    PublishResponseError pre = (PublishResponseError) t;
                    publishResponseWdsDoc(pre.getToWds(), pre.getMessageId(), pre.getDocType(), pre.isSuccess(), pre.getFailureType(), pre.getFailureMessage());
                } else {
                    _logger.error("Error processing message: " + message + " with " + t.getMessage(), t);
                    messageBuffer.removeMessage(message, false);
                }
                if (doPublishResponse) {
                    String toWds = message.getToWds();
                    if (toWds != null)
                        publishResponseWdsDoc(toWds, message.getMessageId(), "baseMessageHandler", false, "unknownError", null);
                }
//            } finally {
//                if (txStatus != null) {
//                    _transactionManager.rollback(txStatus);
//                    txStatus = null;
//                }
            }
        }
    }

    protected Map<String, String> createProps(String requestPath, Map<String, String> requestHeaders, Map<String, String> requestParameters)
    throws BadRequestException
    {
        if (requestParameters.get(PARM_TO_WDS) == null) {
            _logger.warn("ignoring " + getHandlerMessageType() + " message: missingrequired param: toWds");
            throw new BadRequestException()
                          .withErrorResponseBodyJsonKeyValue("success", false)
                          .withErrorResponseBodyJsonKeyValue("missingRequiredParam", PARM_TO_WDS);
        }

        Map<String, String> props = new HashMap<>();
        props.put(PARM_TO_WDS, requestParameters.get(PARM_TO_WDS));
        props.put("__requestPath", requestPath);

        for(String header : requestHeaders.keySet()) {
            props.put("HEADER_" + header, requestHeaders.get(header));
        }

        return props;
    }

    public void publishResponseWdsDoc(String toWds, String messageId, String docType, boolean success, String failureType, String failureMessage)
    {
        publishResponseWdsDoc(toWds, messageId, docType, success, failureType, failureMessage, null);
    }

    public void publishResponseWdsDoc(String toWds, String messageId, String docType, boolean success, String failureType, String failureMessage, Map<String,Object> otherParams)
    {
        WebDataStoreObject object = createResponseWdsDoc(toWds, messageId, docType, success, failureType, failureMessage, otherParams);
        if (object != null) {
            try {
                _wdsService.createOrUpdateObjectSync(object, 0);
            } catch (WebDataStoreException e) {
                _logger.error(MessageFormat.format("unable to publish response document for message: {0}, docType: {1}", messageId, docType), e);
            } catch (InterruptedException e) {
                _logger.error(MessageFormat.format("unable to publish response document for message: {0}, docType: {1}", messageId, docType), e);
            }
        }
    }

    public static WebDataStoreObject createResponseWdsDoc(String toWds, String messageId, String docType, boolean success, String failureType, String failureMessage)
    {
        return createResponseWdsDoc(toWds, messageId, docType, success, failureType, failureMessage, null);
    }

    public static WebDataStoreObject createResponseWdsDoc(String toWds, String messageId, String docType, boolean success, String failureType, String failureMessage, Map<String,Object> otherParams)
    {
        WebDataStoreObject object = new WebDataStoreObject();
        object.addResponseHeader("Content-Type", "application/json;charset=utf-8");
        object.setToWds(toWds);
        //TOxDO: does this matter? object.setServiceCallbackEndpoint(VoteService.ENDPOINT);

        Map<String, Object> resultMap = new HashMap<String, Object>();
        resultMap.put("success", success);
        if (failureType != null) {
            resultMap.put(failureType, true);
        }
        if (failureMessage != null) {
            resultMap.put("message", failureMessage);
        }
        if (otherParams != null) {
            resultMap.putAll(otherParams);
        }

        try {
            object.setData(jsonMapper.writeValueAsBytes(resultMap));
        } catch (IOException e) {
            //this shouldn't happen in a properly configured system with checks in place on the data
            _logger.error(MessageFormat.format("unable to create result document for message: {0}, docType: {1}", messageId, docType), e);
            return null;
        }
        object.setExpirationDate(new Date(new Date().getTime() + EXPIRES_1_HOUR_MS));
        object.setPath(messageId + "/response.json");
        object.setCallbackPassthrough("MSG_RESPONSE:" + messageId);
        return object;
    }

//    protected int getContextId(Map<String, String> props)
//    {
//        String appName = props.get("appId");
//        if (appName == null || appName.trim().length() == 0) {
//            appName = "SHOUT";
//        }
//        App app = _appHelper.getAppByName(appName);
//        return app == null ? _defaultContextId : app.getAppId();
//    }

    protected String getParamFromProps(Map<String, String> props, String messageId, String docType, String name, boolean isRequired)
    throws PublishResponseError
    {
        if (!props.containsKey(name) || props.get(name) == null) {
            if (isRequired) {
                throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, docType, false, "missingRequiredParam", name);
            } else {
                return null;
            }
        }
        return props.get(name);
    }

    protected Integer getIntParamFromProps(Map<String, String> props, String messageId, String docType, String name, boolean isRequired)
    throws PublishResponseError
    {
        String val = getParamFromProps(props, messageId, docType, name, isRequired);
        try {
            if (val == null) return null; //if it was required, it will have already thrown an exception
            return Integer.parseInt(val);

        } catch (NumberFormatException e) {
            throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, docType, false, "invalidParam", name);
        }
    }

    // ENCRPYTION SERVICE SUPPOT METHODS //

    private static byte[] getFileAsBytes(String filename)
    throws IOException
    {
        File f = new File(filename);
        FileInputStream fis = new FileInputStream(f);
        DataInputStream dis = new DataInputStream(fis);
        byte[] keyBytes = new byte[(int)f.length()];
        dis.readFully(keyBytes);
        dis.close();
        return keyBytes;
    }

    public static PublicKey getPublicKey(String filename)
    throws IOException, GeneralSecurityException
    {
        X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(getFileAsBytes(filename));
        return KeyFactory.getInstance("RSA").generatePublic(x509EncodedKeySpec);
    }

    public static PrivateKey getPrivateKey(String filename)
    throws IOException, GeneralSecurityException
    {
        PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(getFileAsBytes(filename));
        return KeyFactory.getInstance("RSA").generatePrivate(pkcs8EncodedKeySpec);
    }

}
