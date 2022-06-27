package tv.shout.snowyowl.service;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.meinc.trigger.domain.Trigger;
import com.meinc.webcollector.message.handler.IMessageTypeHandler;

import tv.shout.snowyowl.domain.QuestionCategory;

public interface ISnowyowlService
extends IMessageTypeHandler
{
    public static final String SERVICE_NAMESPACE = "meinc-service";
    public static final String SERVICE_NAME = "SnowyowlService";
    public static final String SERVICE_INTERFACE = "ISnowyowlService";
    public static final String SERVICE_VERSION = "1.0";

    public static final String APP_ID = "snowyowl";
    public static final String GAME_ENGINE = "SNOWYOWL";
    public static final String GAMES_LIST_NAME = "/snowl_games.json";
    public static final String TEST_GAMES_LIST_NAME = "/snowl_test_games.json";

    public static final String GAME_ENGINE_TYPE_FixedRound_SingleLife = "FixedRound_SingleLife";
    public static final String GAME_ENGINE_TYPE_FixedRound_MultiLife = "FixedRound_MultiLife";
    public Set<String> ENGINE_TYPES = new HashSet<>(Arrays.asList(GAME_ENGINE_TYPE_FixedRound_SingleLife, GAME_ENGINE_TYPE_FixedRound_MultiLife));

    public static final String SYNC_MESSAGE_JOINED_GAME = "joined_game";
    public static final String SYNC_MESSAGE_ABANDONED_GAME = "abandoned_game";
    public static final String SYNC_MESSAGE_JOINED_ROUND = "joined_round";
    public static final String SYNC_MESSAGE_ABANDONED_ROUND = "abandoned_round";
    public static final String SYNC_MESSAGE_USER_MATCHED = "user_matched";
    public static final String SYNC_MESSAGE_WILL_NOTIFY_ON_PAIRED = "will_notify_on_paired";
    public static final String SYNC_MESSAGE_QUESTION = "question";
    public static final String SYNC_MESSAGE_QUESTION_RESULT = "question_result";
    public static final String SYNC_MESSAGE_MATCH_RESULT = "match_result";
    public static final String SYNC_MESSAGE_GAME_RESULT = "game_result";
    public static final String SYNC_MESSAGE_ELIMINATED = "eliminated";

    public static final String TRIGGER_SERVICE_ROUTE = "tv.shout.snowyowl.service.snowyowlService";
    public static final String TRIGGER_KEY_SOCKET_IO_MESSAGE = "SYNC_MESSAGE";
    public static final String TRIGGER_KEY_SUBSCRIBER_STATS_MESSAGE = "SUBSCRIBER_STATS_MESSAGE";

    /** how the user would like to be notified when a game first starts pool play and when a game is about to start bracket play */
    public static final int NOTIFICATION_PREF_TYPE_ROUND_START = 13;
    public static final String NOTIFICATION_TYPE_NOTIFY_ON_ROUND_START = "ROUND_START";

    public static final String SMS_BRACKET_STARTING_NOTIFICATION_UUID = "aa1510af-12b1-411d-ae9a-205a42abc378";

    void start();
    void stop();
    void sendDailyStatusEmail();

    boolean processTriggerMessages(Trigger trigger);

    //ActivityCategory
    List<QuestionCategory> getQuestionCategoriesFromCategoryIds(List<String> categoryUuids);

    void subscriberSignupCallback(long subscriberId);

    void checkForAutoStartGames();

}
