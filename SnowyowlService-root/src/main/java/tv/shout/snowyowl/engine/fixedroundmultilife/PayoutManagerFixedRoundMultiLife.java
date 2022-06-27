package tv.shout.snowyowl.engine.fixedroundmultilife;

import tv.shout.snowyowl.engine.PayoutManagerFixedRoundCommon;
import tv.shout.snowyowl.service.ISnowyowlService;

public class PayoutManagerFixedRoundMultiLife
extends PayoutManagerFixedRoundCommon
{
    //private static Logger _logger = Logger.getLogger(PayoutManagerFixedRoundMultiLife.class);

    @Override
    public String getType()
    {
        return ISnowyowlService.GAME_ENGINE_TYPE_FixedRound_MultiLife;
    }

}


