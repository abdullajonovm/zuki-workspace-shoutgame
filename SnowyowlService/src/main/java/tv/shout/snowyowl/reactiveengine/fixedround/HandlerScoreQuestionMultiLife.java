package tv.shout.snowyowl.reactiveengine.fixedround;

import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.log4j.Logger;

import com.meinc.identity.domain.Subscriber;

import tv.shout.sc.domain.Game;
import tv.shout.sc.domain.GamePlayer;
import tv.shout.sc.domain.Match;
import tv.shout.sc.domain.MatchPlayer;
import tv.shout.sc.domain.Round;
import tv.shout.simplemessagebus.Message;
import tv.shout.snowyowl.domain.MatchQuestion;
import tv.shout.snowyowl.domain.SubscriberQuestionAnswer;
import tv.shout.snowyowl.service.ISnowyowlService;
import tv.shout.util.FastMap;

public class HandlerScoreQuestionMultiLife
extends BaseHandlerScoreQuestion
{
    private static Logger _logger = Logger.getLogger(HandlerScoreQuestionMultiLife.class);

    @Override
    public void handleMessage(Message message)
    {
        switch (message.type)
        {
            case SCORE_QUESTION:
                @SuppressWarnings("unchecked") String engineType = (String) ((Map<String, Object>)message.payload).get("gameEngineType");
                if (!engineType.equals(ISnowyowlService.GAME_ENGINE_TYPE_FixedRound_MultiLife)) return;

if (_logger.isDebugEnabled()) _logger.debug(MessageFormat.format("received message: {0}", message.type));
                handleScoreQuestion(message);
                break;

            //else ignore
        }
    }

    @SuppressWarnings("unchecked")
    private void handleScoreQuestion(Message message)
    {
        Map<String, Object> data = getDataFromMessagePayload(message);
        List<MatchPlayer> matchPlayers = (List<MatchPlayer>) data.get("matchPlayers");
        List<SubscriberQuestionAnswer> sqas = (List<SubscriberQuestionAnswer>) data.get("sqas");
        Game game = (Game) data.get("game");
        Round round = (Round) data.get("round");
        Match match = (Match) data.get("match");
        String correctAnswerId = (String) data.get("correctAnswerId");
        List<Long> botsInGame = (List<Long>) data.get("botsInGame");
        List<Long> sponsorsInGame = (List<Long>) data.get("sponsorsInGame");
        List<MatchQuestion> matchQuestions = (List<MatchQuestion>) data.get("matchQuestions");
        Subscriber s1 = (Subscriber) data.get("s1");
        Subscriber s2 = (Subscriber) data.get("s2");

        Map<Long, Subscriber> subscribers = new FastMap<>(s1.getSubscriberId(), s1, s2.getSubscriberId(), s2);

        MatchQuestion matchQuestion = matchQuestions.stream()
                .filter(mq -> mq.getQuestionId().equals(sqas.get(0).getQuestionId()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("unable to locate match question"));

        List<GamePlayer> gamePlayers = null;

        switch (match.getMatchStatus())
        {
            case OPEN:
            case WAITING_FOR_NEXT_QUESTION:
                gamePlayers = scoreQuestionAsRegular(game, match, matchQuestion, matchPlayers, sqas, s1, s2, correctAnswerId, botsInGame, sponsorsInGame);
                break;

            default:
                //it's possible the match was cancelled (as part of a game cancel)
                _logger.warn(MessageFormat.format(
                    "found match in unexpected state while scoring question. ignoring. matchId: {0}, state: {1}",
                    match.getId(), match.getMatchStatus()));
                return;
        }

        processMatch(game, round, match, gamePlayers, matchPlayers, matchQuestions, getTwitchSubscriberId(game.getId()), botsInGame, sponsorsInGame, subscribers);
    }

    private List<GamePlayer> scoreQuestionAsRegular(
        Game game, Match match, MatchQuestion matchQuestion, List<MatchPlayer> matchPlayers, List<SubscriberQuestionAnswer> sqas,
        Subscriber s1, Subscriber s2, String correctAnswerId, List<Long> botsInGame, List<Long> sponsorsInGame)
    {
        scoreQuestionAsRegularAndSetDeterminationAndSendSubscriberStatsAndCloseMatch(game, matchQuestion, sqas, correctAnswerId, botsInGame, sponsorsInGame);

        List<GamePlayer> gamePlayers = updateSqasAndUpdateMatchAndGetGamePlayers(game, matchQuestion, sqas);

        decrementLives(sqas, gamePlayers);

        sendQuestionResult(game, matchQuestion, sqas, s1, s2, correctAnswerId, gamePlayers, botsInGame, sponsorsInGame);

        return gamePlayers;
    }

    @SuppressWarnings("incomplete-switch")
    private void decrementLives(List<SubscriberQuestionAnswer> sqas, List<GamePlayer> gamePlayers)
    {
        for (SubscriberQuestionAnswer sqa : sqas) {
            switch (sqa.getDetermination())
            {
                case LOST_ALL_TIMEOUT:
                case LOST_INCORRECT:
                case LOST_TIME:
                case LOST_TIMEOUT:
                    //they lost; decrement number of lives and update on the server

                    GamePlayer gamePlayer = gamePlayers.stream()
                    .filter(gp -> gp.getSubscriberId() == sqa.getSubscriberId())
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("no gamePlayer found for matching sqa"));

                    gamePlayer.setCountdownToElimination(gamePlayer.getCountdownToElimination() - 1);
                    _shoutContestService.updateGamePlayer(gamePlayer);
                    break;
            }
        }
    }

    //once a question has been scored, this will determine if more questions need to be asked, or if the match is over, and take appropriate action
    private void processMatch(
        Game game, Round round, Match match, List<GamePlayer> gamePlayers, List<MatchPlayer> matchPlayers, List<MatchQuestion> matchQuestions, Long twitchSubscriberId,
        List<Long> botsInGame, List<Long> sponsorsInGame, Map<Long, Subscriber> subscribers)
    {
        long s1Id = gamePlayers.get(0).getSubscriberId();
        long s2Id = gamePlayers.get(1).getSubscriberId();

        Long winnerSubscriberId;

        int s1LivesLeft = gamePlayers.get(0).getCountdownToElimination();
        int s2LivesLeft = gamePlayers.get(1).getCountdownToElimination();

        if (s1LivesLeft > 0 && s2LivesLeft > 0) {
            //both subscribers have lives left, ask another question
            sendNextQuestion(game, round, match, matchPlayers, botsInGame, sponsorsInGame, subscribers);
            return;

        } else if (s1LivesLeft > 0 && s2LivesLeft <= 0) {
            //only one subscriber has a life left, the match is over and the subscriber with the remaining life moves on
            winnerSubscriberId = s1Id;

if (_logger.isDebugEnabled()) {
    _logger.debug(MessageFormat.format("THERE IS A WINNER; MATCH OVER. winnerSubscriberId: {0,number,#}", winnerSubscriberId));
}

            finishProcessMatch(game, round, match, matchPlayers, matchQuestions, winnerSubscriberId, twitchSubscriberId, botsInGame, sponsorsInGame);
            return;

        } else if (s1LivesLeft <= 0 && s2LivesLeft > 0) {
            //only one subscriber has a life left, the match is over and the subscriber with the remaining life moves on
            winnerSubscriberId = s2Id;

if (_logger.isDebugEnabled()) {
    _logger.debug(MessageFormat.format("THERE IS A WINNER; MATCH OVER. winnerSubscriberId: {0,number,#}", winnerSubscriberId));
}

            finishProcessMatch(game, round, match, matchPlayers, matchQuestions, winnerSubscriberId, twitchSubscriberId, botsInGame, sponsorsInGame);
            return;

        } else {
            //neither have a life left, randomly pick a winner, and the match is over and the chosen winner moves on
            int idx = new Random().nextInt(2); //will pick either 0 or 1
            winnerSubscriberId = gamePlayers.get(idx).getSubscriberId();

            //give this player back a life
            GamePlayer gamePlayer = gamePlayers.stream()
                .filter(gp -> gp.getSubscriberId() == winnerSubscriberId)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("no gamePlayer found for matching subscriberId"));
            gamePlayer.setCountdownToElimination(1);
            _shoutContestService.updateGamePlayer(gamePlayer);

if (_logger.isDebugEnabled()) {
    _logger.debug(MessageFormat.format("THERE IS A WINNER; MATCH OVER. winnerSubscriberId: {0,number,#}", winnerSubscriberId));
}

            finishProcessMatch(game, round, match, matchPlayers, matchQuestions, winnerSubscriberId, twitchSubscriberId, botsInGame, sponsorsInGame);
            return;
        }
    }

}
