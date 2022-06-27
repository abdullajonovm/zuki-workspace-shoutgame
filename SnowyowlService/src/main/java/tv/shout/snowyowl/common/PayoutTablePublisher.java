package tv.shout.snowyowl.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import org.apache.log4j.Logger;

import com.meinc.webdatastore.service.IWebDataStoreService;

import tv.shout.sc.domain.Game;
import tv.shout.sc.domain.Round;
import tv.shout.snowyowl.dao.IDaoMapper;
import tv.shout.snowyowl.domain.GamePayout;
import tv.shout.snowyowl.domain.PayoutModel;
import tv.shout.snowyowl.domain.PayoutModelRound;
import tv.shout.snowyowl.domain.PayoutTableRow;
import tv.shout.snowyowl.engine.EngineCoordinator;
import tv.shout.snowyowl.engine.PayoutManagerException;
import tv.shout.util.FastMap;

public interface PayoutTablePublisher
extends WdsPublisher
{
    //make sure the callers have this wrapped inside of a transaction
    default List<PayoutModelRound> publishPayoutTable(
        Game game, List<Round> rounds, Integer actualNumPlayers, Integer maxNumPlayers,
        EngineCoordinator engineCoordinator, IWebDataStoreService wdsService, IDaoMapper dao, Logger logger)
    throws PayoutManagerException
    {
        if (actualNumPlayers == null && maxNumPlayers == null) {
            throw new IllegalStateException("actualNumPlayers AND maxNumPlayers cannot both be null");
        }

        //divide into n steps (FUTURE: have the client specify this)
        int[] steps = new int[] {10, 20, 50, 100, 250, 500, 750, 1000, 1500, 2000, 3000, 4000, 5000, 7500, 10000, 20000, 50000, 100000, 500000, 1000000};

        //chop out anything above the max
        steps = IntStream.of(steps)
            .filter(i -> i<= maxNumPlayers)
            .toArray();

        Map<String, Object> payoutJsonMap = new HashMap<>();

        GamePayout gamePayout = dao.getGamePayout(game.getId());
        PayoutModel payoutModel = dao.getPayoutModel(gamePayout.getPayoutModelId());

        //generate each step
        List<Map<String,List<PayoutModelRound>>> stepsPayoutTable = new ArrayList<>(steps.length);
        for (int step : steps) {
            List<PayoutModelRound> data = engineCoordinator.getAdjustedPayoutModelRounds(game, step, payoutModel, gamePayout);
            stepsPayoutTable.add(new FastMap<>("numPlayers", step, "payoutTable", data));
        }
        payoutJsonMap.put("stepPayouts", stepsPayoutTable);

        List<PayoutModelRound> maxPayoutTable = engineCoordinator.getAdjustedPayoutModelRounds(game, maxNumPlayers, payoutModel, gamePayout);
        List<PayoutTableRow> maxPayoutTable2 = engineCoordinator.generateCollapsedPayoutTable(game, rounds, maxNumPlayers, gamePayout);
        payoutJsonMap.put("maxPayout", new FastMap<>("numPlayers", maxNumPlayers, "payoutTable", maxPayoutTable));

        publishJsonWdsDoc(logger, wdsService, null, "/" + game.getId() + "/payout.json", payoutJsonMap);

        List<PayoutModelRound> payoutTableToReturn = maxPayoutTable;
        List<PayoutTableRow> payoutTableToSave = maxPayoutTable2;

        //generate actual
        if (actualNumPlayers != null) {
            List<PayoutModelRound> actualPayoutTable = engineCoordinator.getAdjustedPayoutModelRounds(game, actualNumPlayers, payoutModel, gamePayout);
            List<PayoutTableRow> actualPayoutTable2 = engineCoordinator.generateCollapsedPayoutTable(game, rounds, actualNumPlayers, gamePayout);

            //per aidan request (13 Sep 2018), also publish this as a separate doc
            publishJsonWdsDoc(logger, wdsService, null, "/" + game.getId() + "/actualPayout.json", new FastMap<>("numPlayers", actualNumPlayers, "payoutTable", actualPayoutTable));

            payoutTableToReturn = actualPayoutTable;
            payoutTableToSave = actualPayoutTable2;
        }

        //remove any previous payout table rows
        dao.removePayoutTableRows(game.getId());

        //store the payout table in the db
        payoutTableToSave.stream().forEach(row -> {
            dao.addPayoutTableRow(game.getId(), row.getRowId(), row.getRankFrom(), row.getRankTo(), row.getAmount());
        });

        return payoutTableToReturn;
    }
}
