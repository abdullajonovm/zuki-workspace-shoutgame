package tv.shout.snowyowl.common;

import java.util.HashSet;
import java.util.Set;

import tv.shout.snowyowl.domain.Message;

public class MessageBus
{
    private static Set<MessageProcessor> _processors = new HashSet<>();

    public static void register(MessageProcessor messageProcessor)
    {
        _processors.add(messageProcessor);
    }

    public static void unregister(MessageProcessor messageProcessor)
    {
        _processors.remove(messageProcessor);
    }

    public static void sendMessage(Message message)
    {
        _processors.forEach(p -> p.handleMessage(message));
    }
}
