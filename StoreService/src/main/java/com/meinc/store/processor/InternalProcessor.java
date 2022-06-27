package com.meinc.store.processor;

import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.meinc.store.dao.StoreServiceDaoSqlMap;
import com.meinc.store.domain.Item;
import com.meinc.store.domain.Receipt;
import com.meinc.store.domain.Receipt.ReceiptType;
import com.meinc.store.domain.ReceiptResult;
import com.meinc.store.exception.InvalidJsonException;

public class InternalProcessor extends PaymentProcessor {
    private static final Log logger = LogFactory.getLog(InternalProcessor.class);

    private StoreServiceDaoSqlMap dao;
    private ObjectMapper jsonMapper = new ObjectMapper();

    @Override
    public void setDao(StoreServiceDaoSqlMap dao) {
        this.dao = dao;
    }

    @Override
    List<ReceiptType> getTypes() {
        return Arrays.asList(new ReceiptType[] {ReceiptType.INTERNAL});
    }

    @Override
    public ReceiptResult verifyReceipt(Receipt receipt, String storeBundleId) {
        ReceiptResult result = new ReceiptResult();
        Item item = dao.getItemByUuid(receipt.getItemUuid());
        if (item == null) {
            logger.warn("no item for receipt: " + receipt);
            result.setNoRetryError("invalid item");
            return result;
        }

        Receipt storeReceipt = receipt.clone();

        Integer itemDurationQuantity = item.getDurationQuantity();
        if (itemDurationQuantity == null) {
            storeReceipt.setExpirationDate(null);
        } else {
            Calendar now = Calendar.getInstance();
            now.add(item.getDurationUnit().toCalendarUnit(), itemDurationQuantity);
            storeReceipt.setExpirationDate(now.getTime());
        }

        Map<String,String> payloadJson;
        try {
            payloadJson = jsonMapper.readValue(receipt.getPayload(), new TypeReference<Map<String,String>>() { });
        } catch (JsonParseException e) {
            logger.error("Error parsing internal client receipt: " + e.getMessage(), e);
            result.setNoRetryError("Error reading receipt payload");
            return result;
        } catch (JsonMappingException e) {
            logger.error("Error parsing internal client receipt: " + e.getMessage(), e);
            result.setNoRetryError("Error reading receipt payload");
            return result;
        } catch (IOException e) {
            logger.error("Error parsing internal client receipt: " + e.getMessage(), e);
            result.setNoRetryError("Error reading receipt payload");
            return result;
        }

        String internalTransactionId = UUID.randomUUID().toString();
        String description = (payloadJson.containsKey("description")) ? payloadJson.get("description") : "";
        try {
            storeReceipt.setPayload(Receipt.createInternalPayload(internalTransactionId, description).getBytes());
        } catch (InvalidJsonException e) {
            logger.error(String.format("Error creating internal receipt (%s): %s", e.getMessage(), e.getJson()));
            result.setRetryError("Error reading receipt payload");
            return result;
        }

        logReceiptVerified(storeReceipt, internalTransactionId);

        result.setStoreReceipt(storeReceipt);
        return result;
    }

}
