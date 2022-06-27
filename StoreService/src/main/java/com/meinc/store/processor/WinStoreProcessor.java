package com.meinc.store.processor;

import java.util.Arrays;
import java.util.List;

import com.meinc.store.domain.Receipt;
import com.meinc.store.domain.Receipt.ReceiptType;
import com.meinc.store.domain.ReceiptResult;

public class WinStoreProcessor
extends PaymentProcessor
{
//    private static final Log logger = LogFactory.getLog(WinStoreProcessor.class);
//    private ObjectMapper jsonMapper = new ObjectMapper();

    @Override
    List<ReceiptType> getTypes() {
        return Arrays.asList(new ReceiptType[] {ReceiptType.WIN_STORE});
    }

    @Override
    public ReceiptResult verifyReceipt(Receipt receipt, String storeBundleId) {
        //TODO: if windows processing ever becomes a thing, this will need to be fleshed out
        ReceiptResult result = new ReceiptResult();
        result.setStoreReceipt(receipt.clone());
        return result;
    }

}
