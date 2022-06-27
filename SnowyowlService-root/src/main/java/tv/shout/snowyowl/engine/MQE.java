package tv.shout.snowyowl.engine;

import io.socket.client.Socket;

/**
 * The MatchQueueEngine interface
 */
public interface MQE
{
    String getType();

    void start(Socket socketIoSocket);
    void stop();

    void subscriberCancelledQueuing(long subscriberId);
    void run(Void x);
}
