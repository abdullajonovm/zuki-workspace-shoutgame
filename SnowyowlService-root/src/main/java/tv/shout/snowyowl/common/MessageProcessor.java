package tv.shout.snowyowl.common;

import tv.shout.snowyowl.domain.Message;

/**
 * Any class that implements this interface can register/unregister/process messages from the message bus
 */
public interface MessageProcessor
{
    /**
     * A MessageProcessor can choose to handle (or ignore) any message that it receives
     *
     * @param message the message
     */
    void handleMessage(Message message);
}
