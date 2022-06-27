package tv.shout.shoutcontestaward.dao;

import java.util.ArrayList;
import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

//import com.meinc.nosql.annotation.DQL;

import tv.shout.shoutcontestaward.domain.GameInteractionEvent;
import tv.shout.shoutcontestaward.domain.GamePayout;
import tv.shout.shoutcontestaward.domain.GameInteractionEvent.GameBadge;
import tv.shout.shoutcontestaward.domain.GameInteractionEvent.SubscriberStats;

public interface IShoutContestAwardServiceDao
{

    @Insert(
        "INSERT INTO gameplay.game_interaction_event (" +
        "   context_id            " +
        "   ,event_type_key       " +
        "   ,game_id              " +
        "   ,round_id             " +
        "   ,level_id             " +
        "   ,level_value          " +
        "   ,subscriber_id        " +
        "   ,association_id       " +
        "   ,association_description   " +
        "   ,target_type          " +
        "   ,points_value         " +
        "   ,purchase_amount      " +
        "   ,award_amount         " +
        "   ,received_payload     " +
        "   ,delivered_payload    " +
        "   ,is_notification      " +
        "   ,is_persisted         " +
        "   ,is_badge             " +
        "   ,is_question_won      " +
        "   ,is_question_lost     " +
        "   ,is_round_won         " +
        "   ,is_round_lost        " +
        "   ,created_date         " +
        ")                        " +
        "VALUES(                  " +
        "    #{contextId}         " +
        "    ,#{eventTypeKey}     " +
        "    ,#{gameId}           " +
        "    ,#{roundId}          " +
        "    ,#{levelId}          " +
        "    ,#{levelValue}       " +
        "    ,#{subscriberId}     " +
        "    ,#{associationId}    " +
        "    ,#{associationDescription}" +        
        "    ,#{targetType}       " +
        "    ,#{pointsValue}      " +
        "    ,#{purchaseAmount}   " +
        "    ,#{awardAmount}      " +
        "    ,#{receivedPayload}  " +
        "    ,#{deliveredPayload} " +
        "    ,#{isNotification}   " +
        "    ,#{isPersisted}      " +
        "    ,#{isBadge}          " +
        "    ,#{isQuestionWon}    " +
        "    ,#{isQuestionLost}   " +
        "    ,#{isRoundWon}       " +
        "    ,#{isRoundLost}      " +
        "    ,NOW()               " +
        ")                        "
        )
    @Options(useGeneratedKeys=true, keyProperty="gameInteractionEventId", keyColumn="game_interaction_event_id")
    public int addGameInteractionEvent(GameInteractionEvent epe);    
    
    @Select(
		"SELECT * FROM gameplay.game_interaction_event" +
		" WHERE event_type_key = #{0}" +
		"   AND subscriber_id = #{1} " +
		" ORDER BY created_date DESC;"
    )
    @ResultMap(value="tv.shout.shoutcontestaward.dao.IXmlEventProcessorDaoMapper.gameInteractionEventMap")
    public List<GameInteractionEvent> getEventsByEventTypeKey(@Param("eventTypeKey") String eventTypeKey, @Param("subscriberId") int subscriberId);    
    
    @Select(
    	"SELECT * FROM gameplay.game_interaction_event" +
        " WHERE subscriber_id = #{subscriberId} " +
        " ORDER BY created_date DESC;"
    )
    @ResultMap(value="tv.shout.shoutcontestaward.dao.IXmlEventProcessorDaoMapper.gameInteractionEventMap")
    public List<GameInteractionEvent> getEventsForSubscriber(@Param("subscriberId") int subscriberId);
    
    @Delete(
    	"DELETE FROM gameplay.game_interaction_event" +
        " WHERE game_interaction_event_id = #{eventId}"
    )
    public void deleteGameInteractionEvent(@Param("eventId") String eventId);
    
    @Select(
		"SELECT gie1.subscriber_id,                                          " +
		"       IFNULL(sp.xp, 0) AS total_xp,                                " +
		"       IFNULL(sp.points, 0) AS total_points,                        " +
		"       SUM(points_value) AS total_event_points,                     " +
		"       SUM(purchase_amount) AS total_purchased_amount,              " +
		"       SUM(award_amount) AS total_awarded_amount,                   " +
		"       IFNULL(gie2.total_questions_won, 0) AS total_questions_won,  " +
		"       IFNULL(gie3.total_questions_lost, 0) AS total_questions_lost," +
		"       IFNULL(gie4.total_rounds_won, 0) AS total_rounds_won,        " +
		"       IFNULL(gie5.total_rounds_lost, 0) AS total_rounds_lost       " +
		"  FROM gameplay.game_interaction_event gie1                         " +
		"  LEFT JOIN gameplay.subscriber_profile sp                          " +
		"    ON gie1.subscriber_id = sp.subscriber_id                        " +
		"  LEFT JOIN                                                         " +
		"        (SELECT subscriber_id, COUNT(1) AS total_questions_won      " +
		"           FROM gameplay.game_interaction_event                     " +
		"          WHERE subscriber_id = #{subscriberId}                     " +
		"            AND is_question_won = 1) AS gie2                        " +
		"    ON gie1.subscriber_id = gie2.subscriber_id                      " +
		"  LEFT JOIN                                                         " +
		"        (SELECT subscriber_id, COUNT(1) AS total_questions_lost     " +
		"           FROM gameplay.game_interaction_event                     " +
		"          WHERE subscriber_id = #{subscriberId}                     " +
		"            AND is_question_lost = 1) AS gie3                       " +
		"    ON gie1.subscriber_id = gie3.subscriber_id                      " +
		"  LEFT JOIN                                                         " +
		"        (SELECT subscriber_id, COUNT(1) AS total_rounds_won         " +
		"           FROM gameplay.game_interaction_event                     " +
		"          WHERE subscriber_id = #{subscriberId}                     " +
		"            AND is_round_won = 1) AS gie4                           " +
		"    ON gie1.subscriber_id = gie4.subscriber_id                      " +
		"  LEFT JOIN                                                         " +
		"        (SELECT subscriber_id, COUNT(1) AS total_rounds_lost        " +
		"           FROM gameplay.game_interaction_event                     " +
		"          WHERE subscriber_id = #{subscriberId}                     " +
		"            AND is_round_lost = 1) AS gie5                          " +
		"    ON gie1.subscriber_id = gie5.subscriber_id                      " +
		" WHERE gie1.subscriber_id = #{subscriberId}                         "
    )
    @ResultMap(value="tv.shout.shoutcontestaward.dao.IXmlEventProcessorDaoMapper.subscriberStatsMap")
    public List<SubscriberStats> getSubscriberStats(@Param("subscriberId") int subscriberId);

    @Select(
    	"SELECT SUM(1) AS total_rounds_played           " +
    	"  FROM gameplay.game_interaction_event         " +
    	" WHERE subscriber_id = #{subscriberId}         " +
    	"   AND (is_round_won = 1 OR is_round_lost = 1);"  		
	)
    public Integer getSubscriberRoundsPlayed(@Param("subscriberId") int subscriberId);
    
    @Select(
		"SELECT                                                " +
		"	CASE WHEN is_question_won = 1 THEN 1               " +
		"        WHEN is_round_won = 1 THEN 1                  " +
		"        WHEN is_question_lost = 1 THEN 0              " +
		"        WHEN is_round_lost = 1 THEN 0                 " +
		"        ELSE 0                                        " +
		"    END AS streak                                     " +
		"  FROM gameplay.game_interaction_event                " +
		" WHERE subscriber_id = #{subscriberId}                " +
		"   AND (is_question_won = 1 OR is_question_lost = 1 OR" +
		"        is_round_won = 1    OR is_round_lost = 1)     " +
		" ORDER BY created_date DESC;                          "
    )
    public ArrayList<Integer> getSubscriberQuestionStreak(@Param("subscriberId") int subscriberId);
    
    @Insert(
        "INSERT INTO gameplay.game_badge (" +
        "   context_id        " +
        "   ,subscriber_id    " +
        "   ,association_id   " +
        "   ,event_type_key   " +
        "   ,badge_key        " +
        "   ,created_date     " +
        ")                    " +
        "VALUES(              " +
        "   #{contextId}      " +
        "   ,#{subscriberId}  " +
        "   ,#{associationId} " +
        "   ,#{eventTypeKey}  " +
        "   ,#{badgeKey}      " +
        "   ,NOW()            " +
        ")                    "
    )
    @Options(useGeneratedKeys=true, keyProperty="gameBadgeId", keyColumn="game_badge_id")
    public int addGameBadge(GameBadge gb);

    @Select(
        "SELECT gb.game_badge_id, gb.context_id, gb.subscriber_id, gb.association_id, gb.event_type_key, gb.created_date, " +
        "       gbr.badge_key, gbr.badge_name, gbr.badge_overlay_threshold, gbr.badge_description, gbr.badge_photo_url, gbr.badge_set_key " +
        "  FROM gameplay.game_badge AS gb           " +
        "  JOIN gameplay.game_badge_resource AS gbr " +
        "    ON gb.badge_key = gbr.badge_key        " +
        " WHERE subscriber_id = #{subscriberId}     " +
        " ORDER BY created_date DESC                "
    )
    @ResultMap(value="tv.shout.shoutcontestaward.dao.IXmlEventProcessorDaoMapper.gameBadgeMap")
    public List<GameBadge> getBadgesForSubscriber(@Param("subscriberId") int subscriberId);

    @Select(
        "SELECT gb.game_badge_id, gb.context_id, gb.subscriber_id, gb.association_id, gb.event_type_key, gb.created_date, " +
        "       gbr.badge_key, gbr.badge_name, gbr.badge_overlay_threshold, gbr.badge_description, gbr.badge_photo_url, gbr.badge_set_key " +
        "  FROM gameplay.game_badge AS gb           " +
        "  JOIN gameplay.game_badge_resource AS gbr " +
        "    ON gb.badge_key = gbr.badge_key        " +
        " WHERE game_badge_id = #{gameBadgeId}      "
    )
    @ResultMap(value="tv.shout.shoutcontestaward.dao.IXmlEventProcessorDaoMapper.gameBadgeMap")
    public GameBadge getBadgeForGameBadgeId(@Param("gameBadgeId") int gameBadgeId);
    
    @Insert(
        "INSERT INTO gameplay.game_payout (" +
        "   game_payout_id        " +
        "   ,prize_key            " +
        "   ,context_id           " +
        "   ,game_id              " +
        "   ,level_id             " +
        "   ,level_number         " +
        "   ,subscriber_id        " +
        "   ,subscriber_details   " +
        "   ,payout_key           " +
        "   ,payout_email         " +
        "   ,payout_channel       " +
        "   ,payout_description   " +
        "   ,payout_status        " +
        "   ,payout_processor_status" +        
        "   ,payout_request_amount  " +
        "   ,payout_actual_amount   " +
        "   ,payout_currency        " +
        "   ,finalized_date         " +
        "   ,updated_date           " +
        "   ,created_date           " +
        ")                          " +
        "VALUES(                  " +
        "   #{gamePayoutId}       " +
        "   ,#{prizeKey}          " +
        "   ,#{contextId}         " +
        "   ,#{gameId}            " +
        "   ,#{levelId}           " +
        "   ,#{levelNumber}       " +
        "   ,#{subscriberId}      " +
        "   ,#{subscriberDetails} " +
        "   ,#{payoutKey}         " +
        "   ,#{payoutEmail}       " +
        "   ,#{payoutChannel}     " +
        "   ,#{payoutDescription} " +
        "   ,#{payoutStatus}      " +
        "   ,#{payoutProcessorStatus}" +
        "   ,#{payoutRequestAmount}  " +
        "   ,#{payoutActualAmount}   " +
        "   ,#{payoutCurrency}       " +
        "   ,NULL                    " +
        "   ,NOW()                   " +
        "   ,#{createdDate}          " +
        ")"
    )
    @Options(useGeneratedKeys=true, keyProperty="gamePayoutId", keyColumn="game_payout_id")
    public int addGamePayout(GamePayout gp);    
        
    @Delete(
    	"DELETE FROM gameplay.game_payout" +
        " WHERE game_payout_id = #{gamePayoutId}"
    )
    public void deleteGamePayout(@Param("gamePayoutId") String gamePayoutId);
        
    @Update(
        "UPDATE gameplay.game_payout SET " +
        "    prize_key               = #{prizeKey}             " +
        "   ,context_id              = #{contextId}            " +
        "   ,game_id                 = #{gameId}               " +
        "   ,level_id                = #{levelId}              " +
        "   ,level_number            = #{levelNumber}          " +
        "   ,subscriber_id           = #{subscriberId}         " +
        "   ,subscriber_details      = #{subscriberDetails}    " +
        "   ,payout_key              = #{payoutKey}            " +
        "   ,payout_email            = #{payoutEmail}          " +
        "   ,payout_channel          = #{payoutChannel}        " +
        "   ,payout_description      = #{payoutDescription}    " +
        "   ,payout_status           = #{payoutStatus}         " +
        "   ,payout_processor_status = #{payoutProcessorStatus}" +        
        "   ,payout_request_amount   = #{payoutRequestAmount}  " +
        "   ,payout_actual_amount    = #{payoutActualAmount}   " +
        "   ,payout_currency         = #{payoutCurrency}       " +
        "   ,finalized_date          = #{finalizedDate}        " +
        "   ,updated_date            = NOW()                   " +
        " WHERE game_payout_id       = #{gamePayoutId}         "
    )
    public int updateGamePayout(GamePayout gp);
    
    @Select(
        "SELECT " +
		"       game_payout_id, prize_key, context_id, game_id, level_id, level_number, subscriber_id, subscriber_details," +
		"       payout_key, payout_email, payout_channel, payout_description, payout_status, payout_processor_status, payout_request_amount, payout_actual_amount, payout_currency," +
		"       finalized_date, updated_date, created_date" +
        "  FROM gameplay.game_payout " +
        " WHERE game_payout_id = #{gamePayoutId}"
    )
    @ResultMap(value="tv.shout.shoutcontestaward.dao.IXmlEventProcessorDaoMapper.gamePayoutMap")
    public GamePayout getGamePayoutForId(@Param("gamePayoutId") int gamePayoutId);

    @Select(
        "SELECT " +
        "       game_payout_id, prize_key, context_id, game_id, level_id, level_number, subscriber_id, subscriber_details," +
        "       payout_key, payout_email, payout_channel, payout_description, payout_status, payout_processor_status, payout_request_amount, payout_actual_amount, payout_currency," +
        "       finalized_date, updated_date, created_date" +
        "  FROM gameplay.game_payout " +
        " WHERE payout_key = #{gamePayoutKey}"
    )
    @ResultMap(value="tv.shout.shoutcontestaward.dao.IXmlEventProcessorDaoMapper.gamePayoutMap")
    public GamePayout getGamePayoutForKey(@Param("gamePayoutKey") String gamePayoutKey);
    
    @Select(
        "SELECT " +
		"       game_payout_id, prize_key, context_id, game_id, level_id, level_number, subscriber_id, subscriber_details," +
		"       payout_key, payout_email, payout_channel, payout_description, payout_status, payout_processor_status, payout_request_amount, payout_actual_amount, payout_currency," +
		"       finalized_date, updated_date, created_date" +
        "  FROM gameplay.game_payout " +
        " WHERE subscriber_id = #{subscriberId}"
    )
    @ResultMap(value="tv.shout.shoutcontestaward.dao.IXmlEventProcessorDaoMapper.gamePayoutMap")
    public List<GamePayout> getGamePayoutListForSubscriberId(@Param("subscriberId") int subscriberId);
    
    @Select(
        "SELECT " +
		"       game_payout_id, prize_key, context_id, game_id, level_id, level_number, subscriber_id, subscriber_details," +
		"       payout_key, payout_email, payout_channel, payout_description, payout_status, payout_processor_status, payout_request_amount, payout_actual_amount, payout_currency," +
		"       finalized_date, updated_date, created_date" +
        "  FROM gameplay.game_payout " +
        " WHERE payout_status NOT IN ('NEW','PAID','DENIED')"
    )
    @ResultMap(value="tv.shout.shoutcontestaward.dao.IXmlEventProcessorDaoMapper.gamePayoutMap")
    public List<GamePayout> getGamePayoutListForOpenStatus();
    
    //////////////////////////////////////////////////////////////
    //                     XP and POINTS                        //
    // TODO: COPYHACK - GamePlayService.IMetricsDaoMapper.java  //
    //////////////////////////////////////////////////////////////    
    
    @Update("UPDATE gameplay.subscriber_profile SET xp = xp + #{1}, points = points + #{2} WHERE subscriber_id = #{0}")
    public int updateSubscriberXpAndPoints(int subscriberId, int additionalXp, int additionalPoints);
    
    @Insert("INSERT INTO gameplay.subscriber_profile (subscriber_id, xp, points) values(#{0}, #{1}, #{2})")
    @Options(useGeneratedKeys=false)
    public void insertSubscriberXpAndPoints(int subscriberId, int additionalXp, int additionalPoints);
    
    @Insert(
        "INSERT INTO `gameplay`.`subscriber_xp_history` " +
        "    (`subscriber_id`, `xp`, `xp_type`, `context_id`, `create_date`) VALUES " +
        "    (#{0}, #{1}, #{2}, #{3}, NOW())"
    )
    @Options(useGeneratedKeys=false)
    public void addSubscriberXpHistory(int subscriberId, int xp, String xpType, Integer contextId);
    
    @Insert(
        "INSERT INTO gameplay.subscriber_point_history " +
        "     (subscriber_id, points, event_id, create_date) VALUES (#{0}, #{1}, #{2}, NOW())"
    )
    @Options(useGeneratedKeys=false)
    public void addSubscriberPointHistory(int subscriberId, int points, int eventId);
   
}

/*
DROP TABLE IF EXISTS gameplay.game_interaction_event;
CREATE TABLE gameplay.game_interaction_event (	  
    game_interaction_event_id INTEGER unsigned NOT NULL AUTO_INCREMENT
    ,context_id INTEGER NOT NULL
    ,event_type_key varchar(128) NOT NULL
    ,game_id varchar(64) NULL
    ,round_id varchar(64) NULL
    ,level_id varchar(64) NULL
    ,level_value varchar(64) NULL
    ,subscriber_id INTEGER NULL
    ,association_id INTEGER NULL
    ,association_description varchar(255)
    ,target_type varchar(32) NOT NULL
	,points_value INTEGER unsigned NOT NULL DEFAULT 0
    ,purchase_amount DECIMAL NULL
    ,award_amount DECIMAL NULL
    ,received_payload text NULL
    ,delivered_payload text NULL
    ,is_notification BOOLEAN NOT NULL DEFAULT 0
    ,is_persisted BOOLEAN NOT NULL DEFAULT 0
    ,is_badge BOOLEAN NOT NULL DEFAULT 0
    ,is_question_won BOOLEAN NOT NULL DEFAULT 0
    ,is_question_lost BOOLEAN NOT NULL DEFAULT 0
    ,is_round_won BOOLEAN NOT NULL DEFAULT 0
    ,is_round_lost BOOLEAN NOT NULL DEFAULT 0
    ,created_date DATETIME NOT NULL
    ,PRIMARY KEY (game_interaction_event_id)  
);

DROP TABLE IF EXISTS gameplay.game_payout;
CREATE TABLE gameplay.game_payout (	  
    game_payout_id INTEGER unsigned NOT NULL AUTO_INCREMENT
    ,prize_key varchar(64) NOT NULL DEFAULT ''
    ,context_id INTEGER NOT NULL
    ,game_id varchar(64) NULL
    ,level_id varchar(64) NULL
    ,level_number varchar(64) NULL
    ,subscriber_id INTEGER NULL
    ,subscriber_details varchar(1022) NULL ## SSN, tax IDs, routing instructions, etc.
    ,payout_key varchar(255) NOT NULL      ## unique payout key returned from provider
    ,payout_email varchar(255) NOT NULL    ## email of subscriber
    ,payout_channel varchar(255) NOT NULL  ## PayPal:email, Bank:wire
    ,payout_description varchar(255) NULL
    ,payout_status varchar(32) NOT NULL    ## INPROCESS, INREVIEW, DENIED, PAID
    ,payout_request_amount DECIMAL NULL
    ,payout_actual_amount DECIMAL NULL
    ,payout_currency char(3) NULL    
    ,finalized_date DATETIME NOT NULL        
    ,updated_date DATETIME NOT NULL    
    ,created_date DATETIME NOT NULL
    ,PRIMARY KEY (game_payout_id)  
);

DROP TABLE IF EXISTS gameplay.game_badge;
CREATE TABLE gameplay.game_badge (	    
    game_badge_id INTEGER unsigned NOT NULL AUTO_INCREMENT, 
    context_id INTEGER NOT NULL,   
    subscriber_id INTEGER NULL,    
    association_id INTEGER NULL,   
    event_type_key varchar(128) NOT NULL,    
    badge_key varchar(128) NOT NULL,    
    created_date datetime NOT NULL,
    PRIMARY KEY (game_badge_id),   
    KEY INDEX_subscriber_badge (subscriber_id, badge_key)   
);

DROP TABLE IF EXISTS gameplay.game_badge_resource;
CREATE TABLE gameplay.game_badge_resource (	 
    badge_key varchar(128) NOT NULL,    
    badge_name varchar(128) NOT NULL,   
    badge_overlay_threshold INTEGER,    
    badge_description varchar(255),
    badge_photo_url varchar(511),
    badge_set_key varchar(32),
    PRIMARY KEY (badge_key)   
);

INSERT gameplay.game_badge_resource VALUES ('badge_user_account_updated',         'Account updated, thanks', 0, '', 'http://shout.tv/img/assets/badges/trophy_badge.png', '');
INSERT gameplay.game_badge_resource VALUES ('badge_user_account_profile_picture', 'Photo updated, nice',     0, '', 'http://shout.tv/img/assets/badges/trophy_badge.png', '');
INSERT gameplay.game_badge_resource VALUES ('badge_player_played_first_round',    'Warm Up',          0, '', 'http://shout.tv/img/assets/badges/trophy_badge.png', '');
INSERT gameplay.game_badge_resource VALUES ('badge_player_won_first_round',       'First win',        0, '', 'http://shout.tv/img/assets/badges/first_win_badge.png', '');
INSERT gameplay.game_badge_resource VALUES ('badge_player_rounds_played_5',       'The Contestant',   5,   '', 'http://shout.tv/img/assets/badges/rounds_played_badge.png', 'rounds_played');
INSERT gameplay.game_badge_resource VALUES ('badge_player_rounds_played_10',      'The Novice',       10,  '', 'http://shout.tv/img/assets/badges/rounds_played_badge.png', 'rounds_played');
INSERT gameplay.game_badge_resource VALUES ('badge_player_rounds_played_15',      'The Graduate',     15,  '', 'http://shout.tv/img/assets/badges/rounds_played_badge.png', 'rounds_played');
INSERT gameplay.game_badge_resource VALUES ('badge_player_rounds_played_25',      'The Professional', 25,  '', 'http://shout.tv/img/assets/badges/rounds_played_badge.png', 'rounds_played');
INSERT gameplay.game_badge_resource VALUES ('badge_player_rounds_played_50',      'The Finisher',     50,  '', 'http://shout.tv/img/assets/badges/rounds_played_badge.png', 'rounds_played');
INSERT gameplay.game_badge_resource VALUES ('badge_player_rounds_played_100',     'The Veteran',      100, '', 'http://shout.tv/img/assets/badges/rounds_played_badge.png', 'rounds_played');
INSERT gameplay.game_badge_resource VALUES ('badge_player_rounds_played_200',     'The Marathoner',   200, '', 'http://shout.tv/img/assets/badges/rounds_played_badge.png', 'rounds_played');
INSERT gameplay.game_badge_resource VALUES ('badge_player_rounds_played_500',     'The Iron Player ', 500, '', 'http://shout.tv/img/assets/badges/rounds_played_badge.png', 'rounds_played');
INSERT gameplay.game_badge_resource VALUES ('badge_player_cash_level_attained',   'In the Money', 0,  '', 'http://shout.tv/img/assets/badges/coin_badge.png', 'money');
INSERT gameplay.game_badge_resource VALUES ('badge_player_cash_awarded',          'You won {0}',  0,  '', 'http://shout.tv/img/assets/badges/dollar_badge.png', 'award');
INSERT gameplay.game_badge_resource VALUES ('badge_correct_answer_streak_5',      'Smarty',       5,  '', 'http://shout.tv/img/assets/badges/streak_badge.png', 'streak');
INSERT gameplay.game_badge_resource VALUES ('badge_correct_answer_streak_10',     'Wizard',       10, '', 'http://shout.tv/img/assets/badges/streak_badge.png', 'streak');
INSERT gameplay.game_badge_resource VALUES ('badge_correct_answer_streak_15',     'Brainy',       15, '', 'http://shout.tv/img/assets/badges/streak_badge.png', 'streak');
INSERT gameplay.game_badge_resource VALUES ('badge_correct_answer_streak_20',     'Genius',       20, '', 'http://shout.tv/img/assets/badges/streak_badge.png', 'streak');
INSERT gameplay.game_badge_resource VALUES ('badge_correct_answer_streak_25',     'Prodigy',      25, '', 'http://shout.tv/img/assets/badges/streak_badge.png', 'streak');

 * 
 */
