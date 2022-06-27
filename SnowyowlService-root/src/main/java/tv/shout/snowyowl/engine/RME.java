package tv.shout.snowyowl.engine;

import io.socket.client.Socket;

public interface RME
{
    String getType();

    void start(Socket socketIoSocket);
    void stop();
}
