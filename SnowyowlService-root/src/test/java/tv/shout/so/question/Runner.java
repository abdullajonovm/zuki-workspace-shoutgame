package tv.shout.so.question;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import tv.shout.sc.domain.MatchPlayer;
import tv.shout.sc.service.IShoutContestService;
import tv.shout.sm.db.DbProvider;
import tv.shout.snowyowl.domain.Question;
import tv.shout.snowyowl.domain.SubscriberRoundQuestion;
import tv.shout.snowyowl.engine.QuestionSupplierRandomSupplier;

public class Runner
{
    private static DbProvider.DB WHICH = DbProvider.DB.NC11_1;
    private static final String CATEGORY_UUID_UTAHJAZZ = "3d7e0fab-3f86-41c9-803f-99aacb8d5ab6";
    private static final String CATEGORY_UUID_ZIONSBANK = "f54ecca9-4026-4d14-b4e6-ea8e7c9b9fa1";

    private static final String GAME_ID = "da009c11-37bc-4343-96f6-d9ade174d94e";
    private static final String POOL1_ID = "b93286c1-c745-46b5-9494-65e6a269c756";
    private static final String POOL2_ID = "2b79258f-2d5a-4135-b49a-0d153d812885";
    private static final String BRACKET_ID = "054b7002-df80-419c-be19-f8dfea27037f";

    private void printTable(MockSnowyowlDaoMapper snowyowlDao, String gameId, String roundId, List<Integer> subscriberIds)
    {
        List<String> gameRoundQuestionIdsInOrder = snowyowlDao.getGameRoundQuestionIds(gameId, roundId);
        Map<Integer, List<SubscriberRoundQuestion>> tableData = new HashMap<>();
        for (int subscriberId : subscriberIds) {
            List<SubscriberRoundQuestion> srqs = snowyowlDao.getSubscriberRoundQuestions(subscriberId, roundId);
            tableData.put(subscriberId, srqs);
        }
        printTableRefactor(subscriberIds, gameRoundQuestionIdsInOrder, tableData);
    }

    private void printTableRefactor(List<Integer> subscriberIds, List<String> gameRoundQuestionIdsInOrder, Map<Integer, List<SubscriberRoundQuestion>> tableData)
    {
        //print header
        StringBuilder buf = new StringBuilder();

        buf.append("        |"); //this is where the questionId will be for each row

        //this assumes each subscriberId is less than 100,000 (which is fine for now)
        for (int subscriberId : subscriberIds) {
            if (subscriberId < 10) {
                buf.append("    ");
            } else if (subscriberId < 100) {
                buf.append("   ");
            } else if (subscriberId < 1000) {
                buf.append("  ");
            } else if (subscriberId < 10000) {
                buf.append(" ");
            }
            buf.append(subscriberId).append("|");
        }
        buf.append("\n");

        //print each row
        for (int i=0; i<gameRoundQuestionIdsInOrder.size(); i++) {
            //print questionId
            String questionId = gameRoundQuestionIdsInOrder.get(i);
            buf.append(questionId.substring(0, 3)).append("/").append(questionId.substring(33)).append(" |");

            //for each subscriber on that question, print the seen value
            for (int subscriberId : subscriberIds) {
                SubscriberRoundQuestion srq = tableData.get(subscriberId).get(i);
                buf.append("  ").append(srq.isSeen() ? "x" : " ").append("  |");
            }

            buf.append("\n");
        }

        System.out.println(buf);
    }

    public static void main(String[] args)
    {
        Runner runner = new Runner();

        Set<String> allowedLanguageCodes = new HashSet<>(Arrays.asList("en"));
        int minDifficulty = 0;
        int maxDifficulty = 10;
        Set<Long> botIds = new HashSet<>(Arrays.asList(11L, 12L));

        MockShoutContestServiceDaoMapper shoutContestDao = new MockShoutContestServiceDaoMapper(WHICH);
        MockSnowyowlDaoMapper snowyowlDao = new MockSnowyowlDaoMapper(WHICH);
        IShoutContestService shoutContestService = new MockShoutContestService(shoutContestDao);
        QuestionSupplierRandomSupplier randomSupplier = new MockRandomSupplier();
        TestQuestionSupplier questionSupplier = new TestQuestionSupplier(snowyowlDao, shoutContestService, randomSupplier);

        //hard code some categories
        Set<String> round1CategoryUuids = new HashSet<>(Arrays.asList(CATEGORY_UUID_UTAHJAZZ));
        Set<String> round2CategoryUuids = new HashSet<>(Arrays.asList(CATEGORY_UUID_ZIONSBANK));
        Set<String> round3CategoryUuids = new HashSet<>(Arrays.asList(CATEGORY_UUID_UTAHJAZZ, CATEGORY_UUID_ZIONSBANK));

        //TODO: for each round, create a bunch of matches to pass in to the method and see what comes out the other side
        //List<MatchPlayer> players

        MatchPlayer p1r1 = new MatchPlayer(GAME_ID, POOL1_ID, null, null, 1);
        MatchPlayer p2r1 = new MatchPlayer(GAME_ID, POOL1_ID, null, null, 2);
        List<MatchPlayer> players = Arrays.asList(p1r1, p2r1);
        Question q = questionSupplier.getQuestion(players, botIds, allowedLanguageCodes, minDifficulty, maxDifficulty);

        System.out.println("ROUND 1 - POOL");
        runner.printTable(snowyowlDao, GAME_ID, POOL1_ID, Arrays.asList(1,2));

        System.out.println("ROUND 2 - POOL");
        runner.printTable(snowyowlDao, GAME_ID, POOL2_ID, Arrays.asList(1,2));

        System.out.println("ROUND 3+ - BRACKET");
        runner.printTable(snowyowlDao, GAME_ID, BRACKET_ID, Arrays.asList(1,2));

//        List<Integer> subscriberIds = Arrays.asList(56, 1012);
//        List<String> gameRoundQuestionIdsInOrder = Arrays.asList("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa", "bbbaaaaa-aaaa-aaaa-aaaa-aaaaaaaaabbb");
//
//        List<SubscriberRoundQuestion> s1Srqs = Arrays.asList(
//            new SubscriberRoundQuestion(56, "r1", "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa", 0, true),
//            new SubscriberRoundQuestion(56, "r1", "bbbaaaaa-aaaa-aaaa-aaaa-aaaaaaaaabbb", 0, false)
//        );
//
//        List<SubscriberRoundQuestion> s2Srqs = Arrays.asList(
//            new SubscriberRoundQuestion(1012, "r1", "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa", 0, false),
//            new SubscriberRoundQuestion(1012, "r1", "bbbaaaaa-aaaa-aaaa-aaaa-aaaaaaaaabbb", 0, true)
//        );
//
//        Map<Integer, List<SubscriberRoundQuestion>> tableData = new HashMap<>();
//        tableData.put(56, s1Srqs);
//        tableData.put(1012, s2Srqs);
//        runner.printTableRefactor(subscriberIds, gameRoundQuestionIdsInOrder, tableData);
    }

}
