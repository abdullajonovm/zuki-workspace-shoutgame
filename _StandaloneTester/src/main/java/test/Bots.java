package test;

import java.util.ArrayList;
import java.util.List;

public class Bots
{
    private double _percentageToFillBracketWithBots = 100D;

    private class Round
    {
        private int _maximumPlayerCount;

        public int getMaximumPlayerCount()
        {
            return _maximumPlayerCount;
        }
    }

    public void addBots()
    {
        List<Integer> botSubscriberIds = new ArrayList<>();
        int actualPlayers = 1;
        botSubscriberIds.add(52989);
        Round round = new Round();
        round._maximumPlayerCount = 16;

        if (_percentageToFillBracketWithBots > 0D) {
            int totalPlayers = actualPlayers + botSubscriberIds.size();
            int numPlayersNeededToFillToDesiredAmount = (int) (_percentageToFillBracketWithBots / 100D * round.getMaximumPlayerCount());

            System.out.println("totalPlayers: " + totalPlayers);
            System.out.println("numPlayersNeededToFillToDesiredAmount: " + numPlayersNeededToFillToDesiredAmount);

            if (totalPlayers < numPlayersNeededToFillToDesiredAmount) {
                int botsNeeded = numPlayersNeededToFillToDesiredAmount - totalPlayers;
                System.out.println("botsNeeded: " + botsNeeded);
            }
        }
    }

    public static void main(String[] args)
    {
        Bots b = new Bots();
        b.addBots();
        // TODO Auto-generated method stub

    }

}
