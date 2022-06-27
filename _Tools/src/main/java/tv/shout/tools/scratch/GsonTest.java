package tv.shout.tools.scratch;

import java.util.Date;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class GsonTest
{
    class Foo
    {
        private String id;
        public String x;
        public Date dte;
    }

    public static void main(String[] args)
    {
        //Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX").create();
        Gson gson = new GsonBuilder().create();

        //String jsonStr = "{ \"createdDate\": \"2016-10-11T16:19:28.371+0000\", \"determination\": \"AWARDED\", \"gameId\": \"32bad04d-e96a-4fea-b4f9-6594b58595e6\", \"id\": \"88686d5e-f31d-40ab-bc0f-53783503cd2d\", \"isPayoutCompleted\": true, \"lastRoundId\": \"whatever\", \"nextRoundId\": null, \"payoutAwardedAmount\": 25, \"payoutPaymentId\": \"1\", \"payoutVenue\": \"PAYPAL\", \"rank\": 2, \"subscriberId\": 8 }";
        //GamePlayer gp = gson.fromJson(jsonStr, GamePlayer.class);
        //System.out.println(gp);

        String fooStr = "{\"id\": \"12345\", \"x\": \"x27\", \"dte\": \"2016-10-11T16:19:28.371+0000\" }";
        Foo foo  = gson.fromJson(fooStr, Foo.class);
        System.out.println(foo.id + ", " + foo.x + ", " + foo.dte);
    }
}
