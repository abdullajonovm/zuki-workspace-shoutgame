//package tv.shout.shoutcontestaward.dao;
//
//import java.util.ArrayList;
//import java.util.Comparator;
//import java.util.List;
//import java.util.stream.Collectors;
//
//import tv.shout.shoutcontestaward.domain.GameInteractionEvent;
//import tv.shout.shoutcontestaward.domain.GameInteractionEvent.GameBadge;
//import tv.shout.shoutcontestaward.domain.GameInteractionEvent.SubscriberStats;
//import tv.shout.shoutcontestaward.domain.GamePayout;
//
//
//public class InMemoryAwardsDao 
//implements IShoutContestAwardServiceDao
//{
//    private List<GameInteractionEvent> gieList;
//    private List<SubscriberStats> subList;
//    private List<GameBadge> bdgList;
//    private List<GamePayout> payList;
//    
//    public InMemoryAwardsDao()
//    {
//        gieList = new ArrayList<>();
//        subList = new ArrayList<>();
//        bdgList = new ArrayList<>();
//        payList = new ArrayList<>();
//    }
//
//    @Override
//    public int addGameInteractionEvent(GameInteractionEvent epe) {
//        gieList.add(epe);
//        return 0;
//    }
//
//    @Override
//    public List<GameInteractionEvent> getEventsByEventTypeKey(String eventTypeKey, int subscriberId) {
//        return gieList.stream()
//          .filter(x -> x.getSubscriberId() == subscriberId)
//          .filter(x -> x.getEventTypeKey().equals(eventTypeKey))
//          .sorted(Comparator.comparing( GameInteractionEvent::getCreatedDate, Comparator.reverseOrder()))
//          .collect(Collectors.toList());
//    }
//
//    @Override
//    public List<GameInteractionEvent> getEventsForSubscriber(int subscriberId) {
//        return gieList.stream()
//            .filter(x -> x.getSubscriberId() == subscriberId)
//            .sorted(Comparator.comparing( GameInteractionEvent::getCreatedDate, Comparator.reverseOrder()))
//            .collect(Collectors.toList());
//    }
//
//    @Override
//    public void deleteGameInteractionEvent(String eventId) {
//        gieList.remove(gieList.stream()
//            .filter(x -> x.getGameId() == eventId)
//            .collect(Collectors.toList()).get(0));
//    }
//
//    @Override
//    public List<SubscriberStats> getSubscriberStats(int subscriberId) {
////        "SELECT gie1.subscriber_id,                                          " +
////        "       IFNULL(sp.xp, 0) AS total_xp,                                " +
////        "       IFNULL(sp.points, 0) AS total_points,                        " +
////        "       SUM(points_value) AS total_event_points,                     " +
////        "       SUM(purchase_amount) AS total_purchased_amount,              " +
////        "       SUM(award_amount) AS total_awarded_amount,                   " +
////        "       IFNULL(gie2.total_questions_won, 0) AS total_questions_won,  " +
////        "       IFNULL(gie3.total_questions_lost, 0) AS total_questions_lost," +
////        "       IFNULL(gie4.total_rounds_won, 0) AS total_rounds_won,        " +
////        "       IFNULL(gie5.total_rounds_lost, 0) AS total_rounds_lost       " +
////        "  FROM gameplay.game_interaction_event gie1                         " +
////        "  LEFT JOIN gameplay.subscriber_profile sp                          " +
////        "    ON gie1.subscriber_id = sp.subscriber_id                        " +
////        "  LEFT JOIN                                                         " +
////        "        (SELECT subscriber_id, COUNT(1) AS total_questions_won      " +
////        "           FROM gameplay.game_interaction_event                     " +
////        "          WHERE subscriber_id = #{subscriberId}                     " +
////        "            AND is_question_won = 1) AS gie2                        " +
////        "    ON gie1.subscriber_id = gie2.subscriber_id                      " +
////        "  LEFT JOIN                                                         " +
////        "        (SELECT subscriber_id, COUNT(1) AS total_questions_lost     " +
////        "           FROM gameplay.game_interaction_event                     " +
////        "          WHERE subscriber_id = #{subscriberId}                     " +
////        "            AND is_question_lost = 1) AS gie3                       " +
////        "    ON gie1.subscriber_id = gie3.subscriber_id                      " +
////        "  LEFT JOIN                                                         " +
////        "        (SELECT subscriber_id, COUNT(1) AS total_rounds_won         " +
////        "           FROM gameplay.game_interaction_event                     " +
////        "          WHERE subscriber_id = #{subscriberId}                     " +
////        "            AND is_round_won = 1) AS gie4                           " +
////        "    ON gie1.subscriber_id = gie4.subscriber_id                      " +
////        "  LEFT JOIN                                                         " +
////        "        (SELECT subscriber_id, COUNT(1) AS total_rounds_lost        " +
////        "           FROM gameplay.game_interaction_event                     " +
////        "          WHERE subscriber_id = #{subscriberId}                     " +
////        "            AND is_round_lost = 1) AS gie5                          " +
////        "    ON gie1.subscriber_id = gie5.subscriber_id                      " +
////        " WHERE gie1.subscriber_id = #{subscriberId}                         "
//        return subList.stream()
//            .filter(x -> x.getSubscriberId() == subscriberId)
//            .collect(Collectors.toList());
//    }
//
//    @Override
//    public Integer getSubscriberRoundsPlayed(int subscriberId) {
////        "SELECT SUM(1) AS total_rounds_played           " +
////        "  FROM gameplay.game_interaction_event         " +
////        " WHERE subscriber_id = #{subscriberId}         " +
////        "   AND (is_round_won = 1 OR is_round_lost = 1);"       
//        
//        return null;
//    }
//
//    @Override
//    public ArrayList<Integer> getSubscriberQuestionStreak(int subscriberId) {
////        "SELECT                                                " +
////        "   CASE WHEN is_question_won = 1 THEN 1               " +
////        "        WHEN is_round_won = 1 THEN 1                  " +
////        "        WHEN is_question_lost = 1 THEN 0              " +
////        "        WHEN is_round_lost = 1 THEN 0                 " +
////        "        ELSE 0                                        " +
////        "    END AS streak                                     " +
////        "  FROM gameplay.game_interaction_event                " +
////        " WHERE subscriber_id = #{subscriberId}                " +
////        "   AND (is_question_won = 1 OR is_question_lost = 1 OR" +
////        "        is_round_won = 1    OR is_round_lost = 1)     " +
////        " ORDER BY created_date DESC;                          "
//        return null;
//    }
//
//    @Override
//    public int addGameBadge(GameBadge gb) {
//        this.bdgList.add(gb);
//        return 0;
//    }
//
//    @Override
//    public List<GameBadge> getBadgesForSubscriber(int subscriberId) {
////        "SELECT gb.game_badge_id, gb.context_id, gb.subscriber_id, gb.association_id, gb.event_type_key, gb.created_date, " +
////        "       gbr.badge_key, gbr.badge_name, gbr.badge_overlay_threshold, gbr.badge_description, gbr.badge_photo_url, gbr.badge_set_key " +
////        "  FROM gameplay.game_badge AS gb           " +
////        "  JOIN gameplay.game_badge_resource AS gbr " +
////        "    ON gb.badge_key = gbr.badge_key        " +
////        " WHERE subscriber_id = #{subscriberId}     " +
////        " ORDER BY created_date DESC                "
//        return null;
//    }
//
//    @Override
//    public GameBadge getBadgeForGameBadgeId(int gameBadgeId) {
////        "SELECT gb.game_badge_id, gb.context_id, gb.subscriber_id, gb.association_id, gb.event_type_key, gb.created_date, " +
////        "       gbr.badge_key, gbr.badge_name, gbr.badge_overlay_threshold, gbr.badge_description, gbr.badge_photo_url, gbr.badge_set_key " +
////        "  FROM gameplay.game_badge AS gb           " +
////        "  JOIN gameplay.game_badge_resource AS gbr " +
////        "    ON gb.badge_key = gbr.badge_key        " +
////        " WHERE game_badge_id = #{gameBadgeId}      "
//        return null;
//    }
//
//    @Override
//    public int addGamePayout(GamePayout gp) {
//        this.payList.add(gp);
//        return 0;
//    }
//
//    @Override
//    public void deleteGamePayout(String gamePayoutId) {
////        "DELETE FROM gameplay.game_payout" +
////        " WHERE game_payout_id = #{gamePayoutId}"
//        
//    }
//
//    @Override
//    public int updateGamePayout(GamePayout gp) {
////        "UPDATE gameplay.game_payout SET " +
////        "    prize_key               = #{prizeKey}             " +
////        "   ,context_id              = #{contextId}            " +
////        "   ,game_id                 = #{gameId}               " +
////        "   ,level_id                = #{levelId}              " +
////        "   ,level_number            = #{levelNumber}          " +
////        "   ,subscriber_id           = #{subscriberId}         " +
////        "   ,subscriber_details      = #{subscriberDetails}    " +
////        "   ,payout_key              = #{payoutKey}            " +
////        "   ,payout_email            = #{payoutEmail}          " +
////        "   ,payout_channel          = #{payoutChannel}        " +
////        "   ,payout_description      = #{payoutDescription}    " +
////        "   ,payout_status           = #{payoutStatus}         " +
////        "   ,payout_processor_status = #{payoutProcessorStatus}" +        
////        "   ,payout_request_amount   = #{payoutRequestAmount}  " +
////        "   ,payout_actual_amount    = #{payoutActualAmount}   " +
////        "   ,payout_currency         = #{payoutCurrency}       " +
////        "   ,finalized_date          = #{finalizedDate}        " +
////        "   ,updated_date            = NOW()                   " +
////        " WHERE game_payout_id       = #{gamePayoutId}         "
//        return 0;
//    }
//
//    @Override
//    public GamePayout getGamePayoutForId(int gamePayoutId) {
////        "SELECT " +
////        "       game_payout_id, prize_key, context_id, game_id, level_id, level_number, subscriber_id, subscriber_details," +
////        "       payout_key, payout_email, payout_channel, payout_description, payout_status, payout_processor_status, payout_request_amount, payout_actual_amount, payout_currency," +
////        "       finalized_date, updated_date, created_date" +
////        "  FROM gameplay.game_payout " +
////        " WHERE game_payout_id = #{gamePayoutId}"
//        return null;
//    }
//
//    @Override
//    public List<GamePayout> getGamePayoutListForSubscriberId(int subscriberId) {
////        "SELECT " +
////        "       game_payout_id, prize_key, context_id, game_id, level_id, level_number, subscriber_id, subscriber_details," +
////        "       payout_key, payout_email, payout_channel, payout_description, payout_status, payout_processor_status, payout_request_amount, payout_actual_amount, payout_currency," +
////        "       finalized_date, updated_date, created_date" +
////        "  FROM gameplay.game_payout " +
////        " WHERE subscriber_id = #{subscriberId}"
//        return null;
//    }
//
//    @Override
//    public List<GamePayout> getGamePayoutListForOpenStatus() {
////        "SELECT " +
////        "       game_payout_id, prize_key, context_id, game_id, level_id, level_number, subscriber_id, subscriber_details," +
////        "       payout_key, payout_email, payout_channel, payout_description, payout_status, payout_processor_status, payout_request_amount, payout_actual_amount, payout_currency," +
////        "       finalized_date, updated_date, created_date" +
////        "  FROM gameplay.game_payout " +
////        " WHERE payout_status NOT IN ('NEW','PAID','DENIED')"
//        return null;
//    }
//
//    @Override
//    public int updateSubscriberXpAndPoints(int subscriberId, int additionalXp, int additionalPoints) {
////        "UPDATE gameplay.subscriber_profile SET xp = xp + #{1}, points = points + #{2} WHERE subscriber_id = #{0}"
//        return 0;
//    }
//
//    @Override
//    public void insertSubscriberXpAndPoints(int subscriberId, int additionalXp, int additionalPoints) {
////        "INSERT INTO gameplay.subscriber_profile (subscriber_id, xp, points) values(#{0}, #{1}, #{2})"
//        
//    }
//
//    @Override
//    public void addSubscriberXpHistory(int subscriberId, int xp, String xpType, Integer contextId) {
////        "INSERT INTO `gameplay`.`subscriber_xp_history` " +
////        "    (`subscriber_id`, `xp`, `xp_type`, `context_id`, `create_date`) VALUES " +
////        "    (#{0}, #{1}, #{2}, #{3}, NOW())"
//        
//    }
//    @Override
//    public void addSubscriberPointHistory(int subscriberId, int points, int eventId) {
////        "INSERT INTO gameplay.subscriber_point_history " +
////        "     (subscriber_id, points, event_id, create_date) VALUES (#{0}, #{1}, #{2}, NOW())"
//        
//    }
//
//}
