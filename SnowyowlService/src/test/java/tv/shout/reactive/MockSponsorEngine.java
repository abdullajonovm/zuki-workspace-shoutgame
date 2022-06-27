package tv.shout.reactive;

import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import tv.shout.sc.domain.Game;
import tv.shout.sc.domain.Round;
import tv.shout.simplemessagebus.MessageBus;
import tv.shout.snowyowl.collector.CommonBusinessLogic;
import tv.shout.snowyowl.domain.AiQuestionBundle;
import tv.shout.snowyowl.engine.NotEnoughSponsorPoolCashException;
import tv.shout.snowyowl.engine.NotEnoughSponsorsException;
import tv.shout.snowyowl.engine.SponsorEngine;

public class MockSponsorEngine
extends SponsorEngine
implements MockAiEngineCommon
{
    private static Logger _logger = Logger.getLogger(MockSponsorEngine.class);

    private MockSnowyowlDao _dao;
    private CommonBusinessLogic _commonBusinessLogic;
    private MessageBus _messageBus;

    public MockSponsorEngine(MessageBus messageBus, MockSnowyowlDao dao, CommonBusinessLogic commonBusinessLogic)
    {
        _messageBus = messageBus;
        _dao = dao;
        _commonBusinessLogic = commonBusinessLogic;
    }

    @Override
    public int getNumberOfAvailableSponsors()
    {
        return _dao.getNumberOfAvailableSponsors();
    }

    @Override
    public void addSponsorsToGame(int sponsorCashPoolId, Game game, int numberOfSponsorPlayers, Round round, double costToJoin, double totalCost)
    throws NotEnoughSponsorsException, NotEnoughSponsorPoolCashException
    {
        int numAvailable = getNumberOfAvailableSponsors();
        if (numAvailable < numberOfSponsorPlayers) {
            throw new NotEnoughSponsorsException();
        }

        List<MockSponsorPlayer> allAvailableSponsorPlayers = _dao.getAllAvailableSponsorPlayers();
        for (int i=0; i<numberOfSponsorPlayers; i++) {
            MockSponsorPlayer sponsorPlayer = allAvailableSponsorPlayers.get(i);
            sponsorPlayer.busyFlag = true;
            sponsorPlayer.gameId = game.getId();
            sponsorPlayer.lastUsedDate = new Date();

            _dao.updateSponsorPlayer(sponsorPlayer);
        }
    }

    @Override
    public List<Long> getSponsorsForGame(String gameId)
    {
        return _dao.getSponsorIdsGame(gameId);
    }

    @Override
    public void submitAnswer(final AiQuestionBundle bundle)
    {
        mockSubmitAnswer(bundle, _logger, _commonBusinessLogic, _dao, _messageBus);
    }

}
