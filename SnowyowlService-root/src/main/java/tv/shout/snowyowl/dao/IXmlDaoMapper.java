package tv.shout.snowyowl.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

public interface IXmlDaoMapper
{
    void addBotsToSystem(@Param("botSubscriberIds") List<Long> botSubscriberIds);
    void addBotsToGame(@Param("gameId") String gameId, @Param("botSubscriberIds") List<Long> botSubscriberIds);
}
