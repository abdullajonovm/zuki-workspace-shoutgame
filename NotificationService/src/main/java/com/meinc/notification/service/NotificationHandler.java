package com.meinc.notification.service;

import java.io.IOException;

import com.meinc.http.domain.HttpRequest;
import com.meinc.http.domain.HttpResponse;

public interface NotificationHandler {
    public HttpResponse handleRequest(HttpRequest request, long subscriberId) throws IOException;
}
