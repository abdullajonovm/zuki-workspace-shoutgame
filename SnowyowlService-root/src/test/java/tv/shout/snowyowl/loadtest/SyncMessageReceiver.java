package tv.shout.snowyowl.loadtest;

import tv.shout.sync.domain.SyncMessage;

interface SyncMessageReceiver
{
    /** called to indicate that the SyncMessageProvider is ready to begin sending SyncMessages */
    void syncMessageReceiverReady();

    /** called to indicate that the SyncMessageProvider could not connect and will not be providing SyncMessages */
    void syncMessageReceiverError();

    void syncMessageReceived(SyncMessage sm);
}
