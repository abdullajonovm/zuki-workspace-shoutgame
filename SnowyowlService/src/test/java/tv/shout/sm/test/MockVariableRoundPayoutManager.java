package tv.shout.sm.test;

import java.util.Arrays;
import java.util.Date;

import tv.shout.snowyowl.domain.GamePayout;
import tv.shout.snowyowl.domain.PayoutModel;
import tv.shout.snowyowl.domain.PayoutModelRound;
import tv.shout.snowyowl.domain.PayoutModelRound.CATEGORY;
import tv.shout.snowyowl.engine.fixedround.PayoutManagerFixedRoundSingleLife;

public class MockVariableRoundPayoutManager
extends PayoutManagerFixedRoundSingleLife
{
//    @Override
//    protected float getScaleFactor(PayoutModel payoutModel, int playerCount)
//    throws PayoutManagerException
//    {
//        return 1.0F;
//    }

    @Override
    public PayoutModel getPayoutModel(GamePayout gamePayout)
    {
        int payoutModelId = gamePayout.getPayoutModelId();
        PayoutModel pm;

        switch (payoutModelId)
        {
            case 1: {
                pm = new PayoutModel();
                pm.setPayoutModelId(1);
                pm.setName("$10 Premium");
                pm.setBasePlayerCount(1048576);
                pm.setEntranceFeeAmount(10F);
                pm.setActive(true);
                pm.setCreatorId(0);
                pm.setCreateDate(new Date());
                pm.setPayoutModelRounds(Arrays.asList(
                    createPayoutModelRound(pm,  0, "Champion",       1, 1, 1000000F, "CASH", PayoutModelRound.CATEGORY.PHYSICAL),
                    createPayoutModelRound(pm,  1, "Round 20",       2, 1,  100000F, "CASH", PayoutModelRound.CATEGORY.PHYSICAL),
                    createPayoutModelRound(pm,  2, "Round 19",       4, 2,   50000F, "CASH", PayoutModelRound.CATEGORY.PHYSICAL),
                    createPayoutModelRound(pm,  3, "Round 18",       8, 4,   25000F, "CASH", PayoutModelRound.CATEGORY.PHYSICAL),
                    createPayoutModelRound(pm,  4, "Round 17",      16, 8,   10000F, "CASH", PayoutModelRound.CATEGORY.PHYSICAL),
                    createPayoutModelRound(pm,  5, "Round 16",      32, 16,   5000F, "CASH", PayoutModelRound.CATEGORY.PHYSICAL),
                    createPayoutModelRound(pm,  6, "Round 15",      64, 32,   1000F, "CASH", PayoutModelRound.CATEGORY.PHYSICAL),
                    createPayoutModelRound(pm,  7, "Round 14",     128, 64,    500F, "CASH", PayoutModelRound.CATEGORY.PHYSICAL),
                    createPayoutModelRound(pm,  8, "Round 13",     256, 128,   100F, "CASH", PayoutModelRound.CATEGORY.PHYSICAL),
                    createPayoutModelRound(pm,  9, "Round 12",     512, 256,    50F, "CASH", PayoutModelRound.CATEGORY.PHYSICAL),
                    createPayoutModelRound(pm, 10, "Round 11",    1024, 512,    40F, "CASH", PayoutModelRound.CATEGORY.PHYSICAL),
                    createPayoutModelRound(pm, 11, "Round 10",    2048, 1024,   35F, "CASH", PayoutModelRound.CATEGORY.PHYSICAL),
                    createPayoutModelRound(pm, 12,  "Round 9",    4096, 2048,   30F, "CASH", PayoutModelRound.CATEGORY.PHYSICAL),
                    createPayoutModelRound(pm, 13,  "Round 8",    8192, 4096,   25F, "CASH", PayoutModelRound.CATEGORY.PHYSICAL),
                    createPayoutModelRound(pm, 14,  "Round 7",   16384, 8192,   20F, "CASH", PayoutModelRound.CATEGORY.PHYSICAL),
                    createPayoutModelRound(pm, 15,  "Round 6",   32768, 16384,  15F, "CASH", PayoutModelRound.CATEGORY.PHYSICAL),
                    createPayoutModelRound(pm, 16,  "Round 5",   65536, 32768,  12F, "CASH", PayoutModelRound.CATEGORY.PHYSICAL),
                    createPayoutModelRound(pm, 17,  "Round 4",  131072, 65536,  10F, "CASH", PayoutModelRound.CATEGORY.PHYSICAL),
                    createPayoutModelRound(pm, 18,  "Round 3",  262144, 131072,  0F, "CASH", PayoutModelRound.CATEGORY.PHYSICAL),
                    createPayoutModelRound(pm, 19,  "Round 2",  524288, 262144,  0F, "CASH", PayoutModelRound.CATEGORY.PHYSICAL),
                    createPayoutModelRound(pm, 20,  "Round 1", 1048576, 524288,  0F, "CASH", PayoutModelRound.CATEGORY.PHYSICAL)
                ));
            }
            break;

            case 2: {
                pm = new PayoutModel();
                pm.setPayoutModelId(2);
                pm.setName("$2 Premium");
                pm.setBasePlayerCount(1048576);
                pm.setEntranceFeeAmount(2F);
                pm.setActive(true);
                pm.setCreatorId(0);
                pm.setCreateDate(new Date());
                pm.setPayoutModelRounds(Arrays.asList(
                    createPayoutModelRound(pm,  0, "Champion",       1, 1, 1000000F, "CASH", PayoutModelRound.CATEGORY.PHYSICAL),
                    createPayoutModelRound(pm,  1, "Round 20",       2, 1,   10000F, "CASH", PayoutModelRound.CATEGORY.PHYSICAL),
                    createPayoutModelRound(pm,  2, "Round 19",       4, 2,    1000F, "CASH", PayoutModelRound.CATEGORY.PHYSICAL),
                    createPayoutModelRound(pm,  3, "Round 18",       8, 4,     500F, "CASH", PayoutModelRound.CATEGORY.PHYSICAL),
                    createPayoutModelRound(pm,  4, "Round 17",      16, 8,     100F, "CASH", PayoutModelRound.CATEGORY.PHYSICAL),
                    createPayoutModelRound(pm,  5, "Round 16",      32, 16,     50F, "CASH", PayoutModelRound.CATEGORY.PHYSICAL),
                    createPayoutModelRound(pm,  6, "Round 15",      64, 32,     25F, "CASH", PayoutModelRound.CATEGORY.PHYSICAL),
                    createPayoutModelRound(pm,  7, "Round 14",     128, 64,     20F, "CASH", PayoutModelRound.CATEGORY.PHYSICAL),
                    createPayoutModelRound(pm,  8, "Round 13",     256, 128,    15F, "CASH", PayoutModelRound.CATEGORY.PHYSICAL),
                    createPayoutModelRound(pm,  9, "Round 12",     512, 256,    10F, "CASH", PayoutModelRound.CATEGORY.PHYSICAL),
                    createPayoutModelRound(pm, 10, "Round 11",    1024, 512,     9F, "CASH", PayoutModelRound.CATEGORY.PHYSICAL),
                    createPayoutModelRound(pm, 11, "Round 10",    2048, 1024,    8F, "CASH", PayoutModelRound.CATEGORY.PHYSICAL),
                    createPayoutModelRound(pm, 12,  "Round 9",    4096, 2048,    7F, "CASH", PayoutModelRound.CATEGORY.PHYSICAL),
                    createPayoutModelRound(pm, 13,  "Round 8",    8192, 4096,    6F, "CASH", PayoutModelRound.CATEGORY.PHYSICAL),
                    createPayoutModelRound(pm, 14,  "Round 7",   16384, 8192,    5F, "CASH", PayoutModelRound.CATEGORY.PHYSICAL),
                    createPayoutModelRound(pm, 15,  "Round 6",   32768, 16384,   0F, "CASH", PayoutModelRound.CATEGORY.PHYSICAL),
                    createPayoutModelRound(pm, 16,  "Round 5",   65536, 32768,   0F, "CASH", PayoutModelRound.CATEGORY.PHYSICAL),
                    createPayoutModelRound(pm, 17,  "Round 4",  131072, 65536,   0F, "CASH", PayoutModelRound.CATEGORY.PHYSICAL),
                    createPayoutModelRound(pm, 18,  "Round 3",  262144, 131072,  0F, "CASH", PayoutModelRound.CATEGORY.PHYSICAL),
                    createPayoutModelRound(pm, 19,  "Round 2",  524288, 262144,  0F, "CASH", PayoutModelRound.CATEGORY.PHYSICAL),
                    createPayoutModelRound(pm, 20,  "Round 1", 1048576, 524288,  0F, "CASH", PayoutModelRound.CATEGORY.PHYSICAL)
                ));
            }
            break;

            case 11: {
                pm = new PayoutModel();
                pm.setPayoutModelId(11);
                pm.setName("Matt #1");
                pm.setBasePlayerCount(100);
                pm.setEntranceFeeAmount(1F);
                pm.setActive(true);
                pm.setCreatorId(0);
                pm.setCreateDate(new Date());
                pm.setPayoutModelRounds(Arrays.asList(
                    createPayoutModelRound(pm,  0, "1st Place Winner",   1,  1, 20F, "CASH", PayoutModelRound.CATEGORY.PHYSICAL),
                    createPayoutModelRound(pm,  1, "2nd Place Winner",   2,  1,  5F, "CASH", PayoutModelRound.CATEGORY.PHYSICAL),
                    createPayoutModelRound(pm,  2,      "Winners 3-4",   4,  2,  2F, "CASH", PayoutModelRound.CATEGORY.PHYSICAL),
                    createPayoutModelRound(pm,  3,      "Winners 5-8",   8,  4,  1F, "CASH", PayoutModelRound.CATEGORY.PHYSICAL),
                    createPayoutModelRound(pm,  4,     "Winners 9-16",  16,  8,  1F, "CASH", PayoutModelRound.CATEGORY.PHYSICAL),
                    createPayoutModelRound(pm,  5,    "Winners 17-32",  32, 16,  1F, "CASH", PayoutModelRound.CATEGORY.PHYSICAL),
                    createPayoutModelRound(pm,  6,    "Winners 33-64",  64, 32,  1F, "CASH", PayoutModelRound.CATEGORY.PHYSICAL),
                    createPayoutModelRound(pm,  7,   "Winners 65-100", 100, 36,  0F, "CASH", PayoutModelRound.CATEGORY.PHYSICAL)
                ));
            }
            break;

            case 14: {
                pm = new PayoutModel();
                pm.setPayoutModelId(14);
                pm.setName("Aidan $10 for 100");
                pm.setBasePlayerCount(100);
                pm.setEntranceFeeAmount(10F);
                pm.setActive(true);
                pm.setCreatorId(10);
                pm.setCreateDate(new Date());
                pm.setPayoutModelRounds(Arrays.asList(
                    createPayoutModelRound(pm,  0, "Champion",      1,      1, 250.00F, "CASH", PayoutModelRound.CATEGORY.PHYSICAL),
                    createPayoutModelRound(pm,  1, "Champion",      2,      1, 100.00F, "CASH", PayoutModelRound.CATEGORY.PHYSICAL),
                    createPayoutModelRound(pm,  2, "Champion",      4,      2,  50.00F, "CASH", PayoutModelRound.CATEGORY.PHYSICAL),
                    createPayoutModelRound(pm,  3, "Champion",      8,      4,  25.00F, "CASH", PayoutModelRound.CATEGORY.PHYSICAL),
                    createPayoutModelRound(pm,  4, "Champion",     16,      8,  20.00F, "CASH", PayoutModelRound.CATEGORY.PHYSICAL),
                    createPayoutModelRound(pm,  5, "Champion",     32,     16,   0.00F, "CASH", PayoutModelRound.CATEGORY.PHYSICAL),
                    createPayoutModelRound(pm,  6, "Champion",     64,     32,   0.00F, "CASH", PayoutModelRound.CATEGORY.PHYSICAL),
                    createPayoutModelRound(pm,  7, "Champion",    100,     36,   0.00F, "CASH", PayoutModelRound.CATEGORY.PHYSICAL)
                ));
            }
            break;

            case 15: {
                pm = new PayoutModel();
                pm.setPayoutModelId(15);
                pm.setName("Shout $10 Millionaire Game");
                pm.setBasePlayerCount(1048576);
                pm.setEntranceFeeAmount(10F);
                pm.setActive(true);
                pm.setCreatorId(0);
                pm.setCreateDate(new Date());
                pm.setPayoutModelRounds(Arrays.asList(
                    createPayoutModelRound(pm,  0, "Champion",      1,       1, 1000000.00F, "CASH", PayoutModelRound.CATEGORY.PHYSICAL),
                    createPayoutModelRound(pm,  1, "round 20",      2,       1,  100000.00F, "CASH", PayoutModelRound.CATEGORY.PHYSICAL),
                    createPayoutModelRound(pm,  2, "round 19",      4,       2,   50000.00F, "CASH", PayoutModelRound.CATEGORY.PHYSICAL),
                    createPayoutModelRound(pm,  3, "round 18",      8,       4,   25000.00F, "CASH", PayoutModelRound.CATEGORY.PHYSICAL),
                    createPayoutModelRound(pm,  4, "round 17",     16,       8,   10000.00F, "CASH", PayoutModelRound.CATEGORY.PHYSICAL),
                    createPayoutModelRound(pm,  5, "round 16",     32,      16,    5000.00F, "CASH", PayoutModelRound.CATEGORY.PHYSICAL),
                    createPayoutModelRound(pm,  6, "round 15",     64,      32,    1000.00F, "CASH", PayoutModelRound.CATEGORY.PHYSICAL),
                    createPayoutModelRound(pm,  7, "round 14",    128,      64,     500.00F, "CASH", PayoutModelRound.CATEGORY.PHYSICAL),
                    createPayoutModelRound(pm,  8, "round 13",    256,     128,     100.00F, "CASH", PayoutModelRound.CATEGORY.PHYSICAL),
                    createPayoutModelRound(pm,  9, "round 12",    512,     256,      50.00F, "CASH", PayoutModelRound.CATEGORY.PHYSICAL),
                    createPayoutModelRound(pm, 10, "round 11",   1024,     512,      30.00F, "CASH", PayoutModelRound.CATEGORY.PHYSICAL),
                    createPayoutModelRound(pm, 11, "round 10",   2048,    1024,      25.00F, "CASH", PayoutModelRound.CATEGORY.PHYSICAL),
                    createPayoutModelRound(pm, 12,  "round 9",   4096,    2048,      20.00F, "CASH", PayoutModelRound.CATEGORY.PHYSICAL),
                    createPayoutModelRound(pm, 13,  "round 8",   8192,    4096,      15.00F, "CASH", PayoutModelRound.CATEGORY.PHYSICAL),
                    createPayoutModelRound(pm, 14,  "round 7",  16384,    8192,      13.00F, "CASH", PayoutModelRound.CATEGORY.PHYSICAL),
                    createPayoutModelRound(pm, 15,  "round 6",  32768,   16384,      12.00F, "CASH", PayoutModelRound.CATEGORY.PHYSICAL),
                    createPayoutModelRound(pm, 16,  "round 5",  65536,   32768,      11.00F, "CASH", PayoutModelRound.CATEGORY.PHYSICAL),
                    createPayoutModelRound(pm, 17,  "round 4", 131072,   65536,      10.00F, "CASH", PayoutModelRound.CATEGORY.PHYSICAL),
                    createPayoutModelRound(pm, 18,  "round 3", 262144,  131072,       7.00F, "CASH", PayoutModelRound.CATEGORY.PHYSICAL),
                    createPayoutModelRound(pm, 19,  "round 2", 524288,  262144,       5.00F, "CASH", PayoutModelRound.CATEGORY.PHYSICAL),
                    createPayoutModelRound(pm, 20,  "round 1", 1048576, 524288,       0.00F, "CASH", PayoutModelRound.CATEGORY.PHYSICAL)
                ));
            }
            break;

            case 20: {
                pm = new PayoutModel();
                pm.setPayoutModelId(20);
                pm.setName("$5 Ticket Millionaire Game");
                pm.setBasePlayerCount(1048576);
                pm.setEntranceFeeAmount(5F);
                pm.setMinimumFirstPlacePayoutAmount(150F);
                pm.setMinimumSecondPlacePayoutAmount(50F);
                pm.setMinimumOverallPayoutAmount(300F);
                pm.setActive(true);
                pm.setCreatorId(0);
                pm.setCreateDate(new Date());
                pm.setPayoutModelRounds(Arrays.asList(
                        createPayoutModelRound(pm,  0, "1st Place Winner",                  1,      1, 1000000.00F, "CASH", PayoutModelRound.CATEGORY.PHYSICAL),
                        createPayoutModelRound(pm,  1, "2nd Place Winner",                  2,      1,  100000.00F, "CASH", PayoutModelRound.CATEGORY.PHYSICAL),
                        createPayoutModelRound(pm,  2, "Winners 3 - 4",                     4,      2,   10000.00F, "CASH", PayoutModelRound.CATEGORY.PHYSICAL),
                        createPayoutModelRound(pm,  3, "Winners 5 - 8",                     8,      4,    1000.00F, "CASH", PayoutModelRound.CATEGORY.PHYSICAL),
                        createPayoutModelRound(pm,  4, "Winners 9 - 16",                   16,      8,     500.00F, "CASH", PayoutModelRound.CATEGORY.PHYSICAL),
                        createPayoutModelRound(pm,  5, "Winners 17 - 32",                  32,     16,     100.00F, "CASH", PayoutModelRound.CATEGORY.PHYSICAL),
                        createPayoutModelRound(pm,  6, "Winners 33 - 64",                  64,     32,      50.00F, "CASH", PayoutModelRound.CATEGORY.PHYSICAL),
                        createPayoutModelRound(pm,  7, "Winners 65 - 128",                128,     64,      25.00F, "CASH", PayoutModelRound.CATEGORY.PHYSICAL),
                        createPayoutModelRound(pm,  8, "Winners 129 - 256",               256,    128,      20.00F, "CASH", PayoutModelRound.CATEGORY.PHYSICAL),
                        createPayoutModelRound(pm,  9, "Winners 257 - 512",               512,    256,      15.00F, "CASH", PayoutModelRound.CATEGORY.PHYSICAL),
                        createPayoutModelRound(pm, 10, "Winners 513 - 1,024",            1024,    512,      10.00F, "CASH", PayoutModelRound.CATEGORY.PHYSICAL),
                        createPayoutModelRound(pm, 11, "Winners 1,025 - 2,048",          2048,   1024,       9.00F, "CASH", PayoutModelRound.CATEGORY.PHYSICAL),
                        createPayoutModelRound(pm, 12, "Winners 2,049 - 4,096",          4096,   2048,       8.00F, "CASH", PayoutModelRound.CATEGORY.PHYSICAL),
                        createPayoutModelRound(pm, 13, "Winners 4,097 - 8,192",          8192,   4096,       7.00F, "CASH", PayoutModelRound.CATEGORY.PHYSICAL),
                        createPayoutModelRound(pm, 14, "Winners 8,193 - 16,384",        16384,   8192,       6.00F, "CASH", PayoutModelRound.CATEGORY.PHYSICAL),
                        createPayoutModelRound(pm, 15, "Winners 16,385 - 32,768",       32768,  16384,       5.00F, "CASH", PayoutModelRound.CATEGORY.PHYSICAL),
                        createPayoutModelRound(pm, 16, "Winners 32,769 - 65,536",       65536,  32768,       4.00F, "CASH", PayoutModelRound.CATEGORY.PHYSICAL),
                        createPayoutModelRound(pm, 17, "Winners 65,537 - 131,072",     131072,  65536,       3.00F, "CASH", PayoutModelRound.CATEGORY.PHYSICAL),
                        createPayoutModelRound(pm, 18, "Winners 131,073 - 262,144",    262144, 131072,       2.50F, "CASH", PayoutModelRound.CATEGORY.PHYSICAL),
                        createPayoutModelRound(pm, 19, "Winners 262,145 - 524,288",    524288, 262144,       2.00F, "CASH", PayoutModelRound.CATEGORY.PHYSICAL),
                        createPayoutModelRound(pm, 20, "Winners 524,289 - 1,048,576", 1048576, 524288,       0.00F, "CASH", PayoutModelRound.CATEGORY.PHYSICAL)
                ));
            }
            break;

            case 22: {
                pm = new PayoutModel();
                pm.setPayoutModelId(22);
                pm.setName("Sponsor Game");
                pm.setBasePlayerCount(1048576);
                pm.setEntranceFeeAmount(0F);
                pm.setActive(true);
                pm.setCreatorId(0);
                pm.setCreateDate(new Date());
                pm.setPayoutModelRounds(Arrays.asList(
                    createPayoutModelRound(pm,  0, "1st Place Winner",                  1,      1, 100.00F, "CASH", PayoutModelRound.CATEGORY.PHYSICAL),
                    createPayoutModelRound(pm,  1, "2nd Place Winner",                  2,      1,  50.00F, "CASH", PayoutModelRound.CATEGORY.PHYSICAL),
                    createPayoutModelRound(pm,  2, "Winners 3 - 4",                     4,      2,  25.00F, "CASH", PayoutModelRound.CATEGORY.PHYSICAL),
                    createPayoutModelRound(pm,  3, "Winners 5 - 8",                     8,      4,  10.00F, "CASH", PayoutModelRound.CATEGORY.PHYSICAL),
                    createPayoutModelRound(pm,  4, "Winners 9 - 16",                   16,      8,   5.00F, "CASH", PayoutModelRound.CATEGORY.PHYSICAL),
                    createPayoutModelRound(pm,  5, "Winners 17 - 32",                  32,     16,   0.00F, "CASH", PayoutModelRound.CATEGORY.PHYSICAL),
                    createPayoutModelRound(pm,  6, "Winners 33 - 64",                  64,     32,   0.00F, "CASH", PayoutModelRound.CATEGORY.PHYSICAL),
                    createPayoutModelRound(pm,  7, "Winners 65 - 128",                128,     64,   0.00F, "CASH", PayoutModelRound.CATEGORY.PHYSICAL),
                    createPayoutModelRound(pm,  8, "Winners 129 - 256",               256,    128,   0.00F, "CASH", PayoutModelRound.CATEGORY.PHYSICAL),
                    createPayoutModelRound(pm,  9, "Winners 257 - 512",               512,    256,   0.00F, "CASH", PayoutModelRound.CATEGORY.PHYSICAL),
                    createPayoutModelRound(pm, 10, "Winners 513 - 1,024",            1024,    512,   0.00F, "CASH", PayoutModelRound.CATEGORY.PHYSICAL),
                    createPayoutModelRound(pm, 11, "Winners 1,025 - 2,048",          2048,   1024,   0.00F, "CASH", PayoutModelRound.CATEGORY.PHYSICAL),
                    createPayoutModelRound(pm, 12, "Winners 2,049 - 4,096",          4096,   2048,   0.00F, "CASH", PayoutModelRound.CATEGORY.PHYSICAL),
                    createPayoutModelRound(pm, 13, "Winners 4,097 - 8,192",          8192,   4096,   0.00F, "CASH", PayoutModelRound.CATEGORY.PHYSICAL),
                    createPayoutModelRound(pm, 14, "Winners 8,193 - 16,384",        16384,   8192,   0.00F, "CASH", PayoutModelRound.CATEGORY.PHYSICAL),
                    createPayoutModelRound(pm, 15, "Winners 16,385 - 32,768",       32768,  16384,   0.00F, "CASH", PayoutModelRound.CATEGORY.PHYSICAL),
                    createPayoutModelRound(pm, 16, "Winners 32,769 - 65,536",       65536,  32768,   0.00F, "CASH", PayoutModelRound.CATEGORY.PHYSICAL),
                    createPayoutModelRound(pm, 17, "Winners 65,537 - 131,072",     131072,  65536,   0.00F, "CASH", PayoutModelRound.CATEGORY.PHYSICAL),
                    createPayoutModelRound(pm, 18, "Winners 131,073 - 262,144",    262144, 131072,   0.00F, "CASH", PayoutModelRound.CATEGORY.PHYSICAL),
                    createPayoutModelRound(pm, 19, "Winners 262,145 - 524,288",    524288, 262144,   0.00F, "CASH", PayoutModelRound.CATEGORY.PHYSICAL),
                    createPayoutModelRound(pm, 20, "Winners 524,289 - 1,048,576", 1048576, 524288,   0.00F, "CASH", PayoutModelRound.CATEGORY.PHYSICAL)
                ));
            }
            break;

            default:
                throw new IllegalArgumentException();
        }

        return pm;
    }

    private PayoutModelRound createPayoutModelRound(
        PayoutModel pm, int sortOrder, String description, int startingPlayerCount, int eliminatedPlayerCount, float eliminatedPayoutAmount, String type, CATEGORY category)
    {
        PayoutModelRound pmr = new PayoutModelRound();

        pmr.setPayoutModelId(pm.getPayoutModelId());
        pmr.setSortOrder(sortOrder);
        pmr.setDescription(description);
        pmr.setStartingPlayerCount(startingPlayerCount);
        pmr.setEliminatedPlayerCount(eliminatedPlayerCount);
        pmr.setEliminatedPayoutAmount(eliminatedPayoutAmount);
        pmr.setType(type);
        pmr.setCategory(category);
        pmr.setRoundNumber(sortOrder);

        return pmr;
    }
}
