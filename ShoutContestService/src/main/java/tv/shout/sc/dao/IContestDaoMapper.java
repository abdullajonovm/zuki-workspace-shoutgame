package tv.shout.sc.dao;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.meinc.gameplay.domain.App;
import com.meinc.gameplay.domain.Language;
import com.meinc.gameplay.domain.Tuple;

import tv.shout.sc.domain.BankAccount;
import tv.shout.sc.domain.CashPoolTransaction2;
import tv.shout.sc.domain.Country;
import tv.shout.sc.domain.CouponBatch;
import tv.shout.sc.domain.CouponCode;
import tv.shout.sc.domain.Game;
import tv.shout.sc.domain.GamePlayer;
import tv.shout.sc.domain.ManualRedeemRequest;
import tv.shout.sc.domain.Match;
import tv.shout.sc.domain.MatchPlayer;
import tv.shout.sc.domain.MatchQueue;
import tv.shout.sc.domain.Round;
import tv.shout.sc.domain.RoundPlayer;

public interface IContestDaoMapper
{
    //
    // multi-localization
    //

    @Insert(
        "INSERT IGNORE INTO contest.multi_localization (" +
        "   `uuid`, `type`, language_code, `value`) " +
        "VALUES (#{0}, #{1}, #{2}, #{3}) "
    )
    void insertOrReplaceMultiLocalizationValue(String uuid, String type, String languageCode, String value);

    @Select("SELECT language_code, `value` FROM contest.multi_localization WHERE `uuid` = #{0} AND `type` = #{1}")
    @Results({
        @Result(property="key", column="language_code"),
        @Result(property="val", column="value")
    })
    List<Tuple<String>> getMultiLocalizationValues(String uuid, String type);

    @Delete("DELETE FROM contest.multi_localization WHERE `uuid` = #{0} AND `type` = #{1}")
    void removeMutliLocalizationValues(String uuid, String type);

    //
    // app
    //

    @Select("select * from gameplay.app")
    @Results({
        @Result(property="appId", column="app_id"),
        @Result(property="appName", column="app_name"),
        @Result(property="firewalled", column="firewalled"),
        @Result(property="method", column="endpoint_method"),
        @Result(property="protocol", column="endpoint_protocol"),
        @Result(property="endpoint", column="endpoint_url"),
        @Result(property="port", column="endpoint_port"),
        @Result(property="clientKey", column="client_key"),
        @Result(property="iOSBundleId", column="iOS_bundle_id"),
        @Result(property="androidBundleId", column="android_bundle_id"),
        @Result(property="windowsBundleId", column="windows_bundle_id"),
        @Result(property="vipBoxPushType", column="vipbox_push_type")
    })
    List<App> getApps();

    @Select("select * from gameplay.app_language where app_id = #{0}")
    @Results({
        @Result(property="languageCode", column="language_code"),
        @Result(property="default", column="default_flag")
    })
    List<Language> getLanguageForApp(int appId);

    //
    // countries
    //

    @Select(
        "select country_code, dial_code, sort_order " +
        "  from contest.countries " +
        " order by sort_order"
    )
    List<Country> getCountries();

    //
    // acra
    //

    @Insert(
        "INSERT IGNORE INTO contest.acra_report (" +
        "   report_id, context_id, subscriber_id, user_email, " +
        "   app_version_code, app_version_name, app_package_name, " +
        "   phone_model, phone_brand, phone_product, phone_display, " +
        "   phone_initial_config, phone_crash_config, android_version, android_build, " +
        "   total_mem_size, available_mem_size, stacktrace, logcat, " +
        "   app_start_date, crash_date" +
        ") VALUES (" +
        "   #{REPORT_ID}, #{contextId}, #{subscriberId}, #{USER_EMAIL}, " +
        "   #{APP_VERSION_CODE}, #{APP_VERSION_NAME}, #{PACKAGE_NAME}, " +
        "   #{PHONE_MODEL}, #{BRAND}, #{PRODUCT}, #{DISPLAY}, " +
        "   #{INITIAL_CONFIGURATION}, #{CRASH_CONFIGURATION}, #{ANDROID_VERSION}, #{BUILD}, " +
        "   #{TOTAL_MEM_SIZE}, #{AVAILABLE_MEM_SIZE}, #{STACK_TRACE}, #{LOGCAT}, " +
        "   #{userAppStartDate}, #{userCrashDate}" +
        ")"
    )
    void insertAcraReport(Map<String, Object> data);

    //
    // cash pool + helpers
    //

    @Select(
        "select * from contest.`cash_pool_transaction2` where subscriber_id = #{0} order by transaction_date desc limit 1"
    )
    CashPoolTransaction2 getMostRecentCashPoolTransactionForSubscriber(long subscriberId);

    @Insert(
        "INSERT INTO contest.cash_pool_transaction2 (" +
        "   subscriber_id, amount, `type`, `description`, " +
        "   current_pool_amount, current_bonus_amount, used_pool_amount, used_bonus_amount, " +
        "   receipt_id, context_uuid, transaction_date) " +
        " VALUES (" +
        "   #{subscriberId}, #{amount}, #{type}, #{description}, " +
        "   #{currentPoolAmount}, #{currentBonusAmount}, #{usedPoolAmount}, #{usedBonusAmount}, " +
        "   #{receiptId}, #{contextUuid}, #{transactionDate}" +
        ")"
    )
    void addCashPoolTransaction(CashPoolTransaction2 cpt);

    @Select(
        "select * " +
        "  from contest.cash_pool_transaction2 " +
        " where subscriber_id = #{0} AND FIND_IN_SET (`type`, #{1}) <> 0 " +
        " order by transaction_date"
    )
    List<CashPoolTransaction2> getCashPoolTransactionsForSubscriberForTypes(long subscriberId, String transactionTypeAsCommaDelimitedList);

    @Insert("INSERT INTO contest.manual_redeem_request (subscriber_id, amount, request_date) VALUES (#{0}, #{1}, NOW())")
    void addManualRedeemRequest(long subscriberId, float amount);

    @Select("SELECT SUM(amount) FROM contest.manual_redeem_request WHERE subscriber_id = #{0} AND fulfilled_date IS NULL AND cancelled_date IS NULL")
    Float getOutstandingManualRedeemRequestAmounts(long subscriberId);

    @Select("SELECT * FROM contest.manual_redeem_request WHERE manual_redeem_request_id = #{0}")
    ManualRedeemRequest getManualRedeemRequest(int manualRedeemRequestId);

    @Update("UPDATE contest.manual_redeem_request SET fulfilled_date = NOW() WHERE manual_redeem_request_id = #{0}")
    void markManualRedeemRequestFulfilled(int manualRedeemRequestId);

    @Select("SELECT * FROM contest.manual_redeem_request WHERE fulfilled_date IS NULL AND cancelled_date IS NULL")
    List<ManualRedeemRequest> getOutstandingManualRedeemRequests();

    @Select(
        "select cpt21.* " +
        "  from contest.cash_pool_transaction2 cpt21 " +
        " where cpt21.context_uuid = #{0} " +
        "   and cpt21.`type` in ('JOINED_ROUND','ABANDONED_ROUND')"
    )
    List<CashPoolTransaction2> getJoinedAndAbandonedForContext(String contextId);

    //
    // CouponCode / CouponBatch
    //

    @Select("SELECT * FROM contest.coupon_code WHERE coupon_code = #{0}")
    CouponCode getCouponCode(String couponCode);

//    @Select("SELECT * FROM contest.coupon_batch WHERE batch_id = #{0}")
//    CouponBatch getCouponBatch(int batchId);

    @Insert("INSERT INTO contest.coupon_batch (batch_name, amount, expire_date, create_date) VALUES (#{batchName}, #{amount}, #{expireDate}, NOW())")
    @Options(useGeneratedKeys=true, keyProperty="batchId", keyColumn="batch_id")
    void createCouponBatch(CouponBatch couponBatch);

    @Insert("INSERT INTO contest.coupon_code (coupon_code, batch_id, amount, create_date, expire_date) VALUES (#{couponCode}, #{batchId}, #{amount}, NOW(), #{expireDate})")
    void createCouponCode(CouponCode couponCode);

    @Update(
        "UPDATE contest.coupon_code " +
        "   SET cancelled = 1, cancelled_date = NOW() " +
        " WHERE batch_id = #{0} " +
        "   AND redeemed_date IS NULL"
    )
    void cancelCouponCodesInBatch(int batchId);

    @Update(
        "UPDATE contest.coupon_code " +
        "   SET cancelled = 1, cancelled_date = NOW() " +
        " WHERE coupon_code = #{0} " +
        "   AND redeemed_date IS NULL"
    )
    void cancelCouponCode(String couponCode);

    @Select("SELECT * FROM contest.coupon_batch ORDER BY create_date ASC")
    List<CouponBatch> getCouponBatches();

    @Select("SELECT * FROM contest.coupon_code WHERE batch_id = #{0}")
    List<CouponCode> getCouponsForBatch(int batchId);

    @Select("SELECT * FROM contest.coupon_code WHERE (expire_date IS NULL OR expire_date > NOW()) AND cancelled = 0 AND redeemed_date IS NULL ORDER BY create_date DESC")
    List<CouponCode> getUnusedCoupons();

    @Select("SELECT * FROM contest.coupon_code WHERE redeemed_date >= #{0}")
    List<CouponCode> getCouponsRedeemedSince(Date since);

    @Select("SELECT * FROM contest.coupon_batch WHERE batch_id = #{0}")
    CouponBatch getCouponBatch(int batchId);

    @Update("UPDATE contest.coupon_code SET redeemed_by_subscriber_id = #{1}, redeemed_date = NOW() WHERE coupon_code = #{0}")
    void  markCouponAsRedeemed(String couponCode, long subscriberId);

    //
    // BankAccount
    //

    @Insert(
        "INSERT INTO contest.bank_account (" +
        "   id, subscriber_id, bank_name, checking_account_name, routing_number, account_number, create_date, update_date " +
        ") VALUES (" +
        "   #{id}, #{subscriberId}, " +
        "   #{bankName}, #{checkingAccountName}, " +
        "   #{routingNumber}, #{accountNumber}, " +
        "   NOW(), NOW() " +
        ")"
    )
    void createBankAccount(BankAccount account);

    @Select("SELECT * FROM contest.bank_account WHERE subscriber_id = #{0}")
    List<BankAccount> retrieveBankAccounts(long subscriberId);

    @Update(
        "UPDATE contest.bank_account SET " +
        "   bank_name = #{bankName}, checking_account_name = #{checkingAccountName}, " +
        "   routing_number = #{routingNumber}, account_number = #{accountNumber}, " +
        "   update_date = NOW() " +
        " WHERE id = #{id}"
    )
    void updateBankAccount(BankAccount account);

    @Delete("DELETE FROM contest.bank_account WHERE id = #{0}")
    void deleteBankAccount(String id);

    //
    // sms verification code
    //

    @Delete("DELETE FROM contest.phone_verification_code WHERE subscriber_id = #{0}")
    void clearPhoneVerificationCodeForSubscriber(long subscriberId);

    @Insert("INSERT INTO contest.phone_verification_code (subscriber_id, phone, code, create_date) VALUES (#{0}, #{1}, #{2}, NOW())")
    void addPhoneVerificationCodeForSubscriber(long subscriberId, String phone, String code);

    @Select("SELECT count(*) FROM contest.phone_verification_code WHERE subscriber_id = #{0} AND phone = #{1} AND code = #{2} AND create_date >= #{3}")
    boolean isPhoneVerificationCodeValidForSubscriber(long subscriberId, String phone, String code, Date cutoffDate);

    //
    // Game / GamePlayer
    //

    @Insert(
        "INSERT INTO contest.game ( " +
        "   id, game_type, game_engine, engine_type, producer, game_photo_url, game_status, bracket_elimination_count, " +
        "   allow_bots, use_doctored_time_for_bots, fill_with_bots, max_bot_fill_count, pair_immediately, can_appear_in_mobile, production_game, " +
        "   private_game, invite_code, " +
        "   include_activity_answers_before_scoring, guide_url, pending_date, starting_lives_count, additional_life_cost, max_lives_count, " +
        "   auto_start_pool_play, auto_start_bracket_play, auto_bracket_play_pre_start_notification_time_ms ) " +
        "VALUES (" +
        "   #{id}, #{gameType}, #{gameEngine}, #{engineType}, #{producer}, #{gamePhotoUrl}, #{gameStatus}, #{bracketEliminationCount}, " +
        "   #{allowBots}, #{useDoctoredTimeForBots}, #{fillWithBots}, #{maxBotFillCount}, #{pairImmediately}, #{canAppearInMobile}, #{productionGame}, " +
        "   #{privateGame}, #{inviteCode}, " +
        "   #{includeActivityAnswersBeforeScoring}, #{guideUrl}, NOW(), #{startingLivesCount}, #{additionalLifeCost}, #{maxLivesCount}, " +
        "   #{autoStartPoolPlay}, #{autoStartBracketPlay}, #{autoBracketPlayPreStartNotificationTimeMs} ) " +
        "ON DUPLICATE KEY UPDATE " +
        "   game_type = #{gameType}, game_engine = #{gameEngine}, engine_type = #{engineType}, producer = #{producer}, game_photo_url = #{gamePhotoUrl}, " +
        "   game_status = #{gameStatus}, bracket_elimination_count = #{bracketEliminationCount}, " +
        "   allow_bots = #{allowBots}, use_doctored_time_for_bots = #{useDoctoredTimeForBots}, fill_with_bots = #{fillWithBots}, max_bot_fill_count = #{maxBotFillCount}, " +
        "   pair_immediately = #{pairImmediately}, can_appear_in_mobile = #{canAppearInMobile}, production_game = #{productionGame}, " +
        "   private_game = #{privateGame}, invite_code = #{inviteCode}, " +
        "   include_activity_answers_before_scoring = #{includeActivityAnswersBeforeScoring}, guide_url = #{guideUrl}, " +
        "   starting_lives_count = #{startingLivesCount}, additional_life_cost = #{additionalLifeCost}, max_lives_count = #{maxLivesCount}, " +
        "   auto_start_pool_play = #{autoStartPoolPlay}, auto_start_bracket_play = #{autoStartBracketPlay}, auto_bracket_play_pre_start_notification_time_ms = #{autoBracketPlayPreStartNotificationTimeMs} "
    )
    void insertOrReplaceGame(Game game);

    @Insert("INSERT IGNORE INTO contest.game_app_ids (game_id, app_id) VALUES (#{0}, #{1})")
    void insertOrReplaceGameAppId(String gameId, int appId);

    @Insert("INSERT IGNORE INTO contest.game_language_codes (game_id, language_code) VALUES (#{0}, #{1})")
    void insertOrReplaceGameLanguageCodes(String gameId, String languageCode);

    @Insert("INSERT IGNORE INTO contest.game_forbidden_country_codes (game_id, country_code) VALUES (#{0}, #{1})")
    void insertOrReplaceGameForbiddenCountryCodes(String gameId, String countryCode);

//    void deleteGame(String gameId);

    @Select("SELECT * FROM contest.game WHERE id = #{0}")
    Game getGame(String gameId);

    @Select("SELECT id FROM contest.game WHERE invite_code = #{0}")
    String getGameIdViaInviteCode(String inviteCode);

    @Select("SELECT app_id FROM contest.game_app_ids WHERE game_id = #{0}")
    Set<Integer> getGameAllowableAppIds(String gameId);

    @Select("SELECT language_code FROM contest.game_language_codes WHERE game_id = #{0}")
    Set<String> getGameAllowableLanguageCodes(String gameId);

    @Select("SELECT country_code FROM contest.game_forbidden_country_codes WHERE game_id = #{0}")
    Set<String> getGameForbiddenCountryCodes(String gameId);

    @Select(
        "SELECT g.id " +
        "  FROM contest.game g, contest.game_app_ids a " +
        " WHERE a.app_id = #{1} AND a.game_id = g.id AND g.game_status = #{0}")
    List<String> getGameIdsByStatusAndAllowableAppId(Game.GAME_STATUS status, int appId);

    @Select(
        "SELECT g.id " +
        "  FROM contest.game g " +
        " WHERE g.game_status = #{0}")
    List<String> getGameIdsByStatus(Game.GAME_STATUS status);

    //this had to be modified slightly for MySql, and will likely have to change for a non-MySql solution
    //"FIND_IN_SET(column, list) <> 0" is equivalent to "column IN (list)"
    //it's a LOT slower, but MyBatis annotations can't handle an IN clause
    @Select("SELECT id FROM contest.game WHERE game_engine = #{0} AND FIND_IN_SET (game_status, #{1}) <> 0")
    List<String> getGameIdsByEngineAndStatus(String gameEngine, String statusesAsCommaDelimitedString);

    @Select(
        "SELECT g.id " +
        "  FROM contest.game g, contest.game_player gp, contest.game_app_ids a " +
        " WHERE g.game_status = #{0} " +
        "   AND g.id = gp.game_id AND gp.subscriber_id = #{2} " +
        "   AND g.id = a.game_id AND a.app_id = #{1}"
    )
    List<String> getSubscriberGameIdsByStatus(Game.GAME_STATUS status, int appId, long subscriberId);

    @Select(
        "SELECT g.id " +
        "  FROM contest.game g, contest.game_player gp, contest.game_app_ids a " +
        " WHERE FIND_IN_SET (g.game_status, #{2}) <> 0 " +
        "   AND g.id = gp.game_id AND gp.subscriber_id = #{1} " +
        "   AND g.id = a.game_id AND a.app_id = #{0}"
    )
    List<String> getSubscriberGameIdsByStatuses(int appId, long subscriberId, String statusesAsCommaDelimitedString);

    @Select(
        "SELECT g.id " +
        "  FROM contest.game g " +
        " WHERE g.game_engine = #{1} AND FIND_IN_SET (game_status, #{2}) <> 0 AND g.private_game = 1 AND g.id in (" +
        "   select game_id from contest.game_player where subscriber_id = #{0}" +
        " )"
    )
    List<String> getPrivateGameIdsForSubscriberByStatusAndEngine(long subscriberId, String gameEngine, String statusesAsCommaDelimitedString);

    @Delete("DELETE FROM contest.game_player WHERE game_id = #{1} AND subscriber_id = #{0}")
    void removeGamePlayer(long subscriberId, String gameId);

//    List<Game> getGamesByStatusByGameIds(Game.GAME_STATUS status, List<String> gameIds);

    @Insert(
        "INSERT INTO contest.game_player (" +
        "   id, game_id, subscriber_id, freeplay, rank, " +
        "   payout_payment_id, payout_awarded_amount, payout_venue, payout_completed, " +
        "   determination, countdown_to_elimination, total_lives, next_round_id, last_round_id, create_date) " +
        "VALUES ( " +
        "   #{id}, #{gameId}, #{subscriberId}, #{freeplay}, #{rank}, " +
        "   #{payoutPaymentId}, #{payoutAwardedAmount}, #{payoutVenue}, #{payoutCompleted}, " +
        "   #{determination}, #{countdownToElimination}, #{totalLives}, #{nextRoundId}, #{lastRoundId}, NOW() " +
        ") ON DUPLICATE KEY UPDATE " +
        "   freeplay = #{freeplay}, rank = #{rank}, payout_payment_id = #{payoutPaymentId}, " +
        "   payout_awarded_amount = #{payoutAwardedAmount}, payout_venue = #{payoutVenue}, " +
        "   payout_completed = #{payoutCompleted}, determination = #{determination}, countdown_to_elimination =  #{countdownToElimination}, " +
        "   total_lives = #{totalLives}, " +
        "   next_round_id = #{nextRoundId}, last_round_id = #{lastRoundId}"
    )
    void insertOrReplaceGamePlayer(GamePlayer gamePlayer);

    @Select("SELECT * FROM contest.game_player WHERE game_id = #{0} AND subscriber_id = #{1} AND determination != 'REMOVED'")
    GamePlayer getGamePlayer(String gameId, long subscriberId);

    @Select("SELECT * FROM contest.game_player WHERE subscriber_id = #{0} AND determination NOT IN('REMOVED','CANCELLED')")
    List<GamePlayer> getGamePlayers(long subscriberId);

    @Select("SELECT * FROM contest.game_player WHERE game_id = #{0} AND determination NOT IN('REMOVED','CANCELLED')")
    List<GamePlayer> getGamePlayersForGame(String gameId);

//    void deleteGamePlayer(GamePlayer gamePlayer);

    @Update(
        "UPDATE contest.game_player SET " +
        "   freeplay = #{freeplay}, rank = #{rank}, payout_payment_id = #{payoutPaymentId}, " +
        "   payout_awarded_amount = #{payoutAwardedAmount}, payout_venue = #{payoutVenue}, " +
        "   payout_completed = #{payoutCompleted}, determination = #{determination}, countdown_to_elimination =  #{countdownToElimination}, " +
        "   total_lives = #{totalLives}, next_round_id = #{nextRoundId}, last_round_id = #{lastRoundId} " +
        "WHERE id = #{id}"
    )
    void updateGamePlayer(GamePlayer gamePlayer);

    @Update(
        "UPDATE contest.game SET " +
        "   game_type = #{gameType}, game_engine = #{gameEngine}, engine_type = #{engine_type}, producer = #{producer}, game_photo_url = #{gamePhotoUrl}, " +
        "   allow_bots = #{allowBots}, use_doctored_time_for_bots = #{useDoctoredTimeForBots}, fill_with_bots = #{fillWithBots}, max_bot_fill_count = #{maxBotFillCount}, " +
        "   pair_immediately = #{pairImmediately}, can_appear_in_mobile = #{canAppearInMobile}, production_game = #{productionGame}, " +
        "   bracket_elimination_count = #{bracketEliminationCount}, " +
        "   private_game = #{privateGame}, invite_code = #{inviteCode}, " +
        "   include_activity_answers_before_scoring = #{includeActivityAnswersBeforeScoring}, guide_url = #{guideUrl}, " +
        "   starting_lives_count = #{startingLivesCount}, additional_life_cost = #{additionalLifeCost}, max_lives_count = #{maxLivesCount}, " +
        "   auto_start_pool_play = #{autoStartPoolPlay}, auto_start_bracket_play = #{autoStartBracketPlay}, auto_bracket_play_pre_start_notification_time_ms = {autoBracketPlayPreStartNotificationTimeMs} " +
        "WHERE id = #{id}"
    )
    void updateGame(Game game);

//    @Update("UPDATE contest.game SET game_status = #{1} WHERE id = #{0}")
//    void updateGameStatus(String gameId, Game.GAME_STATUS newStatus);

    @Update("UPDATE contest.game SET game_status = 'OPEN', open_date = NOW() WHERE id = #{0}")
    void setGameStatusOpen(String gameId);

    @Update("UPDATE contest.game SET game_status = 'INPLAY', inplay_date = NOW() WHERE id = #{0}")
    void setGameStatusInplay(String gameId);

    @Update("UPDATE contest.game SET game_status = 'CLOSED', closed_date = NOW() WHERE id = #{0}")
    void setGameStatusClosed(String gameId);

    @Update("UPDATE contest.game SET starting_sms_sent_date = NOW() WHERE id = #{0}")
    void setGameSmsSentDate(String gameId);

    @Delete("DELETE FROM contest.game_app_ids WHERE game_id = #{0}")
    void deleteGameAppId(String gameId);

    @Delete("DELETE FROM contest.game_language_codes WHERE game_id = #{0}")
    void deleteGameLanguageCodes(String gameId);

    @Delete("DELETE FROM contest.game_forbidden_country_codes WHERE game_id = #{0}")
    void deleteGameForbiddenCountryCodes(String gameId);

    @Insert(
        "INSERT INTO contest.game (id, " +
        "   game_type, game_engine, engine_type, producer, game_photo_url, game_status, bracket_elimination_count, " +
        "   allow_bots, use_doctored_time_for_bots, fill_with_bots, max_bot_fill_count, pair_immediately, can_appear_in_mobile, production_game, " +
        "   include_activity_answers_before_scoring, guide_url, pending_date, private_game, invite_code, starting_lives_count, additional_life_cost, max_lives_count, " +
        "   auto_start_pool_play, auto_start_bracket_play, auto_bracket_play_pre_start_notification_time_ms) " +
        "   SELECT #{1}, game_type, game_engine, engine_type, producer, game_photo_url, 'PENDING', bracket_elimination_count, " +
        "   allow_bots, use_doctored_time_for_bots, fill_with_bots, max_bot_fill_count, pair_immediately, can_appear_in_mobile, production_game, " +
        "   include_activity_answers_before_scoring, guide_url, NOW(), private_game, #{2}, starting_lives_count, additional_life_cost, max_lives_count, " +
        "   auto_start_pool_play, auto_start_bracket_play, auto_bracket_play_pre_start_notification_time_ms " +
        "     FROM contest.game " +
        "    WHERE id = #{0}"
    )
    void cloneGame(String gameId, String newGameId, String newInviteCode);

    @Insert(
        "INSERT INTO contest.multi_localization (uuid, `type`, language_code, `value`) " +
        "   SELECT #{1}, `type`, language_code, `value` " +
        "     FROM contest.multi_localization " +
        "    WHERE uuid = #{0}"
    )
    void cloneGameMultilocalizationValues(String gameId, String newGameId);

    @Insert(
        "INSERT INTO contest.game_app_ids (game_id, app_id) " +
        "   SELECT #{1}, app_id " +
        "     FROM contest.game_app_ids " +
        "    WHERE game_id = #{0}"
    )
    void cloneGameAllowableAppIds(String gameId, String newGameId);

    @Insert(
        "INSERT INTO contest.game_language_codes (game_id, language_code) " +
        "   SELECT #{1}, language_code " +
        "     FROM contest.game_language_codes " +
        "    WHERE game_id = #{0}"
    )
    void cloneGameAllowableLanguageCodes(String gameId, String newGameId);

    @Insert(
        "INSERT INTO contest.game_forbidden_country_codes (game_id, country_code) " +
        "   SELECT #{1}, country_code " +
        "     FROM contest.game_forbidden_country_codes " +
        "    WHERE game_id = #{0}"
    )
    void cloneGameForbiddenCountryCodes(String gameId, String newGameId);

    @Update(
        "UPDATE contest.multi_localization " +
        "   SET `value` = #{2} " +
        " WHERE uuid = #{0} AND `type` = 'gameName' AND language_code = #{1}"
    )
    void cloneUpdateGameName(String newGameId, String languageCode, String newName);

//    @Select("SELECT COUNT(*) FROM contest.game_player WHERE game_id = #{0} AND FIND_IN_SET (`subscriber_id`, #{1}) <> 1 AND determination NOT IN ('CANCELLED','REMOVED') AND freeplay = 0")
//    int getCurrentGamePlayerCount(String gameId, String botIdsAsCommaDelimitedString);

//    @Select(
//        "SELECT COUNT(*) as `count`, freeplay " +
//        "  FROM contest.game_player " +
//        " WHERE game_id = #{0} " +
//        "   AND FIND_IN_SET (`subscriber_id`, #{1}) <> 1 " +
//        "   AND determination NOT IN ('CANCELLED','REMOVED') " +
//        " GROUP BY freeplay"
//    )
//    @Results({
//        @Result(property="key", column="freeplay"),
//        @Result(property="val", column="count")
//    })
//    List<Tuple<Long>> getCurrentGamePlayerCount(String gameId, String botIdsAsCommaDelimitedString);

    @Select("SELECT * FROM contest.game_player WHERE game_id = #{0} AND FIND_IN_SET (`subscriber_id`, #{1}) = 0 AND determination NOT IN ('CANCELLED','REMOVED')")
    List<GamePlayer> getCurrentGamePlayerCount(String gameId, String botIdsAsCommaDelimitedString);

    //
    // Round / RoundPlayer
    //

    //see FIND_IN_SET comment for getGameIdsByEngineAndStatus
    @Select("SELECT * FROM contest.round WHERE game_id = #{0} AND FIND_IN_SET(round_status, #{1}) <> 0 ORDER BY round_sequence")
    List<Round> getRoundsForGameForStatus(String gameId, String statusesAsCommaDelimitedString);

    @Insert(
        "INSERT INTO contest.round (" +
        "   id, game_id, " +
        "   round_type, round_status, round_sequence, final_round, round_purse, current_player_count, " +
        "   maximum_player_count, minimum_match_count, maximum_match_count, cost_per_player, round_activity_type, " +
        "   round_activity_value, minimum_activity_to_win_count, maximum_activity_count, activity_minimum_difficulty, " +
        "   activity_maximum_difficulty, activity_maximum_duration_seconds, player_maximum_duration_seconds, " +
        "   duration_between_activities_seconds, match_global, maximum_duration_minutes, match_player_count, pending_date, expected_open_date) " +
        "VALUES ( " +
        "   #{id}, #{gameId}, " +
        "   #{roundType}, #{roundStatus}, #{roundSequence}, #{finalRound}, #{roundPurse}, #{currentPlayerCount}, " +
        "   #{maximumPlayerCount}, #{minimumMatchCount}, #{maximumMatchCount}, #{costPerPlayer}, #{roundActivityType}, " +
        "   #{roundActivityValue}, #{minimumActivityToWinCount}, #{maximumActivityCount}, #{activityMinimumDifficulty}, " +
        "   #{activityMaximumDifficulty}, #{activityMaximumDurationSeconds}, #{playerMaximumDurationSeconds}, " +
        "   #{durationBetweenActivitiesSeconds}, #{matchGlobal}, #{maximumDurationMinutes}, #{matchPlayerCount}, NOW(), #{expectedOpenDate} " +
        ") ON DUPLICATE KEY UPDATE " +
        "   round_type = #{roundType}, round_status = #{roundStatus}, round_sequence = #{roundSequence}, " +
        "   final_round = #{finalRound}, round_purse = #{roundPurse}, current_player_count = #{currentPlayerCount}, "  +
        "   maximum_player_count = #{maximumPlayerCount}, minimum_match_count = #{minimumMatchCount}, " +
        "   maximum_match_count = #{maximumMatchCount}, cost_per_player = #{costPerPlayer}, " +
        "   round_activity_type = #{roundActivityType}, round_activity_value = #{roundActivityValue}, " +
        "   minimum_activity_to_win_count = #{minimumActivityToWinCount}, maximum_activity_count = #{maximumActivityCount}, " +
        "   activity_minimum_difficulty = #{activityMinimumDifficulty}, activity_maximum_difficulty = #{activityMaximumDifficulty}, " +
        "   activity_maximum_duration_seconds = #{activityMaximumDurationSeconds}, " +
        "   player_maximum_duration_seconds = #{playerMaximumDurationSeconds}, match_global = #{matchGlobal}, " +
        "   maximum_duration_minutes = #{maximumDurationMinutes}, match_player_count = #{matchPlayerCount}, " +
        "   pending_date = #{pendingDate}, duration_between_activities_seconds = #{durationBetweenActivitiesSeconds}, " +
        "   expected_open_date = #{expectedOpenDate}"
    )
    void insertOrReplaceRound(Round round);

    @Insert("INSERT IGNORE INTO contest.round_categories (round_id, category) VALUES (#{0}, #{1})")
    void insertOrReplaceRoundCategory(String roundId, String category);

    @Update(
        "UPDATE contest.round SET " +
        "   round_type = #{roundType}, round_sequence = #{roundSequence}, " +
        "   final_round = #{finalRound}, round_purse = #{roundPurse}, current_player_count = #{currentPlayerCount}, "  +
        "   maximum_player_count = #{maximumPlayerCount}, minimum_match_count = #{minimumMatchCount}, " +
        "   maximum_match_count = #{maximumMatchCount}, cost_per_player = #{costPerPlayer}, " +
        "   round_activity_type = #{roundActivityType}, round_activity_value = #{roundActivityValue}, " +
        "   minimum_activity_to_win_count = #{minimumActivityToWinCount}, maximum_activity_count = #{maximumActivityCount}, " +
        "   activity_minimum_difficulty = #{activityMinimumDifficulty}, activity_maximum_difficulty = #{activityMaximumDifficulty}, " +
        "   activity_maximum_duration_seconds = #{activityMaximumDurationSeconds}, " +
        "   player_maximum_duration_seconds = #{playerMaximumDurationSeconds}, match_global = #{matchGlobal}, " +
        "   maximum_duration_minutes = #{maximumDurationMinutes}, match_player_count = #{matchPlayerCount}, " +
        "   expected_open_date = #{expectedOpenDate}, duration_between_activities_seconds = #{durationBetweenActivitiesSeconds} " +
        "WHERE id = #{id}"
    )
    void updateRound(Round round);

//    @Update("UPDATE contest.round SET round_status = #{2}, final_round = #{1} WHERE id = #{0}")
//    void updateRoundStatus(String roundId, boolean finalRound, Round.ROUND_STATUS newStatus);

    @Update("UPDATE contest.round SET round_status = 'VISIBLE', final_round = #{1}, visible_date = NOW() WHERE id = #{0}")
    void setRoundStatusVisible(String roundId, boolean finalRound);

    @Update("UPDATE contest.round SET round_status = 'OPEN', final_round = #{1}, open_date = NOW() WHERE id = #{0}")
    void setRoundStatusOpen(String roundId, boolean finalRound);

    @Update("UPDATE contest.round SET round_status = 'FULL', final_round = #{1} WHERE id = #{0}")
    void setRoundStatusFull(String roundId, boolean finalRound);

    @Update("UPDATE contest.round SET round_status = 'INPLAY', final_round = #{1}, inplay_date = NOW() WHERE id = #{0}")
    void setRoundStatusInplay(String roundId, boolean finalRound);

    @Update("UPDATE contest.round SET round_status = 'CLOSED', final_round = #{1}, closed_date = NOW() WHERE id = #{0}")
    void setRoundStatusClosed(String roundId, boolean finalRound);

    @Update("UPDATE contest.round SET round_status = #{1}, current_player_count = #{2} WHERE id = #{0}")
    void updateRoundStatusAndPlayerCount(String roundId, Round.ROUND_STATUS newStatus, int newPlayerCount);

    @Delete("DELETE FROM contest.round_categories WHERE round_id = #{0} and category = #{1}")
    void removeRoundCategory(String roundId, String category);

    @Select("SELECT * FROM contest.round WHERE id = #{0}")
    Round getRound(String roundId);

    @Select("SELECT category FROM contest.round_categories WHERE round_id = #{0}")
    Set<String> getRoundCategories(String roundId);

    @Select("SELECT id FROM contest.round WHERE game_id = #{0} AND round_sequence = #{1}")
    String getRoundIdForGameAndSequence(String gameId, int sequence);

    @Select("SELECT * FROM contest.round_player WHERE id = #{0}")
    RoundPlayer getRoundPlayer(String roundPlayerId);

    @Select(
        "SELECT * " +
        "  FROM contest.round_player " +
        " WHERE game_id = #{0} AND round_id = #{1} AND subscriber_id = #{2} AND determination = #{3} " +
        " ORDER BY create_date DESC")
    List<RoundPlayer> getRoundPlayerForDetermination(String gameId, String roundId, long subscriberId, RoundPlayer.ROUND_PLAYER_DETERMINATION determination);

    @Select(
        "SELECT * " +
        "  FROM contest.round_player " +
        " WHERE game_id = #{0} AND subscriber_id = #{1} AND determination <> 'ABANDONED' " +
        " ORDER BY create_date DESC LIMIT 1"
    )
    RoundPlayer getMostRecentRoundPlayer(String gameId, long subscriberId);

    @Select(
        "SELECT * " +
        "  FROM contest.round_player " +
        " WHERE round_id = #{0} AND subscriber_id = #{1} "
    )
    RoundPlayer getRoundPlayer2(String roundId, long subscriberId);

    //https://stackoverflow.com/questions/17038193/select-row-with-most-recent-date-per-user
    @Select(
        "select rp1.* " +
        "  from contest.round_player rp1, contest.round r " +
        " where r.id = rp1.round_id " +
        "   and find_in_set (r.round_type, #{1}) <> 0" +
        "   and rp1.game_id = #{0} " +
        "   and rp1.determination <> 'ABANDONED' " +
        "   and rp1.create_date = ( " +
        "   select max(rp2.create_date) " +
        "     from contest.round_player rp2 " +
        "    where rp2.subscriber_id = rp1.subscriber_id " +
        ")"
    )
    List<RoundPlayer> getMostRecentRoundPlayersForGame(String gameId, String roundTypeCommaDelimitedList);

    @Update("UPDATE contest.round_player SET determination = #{1} WHERE id = #{0}")
    void updateRoundPlayerDetermination(String roundPlayerId, RoundPlayer.ROUND_PLAYER_DETERMINATION determination);

    @Select("SELECT COUNT(*) FROM contest.match WHERE round_id = #{0}")
    int getMatchCountForRound(String roundId);
//    void deleteRound(Round round);

    @Insert(
        "INSERT INTO contest.round_player (" +
        "   id, game_id, round_id, subscriber_id, " +
        "   played_match_count, determination, receipt_id, amount_paid, refunded, rank, create_date" +
        ") VALUES (" +
        "   #{id}, #{gameId}, #{roundId}, #{subscriberId}, " +
        "   #{playedMatchCount}, #{determination}, #{receiptId}, #{amountPaid}, #{refunded}, #{rank}, NOW()" +
        ")"
    )
    void insertRoundPlayer(RoundPlayer roundPlayer);

    @Insert(
        "INSERT INTO contest.round_player (" +
        "   id, game_id, round_id, subscriber_id, " +
        "   played_match_count, determination, receipt_id, amount_paid, refunded, rank, create_date" +
        ") VALUES (" +
        "   #{id}, #{gameId}, #{roundId}, #{subscriberId}, " +
        "   #{playedMatchCount}, #{determination}, #{receiptId}, #{amountPaid}, #{refunded}, #{rank}, NOW()" +
        ") ON DUPLICATE KEY UPDATE " +
        "   played_match_count = #{playedMatchCount}, determination = #{determination}, " +
        "   receipt_id = #{receiptId}, amount_paid = #{amountPaid}, refunded = #{refunded}, rank = #{rank}, " +
        "   skill_answer_correct_pct = #{skillAnswerCorrectPct}, skill_average_answer_ms = #{skillAverageAnswerMs} "
    )
    void insertOrReplaceRoundPlayer(RoundPlayer roundPlayer);

    @Update(
        "UPDATE contest.round_player SET " +
        "   played_match_count = #{playedMatchCount}, determination = #{determination}, " +
        "   receipt_id = #{receiptId}, amount_paid = #{amountPaid}, refunded = #{refunded}, rank = #{rank}, " +
        "   skill_answer_correct_pct = #{skillAnswerCorrectPct}, skill_average_answer_ms = #{skillAverageAnswerMs}, " +
        "   skill = #{skill} " +
        "WHERE id = #{id}"
    )
    void updateRoundPlayer(RoundPlayer roundPlayer);

//    void deleteRoundPlayer(RoundPlayer roundPlayer);

    @Select("SELECT * FROM contest.round WHERE game_id = #{0} order by round_sequence")
    List<Round> getRoundsForGame(String gameId);

    @Select("SELECT * FROM contest.round_player where game_id = #{0} AND subscriber_id = #{1} ORDER BY create_date")
    List<RoundPlayer> getRoundPlayersForGame(String gameId, long subscriberId);

    @Select("SELECT * FROM contest.round_player where game_id = #{0} and determination in ('WON','LOST','TIMEDOUT','SAVED') order by create_date")
    List<RoundPlayer> getAllRoundPlayersForGame(String gameId);

    //@Select("SELECT subscriber_id FROM contest.round_player WHERE round_id = #{0}")
    @Select("SELECT distinct(subscriber_id) FROM contest.round_player WHERE round_id = #{0} and determination NOT IN ('ABANDONED','CANCELLED')")
    List<Long> getSubscriberIdsForRound(String roundId);

    @Insert(
        "INSERT INTO contest.round (id, game_id, " +
        "   round_type, round_status, round_sequence, final_round, round_purse, " +
        "   current_player_count, maximum_player_count, minimum_match_count, maximum_match_count, " +
        "   cost_per_player, round_activity_type, round_activity_value, " +
        "   minimum_activity_to_win_count, maximum_activity_count, " +
        "   activity_minimum_difficulty, activity_maximum_difficulty, " +
        "   activity_maximum_duration_seconds, player_maximum_duration_seconds, " +
        "   duration_between_activities_seconds, " +
        "   match_global, maximum_duration_minutes, match_player_count, " +
        "   pending_date, expected_open_date) " +
        "   SELECT #{2}, #{0}, " +
        "       round_type, 'PENDING', round_sequence, final_round, round_purse, " +
        "       0, maximum_player_count, minimum_match_count, maximum_match_count, " +
        "       cost_per_player, round_activity_type, round_activity_value, " +
        "       minimum_activity_to_win_count, maximum_activity_count, " +
        "       activity_minimum_difficulty, activity_maximum_difficulty, " +
        "       activity_maximum_duration_seconds, player_maximum_duration_seconds, " +
        "       duration_between_activities_seconds, " +
        "       match_global, maximum_duration_minutes, match_player_count, " +
        "       NOW(), #{3} " +
        "     FROM contest.round " +
        "    WHERE id = #{1}"
    )
    void cloneRound(String newGameId, String roundId, String newRoundId, Date expectedOpenDate);

    @Insert(
        "INSERT INTO contest.multi_localization (uuid, `type`, language_code, `value`) " +
        "   SELECT #{1}, `type`, language_code, `value` " +
        "     FROM contest.multi_localization " +
        "    WHERE uuid = #{0}"
    )
    void cloneRoundMultilocalizationValues(String roundId, String newRoundId);

    @Insert(
        "INSERT INTO contest.round_categories (round_id, category) " +
        "   SELECT #{1}, category " +
        "     FROM contest.round_categories " +
        "    WHERE round_id = #{0}"
    )
    void cloneRoundCategories(String roundId, String newRoundId);

    //
    // Match / MatchPlayer / MatchQueue
    //

    @Insert(
        "INSERT INTO contest.match (" +
        "   id, game_engine, engine_type, game_id, round_id, " +
        "   match_status, match_status_set_at, won_subscriber_id, minimum_activity_to_win_count, " +
        "   maximum_activity_count, actual_activity_count, determination, " +
        "   start_date, complete_date, create_date " +
        ") VALUES (" +
        "   #{id}, #{gameEngine}, #{engineType}, #{gameId}, #{roundId}, " +
        "   #{matchStatus}, #{matchStatusSetAt}, #{wonSubscriberId}, #{minimumActivityToWinCount}, " +
        "   #{maximumActivityCount}, #{actualActivityCount}, #{determination}, " +
        "   #{startDate}, #{completeDate}, NOW() " +
        ") ON DUPLICATE KEY UPDATE " +
        "   id = #{id}, game_engine = #{gameEngine}, engine_type = #{engineType}, game_id = #{gameId}, round_id = #{roundId}, " +
        "   match_status = #{matchStatus}, match_status_set_at = #{matchStatusSetAt}, won_subscriber_id = #{wonSubscriberId}, minimum_activity_to_win_count = #{minimumActivityToWinCount}, " +
        "   maximum_activity_count = #{maximumActivityCount}, actual_activity_count = #{actualActivityCount}, determination = #{determination}, " +
        "   start_date = #{startDate}, complete_date = #{completeDate}"
    )
    void insertOrReplaceMatch(Match match);

    @Select("SELECT * FROM contest.match WHERE id = #{0}")
    Match getMatch(String matchId);


    @Insert(
        "INSERT INTO contest.match_player (" +
        "   id, game_id, round_id, match_id, " +
        "   round_player_id, subscriber_id, " +
        "   determination, score, create_date " +
        ") VALUES (" +
        "   #{id}, #{gameId}, #{roundId}, #{matchId}, " +
        "   #{roundPlayerId}, #{subscriberId}, " +
        "   #{determination}, #{score}, NOW() " +
        ") ON DUPLICATE KEY UPDATE " +
        "   id = #{id}, game_id = #{gameId}, round_id = #{roundId}, match_id = #{matchId}, " +
        "   round_player_id = #{roundPlayerId}, subscriber_id = #{subscriberId}, " +
        "   determination = #{determination}, score = #{score}"
    )
    void insertOrReplaceMatchPlayer(MatchPlayer matchPlayer);

    @Select(
        "SELECT * " +
        "  FROM contest.match_queue " +
        " WHERE game_id = #{0} AND round_id = #{1} AND dequeue_timestamp IS NULL"
    )
	List<MatchQueue> getOpenMatchQueues(String gameId, String roundId);

    @Update(
        "UPDATE contest.match_queue " +
        "   SET cancelled = 1, dequeue_timestamp = #{3} " +
        " WHERE game_id = #{0} AND round_id = #{1} AND subscriber_id = #{2} AND dequeue_timestamp IS NULL"
    )
	void cancelMatchQueue(String gameId, String roundId, long subscriberId, Date dequeueTimestamp);

    @Select("SELECT * FROM contest.match_queue WHERE game_id = #{0} AND round_id = #{1} AND subscriber_id = #{2}")
	List<MatchQueue> getPlayerMatchQueue(String gameId, String roundId, long subscriberId);

    @Select(
        "SELECT * " +
        "  FROM contest.match_queue " +
        " WHERE game_id = #{0} AND round_id = #{1} AND subscriber_id = #{2} AND dequeue_timestamp IS NULL")
	List<MatchQueue> getPlayerAvailableMatchQueue(String gameId, String roundId, long subscriberId);

    @Update(
        "UPDATE contest.match_queue " +
        "   SET cancelled = #{cancelled}, enqueue_timestamp = #{enqueueTimestamp}, dequeue_timestamp = #{dequeueTimestamp} " +
        " WHERE id = #{id}"
    )
	void updateMatchQueue(MatchQueue matchQueue);

    @Insert(
        "INSERT INTO contest.match_queue (" +
        "   id, game_id, round_id, round_player_id, subscriber_id, " +
        "   cancelled, enqueue_timestamp, dequeue_timestamp " +
        ") VALUES (" +
        "   #{id}, #{gameId}, #{roundId}, #{roundPlayerId}, #{subscriberId}, " +
        "   #{cancelled}, #{enqueueTimestamp}, #{dequeueTimestamp} " +
        ") ON DUPLICATE KEY UPDATE " +
        "   id = #{id}, game_id = #{gameId}, round_id = #{roundId}, round_player_id = #{roundPlayerId}, subscriber_id = #{subscriberId}, " +
        "   cancelled = #{cancelled}, enqueue_timestamp = #{enqueueTimestamp}, dequeue_timestamp = #{dequeueTimestamp} "
    )
	void insertOrReplaceMatchQueue(MatchQueue matchQueue);

    @Select(
        "SELECT * " +
        "  FROM contest.match_queue " +
        " WHERE game_id = #{0} AND round_id = #{1} AND dequeue_timestamp IS NULL " +
        "   AND enqueue_timestamp <= DATE_SUB(NOW(), INTERVAL #{2}/1000 SECOND) "
    )
	List<MatchQueue> getMatchPlayersNotQueuedOlderThan(String gameId, String roundId, long ageMs);

    @Select("select subscriber_id from contest.match_queue where dequeue_timestamp is null and game_id = #{0}")
    List<Long> getSubscriberIdsThatWereNeverMatchedForGame(String gameId);

    @Delete("delete from contest.match_queue where dequeue_timestamp is null and game_id = #{0}")
    void removeSubscribersThatWereNeverMatchedForGame(String gameId);

    @Select("SELECT * FROM contest.match WHERE game_engine = #{0} AND engine_type = #{1} AND FIND_IN_SET (match_status, #{2}) <> 0")
	List<Match> getMatchesByStatusAndEngine(String gameEngine, String engineType, String statusesAsCommaDelimitedString);

    @Select("SELECT * FROM contest.match WHERE game_engine = #{0} AND engine_type = #{1} AND game_id = #{2} AND FIND_IN_SET (match_status, #{3}) <> 0")
    List<Match> getMatchesByEngineAndStatusAndGame(String gameEngine, String engineType, String gameId, String statusesAsCommaDelimitedString);

    @Select("SELECT * FROM contest.match_player WHERE match_id = #{0}")
    List<MatchPlayer> getMatchPlayersForMatch(String matchId);

    @Update(
        "UPDATE contest.match SET " +
        "   match_status = #{matchStatus}, match_status_set_at = #{matchStatusSetAt}, won_subscriber_id = #{wonSubscriberId}, " +
        "   minimum_activity_to_win_count = #{minimumActivityToWinCount}, " +
        "   maximum_activity_count = #{maximumActivityCount}, actual_activity_count = #{actualActivityCount}, " +
        "   determination = #{determination}, start_date = #{startDate}, complete_date = #{completeDate}, " +
        "   send_next_question_at = #{sendNextQuestionAt} " +
        " WHERE id = #{id}"
    )
    void updateMatch(Match match);

    @Update(
        "UPDATE contest.match_player SET " +
        "   determination = #{determination}, score = #{score}" +
        " WHERE id = #{id}"
    )
    void updateMatchPlayer(MatchPlayer matchPlayer);

    @Select("SELECT * FROM contest.match WHERE round_id = #{0} AND FIND_IN_SET (match_status, #{1}) <> 0")
    List<Match> getMatchesByRoundAndStatus(String roundId, String statusesAsCommaDelimitedString);

//    @Delete("DELETE FROM contest.game_player WHERE game_id = #{0}")
//    void resetGamePlayerTable(String gameId);

//    @Delete("DELETE FROM contest.round_player WHERE game_id = #{0}")
//    void resetRoundPlayerTable(String gameId);

//    @Delete("DELETE FROM contest.match_player WHERE game_id = #{0}")
//    void resetMatchPlayerTable(String gameId);

//    @Delete("DELETE FROM contest.match WHERE game_id = #{0}")
//    void resetMatchTable(String gameId);

//    @Delete("DELETE FROM contest.match_queue WHERE game_id = #{0}")
//    void resetMatchQueueTable(String gameId);

//    //
//    // open game
//    //
//
//    @Update("UPDATE contest.game SET game_status = 'OPEN' WHERE id = #{0} AND game_status = 'PENDING'")
//    void openGame(String gameId);
//
    @Update("UPDATE contest.round SET round_status = 'VISIBLE', visible_date = NOW() WHERE game_id = #{0} AND round_status = 'PENDING'")
    void setRoundStatusesVisibleForNewlyOpenedGame(String gameId);

    //
    //cancel game
    //

    @Update("UPDATE contest.game SET game_status = 'CANCELLED', cancelled_date = NOW() WHERE id = #{0}")
    void cancelGame(String gameId);

    @Update("UPDATE contest.game_player SET determination = 'CANCELLED' WHERE game_id = #{0}")
    void cancelGamePlayersForGame(String gameId);

    @Update("UPDATE contest.round SET round_status = 'CANCELLED', cancelled_date = NOW() WHERE game_id = #{0}")
    void cancelRoundsForGame(String gameid);

    @Update("UPDATE contest.round_player SET determination = 'CANCELLED' WHERE game_id = #{0}")
    void cancelRoundPlayersForGame(String gameId);

    @Update("UPDATE contest.match SET match_status = 'CANCELLED' WHERE game_id = #{0}")
    void cancelMatchesForGame(String gameId);

    @Update("UPDATE contest.match_player SET determination = 'CANCELLED' WHERE game_id = #{0}")
    void cancelMatchPlayersForGame(String gameId);

    //
    // min_age_by_region
    //

    @Select("select min_age from contest.min_age_by_region where country_region = concat(#{0},'_',#{1})")
    Integer getMinAgeForRegion(String countryCode, String region);
}
