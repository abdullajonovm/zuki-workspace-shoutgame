package test;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class Sorting
{
    private static class RoundPlayer
    {
        public Double rank;

        public Double getRank()
        {
            return rank;
        }

        @Override
        public String toString()
        {
            return rank+"";
        }
    }

    public void sort1(List<RoundPlayer> roundPlayers)
    {
        List<RoundPlayer> sortedRoundPlayers = roundPlayers.stream()
            .sorted( Comparator.comparing(RoundPlayer::getRank, Comparator.nullsLast(Comparator.reverseOrder())) )
            .collect(Collectors.toList());

        System.out.println(sortedRoundPlayers);
    }

    public void sort2(List<RoundPlayer> players)
    {
//        Collections.sort(players, new Comparator<RoundPlayer>() {
//            @Override
//            public int compare(RoundPlayer lhs, RoundPlayer rhs)
//            {
//                if (lhs.getRank() == null) return 1;
//                else if (rhs.getRank() == null) return -1;
//                else return rhs.getRank().compareTo(lhs.getRank());
//            }
//        });

        players = players.stream()
                .sorted( Comparator.comparing(RoundPlayer::getRank, Comparator.nullsLast(Comparator.reverseOrder())) )
                .collect(Collectors.toList());

        System.out.println(players);
    }

    public static void main(String[] args)
    {
        Sorting s = new Sorting();

        RoundPlayer rp1 = new RoundPlayer(); rp1.rank = null;
        RoundPlayer rp2 = new RoundPlayer(); rp2.rank = 4.4D;
        RoundPlayer rp3 = new RoundPlayer(); rp3.rank = 1.3D;
        RoundPlayer rp4 = new RoundPlayer(); rp4.rank = 0D;
        RoundPlayer rp5 = new RoundPlayer(); rp5.rank = 5.9D;
        RoundPlayer rp6 = new RoundPlayer(); rp6.rank = 1.3D;

        List<RoundPlayer> roundPlayers = Arrays.asList(new RoundPlayer[] {rp1, rp2, rp3, rp4, rp5, rp6});
        s.sort2(roundPlayers);
    }

}
