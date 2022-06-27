package tv.shout.snowyowl.engine.fixedroundmultilife;

import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import tv.shout.sc.domain.Game;
import tv.shout.sc.domain.Match;
import tv.shout.sc.domain.Match.MATCH_STATUS;
import tv.shout.sc.domain.Round;
import tv.shout.snowyowl.domain.MatchQuestion;
import tv.shout.snowyowl.domain.SubscriberQuestionAnswer;
import tv.shout.snowyowl.domain.SubscriberStats;
import tv.shout.snowyowl.engine.MMEFixedRoundCommon;
import tv.shout.snowyowl.engine.RMEFixedRoundCommon;
import tv.shout.snowyowl.service.ISnowyowlService;

public class MMEFixedRoundMultiLife
extends MMEFixedRoundCommon
{
    @Resource(name="rmeFixedRoundMultiLife")
    private RMEFixedRoundCommon _roundManagementEngine;

    @PostConstruct
    public void onPostConstruct()
    {
        _mmeCommon.setPrefix("FixedRoundMultiLifeMME");
    }

    @Override
    public String getType()
    {
        return ISnowyowlService.GAME_ENGINE_TYPE_FixedRound_MultiLife;
    }

    @Override
    public MATCH_STATUS[] getSupportedMatchStatuses()
    {
        return new MATCH_STATUS[] {Match.MATCH_STATUS.NEW, Match.MATCH_STATUS.OPEN, Match.MATCH_STATUS.WAITING_FOR_NEXT_QUESTION};
    }

    @Override
    public RMEFixedRoundCommon getRME()
    {
        return _roundManagementEngine;
    }

    @Override
    public void scoreQuestion(Game game, Round round, Match match, MatchQuestion matchQuestion, List<SubscriberQuestionAnswer> sqas, boolean isQuestionTimedOut, List<Long> botsInGame)
    {
        final String correctAnswerId = getCorretAnswerId(matchQuestion.getQuestionId());

        //if the question timed out, find anyone who didn't answer - we know their determination (LOST_TIMEOUT)
        if (isQuestionTimedOut) {
            sqas.stream()
                .filter(sqa -> sqa.getSelectedAnswerId() == null)
                .forEach(sqa -> {
                    sqa.setDetermination(SubscriberQuestionAnswer.ANSWER_DETERMINATION.LOST_TIMEOUT);

                    if (game.isProductionGame() && !botsInGame.contains(sqa.getSubscriberId())) {
                        _subscriberStatsHandler.incrementSubscriberStat(sqa.getSubscriberId(), SubscriberStats.STATS_TYPE.CUMULATIVE_QUESTION_SCORE, _cumulativeScore_LOST_TIMEOUT);
                    }
                });
        }

        //get a list of players who answered correctly (sorted by answer time, fastest answer first)
        List<SubscriberQuestionAnswer> correctSqas = sqas.stream()
                .filter(a -> correctAnswerId.equals(a.getSelectedAnswerId()))
                .sorted((a1, a2) -> a1.getDurationMilliseconds().compareTo(a2.getDurationMilliseconds()))
                .collect(Collectors.toList());

        //get a list of players who answered incorrectly (but that didn't timeout)
        List<SubscriberQuestionAnswer> incorrectSqas = sqas.stream()
                .filter(a -> ! correctAnswerId.equals(a.getSelectedAnswerId()))
                .filter(a -> a.getDetermination() != SubscriberQuestionAnswer.ANSWER_DETERMINATION.LOST_TIMEOUT)
                .collect(Collectors.toList());

        //TODO: every question is equivalent as being a tie breaker question. someone MUST win.
        // the only difference is that whoever loses the question also loses a life
        // basically copy the code (or make it a super method) for "tie braeker question"

    }

    @Override
    public void processMatch(Round round, Match match, List<MatchQuestion> matchQuestions, Long twitchSubscriberId)
    {
        //TODO
        // and then processMatch simply becomes a matter of: are there any lives left? if so, ask another question
    }

}
