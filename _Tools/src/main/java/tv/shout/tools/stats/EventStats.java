package tv.shout.tools.stats;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.google.gson.Gson;

public class EventStats
{
    private static final int POOL_SIZE = 8;
    
    private ShoutDbProvider _db = new ShoutDbProvider(ShoutDbProvider.DB.PRODUCTION);
    private Lock _concurrentLock = new ReentrantLock();

    public void close()
    {
        _db.close();
    }
    
    public List<Integer> getEventIdsForAllActualEventsThatWerePlayedByRealPeopleAndNotJustTestEvents() 
    throws SQLException
    {
        //this is a list of events to ignore. valid as of this date: 9 mar 2017
        Set<Integer> invalidEventIdsThatShouldNotBeConsidered = new HashSet<>(Arrays.asList(
            1058, 913, 916, 917, 922, 938, 2285, 2355, 2364
        ));
        
        //this is a good sql statement to run beforehand in a mysql tool to be able to add items to the filter list
        /*
        select e.event_id, e.name, e.actual_start_date, count(distinct(sa.subscriber_id))
          from gameplay.subscriber_answer sa, gameplay.event e
         where sa.event_id = e.event_id
           # this will filter to only real madrid events# and e.event_id in (select e.event_id from gameplay.event e, gameplay.event_app ea where e.event_id = ea.event_id and ea.app_id = 2)
           and e.private_evt = 0
           and e.actual_start_date > '2017-03-09'
         group by e.event_id
         order by e.actual_start_date;
       */
        
        List<Integer> eventIds = new ArrayList<>();
        
        String sql = 
            "SELECT e.event_id " +
            "  FROM gameplay.event e " +
            " WHERE e.private_evt = 0";
        
        Connection con = null;
        Statement s = null;
        ResultSet rs = null;
        try {
            con = _db.getConnection();
            
            s = con.createStatement();
            rs = s.executeQuery(sql);
            while (rs.next()) {
                int eventId = rs.getInt(1);
                if (!invalidEventIdsThatShouldNotBeConsidered.contains(eventId)) {
                    eventIds.add(eventId);
                }
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
            }
        }
        
        return eventIds;
    }

    private void processEventForActivePlayers(CountDownLatch cdl, int eventId, int numQuestionRequiredToAnswerToBeConsideredActive, Map<Integer, Integer> resultMap)
    throws SQLException
    {
        String sql = 
            "select count(subscriber_id) " + 
            "  from gameplay.subscriber_answer " + 
            " where event_id = ? " +
            " group by subscriber_id";
        
        PreparedStatement ps = null;
        ResultSet rs = null;
        Connection con = _db.getConnection();
        try {
            ps = con.prepareStatement(sql);
            
            ps.setInt(1, eventId);
            
            int activePlayerCount = 0;
            rs = ps.executeQuery();
            while (rs.next()) {
                int answerCountForSubscriber = rs.getInt(1);
                if (answerCountForSubscriber >= numQuestionRequiredToAnswerToBeConsideredActive) {
                    activePlayerCount++;
                }
            }
            
            _concurrentLock.lock();
            try {
                resultMap.put(eventId, activePlayerCount);
                System.out.println(eventId + ": " + activePlayerCount);
            } finally {
                _concurrentLock.unlock();
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
            }
        }
        
        cdl.countDown();
    }
    
    /*
     * @return
     *  key=eventId
     *  val=# of active players
     */
    public void calculateNumberOfActivePlayersPerEvent(List<Integer> eventIds, int numQuestionRequiredToAnswerToBeConsideredActive)
    throws SQLException
    {
        Map<Integer, Integer> resultMap = new HashMap<>();
        
        final CountDownLatch cdl = new CountDownLatch(eventIds.size());
        final ExecutorService threadPool = Executors.newFixedThreadPool(POOL_SIZE);
        
        for (final int eventId : eventIds) {
            threadPool.execute(new Runnable() {
                @Override
                public void run()
                {
                    try {
                        processEventForActivePlayers(cdl, eventId, numQuestionRequiredToAnswerToBeConsideredActive, resultMap);
                    } catch (SQLException e) {
                        cdl.countDown();
                        e.printStackTrace();
                    }
                }
            });
        }
        
        //wait until all processing threads have completed
        try {
            cdl.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        threadPool.shutdown();
        
        long totalPlayersInAllEvents = 0;
        for (int eventId : resultMap.keySet()) {
            int activePlayerCountForEvent = resultMap.get(eventId);
            totalPlayersInAllEvents+= activePlayerCountForEvent;
        }
        double averageNumberOfPlayersPerEvent = (double)totalPlayersInAllEvents / (double) eventIds.size();
        System.out.println("average # of players per event: " + averageNumberOfPlayersPerEvent);
    }
    
    private void getActiveSubscriberIdsForEvent(CountDownLatch cdl, int eventId, int numQuestionRequiredToAnswerToBeConsideredActive, Map<Integer, List<Integer>> resultMap)
    throws SQLException
    {
        List<Integer> activeSubscriberIdList = new ArrayList<>();
        System.out.println("finding active subscriber id's for eventId: " + eventId);
        
        String sql = 
            "select subscriber_id, count(subscriber_id) " + 
            "  from gameplay.subscriber_answer " + 
            " where event_id = ? " +
            " group by subscriber_id";

        PreparedStatement ps = null;
        ResultSet rs = null;
        Connection con = _db.getConnection();
        try {
            ps = con.prepareStatement(sql);
            
            ps.setInt(1, eventId);
            
            rs = ps.executeQuery();
            while (rs.next()) {
                int subscriberId = rs.getInt(1);
                int answerCountForSubscriber = rs.getInt(2);
                if (answerCountForSubscriber >= numQuestionRequiredToAnswerToBeConsideredActive) {
                    activeSubscriberIdList.add(subscriberId);
                }
            }
            
            _concurrentLock.lock();
            try {
                resultMap.put(eventId, activeSubscriberIdList);
            } finally {
                _concurrentLock.unlock();
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
            }
        }
        
        cdl.countDown();
    }
    
    public void calculateTotalRevenueOfPowerupPurchases()
    throws SQLException
    {
        Map<String, Integer> amountsByUuid = new HashMap<>();
        Map<String, Double> pricesByUuid = new HashMap<>();
        
        String sqlAmounts =
            "select r.item_uuid, count(r.item_uuid) " +
            "  from store.receipt r " +
            " where r.`type` <> 'INTERNAL' " +
            " group by r.item_uuid";
        
        String sqlPrices = "select uuid, price from store.item";
                
        Connection con = null;
        Statement s = null;
        ResultSet rs = null;
        try {
            con = _db.getConnection();
            
            //get the amounts
            s = con.createStatement();
            rs = s.executeQuery(sqlAmounts);
            while (rs.next()) {
                amountsByUuid.put(rs.getString(1), rs.getInt(2));
            }
            rs.close();
            
            //get the prices
            rs = s.executeQuery(sqlPrices);
            while (rs.next()) {
                pricesByUuid.put(rs.getString(1), rs.getDouble(2));
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
            }
        }
        
        double total = 0d;
        for (String itemUuid : amountsByUuid.keySet()) {
            int amount = amountsByUuid.get(itemUuid);
            double price = pricesByUuid.get(itemUuid);
            
            total += (double) amount * price;
        }
        
        System.out.println("total revenue from all powerups purchased ever: $" + total);
        
    }
    
    //needed by Gson
    private class PowerupUsedPayload
    {
        @SuppressWarnings("unused") public long questionId;
        public int eventId;
        @SuppressWarnings("unused") public String subscriberEntitlementUuid;
        @SuppressWarnings("unused") public String powerupType;
    }
    
    private void getPowerupUsageCountFromActivePlayerList(
        CountDownLatch cdl, int eventId, int numPowerupsRequiredToUseToBeConsideredActive, List<Integer> activeSubscriberIdList, Map<Integer, Object[]> resultMap)
    throws SQLException
    {
        String sqlThrowdowns = "SELECT count(*) FROM gameplay.sync_messages WHERE message_type = 'throwdownCreated' AND subscriber_id = ? AND context_id = " + eventId;
        String sqlSafetyNets = "SELECT count(*) FROM gameplay.sync_messages WHERE message_type = 'safetyNetActive' AND subscriber_id = ? AND context_id = " + eventId;
        String sqlOtherPowerups = "SELECT payload FROM gameplay.sync_messages WHERE message_type = 'POWERUP_USED' AND subscriber_id = ?";

        PreparedStatement psThrowdowns = null;
        PreparedStatement psSafetyNets = null;
        PreparedStatement psOtherPowerups = null;
        ResultSet rs = null;
        Connection con = _db.getConnection();
        Gson gson = new Gson();
        
        int numberWhoUsedAny = 0;
        int numberWhoUsedX = 0;
        
        try {
            psThrowdowns = con.prepareStatement(sqlThrowdowns);
            psSafetyNets = con.prepareStatement(sqlSafetyNets);
            psOtherPowerups = con.prepareStatement(sqlOtherPowerups);
            
            //loop each subscriber and get the total number of powerups used for the given event
            for (int subscriberId : activeSubscriberIdList) {
                //how many throwdowns
                int totalPowerupsUsed = 0;
                psThrowdowns.setInt(1, subscriberId);
                rs = psThrowdowns.executeQuery();
                rs.next();
                totalPowerupsUsed += rs.getInt(1);
                rs.close();
                
                //how many safetynets
                psSafetyNets.setInt(1, subscriberId);
                rs = psSafetyNets.executeQuery();
                rs.next();
                totalPowerupsUsed += rs.getInt(1);
                rs.close();
                
                //how many of the other types
                psOtherPowerups.setInt(1, subscriberId);
                rs = psOtherPowerups.executeQuery();
                while (rs.next()) {
                    String payload = rs.getString(1);
                    PowerupUsedPayload pup = gson.fromJson(payload, PowerupUsedPayload.class);
                    if (pup.eventId == eventId) {
                        totalPowerupsUsed++;
                    }
                }
                rs.close();
                
                if (totalPowerupsUsed > 0) {
                    numberWhoUsedAny++;
                }
                if (totalPowerupsUsed >= numPowerupsRequiredToUseToBeConsideredActive) {
                    numberWhoUsedX++;
                }
            }
            
            _concurrentLock.lock();
            try {
                resultMap.put(eventId, new Object[] {numberWhoUsedAny, numberWhoUsedX});
            } finally {
                _concurrentLock.unlock();
            }
            
            
        } finally {
            if (rs != null) {
                rs.close();
                rs = null;
            }
            if (psThrowdowns != null) {
                psThrowdowns.close();
                psThrowdowns = null;
            }
            if (psSafetyNets != null) {
                psSafetyNets.close();
                psSafetyNets = null;
            }
            if (psOtherPowerups != null) {
                psOtherPowerups.close();
                psOtherPowerups = null;
            }
            if (con != null) {
                con.close();
            }
        }
        
        cdl.countDown();
    }
    
    public void calculatePercentageOfActivePlayersUsingPowerups(List<Integer> eventIds, int numQuestionRequiredToAnswerToBeConsideredActive, int numPowerupsRequiredToUseToBeConsideredActive)
    {
        Map<Integer, List<Integer>> activeSubscriberIdListByEvent = new HashMap<>();
        
        final CountDownLatch cdl = new CountDownLatch(eventIds.size());
        final ExecutorService threadPool = Executors.newFixedThreadPool(POOL_SIZE);
        
        for (final int eventId : eventIds) {
            threadPool.execute(new Runnable() {
                @Override
                public void run()
                {
                    try {
                        getActiveSubscriberIdsForEvent(cdl, eventId, numQuestionRequiredToAnswerToBeConsideredActive, activeSubscriberIdListByEvent);
                    } catch (SQLException e) {
                        cdl.countDown();
                        e.printStackTrace();
                    }
                }
            });
        }
        
        //wait until all processing threads have completed
        try {
            cdl.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        threadPool.shutdown();

        //find total# of active players
        long totalPlayersInAllEvents = 0;
        for (int eventId : activeSubscriberIdListByEvent.keySet()) {
            int activePlayerCountForEvent = activeSubscriberIdListByEvent.get(eventId).size();
            totalPlayersInAllEvents+= activePlayerCountForEvent;
        }

        //now that we have this list, find how many of the people in each list for each event used powerups
        Map<Integer, Object[]> resultMap = new HashMap<>();
        
        final CountDownLatch cdl2 = new CountDownLatch(eventIds.size());
        final ExecutorService threadPool2 = Executors.newFixedThreadPool(POOL_SIZE);

        for (final int eventId : eventIds) {
            threadPool2.execute(new Runnable() {
                @Override
                public void run()
                {
                    try {
                        getPowerupUsageCountFromActivePlayerList(
                            cdl2, eventId, numPowerupsRequiredToUseToBeConsideredActive, activeSubscriberIdListByEvent.get(eventId), resultMap);
                    } catch (SQLException e) {
                        cdl2.countDown();
                        e.printStackTrace();
                    }
                    System.out.println("# of latches remaining: " + cdl2.getCount());
                }
            });
        }
        
        //wait until all processing threads have completed
        try {
            cdl2.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        threadPool2.shutdown();
        
        //calculate the results
        long totalAtLeastOneUsePowerupUsers = 0;
        long totalAtLeastXPowerupUsers = 0;
        
        for (int eventId : resultMap.keySet()) {
            Object[] data = resultMap.get(eventId);
            int numberWhoUsedAny = (int) data[0];
            int numberWhoUsedX = (int) data[1];
            
            totalAtLeastOneUsePowerupUsers += numberWhoUsedAny;
            totalAtLeastXPowerupUsers += numberWhoUsedX;
        }
        
        double percentageToUseAny = (double) totalAtLeastOneUsePowerupUsers / (double) totalPlayersInAllEvents;
        double percentageToUseX = (double) totalAtLeastXPowerupUsers / (double) totalPlayersInAllEvents;
        
        System.out.println("% of active users who used a powerup: " + percentageToUseAny);
        System.out.println("% of active users who used powerups across the event: " + percentageToUseX);
    }
    
    public static void main(String[] args) 
    throws SQLException
    {
        EventStats stats = new EventStats();
        
        //List<Integer> eventIds = stats.getEventIdsForAllActualEventsThatWerePlayedByRealPeopleAndNotJustTestEvents();
        
        //find the average # of active players per event
        //stats.calculateNumberOfActivePlayersPerEvent(eventIds, 3);
        
        //find the percentage of active players using powerups (at least once and across the event)
        //stats.calculatePercentageOfActivePlayersUsingPowerups(eventIds, 3, 3);
        
        //find the total revenue of all powerup purches
        stats.calculateTotalRevenueOfPowerupPurchases();
        
        stats.close();
        
    }
}
