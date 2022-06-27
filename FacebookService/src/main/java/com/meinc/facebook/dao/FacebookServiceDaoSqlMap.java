package com.meinc.facebook.dao;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import com.meinc.facebook.domain.FbCallback;
import com.meinc.facebook.domain.FbSubscriber;
import com.meinc.jdbc.effect.TransactionSideEffectManager;
import com.meinc.mrsoa.distdata.simple.DistributedMap;

public class FacebookServiceDaoSqlMap implements IFacebookServiceDao {
    
    private IFacebookServiceDaoMapper _mapper;
    private DistributedMap<String, FbCallback> _callbackCache;
    private ReentrantLock _callbackCacheLock = new ReentrantLock();
    private ConcurrentHashMap<String, FbCallback> _allCallbacksCache;
    private ReentrantLock allCallbacksLock = new ReentrantLock();
    
    public FacebookServiceDaoSqlMap() {
    }
    
    @Override
    public void start() {
        _callbackCache = DistributedMap.getMap("facebookCallbackCache");
        _allCallbacksCache = new ConcurrentHashMap<String,FbCallback>();
    }
        
    private String callbackToString(FbCallback callback) {
        return callback.getEndpoint().getNamespace() + "-" + callback.getEndpoint().getServiceName() + "-" + callback.getEndpoint().getVersion();
    }
    
    @Override
    public void insertAccessToken(String facebookId, String authToken) {
        _mapper.insertAccessToken(facebookId, authToken);
    }

//    @Override
//    public void setFacebookId(int subscriberId, String facebookId) {
//        _mapper.setFacebookId(subscriberId, facebookId);
//    }

    @Override
    public String getAccessTokenForFbId(String fbId) {
        return _mapper.getAccessTokenForFbId(fbId);
    }

//    @Override
//    public int getSubscriberIdForFacebookId(String facebookId) {
//        Integer id = _mapper.getSubscriberIdForFacebookId(facebookId);
//        if (id == null) {
//            return 0;
//        }
//        return id;
//    }

//    @Override
//    public int getSubscriberIdForAccessToken(String accessToken) {
//        Integer id = _mapper.getSubscriberIdForAccessToken(accessToken);
//        if (id == null) {
//            return 0;
//        }
//        return id;
//    }

    @Override
    public void setSubscriberIdAndAccessToken(String accessToken, String facebookId) {
        _mapper.setSubscriberIdAndAccessToken(accessToken, facebookId);
    }

    @Override
    public void removeAuthTokenForFacebookSubscriber(String facebookId) {
        _mapper.removeAuthTokenForFacebookSubscriber(facebookId);
    }

//    @Override
//    public String getFacebookIdForSubscriber(int subscriberId) {
//        return _mapper.getFacebookIdForSubscriber(subscriberId);
//    }

    @Override
    public void addCallback(final FbCallback callback) {
        _mapper.addCallback(callback);
        TransactionSideEffectManager.runAfterThisTransactionCommit(new Runnable() {
            public void run() {
                _callbackCache.put(callbackToString(callback), callback);
                _allCallbacksCache.clear();
            }
        });
    }

    @Override
    public FbCallback getCallbackForEndpoint(FbCallback callback) {
        FbCallback actual = _callbackCache.get(callbackToString(callback));
        if (actual == null) {
            _callbackCacheLock.lock();
            try {
                actual = _callbackCache.get(callbackToString(callback));
                if (actual == null) {
                    actual = _mapper.getCallbackForEndpoint(callback);
                    if (actual != null) {
                        _callbackCache.put(callbackToString(actual), actual);
                    }
                }
            } finally {
                _callbackCacheLock.unlock();
            }
        }
        return actual;
    }

    @Override
    public Integer removeCallback(final FbCallback callback) {
        Integer result = _mapper.removeCallback(callback);
        TransactionSideEffectManager.runAfterThisTransactionCommit(new Runnable() {
            public void run() {
                _callbackCache.remove(callbackToString(callback));        
                _allCallbacksCache.clear();
            }
        });
        return result;
    }

    @Override
    public Collection<FbCallback> getCallbacks() {
        if (_allCallbacksCache.isEmpty()) {
        	allCallbacksLock.lock();
            try {
                if (_allCallbacksCache.isEmpty()) {
                    List<FbCallback> result = _mapper.getCallbacks();
                    if (result != null) {
                        int counter = 0;
                        for (FbCallback callback : result) {
                            _allCallbacksCache.put(""+ counter, callback);
                            counter++;
                        }
                    }
                }
            } finally {
            	allCallbacksLock.unlock();
            }
        }
        return _allCallbacksCache.values();
    }

    @Override
    public List<FbSubscriber> getFbSubscribers() {
        return _mapper.getFbSubscribers();
    }

    public IFacebookServiceDaoMapper getMapper()
    {
        return _mapper;
    }

    public void setMapper(IFacebookServiceDaoMapper mapper)
    {
        _mapper = mapper;
    }
}
