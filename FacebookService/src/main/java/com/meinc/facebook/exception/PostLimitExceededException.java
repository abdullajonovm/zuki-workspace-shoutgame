package com.meinc.facebook.exception;

import java.io.Serializable;

public class PostLimitExceededException extends Exception implements Serializable
{
    private int _facebookLimit;
    private int _attempted;
//    private int _subscriberId;

    private static final long serialVersionUID = 5850836519835657282L;

    public PostLimitExceededException() {
        super();
    }

    public PostLimitExceededException(String arg0, Throwable arg1) {
        super(arg0, arg1);
    }

    public PostLimitExceededException(String arg0) {
        super(arg0);
    }

    public PostLimitExceededException(Throwable arg0) {
        super(arg0);
    }
    
    public PostLimitExceededException(/*int subscriberId, */int postLimit, int attempted) {
        super(/*"subscriber " + subscriberId + */" attempted to post a message to " + attempted + " friends, which is over the Facebook limit of " + postLimit + " posts");
        _facebookLimit = postLimit;
        _attempted = attempted;
//        _subscriberId = subscriberId;
    }

    public int getFacebookLimit() {
        return _facebookLimit;
    }

    public void setFacebookLimit(int facebookLimit) {
        _facebookLimit = facebookLimit;
    }

    public int getAttempted() {
        return _attempted;
    }

    public void setAttempted(int attempted) {
        _attempted = attempted;
    }

//    public int getSubscriberId() {
//        return _subscriberId;
//    }
//
//    public void setSubscriberId(int subscriberId) {
//        _subscriberId = subscriberId;
//    }

}
