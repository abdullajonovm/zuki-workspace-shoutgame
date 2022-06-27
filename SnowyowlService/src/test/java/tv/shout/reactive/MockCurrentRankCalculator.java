package tv.shout.reactive;

import tv.shout.sc.service.IShoutContestService;
import tv.shout.snowyowl.service.CurrentRankCalculator;

public class MockCurrentRankCalculator
extends CurrentRankCalculator
{
    public MockCurrentRankCalculator(IShoutContestService shoutContestService)
    {
        _shoutContestService = shoutContestService;
    }
}
