/**
 * 
 */
package com.meinc.gameplay.domain;

import java.io.Serializable;

/**
 * @author grant
 *
 */
public class LeaderLight implements Serializable
{
    private static final long serialVersionUID = 6488301396036694285L;
    
    public static final int PLAY_METHOD_SMS = 1;
    public static final int PLAY_METHOD_APP = 2;
    
    private int _subscriberId;
    private String _mintName;
    private int _currentRank;
    private String _languageCode;
    
    public int getSubscriberId() {
        return _subscriberId;
    }
    
    public void setSubscriberId(int subscriberId) {
        _subscriberId = subscriberId;
    }
    
    public String getMintName() {
        return _mintName;
    }
    
    public void setMintName(String mintName) {
        _mintName = mintName;
    }

    public int getCurrentRank() {
        return _currentRank;
    }

    public void setCurrentRank(int currentRank) {
        _currentRank = currentRank;
    }

    public String getLanguageCode()
    {
        return _languageCode;
    }

    public void setLanguageCode(String languageCode)
    {
        _languageCode = languageCode;
    }
}
