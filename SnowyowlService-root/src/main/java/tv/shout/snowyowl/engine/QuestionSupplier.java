package tv.shout.snowyowl.engine;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import tv.shout.sc.domain.Round;
import tv.shout.snowyowl.dao.IDaoMapper;
import tv.shout.snowyowl.domain.MatchQuestion;
import tv.shout.snowyowl.domain.Question;
import tv.shout.snowyowl.domain.QuestionAnswer;
import tv.shout.snowyowl.service.QuestionHelper;
import tv.shout.util.MaxSizeHashMap;

public class QuestionSupplier
implements RoundQuestionRetriever
{
    private static Logger _logger = Logger.getLogger(QuestionSupplier.class);

    private Map<Integer, List<String>> _questionQueryCriteriaHashToQuestionUuidList = new HashMap<>();
//    private Map<Integer, Integer> _queryQuestionCriteriaHashToCurrentIndex = new HashMap<>();
    private Lock _questionListLock = new ReentrantLock();

    private MaxSizeHashMap<String, String> _questionCorrectAnswerMap = new MaxSizeHashMap<String, String>().withMaxSize(1024);
    private MaxSizeHashMap<String, Long> _questionTimeoutMap = new MaxSizeHashMap<String, Long>().withMaxSize(1024);

    @Autowired
    protected IDaoMapper _dao;

    public void notifyQuetionListChanged()
    {
        _questionListLock.lock();
        try {
//            _queryQuestionCriteriaHashToCurrentIndex.clear();
            _questionQueryCriteriaHashToQuestionUuidList.clear();
        } finally {
            _questionListLock.unlock();
        }
    }

    public Map<String, String> getQuestionCorrectAnswerMap()
    {
        return _questionCorrectAnswerMap;
    }

    public Map<String, Long> getQuestionTimeoutMap()
    {
        return _questionTimeoutMap;
    }

    public Question getQuestion(Set<String> roundCategoryUuids, Set<String> allowedLanguageCodes, int minDifficulty, int maxDifficulty, String gameId, List<Long> subscriberIds)
    throws NoQuestionFoundException
    {
        if (_questionListLock == null) {
            _questionQueryCriteriaHashToQuestionUuidList = new HashMap<>();
//            _queryQuestionCriteriaHashToCurrentIndex = new HashMap<>();
            _questionListLock = new ReentrantLock();
        }
        //FUTURE: take usage count into account when selecting the question
        //FUTURE: scaleability issue if there are tens of thousands or hundreds of thousands or more questions - holding their id's in memory for each hash combo

        String languageCodesAsCommaDelimitedList = allowedLanguageCodes.stream().collect(Collectors.joining(","));

        int hash;
        List<String> questionUuids;
//        int curIndex;
        String categoryUuidsAsCommaDelimiatedList="";

        if (roundCategoryUuids.contains("*")) {
            hash = ("" + minDifficulty + maxDifficulty + languageCodesAsCommaDelimitedList + roundCategoryUuids).hashCode();
        } else {
            categoryUuidsAsCommaDelimiatedList = roundCategoryUuids.stream().collect(Collectors.joining(","));
            hash = ("" + minDifficulty + maxDifficulty + languageCodesAsCommaDelimitedList + categoryUuidsAsCommaDelimiatedList).hashCode();
        }

        _questionListLock.lock();
        try {
            questionUuids = _questionQueryCriteriaHashToQuestionUuidList.get(hash);
            if (questionUuids == null) {
                questionUuids = getQuestionIdsBasedOnCriteria(_dao, minDifficulty, maxDifficulty, languageCodesAsCommaDelimitedList, roundCategoryUuids);
                Collections.shuffle(questionUuids, new Random(UUID.randomUUID().getMostSignificantBits())); //randomize the list
//
//                curIndex = 0;
//                _queryQuestionCriteriaHashToCurrentIndex.put(hash, curIndex);
//                _questionQueryCriteriaHashToQuestionUuidList.put(hash, questionUuids);
//
//            } else {
//                curIndex = _queryQuestionCriteriaHashToCurrentIndex.get(hash);
            }

            if (questionUuids.size() == 0) {
                StringBuilder buf = new StringBuilder();
                buf.append("there are no questions that match the given criteria!");
                buf.append("\nminDifficulty: ").append(minDifficulty);
                buf.append("\nmaxDifficulty: ").append(maxDifficulty);
                buf.append("\nlanguageCodes: ").append(allowedLanguageCodes);
                buf.append("\ncategories: ").append(roundCategoryUuids);
                _logger.error(buf.toString());
                throw new NoQuestionFoundException("there are no questions that match the given criteria!");
            }

            //grab all the questions that these subscribers have currently been asked
            String subscriberIdsAsCommaDelimitedList = subscriberIds.stream().map(id -> id.toString()).collect(Collectors.joining(","));
            Set<String> questionIdsAlreadyAsked = new HashSet<>(_dao.getCombinedSubscriberQuestions(gameId, subscriberIdsAsCommaDelimitedList));

            //grab an unused questionUuid (if any)
            String questionUuid = null;
            for (String qId : questionUuids) {
                if (!questionIdsAlreadyAsked.contains(qId)) {
                    questionUuid = qId;
                    break;
                }
            }

            if (questionUuid != null) {
                //save this as an asked question for each subscriber
                for (long sId : subscriberIds) {
                    _dao.addSubscriberQuestion(gameId, sId, questionUuid);
                }

            } else {
                //there's no help for it - there are no questions that haven't been seen by at least one subscriber
                //randomly pick one
                questionUuid = questionUuids.get(new Random().nextInt(questionUuids.size()));
            }

//            //grab the question and increment the current index
//            String questionUuid = questionUuids.get(curIndex);
//            curIndex++;
//            _queryQuestionCriteriaHashToCurrentIndex.put(hash, curIndex);

            //get the question object
            Question q = QuestionHelper.getQuestion(questionUuid, _dao);

            //save the correct answer for later
            QuestionAnswer correctQuestionAnswer = _dao.getQuestionAnswersForQuestion(q.getId()).stream()
                .filter(a -> a.getCorrect())
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("attempting to use question that does not have a correct answer!"));
            String correctAnswerId = correctQuestionAnswer.getId();
            _questionCorrectAnswerMap.put(q.getId(), correctAnswerId);

            //increment the usage count
            _dao.incrementQuestionUsageCount(questionUuid);

//            //if the index has wrapped around - re-randomize and start again
//            if (curIndex == questionUuids.size()) {
//                curIndex = 0;
//                Collections.shuffle(questionUuids, new Random(UUID.randomUUID().getMostSignificantBits())); //randomize the list
//                _questionQueryCriteriaHashToQuestionUuidList.put(hash, questionUuids);
//                _queryQuestionCriteriaHashToCurrentIndex.put(hash, curIndex);
//            }

            return q;

        } finally {
            _questionListLock.unlock();
        }
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

}
