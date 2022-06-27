package tv.shout.shoutcontestaward.service;


import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.meinc.trigger.domain.Trigger;

import tv.shout.shoutcontestaward.domain.GameInteractionEvent;
import tv.shout.shoutcontestaward.domain.GamePayout;
import tv.shout.shoutcontestaward.domain.GameInteractionEvent.GameBadge;
import tv.shout.shoutcontestaward.domain.GameInteractionEvent.SubscriberStats;

public interface IShoutContestAwardService
{
    public static final String AWARD_MESSAGE_TRIGGER_KEY = "SYNC_MESSAGE";
    public static final String TRIGGER_SERVICE_ROUTE = "tv.shout.shoutcontestaward.eventprocessor";

    public static final String SERVICE_NAMESPACE = "meinc-service";
    public static final String SERVICE_NAME = "ShoutContestAwardService";
    public static final String SERVICE_INTERFACE = "IShoutContestAwardService";
    public static final String SERVICE_VERSION = "1.0";
    
    public static final String NOTIFICATION_TYPE_ROUND_ENDED = "ROUND_ENDED";
    
    public static final String EVENT_KEY_UNKNOWN_EVENT_KEY = "unknown_event_key";

    public static final String USER_EVENT_KEY_user_account_created  = "user_account_created";
    public static final String USER_EVENT_KEY_user_account_loggedin = "user_account_loggedin";
    public static final String USER_EVENT_KEY_user_account_updated  = "user_account_updated";
    public static final String USER_EVENT_KEY_item_purchased        = "item_purchased";

    public static final String GAME_EVENT_KEY_game_started    = "game_started";       
    public static final String GAME_EVENT_KEY_game_ended      = "game_ended";         
    public static final String GAME_EVENT_KEY_question        = "question";           
    public static final String GAME_EVENT_KEY_question_result = "question_result";    
    public static final String GAME_EVENT_KEY_round_started   = "round_started";      
    public static final String GAME_EVENT_KEY_round_ended     = "round_ended";        
    
    public static final String GAME_KEY_game_won              = "game_won";
    public static final String GAME_KEY_game_lost             = "game_lost";
    public static final String ROUND_KEY_round_won            = "round_won";
    public static final String ROUND_KEY_round_lost           = "round_lost";
    public static final String ROUND_KEY_round_lost_in_the_money = "round_lost_in_the_money";    
    public static final String MATCH_KEY_match_won            = "match_won";
    public static final String MATCH_KEY_match_lost           = "match_lost";

    public static final String QUESTION_KEY_question_received          = "question_received";
    public static final String QUESTION_KEY_question_won_correct       = "question_won_correct";
    public static final String QUESTION_KEY_question_won_faster        = "question_won_faster";
    public static final String QUESTION_KEY_question_won_timeout       = "question_won_timeout";
    public static final String QUESTION_KEY_question_lost_incorrect    = "question_lost_incorrect";
    public static final String QUESTION_KEY_question_lost_slower       = "question_lost_slower";
    public static final String QUESTION_KEY_question_lost_timeout      = "question_lost_timeout";
    public static final String QUESTION_KEY_question_lost_all_timeout  = "question_lost_all_timeout";
//  public static final String QUESTION_KEY_round_won_correct          = "round_won_correct";
//  public static final String QUESTION_KEY_round_won_faster           = "round_won_faster";
//  public static final String QUESTION_KEY_round_won_timeout          = "round_won_timeout";
//  public static final String QUESTION_KEY_round_lost_wrong           = "round_lost_wrong";
//  public static final String QUESTION_KEY_round_lost_slower          = "round_lost_slower";
//  public static final String QUESTION_KEY_round_lost_timeout         = "round_lost_timeout";
//  public static final String QUESTION_KEY_round_lost_both_timeout    = "round_lost_both_timeout";    
    
    
    public static final String BADGE_KEY_user_account_updated         = "badge_user_account_updated"        ;
    public static final String BADGE_KEY_user_account_profile_picture = "badge_user_account_profile_picture";
    public static final String BADGE_KEY_player_played_first_round    = "badge_player_played_first_round"   ;
    public static final String BADGE_KEY_player_won_first_round       = "badge_player_won_first_round"      ;
    public static final String BADGE_KEY_player_rounds_played         = "badge_player_rounds_played"        ; // badge key base
    public static final String BADGE_KEY_player_rounds_played_5       = "badge_player_rounds_played_5"      ;
    public static final String BADGE_KEY_player_rounds_played_10      = "badge_player_rounds_played_10"     ;
    public static final String BADGE_KEY_player_rounds_played_15      = "badge_player_rounds_played_15"     ;
    public static final String BADGE_KEY_player_rounds_played_25      = "badge_player_rounds_played_25"     ;
    public static final String BADGE_KEY_player_rounds_played_50      = "badge_player_rounds_played_50"     ;
    public static final String BADGE_KEY_player_rounds_played_100     = "badge_player_rounds_played_100"    ;
    public static final String BADGE_KEY_player_rounds_played_200     = "badge_player_rounds_played_200"    ;
    public static final String BADGE_KEY_player_rounds_played_500     = "badge_player_rounds_played_500"    ;
    public static final String BADGE_KEY_player_cash_level_attained   = "badge_player_cash_level_attained"  ;
    public static final String BADGE_KEY_player_cash_awarded          = "badge_player_cash_awarded"         ;
    public static final String BADGE_KEY_correct_answer_streak        = "badge_correct_answer_streak"       ; // badge key base
    public static final String BADGE_KEY_correct_answer_streak_5      = "badge_correct_answer_streak_5"     ;
    public static final String BADGE_KEY_correct_answer_streak_10     = "badge_correct_answer_streak_10"    ;
    public static final String BADGE_KEY_correct_answer_streak_15     = "badge_correct_answer_streak_15"    ;
    public static final String BADGE_KEY_correct_answer_streak_20     = "badge_correct_answer_streak_20"    ;
    public static final String BADGE_KEY_correct_answer_streak_25     = "badge_correct_answer_streak_25"    ;    
    
    void start();
    
    void stop();
    
    boolean processTriggerMessages(Trigger trigger);
    
    List<SubscriberStats> getSubscriberStats(int subscriberId);
    
    List<GameInteractionEvent> getEventsForSubscriber(int subscriberId);
    
    List<GamePayout> getGamePayoutsForSubscriber(int subscriberId);
    
    List<GameBadge> getBadgesForSubscriber(int subscriberId);
    
    void redeemGamePayout(int gamePayoutId) throws Exception;
    
    GamePayout createGamePayout(int contextId, Integer subscriberId, String gameId, String roundId, String roundSequence, Double amount);
}
