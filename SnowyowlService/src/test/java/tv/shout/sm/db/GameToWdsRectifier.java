package tv.shout.sm.db;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.meinc.gameplay.domain.Tuple;

import tv.shout.sc.service.IShoutContestService;
import tv.shout.sm.db.DbProvider.DB;
import tv.shout.sm.test.HttpLibrary;
import tv.shout.sm.test.NetworkException;
import tv.shout.sm.test.SRD;

public class GameToWdsRectifier
extends BaseDbSupport
{
    private SRD _srd;
    private IShoutContestService _shoutContestService;

    public GameToWdsRectifier(DB which)
    throws Exception
    {
        super(which);
    }

    @Override
    public void init(DB which)
    throws Exception
    {
        throw new UnsupportedOperationException();
//        _srd = SRD.getInstance(which, "shawker@me-inc.com");
//        while (_srd == null) {
//            Thread.sleep(100);
//            _srd = SRD.getInstance(which, "shawker@me-inc.com");
//        }
//
//        //setup the service backdoor entry point based on which database is being used
//        switch (which)
//        {
//            case DC4: {
//                _shoutContestService = new ShoutContestServiceClientProxy(false);
//
//                InetSocketAddress socket = new InetSocketAddress(Inet4Address.getByName("dc4-collector1.shoutgameplay.com"), 43911);
//                MrSoaServer server = new MrSoaServer(socket);
//                MrSoaServerMonitor monitor = MrSoaServerMonitor.getInstance();
//                monitor.registerService(
//                        ((ShoutContestServiceClientProxy)_shoutContestService).getEndpoint(),
//                        server);
//            }
//            break;
//        }

    }

    private List<Tuple<String>> getAllRelevantGameIds()
    throws SQLException
    {
        String sql = "SELECT id, game_status FROM contest.game WHERE game_status in ('CANCELLED', 'CLOSED')";
        List<Tuple<String>> result = new ArrayList<>();

        Statement s = null;
        ResultSet rs = null;
        Connection con = _db.getConnection();
        try {
            s = con.createStatement();
            rs = s.executeQuery(sql);
            while (rs.next()) {
                Tuple<String> t = new Tuple<>();
                t.setKey(rs.getString("id"));
                t.setVal(rs.getString("game_status"));
                result.add(t);
            }

        } finally {
            if (rs != null) {
                rs.close();
                rs = null;
            }
            if (s != null) {
                s.close();
                s = null;
            }
            if (con != null) {
                con.close();
                con = null;
            }
        }

        return result;
    }

    private void republishGame(String gameId, String dbGameStatus, String wdsGameStatus)
    {
        System.out.println(MessageFormat.format("REPUBLISHING: gameId: {0}, db_status: {1}, wds_status: {2}", gameId, dbGameStatus, wdsGameStatus));

        _shoutContestService.publishGameToWds(gameId, null);
    }

    @Override
    public void run()
    throws Exception
    {
        List<Tuple<String>> gameData = getAllRelevantGameIds();

        System.out.println("GAME DATA:");

        //see if the WDS doc exists for the game, and if so, what the status is, and if the wds status matches the db status
        for (Tuple<String> t : gameData) {
            String gameId = t.getKey();
            String dbGameStatus = t.getVal();
            String wdsGameStatus;

            String url = "http://" + _srd.getWdsUrls()[0] + "/" + gameId + "/game.json";

            try {
                String wdsGameStr = HttpLibrary.httpGet(url);
                JsonNode json = new ObjectMapper().readTree(wdsGameStr);
                wdsGameStatus = json.get("gameStatus").textValue();

            } catch (NetworkException e) {
                if (e.httpResponseCode == 404) {
                    wdsGameStatus = "not_published";
                } else {
                    throw e;
                }
            }

            System.out.println(MessageFormat.format("gameId: {0}, db_status: {1}, wds_status: {2}", gameId, dbGameStatus, wdsGameStatus));

            if (!dbGameStatus.equals(wdsGameStatus)) {
                republishGame(gameId, dbGameStatus, wdsGameStatus);
            }
        }

    }

    public static void main(String[] args)
    throws Exception
    {
        new GameToWdsRectifier(DbProvider.DB.LOCAL);
    }

}
