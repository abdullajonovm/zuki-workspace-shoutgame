package tv.shout.sm.test;

import static com.meinc.webcollector.message.CollectorMessage.PARM_TO_WDS;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import tv.shout.collector.PublishResponseError;
import tv.shout.util.JsonUtil;

public class Scratch
{
    private static Logger _logger = Logger.getLogger(Scratch.class);
    private ObjectMapper _jsonMapper = JsonUtil.getObjectMapper();

    public Scratch()
    {
    }

    public <T extends Object> T getJsonObjectFromProps(Map<String, String> props, String messageId, String docType, String name, boolean isRequired, TypeReference<T> typeRef)
    throws PublishResponseError
    {
        if (!props.containsKey(name) || props.get(name) == null) {
            if (isRequired) {
                throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, docType, false, "missingRequiredParam", name);
            } else {
                return null;
            }
        }

        try {
            return _jsonMapper.readValue(props.get(name), typeRef);
        } catch (IOException e) {
            _logger.error("unable to parse", e);
            throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, docType, false, "unexpectedError", name);
        }
    }

//    public void parse() throws PublishResponseError
//    {
//        String gameJsonStr = "{\"id\":\"3c357795-07c7-4f69-8d1e-d7bfe94ae5e6\",\"gameEngine\":\"SHOUT_MILLIONAIRE\",\"gameStatus\":\"PENDING\",\"forbiddenCountryCodes\":[],\"createDate\":\"2017-06-05T21:15:13.496-06:00\",\"allowBots\":true,\"allowableLanguageCodes\":[\"en\"],\"allowableAppIds\":[1],\"expectedStartDate\":\"2017-01-01T00:00:00.000-06:00\",\"bracketEliminationCount\":1,\"payoutCalculationMethod\":\"STATIC\",\"payoutHouseTakePercentage\":0,\"payoutPercentageOfUsersToAward\":59,\"gameNames\":{\"en\":\"ggg\"},\"fetchingActivityTitles\":{\"en\":\"aaa\"},\"submittingAnswerTitles\":{\"en\":\"aaa\"},\"rounds\":[{\"id\":\"e6178fe9-17ea-4ec8-b296-ca8479db1fe4\",\"gameId\":\"3c357795-07c7-4f69-8d1e-d7bfe94ae5e6\",\"roundStatus\":\"PENDING\",\"roundSequence\":1,\"finalRound\":false,\"costPerPlayer\":1,\"matchGlobal\":false,\"createDate\":\"2017-06-05T21:15:34.616-06:00\",\"durationBetweenActivitiesSeconds\":10,\"roundType\":\"POOL\",\"matchPlayerCount\":2,\"maximumPlayerCount\":2,\"minimumMatchCount\":1,\"roundActivityType\":\"Trivia\",\"roundActivityValue\":\"n/a\",\"roundNames\":{\"en\":\"r1\"},\"minimumActivityToWinCount\":2,\"maximumActivityCount\":5,\"activityMaximumDurationSeconds\":5,\"playerMaximumDurationSeconds\":5,\"categories\":[\"*\"]},{\"id\":\"82e356e2-2eed-4355-9a43-d6034b85e71b\",\"gameId\":\"3c357795-07c7-4f69-8d1e-d7bfe94ae5e6\",\"roundStatus\":\"PENDING\",\"roundSequence\":2,\"finalRound\":true,\"matchGlobal\":false,\"createDate\":\"2017-06-05T21:15:48.877-06:00\",\"durationBetweenActivitiesSeconds\":10,\"roundType\":\"BRACKET\",\"matchPlayerCount\":2,\"maximumPlayerCount\":2,\"minimumMatchCount\":1,\"roundActivityType\":\"Trivia\",\"roundActivityValue\":\"n/a\",\"roundNames\":{\"en\":\"br\"},\"categories\":[\"*\"],\"roundPurse\":10,\"minimumActivityToWinCount\":2,\"maximumActivityCount\":5,\"activityMaximumDurationSeconds\":5,\"playerMaximumDurationSeconds\":5}]}";
//        Map<String, String> props = new HashMap<>();
//        props.put("game", gameJsonStr);
//        String messageId = "1";
//
//        TypeReference<GameWithRounds> typeRef = new TypeReference<GameWithRounds>(){};
//        getJsonObjectFromProps(props, messageId, "createGame", "game", true, typeRef);
//    }

    public static void main(String[] args)
    throws Exception
    {
        //initialize the local logging to go to the console
        System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.SimpleLog");
        System.setProperty("org.apache.commons.logging.simplelog.defaultlog", "debug");
        System.setProperty("org.apache.commons.logging.simplelog.defaultlog", "debug");
        System.setProperty("org.apache.commons.logging.simplelog.log.org.apache", "debug");

        System.setProperty("log4j.defaultInitOverride", "true");
        Properties log4jProperties = new Properties();
        log4jProperties.setProperty("log4j.rootLogger", "DEBUG, stdout");
        log4jProperties.setProperty("log4j.appender.stdout", "org.apache.log4j.ConsoleAppender");
        log4jProperties.setProperty("log4j.appender.stdout.layout", "org.apache.log4j.PatternLayout");
        log4jProperties.setProperty("log4j.appender.stdout.layout.ConversionPattern", "%d{MM/dd HH:mm:ss.SSS} %-5p [%c] %m%n");
        log4jProperties.setProperty("log4j.logger.org.apache.http.wire", "DEBUG");
        log4jProperties.setProperty("log4j.logger.org.apache.httpclient", "DEBUG");
        PropertyConfigurator.configure(log4jProperties);

        Scratch s = new Scratch();
        //s.parse();

    }


}
