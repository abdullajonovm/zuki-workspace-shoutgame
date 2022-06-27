package tv.shout.sm.db;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.lang.StringEscapeUtils;

import com.fasterxml.jackson.core.type.TypeReference;

import tv.shout.sm.db.BaseDbSupport.SqlMapper;
import tv.shout.util.JsonUtil;

public class ShoutClassicStaistics
{
    private static final int POOL_SIZE = 16;
    private DbProvider _db;

    private ShoutClassicStaistics()
    {
        _db = new DbProvider(DbProvider.DB.SHOUTPROD);
    }

    private void stop()
    {
        _db.close();
    }

    private Map<Integer, Integer> getSubscriberToXpMap()
    throws SQLException
    {
System.out.println("BEGIN getSubscriberToXpMap");
        Statement s = null;
        ResultSet rs = null;
        Connection con = _db.getConnection();

        String sql = "SELECT subscriber_id, xp FROM gameplay.subscriber_xp_history";

        Map<Integer, Integer> subscriberToXpMap = new HashMap<>();

        try {
            s = con.createStatement();
            rs = s.executeQuery(sql);

            //int count = 0;

            while (rs.next()) {
                int subscriberId = rs.getInt(1);
                int newXp = rs.getInt(2);
                Integer currentXp = subscriberToXpMap.get(subscriberId);
                if (currentXp == null) {
                    subscriberToXpMap.put(subscriberId, newXp);
                } else {
                    currentXp += newXp;
                    subscriberToXpMap.put(subscriberId, currentXp);
                }

                /*count++;
                if (count % 100 == 0) {
                    System.out.print(".");
                }
                if (count % 80000 == 0) {
                    System.out.println();
                }*/
            }

System.out.println("END getSubscriberToXpMap");
            return subscriberToXpMap;

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
                //con = null;
            }
        }
    }

    private List<SubscriberXp> getSubscriberInfoForXpMap(Map<Integer, Integer> subscriberToXpMap)
    throws SQLException, InterruptedException
    {
////TOxDO: remove
//int xp8 = subscriberToXpMap.get(8);
//int xp12 = subscriberToXpMap.get(12);
//int xp15 = subscriberToXpMap.get(15);
//subscriberToXpMap.clear();
//subscriberToXpMap.put(8, xp8);
//subscriberToXpMap.put(12, xp12);
//subscriberToXpMap.put(15, xp15);

System.out.println("BEGIN getSubscriberInfoForXpMap, # of records: " + subscriberToXpMap.size());
        final ExecutorService threadPool = Executors.newFixedThreadPool(POOL_SIZE);
        final CountDownLatch cdl = new CountDownLatch(subscriberToXpMap.size());

        subscriberXpList = new ArrayList<>(subscriberToXpMap.size());

        for (int subscriberId : subscriberToXpMap.keySet()) {
            threadPool.execute(() -> {
                try {
                    addSubscriberInfoForXpToMap(subscriberId, subscriberToXpMap.get(subscriberId));
                } catch (SQLException e) {
                    e.printStackTrace();
                } finally {
                    cdl.countDown();
                }
            });
        }

        cdl.await();
        threadPool.shutdown();
System.out.println("END getSubscriberInfoForXpMap");

        return subscriberXpList;
    }

    private AtomicInteger _counter = new AtomicInteger(0);
    private Lock _lock = new ReentrantLock();
    private static List<SubscriberXp> subscriberXpList;

    private void addSubscriberInfoForXpToMap(int subscriberId, int xp)
    throws SQLException
    {
        PreparedStatement ps = null;
        ResultSet rs = null;
        Connection con = _db.getConnection();

        String sql = "SELECT nickname, email, firstname, lastname FROM gameplay.s_subscriber WHERE subscriber_id = ?";

        try {
            ps = con.prepareStatement(sql);

            ps.setInt(1, subscriberId);
            rs = ps.executeQuery();
            rs.next();

            _lock.lock();
            try {
                subscriberXpList.add(new SubscriberXp(subscriberId, xp, rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4)));
            } finally {
                _lock.unlock();
            }

            rs.close();

            int count = _counter.incrementAndGet();
            if (count % 100 == 0) {
                System.out.print(".");
            }
            if (count % 6000 == 0) {
                System.out.println("\ncount: " + count);
            }

        } finally {
            if (rs != null) {
                rs.close();
                rs = null;
            }
            if (ps != null) {
                ps.close();
                ps = null;
            }
            if (con != null) {
                con.close();
                //con = null;
            }
        }
    }

    private void exportToCsv(List<SubscriberXp> subscriberXpList)
    throws IOException
    {
        //write it all out to a csv file
        Path path = Paths.get("/Users/shawker/Desktop/shout_xp_per_player.csv");
        try (BufferedWriter writer = Files.newBufferedWriter(path)) {
            //the column header row
            writer.write("subscriber_id,xp,nickname,email,firstname,lastname");
            writer.newLine();

            //sort high to low
            subscriberXpList.stream()
                .sorted( Comparator.comparing(SubscriberXp::getXp, Comparator.nullsLast(Comparator.reverseOrder())) )
                .forEach(s -> {
                    try {
                        writer.write(s.toString());
                        writer.newLine();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
        }
    }

    private static class SubscriberXp
    {
        public int subscriberId;
        public int totalXp;
        public String nickname;
        public String email;
        public String firstname;
        public String lastname;

        public SubscriberXp(int subscriberId, int totalXp, String nickname, String email, String firstname, String lastname)
        {
            this.subscriberId = subscriberId;
            this.totalXp = totalXp;
            this.nickname = nickname;
            this.email = email;
            this.firstname = firstname;
            this.lastname = lastname;
        }

        public int getXp() { return totalXp; }

        @Override
        public String toString()
        {
//            return MessageFormat.format(
//                "sId: {0,number,#}, xp: {1}, nickname: {2}, email: {3}, firstname: {4}, lastname: {5}",
//                subscriberId, totalXp, nickname, email, firstname, lastname);

            return MessageFormat.format(
                "{0,number,#},{1,number,#},{2},{3},{4},{5}",
                subscriberId, totalXp, StringEscapeUtils.escapeCsv(nickname), StringEscapeUtils.escapeCsv(email), StringEscapeUtils.escapeCsv(firstname), StringEscapeUtils.escapeCsv(lastname));
        }
    }

    private List<Integer> getPublicEventIds()
    throws SQLException
    {
        String sql = "select event_id from gameplay.event where private_evt = 0";

        List<Integer> count = (List<Integer>) BaseDbSupport.executeSqlForList(_db, sql, new SqlMapper<Integer>() {
            @Override
            public void populatePreparedStatement(PreparedStatement ps) throws SQLException
            {
            }

            @Override
            public Integer mapRowToType(ResultSet rs) throws SQLException
            {
                return rs.getInt(1);
            }

            @Override
            public Collection<Integer> getCollectionObject()
            {
                return new ArrayList<>();
            }
        });

        return count;
    }

    private static int _xcounter=0;
    private Map<Integer, Integer> _subscriberIdToPrizeAmountMap = new HashMap<>();
    private Map<Integer, List<String>> _subscriberIdToNonCashPrizeAmountMap = new HashMap<>();
    private Map<Integer, xSubscriber> _subscriberIdToxSubscriberMap = new HashMap<>();

    private void parseEventResult(final int eventId)
    throws SQLException, IOException
    {
System.out.print(".");
_xcounter++;
if (_xcounter % 100 == 0) System.out.println();
        String sql = "select results from gameplay.event_results where event_id = ?";

        List<EventResult> eventResults = (List<EventResult>) BaseDbSupport.executeSqlForList(_db, sql, new SqlMapper<EventResult>() {
            @Override
            public void populatePreparedStatement(PreparedStatement ps) throws SQLException
            {
                ps.setInt(1, eventId);
            }

            @Override
            public EventResult mapRowToType(ResultSet rs) throws SQLException
            {
                String eventResultJson = rs.getString(1);
                EventResult eventResult=null;

                TypeReference<EventResult> typeRef = new TypeReference<EventResult>(){};
                try {
                    eventResult = JsonUtil.getObjectMapper().readValue(eventResultJson, typeRef);
                    //System.out.println(eventResultJson);

                } catch (IOException e) {
                    System.out.println("unable to parse eventResult:\n" + eventResultJson);
                    e.printStackTrace();
                    System.exit(1);
                }

                return eventResult;
            }

            @Override
            public Collection<EventResult> getCollectionObject()
            {
                return new ArrayList<>();
            }
        });

        EventResult eventResult = eventResults.size() > 0 ? eventResults.get(0) : null;
        if (eventResult != null) {
            parsePrizesFromEventResult(eventResult);
        }
    }

    private void parsePrizesFromEventResult(EventResult eventResult)
    {
        if (eventResult.grandPrizeWinner != null && eventResult.grandPrizeWinner.size() > 0) {
            parseGrandPrizeWinners(eventResult.grandPrizeWinner);
        }
        if (eventResult.grandPrizeWinners != null && eventResult.grandPrizeWinners.size() > 0) {
            parseGrandPrizeWinners(eventResult.grandPrizeWinners);
        }
        if (eventResult.shoutNetworkWinners != null && eventResult.shoutNetworkWinners.size() > 0) {
            parseShoutNetworkWinners(eventResult.shoutNetworkWinners);
        }
        if (eventResult.instantWinners != null && eventResult.instantWinners.size() > 0) {
            parseInstantWinners(eventResult.instantWinners);
        }
        if (eventResult.questionWinners != null && eventResult.questionWinners.size() > 0) {
            parseQuestionWinners(eventResult.questionWinners);
        }
        if (eventResult.vipboxWinners != null && eventResult.vipboxWinners.size() > 0) {
            parseVipboxWinners(eventResult.vipboxWinners);
        }
    }

    private void parseGrandPrizeWinners(List<GrandPrizeWinner> winners)
    {
        for (GrandPrizeWinner winner : winners) {
            parsePrizes(winner.sid, winner.prizes);

            if (winner.recruitedBy != null) {
                parseRecruitedBy(winner.recruitedBy);
            }
        }
    }

    private void parseRecruitedBy(RecruitedBy winner)
    {
        parsePrizes(winner.sid, winner.prizes);
    }

    private void parseShoutNetworkWinners(List<ShoutNetworkWinner> winners)
    {
        for (ShoutNetworkWinner winner : winners) {
            parsePrizes(winner.sid, winner.prizes);
        }
    }

    private void parseInstantWinners(List<InstantWinner> winners)
    {
        for (InstantWinner winner : winners) {
            List<Integer> sids = winner.sids;
            List<String> prizes = winner.prizes;

            for (int sid : sids) {
                parsePrizes(sid, prizes);
            }
        }
    }

    private void parseQuestionWinners(List<QuestionWinner> winners)
    {
        for (QuestionWinner winner : winners) {
            parsePrizes(winner.sid, winner.prizes);
        }
    }

    private void parseVipboxWinners(List<VipboxWinner> winners)
    {
        for (VipboxWinner winner : winners) {
            parseWinners(winner.winners);
        }
    }

    private void parseWinners(List<Winner> winners)
    {
        for (Winner winner : winners) {
            int sid = winner.sid;
            int amount = winner.amount;

            Integer existingAmount = _subscriberIdToPrizeAmountMap.get(sid);
            if (existingAmount == null) {
                existingAmount = amount;
            } else {
                existingAmount += amount;
            }

            _subscriberIdToPrizeAmountMap.put(sid, existingAmount);
        }
    }

    private void parsePrizes(int sid, List<String> prizes)
    {
        Integer startingAmount = _subscriberIdToPrizeAmountMap.get(sid);
        if (startingAmount == null) {
            startingAmount = 0;
        }

        //parse each prize. the following forms all seem to be valid: $25, $25.00, 25, Jazz Tickets
        for (String prize : prizes) {
            if (prize == null || prize.trim().length() == 0) {
                continue;
            }

            if (prize.startsWith("$")) {
                try {
                    int amount = Double.valueOf(prize.substring(1)).intValue();
                    _subscriberIdToPrizeAmountMap.put(sid, startingAmount + amount);
                } catch (NumberFormatException e) {
                    //assume a non cash prize
                    List<String> existingPrizes = _subscriberIdToNonCashPrizeAmountMap.get(sid);
                    if (existingPrizes == null) {
                        existingPrizes = new ArrayList<>();
                    }
                    existingPrizes.add(prize);
                    _subscriberIdToNonCashPrizeAmountMap.put(sid, existingPrizes);
                }
            } else {
                //attempt to parse as an int
                try {
                    int amount = Double.valueOf(prize).intValue();
                    _subscriberIdToPrizeAmountMap.put(sid, startingAmount + amount);

                } catch (NumberFormatException e) {
                    //assume a non cash prize
                    List<String> existingPrizes = _subscriberIdToNonCashPrizeAmountMap.get(sid);
                    if (existingPrizes == null) {
                        existingPrizes = new ArrayList<>();
                    }
                    existingPrizes.add(prize);
                    _subscriberIdToNonCashPrizeAmountMap.put(sid, existingPrizes);
                }
            }
        }
    }

    private static class EventResult
    {
        public int game; //eventId
        public String name;
        public String shoutNetworkSectionTitle;

        public List<GrandPrizeWinner> grandPrizeWinner;
        public List<GrandPrizeWinner> grandPrizeWinners;
        public List<ShoutNetworkWinner> shoutNetworkWinners;
        public List<InstantWinner> instantWinners;
        public List<QuestionWinner> questionWinners;
        public List<VipboxWinner> vipboxWinners;
    }

    private static class GrandPrizeWinner
    {
        public int sid; //subscriberId
        public String title;
        public List<String> prizes; //$25.00
        public RecruitedBy recruitedBy;
    }

    private static class ShoutNetworkWinner
    {
        public int sid; //subscriberId
        public List<String> prizes; //2 Jazz tickets
        public String additionalInfo;
    }

    private static class InstantWinner
    {
        public int qid; //questionId
        public List<Integer> sids; //subscriberIds
        public List<String> prizes; //$25
    }

    private static class QuestionWinner
    {
        public int qid; //questionId
        public int sid; //subscriberId
        public List<String> prizes; //$25
    }

    private static class VipboxWinner
    {
        public int id; //vipboxId
        public String name;
        public int score;
        public int numPlayers;
        public List<Winner> winners;
    }

    private static class Winner
    {
        public int sid; //subscriberId
        public int amount; //25
    }

    private static class RecruitedBy
    {
        public int sid; //subscriberId
        public List<String> prizes; //2 Jazz tickets
    }

    private static class xSubscriber
    {
        public int subscriberId;
        public String nickname;
        public String email;
        public String firstname;
        public String lastname;

        public xSubscriber(int subscriberId, String nickname, String email, String firstname, String lastname)
        {
            this.subscriberId = subscriberId;
            this.nickname = nickname;
            this.email = email;
            this.firstname = firstname;
            this.lastname = lastname;
        }
    }

    private void addSubscriberInfoToPayData(int subscriberId)
    throws SQLException
    {
        PreparedStatement ps = null;
        ResultSet rs = null;
        Connection con = _db.getConnection();

        String sql = "SELECT nickname, email, firstname, lastname FROM gameplay.s_subscriber WHERE subscriber_id = ?";

        try {
            ps = con.prepareStatement(sql);

            ps.setInt(1, subscriberId);
            rs = ps.executeQuery();
            boolean wasRow = rs.next();

            if (wasRow) {
                _subscriberIdToxSubscriberMap.put(subscriberId, new xSubscriber(subscriberId, rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4)));
            } else {
                System.out.println("no subscriber found for: " + subscriberId);
                _subscriberIdToxSubscriberMap.put(subscriberId, new xSubscriber(subscriberId, "N/A", "N/A", "N/A", "N/A"));
            }

            rs.close();

//            int count = _counter.incrementAndGet();
//            if (count % 100 == 0) {
//                System.out.print(".");
//            }
//            if (count % 6000 == 0) {
//                System.out.println("\ncount: " + count);
//            }

        } finally {
            if (rs != null) {
                rs.close();
                rs = null;
            }
            if (ps != null) {
                ps.close();
                ps = null;
            }
            if (con != null) {
                con.close();
                //con = null;
            }
        }
    }

    public static void main(String[] args)
    throws Exception
    {
        ShoutClassicStaistics stats = new ShoutClassicStaistics();

        //Map<Integer, Integer> subscriberToXpMap = stats.getSubscriberToXpMap();
        //List<SubscriberXp> subscriberXpList = stats.getSubscriberInfoForXpMap(subscriberToXpMap);
        //stats.exportToCsv(subscriberXpList);

        List<Integer> publicEventIds = stats.getPublicEventIds();
        publicEventIds.forEach(id -> { try {
            stats.parseEventResult(id);
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        } });

        //populate all the subscriber info
        System.out.println("grabbing sub info...");
        for (int subscriberId : stats._subscriberIdToPrizeAmountMap.keySet()) {
            stats.addSubscriberInfoToPayData(subscriberId);
        }
        System.out.println("...grabbed sub info");

        //write it all out to a csv file
        Path path = Paths.get("/Users/shawker/Desktop/shout_winnings_per_player.csv");
        try (BufferedWriter writer = Files.newBufferedWriter(path)) {
            //the column header row
            writer.write("subscriber_id,cash_winnings,nickname,email,firstname,lastname,non_cash_winnings");
            writer.newLine();

            for (int subscriberId : stats._subscriberIdToPrizeAmountMap.keySet()) {
                int cashWinnings = stats._subscriberIdToPrizeAmountMap.get(subscriberId);
                List<String> nonCashWinningsList = stats._subscriberIdToNonCashPrizeAmountMap.get(subscriberId);
                String nonCashWinnings;
                if (nonCashWinningsList == null || nonCashWinningsList.size() == 0) {
                    nonCashWinnings = "";
                } else {
                    nonCashWinnings = StringEscapeUtils.escapeCsv(MessageFormat.format("{0}", nonCashWinningsList));
                }

                xSubscriber s = stats._subscriberIdToxSubscriberMap.get(subscriberId);

                writer.write(MessageFormat.format(
                        "{0,number,#},{1,number,#},{2},{3},{4},{5},{6}",
                        subscriberId, cashWinnings, StringEscapeUtils.escapeCsv(s.nickname), StringEscapeUtils.escapeCsv(s.email),
                        StringEscapeUtils.escapeCsv(s.firstname), StringEscapeUtils.escapeCsv(s.lastname), nonCashWinnings));
                writer.newLine();
            }
        }

        stats.stop();
    }
}
