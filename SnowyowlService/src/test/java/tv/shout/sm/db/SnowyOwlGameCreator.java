package tv.shout.sm.db;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import tv.shout.sc.domain.Game;
import tv.shout.sc.domain.Round;
import tv.shout.sm.admin.AdminGame;
import tv.shout.snowyowl.service.ISnowyowlService;

/**
 * This will create a game specifically crafted to work with the Shout Millionaire service and engines. Many other types of games are possible using different configurations.
 */
public class SnowyOwlGameCreator
{
    private static Logger _logger = Logger.getLogger(SnowyOwlGameCreator.class);

    public static AdminGame getGame(Map<String, String> questionCategoriesReverseMap, boolean useTheUsualValues)
    throws Exception
    {
        AdminGame game = getGameFromConsoleInput(useTheUsualValues);

        List<Round> rounds = getRoundsForGameFromConsoleInput(game, questionCategoriesReverseMap, useTheUsualValues);
        game.rounds = rounds;

        return game;
    }

    private static AdminGame getGameFromConsoleInput(boolean useTheUsualValues)
    throws ParseException
    {
        //create game with known values hardcoded
        AdminGame game = new AdminGame();
        game.setId(UUID.randomUUID().toString());
        game.setGameEngine(ISnowyowlService.GAME_ENGINE);
        //gamePhotoUrl=null
        game.setGameStatus(Game.GAME_STATUS.PENDING);
        game.setForbiddenCountryCodes(new HashSet<>());
        game.setAllowBots(true);
        game.setProductionGame(false);

        String val;

        //grab which language[s] will be supported
        if (useTheUsualValues) {
            game.setAllowableLanguageCodes(new HashSet<>(Arrays.asList("en")));
        } else {
            val = BaseDbSupport.getConsoleInput("supported language codes (comma delimited): ");
            List<String> supportedLanguageCodes = Arrays.asList(val.split(","));
            Set<String> allowableLanguageCodes = new HashSet<>(supportedLanguageCodes);
            game.setAllowableLanguageCodes(allowableLanguageCodes);
        }

        //grab allowable app ids
        if (useTheUsualValues) {
            game.setAllowableAppIds(new HashSet<>(Arrays.asList(6)));
        } else {
            Set<Integer> allowableAppIds = new HashSet<>();
            val = BaseDbSupport.getConsoleInput("supported app ids (comma delimited [6=SnowyOwl]): ");
            String[] vals = val.split(",");
            for (String s : vals) {
                allowableAppIds.add(Integer.parseInt(s));
            }
            game.setAllowableAppIds(allowableAppIds);
        }

        //game name
        Map<String, String> gameNames = new HashMap<>();
        for (String languageCode : game.getAllowableLanguageCodes()) {
            val = BaseDbSupport.getConsoleInput("game name ("+languageCode+"): ");
            gameNames.put(languageCode, val);
        }
        game.setGameNames(gameNames);

        //game description
        Map<String, String> gameDescriptions = new HashMap<>();
        if (useTheUsualValues) {
            gameDescriptions.put("en", "this is the game descrpition");
        } else {
            for (String languageCode : game.getAllowableLanguageCodes()) {
                val = BaseDbSupport.getConsoleInput("game description ("+languageCode+"): ");
                gameDescriptions.put(languageCode, val);
            }
        }
        game.setGameDescriptions(gameDescriptions);

        //photo url
        if (useTheUsualValues) {
            game.setGamePhotoUrl(null);
        } else {
            val = BaseDbSupport.getConsoleInput("game image (must already have been uploaded) (''=none): ");
            if (val == null || val.trim().length() == 0) {
                game.setGamePhotoUrl(null);
            } else {
                game.setGamePhotoUrl(val);
            }
        }

        //guide url
        val = BaseDbSupport.getConsoleInput("guide html: ");
        if (val.trim().length() > 0) {
            game.setGuideUrl(val);
        }

        //fetching activity titles
        Map<String, String> fetchingActivityTitles = new HashMap<>();
        if (useTheUsualValues) {
            fetchingActivityTitles.put("en", "Retrieving Questions");
        } else {
            for (String languageCode : game.getAllowableLanguageCodes()) {
                val = BaseDbSupport.getConsoleInput("fetching activity title (ex: Retrieving Question) ("+languageCode+"): ");
                fetchingActivityTitles.put(languageCode, val);
            }
        }
        game.setFetchingActivityTitles(fetchingActivityTitles);

        //submitting answer titles
        Map<String, String> submittingActivityTitles = new HashMap<>();
        if (useTheUsualValues) {
            submittingActivityTitles.put("en", "Submitting Answer");
        } else {
            for (String languageCode : game.getAllowableLanguageCodes()) {
                val = BaseDbSupport.getConsoleInput("submitting activity title (ex: Submitting Answer) ("+languageCode+"): ");
                submittingActivityTitles.put(languageCode, val);
            }
        }
        game.setSubmittingActivityTitles(submittingActivityTitles);

        //include answers
        if (useTheUsualValues) {
            game.setIncludeActivityAnswersBeforeScoring(true);
        } else {
            val = BaseDbSupport.getConsoleInput("include answers when asking questions (y/n): ");
            game.setIncludeActivityAnswersBeforeScoring(val.toLowerCase().startsWith("y"));
        }

        //# of lives
        if (useTheUsualValues) {
            game.setBracketEliminationCount(1);
        } else {
            int tournamentLifeCount = Integer.parseInt(BaseDbSupport.getConsoleInput("# of lives for tournament play (1=single elimination, 2=double elimination, etc): "));
            while (tournamentLifeCount < 1) {
                _logger.info("minimum life count is 1");
                tournamentLifeCount = Integer.parseInt(BaseDbSupport.getConsoleInput("# of lives for tournament play (1=single elimination, 2=double elimination, etc): "));
            }
            game.setBracketEliminationCount(tournamentLifeCount);
        }

        //game engine
        if (useTheUsualValues) {
            game.setEngineType(ISnowyowlService.GAME_ENGINE_TYPE_FixedRound_SingleLife);
        } else {
            while (game.getEngineType() == null) {
                val = BaseDbSupport.getConsoleInput("Engine Type (VARIABLE_ROUND): ");
                try {
                    game.setEngineType(val);
                } catch (Exception ignored) { }
            }
        }

        //fill with bots
        if (useTheUsualValues) {
            game.setFillWithBots(true);
        } else {
            val = BaseDbSupport.getConsoleInput("fillWithBots (true/false): ");
            game.setFillWithBots(Boolean.parseBoolean(val));
        }

        //pair immediately
        if (useTheUsualValues) {
            game.setPairImmediately(false);
        } else {
            val = BaseDbSupport.getConsoleInput("pairImmediately (true/false): ");
            game.setPairImmediately(Boolean.parseBoolean(val));
        }

        return game;
    }

    private static List<Round> getRoundsForGameFromConsoleInput(
            AdminGame game, Map<String, String> questionCategoriesReverseMap, boolean useTheUsualValues)
    {
        String gameId = game.getId();

        Set<String> languageCodes = game.getAllowableLanguageCodes();

        int numRounds = Integer.parseInt(BaseDbSupport.getConsoleInput("# of POOL rounds (tourny rounds added automatically): "));

        int matchPlayerCount = 2;

        int maxPlayerCount = Integer.parseInt(BaseDbSupport.getConsoleInput("maxPlayerCount: "));
        while (maxPlayerCount < 2 || maxPlayerCount % 2 != 0) {
            _logger.info("maxPlayerCount must be an even number >= 2");
            maxPlayerCount = Integer.parseInt(BaseDbSupport.getConsoleInput("maxPlayerCount: "));
        }

        int secondsBetweenQuestions;
        if (useTheUsualValues) {
            secondsBetweenQuestions = 10;
        } else {
            secondsBetweenQuestions = Integer.parseInt(BaseDbSupport.getConsoleInput("# of seconds between questions: "));
        }

        List<Round> rounds = new ArrayList<>(numRounds);
        for (int i=0; i<numRounds; i++) {
            rounds.add(getPoolRoundFromConsoleInput(gameId, i+1, languageCodes,
                    matchPlayerCount, maxPlayerCount,
                    questionCategoriesReverseMap, secondsBetweenQuestions, useTheUsualValues));
        }

        Round bracketRound = getBracketRoundFromConsoleInput(
                gameId, numRounds+1, languageCodes, matchPlayerCount, maxPlayerCount, questionCategoriesReverseMap, secondsBetweenQuestions,
                useTheUsualValues);

        rounds.add(bracketRound);

        return rounds;
    }

    private static Round getPoolRoundFromConsoleInput(
            String gameId, int roundSequence, Set<String> languageCodes,
            int matchPlayerCount, int maxPlayerCount, Map<String, String> questionCategoriesReverseMap, int secondsBetweenQuestions,
            boolean useTheUsualValues)
    {
        _logger.info("\n\nEntering data for Round #" + roundSequence);

        Round round = new Round();

        round.setId(UUID.randomUUID().toString());
        round.setGameId(gameId);
        round.setRoundStatus(Round.ROUND_STATUS.PENDING);
        round.setRoundSequence(roundSequence);
        round.setFinalRound(false);
        //round.setCostPerPlayer(cost); //set via PayoutModel
        //round.setActivityMinimumDifficulty(activityMinimumDifficulty);
        //round.setActivityMinimumDifficulty(activityMinimumDifficulty);
        round.setMatchGlobal(false); //i.e. each match created for this round gets a 1:1 wds doc for the players involved
        round.setDurationBetweenActivitiesSeconds(secondsBetweenQuestions);
        round.setRoundType(Round.ROUND_TYPE.POOL);
        round.setMatchPlayerCount(matchPlayerCount);

        String val;

        //roundNames
        Map<String, String> roundNames = new HashMap<>();
        if (useTheUsualValues) {
            roundNames.put("en", "Round " + roundSequence);
        } else {
            for (String languageCode : languageCodes) {
                val = BaseDbSupport.getConsoleInput("round name ("+languageCode+"): ");
                roundNames.put(languageCode, val);
            }
        }
        round.setRoundNames(roundNames);

        //categories
        if (useTheUsualValues) {
            round.setCategories(new HashSet<>(Arrays.asList("*")));
        } else {
            boolean atLeastOneCategoryFound = false;
            Set<String> categoryUuids = new HashSet<>();
            val = BaseDbSupport.getConsoleInput("Categories (comma-delimited): *, " + questionCategoriesReverseMap.entrySet().stream()
                .map(m -> m.getKey())
                .collect(Collectors.joining(",")) + ": ");
            String[] vals = val.split(",");
            for (String key : vals) {
                if (questionCategoriesReverseMap.containsKey(key)) {
                    atLeastOneCategoryFound = true;
                    categoryUuids.add(questionCategoriesReverseMap.get(key));
                } else if ("*".equals(key)) {
                    atLeastOneCategoryFound = true;
                    categoryUuids.add(key);
                }
            }
            if (!atLeastOneCategoryFound) throw new IllegalStateException("At least one category must be specified");
            round.setCategories(categoryUuids);
        }

        round.setMaximumPlayerCount(maxPlayerCount);

//        int minMatchCount = Integer.parseInt(BaseDbSupport.getConsoleInput("minMatchCount (# of matches a player must win to 'move on'): "));
        int minMatchCount = 1;
        round.setMinimumMatchCount(minMatchCount);

        Integer maxMatchCount;
//        val = BaseDbSupport.getConsoleInput("maxMatchCount (if a player loses this many matches without winning, give up, ''==no limit): ");
//        if (val == null || val.trim().length() == 0)
//            maxMatchCount = null;
//        else
//            maxMatchCount = Integer.parseInt(val);
        maxMatchCount = null;
        round.setMaximumMatchCount(maxMatchCount);

        round.setRoundActivityType("Trivia");
        round.setRoundActivityValue("n/a");

        //how many correct to win
        if (useTheUsualValues) {
            round.setMinimumActivityToWinCount(2);
        } else {
            int minActivityToWinCount = Integer.parseInt(BaseDbSupport.getConsoleInput("# of activities needed to win a match (i.e. get 3 questions right): "));
            round.setMinimumActivityToWinCount(minActivityToWinCount);
        }

        //max questions to ask before giving up
        if (useTheUsualValues) {
            round.setMaximumActivityCount(3);
        } else {
            Integer maxActivityCount;
            val = BaseDbSupport.getConsoleInput("max activities to lose before giving up (i.e. if they play more than 5 and don't win, they lose, ''=no limit): ");
            if (val == null || val.trim().length() == 0)
                maxActivityCount = null;
            else
                maxActivityCount = Integer.parseInt(val);
            round.setMaximumActivityCount(maxActivityCount);
        }

        //max seconds before question ACK timeout
        if (useTheUsualValues) {
            round.setActivityMaximumDurationSeconds(10);
        } else {
            Integer activityMaxDurationSeconds;
            val = BaseDbSupport.getConsoleInput("max seconds before question ACK timeout (if player client doesn't acknowledge receiving activity after x seconds, player loses): ");
            activityMaxDurationSeconds = Integer.parseInt(val);
            round.setActivityMaximumDurationSeconds(activityMaxDurationSeconds);
        }

        //max seconds before answer timeout
        if (useTheUsualValues) {
            round.setPlayerMaximumDurationSeconds(10);
        } else {
            Integer playerMaxDurationSeconds;
            val = BaseDbSupport.getConsoleInput("max seconds before answer timeout (if player doesn't answer after x seconds, player loses): ");
            playerMaxDurationSeconds = Integer.parseInt(val);
            round.setPlayerMaximumDurationSeconds(playerMaxDurationSeconds);
        }

        //duration auto-close
        if (useTheUsualValues) {
            round.setMaximumDurationMinutes(null);
        } else {
            Integer maxDurationMinutes;
            val = BaseDbSupport.getConsoleInput("max duration (in minutes) before round auto closes, ''=no limit and will close when all matches complete or manually stopped): ");
            if (val == null || val.trim().length() == 0)
                maxDurationMinutes = null;
            else
                maxDurationMinutes = Integer.parseInt(val);
            round.setMaximumDurationMinutes(maxDurationMinutes);
        }

        return round;
    }

    private static Round getBracketRoundFromConsoleInput(
            String gameId, int roundSequence, Set<String> languageCodes,
            int matchPlayerCount, int maxPlayerCount, Map<String, String> questionCategoriesReverseMap, int secondsBetweenQuestions,
            boolean useTheUsualValues)
    {
        _logger.info("\n\nEntering data for initial tournament round");

        Round round = new Round();

        round.setId(UUID.randomUUID().toString());
        round.setGameId(gameId);
        round.setRoundStatus(Round.ROUND_STATUS.PENDING);
        round.setRoundSequence(roundSequence);
        round.setFinalRound(true); //this will likely be updated to false and another round added, but that is business logic outside the scope of this creation function
        //round.setCostPerPlayer(cost); //coming from the PayoutModel
        //round.setActivityMinimumDifficulty(activityMinimumDifficulty);
        //round.setActivityMinimumDifficulty(activityMinimumDifficulty);
        round.setMatchGlobal(false); //i.e. each match created for this round gets a 1:1 wds doc for the players involved
        round.setDurationBetweenActivitiesSeconds(secondsBetweenQuestions);

        round.setRoundType(Round.ROUND_TYPE.BRACKET);
        round.setMatchPlayerCount(matchPlayerCount);

        String val;

        //roundName prefix
        Map<String, String> roundNames = new HashMap<>();
        if (useTheUsualValues) {
            roundNames.put("en", "Tournament Round 1");
        } else {
            for (String languageCode : languageCodes) {
                val = BaseDbSupport.getConsoleInput("Bracket round name [do not add a number, such as Bracket 1, just say Bracket] '("+languageCode+"): ");
                val += " 1"; //starts with "bracket 1", will auto increment as more are added later
                roundNames.put(languageCode, val);
            }
        }
        round.setRoundNames(roundNames);

        //categories
        if (useTheUsualValues) {
            round.setCategories(new HashSet<>(Arrays.asList("*")));
        } else {
            boolean atLeastOneCategoryFound = false;
            Set<String> categoryUuids = new HashSet<>();
            val = BaseDbSupport.getConsoleInput("Categories (comma-delimited): *, " + questionCategoriesReverseMap.entrySet().stream()
                .map(m -> m.getKey())
                .collect(Collectors.joining(",")) + ": ");
            String[] vals = val.split(",");
            for (String key : vals) {
                if (questionCategoriesReverseMap.containsKey(key)) {
                    atLeastOneCategoryFound = true;
                    categoryUuids.add(questionCategoriesReverseMap.get(key));
                } else if ("*".equals(key)) {
                    atLeastOneCategoryFound = true;
                    categoryUuids.add(key);
                }
            }
            if (!atLeastOneCategoryFound) throw new IllegalStateException("At least one category must be specified");
            round.setCategories(categoryUuids);
        }

        //round.setRoundPurse(purse); //coming from the PayoutModel

        round.setMaximumPlayerCount(maxPlayerCount);

//        int minMatchCount = Integer.parseInt(BaseDbSupport.getConsoleInput("minMatchCount (# of matches a player must win to 'move on'): "));
        int minMatchCount = 1;
        round.setMinimumMatchCount(minMatchCount);

        Integer maxMatchCount;
//        val = BaseDbSupport.getConsoleInput("maxMatchCount (if a player loses this many matches without winning, give up, ''==no limit): ");
//        if (val == null || val.trim().length() == 0)
//            maxMatchCount = null;
//        else
//            maxMatchCount = Integer.parseInt(val);
        maxMatchCount = null;
        round.setMaximumMatchCount(maxMatchCount);

        round.setRoundActivityType("Trivia");
        round.setRoundActivityValue("n/a");

        if (useTheUsualValues) {
            round.setMinimumActivityToWinCount(2);
        } else {
            int minActivityToWinCount = Integer.parseInt(BaseDbSupport.getConsoleInput("# of activities needed to win a match (i.e. get 3 questions right): "));
            round.setMinimumActivityToWinCount(minActivityToWinCount);
        }

        if (useTheUsualValues) {
            round.setMaximumActivityCount(3);
        } else {
            Integer maxActivityCount;
            val = BaseDbSupport.getConsoleInput("max activities to lose before giving up (i.e. if they play more than 5 and don't win, they lose, ''=no limit): ");
            if (val == null || val.trim().length() == 0)
                maxActivityCount = null;
            else
                maxActivityCount = Integer.parseInt(val);
            round.setMaximumActivityCount(maxActivityCount);
        }

        //max seconds before question ACK timeout
        if (useTheUsualValues) {
            round.setActivityMaximumDurationSeconds(10);
        } else {
            Integer activityMaxDurationSeconds;
            val = BaseDbSupport.getConsoleInput("max seconds before question ACK timeout (if player client doesn't acknowledge receiving activity after x seconds, player loses): ");
            activityMaxDurationSeconds = Integer.parseInt(val);
            round.setActivityMaximumDurationSeconds(activityMaxDurationSeconds);
        }

        //max seconds before answer timeout
        if (useTheUsualValues) {
            round.setPlayerMaximumDurationSeconds(10);
        } else {
            Integer playerMaxDurationSeconds;
            val = BaseDbSupport.getConsoleInput("max seconds before answer timeout (if player doesn't answer after x seconds, player loses): ");
            playerMaxDurationSeconds = Integer.parseInt(val);
            round.setPlayerMaximumDurationSeconds(playerMaxDurationSeconds);
        }

        //auto close
        if (useTheUsualValues) {
            round.setMaximumDurationMinutes(null);
        } else {
            Integer maxDurationMinutes;
            val = BaseDbSupport.getConsoleInput("max duration (in minutes) before round auto closes, ''=no limit and will close when all matches complete or manually stopped): ");
            if (val == null || val.trim().length() == 0)
                maxDurationMinutes = null;
            else
                maxDurationMinutes = Integer.parseInt(val);
            round.setMaximumDurationMinutes(maxDurationMinutes);
        }

        return round;
    }

}
