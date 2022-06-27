package tv.shout.snowyowl.engine.fixedroundmultilife;

import javax.annotation.Resource;

import tv.shout.snowyowl.engine.MME;
import tv.shout.snowyowl.engine.MQEFixedRoundCommon;
import tv.shout.snowyowl.service.ISnowyowlService;

public class MQEFixedRoundMultiLife
extends MQEFixedRoundCommon
{
    private static final String FILE_PREFIX = "FixedRound_MultiLifeMQE";

    @Resource(name="mmeFixedRoundMultiLife")
    private MME _matchManagementEngine;

    @Override
    public String getType()
    {
        return ISnowyowlService.GAME_ENGINE_TYPE_FixedRound_MultiLife;
    }

    @Override
    public String getFilePrefix()
    {
        return FILE_PREFIX;
    }

    @Override
    public MME getMME()
    {
        return _matchManagementEngine;
    }

}
