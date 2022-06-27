package com.meinc.push.service;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IRmiPhoneServer extends Remote {
    public final static String rmiName = "PhoneServer";
    
    /**
     * @param host the hostname or IP address of the agent process
     * @return the agent/client ID 
     * @throws RemoteException
     */
    public int getAgentId(String host) throws RemoteException;
    
    /**
     * @param host the hostname or IP address of the agent process
     * @return the process ID for your agent/client
     * @throws RemoteException
     */
    public int getProcessId(String host) throws RemoteException;
    
    public void registerProcess(String host, int port) throws RemoteException;
    
    /**
     * @param lat0  center point
     * @param long0 center point
     * @param lat1  north west bound
     * @param long1 north west bound
     * @param lat2  south east bound
     * @param long2 south east bound
     */
    public void registerLocationBox(double lat0, double long0, int rad0, double lat1, double long1, double lat2, double long2) throws RemoteException;
    public void registerPlayerLocation(double latitude, double longitude, int geoError) throws RemoteException;
    public void outputLocationMap() throws RemoteException;
    public void registerPlayerLocationMatch(double latitude, double longitude) throws RemoteException;
}
