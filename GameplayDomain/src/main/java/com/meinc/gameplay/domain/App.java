package com.meinc.gameplay.domain;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.List;

public class App 
implements Serializable
{
    private static final long serialVersionUID = 1L;
    public static final int classId = 1011;
    
    public static enum METHOD {GET, POST}
    public static enum PROTOCOL {http, https}
    
    public static enum POLICY {NOT_SUPPORTED, DO_NOTHING, SEND_PUSH, SEND_NOTIFICATION, CALL_SERVER_API, SEND_PUSH_CALL_SERVER_API, SEND_NOTIFICATION_CALL_SERVER_API}
    
    //PK in db
    public static final int ACTIVITY_AWARD_CREDS = 1;  
    public static final int ACTIVITY_GAME_STARTED = 2;
    public static final int ACTIVITY_GAME_ENDED = 3;
    public static final int ACTIVITY_RESULTS_PUBLISHED = 4;
    public static final int ACTIVITY_QUESTION_STARTED = 5;
    public static final int ACTIVITY_QUESTION_BATCH_STARTED = 6;
    public static final int ACTIVITY_ANSWER_TOO_LATE = 7;
    public static final int ACTIVITY_PLAYER_ON_LEADERBOARD = 8;
    public static final int ACTIVITY_PLAYER_WON_DEAL = 9;
    public static final int ACTIVITY_PLAYER_GAME_WINNER = 10;
    public static final int ACTIVITY_POWERUP_FAILED = 11;
    public static final int ACTIVITY_THROWDOWN_CONFIRMED = 12;
    public static final int ACTIVITY_THROWDOWN_MESSAGE = 13;
    public static final int ACTIVITY_VIPBOX_INVITATION_REQUEST = 14;
    public static final int ACTIVITY_VIPBOX_INVITATION_ACCEPTED = 15;
    public static final int ACTIVITY_VIPBOX_MEMBERSHIP_ADDED = 16;
    public static final int ACTIVITY_MESSAGE = 17;
    public static final int ACTIVITY_EARNED_CREDS = 18;
    
    //DONE - Don't need since don't use to send an actual push but to decide whether to send creds
    //public static final String ACTIVITY_AWARD_CREDS_LOCALIZED_UUID = "a8a0ea19-517e-46cd-b2c0-bcb282bbc9de";
    /*
    select * from localized where localized_uuid in (
                    '0dbccb74-dc8d-4519-a411-779cab3c2f6d', 'd11a50ac-c1af-4fb8-b52a-f97add50214a', 'a160c5bf-b2ac-44f5-a665-8d553198cb15', '7d18e156-d34f-4d2f-843d-d22a7a510d5d', 'db5b8a08-388a-4570-89d5-6d0e14f5109b',
                     '0321119a-ad66-4bb2-b866-707e75c51b1d', 'd3902c6b-24de-4c67-ae33-ce76ca43f11c', '65fbf79c-81bd-4fbd-8663-7e05398256b7',
                      'c86866bd-052d-4484-b895-c788f7f6c01a', '6030b8eb-3a9c-4c83-82d0-97e79f6df31c',
                       '6030b8eb-3a9c-4c83-82d0-97e79f6df31c', '75948db8-52ce-4403-be44-6b332e1b6947', '7bb4c413-4569-441e-838f-3be3b3f57fef',
                        '4e0a9967-a9e3-4cb3-8937-888664e143ac', '9d1869c1-3453-4b5c-b7c7-1f461e09124d',
                         '0693613c-8688-4e1e-94c5-7f6ccd22d00d', '5e1cb5c2-3b01-4380-bc43-3c3dc1cffcbb', 
                         'cb991241-843a-4aae-8731-922b38323008', 'cb991241-843a-4aae-8731-922b38323008', 'acc6245b-ba93-4ae9-b034-bf1e2392cab8',
                         'ab2cb753-092b-4d87-a0c8-9da985b66a9d', '8d0a02d3-2dcf-409d-943e-c07e43001eb1',
                         'f8784277-5fc1-4cdf-af57-299650a74120');
                         
    select * from localized where localized_uuid in (
        '33a5050e-a2f5-11e3-aea6-aba43316d4e6', '58012b30-a2f5-11e3-aea6-aba43316d4e6', '02557e92-a2f6-11e3-aea6-aba43316d4e6',
        '04d9938c-a2f7-11e3-aea6-aba43316d4e6', '35f24176-a2f7-11e3-aea6-aba43316d4e6', '60870a5c-a2f7-11e3-aea6-aba43316d4e6',
        'df1eaa32-a2f7-11e3-aea6-aba43316d4e6', '15d55a58-a2fd-11e3-aea6-aba43316d4e6', '37dee592-a2fd-11e3-aea6-aba43316d4e6',
        '69ba3fda-a2fd-11e3-aea6-aba43316d4e6', '97822ebe-a2fd-11e3-aea6-aba43316d4e6', 'b89644b4-a2fd-11e3-aea6-aba43316d4e6',
        '0321119a-ad66-4bb2-b866-707e75c51b1d', 'f8784277-5fc1-4cdf-af57-299650a74120', 'd3902c6b-24de-4c67-ae33-ce76ca43f11c',
        '65fbf79c-81bd-4fbd-8663-7e05398256b7', 'e381bf5a-2633-4db3-8745-4144f660abb1', 'f73fc8e1-d658-4eb7-a168-68572500726c',
        '068b86e9-dd59-4e11-801d-ac3175896fbb', 'a7ba766e-d977-42d3-b483-9e16df8742bd', 'e114f8a3-a419-4a6e-8b0d-6c13e858b53b',
        'c86866bd-052d-4484-b895-c788f7f6c01a', '06776186-7e00-4171-8c64-d31e3d70f6c9', '4361a63c-6fdf-4935-a156-e5d30dc615a1',
        '75948db8-52ce-4403-be44-6b332e1b6947', 'a3869306-9f2b-4ec7-a5e4-07fcd0cd609d', '7bb4c413-4569-441e-838f-3be3b3f57fef',
        '63ca71cb-de7b-4292-be41-1eb0bf67b5c1', '1aa1dbe6-3493-4826-bada-a9468b4b7b6f', '308bab5d-1c0e-4692-a4c6-713fcafc1c35',
        '43e85f41-f232-4980-ae7b-2667a6de33e9', '042be314-7b5f-4ab1-8847-0a7c000526cc', 'b49c1201-f611-46b9-a611-527e634af074',
        'e104ee33-0daf-428b-8b08-37d899eccea3', '1e3525ce-300d-43ba-9258-11bd0aa7c400', 'fb6e31cd-00af-4306-aef9-5db9fd15b781',
        'e9832011-3a44-4451-8a32-08240f35a41c', '9024bc73-4942-43ae-abf0-3fc91c316e12', 'a9fd27ca-c321-44ed-85f0-a6a6e6d17546',
        'ffee8b29-bda6-4ede-bf53-d83bad42c27b', '7ec6b94e-ad5e-43e5-9655-8abd81b3206f'
    );
    */
    
    //DONE
    public static final String ACTIVITY_GAME_STARTED_LOCALIZED_UUID = "0dbccb74-dc8d-4519-a411-779cab3c2f6d";
    
    //DONE
    public static final String ACTIVITY_GAME_ENDED_LOCALIZED_UUID = "d11a50ac-c1af-4fb8-b52a-f97add50214a";
    
    //DONE
    public static final String ACTIVITY_RESULTS_PUBLISHED_LOCALIZED_UUID = "a160c5bf-b2ac-44f5-a665-8d553198cb15";
    
    //DONE
    public static final String ACTIVITY_QUESTION_STARTED_LOCALIZED_UUID = "7d18e156-d34f-4d2f-843d-d22a7a510d5d";
    
    //DONE
    public static final String ACTIVITY_QUESTION_BATCH_STARTED_LOCALIZED_UUID = "db5b8a08-388a-4570-89d5-6d0e14f5109b";
    
    //DONE
    public static final String ACTIVITY_ANSWER_TOO_LATE_LOCALIZED_UUID = "0321119a-ad66-4bb2-b866-707e75c51b1d";

    //DONE
    public static final String ACTIVITY_ANSWER_TOO_LATE_TITLE_LOCALIZED_UUID = "f8784277-5fc1-4cdf-af57-299650a74120";
    
    //DONE
    public static final String ACTIVITY_PLAYER_ON_LEADERBOARD_LOCALIZED_UUID = "d3902c6b-24de-4c67-ae33-ce76ca43f11c";
    
    //DONE
    public static final String ACTIVITY_PLAYER_WON_DEAL_LOCALIZED_UUID = "65fbf79c-81bd-4fbd-8663-7e05398256b7";
    
    //DONE
    public static final String ACTIVITY_PLAYER_WON_DEAL_CC_TITLE_LOCALIZED_UUID = "7ec6b94e-ad5e-43e5-9655-8abd81b3206f";

    //DONE
    public static final String ACTIVITY_PLAYER_WON_DEAL_CC_MSG_LOCALIZED_UUID = "6f163cd3-8668-4d5c-82c5-286de302bb6b";
    
    //DONE
    public static final String ACTIVITY_PLAYER_WON_CONSOLATION_DEAL_LOCALIZED_UUID = "e381bf5a-2633-4db3-8745-4144f660abb1";
    
    //DONE
    public static final String ACTIVITY_PLAYER_WON_LEADERBOARD_DEAL_LOCALIZED_UUID = "f73fc8e1-d658-4eb7-a168-68572500726c";
    
    //DONE
    public static final String ACTIVITY_PLAYER_WON_DEAL_EMAIL_MESSAGE_LOCALIZED_UUID = "068b86e9-dd59-4e11-801d-ac3175896fbb";
    
    //DONE
    public static final String ACTIVITY_PLAYER_WON_DEAL_EMAIL_TITLE_LOCALIZED_UUID = "a7ba766e-d977-42d3-b483-9e16df8742bd";
    
    //DONE
    public static final String ACTIVITY_PLAYER_WON_DEAL_EMAIL_FROM_LOCALIZED_UUID = "e114f8a3-a419-4a6e-8b0d-6c13e858b53b";
    
    //DONE
    public static final String ACTIVITY_PLAYER_GAME_WINNER_LOCALIZED_UUID = "c86866bd-052d-4484-b895-c788f7f6c01a";
    
    //DONE
    public static final String ACTIVITY_PLAYER_GAME_WINNER_SHOUT_NETWORK_LOCALIZED_UUID = "06776186-7e00-4171-8c64-d31e3d70f6c9";
    
    //DONE
    public static final String ACTIVITY_PLAYER_GAME_WINNER_SHOUT_RECRUITER_LOCALIZED_UUID = "4361a63c-6fdf-4935-a156-e5d30dc615a1";
    
    //DONE
    public static final String ACTIVITY_THROWDOWN_CONFIRMED_LOCALIZED_UUID = "75948db8-52ce-4403-be44-6b332e1b6947";

    //DONE
    public static final String ACTIVITY_THROWDOWN_CONFIRMED_FULL_MSG_LOCALIZED_UUID = "a3869306-9f2b-4ec7-a5e4-07fcd0cd609d";

    
    
    public static final String ACTIVITY_POWERUP_FAILED_LOCALIZED_UUID = "6030b8eb-3a9c-4c83-82d0-97e79f6df31c";
    
    
    
    
    
    //DONE
    public static final String ACTIVITY_THROWDOWN_MESSAGE_WON_BELOW_LINE_TITLE_LOCALIZED_UUID = "1aa1dbe6-3493-4826-bada-a9468b4b7b6f";
    
    //DONE
    public static final String ACTIVITY_THROWDOWN_MESSAGE_WON_BELOW_LINE_MSG_LOCALIZED_UUID = "308bab5d-1c0e-4692-a4c6-713fcafc1c35";
    
    //DONE
    public static final String ACTIVITY_THROWDOWN_MESSAGE_WON_FINALLEVEL_BELOW_LINE_TITLE_LOCALIZED_UUID = "43e85f41-f232-4980-ae7b-2667a6de33e9";
    
    //DONE
    public static final String ACTIVITY_THROWDOWN_MESSAGE_WON_FINALLEVEL_BELOW_LINE_MSG_LOCALIZED_UUID = "042be314-7b5f-4ab1-8847-0a7c000526cc";
    
    //DONE
    public static final String ACTIVITY_THROWDOWN_MESSAGE_TIED_TITLE_LOCALIZED_UUID = "b49c1201-f611-46b9-a611-527e634af074";
    
    //DONE
    public static final String ACTIVITY_THROWDOWN_MESSAGE_TIED_MSG_LOCALIZED_UUID = "e104ee33-0daf-428b-8b08-37d899eccea3";
    
    //DONE
    public static final String ACTIVITY_THROWDOWN_MESSAGE_TIED_FINALLEVEL_TITLE_LOCALIZED_UUID = "1e3525ce-300d-43ba-9258-11bd0aa7c400";
    
    //DONE
    public static final String ACTIVITY_THROWDOWN_MESSAGE_TIED_FINALLEVEL_MSG_LOCALIZED_UUID = "fb6e31cd-00af-4306-aef9-5db9fd15b781";
    
    //DONE
    public static final String ACTIVITY_THROWDOWN_MESSAGE_LOST_TITLE_LOCALIZED_UUID = "e9832011-3a44-4451-8a32-08240f35a41c";
    
    //DONE
    public static final String ACTIVITY_THROWDOWN_MESSAGE_LOST_MSG_LOCALIZED_UUID = "9024bc73-4942-43ae-abf0-3fc91c316e12";
    
    //DONE
    public static final String ACTIVITY_THROWDOWN_MESSAGE_LOST_FINALLEVEL_TITLE_LOCALIZED_UUID = "a9fd27ca-c321-44ed-85f0-a6a6e6d17546";
    
    //DONE
    public static final String ACTIVITY_THROWDOWN_MESSAGE_LOST_FINALLEVEL_MSG_LOCALIZED_UUID = "ffee8b29-bda6-4ede-bf53-d83bad42c27b";
    
    //DONE
    public static final String ACTIVITY_THROWDOWN_MESSAGE_WON_POINTS_LOCALIZED_UUID = "7bb4c413-4569-441e-838f-3be3b3f57fef";
    
    //DONE
    public static final String ACTIVITY_THROWDOWN_MESSAGE_PAIRED_LOCALIZED_UUID = "63ca71cb-de7b-4292-be41-1eb0bf67b5c1";
    
    //DONE
    public static final String ACTIVITY_VIPBOX_INVITATION_REQUEST_WITHEVENT_LOCALIZED_UUID = "4e0a9967-a9e3-4cb3-8937-888664e143ac";
    
    //DONE
    public static final String ACTIVITY_VIPBOX_INVITATION_REQUEST_NO_EVENT_LOCALIZED_UUID = "9d1869c1-3453-4b5c-b7c7-1f461e09124d";
    
    //DONE
    public static final String ACTIVITY_VIPBOX_INVITATION_REQUEST_EMAIL_LOCALIZED_UUID = "0693613c-8688-4e1e-94c5-7f6ccd22d00d";
    
    //DONE
    public static final String ACTIVITY_VIPBOX_INVITATION_REQUEST_ACCEPT_INVITATION_LOCALIZED_UUID = "5e1cb5c2-3b01-4380-bc43-3c3dc1cffcbb";
    
    //DONE
    public static final String ACTIVITY_VIPBOX_INVITATION_ACCEPTED_LOCALIZED_UUID = "cb991241-843a-4aae-8731-922b38323008";
    
    //DONE
    public static final String ACTIVITY_VIPBOX_INVITATION_ACCEPTED_EMAIL_LOCALIZED_UUID = "cb991241-843a-4aae-8731-922b38323008";
    
    //DONE
    public static final String ACTIVITY_VIPBOX_MEMBERSHIP_ADDED_LOCALIZED_UUID = "acc6245b-ba93-4ae9-b034-bf1e2392cab8";
    
    //DONE
    public static final String ACTIVITY_VIPBOX_MEMBERSHIP_ADDED_EMAIL_LOCALIZED_UUID = "ab2cb753-092b-4d87-a0c8-9da985b66a9d";
    
    //DONE - don't need a localized message since it's custom every time.
    //-public static final String ACTIVITY_MESSAGE_LOCALIZED_UUID = "e418c7db-d2b1-4b2f-ac41-1a468e834e40";
    
    //DONE
    public static final String ACTIVITY_EARNED_CREDS_LOCALIZED_UUID = "8d0a02d3-2dcf-409d-943e-c07e43001eb1";
    
    public static final String EVENT_PRIZE_WINNER_EMAIL_MYMADRID_LOCALIZED_UUID = "30cce132-3aa6-11e4-9f0a-123139429dc1";
    public static final String EVENT_PRIZE_WINNER_EMAIL_DEFAULT_LOCALIZED_UUID = "a7814712-48b5-11e4-8eeb-22000a2f80d8";
    
    public static final String UUID_EVENTRESULTS_GRAND_PRIZE_WINNERS_TITLE = "33a5050e-a2f5-11e3-aea6-aba43316d4e6";
    public static final String UUID_EVENTRESULTS_GRAND_PRIZE_WINNERS_DESCRIPTION = "58012b30-a2f5-11e3-aea6-aba43316d4e6";
    public static final String UUID_EVENTRESULTS_QUESTION_PARENT_TITLE_SINGULAR = "02557e92-a2f6-11e3-aea6-aba43316d4e6";
    public static final String UUID_EVENTRESULTS_QUESTION_PARENT_TITLE_PLURAL = "04d9938c-a2f7-11e3-aea6-aba43316d4e6";
    public static final String UUID_EVENTRESULTS_QUESTION_PARENT_DESCRIPTION = "35f24176-a2f7-11e3-aea6-aba43316d4e6";
    public static final String UUID_EVENTRESULTS_QUESTION_TITLE_SINGULAR = "60870a5c-a2f7-11e3-aea6-aba43316d4e6";
    public static final String UUID_EVENTRESULTS_QUESTION_TITLE_PLURAL = "df1eaa32-a2f7-11e3-aea6-aba43316d4e6";
    public static final String UUID_EVENTRESULTS_CCLEVEL_PARENT_TITLE_SINGULAR = "15d55a58-a2fd-11e3-aea6-aba43316d4e6";
    public static final String UUID_EVENTRESULTS_CCLEVEL_PARENT_TITLE_PLURAL = "37dee592-a2fd-11e3-aea6-aba43316d4e6";
    public static final String UUID_EVENTRESULTS_CCLEVEL_PARENT_DESCRIPTION = "69ba3fda-a2fd-11e3-aea6-aba43316d4e6";
    public static final String UUID_EVENTRESULTS_CCLEVEL_TITLE_SINGULAR = "97822ebe-a2fd-11e3-aea6-aba43316d4e6";
    public static final String UUID_EVENTRESULTS_CCLEVEL_TITLE_PLURAL = "b89644b4-a2fd-11e3-aea6-aba43316d4e6";
    
    public static final String UUID_VIRTUAL_SEAT_READY = "062c0aa4-ccb8-11e3-90d9-22000a66836a";
    
    public static final String UUID_5050_PRIZE_NOTIFICATION_TITLE = "4a780fc5-8fa5-11e4-a37a-22000b2308af";
    public static final String UUID_5050_PRIZE_NOTIFICATION_MSG = "7865c9c1-8fa5-11e4-a37a-22000b2308af";
    
    public static final String REPLACEMENT_KEY_EVENT_NAME = "%EVENT_NAME%";
    public static final String REPLACEMENT_KEY_QUESTION_NUM = "%QUESTION_NUMBER%";
    public static final String REPLACEMENT_KEY_NUM_QUESTIONS = "%NUM_QUESTIONS%";
    public static final String REPLACEMENT_KEY_NICKNAME = "%NICKNAME%";
    public static final String REPLACEMENT_KEY_VIPBOX_NAME = "%VIPBOXNAME%";
    public static final String REPLACEMENT_NAME = "%NAME%";
    public static final String REPLACEMENT_CREDS_AMOUNT = "%CREDSAMOUNT%";
    public static final String REPLACEMENT_SPONSOR_NAME = "%SPONSOR_NAME%";
    public static final String REPLACEMENT_MESSAGE = "%MESSAGE%";
    public static final String REPLACEMENT_PRIZES = "%PRIZES%";
    public static final String REPLACEMENT_POINTS1 = "%POINTS1%";
    public static final String REPLACEMENT_POINTS2 = "%POINTS2%";
    public static final String REPLACEMENT_TO_NAME = "%TO_NAME%";
    public static final String REPLACEMENT_OPPONENTS = "%OPPONENTS%";
    public static final String REPLACEMENT_LEVEL1 = "%LEVEL1%";
    public static final String REPLACEMENT_LEVEL2 = "%LEVEL2%";
    
    private int _appId;
    private String _appName;
    private boolean _firewalled; //are subscribers using this app firewalled from other apps
    private METHOD _method;
    private PROTOCOL _protocol;
    private String _endpoint;
    private Integer _port;
    private String _clientKey;
    private String _iOSBundleId;
    private String _androidBundleId;
    private String _windowsBundleId;
    private String _vipBoxPushType;
    private List<Language> _languageCodes;
    
    public String getiOSBundleId() {
        return _iOSBundleId;
    }
    public void setiOSBundleId(String iOSBundleId) {
        _iOSBundleId = iOSBundleId;
    }
    public String getAndroidBundleId() {
        return _androidBundleId;
    }
    public void setAndroidBundleId(String androidBundleId) {
        _androidBundleId = androidBundleId;
    }
    public String getWindowsBundleId()
    {
        return _windowsBundleId;
    }
    public void setWindowsBundleId(String windowsBundleId)
    {
        _windowsBundleId = windowsBundleId;
    }
    public String getVipBoxPushType() {
        return _vipBoxPushType;
    }
    public void setVipBoxPushType(String vipBoxPushType) {
        _vipBoxPushType = vipBoxPushType;
    }
    public int getAppId()
    {
        return _appId;
    }
    public void setAppId(int appId)
    {
        _appId = appId;
    }
    public String getAppName()
    {
        return _appName;
    }
    public void setAppName(String appName)
    {
        _appName = appName;
    }
    public boolean isFirewalled()
    {
        return _firewalled;
    }
    public void setFirewalled(boolean firewalled)
    {
        _firewalled = firewalled;
    }
    public METHOD getMethod()
    {
        return _method;
    }
    public void setMethod(METHOD method)
    {
        _method = method;
    }
    public PROTOCOL getProtocol()
    {
        return _protocol;
    }
    public void setProtocol(PROTOCOL protocol)
    {
        _protocol = protocol;
    }
    public String getEndpoint()
    {
        return _endpoint;
    }
    public void setEndpoint(String endpoint)
    {
        _endpoint = endpoint;
    }
    
    public Integer getPort()
    {
        return _port;
    }
    public void setPort(Integer port)
    {
        _port = port;
    }
    public String getClientKey()
    {
        return _clientKey;
    }
    public void setClientKey(String clientKey)
    {
        _clientKey = clientKey;
    }
    public List<Language> getLanguageCodes()
    {
        return _languageCodes;
    }
    public void setLanguageCodes(List<Language> languageCodes)
    {
        _languageCodes = languageCodes;
    }
    @Override
    public String toString()
    {
        StringBuilder buf = new StringBuilder();
        buf.append(MessageFormat.format(
            "id: {0,number,#}, name: {1}, method: {2}, protocol: {3}, endpoint: {4}, port: {5,number,#}, ios: {6}, android: {7}, windows: {8}", 
            _appId, _appName, _method, _protocol, _endpoint, _port, _iOSBundleId, _androidBundleId, _windowsBundleId));
        buf.append("\nLANGUAGES: ");
        for (Language language : _languageCodes) {
            buf.append("\n\t").append(language);
        }
        return buf.toString();
    }
}
