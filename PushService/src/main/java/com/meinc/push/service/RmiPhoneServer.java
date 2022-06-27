package com.meinc.push.service;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.log4j.Logger;

import com.meinc.launcher.serverprops.ServerPropertyHolder;
import com.meinc.mrsoa.distdata.simple.DistributedMap;
import com.meinc.push.domain.SubscriberToken;

public class RmiPhoneServer implements IRmiPhoneServer {

    private static Logger _log = Logger.getLogger(RmiPhoneServer.class);

    private RmiRouter rmiRouter;
    private RmiPinger rmiPinger;

    //~ private DistributedLong processCount;
    //~ private DistributedLong agentCount;
    private AtomicLong processCount = new AtomicLong();
    private AtomicLong agentCount = new AtomicLong();
    private DistributedMap<String,Long> processes;
    private DistributedMap<String,Long> agents;
    private DistributedMap<InetSocketAddress,Object> processAddresses;

    private Map<InetSocketAddress,RmiSender> senders = new HashMap<InetSocketAddress,RmiSender>();

    public RmiPhoneServer() {
        /*if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }*/
        
        processes = DistributedMap.getMap("clientProcesses");
        agents = DistributedMap.getMap("clientAgents");
        processAddresses = DistributedMap.getMap("processAddresses");
        
        rmiRouter = new RmiRouter();
        rmiRouter.setDaemon(true);
        rmiRouter.start();

        rmiPinger = new RmiPinger();
        rmiPinger.setDaemon(true);
        rmiPinger.start();

        String localHost = null;
        Enumeration<NetworkInterface> netIfaces;
        try {
            netIfaces = NetworkInterface.getNetworkInterfaces();
            while (netIfaces.hasMoreElements()) {
                NetworkInterface netIface = netIfaces.nextElement();
                if (netIface.isLoopback())
                    continue;
                Enumeration<InetAddress> addresses = netIface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress ip = addresses.nextElement();
                    if (ip.getAddress().length == 4)
                        localHost = ip.getHostName();
                }
            }
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
        _log.info("Detected local host of: " + localHost);
        
        int exportPort = Integer.parseInt(ServerPropertyHolder.getProperty("push.service.rmi.port", "1100"));

        System.setProperty("java.rmi.server.hostname", localHost);
        try {
            Registry localRegistry = LocateRegistry.createRegistry(Registry.REGISTRY_PORT);
            try {
                localRegistry.lookup(IRmiPhoneServer.rmiName);
            } catch (NotBoundException e) {
                IRmiPhoneServer rmiPhoneServerStub = (IRmiPhoneServer) UnicastRemoteObject.exportObject(this, exportPort);
                localRegistry.rebind(IRmiPhoneServer.rmiName, rmiPhoneServerStub);
            }
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int getAgentId(String host) throws RemoteException {
        Long agentId = agents.get(host);
        if (agentId == null) {
            agentId = agentCount.getAndIncrement();
            agents.put(host, agentId);
        }
        return agentId.intValue();
    }

    @Override
    public int getProcessId(String host) throws RemoteException {
        Long processId = processes.get(host);
        if (processId == null) {
            processId = processCount.incrementAndGet();
            processes.put(host, processId);
        }
        return processId.intValue();
    }

    @Override
    public void registerProcess(String host, int port) throws RemoteException {
        _log.info("Test process registered at " + host + ":" + port);
        
        InetSocketAddress processAddress = InetSocketAddress.createUnresolved(host, port);
        
        processAddresses.put(processAddress, true);
        
        synchronized (senders) {
            senders.put(processAddress, new RmiSender(processAddress));
        }
    }

    private void purgeProcess(InetSocketAddress processAddress) {
        _log.info("Purging remote client " + processAddress);
        
        processAddresses.remove(processAddress);
        
        synchronized (senders) {
            RmiSender sender = senders.remove(processAddress);
            if (sender != null)
                sender.interrupt();
        }
        
        Long agentId = agents.remove(processAddress.getHostName());
        if (agentId != null)
            agentCount.decrementAndGet();

        Long processId = processes.remove(processAddress.getHostName());
        if (processId != null)
            processCount.decrementAndGet();
        
        overlaysString = new StringBuffer();
    }

    private String gMapStringTemplate =
        "<!DOCTYPE html>\n" +
        "<html>\n" +
        "  <head>\n" +
        "    <meta name=\"viewport\" content=\"initial-scale=1.0, user-scalable=no\" />\n" +
        "    <style type=\"text/css\">\n" +
        "      html { height: 100% }\n" +
        "      body { height: 100%; margin: 0; padding: 0 }\n" +
        "      #map_canvas { height: 100% }\n" +
        "    </style>\n" +
        "    <script type=\"text/javascript\"\n" +
        "      src=\"https://maps.googleapis.com/maps/api/js?key=AIzaSyCP2H6h6ho4gpy4sxMg24at_y-kAJ6_vVA&sensor=false\">\n" +
        "    </script>\n" +
        "    <script type=\"text/javascript\">\n" +
        "      function initialize() {\n" +
        "        var mapOptions = {\n" +
        "          center: new google.maps.LatLng(<lat0/>, <long0/>),\n" +
        "          zoom: 12,\n" +
        "          mapTypeId: google.maps.MapTypeId.ROADMAP\n" +
        "        };\n" +
        "        var map = new google.maps.Map(document.getElementById(\"map_canvas\"),\n" +
        "            mapOptions);\n" +
        "        // Get the current bounds, which reflect the bounds before the zoom.\n" +
        "        var rectOptions = {\n" +
        "          strokeColor: \"#0000FF\",\n" +
        "          strokeOpacity: 0.8,\n" +
        "          strokeWeight: 2,\n" +
        "          fillColor: \"#0000FF\",\n" +
        "          fillOpacity: 0.15,\n" +
        "          map: map,\n" +
        "          bounds: new google.maps.LatLngBounds(\n" +
        "            new google.maps.LatLng(<lat1/>, <long1/>),\n" +
        "            new google.maps.LatLng(<lat2/>, <long2/>) )\n" +
        "        };\n" +
        "        var rectangle = new google.maps.Rectangle(rectOptions);\n" +
        "        var circleOptions = {\n" +                                                              
        "          strokeColor: \"#00FF00\",\n" +                                                        
        "          strokeOpacity: 0.8,\n" +                                                              
        "          strokeWeight: 2,\n" +                                                                 
        "          fillColor: \"#00FF00\",\n" +                                                          
        "          fillOpacity: 0.35,\n" +                                                               
        "          map: map,\n" +                                                                        
        "          center: new google.maps.LatLng(<lat0/>, <long0/>),\n" +                                          
        "          radius: 400 };\n" +                                                                    
        "        var stadiumCircle = new google.maps.Circle(circleOptions);\n" +
        "        <overlays/>\n" +
        "      }\n" +
        "    </script>\n" +
        "  </head>\n" +
        "  <body onload=\"initialize()\">\n" +
        "    <div id=\"map_canvas\" style=\"width:100%; height:100%\"></div>\n" +
        "  </body>\n" +
        "</html>\n";

    private StringBuffer overlaysString = new StringBuffer();
    private double lat0, long0, rad0, lat1, long1, lat2, long2;

    @Override
    public void registerLocationBox(double lat0, double long0, int rad0, double lat1, double long1, double lat2, double long2) throws RemoteException {
        this.lat0 = lat0;
        this.long0 = long0;
        this.rad0 = rad0;
        this.lat1 = lat1;
        this.long1 = long1;
        this.lat2 = lat2;
        this.long2 = long2;
    }
    
    @Override
    public void registerPlayerLocation(double latitude, double longitude, int geoError) throws RemoteException {
        //overlaysString.append(
        //    String.format("        var circleOptions = {\n" +
        //                  "          strokeColor: \"#FF0000\",\n" +
        //                  "          strokeOpacity: 0.8,\n" +
        //                  "          strokeWeight: 2,\n" +
        //                  "          fillColor: \"#FF0000\",\n" +
        //                  "          fillOpacity: 0.35,\n" +
        //                  "          map: map,\n" +
        //                  "          center: new google.maps.LatLng(%f, %f),\n" +
        //                  "          radius: %d };\n" +
        //                  "        var playerCircle = new google.maps.Circle(circleOptions);\n", latitude, longitude, geoError) );
    }
    
    @Override
    public void registerPlayerLocationMatch(double latitude, double longitude) throws RemoteException {
        //overlaysString.append(
        //    String.format("        var marker = new google.maps.Marker({\n" +
        //                  "          position: new google.maps.LatLng(%f, %f),\n" +
        //                  "          map: map });\n", latitude, longitude) );
    }
    
    @Override
    public void outputLocationMap() throws RemoteException {
        String template = gMapStringTemplate;
        template = template.replaceAll("<lat0/>",     ""+lat0);
        template = template.replaceAll("<long0/>",    ""+long0);
        template = template.replaceAll("<rad0/>",    ""+rad0);
        template = template.replaceAll("<lat1/>",     ""+lat1);
        template = template.replaceAll("<long1/>",    ""+long1);
        template = template.replaceAll("<lat2/>",     ""+lat2);
        template = template.replaceAll("<long2/>",    ""+long2);
        template = template.replaceAll("<overlays/>", overlaysString.toString());
        try {
            FileOutputStream fos = new FileOutputStream("/tmp/barclays.html");
            fos.write(template.getBytes());
            fos.close();
        } catch (FileNotFoundException e) {
            _log.error(e);
        } catch (IOException e) {
            _log.error(e);
        }
    }

    private class RmiPinger extends Thread {
        @Override
        public void run() {
            while (!isInterrupted()) {
                List<InetSocketAddress> badSenders = new ArrayList<InetSocketAddress>();
                synchronized (senders) {
                    for (Entry<InetSocketAddress,RmiSender> senderEntry : senders.entrySet()) {
                            try {
                                senderEntry.getValue().ping();
                            } catch (Exception e) {
                                _log.error(e.getMessage());
                                badSenders.add(senderEntry.getKey());
                            }
                    }
                }
                if (!badSenders.isEmpty()) {
                    for (InetSocketAddress badSender : badSenders)
                        purgeProcess(badSender);
                }
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    interrupt();
                    continue;
                }
            }
        }
    }

    private class RmiRouter extends Thread {
        private LinkedBlockingQueue<PushMessage> pushMessages = new LinkedBlockingQueue<PushMessage>();

        public void queuePushMessage(PushMessage pushMessage) {
            pushMessages.add(pushMessage);
        }

        @Override
        public void run() {
            throw new UnsupportedOperationException("RmiPhoneServer isn't used anymore. Fix me if you want to use me.");
            /*
            while (!isInterrupted()) {
                try {
                    List<PushMessage> messageList = new ArrayList<PushMessage>();
                    try {
                        messageList.add(pushMessages.take());
                    } catch (InterruptedException e) {
                        interrupt();
                        continue;
                    }
                    pushMessages.drainTo(messageList);
                    _log.info("Sending push to " + messageList.size() + " phones: " + messageList.get(0).payload);
                    synchronized (senders) {
                        for (InetSocketAddress processAddress : processAddresses.keySet()) {
                            if (!senders.containsKey(processAddress))
                                try {
                                    senders.put(processAddress, new RmiSender(processAddress));
                                } catch (Exception e) {
                                    _log.error("Could not connect to client at " + processAddress + ": " + e.getMessage());
                                }
                        }
                        for (RmiSender sender : senders.values()) {
                            _log.info("queuing...");
                            sender.queuePushMessages(messageList);
                        }
                    }
                } catch (Exception e) {
                    _log.error(e.getMessage(), e);
                }
            }
            */
        }
    }
    
    private class RmiSender extends Thread {
        private IRmiPhoneClient stub;
        private InetSocketAddress stubAddress;
        private LinkedBlockingQueue<List<PushMessage>> pushMessages = new LinkedBlockingQueue<List<PushMessage>>();

        private RmiSender(InetSocketAddress stubAddress) {
            this.stubAddress = stubAddress;
            
            try {
                Registry phoneServerRegistry = LocateRegistry.getRegistry(stubAddress.getHostName(), stubAddress.getPort());
                stub = (IRmiPhoneClient) phoneServerRegistry.lookup(IRmiPhoneClient.rmiName);
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            } catch (NotBoundException e) {
                throw new RuntimeException(e);
            }
            
            setDaemon(true);
            start();
        }
        
        public void queuePushMessages(List<PushMessage> messages) {
            pushMessages.add(messages);
        }
        
        public void ping() throws RemoteException {
            stub.ping();
        }

        @Override
        public void run() {
            _log.info("sender starting...");
            List<PushMessage> messages = new ArrayList<PushMessage>();
            while (!isInterrupted()) {
                messages.clear();
                try {
                    messages.addAll(pushMessages.take());
                } catch (InterruptedException e) {
                    interrupt();
                    continue;
                }
                List<PushMessage> next = pushMessages.poll();
                while (next != null) {
                    messages.addAll(next);
                    next = pushMessages.poll();
                }
                
                _log.info("Sending messages for " + messages.size() + " phones to " + stubAddress);
                try {
                    stub.receiveMessages((PushMessage[])messages.toArray(new PushMessage[] {}));
                    _log.info("Finished sending messages for " + messages.size() + " phones to " + stubAddress);
                } catch (RemoteException e) {
                    _log.error(e.getMessage());
                    purgeProcess(stubAddress);
                    interrupt();
                    continue;
                }
            }
            _log.info("sender exiting...");
        }
    }

    public void pushToAndroid(List<SubscriberToken> tokens, String payload) {
        ArrayList<String> stringTokens = new ArrayList<String>(tokens.size());
        for (SubscriberToken token : tokens)
            stringTokens.add(token.getDeviceToken());
        PushMessage pushMessage = new PushMessage();
        pushMessage.type = PushMessage.ANDROID;
        pushMessage.tokens = stringTokens;
        pushMessage.payload = payload;
        rmiRouter.queuePushMessage(pushMessage);
    }

    public void pushToIOS(SubscriberToken token, String payload) {
        PushMessage pushMessage = new PushMessage();
        pushMessage.type = PushMessage.IOS;
        ArrayList<String> tokens = new ArrayList<String>(1);
        tokens.add(token.getDeviceToken());
        pushMessage.tokens = tokens;
        pushMessage.payload = payload;
        rmiRouter.queuePushMessage(pushMessage);
    }

    public void pushToWindows(List<SubscriberToken> tokens, String payload) {
        ArrayList<String> stringTokens = new ArrayList<String>(tokens.size());
        for (SubscriberToken token : tokens)
            stringTokens.add(token.getDeviceToken());
        PushMessage pushMessage = new PushMessage();
        pushMessage.type = PushMessage.WINDOWS;
        pushMessage.tokens = stringTokens;
        pushMessage.payload = payload;
        rmiRouter.queuePushMessage(pushMessage);
    }

}
