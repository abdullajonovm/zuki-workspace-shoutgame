package com.shawker.test.enums;

public abstract class BaseNetworkManager<T, E>
{
    public static final int ERROR_UNKNOWN = 1;
    public static final int ERROR_IOEXCEPTION = 2;
    public static final int ERROR_JSONPARSEEXCEPTION = 4;

    public void doGet(DataReceiver<T, E> callback, T type, int data)
    {
        switch (data)
        {
            case 0: {
                callback.onSuccess(type, "foo");
            }
            break;

            case 1: {
                callback.onFailure(type, getError(ERROR_JSONPARSEEXCEPTION), 401);
            }
            break;

            case 2: {
                callback.onFailure(type, getError(ERROR_IOEXCEPTION), 503);
            }
            break;

            default: {
                callback.onFailure(type, getError(ERROR_UNKNOWN), 503);
            }
        }
    }

    public abstract E getError(int errorType);
}
