package tv.shout.snowyowl.dao;

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Many;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.meinc.gameplay.domain.Tuple;

import tv.shout.snowyowl.domain.AffiliatePlan;
import tv.shout.snowyowl.domain.GamePayout;
import tv.shout.snowyowl.domain.GameWinner;
import tv.shout.snowyowl.domain.IneligibleSubscriber;
import tv.shout.snowyowl.domain.MatchQuestion;
import tv.shout.snowyowl.domain.PayoutModel;
import tv.shout.snowyowl.domain.PayoutModelRound;
import tv.shout.snowyowl.domain.PayoutTableRow;
import tv.shout.snowyowl.domain.ProhibitedSubscriber;
import tv.shout.snowyowl.domain.Question;
import tv.shout.snowyowl.domain.QuestionAnswer;
import tv.shout.snowyowl.domain.QuestionCategory;
import tv.shout.snowyowl.domain.ReportStructAffiliatePayoutSubscriberInfo;
import tv.shout.snowyowl.domain.ReportStructAffiliatePayoutSubscriberNetworkSize;
import tv.shout.snowyowl.domain.ReportStructAffiliatePayoutSubscriberWinnings;
import tv.shout.snowyowl.domain.Sponsor;
import tv.shout.snowyowl.domain.SponsorCashPool;
import tv.shout.snowyowl.domain.SubscriberFromSearch;
import tv.shout.snowyowl.domain.SubscriberQuestionAnswer;
import tv.shout.snowyowl.domain.SubscriberRoundQuestion;
import tv.shout.snowyowl.domain.SubscriberStats;

public interface IDaoMapper
{
    //
    // multi-localization
    //

    @Insert(
        "INSERT IGNORE INTO snowyowl.multi_localization (" +
        "   `uuid`, `type`, language_code, `value`) " +
        "VALUES (#{0}, #{1}, #{2}, #{3}) "
    )
    void insertOrReplaceMultiLocalizationValue(String uuid, String type, String languageCode, String value);

    @Select("SELECT language_code, `value` FROM snowyowl.multi_localization WHERE `uuid` = #{0} AND `type` = #{1}")
    @Results({
        @Result(property="key", column="language_code"),
        @Result(property="val", column="value")
    })
    List<Tuple<String>> getMultiLocalizationValues(String uuid, String type);

    @Delete("DELETE FROM snowyowl.multi_localization WHERE `uuid` = #{0} AND `type` = #{1}")
    void removeMutliLocalizationValues(String uuid, String type);

    //
    // bot
    //

    @Select("SELECT subscriber_id FROM snowyowl.bot_player WHERE busy_flag = 0")
    List<Long> getIdleBotIds();

    @Select("SELECT COUNT(*) FROM snowyowl.bot_player")
    int getBotCount();

    @Update("UPDATE snowyowl.bot_player SET busy_flag = 0, game_id = null WHERE subscriber_id = #{0}")
    void markBotIdle(long subscriberId);

    @Select("SELECT subscriber_id FROM snowyowl.bot_player WHERE game_id = #{0}")
    List<Long> getBotsForGame(String gameId);

    @Update("UPDATE snowyowl.bot_player SET busy_flag = 0, game_id = null WHERE game_id = #{0}")
    void releaseBotsForGame(String gameId);

    //
    // app
    //

    @Select("SELECT ios_bundle_id, android_bundle_id, windows_bundle_id FROM gameplay.app WHERE app_id = #{0}")
    List<String> getBundleIdsForApp(int appId);

    //
    // socket.io logs
    //

    @Insert("INSERT INTO snowyowl.socket_io_log (subscriber_id, message_type, message, `status`, sent_date) VALUES (#{0}, #{1}, #{2}, #{3}, #{4})")
    void addSocketIoLog(Integer subscriberId, String messageType, String message, String status, Date sentDate);

    //
    // phone verification
    //

    @Delete("DELETE FROM snowyowl.phone_verification_code WHERE subscriber_id = #{0}")
    void clearPhoneVerificationCodeForSubscriber(long subscriberId);

    @Insert("INSERT INTO snowyowl.phone_verification_code (subscriber_id, phone, code, create_date) VALUES (#{0}, #{1}, #{2}, NOW())")
    void addPhoneVerificationCodeForSubscriber(long subscriberId, String phone, String code);

    @Select("SELECT count(*) FROM snowyowl.phone_verification_code WHERE subscriber_id = #{0} AND phone = #{1} AND code = #{2} AND create_date >= #{3}")
    boolean isPhoneVerificationCodeValidForSubscriber(long subscriberId, String phone, String code, Date cutoffDate);

    //
    // subscriber
    //

    //this query doesn't really belong here, but it was so specific that it didn't seem right to put it inside of the identity service either
    @Select(
        "SELECT subscriber_id, email, nickname, firstname, lastname, create_date " +
        "  FROM gameplay.s_subscriber " +
        " WHERE nickname not like 'playerbot_%' and nickname not like '__player%' and email not like '%@shoutgp.com' " +
        "   AND create_date >= #{0} AND create_date <= #{1}" +
        " ORDER BY create_date ASC"
    )
    List<SubscriberFromSearch> getSubscribersInSignupDateRange(Date from, Date to);

    //
    // QuestionCategory
    //

    @Insert("INSERT INTO snowyowl.question_category_list (id, category_key) VALUES (#{id}, #{categoryKey})")
    void insertQuestionCategory(QuestionCategory category);

    @Select("SELECT id, category_key FROM snowyowl.question_category_list")
    @Results({
        @Result(property="key", column="id"),
        @Result(property="val", column="category_key")
    })
    List<Tuple<String>> getQuestionCategoryIdToKey();

    @Select("SELECT * FROM snowyowl.question_category_list")
    List<QuestionCategory> getAllQuestionCategories();

    @Select("SELECT * FROM snowyowl.question_category_list WHERE category_key = #{0}")
    QuestionCategory getQuestionCategoryByKey(String key);

    @Select("SELECT * FROM snowyowl.question_category_list WHERE id = #{0}")
    QuestionCategory getQuestionCategoryById(String id);

    @Select("SELECT question_id FROM snowyowl.question_categories WHERE category_id = #{0}")
    List<String> getQuestionIdsForCategory(String categoryId);

    @Delete("DELETE FROM snowyowl.multi_localization WHERE uuid = #{0} and `type` = 'categoryName'")
    void removeQuestionCategoryNames(String categoryId);

    @Delete("DELETE FROM snowyowl.question_category_list WHERE id = #{0}")
    void deleteQuestionCategory(String categoryId);

    //
    // SubscriberQuestionAnswer
    //

    @Select("SELECT * FROM snowyowl.subscriber_question_answer WHERE id = #{0}")
    SubscriberQuestionAnswer getSubscriberQuestionAnswer(String subscriberQuestionAnswerId);

    @Update(
        "UPDATE snowyowl.subscriber_question_answer " +
        "   SET selected_answer_id = #{selectedAnswerId}, duration_milliseconds = #{durationMilliseconds} " +
        " WHERE id = #{id}"
    )
    void setAnswerOnSubscriberQuestionAnswer(SubscriberQuestionAnswer sqa);

    @Update(
        "UPDATE snowyowl.subscriber_question_answer " +
        "   SET question_presented_timestamp = #{questionPresentedTimestamp} " +
        " WHERE id = #{id}"
    )
    void setQuestionViewedTimestampOnSubscriberQuestionAnswer(SubscriberQuestionAnswer sqa);

    //
    // MatchManagementEngine
    //

    @Select("SELECT * FROM snowyowl.match_question WHERE match_id = #{0}")
    List<MatchQuestion> getMatchQuestionsForMatch(String matchId);

    @Select("SELECT * FROM snowyowl.subscriber_question_answer WHERE match_question_id = #{0}")
    List<SubscriberQuestionAnswer> getSubscriberQuestionAnswersViaMatchQuestion(String matchQuestionId);

    @Insert(
        "INSERT INTO snowyowl.match_question (" +
        "   id, game_id, round_id, match_id, question_id, question_value, " +
        "   match_question_status, create_date) VALUES (" +
        "   #{id}, #{gameId}, #{roundId}, #{matchId}, #{questionId}, #{questionValue}, " +
        "   #{matchQuestionStatus}, NOW())"
    )
    void insertMatchQuestion(MatchQuestion matchQuestion);

    @Update(
        "UPDATE snowyowl.match_question " +
        "   SET match_question_status = #{matchQuestionStatus}, won_subscriber_id = #{wonSubscriberId}, " +
        "       determination = #{determination}, completed_date = #{completedDate} " +
        " WHERE id = #{id}"
    )
    void updateMatchQuestion(MatchQuestion matchQuestion);

    @Insert(
        "INSERT INTO snowyowl.subscriber_question_answer (" +
        "   id, game_id, round_id, match_id, question_id, match_question_id, subscriber_id, " +
        "   question_decrypt_key, determination, create_date " +
        ") VALUES (" +
        "   #{id}, #{gameId}, #{roundId}, #{matchId}, #{questionId}, #{matchQuestionId}, #{subscriberId}, " +
        "   #{questionDecryptKey}, #{determination}, NOW())"
    )
    void insertSubscriberQuestionAnswer(SubscriberQuestionAnswer sqa);

    @Update(
        "UPDATE snowyowl.subscriber_question_answer" +
        "   SET selected_answer_id = #{selectedAnswerId}, question_presented_timestamp = #{questionPresentedTimestamp}, " +
        "       duration_milliseconds = #{durationMilliseconds}, determination = #{determination}, won = #{won} " +
        " WHERE id = #{id}"
    )
    void updateSubscriberQuestionAnswer(SubscriberQuestionAnswer sqa);

    @Select(
        "SELECT DISTINCT(q.id) " +
        "  FROM snowyowl.question q, " +
        "       snowyowl.question_language_codes qlc, " +
        "       snowyowl.question_categories qc, " +
        "       snowyowl.question_category_list cat " +
        " WHERE q.id = qlc.question_id AND q.id = qc.question_id AND qc.category_id = cat.id " +
        "   AND q.difficulty >= #{0} AND q.difficulty <= #{1} " +
        "   AND (q.expiration_date IS NULL OR q.expiration_date > NOW()) " +
        "   AND q.`status` = 'PUBLISHED' " +
        "   AND FIND_IN_SET(qlc.language_code, #{2}) <> 0 " +
        "   AND FIND_IN_SET(cat.id, #{3}) <> 0"
    )
    List<String> getQuestionIdsBasedOnFilters(int minDifficulty, int maxDifficulty, String languageCodesAsCommaDelimitedList, String categoryUuidsAsCommaDelimiatedList);

    @Select(
        "SELECT DISTINCT(q.id) " +
        "  FROM snowyowl.question q, " +
        "       snowyowl.question_language_codes qlc " +
        " WHERE q.id = qlc.question_id " +
        "   AND q.difficulty >= #{0} AND q.difficulty <= #{1} " +
        "   AND (q.expiration_date IS NULL OR q.expiration_date > NOW()) " +
        "   AND q.`status` = 'PUBLISHED' " +
        "   AND FIND_IN_SET(qlc.language_code, #{2}) <> 0"
    )
    List<String> getQuestionIdsBasedOnFiltersSansCategory(int minDifficulty, int maxDifficulty, String languageCodesAsCommaDelimitedList);

    @Update("UPDATE snowyowl.question SET usage_count = usage_count+1 WHERE id = #{0}")
    void incrementQuestionUsageCount(String questionId);

    @Select("SELECT * FROM snowyowl.subscriber_question_answer WHERE match_id = #{0} AND subscriber_id = #{1}")
    List<SubscriberQuestionAnswer> getSubscriberQuestionAnswersForMatch(String matchId, long subscriberId);

    @Insert("INSERT INTO snowyowl.tie_breaker_question (game_id, match_id) VALUES (#{0}, #{1})")
    void addTieBreakerQuestion(String gameId, String matchId);

    @Select("SELECT COUNT(*) FROM snowyowl.tie_breaker_question WHERE game_id = #{0} AND match_id = #{1}")
    boolean isTieBreakerQuestion(String gameId, String matchId);

    @Select("SELECT winner_subscriber_id FROM snowyowl.tie_breaker_question WHERE game_id = #{0} AND match_id = #{1}")
    long getTieBreakerWinnerSubscriberId(String gameId, String matchId);

    @Update("UPDATE snowyowl.tie_breaker_question SET winner_subscriber_id = #{2} WHERE game_id = #{0} AND match_id = #{1}")
    void addTieBreakerWinnerSubscriberId(String gameId, String matchId, long subscriberId);

    //
    // Question/QuestionAnswer
    //

    @Select("SELECT * FROM snowyowl.question WHERE id = #{0}")
    Question getQuestion(String questionId);

    @Select("SELECT language_code FROM snowyowl.question_language_codes WHERE question_id = #{0}")
    Set<String> getQuestionLanguageCodes(String questionId);

    @Select("SELECT country_code FROM snowyowl.question_forbidden_country_codes WHERE question_id = #{0}")
    Set<String> getQuestionForbiddenCountryCodes(String questionId);

    @Select("SELECT category_id FROM snowyowl.question_categories WHERE question_id = #{0}")
    Set<String> getQuestionCategoryUuids(String questionId);

    @Select("SELECT * FROM snowyowl.question_answer WHERE question_id = #{0}")
    List<QuestionAnswer> getQuestionAnswersForQuestion(String questionId);

    //
    // admin - Question
    //

    @Insert(
        "INSERT INTO snowyowl.question (" +
        "   id, difficulty, source, media_url, media_type, create_date, expiration_date, `status`) " +
        "VALUES (#{id}, #{difficulty}, #{source}, #{mediaUrl}, #{mediaType}, NOW(), expiration_date, 'UNPUBLISHED')"
    )
    void createQuestion(Question question);

    @Insert(
        "INSERT INTO snowyowl.question_language_codes (question_id, language_code) " +
        "VALUES (#{0} ,#{1})")
    void addQuestionLanguageCode(String questionId, String languageCode);

    @Insert(
        "INSERT INTO snowyowl.question_forbidden_country_codes (question_id, country_code) " +
        "VALUES (#{0} ,#{1})")
    void addQuestionForbiddenCountryCode(String questionId, String countryCode);

    @Insert(
        "INSERT INTO snowyowl.question_categories (question_id, category_id) " +
        "VALUES (#{0}, #{1})")
    void addQuestionCategory(String questionId, String categoryUuid);

    @Insert(
        "INSERT INTO snowyowl.question_answer (" +
        "   id, question_id, media_url, media_type, correct, survey_percent, create_date) " +
        "VALUES (#{id}, #{questionId}, #{mediaUrl}, #{mediaType}, #{correct}, #{surveyPercent}, NOW())"
    )
    void addQuestionAnswer(QuestionAnswer answer);

    @Select("SELECT id FROM snowyowl.question WHERE `status` = #{0} ORDER BY create_date DESC")
    List<String> getQuestionIdsByState(Question.STATUS status);

    @Update("UPDATE snowyowl.question SET `status` = #{1} WHERE id = #{0}")
    void updateQuestionStatus(String questionId, Question.STATUS newStatus);

    @Delete("DELETE FROM snowyowl.question_answer WHERE question_id = #{0}")
    void deleteQuestionAnswers(String questionid);

    @Delete("DELETE FROM snowyowl.question_categories WHERE question_id = #{0}")
    void deleteQuestionCategories(String questionId);

    @Delete("DELETE FROM snowyowl.question_forbidden_country_codes WHERE question_id = #{0}")
    void deleteQuestionForbiddenCountryCodes(String questionId);

    @Delete("DELETE FROM snowyowl.question_language_codes WHERE question_id = #{0}")
    void deleteQuestionLanguageCodes(String questionId);

    @Delete("DELETE FROM snowyowl.question WHERE id = #{0}")
    void deleteQuestion(String questionId);

    //
    // admin - game winners
    //

    @Select(
        "select gp.game_id, gp.rank, s.email, s.nickname, gp.subscriber_id, gp.payout_awarded_amount as amount " +
        "  from contest.game_player gp, gameplay.s_subscriber s " +
        " where gp.game_id = #{0} " +
        "   and gp.determination = 'AWARDED' " +
        "   and gp.subscriber_id not in (select bp.subscriber_id from snowyowl.bot_player bp) " +
        "   and gp.subscriber_id = s.subscriber_id " +
        " order by -gp.rank DESC"
    )
    List<GameWinner> getGameWinners(String gameId);

    //
    // game round grid
    //

    @Select("SELECT question_id FROM snowyowl.game_round_questions WHERE game_id = #{0} AND round_id = #{1} ORDER BY `order`")
    List<String> getGameRoundQuestionIds(String gameId, String roundid);

    @Insert("INSERT INTO snowyowl.game_round_questions (game_id, round_id, question_id, `order`) VALUES (#{0}, #{1}, #{3}, #{4})")
    void addGameRoundQuestion(String gameId, String roundId, String questionId, int order);

    @Select("SELECT subscriber_id, round_id, question_id, `order`, seen FROM snowyowl.subscriber_round_questions WHERE subscriber_id = #{0} AND round_id = #{1} ORDER BY `order`")
    List<SubscriberRoundQuestion> getSubscriberRoundQuestions(long subscriberId, String roundId);

    @Insert("INSERT INTO snowyowl.subscriber_round_questions (subscriber_id, round_id, question_id, `order`, seen) VALUES (#{0}, #{1}, #{2}, #{3}, 0)")
    void addSubscriberRoundQuestion(long subscriberId, String roundId, String questionId, int order);

    @Update("UPDATE snowyowl.subscriber_round_questions SET seen = #{seen} WHERE subscriber_id = #{subscriberId} AND round_id = #{roundId} AND question_id = #{questionId}")
    void updateSubscriberRoundQuestion(SubscriberRoundQuestion srq);

    //
    // payout table
    //

    @Insert("INSERT INTO snowyowl.payout_table (game_id, row_id, rank_from, rank_to, amount) VALUES (#{0}, #{1}, #{2}, #{3}, #{4})")
    void addPayoutTableRow(String gameId, String rowId, int rankFrom, int rankTo, float amount);

    @Delete("DELETE FROM snowyowl.payout_table WHERE game_id = #{0}")
    void removePayoutTableRows(String gameId);

    @Select("SELECT * FROM snowyowl.payout_table WHERE game_id = #{0} ORDER BY rank_from ASC")
    List<PayoutTableRow> getPayoutTableRows(String gameId);

    //
    // game payout
    //

    @Select("SELECT * FROM snowyowl.game_payout WHERE game_id = #{0}")
    GamePayout getGamePayout(String gameId);

    //@Delete("DELETE FROM snowyowl.game_payout WHERE game_id = #{0}")
    //void deleteGamePayout(String gameId);

    @Insert(
        "INSERT INTO snowyowl.game_payout (game_id, payout_model_id, minimum_payout_amount, give_sponsor_player_winnings_back_to_sponsor) " +
        "VALUES (#{gameId}, #{payoutModelId}, #{minimumPayoutAmount}, #{giveSponsorPlayerWinningsBackToSponsor})"
    )
    void addGamePayout(GamePayout gamePayout);

    //
    // payout model
    //

    @Select("SELECT * FROM snowyowl.payout_model WHERE payout_model_id = #{0}")
    @Results({
        @Result(property="payoutModelId", column="payout_model_id"),
        @Result(property="name", column="name"),
        @Result(property="basePlayerCount", column="base_player_count"),
        @Result(property="entranceFeeAmount", column="entrance_fee_amount"),
        @Result(property="active", column="active"),
        @Result(property="deactivationReason", column="deactivation_reason"),
        @Result(property="creatorId", column="creator_id"),
        @Result(property="deactivatorId", column="deactivator_id"),
        @Result(property="createDate", column="create_date"),
        @Result(property="deactivatedDate", column="deactivated_date"),
        @Result(property="scalePayout", column="scale_payout"),
        @Result(property="minimumFirstPlacePayoutAmount", column="minimum_first_place_payout_mount"),
        @Result(property="minimumSecondPlacePayoutAmount", column="minimum_second_place_payout_amount"),
        @Result(property="minimumOverallPayoutAmount", column="minimum_overall_payout_amount"),
        @Result(property="payoutModelRounds", javaType=List.class, column="payout_model_id", many=@Many(select="getPayoutModelRounds"))
    })
    PayoutModel getPayoutModel(int payoutModelId);

    @Select("SELECT * FROM snowyowl.payout_model WHERE entrance_fee_amount = #{0}")
    @Results({
        @Result(property="payoutModelId", column="payout_model_id"),
        @Result(property="name", column="name"),
        @Result(property="basePlayerCount", column="base_player_count"),
        @Result(property="entranceFeeAmount", column="entrance_fee_amount"),
        @Result(property="active", column="active"),
        @Result(property="deactivationReason", column="deactivation_reason"),
        @Result(property="creatorId", column="creator_id"),
        @Result(property="deactivatorId", column="deactivator_id"),
        @Result(property="createDate", column="create_date"),
        @Result(property="deactivatedDate", column="deactivated_date"),
        @Result(property="scalePayout", column="scale_payout"),
        @Result(property="minimumFirstPlacePayoutAmount", column="minimum_first_place_payout_mount"),
        @Result(property="minimumSecondPlacePayoutAmount", column="minimum_second_place_payout_amount"),
        @Result(property="minimumOverallPayoutAmount", column="minimum_overall_payout_amount"),
        @Result(property="payoutModelRounds", javaType=List.class, column="payout_model_id", many=@Many(select="getPayoutModelRounds"))
    })
    List<PayoutModel> getPayoutModelsByEntranceFee(float entranceFee);

    @Select("SELECT * FROM snowyowl.payout_model")
    @Results({
        @Result(property="payoutModelId", column="payout_model_id"),
        @Result(property="name", column="name"),
        @Result(property="basePlayerCount", column="base_player_count"),
        @Result(property="entranceFeeAmount", column="entrance_fee_amount"),
        @Result(property="active", column="active"),
        @Result(property="deactivationReason", column="deactivation_reason"),
        @Result(property="creatorId", column="creator_id"),
        @Result(property="deactivatorId", column="deactivator_id"),
        @Result(property="createDate", column="create_date"),
        @Result(property="deactivatedDate", column="deactivated_date"),
        @Result(property="scalePayout", column="scale_payout"),
        @Result(property="minimumFirstPlacePayoutAmount", column="minimum_first_place_payout_mount"),
        @Result(property="minimumSecondPlacePayoutAmount", column="minimum_second_place_payout_amount"),
        @Result(property="minimumOverallPayoutAmount", column="minimum_overall_payout_amount"),
        @Result(property="payoutModelRounds", javaType=List.class, column="payout_model_id", many=@Many(select="getPayoutModelRounds"))
    })
    List<PayoutModel> getAllPayoutModels();

    @Select("SELECT * FROM snowyowl.payout_model_round WHERE payout_model_id = #{0} ORDER BY sort_order")
    List<PayoutModelRound> getPayoutModelRounds(int payoutModelId);

    @Insert(
        "INSERT INTO snowyowl.payout_model (" +
        "   `name`, base_player_count, entrance_fee_amount, " +
        "   scale_payout, minimum_first_place_payout_mount, minimum_second_place_payout_amount, minimum_overall_payout_amount, " +
        "   creator_id, create_date" +
        ") VALUES (" +
        "   #{name}, #{basePlayerCount}, #{entranceFeeAmount}, " +
        "   #{scalePayout}, #{minimumFirstPlacePayoutAmount}, #{minimumSecondPlacePayoutAmount}, #{minimumOverallPayoutAmount}, " +
        "   #{creatorId}, #{createDate}" +
        ")"
    )
    @Options(useGeneratedKeys=true, keyProperty="payoutModelId", keyColumn="payout_model_id")
    void insertPayoutModel(PayoutModel payoutModel);

    @Insert(
        "INSERT INTO snowyowl.payout_model_round (" +
        "   payout_model_id, sort_order, `description`, starting_player_count, eliminated_player_count, eliminated_payout_amount, `type`, `category`" +
        ") VALUES (" +
        "   #{payoutModelId}, #{sortOrder}, #{description}, #{startingPlayerCount}, #{eliminatedPlayerCount}, #{eliminatedPayoutAmount}, #{type}, #{category}" +
        ")"
    )
    void insertPayoutModelRound(PayoutModelRound pmr);

    @Select("SELECT COUNT(*) FROM snowyowl.game_payout WHERE payout_model_id = #{0}")
    boolean isPayoutModelInUse(int payoutModelId);

    @Update(
        "UPDATE snowyowl.payout_model SET " +
        "   `name` = #{name}, base_player_count = #{basePlayerCount}, entrance_fee_amount = #{entranceFeeAmount}, " +
        "   scale_payout = #{scalePayout}, " +
        "   minimumFirstPlacePayoutAmount = #{minimum_first_place_payout_mount}, minimumSecondPlacePayoutAmount = #{minimum_second_place_payout_amount}, minimumOverallPayoutAmount = #{minimum_overall_payout_amount} " +
        " WHERE payout_model_id = #{payoutModelId}"
    )
    void updatePayoutModel(PayoutModel pm);

    @Delete("DELETE FROM snowyowl.payout_model WHERE payout_model_id = #{0}")
    void deletePayoutModel(int payoutModelId);

    @Delete("DELETE FROM snowyowl.payout_model_round WHERE payout_model_id = #{0}")
    void deletePayoutModelRounds(int payoutModelId);

    @Update("UPDATE snowyowl.payout_model SET `active` = 0, deactivator_id = #{1}, deactivated_date = NOW(), deactivation_reason = #{2} WHERE payout_model_id = #{0}")
    void deactivatePayoutModel(int payoutModelId, long deactivatorId, String reason);

    //
    // snowyowl.affiliate_plan
    //

    @Insert(
        "INSERT INTO snowyowl.affiliate_plan (" +
        "   `current`, affiliate_direct_payout_pct, affiliate_secondary_payout_pct, affiliate_tertiary_payout_pct, player_initial_payout_pct) " +
        "VALUES (" +
        "   1, #{affiliateDirectPayoutPct}, #{affiliateSecondaryPayoutPct}, #{affiliateTertiaryPayoutPct}, #{playerInitialPayoutPct} " +
        ")"
    )
    @Options(useGeneratedKeys=true, keyProperty="affiliatePlanId", keyColumn="affiliate_plan_id")
    void addAffiliatePlan(AffiliatePlan plan);

    @Update("UPDATE snowyowl.affiliate_plan SET `current` = 0 WHERE `current` = 1")
    void clearCurrentAffiliatePlan();

    @Select("SELECT * FROM snowyowl.affiliate_plan WHERE `current` = 1")
    AffiliatePlan getCurrentAffiliatePlan();

    @Select("SELECT * FROM snowyowl.affiliate_plan WHERE affiliate_plan_id = #{0}")
    AffiliatePlan getAffiliatePlan(int affiliatePlanId);

    //
    // inelligible subscribers
    //

    @Insert(
        "INSERT INTO snowyowl.ineligible_subscribers (subscriber_id, email, linked_subscriber_id, linked_email, `reason`, create_date) " +
        "VALUES (#{subscriberId}, #{email}, #{linkedSubscriberId}, #{linkedEmail}, #{reason}, NOW())"
    )
    void insertIneligibleSubscriber(IneligibleSubscriber is);

    @Select("SELECT * FROM snowyowl.ineligible_subscribers")
    List<IneligibleSubscriber> getIneligibleSubscribers();

    @Delete("DELETE FROM snowyowl.ineligible_subscribers WHERE is_id = #{0}")
    void deleteIneligibleSubscriber(long isId);

    @Select("SELECT COUNT(*) FROM snowyowl.ineligible_subscribers WHERE subscriber_id = #{0}")
    boolean isSubscriberIneligible(long subscriberId);

    //
    // prohibited subscribers
    //

    @Select("SELECT * FROM snowyowl.prohibited_subscribers")
    List<ProhibitedSubscriber> getProhibitedSubscribers();

    @Insert(
        "INSERT INTO snowyowl.prohibited_subscribers (subscriber_id, email, nickname, reason, note, create_date) VALUES (" +
        "#{subscriberId}, #{email}, #{nickname}, #{reason}, #{note}, NOW())"
    )
    void insertProhibitedSubscriber(ProhibitedSubscriber ps);

    @Delete("DELETE FROM snowyowl.prohibited_subscribers WHERE subscriber_id = #{0}")
    void deleteProhibitedSubscriber(long subscriberId);

    @Select("SELECT COUNT(*) FROM snowyowl.prohibited_subscribers WHERE subscriber_id = #{0}")
    boolean isSubscriberProhibited(long subscriberId);

    //
    // subscriber action log
    //

    @Insert("INSERT INTO snowyowl.subscriber_action_log (subscriber_id, `action`, `reason`, `note`, create_date) VALUES (#{0}, #{1}, #{2}, #{3}, NOW())")
    void addSubscriberActionLog(long subscriberId, String action, String reason, String note);

    //
    // subscriber stats
    //

    @Select("SELECT * FROM snowyowl.subscriber_stats WHERE subscriber_id = #{0}")
    SubscriberStats getSubscriberStats(long subscriberId);

    @Insert("INSERT INTO snowyowl.subscriber_stats " +
            "   (subscriber_id, games_played, bracket_rounds_played, pool_rounds_played, questions_answered, questions_correct, cumulative_question_score, affiliate_plan_id) " +
            "   VALUES (#{subscriberId}, #{gamesPlayed}, #{bracketRoundsPlayed}, #{poolRoundsPlayed}, #{questionsAnswered}, #{questionsCorrect}, #{cumulativeQuestionScore}, #{affiliatePlanId})"
    )
    void insertSubscriberStats(SubscriberStats stats);

    @Update("UPDATE snowyowl.subscriber_stats SET " +
            "   games_played = #{gamesPlayed}, bracket_rounds_played = #{bracketRoundsPlayed}, pool_rounds_played = #{poolRoundsPlayed}, " +
            "   questions_answered = #{questionsAnswered}, questions_correct = #{questionsCorrect}, cumulative_question_score = #{cumulativeQuestionScore}, " +
            "   affiliate_plan_id = #{affiliatePlanId} " +
            "WHERE subscriber_id = #{subscriberId}"
    )
    void updateSubscriberStats(SubscriberStats stats);

    @Select("SELECT * FROM snowyowl.subscriber_stats")
    List<SubscriberStats> getAllSubscriberStats();

    //
    // subscriber_question_answers
    //

    @Insert("INSERT IGNORE INTO `snowyowl`.`subscriber_game_questions` (`game_id`, `subscriber_id`, `question_id`) VALUES (#{0}, #{1}, #{2})")
    void addSubscriberQuestion(String gameId, long subscriberId, String questionId);

    @Select("select distinct(question_id) from snowyowl.subscriber_game_questions where find_in_set(subscriber_id, #{1}) <> 0 and game_id = #{0}")
    List<String> getCombinedSubscriberQuestions(String gameId, String subscriberIdsAsCommaDelimitedList);

    @Delete("delete from snowyowl.subscriber_game_questions where game_id = #{0}")
    void clearSubscriberQuestions(String gameId);

    //
    // daily status email (note: this crosses databases, but is all here for convenience)
    //

    @Select("select subscriber_id, sum(amount) as total from contest.cash_pool_transaction2 where `type` = 'PAYOUT_REFERRAL' and transaction_date > #{0} group by subscriber_id")
    List<ReportStructAffiliatePayoutSubscriberWinnings> getReportStructAffiliatePayoutSubscriberWinnings(Date since);

    @Select(
        "select mint_parent_subscriber_id as subscriber_id, count(*) as size " +
        "  from gameplay.s_subscriber " +
        " where FIND_IN_SET(mint_parent_subscriber_id, #{0}) <> 0" +
        " group by mint_parent_subscriber_id"
    )
    List<ReportStructAffiliatePayoutSubscriberNetworkSize> getReportStructAffiliatePayoutSubscriberNetworkSize(String subscriberIdsAsCommaDelimitedList);

    @Select("select subscriber_id, nickname, email from gameplay.s_subscriber where find_in_set(subscriber_id, #{0}) <> 0")
    List<ReportStructAffiliatePayoutSubscriberInfo> getReportStructAffiliatePayoutSubscriberInfo(String subscriberIdsAsCommaDelimitedList);

    //
    // sponsor
    //

    @Select("SELECT * FROM snowyowl.sponsor_cash_pool WHERE subscriber_id = #{0}")
    SponsorCashPool getSponsorCashPoolByPoolOwnerSubscriberId(long subscriberId);

    @Select(
        "select p.* " +
        "  from snowyowl.sponsor_cash_pool p, snowyowl.sponsor_player sp " +
        " where sp.subscriber_id = #{0} " +
        "   and p.sponsor_cash_pool_id = sp.sponsor_cash_pool_id"
    )
    SponsorCashPool getSponsorCashPoolBySponsorPlayerSubscriberId(long subscriberId);

    @Select("SELECT * FROM snowyowl.sponsor_cash_pool WHERE sponsor_cash_pool_id = #{0}")
    SponsorCashPool getSponsorCashPoolById(int sponsorCashPoolId);

    @Insert("INSERT INTO snowyowl.sponsor_cash_pool (subscriber_id, `amount`) VALUES (#{subscriberId}, #{amount})")
    @Options(useGeneratedKeys=true, keyProperty="sponsorCashPoolId", keyColumn="sponsor_cash_pool_id")
    void insertSponsorCashPool(SponsorCashPool pool);

    @Update("UPDATE snowyowl.sponsor_cash_pool SET `amount` = #{amount} WHERE sponsor_cash_pool_id = #{sponsorCashPoolId}")
    void updateSponsorCashPool(SponsorCashPool pool);

    @Insert(
        "INSERT INTO snowyowl.sponsor_cash_pool_transaction (" +
        "     sponsor_cash_pool_id, `amount`, `reason`, transaction_date " +
        ") VALUES (" +
        "     #{0}, #{1}, #{2}, NOW() " +
        ")"
    )
    void addSponsorCashPoolTransaction(int sponsorCashPoolId, double amount, String reason);

    @Select("SELECT COUNT(*) FROM snowyowl.sponsor_player WHERE busy_flag = 0")
    int getNumberOfAvailableSponsors();

    @Select("SELECT subscriber_id FROM snowyowl.sponsor_player WHERE busy_flag = 0")
    List<Long> getAvailableSponsorIds();

    @Update("UPDATE snowyowl.sponsor_player SET busy_flag = 1, game_id = #{1}, sponsor_cash_pool_id = #{0}, last_used_date = NOW() WHERE subscriber_id = #{2}")
    void addSponsorToGame(int sponsorCashPoolId, String gameId, long sponsorSubscriberId);

    @Select("SELECT * from snowyowl.sponsor_player WHERE game_id = #{0} AND busy_flag = 1")
    List<Sponsor> getSponsorsForGame(String gameId);

    @Select("SELECT subscriber_id from snowyowl.sponsor_player WHERE game_id = #{0} AND busy_flag = 1")
    List<Long> getSponsorIdsGame(String gameId);

    @Select("SELECT * from snowyowl.sponsor_player WHERE game_id = #{0} AND busy_flag = 1 LIMIT 1")
    Sponsor getSingleSponsorForGame(String gameId);

    @Select("SELECT * from snowyowl.sponsor_player WHERE subscriber_id = #{0}")
    Sponsor getSponsorById(long subscriberId);

    @Update("UPDATE snowyowl.sponsor_player SET busy_flag = 0, game_id = null WHERE game_id = #{0}")
    void releaseSponsorPlayersForGame(String gameId);

    @Update("UPDATE snowyowl.sponsor_player SET busy_flag = 0, game_id = null WHERE subscriber_id = #{0}")
    void releaseSponsorPlayerForGame(long subscriberId);
}
