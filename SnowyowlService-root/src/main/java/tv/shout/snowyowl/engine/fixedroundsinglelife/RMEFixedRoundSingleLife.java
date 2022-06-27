package tv.shout.snowyowl.engine.fixedroundsinglelife;

import javax.annotation.Resource;

import tv.shout.snowyowl.engine.MQE;
import tv.shout.snowyowl.engine.PayoutManager;
import tv.shout.snowyowl.engine.RMEFixedRoundCommon;
import tv.shout.snowyowl.service.ISnowyowlService;

public class RMEFixedRoundSingleLife
extends RMEFixedRoundCommon
{
    @Resource(name="mqeFixedRoundSingleLife")
    private MQE _matchQueueEngine;

    @Resource(name="payoutManagerFixedRoundSingleLife")
    private PayoutManager _payoutManager;

    @Override
    public MQE getMQE()
    {
        return _matchQueueEngine;
    }

    @Override
    public PayoutManager getPayoutManager()
    {
        return _payoutManager;
    }

    @Override
    public String getType()
    {
        return ISnowyowlService.GAME_ENGINE_TYPE_FixedRound_SingleLife;
    }
}
