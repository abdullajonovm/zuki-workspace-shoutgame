package com.meinc.store.domain;

import java.io.Serializable;
import java.util.List;

public class ReceiptResult implements Serializable {

    private static final long serialVersionUID = 1L;

    private Receipt storeReceipt;
    private List<SubscriberEntitlement> subscriberEntitlements;
    private String noRetryError;
    private String retryError;
    private boolean duplicateReceiptError;
    private boolean invalidReceiptError;
    private boolean malformedReceiptError;
    
    public Receipt getStoreReceipt() {
        return storeReceipt;
    }

    public void setStoreReceipt(Receipt storeReceipt) {
        this.storeReceipt = storeReceipt;
    }

    public List<SubscriberEntitlement> getSubscriberEntitlements() {
        return subscriberEntitlements;
    }

    public void setSubscriberEntitlements(List<SubscriberEntitlement> subscriberEntitlements) {
        this.subscriberEntitlements = subscriberEntitlements;
    }

    public String getNoRetryError() {
        return noRetryError;
    }

    public void setNoRetryError(String receiptInvalidError) {
        this.noRetryError = receiptInvalidError;
    }

    public String getRetryError() {
        return retryError;
    }

    public void setRetryError(String unknownError) {
        this.retryError = unknownError;
    }
    
    @Override
    public String toString() {
        if (storeReceipt != null) {
            StringBuffer result = new StringBuffer();
            result.append(storeReceipt.toString());
            if (subscriberEntitlements != null) {
                result.append(" [");
                for (SubscriberEntitlement subEnt : subscriberEntitlements)
                    result.append(subEnt.toString() + ",");
                result.append("]");
            }
            return result.toString();
        }
        if (noRetryError != null)
            return "receipt invalid: " + noRetryError;
        if (retryError != null)
            return "receipt unknown error: " + retryError;
        if (duplicateReceiptError)
            return "receipt duplicate error: " + duplicateReceiptError;
        return super.toString();
    }

    public boolean isDuplicateReceiptError() {
        return duplicateReceiptError;
    }

    public void setDuplicateReceiptError(boolean duplicateItemError) {
        this.duplicateReceiptError = duplicateItemError;
    }

    public boolean isInvalidReceiptError() {
        return invalidReceiptError;
    }

    public void setInvalidReceiptError(boolean invalidReceiptError) {
        this.invalidReceiptError = invalidReceiptError;
    }

    public boolean isMalformedReceiptError() {
        return malformedReceiptError;
    }

    public void setMalformedReceiptError(boolean malformedReceiptError) {
        this.malformedReceiptError = malformedReceiptError;
    }
    
}
