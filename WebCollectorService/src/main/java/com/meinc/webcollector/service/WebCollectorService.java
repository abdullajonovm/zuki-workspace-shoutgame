package com.meinc.webcollector.service;

import java.io.File;
import java.io.IOException;
import java.security.KeyStore;
import java.security.cert.CRL;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.NCSARequestLog;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.handler.RequestLogHandler;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.server.ssl.SslSelectChannelConnector;
import org.eclipse.jetty.util.BlockingArrayQueue;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.meinc.http.jetty.CORSHandler;
import com.meinc.launcher.serverprops.ServerPropertyHolder;
import com.meinc.launcher.serverprops.ServerPropertyHolder.Change;
import com.meinc.launcher.serverprops.ServerPropertyHolder.ChangeListener;
import com.meinc.mrsoa.service.ServiceCallStack;
import com.meinc.mrsoa.service.ServiceEndpoint;
import com.meinc.mrsoa.service.annotation.OnStart;
import com.meinc.mrsoa.service.annotation.OnStop;
import com.meinc.mrsoa.service.annotation.Service;
import com.meinc.mrsoa.service.annotation.ServiceMethod;
import com.meinc.webcollector.message.CollectorMessage;
import com.meinc.webcollector.message.MessageProcessorDaemon;
import com.meinc.webcollector.message.handler.MessageTypeHandlerRegistry;

@Service(      name = WebCollectorService.WEBCOLLECTOR_SERVICE,
         interfaces = WebCollectorService.WEBCOLLECTOR_INTERFACE,
           exposeAs = IWebCollectorService.class)
public class WebCollectorService implements IWebCollectorService/*, IWebDataStoreCallback*/ {
    private static final Log log = LogFactory.getLog(WebCollectorService.class);
    private static final Log jettyLog = LogFactory.getLog("com.meinc.jetty.JettyStatistics");
    
    private static final String JETTY_THREADS_MAX_IDLE_MS = "jetty.threads.max.idle.ms";
    private static final String JETTY_THREADS_COUNT_MAX   = "jetty.threads.count.max";
    private static final String JETTY_THREADS_COUNT_MIN   = "jetty.threads.count.min";
    private static final String JETTY_PORT                = "jetty.port";

    private static final String JETTY_REQUEST_QUEUE_MAX_SIZE = "jetty.requests.max.queued";
    private static final String JETTY_LOG_STATS              = "jetty.log.connection.stats";
    private static final String JETTY_LOG_STATS_PERIOD_MS    = "jetty.log.connection.stats.period.ms";
    private static final String JETTY_SOCKET_LISTEN_BACKLOG  = "jetty.socket.listen.backlog";
    private static final String JETTY_SOCKET_TIMEOUT_MS      = "jetty.socket.timeout.ms";

    private QueuedThreadPool jettyThreadPool;
    
    private ChangeListener serverPropertiesChangeListener = new ChangeListener() {
        public void propertiesChanged(List<Change> changes) {
            for (Change change : changes) {
                processJettyProperty(change.key, change.newValue);
            }
        }
    };
    private Server _server;
    
    @Autowired
    private WebCollectorJettyHandler webCollectorHandler;
    
    @Autowired
    private MessageTypeHandlerRegistry messageTypeHandlerRegistry;
    
    @Autowired
    private MessageProcessorDaemon messageProcessorDaemon;
    
    @Value("${web.collector.message.buffer.path}")
    private String messageBufferPathString;
    private File messageBufferPath;
    
    private BlockingArrayQueue<Runnable> jettyRequestQueue;
    
    private Timer jettyStatusLoggerTimer;
    private TimerTask jettyStatusLoggerTask = new TimerTask() {
        public void run() {
            try {
                if (_server == null) 
                    // Not ready yet
                    return;

                StringBuffer msg = new StringBuffer();
                msg.append("\n=============== Jetty Statistics ===============\n");
                
                String logStatsString = ServerPropertyHolder.getProperty(JETTY_LOG_STATS, "false");
                boolean logStats = Boolean.parseBoolean(logStatsString);

                Connector[] connectors = _server.getConnectors();
                if (connectors == null)
                    // Not ready yet
                    return;
                
                for (Connector connector : connectors) {
                    if (!logStats) {
                        connector.setStatsOn(false);
                        continue;
                    }

                    int port = connector.getPort();
                    long requestCount = connector.getRequests();
                    int cxnOpenCount = connector.getConnectionsOpen();
                    int cxnMaxConcurrentOpenCount = connector.getConnectionsOpenMax();
                    long cxnDurationMsMean = (long) connector.getConnectionsDurationMean();
                    long cxnRequestCountMean = (long) connector.getConnectionsRequestsMean();
                    msg.append(String.format("Connector port=%d requestsHandled=%d openedCxns=%d maxConcurrentCxns=%d avgCxnDurationMs=%d avgRequestsPerCxn=%d\n",
                               port, requestCount, cxnOpenCount, cxnMaxConcurrentOpenCount, cxnDurationMsMean, cxnRequestCountMean));

                    connector.setStatsOn(true);
                }
                
                if (!logStats)
                    return;
                
                if (jettyThreadPool == null)
                    // Not ready yet?
                    return;
                
                int minThreadCount = jettyThreadPool.getMinThreads();
                int maxThreadCount = jettyThreadPool.getMaxThreads();
                int nowThreadCount = jettyThreadPool.getThreads();
                int idleThreadCount = jettyThreadPool.getIdleThreads();
                int activeThreadCount = nowThreadCount - idleThreadCount;
                msg.append(String.format("queuedRequests=%d activeThreads=%d idleThreads=%d totalThreads=%d minThreads=%d maxThreads=%d",
                                         jettyRequestQueue.size(), activeThreadCount, idleThreadCount, nowThreadCount, minThreadCount, maxThreadCount));
                
                jettyLog.info(msg);
            } catch (Exception e) {
                jettyLog.error("Error in Jetty Stats Logger: " + e.getMessage(), e);
            }
        }
    };
    
    @Override
    @ServiceMethod
    public void registerMessageTypeHandler(CollectorEndpoint collectorPath, String messageType, ServiceEndpoint messageTypeHandlerEndpoint) {
        messageTypeHandlerRegistry.registerMessageTypeHandler(collectorPath, messageType, messageTypeHandlerEndpoint);
    }
    
    @Override
    @ServiceMethod
    public void unregisterMessageTypeHandler(String messageType) {
        messageTypeHandlerRegistry.unregisterMessageTypeHandler(messageType);
    }

    private void processJettyProperty(String key, String value) {
        if (JETTY_THREADS_COUNT_MIN.equals(key)) {
            int threadsMin = Integer.parseInt(value);
            jettyThreadPool.setMinThreads(threadsMin);
        } else if (JETTY_THREADS_COUNT_MAX.equals(key)) {
            int threadsMax = Integer.parseInt(value);
            jettyThreadPool.setMaxThreads(threadsMax);
        } else if (JETTY_THREADS_MAX_IDLE_MS.equals(key)) {
            int maxIdleMs = Integer.parseInt(value);
            jettyThreadPool.setMaxIdleTimeMs(maxIdleMs);
        } else {
            throw new IllegalArgumentException("Illegal Jetty property: " + key);
        }
    }

    @Override
    @ServiceMethod
    @OnStart
    public void onServiceStart() throws IOException {
        boolean collectorDisabled = false;
        String disableCollector = ServerPropertyHolder.getProperty("disable.webcollector");
        if (disableCollector != null && disableCollector.toLowerCase().equals("true")) {
            log.info("Web Collector is Disabled");
            collectorDisabled = true;
        }

        String bindSocketString = ServerPropertyHolder.getProperty("webcollector.bind.socket", "true");
        boolean bindCollectorSocket = Boolean.parseBoolean(bindSocketString);
        
        if (collectorDisabled) {
            return;
        }
        
        /* Set up buffer path */
        
        if (messageBufferPathString == null || messageBufferPathString.trim().isEmpty())
            throw new IllegalStateException("Missing message buffer path property");
        messageBufferPath = new File(messageBufferPathString);
        if (!messageBufferPath.exists() && !messageBufferPath.mkdirs())
            throw new IllegalStateException("Could not access message buffer path at " + messageBufferPathString);
        if (!messageBufferPath.isDirectory() || !messageBufferPath.canRead() || !messageBufferPath.canWrite())
            throw new IllegalStateException("Could not read/write to message buffer path at " + messageBufferPathString);
        
        /* Start message processor daemon */
        
        messageProcessorDaemon.start();
        
        /* Start Jetty */
        
        ServiceCallStack.setCurrentServiceClassLoader(ServiceCallStack.THREAD_CONTEXT_LOADER);
        
        //initialize the appHelper map
        
        if (!bindCollectorSocket)
            return;
        
        String requestQueueSizeString = ServerPropertyHolder.getProperty(JETTY_REQUEST_QUEUE_MAX_SIZE);
        if (requestQueueSizeString != null) {
            int requestQueueSize = 0;
            try {
                requestQueueSize = Integer.parseInt(requestQueueSizeString);
            } catch (NumberFormatException e) {
                log.error("Invalid value for " + JETTY_REQUEST_QUEUE_MAX_SIZE + ": " + requestQueueSizeString);
            }
            int maxQueueSize = Math.max(requestQueueSize, 0);
            int minQueueSize = maxQueueSize / 2;
            int incrementSize = (maxQueueSize == 0) ? 0 : Math.max(maxQueueSize/3, 1);
            jettyRequestQueue = new BlockingArrayQueue<Runnable>(minQueueSize, incrementSize, maxQueueSize);
        } else {
            jettyRequestQueue = new BlockingArrayQueue<Runnable>();
        }
        jettyThreadPool = new BraveQueuedThreadPool(jettyRequestQueue);
        
        processJettyProperty(JETTY_THREADS_COUNT_MIN, ServerPropertyHolder.getProperty(JETTY_THREADS_COUNT_MIN, "10"));
        processJettyProperty(JETTY_THREADS_COUNT_MAX, ServerPropertyHolder.getProperty(JETTY_THREADS_COUNT_MAX, "100"));
        processJettyProperty(JETTY_THREADS_MAX_IDLE_MS, ServerPropertyHolder.getProperty(JETTY_THREADS_MAX_IDLE_MS, "600000"));
        
        ServerPropertyHolder.addPropertyChangeListener("jetty\\..*", serverPropertiesChangeListener);

        int collectorPort = Integer.parseInt(ServerPropertyHolder.getProperty("webcollector.port", "0"));
        int jettyPort = (collectorPort != 0) ? collectorPort : Integer.parseInt(ServerPropertyHolder.getProperty(JETTY_PORT, "8080"));
        
        String jettyStatusLoggerPeriodMsString = ServerPropertyHolder.getProperty(JETTY_LOG_STATS_PERIOD_MS, "60000");
        int jettyStatusLoggerPeriodMs = 60000;
        try {
            jettyStatusLoggerPeriodMs = Integer.parseInt(jettyStatusLoggerPeriodMsString);
        } catch (NumberFormatException e) {
            log.error("Invalid value for " + JETTY_LOG_STATS_PERIOD_MS + ": " + jettyStatusLoggerPeriodMsString);
        }

        jettyStatusLoggerTimer = new Timer("JettyStatusLogger", true);
        jettyStatusLoggerTimer.schedule(jettyStatusLoggerTask, jettyStatusLoggerPeriodMs, jettyStatusLoggerPeriodMs);
        
        _server = new Server();
        
        SelectChannelConnector nioConnector = new SelectChannelConnector();

        int acceptQueueSize = 0;
        String acceptQueueSizeString = ServerPropertyHolder.getProperty(JETTY_SOCKET_LISTEN_BACKLOG, "0");
        try {
            acceptQueueSize = Integer.parseInt(acceptQueueSizeString);
        } catch (NumberFormatException e) {
            log.error("Invalid value for " + JETTY_SOCKET_LISTEN_BACKLOG + ": " + acceptQueueSizeString);
        }
        nioConnector.setAcceptQueueSize(acceptQueueSize);

        int socketTimeoutMs = 200000;
        String socketTimeoutMsString = ServerPropertyHolder.getProperty(JETTY_SOCKET_TIMEOUT_MS, "200000");
        try {
            socketTimeoutMs = Integer.parseInt(socketTimeoutMsString);
        } catch (NumberFormatException e) {
            log.error("Invalid value for " + JETTY_SOCKET_TIMEOUT_MS + ": " + socketTimeoutMsString);
        }
        nioConnector.setMaxIdleTime(socketTimeoutMs);

        //We add one to the port as a temporary measure to allow JettyService and WebCollectorService to coexist during development
        log.info("Starting Web Collector on port " + jettyPort);
        nioConnector.setPort(jettyPort);
        nioConnector.setAcceptors(2 * Runtime.getRuntime().availableProcessors() - 1);
        _server.addConnector(nioConnector);

        String collectorKeystoreFilePath = ServerPropertyHolder.getProperty("webcollector.ssl.keystore.file");
        String sslKeystoreFilePath = (collectorKeystoreFilePath != null) ? collectorKeystoreFilePath : ServerPropertyHolder.getProperty("ssl.keystore.file");
        String keyStorePath = null;
        if (sslKeystoreFilePath != null) {
            File keyStoreFile = new File(sslKeystoreFilePath.trim());
            if (keyStoreFile.exists())
                keyStorePath = keyStoreFile.getCanonicalPath();
        }
        
        if (keyStorePath == null) {
            log.info("WebCollectorService launching without SSL");
        } else {
            SslContextFactory sslContextFactory = new CollectorSslContextFactory();

            sslContextFactory.setKeyStorePath(keyStorePath);
            String collectorKeyStorePassword = ServerPropertyHolder.getProperty("webcollector.ssl.keystore.password");
            String keyStorePassword = (collectorKeyStorePassword != null) ? collectorKeyStorePassword : ServerPropertyHolder.getProperty("ssl.keystore.password");
            if (collectorKeyStorePassword != null)
                keyStorePassword = collectorKeyStorePassword;
            if (keyStorePassword != null)
                sslContextFactory.setKeyStorePassword(keyStorePassword);

            String trustStorePath = ServerPropertyHolder.getProperty("webcollector.ssl.truststore.file");
            trustStorePath = trustStorePath != null ? trustStorePath : ServerPropertyHolder.getProperty("ssl.truststore.file");
            String trustStorePassword = ServerPropertyHolder.getProperty("webcollector.ssl.truststore.password");
            trustStorePassword = trustStorePassword != null ? trustStorePassword : ServerPropertyHolder.getProperty("ssl.truststore.password");
            if (trustStorePath != null && trustStorePassword != null) {
                log.info("Launching with SSL truststore");
                sslContextFactory.setTrustStore(trustStorePath);
                sslContextFactory.setTrustStorePassword(trustStorePassword);
                sslContextFactory.setWantClientAuth(true);
            }

            SslSelectChannelConnector sslNioConnector = new SslSelectChannelConnector(sslContextFactory);
            int collectorHttpsPort = Integer.parseInt(ServerPropertyHolder.getProperty("webcollector.https.port", "0"));
            int jettyHttpsPort = (collectorHttpsPort != 0) ? collectorHttpsPort : Integer.parseInt(ServerPropertyHolder.getProperty("jetty.https.port", "8443"));
            sslNioConnector.setPort(jettyHttpsPort);
            sslNioConnector.setAcceptors(2 * Runtime.getRuntime().availableProcessors() - 1);
            sslNioConnector.setMaxIdleTime(30000);
            log.info("Launching with HTTPS on port " + jettyHttpsPort);
            _server.addConnector(sslNioConnector);
        }
        
        String mrsoaHome = System.getProperty("mrsoa.home", "/opt/meinc/mrsoa");
        File logsRoot = new File(mrsoaHome, "logs");
        File accessLog = new File(logsRoot, "access-yyyy_mm_dd.log");
        NCSARequestLog requestLogger = new NCSARequestLog();
        try {
            requestLogger.setFilename(accessLog.getCanonicalPath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        requestLogger.setRetainDays(31);
        requestLogger.setAppend(true);
        requestLogger.setExtended(false);
        requestLogger.setLogTimeZone("UTC");
        requestLogger.setLogLatency(true);
        
        RequestLogHandler requestLogHandler = new RequestLogHandler();
        requestLogHandler.setRequestLog(requestLogger);
        
        HandlerCollection handlers = new HandlerCollection();
        handlers.addHandler(new CORSHandler(true));
        handlers.addHandler(webCollectorHandler);
        handlers.addHandler(requestLogHandler);

        _server.setThreadPool(jettyThreadPool);
        _server.setHandler(handlers);
        _server.setStopAtShutdown(true);
        _server.setSendDateHeader(true);
        _server.setSendServerVersion(false);
        _server.setGracefulShutdown(3000);

        try {
            _server.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    @Override
    @ServiceMethod
    @OnStop
    public void onServiceStop() {
        /* Stop Jetty */
        
        ServerPropertyHolder.removePropertyChangeListener(serverPropertiesChangeListener);
        if (_server != null)
            try {
                _server.stop();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        
        messageProcessorDaemon.shutdown();
    }

    @Override
    @ServiceMethod
    public String addMessageToBuffer(CollectorMessage message) {
        try {
            return webCollectorHandler.addMessageToBuffer(message);
        } catch (InterruptedException e) {
            log.error("Error adding message to buffer: " + e.getMessage(), e);
            return null;
        } catch (IOException e) {
            log.error("Error adding message to buffer: " + e.getMessage(), e);
            return null;
        }
    }

    private static class BraveQueuedThreadPool extends QueuedThreadPool {
        public BraveQueuedThreadPool(BlockingArrayQueue<Runnable> jettyRequestQueue) {
            super(jettyRequestQueue);
        }

        @Override
        public boolean isLowOnThreads() {
            return false;
        }
    }
    
    private static class CollectorSslContextFactory extends SslContextFactory {
        @Override
        protected TrustManager[] getTrustManagers(KeyStore trustStore, Collection<? extends CRL> crls) throws Exception {
            TrustManager[] managers = super.getTrustManagers(trustStore, crls);

            WebCollectorJettyHandler.setTrustedCaNameByIssuerDn(
                            Arrays.stream(managers)
                                  .filter(mgr -> mgr instanceof X509TrustManager)
                                  .map(mgr -> (X509TrustManager) mgr)
                                  .flatMap(mgr -> Arrays.stream(mgr.getAcceptedIssuers()))
                                  .map(X509Certificate::getIssuerDN)
                                  .collect(Collectors.toMap(Function.identity(),
                                                            p -> Arrays.stream(p.getName().split(", "))
                                                                       .filter(pnPart -> pnPart.startsWith("CN="))
                                                                       .map(pnPart -> pnPart.substring(3))
                                                                       .findFirst()
                                                                           .orElse(null) )));

            return managers;
        }
    }
}
