package test;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Rounder
{
    public static float getPlayerPot(String payoutCalculationMethod, float payoutHouseTakePercentage, float costToJoin, float purse, int numPlayers)
    {
        float playerPot;
        switch (payoutCalculationMethod)
        {
            case "DYNAMIC":
                float gameRevenue = numPlayers * costToJoin;
                playerPot = gameRevenue - ((payoutHouseTakePercentage/100F) * gameRevenue);
                break;

            //case STATIC:
            default:
                playerPot = purse;
                break;
        }

        return playerPot;
    }

    //call this as soon as the number of players becomes locked (i.e. when bracket play begins)
    private static List<Float> calculatePayoutPercentages(int numPlayers, float percentOfPlayersToAward)
    {
        List<Float> playerPercentages = new ArrayList<>(1);
        playerPercentages.add(100F);

        int playerAwardCount = (int) (numPlayers * (percentOfPlayersToAward/100F));

        //expand the percentages array until all payout amounts are determined
        while (playerPercentages.size() < playerAwardCount) {
            System.out.println("size is too small. doubling array size...");
            List<Float> newPlayerPercentages = new ArrayList<>();
            for (int i=playerPercentages.size()-1; i>=0; i--) {
                float percentage = playerPercentages.get(i);
                float newPercentage = percentage * (percentOfPlayersToAward/100F);
                newPlayerPercentages.add(0, newPercentage);
                newPlayerPercentages.add(0, percentage - newPercentage);

                System.out.println(MessageFormat.format("i: {0}, payout percentages: {1}", i, newPlayerPercentages));
            }
            playerPercentages = newPlayerPercentages;
        }

        //reverse sort to get high to low
        Collections.sort(playerPercentages, new Comparator<Float>() {
            @Override
            public int compare(Float lhs, Float rhs)
            {
                return rhs.compareTo(lhs);
            }
        });

        //trim down to the correct size (it will always be a power of 2 due to the nature of the loop, so it will probably be too large)
        if (playerPercentages.size() > playerAwardCount) {
            List<Float> newPlayerPercentages = new ArrayList<>(playerAwardCount);
            for (int i=0; i<playerAwardCount; i++) {
                newPlayerPercentages.add(playerPercentages.get(i));
            }
            playerPercentages = newPlayerPercentages;
        }

        return playerPercentages;
    }

    public static void main(String[] args)
    {
//        float f = 3.545555555F;
//
//        f = (float) (Math.floor(f * 100F) / 100F);
//
//        System.out.println(f);

        int numPlayers = 10;
        float percentOfPlayersToAward = 72F;
        float payoutHouseTakePercentage = 31F;
        float costToJoin = 1F;
        float purse = 10F;

        //List<Float> payoutPercentages = calculatePayoutPercentages(numPlayers, percentOfPlayersToAward);
        //System.out.println(payoutPercentages);

        float playerPot = getPlayerPot("STATIC", payoutHouseTakePercentage, costToJoin, purse, numPlayers);
        System.out.println("pot: " + playerPot);

    }

}
