package tv.shout.snowyowl.engine.fixedroundsinglelife;

import javax.annotation.Resource;

import tv.shout.snowyowl.engine.MME;
import tv.shout.snowyowl.engine.MQEFixedRoundCommon;
import tv.shout.snowyowl.service.ISnowyowlService;

public class MQEFixedRoundSingleLife
extends MQEFixedRoundCommon
{
    private static final String FILE_PREFIX = "FixedRound_SingleLifeMQE";

    @Resource(name="mmeFixedRoundSingleLife")
    private MME _matchManagementEngine;

    @Override
    public String getType()
    {
        return ISnowyowlService.GAME_ENGINE_TYPE_FixedRound_SingleLife;
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
