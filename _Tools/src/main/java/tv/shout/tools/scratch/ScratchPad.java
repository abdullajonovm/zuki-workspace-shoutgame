package tv.shout.tools.scratch;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

public class ScratchPad
{
    public static enum MATCH_STATUS { NEW, OPEN, PROCESSING, CLOSED, CANCELLED }
    
    public static enum ROUND_STATUS { PENDING, CANCELLED, READY, OPEN, FULL, INPLAY, CLOSED }
    
    private static final List<String> validFormVars = Arrays.asList(
        "categories"
    );

    public void foo(MATCH_STATUS... statuses)
    {
        String x = Arrays.stream(statuses).map(s -> s.toString()).collect(Collectors.joining(","));
        System.out.println(x);
    }
    
    public void createMessage(Map<String, String> requestParameters)
    {
        Map<String, String> props = new HashMap<>();
        
        requestParameters.entrySet().stream()
            .filter(map -> validFormVars.contains(map.getKey()))
            .forEach(map -> props.put(map.getKey(), map.getValue()));
        
        System.out.println(props);
    }
    
    public void readPropertiesFile()
    throws IOException
    {
        Properties prop = new Properties();
        InputStream is = new FileInputStream(new File("/Volumes/Encrypted Data/ShoutMeinc/dbinfo.properties"));
        try {
            prop.load(is);
        } finally {
            if (is != null) {
                is.close();
            }
        }
        
        System.out.println("un: " + prop.getProperty("dc4.db.username"));
        System.out.println("pw: " + prop.getProperty("dc4.db.password"));
    }
    
    public static void main(String[] args)
    throws Exception
    {
        //new ScratchPad().foo(MATCH_STATUS.NEW, MATCH_STATUS.OPEN);
        //new ScratchPad().createMessage(new FastMap<>("foo", "bar", "categories", "SPORTS,GOLF"));
        //new ScratchPad().readPropertiesFile();
        
        
//        int size = 4;
//        for (int i=0; i<25; i++) {
//            System.out.println(new Random().nextInt(size));
//        }
        
//        String id = UUID.randomUUID().toString();
//        String lockName = "ROUND_" + id.hashCode();
//        System.out.println(lockName);
//        System.out.println(Integer.MAX_VALUE);
        
//        int matchPlayerCount = 2;
//        int currentPlayerCount = 7;
//        int maximumPlayerCount = 10;
//        
//        if (currentPlayerCount < maximumPlayerCount && currentPlayerCount % matchPlayerCount != 0) {
//            int numBotsNeeded = matchPlayerCount - (currentPlayerCount % matchPlayerCount);
//            System.out.println("need bots: " + numBotsNeeded);
//        } else {
//            System.out.println("bots not needed");
//        }
        
        double correctValue = 1d;
        double score = 1d;
        int roundCount = 3;
        
        double newScore = correctValue + ((double)roundCount-1d) * score / (double)roundCount;
        System.out.println(newScore);
        
    }

}
