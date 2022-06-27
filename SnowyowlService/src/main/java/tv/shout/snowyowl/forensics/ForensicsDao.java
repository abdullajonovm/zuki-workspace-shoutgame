package tv.shout.snowyowl.forensics;

import java.util.List;

import org.apache.ibatis.annotations.Many;
import org.apache.ibatis.annotations.One;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

public interface ForensicsDao
{
    @Select(
        "SELECT g.id, ml.`value` as name, g.game_status, g.bracket_elimination_count, " +
        "       g.pending_date, g.open_date, g.closed_date " +
        "  FROM contest.game g, contest.multi_localization ml " +
        " WHERE g.id = #{0} " +
        "   AND g.id = ml.uuid " +
        "   AND ml.`type` = 'gameName' " +
        "   AND ml.language_code = 'en'"
    )
    @Results(value = {
        @Result(property="id", column="id", id=true),
        @Result(property="gamePlayers", javaType=List.class, column="id", many=@Many(select="getGamePlayers")),
        @Result(property="rounds", javaType=List.class, column="id", many=@Many(select="getRounds"))
    })
    FGame getGame(String gameId);

    @Select(
        "SELECT gp.id, gp.rank, gp.determination, gp.create_date, gp.subscriber_id, s.nickname " +
        "  FROM contest.game_player gp, gameplay.s_subscriber s " +
        " WHERE gp.game_id = #{0} " +
        "   AND gp.subscriber_id = s.subscriber_id"
    )
    @Results(value = {
        @Result(property="id", column="id", id=true)
    })
    List<FGamePlayer> getGamePlayers(String gameId);

    @Select(
        "SELECT r.id, ml.`value` as name, r.round_type, r.round_status, r.round_sequence, r.final_round, " +
        "       r.current_player_count, r.maximum_player_count, r.minimum_match_count, r.maximum_match_count, " +
        "       r.minimum_activity_to_win_count, r.maximum_activity_count, r.pending_date, r.open_date " +
        "  FROM contest.round r, contest.multi_localization ml " +
        " WHERE r.game_id = #{0} " +
        "   AND r.id = ml.uuid " +
        "   AND ml.`type` = 'roundName' " +
        "   AND ml.language_code = 'en' " +
        " ORDER BY r.round_sequence"
    )
    @Results(value = {
        @Result(property="id", column="id", id=true),
        @Result(property="roundPlayers", javaType=List.class, column="id", many=@Many(select="getRoundPlayers")),
        @Result(property="matches", javaType=List.class, column="id", many=@Many(select="getMatches"))
    })
    List<FRound> getRounds(String gameId);

    @Select(
        "SELECT rp.id, rp.played_match_count, rp.determination, rp.skill_answer_correct_pct, " +
        "       rp.skill_average_answer_ms, rp.rank, rp.skill, rp.create_date, rp.subscriber_id, s.nickname " +
        "  FROM contest.round_player rp, gameplay.s_subscriber s " +
        " WHERE rp.round_id = #{0} " +
        "   AND rp.subscriber_id = s.subscriber_id"
    )
    @Results(value = {
        @Result(property="id", column="id", id=true)
    })
    List<FRoundPlayer> getRoundPlayers(String roundId);

    @Select(
        "SELECT id, match_status, won_subscriber_id, minimum_activity_to_win_count, maximum_activity_count, actual_activity_count, determination, " +
        "       create_date, start_date, complete_date " +
        "  FROM contest.match " +
        " WHERE round_id = #{0}"
    )
    @Results(value = {
        @Result(property="id", column="id", id=true),
        @Result(property="matchPlayers", javaType=List.class, column="id", many=@Many(select="getMatchPlayers")),
        @Result(property="matchQuestions", javaType=List.class, column="id", many=@Many(select="getMatchQuestions"))
    })
    List<FMatch> getMatches(String roundId);

    @Select(
        "SELECT mp.id, mp.determination, mp.score, mp.create_date, mp.subscriber_id, s.nickname " +
        "  FROM contest.match_player mp, gameplay.s_subscriber s " +
        " WHERE mp.match_id = #{0} " +
        "   AND mp.subscriber_id = s.subscriber_id"
    )
    @Results(value = {
        @Result(property="id", column="id", id=true)
    })
    List<FMatchPlayer> getMatchPlayers(String matchid);

    @Select(
        "SELECT mq.id, mq.question_id, mq.match_question_status, mq.won_subscriber_id, mq.determination, mq.create_date, mq.completed_date " +
        "  FROM snowyowl.match_question mq " +
        " WHERE mq.match_id = #{0} " +
        " ORDER BY mq.create_date"
    )
    @Results(value = {
        @Result(property="id", column="id", id=true),
        @Result(property="question", javaType=FQuestion.class, column="question_id", one=@One(select="getQuestion")),
        @Result(property="sqas", javaType=List.class, column="id", many=@Many(select="getSubscriberQuestionAnswers"))
    })
    List<FMatchQuestion> getMatchQuestions(String matchId);

    @Select(
        "SELECT q.id, ml.`value` as question_text, q.create_date " +
        "  FROM snowyowl.question q, snowyowl.multi_localization ml " +
        " WHERE q.id = #{0} " +
        "   AND q.id = ml.uuid " +
        "   AND ml.`type` = 'questionText' " +
        "   AND ml.language_code = 'en'"
    )
    @Results(value = {
        @Result(property="id", column="id", id=true),
        @Result(property="answers", javaType=List.class, column="id", many=@Many(select="getQuestionAnswers"))
    })
    FQuestion getQuestion(String questionId);

    @Select(
        "SELECT qa.id, ml.`value` as answer_text, qa.correct, qa.create_date " +
        "  FROM snowyowl.question_answer qa, snowyowl.multi_localization ml " +
        " WHERE qa.question_id = #{0} " +
        "   AND qa.id = ml.uuid " +
        "   AND ml.`type` = 'answerText' " +
        "   AND ml.language_code = 'en'"
    )
    @Results(value = {
        @Result(property="id", column="id", id=true)
    })
    List<FQuestionAnswer> getQuestionAnswers(String questionId);

    @Select(
        "SELECT sqa.id, sqa.selected_answer_id, sqa.question_presented_timestamp, sqa.duration_milliseconds, sqa.determination, sqa.won, sqa.create_date, sqa.subscriber_id, s.nickname " +
        "  FROM snowyowl.subscriber_question_answer sqa, gameplay.s_subscriber s " +
        " WHERE sqa.match_question_id = #{0} " +
        "   AND sqa.subscriber_id = s.subscriber_id"
    )
    @Results(value = {
        @Result(property="id", column="id", id=true)
    })
    List<FSubscriberQuestionAnswer> getSubscriberQuestionAnswers(String matchQuestionId);
}
