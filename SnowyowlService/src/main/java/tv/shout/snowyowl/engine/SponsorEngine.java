package tv.shout.snowyowl.engine;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.springframework.beans.factory.annotation.Autowired;

import tv.shout.sc.domain.Game;
import tv.shout.sc.domain.Round;
import tv.shout.snowyowl.collector.CommonBusinessLogic;
import tv.shout.snowyowl.domain.SponsorCashPool;

public class SponsorEngine
extends BaseAiEngine
{
//    private static Logger _logger = Logger.getLogger(SponsorEngine.class);

    @Autowired
    private CommonBusinessLogic _commonBusinessLogic;

    private Lock _botBusyLock = new ReentrantLock();

    //assumes caller has created a db transaction
    /**
     * @return how many sponsor players are available right now to be added to a game
     */
    public int getNumberOfAvailableSponsors()
    {
        return _dao.getNumberOfAvailableSponsors();
    }

    //assumes caller has created a db transaction
    /**
     * @param sponsorCashPoolId who's paying for it
     * @param game which game is it going to
     * @param numberOfSponsorPlayers how many are being added
     * @param round the first bracket round
     * @param costToJoin how much to join
     * @param totlaCost .
     * @throws NotEnoughSponsorsException thrown if there's not enough free sponsor players to cover it
     * @throws NotEnoughSponsorPoolCashException if there's not enough cash in the sponsor's pool to cover it
     */
    public void addSponsorsToGame(int sponsorCashPoolId, Game game, int numberOfSponsorPlayers, Round round, double costToJoin, double totalCost)
    throws NotEnoughSponsorsException, NotEnoughSponsorPoolCashException
    {
        _botBusyLock.lock();
        try {
            List<Long> availableSponsorIds = _dao.getAvailableSponsorIds();
//_logger.info(">>> #availalbe sponsors: " + availableSponsorIds.size());

            //make sure there's enough sposors
            if (availableSponsorIds.size() < numberOfSponsorPlayers) {
                throw new NotEnoughSponsorsException();
            }

            //make sure there's enough cash in the sponsor pool to cover it
            SponsorCashPool pool = _dao.getSponsorCashPoolById(sponsorCashPoolId);
            if (pool == null) {
                throw new IllegalArgumentException("invalid sponsorCashPoolId");
            }
            if (pool.getAmount() < totalCost) {
                throw new NotEnoughSponsorPoolCashException();
            }
//_logger.info(">>> pool amount: " + pool.getAmount() + ", totalCost: " + totalCost);

            //to make sure the same sponsor players don't get used over and over each time, shuffle the list
            Collections.shuffle(availableSponsorIds);

            //add to the game
            for (int i=0; i<numberOfSponsorPlayers; i++) {
                long sponsorSubscriberId = availableSponsorIds.get(i);
                boolean lastCall = i == numberOfSponsorPlayers-1;
//_logger.info(">>> adding sponsor " + sponsorSubscriberId + ", lastSponsorBeingAdded? " + lastCall);

                _dao.addSponsorToGame(sponsorCashPoolId, game.getId(), sponsorSubscriberId);
                _commonBusinessLogic.joinGameAsSponsor(pool, game, sponsorSubscriberId, round, costToJoin);
                _commonBusinessLogic.setPlayerAvailabilityAsSponsor(game, null, sponsorSubscriberId, lastCall);
            }
        } finally {
            _botBusyLock.unlock();
        }
    }

    //assumes caller has created a db transaction
    /**
     * @param gameId the game to check
     * @return a list of all the subscriberIds for the given game that are actually sponsor players
     */
    public List<Long> getSponsorsForGame(String gameId)
    {
        return _dao.getSponsorIdsGame(gameId);
    }
}
