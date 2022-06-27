package tv.shout.snowyowl.engine;

import io.socket.client.Socket;

public interface MME
{
    String getType();

    void start(Socket socketIoSocket);
    void run(String id, String gameId, boolean isBracket);
    void stop();

    void notifyQuetionListChanged();
    void killProcess(String id);
}
