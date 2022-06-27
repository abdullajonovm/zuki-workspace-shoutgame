package com.meinc.push.service;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IRmiPhoneClient extends Remote {
    public final static String rmiName = "PhoneClient";
    public void ping() throws RemoteException;
    public void receiveMessages(PushMessage[] pushMessages) throws RemoteException;
}
