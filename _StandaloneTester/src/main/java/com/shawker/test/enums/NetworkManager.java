package com.shawker.test.enums;

import com.shawker.test.enums.NetworkManager.ERROR_TYPE;
import com.shawker.test.enums.NetworkManager.TYPE;

public class NetworkManager
extends BaseNetworkManager<TYPE, ERROR_TYPE>
{
    public enum TYPE {
        GET_DETAILS
    }

    public enum ERROR_TYPE {
        UNKNOWN_ERROR, IOEXCEPTION, JSONPARSEEXCEPTION
    }

    @Override
    public ERROR_TYPE getError(int errorType)
    {
        switch (errorType)
        {
            case BaseNetworkManager.ERROR_IOEXCEPTION:
                return ERROR_TYPE.IOEXCEPTION;

            case BaseNetworkManager.ERROR_JSONPARSEEXCEPTION:
                return ERROR_TYPE.JSONPARSEEXCEPTION;

            default:
                return ERROR_TYPE.UNKNOWN_ERROR;
        }
    }
}
