package tv.shout.sync.collector;

import com.meinc.webcollector.message.CollectorMessage;
import com.meinc.webcollector.message.handler.BadRequestException;
import com.meinc.webcollector.message.handler.IMessageTypeHandler;

public interface ICollectorMessageHandler 
extends IMessageTypeHandler
{
    public void handleMessage(CollectorMessage message) throws PublishResponseError, BadRequestException;
}