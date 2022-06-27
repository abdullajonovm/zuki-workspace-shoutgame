package tv.shout.sm.test;

import java.util.Map;

import tv.shout.util.FastMap;

public class ListGames
{

    public static void main(String[] args)
    throws Exception
    {
        String postUrl = "https://snowl-collector--0--nc10-1.shoutgameplay.com/snowladmin/games/list";

        Map<String, String> headers = new FastMap<>(
            "X-REST-SESSION-KEY", "008e37e4-fd89-4512-a690-76bbf7fb7bfd",
            "X-REST-DEVICE-ID", "6170b6711e251ad6"
        );

        Map<String, String> params = new FastMap<>(
            "toWds", "snowl-wds-origin--0--nc10-1.shoutgameplay.com",
            "appId", "snowyowl",
            "statuses", "PENDING,OPEN,INPLAY"
        );

        CertificateManager.trustAllCertificates();
        HttpLibrary.httpPost(postUrl, headers, params);
    }

}
