package com.meinc.ergo.domain;

import java.util.List;

public class AuditReceipts {
    public List<AuditReceiptRow> receipts;

    public static class AuditReceiptRow {
        public String receiptType;

        // Google & iTunes
        public String itemId;

        // Google recurring
        public String token;
        public String subscriptionId;

        // Google one-time
        public String receipt;    // Also iTunes
        public String signature;
    }
}
