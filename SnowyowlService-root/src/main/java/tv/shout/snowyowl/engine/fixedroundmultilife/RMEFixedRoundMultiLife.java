package tv.shout.snowyowl.engine.fixedroundmultilife;

import javax.annotation.Resource;

import tv.shout.snowyowl.engine.MQE;
import tv.shout.snowyowl.engine.PayoutManager;
import tv.shout.snowyowl.engine.RMEFixedRoundCommon;
import tv.shout.snowyowl.service.ISnowyowlService;

public class RMEFixedRoundMultiLife
extends RMEFixedRoundCommon
{
    @Resource(name="mqeFixedRoundMultiLife")
    private MQE _matchQueueEngine;

    @Resource(name="payoutManagerFixedRoundMultiLife")
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
        return ISnowyowlService.GAME_ENGINE_TYPE_FixedRound_MultiLife;
    }
}
