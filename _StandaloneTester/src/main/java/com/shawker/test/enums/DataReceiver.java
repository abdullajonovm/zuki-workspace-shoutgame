package com.shawker.test.enums;

public interface DataReceiver<T,E>
{
    void onSuccess(T type, Object payload);
    void onFailure(T type, E errorType, int httpCode);
}
