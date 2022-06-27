package com.meinc.facebook.exception;

public class FacebookAuthenticationNeededException extends Exception {
    
    private static final long serialVersionUID = 5568346260195782498L;
    private int _subscriberId;
    private String _facebookId;
    
    public FacebookAuthenticationNeededException(int subscriberId) {
        super("subscriber " + subscriberId + " nees to re-auth with Facebook");
        _subscriberId = subscriberId;
    }
    
    public FacebookAuthenticationNeededException(int subscriberId, Throwable e) {
        super("subscriber " + subscriberId + " nees to re-auth with Facebook", e);
        _subscriberId = subscriberId;
    }

    public FacebookAuthenticationNeededException() {
    }

    public FacebookAuthenticationNeededException(String facebookId) {
        super("facebook user " + facebookId + "needs to re-auth with Facebook");
        _facebookId = facebookId;
    }

    private FacebookAuthenticationNeededException(Throwable arg0) {
        super(arg0);
    }

    private FacebookAuthenticationNeededException(String arg0, Throwable arg1) {
        super(arg0, arg1);
    }

    public int getSubscriberId() {
        return _subscriberId;
    }

    public void setSubscriberId(int subscriberId) {
        _subscriberId = subscriberId;
    }

    public String getFacebookId() {
        return _facebookId;
    }

    public void setFacebookId(String facebookId) {
        _facebookId = facebookId;
    }

}
