package com.meinc.gameplay.domain;

import java.io.Serializable;

/**
 * Used by the tools app
 */
public class EventWinner2 
implements Serializable
{
    private static final long serialVersionUID = 1L;

    public static enum TYPE {CC_LEVEL_WINNER, LEADERBOARD_WINNER, QUESTION_WINNER};
    
    public TYPE type;
    public Integer questionNumber;
    public Integer ccLevelNumber;
    public Integer leaderboardPosition;
    public int eventId;
    public int subscriberId;
    public String nickname;
    public String languageCode;
    public String email;
    public String fromCountryCode;
    public String prizeDescription;
    
    public EventWinner2()
    {
    }
    
    public EventWinner2(
        TYPE type, Integer questionNumber, Integer ccLevelNumber, Integer leaderboardPosition, int eventId,
        int subscriberId, String nickname, String languageCode, String email, String fromCountryCode, 
        String prizeDescription)
    {
        this.type = type;
        this.questionNumber = questionNumber;
        this.ccLevelNumber = ccLevelNumber;
        this.leaderboardPosition = leaderboardPosition;
        this.eventId = eventId;
        this.subscriberId = subscriberId;
        this.nickname = nickname;
        this.languageCode = languageCode;
        this.email = email;
        this.fromCountryCode = fromCountryCode;
        this.prizeDescription = prizeDescription;
    }
    
//    @Override
//    public String toString()
//    {
//        switch (type)
//        {
//            case QUESTION_WINNER:
//                return MessageFormat.format(
//                    "type: {0}, question#: {1,number,#}, sId: {3,number,#}, nickname: {4}, language: {5}, email: {6}, country: {7}, prize: {8}", 
//                    type, questionNumber, ccLevelNumber, subscriberId, nickname, languageCode, email, fromCountryCode, prizeDescription);
//            case LEADERBOARD_WINNER:
//                return MessageFormat.format(
//                    "type: {0}, sId: {3,number,#}, nickname: {4}, language: {5}, email: {6}, country: {7}, prize: {8}", 
//                    type, questionNumber, ccLevelNumber, subscriberId, nickname, languageCode, email, fromCountryCode, prizeDescription);
//            case CC_LEVEL_WINNER:
//                return MessageFormat.format(
//                    "type: {0}, level#: {2,number,#}, sId: {3,number,#}, nickname: {4}, language: {5}, email: {6}, country: {7}, prize: {8}", 
//                    type, questionNumber, ccLevelNumber, subscriberId, nickname, languageCode, email, fromCountryCode, prizeDescription);
//        }
//        
//        return "<UNKNOWN TYPE: " + type + ">";
//    }
    
}
