package com.meinc.identity.helper;

import java.io.IOException;

import com.meinc.facebook.exception.FacebookAuthenticationNeededException;
import com.meinc.facebook.exception.FacebookGeneralException;
import com.meinc.facebook.exception.FacebookPostInvalidException;
import com.meinc.facebook.exception.PostLimitExceededException;
import com.meinc.http.domain.HttpRequest;
import com.meinc.http.domain.HttpResponse;
import com.meinc.http.domain.NotAuthorizedException;

public interface FacebookHandler {
    public HttpResponse handleRequest(HttpRequest request, long subscriberId)
    throws IOException, FacebookGeneralException, NotAuthorizedException, FacebookAuthenticationNeededException, PostLimitExceededException, FacebookPostInvalidException;
    public boolean requiresAuthentication();
}
