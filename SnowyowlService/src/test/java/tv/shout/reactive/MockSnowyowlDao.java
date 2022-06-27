package tv.shout.reactive;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.meinc.gameplay.domain.Tuple;

import tv.shout.snowyowl.dao.IDaoMapper;
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
import tv.shout.snowyowl.domain.Question.STATUS;
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

public class MockSnowyowlDao
implements IDaoMapper
{
    private List<MockBotPlayer> _botPlayers = new ArrayList<>();
    private List<MockSponsorPlayer> _sponsorPlayers = new ArrayList<>();
    private List<Question> _questions = new ArrayList<>();
    private Map<String, Set<String>> _tieBreakers = new HashMap<>();
    private List<MatchQuestion> _matchQuestions = new ArrayList<>();
    private List<SubscriberQuestionAnswer> _sqas = new ArrayList<>();
    private Map<String, Long> _tieBreakerWinnerSubscribers = new HashMap<>();
    private List<GamePayout> _gamePayouts = new ArrayList<>();
    private List<PayoutModel> _payoutModels = new ArrayList<>();
    private List<DbPayoutTableRow> _payoutTableRows = new ArrayList<>();
    private List<MockMultiLocalizationValue> _multiLocalizationValues = new ArrayList<>();

    @Override
    public List<Long> getIdleBotIds()
    {
        return _botPlayers.stream()
                .filter(bp -> !bp.busyFlag)
                .map(bp -> bp.subscriberId)
                .collect(Collectors.toList());
    }

    @Override
    public int getBotCount()
    {
        return _botPlayers.size();
    }

    @Override
    public void markBotIdle(long subscriberId)
    {
        Optional<MockBotPlayer> bot = _botPlayers.stream()
                .filter(bp -> bp.subscriberId == subscriberId)
                .findFirst();

        if (bot.isPresent()) {
            bot.get().gameId = null;
            bot.get().busyFlag = false;
        }
    }

    @Override
    public List<Long> getBotsForGame(String gameId)
    {
        return _botPlayers.stream()
                .filter(bp -> bp.gameId != null && bp.gameId.equals(gameId))
                .map(bp -> bp.subscriberId)
                .collect(Collectors.toList());
    }

    @Override
    public void releaseBotsForGame(String gameId)
    {
        _botPlayers.stream()
            .filter(bp -> bp.gameId != null && bp.gameId.equals(gameId))
            .forEach(bp -> {
                bp.gameId = null;
                bp.busyFlag = false;
            });
    }

    void addBotsToSystem(List<Long> botSubscriberIds)
    {
        for (long subscriberId : botSubscriberIds) {
            MockBotPlayer bp = new MockBotPlayer();
            bp.subscriberId = subscriberId;
            _botPlayers.add(bp);
        }
    }

    void addBotsToGame(String gameId, List<Long> botSubscriberIds)
    {
        for (long subscriberId : botSubscriberIds) {
            MockBotPlayer bot = _botPlayers.stream()
                    .filter(bp -> bp.subscriberId == subscriberId)
                    .findFirst()
                    .orElse(null);

            if (bot != null) {
                bot.gameId = gameId;
                bot.busyFlag = true;

            } else {
                bot = new MockBotPlayer();
                bot.subscriberId = subscriberId;
                bot.gameId = gameId;
                bot.busyFlag = true;
                _botPlayers.add(bot);
            }
        }
    }

    void addSponsorPlayer(MockSponsorPlayer sponsorPlayer)
    {
        _sponsorPlayers.add(sponsorPlayer);
    }

    @Override
    public int getNumberOfAvailableSponsors()
    {
        return (int) _sponsorPlayers.stream()
                .filter(sp -> !sp.busyFlag)
                .count();
    }

    List<MockSponsorPlayer> getAllAvailableSponsorPlayers()
    {
        return _sponsorPlayers.stream()
                .filter(sp -> !sp.busyFlag)
                .collect(Collectors.toList());
    }

    void updateSponsorPlayer(MockSponsorPlayer sponsorPlayer)
    {
        Optional<MockSponsorPlayer> oSponsorPlayer = _sponsorPlayers.stream()
                .filter(sp -> sp.subscriberId == sponsorPlayer.subscriberId)
                .findFirst();

        if (oSponsorPlayer.isPresent()) {
            _sponsorPlayers.remove(oSponsorPlayer.get());
        }

        _sponsorPlayers.add(sponsorPlayer);
    }

    @Override
    public List<Long> getSponsorIdsGame(String gameId)
    {
        return _sponsorPlayers.stream()
                .filter(sp -> sp.gameId != null && sp.gameId.equals(gameId))
                .map(sp -> sp.subscriberId)
                .collect(Collectors.toList());
    }

    @Override
    public List<Sponsor> getSponsorsForGame(String gameId)
    {
        //no-op
        return new ArrayList<>();
    }

    @Override
    public void releaseSponsorPlayersForGame(String gameId)
    {
        //no-op
    }

    @Override
    public List<String> getCombinedSubscriberQuestions(String gameId, String subscriberIdsAsCommaDelimitedList)
    {
        //no-op
        return new ArrayList<>();
    }

    @Override
    public void addSubscriberQuestion(String gameId, long subscriberId, String questionId)
    {
        //no-op
    }

    @Override
    public void createQuestion(Question question)
    {
        _questions.add(question);

        //break out the question text and answer texts into the multilocalization objects
        _multiLocalizationValues.add(new MockMultiLocalizationValue(question.getId(), "questionText", "en", question.getQuestionText().get("en")));

        for (QuestionAnswer a : question.getAnswers()) {
            _multiLocalizationValues.add(new MockMultiLocalizationValue(
                a.getId(), "answerText", "en", a.getAnswerText().get("en")));
        }
    }

    @Override
    public void addQuestionLanguageCode(String questionId, String languageCode)
    {
        //no-op
    }

    @Override
    public void addQuestionForbiddenCountryCode(String questionId, String countryCode)
    {
        //no-op
    }

    @Override
    public void addQuestionCategory(String questionId, String categoryUuid)
    {
        //no-op
    }

    @Override
    public void addQuestionAnswer(QuestionAnswer answer)
    {
        //no-op
    }

    @Override
    public Question getQuestion(String questionId)
    {
        return _questions.stream()
                .filter(q -> q.getId().equals(questionId))
                .findFirst()
                .orElse(null);
    }

    @Override
    public List<QuestionAnswer> getQuestionAnswersForQuestion(String questionId)
    {
        Optional<Question> oQ = _questions.stream()
                .filter(q -> q.getId().equals(questionId))
                .findFirst();

        if (oQ.isPresent()) {
            return oQ.get().getAnswers();
        } else {
            return new ArrayList<>();
        }
    }

    @Override
    public void incrementQuestionUsageCount(String questionId)
    {
        Optional<Question> oQ = _questions.stream()
                .filter(q -> q.getId().equals(questionId))
                .findFirst();

        if (oQ.isPresent()) {
            oQ.get().setUsageCount(oQ.get().getUsageCount()+1);
        }
    }

    @Override
    public Set<String> getQuestionLanguageCodes(String questionId)
    {
        Optional<Question> oQ = _questions.stream()
                .filter(q -> q.getId().equals(questionId))
                .findFirst();

        if (oQ.isPresent()) {
            return oQ.get().getLanguageCodes();
        } else {
            return new HashSet<>();
        }
    }

    @Override
    public Set<String> getQuestionForbiddenCountryCodes(String questionId)
    {
        Optional<Question> oQ = _questions.stream()
                .filter(q -> q.getId().equals(questionId))
                .findFirst();

        if (oQ.isPresent()) {
            return oQ.get().getForbiddenCountryCodes();
        } else {
            return new HashSet<>();
        }
    }

    @Override
    public Set<String> getQuestionCategoryUuids(String questionId)
    {
        Optional<Question> oQ = _questions.stream()
                .filter(q -> q.getId().equals(questionId))
                .findFirst();

        if (oQ.isPresent()) {
            return oQ.get().getQuestionCategoryUuids();
        } else {
            return new HashSet<>();
        }
    }

    @Override
    public void insertOrReplaceMultiLocalizationValue(String uuid, String type, String languageCode, String value)
    {
        MockMultiLocalizationValue val = new MockMultiLocalizationValue(uuid, type, languageCode, value);
        _multiLocalizationValues.add(val);
    }

    @Override
    public List<Tuple<String>> getMultiLocalizationValues(String uuid, String type)
    {
        List<Tuple<String>> result = new ArrayList<>();

        List<MockMultiLocalizationValue> vals = _multiLocalizationValues.stream()
                .filter(v -> v.type.equals(type))
                .filter(v -> v.uuid.equals(uuid))
                .collect(Collectors.toList());

        for (MockMultiLocalizationValue val : vals) {
            Tuple<String> t = new Tuple<>();
            t.setKey(val.languageCode);
            t.setVal(val.value);

            result.add(t);
        }

        return result;
    }

    @Override
    public List<String> getQuestionIdsBasedOnFiltersSansCategory(int minDifficulty, int maxDifficulty, String languageCodesAsCommaDelimitedList)
    {
//ignoring all that and just returning everything; this is a mock after all
        return _questions.stream()
                .map(q -> q.getId())
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getQuestionIdsBasedOnFilters(int minDifficulty, int maxDifficulty, String languageCodesAsCommaDelimitedList, String categoryUuidsAsCommaDelimiatedList)
    {
//ignoring all that and just returning everything; this is a mock after all
        return _questions.stream()
                .map(q -> q.getId())
                .collect(Collectors.toList());
    }

    @Override
    public void addTieBreakerQuestion(String gameId, String matchId)
    {
        Set<String> matchesForGame = _tieBreakers.get(gameId);
        if (matchesForGame == null) {
            matchesForGame = new HashSet<>();
            _tieBreakers.put(gameId, matchesForGame);
        }
        matchesForGame.add(matchId);
    }

    @Override
    public boolean isTieBreakerQuestion(String gameId, String matchId)
    {
        Set<String> matchesForGame = _tieBreakers.get(gameId);
        if (matchesForGame == null) return false;
        return matchesForGame.contains(matchId);
    }

    @Override
    public void addTieBreakerWinnerSubscriberId(String gameId, String matchId, long subscriberId)
    {
        _tieBreakerWinnerSubscribers.put(gameId + matchId, subscriberId);
    }

    @Override
    public long getTieBreakerWinnerSubscriberId(String gameId, String matchId)
    {
        return _tieBreakerWinnerSubscribers.get(gameId + matchId);
    }

    @Override
    public void insertMatchQuestion(MatchQuestion matchQuestion)
    {
        _matchQuestions.add(matchQuestion);
    }

    @Override
    public void insertSubscriberQuestionAnswer(SubscriberQuestionAnswer sqa)
    {
        _sqas.add(sqa);
    }

    @Override
    public SubscriberQuestionAnswer getSubscriberQuestionAnswer(String subscriberQuestionAnswerId)
    {
        return _sqas.stream()
                .filter(sqa -> sqa.getId().equals(subscriberQuestionAnswerId))
                .findFirst()
                .orElse(null);
    }

    @Override
    public void setQuestionViewedTimestampOnSubscriberQuestionAnswer(SubscriberQuestionAnswer sqa)
    {
        Optional<SubscriberQuestionAnswer> oSqa = _sqas.stream()
                .filter(s -> s.getId().equals(sqa.getId()))
                .findFirst();

        if (oSqa.isPresent()) {
            oSqa.get().setQuestionPresentedTimestamp(sqa.getQuestionPresentedTimestamp());
        }

    }

    @Override
    public void setAnswerOnSubscriberQuestionAnswer(SubscriberQuestionAnswer sqa)
    {
        Optional<SubscriberQuestionAnswer> oSqa = _sqas.stream()
                .filter(s -> s.getId().equals(sqa.getId()))
                .findFirst();

        if (oSqa.isPresent()) {
            oSqa.get().setSelectedAnswerId(sqa.getSelectedAnswerId());
            oSqa.get().setDurationMilliseconds(sqa.getDurationMilliseconds());
        }
    }

    @Override
    public List<SubscriberQuestionAnswer> getSubscriberQuestionAnswersForMatch(String matchId, long subscriberId)
    {
        return _sqas.stream()
                .filter(sqa -> sqa.getSubscriberId() == subscriberId)
                .filter(sqa -> sqa.getMatchId().equals(matchId))
                .collect(Collectors.toList());
    }

    @Override
    public List<MatchQuestion> getMatchQuestionsForMatch(String matchId)
    {
        return _matchQuestions.stream()
                .filter(mq -> mq.getMatchId().equals(matchId))
                .collect(Collectors.toList());
    }

    @Override
    public void updateSubscriberQuestionAnswer(SubscriberQuestionAnswer sqa)
    {
        Optional<SubscriberQuestionAnswer> oSqa = _sqas.stream()
                .filter(s -> s.getId().equals(sqa.getId()))
                .findFirst();

        if (oSqa.isPresent()) {
            oSqa.get().setSelectedAnswerId(sqa.getSelectedAnswerId());
            oSqa.get().setQuestionPresentedTimestamp(sqa.getQuestionPresentedTimestamp());
            oSqa.get().setDurationMilliseconds(sqa.getDurationMilliseconds());
            oSqa.get().setDetermination(sqa.getDetermination());
            oSqa.get().setWon(sqa.isWon());
        }
    }

    @Override
    public void updateMatchQuestion(MatchQuestion matchQuestion)
    {
        Optional<MatchQuestion> oMq = _matchQuestions.stream()
                .filter(mq -> mq.getId().equals(matchQuestion.getId()))
                .findFirst();

        if (oMq.isPresent()) {
            oMq.get().setMatchQuestionStatus(matchQuestion.getMatchQuestionStatus());
            oMq.get().setWonSubscriberId(matchQuestion.getWonSubscriberId());
            oMq.get().setDetermination(matchQuestion.getDetermination());
            oMq.get().setCompletedDate(matchQuestion.getCompletedDate());
        }
    }

    @Override
    public void addGamePayout(GamePayout gamePayout)
    {
        _gamePayouts.add(gamePayout);
    }

    @Override
    public GamePayout getGamePayout(String gameId)
    {
        return _gamePayouts.stream()
                .filter(gp -> gp.getGameId().equals(gameId))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("no game payout found for: " + gameId));
    }

    @Override
    public void insertPayoutModel(PayoutModel payoutModel)
    {
        _payoutModels.add(payoutModel);
    }

    @Override
    public PayoutModel getPayoutModel(int payoutModelId)
    {
        return _payoutModels.stream()
                .filter(pm -> pm.getPayoutModelId() == payoutModelId)
                .findFirst()
                .orElse(null);
    }

    //INSERT INTO snowyowl.payout_table (game_id, row_id, rank_from, rank_to, amount) VALUES (#{0}, #{1}, #{2}, #{3}, #{4})
    @Override
    public void addPayoutTableRow(String gameId, String rowId, int rankFrom, int rankTo, float amount)
    {
        DbPayoutTableRow row = new DbPayoutTableRow(gameId, rankFrom, rankTo, amount, null, null, rowId);
        _payoutTableRows.add(row);
    }

    //SELECT * FROM snowyowl.payout_table WHERE game_id = #{0} ORDER BY rank_from ASC
    @Override
    public List<PayoutTableRow> getPayoutTableRows(String gameId)
    {
        return _payoutTableRows.stream()
                .filter(ptr -> ptr.gameId.equals(gameId))
                .sorted(Comparator.comparing(DbPayoutTableRow::getRankFrom))
                .collect(Collectors.toList());
    }

    //DELETE FROM snowyowl.payout_table WHERE game_id = #{0}
    @Override
    public void removePayoutTableRows(String gameId)
    {
        _payoutTableRows.removeAll(
                _payoutTableRows.stream()
                .filter(ptr -> ptr.gameId.equals(gameId))
                .collect(Collectors.toList())
        );
    }

    @SuppressWarnings("serial")
    private static class DbPayoutTableRow
    extends PayoutTableRow
    {
        public String gameId;

        public DbPayoutTableRow(String gameId, int rankFrom, int rankTo, float amount, String type, String category, String rowId)
        {
            super(rankFrom, rankTo, amount, type, category, rowId);
            this.gameId = gameId;
        }
    }

    @Override
    public void clearSubscriberQuestions(String gameId)
    {
        //no-op
    }

    @Override
    public SubscriberStats getSubscriberStats(long subscriberId)
    {
        //no-op
        return null;
    }






























    @Override
    public void removeMutliLocalizationValues(String uuid, String type)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public List<String> getBundleIdsForApp(int appId)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void addSocketIoLog(Integer subscriberId, String messageType, String message, String status, Date sentDate)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void clearPhoneVerificationCodeForSubscriber(long subscriberId)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void addPhoneVerificationCodeForSubscriber(long subscriberId, String phone, String code)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean isPhoneVerificationCodeValidForSubscriber(long subscriberId, String phone, String code,
            Date cutoffDate)
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public List<SubscriberFromSearch> getSubscribersInSignupDateRange(Date from, Date to)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void insertQuestionCategory(QuestionCategory category)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public List<Tuple<String>> getQuestionCategoryIdToKey()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<QuestionCategory> getAllQuestionCategories()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public QuestionCategory getQuestionCategoryByKey(String key)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public QuestionCategory getQuestionCategoryById(String id)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<String> getQuestionIdsForCategory(String categoryId)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void removeQuestionCategoryNames(String categoryId)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void deleteQuestionCategory(String categoryId)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public List<SubscriberQuestionAnswer> getSubscriberQuestionAnswersViaMatchQuestion(String matchQuestionId)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<String> getQuestionIdsByState(STATUS status)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void updateQuestionStatus(String questionId, STATUS newStatus)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void deleteQuestionAnswers(String questionid)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void deleteQuestionCategories(String questionId)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void deleteQuestionForbiddenCountryCodes(String questionId)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void deleteQuestionLanguageCodes(String questionId)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void deleteQuestion(String questionId)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public List<GameWinner> getGameWinners(String gameId)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<String> getGameRoundQuestionIds(String gameId, String roundid)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void addGameRoundQuestion(String gameId, String roundId, String questionId, int order)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public List<SubscriberRoundQuestion> getSubscriberRoundQuestions(long subscriberId, String roundId)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void addSubscriberRoundQuestion(long subscriberId, String roundId, String questionId, int order)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void updateSubscriberRoundQuestion(SubscriberRoundQuestion srq)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public List<PayoutModel> getPayoutModelsByEntranceFee(float entranceFee)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<PayoutModel> getAllPayoutModels()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<PayoutModelRound> getPayoutModelRounds(int payoutModelId)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void insertPayoutModelRound(PayoutModelRound pmr)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean isPayoutModelInUse(int payoutModelId)
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void updatePayoutModel(PayoutModel pm)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void deletePayoutModel(int payoutModelId)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void deletePayoutModelRounds(int payoutModelId)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void deactivatePayoutModel(int payoutModelId, long deactivatorId, String reason)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void addAffiliatePlan(AffiliatePlan plan)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void clearCurrentAffiliatePlan()
    {
        // TODO Auto-generated method stub

    }

    @Override
    public AffiliatePlan getCurrentAffiliatePlan()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public AffiliatePlan getAffiliatePlan(int affiliatePlanId)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void insertIneligibleSubscriber(IneligibleSubscriber is)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public List<IneligibleSubscriber> getIneligibleSubscribers()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void deleteIneligibleSubscriber(long isId)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean isSubscriberIneligible(long subscriberId)
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public List<ProhibitedSubscriber> getProhibitedSubscribers()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void insertProhibitedSubscriber(ProhibitedSubscriber ps)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void deleteProhibitedSubscriber(long subscriberId)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean isSubscriberProhibited(long subscriberId)
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void addSubscriberActionLog(long subscriberId, String action, String reason, String note)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void insertSubscriberStats(SubscriberStats stats)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void updateSubscriberStats(SubscriberStats stats)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public List<SubscriberStats> getAllSubscriberStats()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<ReportStructAffiliatePayoutSubscriberWinnings> getReportStructAffiliatePayoutSubscriberWinnings(
            Date since)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<ReportStructAffiliatePayoutSubscriberNetworkSize> getReportStructAffiliatePayoutSubscriberNetworkSize(
            String subscriberIdsAsCommaDelimitedList)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<ReportStructAffiliatePayoutSubscriberInfo> getReportStructAffiliatePayoutSubscriberInfo(
            String subscriberIdsAsCommaDelimitedList)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public SponsorCashPool getSponsorCashPoolByPoolOwnerSubscriberId(long subscriberId)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public SponsorCashPool getSponsorCashPoolBySponsorPlayerSubscriberId(long subscriberId)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public SponsorCashPool getSponsorCashPoolById(int sponsorCashPoolId)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void insertSponsorCashPool(SponsorCashPool pool)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void updateSponsorCashPool(SponsorCashPool pool)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void addSponsorCashPoolTransaction(int sponsorCashPoolId, double amount, String reason)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public List<Long> getAvailableSponsorIds()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void addSponsorToGame(int sponsorCashPoolId, String gameId, long sponsorSubscriberId)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public Sponsor getSingleSponsorForGame(String gameId)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Sponsor getSponsorById(long subscriberId)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void releaseSponsorPlayerForGame(long subscriberId)
    {
        // TODO Auto-generated method stub

    }
}
