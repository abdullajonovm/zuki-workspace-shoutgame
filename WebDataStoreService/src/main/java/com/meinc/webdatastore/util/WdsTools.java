package com.meinc.webdatastore.util;

import com.meinc.commons.encryption.HexUtils;
import com.meinc.launcher.serverprops.ServerPropertyHolder;

public class WdsTools {
    private static Integer partitionHashPrefixByteCount;

    //NOTE: this method is duplicated in the GamePlayService/PublicProfilePublisher class
    // if you update it here, update it there as well
    public static long getSubscriberPartitionDividend(String emailHash) {
        if (partitionHashPrefixByteCount == null) {
            String selectMethod = ServerPropertyHolder.getProperty("gameplay.srddaemon.srd.server.selectMethod.wds", "sha256%4");
            String[] selectMethodParts = selectMethod.split("%");
            if (selectMethodParts.length != 2)
                throw new IllegalStateException("Invalid selectMethod string: " + selectMethod);
            try {
                partitionHashPrefixByteCount = Integer.parseInt(selectMethodParts[1]);
            } catch (NumberFormatException e) {
                throw new IllegalStateException("Invalid selectMethod string: " + selectMethod);
            }
        }
        
        byte[] emailHashBytes = HexUtils.hexStringToBytes(emailHash);
        long byteValue = 0;
        for (int i = 0; i < partitionHashPrefixByteCount; i++) {
            if (i > 0) byteValue <<= 8;
            byteValue |= (int)emailHashBytes[i] & 0xFF;
        }

        return byteValue % partitionHashPrefixByteCount;
    }
}
