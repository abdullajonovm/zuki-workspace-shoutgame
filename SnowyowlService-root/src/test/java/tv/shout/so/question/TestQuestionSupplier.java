package tv.shout.so.question;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import tv.shout.sc.domain.MatchPlayer;
import tv.shout.sc.domain.Round;
import tv.shout.sc.service.IShoutContestService;
import tv.shout.snowyowl.dao.IDaoMapper;
import tv.shout.snowyowl.domain.MatchQuestion;
import tv.shout.snowyowl.domain.Question;
import tv.shout.snowyowl.domain.QuestionAnswer;
import tv.shout.snowyowl.domain.SubscriberRoundQuestion;
import tv.shout.snowyowl.engine.QuestionSupplierRandomSupplier;
import tv.shout.snowyowl.engine.RoundQuestionRetriever;
import tv.shout.snowyowl.service.QuestionHelper;
import tv.shout.util.MaxSizeHashMap;

/*
CREATE TABLE `snowyowl`.`game_round_questions` (
  `game_id` CHAR(36) NOT NULL,
  `round_id` CHAR(36) NOT NULL,
  `question_id` CHAR(36) NOT NULL,
  `order` INT NOT NULL,
  PRIMARY KEY (`game_id`, `round_id`, `question_id`));

CREATE TABLE `snowyowl`.`subscriber_round_questions` (
  `subscriber_id` INT UNSIGNED NOT NULL,
  `round_id` CHAR(36) NOT NULL,
  `question_id` CHAR(36) NOT NULL,
  `order` INT NOT NULL,
  `seen` INT NOT NULL DEFAULT 0,
  PRIMARY KEY (`subscriber_id`, `round_id`, `question_id`));

 */
public class TestQuestionSupplier
implements RoundQuestionRetriever
{
    private IDaoMapper _dao;
    private QuestionSupplierRandomSupplier _randomSupplier;
    private IShoutContestService _shoutContestService;
    private MaxSizeHashMap<String, String> _questionCorrectAnswerMap = new MaxSizeHashMap<String, String>().withMaxSize(1024);
    private MaxSizeHashMap<String, Long> _questionTimeoutMap = new MaxSizeHashMap<String, Long>().withMaxSize(1024);

    public TestQuestionSupplier(IDaoMapper dao, IShoutContestService shoutContestService, QuestionSupplierRandomSupplier randomSupplier)
    {
        _dao = dao;
        _shoutContestService = shoutContestService;
        _randomSupplier = randomSupplier;
    }

    public Map<String, String> getQuestionCorrectAnswerMap()
    {
        return _questionCorrectAnswerMap;
    }

    public Map<String, Long> getQuestionTimeoutMap()
    {
        return _questionTimeoutMap;
    }

    public boolean hasQuestionTimedOut(Round round, MatchQuestion matchQuestion)
    {
        long elapsedTimeSinceQuestionWasAsked = System.currentTimeMillis() - matchQuestion.getCreateDate().getTime();

        //check cache. only compute if necessary
        Long questionMaxTimeoutTime = _questionTimeoutMap.get(matchQuestion.getId());
        if (questionMaxTimeoutTime == null) {
            long maxTimeAnyoneCanWaitToRequestQuestion = round.getActivityMaximumDurationSeconds() * 1_000L;
            long maxTimeAnyoneCanTakeToAnswerQuestion = round.getPlayerMaximumDurationSeconds() * 1_000L;
            questionMaxTimeoutTime = maxTimeAnyoneCanWaitToRequestQuestion + maxTimeAnyoneCanTakeToAnswerQuestion;

            _questionTimeoutMap.put(matchQuestion.getId(), questionMaxTimeoutTime);
        }

//_logger.info(MessageFormat.format("hasQuestionTimedOut: elapsed time (ms): {0}, max time (ms): {1}", elapsedTimeSinceQuestionWasAsked, questionMaxTimeoutTime));
        return elapsedTimeSinceQuestionWasAsked >= questionMaxTimeoutTime;
    }

    public void notifyQuetionListChanged()
    {
        //no-op in this implementation
    }

//TODO: don't create a new grid for each bracket play. only 1 bracket play grid must exist
    //public Question getQuestion(Set<String> roundCategoryUuids, Set<String> allowedLanguageCodes, int minDifficulty, int maxDifficulty)
    public Question getQuestion(List<MatchPlayer> players, Set<Long> botIds, Set<String> allowedLanguageCodes, int minDifficulty, int maxDifficulty)
    {
        String questionId;
        String gameId = players.get(0).getGameId();
        String roundId = players.get(0).getRoundId();
        long player1SubscriberId = players.get(0).getSubscriberId();
        long player2SubscriberId = players.get(1).getSubscriberId();

        List<Round> rounds = _shoutContestService.getRoundsForGame(gameId);
        List<String> gameRoundQuestionIds = _dao.getGameRoundQuestionIds(gameId, roundId);

        //check to see if the usage grid has been created for this round
        // if not, then for each pool play round, and for the first bracket round:
        if (gameRoundQuestionIds == null || gameRoundQuestionIds.size() == 0) {
            //Game game = _shoutContestService.getGame(round.getGameId());
            for (Round r : rounds) {
                //grab all the possible questions for the round
                String languageCodesAsCommaDelimitedList = allowedLanguageCodes.stream().collect(Collectors.joining(","));
                List<String> questionUuids = getQuestionIdsBasedOnCriteria(_dao, minDifficulty, maxDifficulty, languageCodesAsCommaDelimitedList, r.getCategories());
                Collections.shuffle(questionUuids, new Random(_randomSupplier.getSeedForRandomGameQuestionShuffle())); //randomize the list

                //create the "grid"
                for (int i=0; i<questionUuids.size(); i++) {
                    _dao.addGameRoundQuestion(gameId, r.getId(), questionUuids.get(i), i);
                }

                if (r.getRoundType() == Round.ROUND_TYPE.BRACKET) break;
            }

            //now that the usage grid exists for this round, retrieve it
            gameRoundQuestionIds = _dao.getGameRoundQuestionIds(gameId, roundId);
        }

        //if both players are bots, it doesn't matter what question - just use the first one and be done with it
        if (botIds.contains(player1SubscriberId) && botIds.contains(player2SubscriberId)) {
            questionId = gameRoundQuestionIds.get(0);
            return fattenQuestion(questionId);
        }

        //if player 1 is a bot, swap players 1 and 2 so that player 1 is always a real person (because player2 won't be a bot or else the logic wouldn't have got here
        boolean player2IsBot;
        if (botIds.contains(player1SubscriberId)) {
            long swap = player2SubscriberId;
            player2SubscriberId = player1SubscriberId;
            player1SubscriberId = swap;
            player2IsBot = true;

        } else {
            player2IsBot = botIds.contains(player2SubscriberId);
        }

        //for the first player, see if there is a row for this round.
        List<SubscriberRoundQuestion> player1SubscriberRoundQuetsions = _dao.getSubscriberRoundQuestions(player1SubscriberId, roundId);

        //if not, then create a row for each of the rounds for that player
        if (player1SubscriberRoundQuetsions == null || player1SubscriberRoundQuetsions.size() == 0) {
            //for EACH of the rounds, add the questions for that player
            for (Round r : rounds) {
                List<String> qIds = _dao.getGameRoundQuestionIds(gameId, r.getId());
                for (int i=0; i<qIds.size(); i++) {
                    _dao.addSubscriberRoundQuestion(player1SubscriberId, r.getId(), qIds.get(i), i);
                }
            }

            player1SubscriberRoundQuetsions = _dao.getSubscriberRoundQuestions(player1SubscriberId, roundId);
        }

        //now that the rows exist, retrieve them for each round and store in a map
        Map<String, List<SubscriberRoundQuestion>> subscriber1QuestionRowsByRoundIdMap = new HashMap<>();
        for (Round r : rounds) {
            List<SubscriberRoundQuestion> qIds = _dao.getSubscriberRoundQuestions(player1SubscriberId, r.getId());
            subscriber1QuestionRowsByRoundIdMap.put(r.getId(), qIds);
        }

        //for the second player, see if there is a row for this round.
        //unless player 2 is abot
        List<SubscriberRoundQuestion> player2SubscriberRoundQuetsions = null;
        Map<String, List<SubscriberRoundQuestion>> subscriber2QuestionRowsByRoundIdMap = new HashMap<>();
        if (!player2IsBot) {
            player2SubscriberRoundQuetsions = _dao.getSubscriberRoundQuestions(player2SubscriberId, roundId);

            //if not, then create a row for each of the rounds for that player
            if (player2SubscriberRoundQuetsions == null || player2SubscriberRoundQuetsions.size() == 0) {
                //for EACH of the rounds, add the questions for that player
                for (Round r : rounds) {
                    List<String> qIds = _dao.getGameRoundQuestionIds(gameId, r.getId());
                    for (int i=0; i<qIds.size(); i++) {
                        _dao.addSubscriberRoundQuestion(player2SubscriberId, r.getId(), qIds.get(i), i);
                    }

                }

                player2SubscriberRoundQuetsions = _dao.getSubscriberRoundQuestions(player2SubscriberId, roundId);
            }

            //now that the rows exist, retrieve them for each round and store in a map
            for (Round r : rounds) {
                List<SubscriberRoundQuestion> qIds = _dao.getSubscriberRoundQuestions(player2SubscriberId, r.getId());
                subscriber2QuestionRowsByRoundIdMap.put(r.getId(), qIds);
            }
        }

        //check the player1.row - have all questions been seen? if so, reset the row
        boolean allSeen = true;
        for (SubscriberRoundQuestion srq : player1SubscriberRoundQuetsions) {
            if (!srq.isSeen()) {
                allSeen = false;
                break;
            }
        }
        if (allSeen) {
            for (SubscriberRoundQuestion srq : player1SubscriberRoundQuetsions) {
                srq.setSeen(false);
                _dao.updateSubscriberRoundQuestion(srq);
            }
        }

        //check the player2.row - have all questions been seen? if so, reset the row
        //unless player 2 is a bot
        if (!player2IsBot) {
            allSeen = true;
            for (SubscriberRoundQuestion srq : player2SubscriberRoundQuetsions) {
                if (!srq.isSeen()) {
                    allSeen = false;
                    break;
                }
            }
            if (allSeen) {
                for (SubscriberRoundQuestion srq : player2SubscriberRoundQuetsions) {
                    srq.setSeen(false);
                    _dao.updateSubscriberRoundQuestion(srq);
                }
            }
        }

        //pick a random number from [0,player1.row.questions.size) and store as "initialIndexPosition"
        int initialIndexPosition = _randomSupplier.getRandomIntForSubscriberQuestionShuffle(player1SubscriberRoundQuetsions.size());
        boolean looped = false;
        int index = initialIndexPosition;

        while (true) {
            //if loop has wrapped around and come back to initial index (index == initialIndexPosition && looped==true), it means no question is available that at least one hasn't seen
            if (looped && index == initialIndexPosition) {
                //just use the question at the initial index position (which is also the index position currently)
                break;

            } else {
                //who has seen this question?
                boolean hasPlayer1SeenThisQuestion = player1SubscriberRoundQuetsions.get(index).isSeen();

                //if player 2 is a bot, this is always false
                boolean hasPlayer2SeenThisQuestion = player2IsBot ? false : player2SubscriberRoundQuetsions.get(index).isSeen();

                if (!hasPlayer1SeenThisQuestion && !hasPlayer2SeenThisQuestion) {
                    //neither have seen it. use it
                    break;

                } else {
                    //one or both have already seen it. move on to the next
                    index++;

                    //determine if a wrap-around has occurred
                    if (index >= player1SubscriberRoundQuetsions.size()) {
                        index = 0;
                        looped = true;
                    }
                }
            }
        }

        //this is the questionId to use
        questionId = player1SubscriberRoundQuetsions.get(index).getQuestionId();

        //grab player1.row for each round and mark this question as having been seen (if the question exists in the round being examined)
//TODO: make this a map for quicker lookup
        for (String rId : subscriber1QuestionRowsByRoundIdMap.keySet()) {
            List<SubscriberRoundQuestion> row = subscriber1QuestionRowsByRoundIdMap.get(rId);
            for (SubscriberRoundQuestion srq : row) {
                if (srq.getQuestionId().equals(questionId)) {
                    if (!srq.isSeen()) {
                        srq.setSeen(true);
                        _dao.updateSubscriberRoundQuestion(srq);
                    }
                    break;
                }
            }
        }

        //grab player2.row for each round and mark this question as having been seen (if the question exists in the round being examined)
        //unless player 2 is a bot
//TODO: make this a map for quicker lookup
        if (!player2IsBot) {
            for (String rId : subscriber2QuestionRowsByRoundIdMap.keySet()) {
                List<SubscriberRoundQuestion> row = subscriber2QuestionRowsByRoundIdMap.get(rId);
                for (SubscriberRoundQuestion srq : row) {
                    if (srq.getQuestionId().equals(questionId)) {
                        if (!srq.isSeen()) {
                            srq.setSeen(true);
                            _dao.updateSubscriberRoundQuestion(srq);
                        }
                        break;
                    }
                }
            }
        }

        return fattenQuestion(questionId);
    }

    private Question fattenQuestion(String questionId)
    {
        //the remainder of the method remains the same as before - grab the question, encrypt it, etc. etc.
        //get the question object
        Question q = QuestionHelper.getQuestion(questionId, _dao);

        //save the correct answer for later
        QuestionAnswer correctQuestionAnswer = _dao.getQuestionAnswersForQuestion(q.getId()).stream()
            .filter(a -> a.getCorrect())
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("attempting to use question that does not have a correct answer!"));
        String correctAnswerId = correctQuestionAnswer.getId();
        _questionCorrectAnswerMap.put(q.getId(), correctAnswerId);

        //increment the usage count
        _dao.incrementQuestionUsageCount(questionId);

        return q;
    }
}
