package com.meinc.webdatastore.store;

import static com.meinc.webdatastore.service.WebDataStoreException.Type.DUPLICATE;
import static com.meinc.webdatastore.service.WebDataStoreException.Type.MISSING;
import static com.meinc.webdatastore.store.Store.UploadType.CREATE_ONLY;
import static com.meinc.webdatastore.store.Store.UploadType.UPDATE_ONLY;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Properties;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.client.entity.GzipDecompressingEntity;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.FormBodyPart;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.impl.nio.DefaultHttpClientIODispatch;
import org.apache.http.impl.nio.pool.BasicNIOConnPool;
import org.apache.http.impl.nio.reactor.DefaultConnectingIOReactor;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.impl.nio.reactor.IOReactorConfig.Builder;
import org.apache.http.message.BasicHttpEntityEnclosingRequest;
import org.apache.http.message.BasicHttpRequest;
import org.apache.http.nio.entity.NByteArrayEntity;
import org.apache.http.nio.protocol.BasicAsyncRequestProducer;
import org.apache.http.nio.protocol.BasicAsyncResponseConsumer;
import org.apache.http.nio.protocol.HttpAsyncRequestExecutor;
import org.apache.http.nio.protocol.HttpAsyncRequester;
import org.apache.http.nio.reactor.IOEventDispatch;
import org.apache.http.nio.reactor.IOReactorException;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpCoreContext;
import org.apache.http.protocol.HttpProcessor;
import org.apache.http.protocol.HttpProcessorBuilder;
import org.apache.http.protocol.RequestConnControl;
import org.apache.http.protocol.RequestContent;
import org.apache.http.protocol.RequestTargetHost;
import org.apache.http.protocol.RequestUserAgent;
import org.springframework.beans.factory.annotation.Autowired;

import com.meinc.commons.encryption.HexUtils;
import com.meinc.launcher.serverprops.ServerPropertyHolder;
import com.meinc.launcher.serverprops.ServerPropertyHolder.Change;
import com.meinc.launcher.serverprops.ServerPropertyHolder.ChangeListener;
import com.meinc.webdatastore.domain.RepeatWebDataStoreObject;
import com.meinc.webdatastore.domain.WebDataStoreObject;
import com.meinc.webdatastore.domain.WebDataStoreObject.Endpoint;
import com.meinc.webdatastore.domain.WebDataStoreObject.Endpoint.Root;
import com.meinc.webdatastore.domain.WebDataStoreObjectOperation;
import com.meinc.webdatastore.service.IWebDataStoreCallback.WebDataStoreCallbackType;
import com.meinc.webdatastore.service.IWebDataStoreService;
import com.meinc.webdatastore.service.WebDataStoreException;
import com.meinc.webdatastore.util.HttpDateUtil;

import tv.shout.util.MultiMap;

public class ShoutWebStore extends Store {
    private final Log log = LogFactory.getLog(ShoutWebStore.class);

    @Autowired
    private ExecutorService wdsExecutor;
    
    private Method addPartMethod;
    private DefaultConnectingIOReactor ioReactor;
    private BasicNIOConnPool pool;
    private HttpAsyncRequester requester;

    private ReadWriteLock hostsLock = new ReentrantReadWriteLock();
    private List<SWHost> originHosts;
    private List<SWHost> cacheHosts;
    private MultiMap<Integer,SWHost> originHostsByPartition = new MultiMap<Integer,SWHost>();
    private MultiMap<Integer,SWHost> cacheHostsByPartition = new MultiMap<Integer,SWHost>();
    SWHostFinder hostFinder = new SWHostFinder();

    public ShoutWebStore(final PropertyKeySource propertyKeySource) {
        if (!readServerProperties(propertyKeySource)) {
            log.warn(String.format("A ShoutWebStore could not be loaded because server properties '%s' and '%s' could not be found",
                                   propertyKeySource.getOriginHostsPropertyKey(),
                                   propertyKeySource.getCacheHostsPropertyKey()));
            return;
        }

        ServerPropertyHolder.addPropertyChangeListener("webdatastore\\.shoutweb\\..*", new ChangeListener() {
            public void propertiesChanged(List<Change> properties) {
                readServerProperties(propertyKeySource);
            }
        });

        Class<? extends MultipartEntityBuilder> builderClass = MultipartEntityBuilder.class;
        try {
            addPartMethod = builderClass.getDeclaredMethod("addPart", FormBodyPart.class);
        } catch (SecurityException e) {
            throw new IllegalStateException("Unexpected restriction of class " + MultipartEntityBuilder.class, e);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("Unexpected version of class " + MultipartEntityBuilder.class, e);
        }
        addPartMethod.setAccessible(true);

        // Start the IO reactor which devotes async/NIO threads for all HTTP requests
        Builder ioConfigBuilder = IOReactorConfig.custom();
        ioConfigBuilder.setSoTimeout(30000);
        try {
            ioReactor = new DefaultConnectingIOReactor(ioConfigBuilder.build());
        } catch (IOReactorException e) {
            log.error("ShoutWebStore failed to create io reactor", e);
            return;
        }
        Thread t = new Thread("ShoutWebStore IO Reactor") {
            public void run() {
                HttpAsyncRequestExecutor protocolHandler = new HttpAsyncRequestExecutor();
                IOEventDispatch ioEventDispatch = new DefaultHttpClientIODispatch(protocolHandler, ConnectionConfig.DEFAULT);
				try {
                    ioReactor.execute(ioEventDispatch);
                } catch (InterruptedIOException e) {
                    // Exit
                } catch (IOReactorException e) {
                    ioReactor = null;
                    log.error("ShoutWebStore failed to start io reactor", e);
                }
            }
        };
        t.setDaemon(true);
        t.start();

        // Create HTTP connection pool
        pool = new BasicNIOConnPool(ioReactor, 30000, ConnectionConfig.DEFAULT);
        pool.setDefaultMaxPerRoute(100);
        pool.setMaxTotal(1000);

        // Create requester object with the following defaults for requests
        HttpProcessor httpproc = HttpProcessorBuilder.create()
                                                     .add(new RequestContent())                        //adds Content-Length header
                                                     .add(new RequestTargetHost())                     //adds Host header
                                                     .add(new RequestConnControl())                    //adds Connection: keep-alive header
                                                     .add(new RequestUserAgent("WebDataStoreService")) //adds User-Agent header
                                                     .add(new HttpResponseInterceptor() {
                                                        public void process(HttpResponse response, HttpContext context) throws HttpException, IOException {
                                                            HttpEntity entity = response.getEntity();
                                                            if (entity != null) {
                                                                Header ceheader = entity.getContentEncoding();
                                                                if (ceheader != null) {
                                                                    HeaderElement[] codecs = ceheader.getElements();
                                                                    for (int i = 0; i < codecs.length; i++) {
                                                                        if (codecs[i].getName().equalsIgnoreCase("gzip")) {
                                                                            response.setEntity(new GzipDecompressingEntity(response.getEntity()));
                                                                            return;
                                                                        }
                                                                    }
                                                                }
                                                            }

                                                        }
                                                    })
                                                    .build();
        requester = new HttpAsyncRequester(httpproc);
    }
    
    private boolean readServerProperties(PropertyKeySource propertyKeySource) {
        if (!propertyKeySource.propertiesExist())
            return false;
        
        Lock lock = hostsLock.writeLock();
        lock.lock();
        try {
            hostFinder.clear();

            //String key = "webdatastore.shoutweb.origin.hosts";
            String key = propertyKeySource.getOriginHostsPropertyKey();
            String originHostsString = ServerPropertyHolder.getProperty(key);
            if (originHostsString == null)
                throw new IllegalStateException("Missing required property " + key);
            originHosts = SWHost.parseSpecialHostsString(originHostsString);
            for (SWHost originHost : originHosts) {
                hostFinder.addOrigin(originHost);
                originHostsByPartition.put(originHost.partition, originHost);
            }

            //key = "webdatastore.shoutweb.cache.hosts";
            key = propertyKeySource.getCacheHostsPropertyKey();
            String cacheHostsString = ServerPropertyHolder.getProperty(key);
            cacheHosts = SWHost.parseSpecialHostsString(cacheHostsString);
            if (cacheHostsString == null)
                throw new IllegalStateException("Missing required property " + key);
            for (SWHost cacheHost : cacheHosts) {
                hostFinder.addCache(cacheHost);
                cacheHostsByPartition.put(cacheHost.partition, cacheHost);
            }
            
            String keyPrefix = propertyKeySource.getAliasPrefixPropertyKey();
            if (keyPrefix != null) {
                Properties serverProps = ServerPropertyHolder.getProps();
                for (Entry<Object,Object> serverPropEntry : serverProps.entrySet()) {
                    String propKey = (String) serverPropEntry.getKey();
                    if (propKey.startsWith(keyPrefix)) {
                        String aliasName = propKey.substring(keyPrefix.length()+1);
                        String aliasTargetString = ((String) serverPropEntry.getValue()).trim();
                        SWHost aliasTarget = hostFinder.findHost(aliasTargetString)
                            .orElseThrow(() -> 
                                new IllegalArgumentException(String.format("No such alias target: %s -> %s", aliasName, aliasTargetString))
                            );
                        hostFinder.addAlias(aliasName, aliasTarget);
                    }
                }
            }
        } finally {
            lock.unlock();
        }
        return true;
    }
    
    @Override
    Type getType() {
        return Store.Type.SHOUT_WEB;
    }

    private static class SWHostFinder {
        private static Pattern toWdsHostPattern = Pattern.compile("^(?:https?://)?([a-zA-Z0-9.-]+)(?::(\\d+))?");
        private Map<String,SWHost> originHostByHostnameAndPort = new HashMap<String,SWHost>();
        private Map<String,SWHost> cacheHostByHostnameAndPort = new HashMap<String,SWHost>();
        private Map<String,SWHost> originHostByAlias = new HashMap<String,SWHost>();
        private Map<String,SWHost> cacheHostByAlias = new HashMap<String,SWHost>();
        
        private void addOrigin(SWHost originHost) {
            String hostnameAndPort = originHost.getHostnameAndPort();
            SWHost old = originHostByHostnameAndPort.put(hostnameAndPort, originHost);
            if (old != null || cacheHostByHostnameAndPort.containsKey(hostnameAndPort))
                throw new IllegalArgumentException("WDS hostname and port used twice: " + hostnameAndPort);
        }
        
        private void addCache(SWHost cacheHost) {
            String hostnameAndPort = cacheHost.getHostnameAndPort();
            SWHost old = cacheHostByHostnameAndPort.put(hostnameAndPort, cacheHost);
            if (old != null || originHostByHostnameAndPort.containsKey(hostnameAndPort))
                throw new IllegalArgumentException("WDS hostname and port used twice: " + hostnameAndPort);
        }
        
        private void addAlias(String alias, SWHost target) {
            String hostnameAndPort = target.getHostnameAndPort();
            if (originHostByHostnameAndPort.containsKey(hostnameAndPort)) {
                originHostByAlias.put(alias, target);
            } else if (cacheHostByHostnameAndPort.containsKey(hostnameAndPort)) {
                cacheHostByAlias.put(alias, target);
            } else {
                throw new IllegalArgumentException("Cannot find alias target: " + hostnameAndPort);
            }
        }
        
        public void clear() {
            originHostByHostnameAndPort.clear();
            cacheHostByHostnameAndPort.clear();
            originHostByAlias.clear();
            cacheHostByAlias.clear();
        }

        private Optional<SWHost> findHost(String toWds) {
            Matcher matcher = toWdsHostPattern.matcher(toWds.trim());
            if (matcher.find()) {
                String hostname = matcher.group(1);
                String port = matcher.group(2);
                String hostnameAndMaybePort = hostname + (port != null ? ":"+port : "");
                SWHost host = originHostByAlias.get(hostname);
                if (host != null)
                    return Optional.of(host);
                host = cacheHostByAlias.get(hostname);
                if (host != null)
                    return Optional.of(host);
                host = cacheHostByHostnameAndPort.get(hostnameAndMaybePort);
                if (host != null)
                    return Optional.of(host);
                host = originHostByHostnameAndPort.get(hostnameAndMaybePort);
                if (host != null)
                    return Optional.of(host);
                host = originHostByAlias.get(hostnameAndMaybePort);
                if (host != null)
                    return Optional.of(host);
                host = cacheHostByAlias.get(hostnameAndMaybePort);
                if (host != null)
                    return Optional.of(host);
            }
            return Optional.empty();
        }
    }

    private static class SWHost {
        int partition;
        String hostname;
        int port;
        
        private static SWHost parseHostnameString(String hostname) {
            int portDelimiterIndex = hostname.indexOf(':');
            if (portDelimiterIndex == -1) {
                throw new IllegalArgumentException("Special host missing port: " + hostname);
            } else {
                SWHost host = new SWHost();
                String portString = hostname.substring(portDelimiterIndex+1);
                host.port = Integer.parseInt(portString);
                host.hostname = hostname.substring(0, portDelimiterIndex);
                return host;
            }
        }

        private static List<SWHost> parseSpecialHostsString(String specialHostsString) {
            if (specialHostsString == null || specialHostsString.trim().isEmpty())
                return Collections.emptyList();

            String[] specialHosts = specialHostsString.split(",");
            ArrayList<SWHost> result = new ArrayList<SWHost>(specialHosts.length);
            Pattern p = Pattern.compile("<(\\d+)>(.*)");
            for (String specialHost : specialHosts) {
                Matcher m = p.matcher(specialHost.trim());
                if (!m.matches())
                    throw new IllegalArgumentException("Invalid special host: " + specialHost);
                String hostname = m.group(2);
                SWHost host = parseHostnameString(hostname);
                String partitionIdString = m.group(1);
                host.partition = Integer.parseInt(partitionIdString);
                result.add(host);
            }
            return result;
        }
        
        public String getHostnameAndPort() {
            return String.format("%s:%d", hostname, port);
        }
    }

    private static class TimedHttpContext extends BasicHttpContext {
        public static String REQUEST_START_TIME = "REQUEST_START_TIME";

        @Override
        public void setAttribute(String id, Object obj) {
            super.setAttribute(id, obj);
            if (id == HttpCoreContext.HTTP_CONNECTION)
                setAttribute(REQUEST_START_TIME, System.nanoTime());
        }
        
        public long getRequestStartTime() {
            return (Long) getAttribute(REQUEST_START_TIME);
        }
    }
    
    @Override
    public WebDataStoreObject read(String fromWds, Root fromRoot, final String path, Long partitionDividend) throws WebDataStoreException, InterruptedException {
        final WebDataStoreObject readObject = new WebDataStoreObject();

        final CountDownLatch doneSignal = new CountDownLatch(1);

        final Hashtable<SWHost,Exception> failures = new Hashtable<SWHost,Exception>();

        List<SWHost> partitionOriginHosts;
        Lock lock = hostsLock.readLock();
        lock.lock();
        try {
            // Find the origin and partition number from what the client specified, or if the
            // client didn't specify anything, then try to figure out what the client wants
            if (fromWds == null) {
                Integer partition = (partitionDividend != null) ? (int)(partitionDividend % originHostsByPartition.size())+1 : null;
                partitionOriginHosts = (partition != null) ? originHostsByPartition.get(partition) : originHosts;
            } else {
                partitionOriginHosts = hostFinder.findHost(fromWds)
                    .map(host -> host.partition)
                    .map(originHostsByPartition::get)
                    .orElseThrow(() -> {
                        String msg = "Client specified unknown WDS hostname: " + fromWds;
                        log.warn(msg);
                        return new IllegalArgumentException(msg);
                    });
            }
        } finally {
            lock.unlock();
        }

        StringBuffer x = new StringBuffer();
        for (SWHost host : partitionOriginHosts)
            x.append(host.hostname+":"+host.port+"/"+host.partition+",");
        log.debug("Attempting origin hosts " + x);

        final SWHost readHost = partitionOriginHosts.get(new Random().nextInt(partitionOriginHosts.size()));

        HttpHost target = new HttpHost(readHost.hostname, readHost.port, "http");
        BasicHttpRequest request = new BasicHttpRequest("HEAD", "http://"+readHost.hostname+":"+readHost.port+"/GETMETA/"+path);
        if (fromRoot != null)
            request.addHeader("OP-ROOT", fromRoot.getName());
        BasicAsyncRequestProducer producer = new BasicAsyncRequestProducer(target, request);
        final TimedHttpContext httpContext = new TimedHttpContext();
        if (log.isDebugEnabled())
            log.debug("Reading " + (fromRoot!=null?fromRoot.getName()+":":"") + path + " from " + readHost.hostname);
        requester.execute(producer, new BasicAsyncResponseConsumer(), pool, httpContext, new FutureCallback<HttpResponse>() {
            @Override
            public void completed(HttpResponse result) {
                try {
                    if (log.isDebugEnabled()) {
                        long readElapsedMs = (System.nanoTime() - httpContext.getRequestStartTime()) / 1000000;
                        log.debug("Read of " + path + " from " + readHost.hostname + " completed in " + readElapsedMs + "ms");
                    }

                    int status = result.getStatusLine().getStatusCode();
                    if (status != 200) {
                        failed(new WebDataStoreException(status));
                    }

                    Header[] headers = result.getAllHeaders();
                    Map<String,Object> readObjectProperties = new HashMap<String,Object>();
                    for (Header header : headers) {
                        if (header.getName().startsWith("META-") || header.getName().startsWith("RHDR-"))
                            //TODO: detect and convert rfc1123 dates
                            readObjectProperties.put(header.getName(), header.getValue());
                        else if ("PROP-SvcCb".equals(header.getName()))
                            readObject.setServiceCallback(header.getValue());
                        else if ("PROP-SvcCbArg".equals(header.getName()))
                            readObject.setCallbackPassthrough(header.getValue());
                        else if ("PROP-ObjType".equals(header.getName()))
                            readObject.setInternalObjectType(header.getValue());
                        else if ("PROP-ObjId".equals(header.getName()))
                            readObject.setInternalObjectId(Long.parseLong(header.getValue()));
                        else if ("PROP-Expire".equals(header.getName()))
                            readObject.setExpirationDate(new Date(Long.parseLong(header.getValue())));
                        else if ("PROP-UpdateDate".equals(header.getName()))
                            readObject.setUpdateDate(new Date(Long.parseLong(header.getValue())));
                        else if ("PROP-CreateDate".equals(header.getName()))
                            readObject.setCreateDate(new Date(Long.parseLong(header.getValue())));
                    }
                    readObject.setProperties(readObjectProperties);

                    doneSignal.countDown();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
            @Override
            public void failed(Exception ex) {
                try {
                    //log.error("Failed to read object " + path, ex);
                    if (log.isDebugEnabled()) {
                        long precacheElapsedMs = (System.nanoTime() - httpContext.getRequestStartTime()) / 1000000;
                        WebDataStoreException wdse = (WebDataStoreException) ex;
                        if (ex instanceof WebDataStoreException && wdse.getHttpStatus() == 404) {
                            log.debug("Read of " + path + " from " + readHost.hostname + " failed in " + precacheElapsedMs + "ms: 404 Not Found");
                            wdse.setType(MISSING);
                        } else
                            log.debug("Read of " + path + " from " + readHost.hostname + " failed in " + precacheElapsedMs + "ms", ex);
                    }
                    failures.put(readHost, ex);
                    doneSignal.countDown();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
            @Override
            public void cancelled() {
                try {
                    if (log.isDebugEnabled()) {
                        long precacheElapsedMs = (System.nanoTime() - httpContext.getRequestStartTime()) / 1000000;
                        log.debug("Read of " + path + " from " + readHost.hostname + " cancelled in " + precacheElapsedMs + "ms");
                    }
                    doneSignal.countDown();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        });

        if (doneSignal != null) {
            doneSignal.await();
            if (!failures.isEmpty()) {
                StringBuffer msg = new StringBuffer();
                Enumeration<SWHost> failureKeys = failures.keys();
                int httpStatus = 0;
                while (failureKeys.hasMoreElements()) {
                    SWHost host = failureKeys.nextElement();
                    Exception ex = failures.get(host);
                    msg.append("Failed to upload ")
                    .append(path)
                    .append(" to ")
                    .append(host.hostname)
                    .append(": (")
                    .append(ex.getClass().getName())
                    .append(") ")
                    .append(ex.getMessage())
                    .append(";; ");
                    if (ex instanceof WebDataStoreException && ((WebDataStoreException)ex).getHttpStatus() != 0)
                        httpStatus = ((WebDataStoreException)ex).getHttpStatus();
                }
                WebDataStoreException wdse = new WebDataStoreException(msg.toString(), failures.values().iterator().next(), httpStatus);
                throw wdse;
            }
        }

        return readObject;
    }

    @Override
    public void operate(final Endpoint endpoint, boolean async, WebDataStoreObjectOperation[] operations) throws WebDataStoreException, InterruptedException {
        final CountDownLatch doneSignal = new CountDownLatch(1);

        final Hashtable<SWHost,Exception> failures = new Hashtable<SWHost,Exception>();

        Integer partition = null;
        List<SWHost> operateOriginHosts;
        Lock lock = hostsLock.readLock();
        lock.lock();
        try {
            // Find the origin and partition number from what the client specified, or if the
            // client didn't specify anything, then try to figure out what the client wants
            if (endpoint.getToWds() == null) {
                if (endpoint.getPartitionDividend() != null) {
                    partition = (int)(endpoint.getPartitionDividend() % originHostsByPartition.size())+1;
                    operateOriginHosts = originHostsByPartition.get(partition);
                } else {
                    partition = null;
                    operateOriginHosts = originHosts;
                }
            } else {
                operateOriginHosts = hostFinder.findHost(endpoint.getToWds())
                    .map(host -> host.partition)
                    .map(originHostsByPartition::get)
                    .orElseThrow(() -> {
                        String msg = "Client specified unknown WDS hostname: " + endpoint.getToWds();
                        log.warn(msg);
                        return new IllegalArgumentException(msg);
                    });
            }
        } finally {
            lock.unlock();
        }

        StringBuffer x = new StringBuffer();
        for (SWHost host : operateOriginHosts)
            x.append(host.hostname+"/"+host.partition+",");
        log.debug("Attempting origin hosts " + x);

        for (final SWHost partitionHost : operateOriginHosts) {
            HttpHost target = new HttpHost(partitionHost.hostname, partitionHost.port, "http");
            BasicHttpRequest request = new BasicHttpRequest("GET", "http://"+partitionHost.hostname+":"+partitionHost.port+"/OPERATE/"+endpoint.getPath());

            if (endpoint.getRoot() != null)
                request.addHeader("OP-ROOT", endpoint.getRoot().getName());

            for (int i = 0; i < operations.length; i++) {
                String opType = operations[i].getType().name();
                String[] parms = operations[i].getParms();
                StringBuffer parmsString = new StringBuffer();
                parmsString.append(String.format("%02d", opType.length()));
                parmsString.append(opType);
                parmsString.append(String.format("%02d", parms.length));
                for (String parm : parms) {
                    parmsString.append(String.format("%03d", parm.length()));
                    parmsString.append(parm);
                }
                String headerKey = "OP-"+ (i+1);
                String headerValue = parmsString.toString();
                request.addHeader(headerKey, headerValue);
            }

            BasicAsyncRequestProducer producer = new BasicAsyncRequestProducer(target, request);
            final TimedHttpContext httpContext = new TimedHttpContext();
            final String opVerb = "Operate ";
            if (log.isDebugEnabled())
                log.debug(opVerb + endpoint.getPath() + " on " + partitionHost.hostname + ":" + partitionHost.port);
            requester.execute(producer, new BasicAsyncResponseConsumer(), pool, httpContext, new FutureCallback<HttpResponse>() {
                @Override
                public void completed(HttpResponse result) {
                    try {
                        if (log.isDebugEnabled()) {
                            long operateElapsedMs = (System.nanoTime() - httpContext.getRequestStartTime()) / 1000000;
                            log.debug(opVerb + endpoint.getPath() + " on " + partitionHost.hostname + ":" + partitionHost.port + " completed in " + operateElapsedMs + "ms");
                        }

                        int status = result.getStatusLine().getStatusCode();
                        if (status != 200 && status != 100) {
                            failed(new WebDataStoreException(status));
                        } else {
                            if (doneSignal != null)
                                doneSignal.countDown();
                        }
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                    }
                }
                @Override
                public void failed(Exception ex) {
                    try {
                        //log.error("Failed to upload object " + object.getPath(), ex);
                        WebDataStoreException wdse = !(ex instanceof WebDataStoreException) ? new WebDataStoreException(ex.getMessage(), ex) : (WebDataStoreException)ex;
                        if (log.isDebugEnabled()) {
                            long uploadElapsedMs = -1;
                            try {
                                uploadElapsedMs = (System.nanoTime() - httpContext.getRequestStartTime()) / 1000000;
                            } catch (NullPointerException e) { }
                            if (wdse.getHttpStatus() == 404)
                                log.debug(opVerb + endpoint.getPath() + " on " + partitionHost.hostname + ":" + partitionHost.port + " failed in " + uploadElapsedMs + "ms: 404 Not Found");
                            else if (wdse.getHttpStatus() == 412)
                                log.debug(opVerb + endpoint.getPath() + " on " + partitionHost.hostname + ":" + partitionHost.port + " failed in " + uploadElapsedMs + "ms: 412 Precondition Failed");
                            else
                                log.debug(opVerb + endpoint.getPath() + " on " + partitionHost.hostname + ":" + partitionHost.port + " failed in " + uploadElapsedMs + "ms", ex);
                        }
                        if (wdse.getHttpStatus() == 404)
                            wdse.setType(MISSING);
                        else if (wdse.getHttpStatus() == 412) {
                            //if (uploadType == CREATE_ONLY)
                            //    wdse.setType(DUPLICATE);
                            //else if (uploadType == UPDATE_ONLY)
                            //    wdse.setType(MISSING);
                        }
                        RepeatWebDataStoreObject repeatObject = new RepeatWebDataStoreObject(endpoint, partitionHost.hostname);
                        wdse.addRepeatObject(repeatObject);
                        if (failures != null)
                            failures.put(partitionHost, ex);
                        if (doneSignal != null)
                            doneSignal.countDown();
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                    }
                }
                @Override
                public void cancelled() {
                    try {
                        if (log.isDebugEnabled()) {
                            long uploadElapsedMs = -1;
                            try {
                                uploadElapsedMs = (System.nanoTime() - httpContext.getRequestStartTime()) / 1000000;
                            } catch (NullPointerException e) { }
                            log.debug(opVerb + endpoint.getPath() + " on " + partitionHost.hostname + ":" + partitionHost.port + " cancelled in " + uploadElapsedMs + "ms");
                        }
                        if (doneSignal != null)
                            doneSignal.countDown();
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                    }
                }
            });
        }
        if (doneSignal != null) {
            doneSignal.await();
            if (failures != null && !failures.isEmpty()) {
                StringBuffer msg = new StringBuffer();
                Enumeration<SWHost> failureKeys = failures.keys();
                while (failureKeys.hasMoreElements()) {
                    SWHost host = failureKeys.nextElement();
                    Exception ex = failures.get(host);
                    msg.append("Failed to operate ")
                    .append(endpoint.getPath())
                    .append(" on ")
                    .append(host.hostname).append(":").append(host.port)
                    .append(": (")
                    .append(ex.getClass().getName())
                    .append(") ")
                    .append(ex.getMessage())
                    .append(";; ");
                }
                WebDataStoreException newEx = new WebDataStoreException(msg.toString(), failures.values().iterator().next());
                for (Exception failEx : failures.values())
                    if (failEx instanceof WebDataStoreException && ((WebDataStoreException)failEx).getRepeatObjects() != null)
                        for (RepeatWebDataStoreObject repeatObject : ((WebDataStoreException)failEx).getRepeatObjects().getRepeatObjects())
                            newEx.addRepeatObject(repeatObject);
                throw newEx;
            }
        }
    }

    @Override
    public void upload(final WebDataStoreObject object, final boolean async, final int uploadFlags, final UploadType uploadType) throws InterruptedException, WebDataStoreException {
        if (object.getProperties() != null)
            for (Entry<String,Object> propEntry : object.getProperties().entrySet())
                if (propEntry.getValue() instanceof Date)
                    propEntry.setValue(HttpDateUtil.formatRfc1123Date(((Date)propEntry.getValue()).getTime()));

        final CountDownLatch doneSignal = (async) ? null : new CountDownLatch(1);

        final Hashtable<SWHost,Exception> failures = (async) ? null : new Hashtable<SWHost,Exception>();

        final Integer partition;
        List<SWHost> partitionOriginHosts;
        final Set<SWHost> precacheHosts = new HashSet<>();
        Lock lock = hostsLock.readLock();
        lock.lock();
        try {
            // Find the origin and partition number from what the client specified, or if the
            // client didn't specify anything, then try to figure out what the client wants
            if (object.getToWds() == null) {
                if (object.getPartitionDividend() != null) {
                    partition = (int)(object.getPartitionDividend() % originHostsByPartition.size())+1;
                    partitionOriginHosts = originHostsByPartition.get(partition);
                    precacheHosts.addAll(cacheHostsByPartition.get(partition));
                } else {
                    partition = null;
                    partitionOriginHosts = originHosts;
                    precacheHosts.addAll(cacheHosts);
                }
            } else {
                partition = hostFinder.findHost(object.getToWds())
                    .map(host -> host.partition)
                    .orElseThrow(() -> {
                        String msg = "Client specified unknown WDS hostname: " + object.getToWds();
                        log.warn(msg);
                        return new IllegalArgumentException(msg);
                    });
                partitionOriginHosts = originHostsByPartition.get(partition);
                
                if (cacheHostsByPartition.containsKey(partition))
                    precacheHosts.addAll(cacheHostsByPartition.get(partition));
            }
        } finally {
            lock.unlock();
        }

        StringBuffer x = new StringBuffer();
        for (SWHost host : partitionOriginHosts)
            x.append(host.hostname+"/"+host.partition+",");
        log.debug("Attempting origin hosts " + x);

        for (final SWHost partitionHost : partitionOriginHosts) {
            // A repeat object is the result of a previous error from a specific
            // hostname so make sure we only retry to upload to that specific hostname
            if (object instanceof RepeatWebDataStoreObject &&
                    ((RepeatWebDataStoreObject)object).getHostname() != null &&
                   !((RepeatWebDataStoreObject)object).getHostname().equals(partitionHost.hostname))
                continue;

            MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create()
                                                                                  .setCharset(Charset.forName("UTF-8"))
                                                                                  .setMode(HttpMultipartMode.RFC6532);

            byte[] data = (object.getData() != null) ? object.getData() : new byte[] { };
            //file system path/filename is taken from HTTP request path and not from form fields
            FormBodyPart formBodyPart = new FormBodyPart("X", new ByteArrayBody(data, ContentType.DEFAULT_BINARY, null));

            if (object.getProperties() != null) {
                for (Entry<String,Object> prop : object.getProperties().entrySet()) {
                    if (prop.getValue() != null)
                        formBodyPart.addField(prop.getKey(), prop.getValue().toString());
                }
            }

            if (object.getPatchInsertBeforePattern() != null) {
                if (uploadType == CREATE_ONLY)
                    throw new IllegalArgumentException("object may not be created and patched at the same time: " + object);
                formBodyPart.addField("OP-PATCH", String.format("%05d%s", object.getPatchTailSearchSize(), object.getPatchInsertBeforePattern()));
            }

            if (uploadType == CREATE_ONLY)
                formBodyPart.addField("OP-CREATE-ONLY", "true");
            else if (uploadType == UPDATE_ONLY)
                formBodyPart.addField("OP-UPDATE-ONLY", "true");

            if (object.getData() == null) {
                if (uploadType == CREATE_ONLY)
                    throw new IllegalArgumentException("object may not be created with no data: " + object);
                formBodyPart.addField("OP-META-ONLY", "true");
            }

            if (object.getServiceCallbackString() != null)
                formBodyPart.addField("PROP-SvcCb",         object.getServiceCallbackString());
            if (object.getCallbackPassthrough() != null)
                formBodyPart.addField("PROP-SvcCbArg",      object.getCallbackPassthrough());
            if (object.getInternalObjectType() != null)
                formBodyPart.addField("PROP-ObjType",       object.getInternalObjectType());
            if (object.getInternalObjectId() != null)
                formBodyPart.addField("PROP-ObjId",      ""+object.getInternalObjectId());
            if (object.getExpirationDate() != null)
                formBodyPart.addField("PROP-Expire",     ""+object.getExpirationDate().getTime());
            if (object.getUpdateDate() != null)
                formBodyPart.addField("PROP-UpdateDate", ""+object.getUpdateDate().getTime());
            if (object.getCreateDate() != null)
                formBodyPart.addField("PROP-CreateDate", ""+object.getCreateDate().getTime());
            if (partition == null)
                formBodyPart.addField("PROP-Global", "true");

            try {
                // why is this protected?
                addPartMethod.invoke(multipartEntityBuilder, formBodyPart);
            } catch (IllegalArgumentException e) {
                throw new IllegalStateException("Unexpected version of class " + MultipartEntityBuilder.class, e);
            } catch (InvocationTargetException e) {
                throw new IllegalStateException("Unexpected error from class " + MultipartEntityBuilder.class, e);
            } catch (IllegalAccessException e) {
                throw new IllegalStateException("Unexpected restriction of class " + MultipartEntityBuilder.class, e);
            }

            HttpEntity multipartEntity = multipartEntityBuilder.build();

            // MultipartFormEntity does not support asynchronous transmission, so copy payload to a generic NByteArrayEntity that does
            //TODO create asynchronous solution for multipart uploads such that the payload is not copied like this
            ByteArrayOutputStream baos = new ByteArrayOutputStream(data.length + 1024);
            try {
                multipartEntity.writeTo(baos);
            } catch (IOException e) {
                String msg = "Error creating multipart upload entity";
                log.error(msg, e);
                throw new WebDataStoreException(msg, e);
            }
            Header contentType = multipartEntity.getContentType();
            multipartEntity = new NByteArrayEntity(baos.toByteArray());
            ((NByteArrayEntity)multipartEntity).setContentType(contentType);

            HttpHost target = new HttpHost(partitionHost.hostname, partitionHost.port, "http");
            BasicHttpEntityEnclosingRequest request = new BasicHttpEntityEnclosingRequest("POST", "http://"+partitionHost.hostname+":"+partitionHost.port+"/UPLOAD/"+object.getPath());
            request.setEntity(multipartEntity);
            BasicAsyncRequestProducer producer = new BasicAsyncRequestProducer(target, request);
            final TimedHttpContext httpContext = new TimedHttpContext();
            final String opVerb = ((object instanceof RepeatWebDataStoreObject) ? "Repeat " : "") + ((object.getPatchInsertBeforePattern() != null) ? "Patching " : "Updating ");
            if (log.isDebugEnabled())
                log.debug(opVerb + object.getPath() + " to " + partitionHost.hostname);
            requester.execute(producer, new BasicAsyncResponseConsumer(), pool, httpContext, new FutureCallback<HttpResponse>() {
                @Override
                public void completed(HttpResponse result) {
                    try {
                        if (log.isDebugEnabled()) {
                            long uploadElapsedMs = (System.nanoTime() - httpContext.getRequestStartTime()) / 1000000;
                            log.debug(opVerb + object.getPath() + " to " + partitionHost.hostname + " completed in " + uploadElapsedMs + "ms");
                        }

                        int status = result.getStatusLine().getStatusCode();
                        if (status != 200 && status != 100) {
                            failed(new WebDataStoreException(status));

                        } else if ((uploadFlags & IWebDataStoreService.OPFLAG_PRECACHE) != 0 && !precacheHosts.isEmpty()) {
                            wdsExecutor.execute(new Runnable() {
                                public void run() {
                                    try {
                                        String md5Sum = (object.getData() == null) ? null : HexUtils.bytesToMd5HexString(object.getData(), true);
                                        precache(object, md5Sum, async, precacheHosts, doneSignal, failures, 0);
                                    } catch (Exception e) {
                                        log.error("Error precaching " + object.getPath() + ": " + e.getMessage(), e);
                                    }
                                }
                            });

                        } else if (async) {
                            wdsExecutor.execute(new CallbackRunnable(object));

                        } else if (doneSignal != null) {
                            doneSignal.countDown();
                        }
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                    }
                }
                @Override
                public void failed(Exception ex) {
                    try {
                        //log.error("Failed to upload object " + object.getPath(), ex);
                        WebDataStoreException wdse = !(ex instanceof WebDataStoreException) ? new WebDataStoreException(ex.getMessage(), ex) : (WebDataStoreException)ex;
                        if (log.isDebugEnabled()) {
                            long uploadElapsedMs = -1;
                            try {
                                uploadElapsedMs = (System.nanoTime() - httpContext.getRequestStartTime()) / 1000000;
                            } catch (NullPointerException e) { }
                            if (wdse.getHttpStatus() == 404)
                                log.debug(opVerb + object.getPath() + " to " + partitionHost.hostname + " failed in " + uploadElapsedMs + "ms: 404 Not Found");
                            else if (wdse.getHttpStatus() == 412)
                                log.debug(opVerb + object.getPath() + " to " + partitionHost.hostname + " failed in " + uploadElapsedMs + "ms: 412 Precondition Failed");
                            else
                                log.debug(opVerb + object.getPath() + " to " + partitionHost.hostname + " failed in " + uploadElapsedMs + "ms", ex);
                        }
                        if (wdse.getHttpStatus() == 404)
                            wdse.setType(MISSING);
                        else if (wdse.getHttpStatus() == 412) {
                            if (uploadType == CREATE_ONLY)
                                wdse.setType(DUPLICATE);
                            else if (uploadType == UPDATE_ONLY)
                                wdse.setType(MISSING);
                        }
                        RepeatWebDataStoreObject repeatObject = new RepeatWebDataStoreObject(object, partitionHost.hostname);
                        wdse.addRepeatObject(repeatObject);
                        if (async)
                            wdsExecutor.execute(new CallbackRunnable(repeatObject, WebDataStoreCallbackType.FAILURE, wdse));
                        if (failures != null)
                            failures.put(partitionHost, ex);
                        if (doneSignal != null)
                            doneSignal.countDown();
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                    }
                }
                @Override
                public void cancelled() {
                    try {
                        if (log.isDebugEnabled()) {
                            long uploadElapsedMs = -1;
                            try {
                                uploadElapsedMs = (System.nanoTime() - httpContext.getRequestStartTime()) / 1000000;
                            } catch (NullPointerException e) { }
                            log.debug(opVerb + object.getPath() + " to " + partitionHost.hostname + " cancelled in " + uploadElapsedMs + "ms");
                        }
                        if (doneSignal != null)
                            doneSignal.countDown();
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                    }
                }
            });
        }
        if (doneSignal != null) {
            doneSignal.await();
            if (failures != null && !failures.isEmpty()) {
                StringBuffer msg = new StringBuffer();
                Enumeration<SWHost> failureKeys = failures.keys();
                while (failureKeys.hasMoreElements()) {
                    SWHost host = failureKeys.nextElement();
                    Exception ex = failures.get(host);
                    msg.append("Failed to upload ")
                    .append(object.getPath())
                    .append(" to ")
                    .append(host.hostname)
                    .append(": (")
                    .append(ex.getClass().getName())
                    .append(") ")
                    .append(ex.getMessage())
                    .append(";; ");
                }
                WebDataStoreException newEx = new WebDataStoreException(msg.toString(), failures.values().iterator().next());
                for (Exception failEx : failures.values())
                    if (failEx instanceof WebDataStoreException && ((WebDataStoreException)failEx).getRepeatObjects() != null)
                        for (RepeatWebDataStoreObject repeatObject : ((WebDataStoreException)failEx).getRepeatObjects().getRepeatObjects())
                            newEx.addRepeatObject(repeatObject);
                throw newEx;
            }
        }
    }
    
    private void precache(final WebDataStoreObject object, final String md5Sum, final boolean async, Set<SWHost> precacheHosts,
                          final CountDownLatch doneSignal, final Hashtable<SWHost,Exception> failures, final int retryCount) {
        for (final SWHost precacheHost : precacheHosts) {
            HttpHost target = new HttpHost(precacheHost.hostname, precacheHost.port, "http");
            BasicHttpRequest request = new BasicHttpRequest("HEAD", "http://"+precacheHost.hostname+":"+precacheHost.port+"/PRECACHE/"+object.getPath());
            request.addHeader("Accept-Encoding", "gzip");
            BasicAsyncRequestProducer producer = new BasicAsyncRequestProducer(target, request);
            final TimedHttpContext httpContext = new TimedHttpContext();
            if (log.isDebugEnabled())
                log.debug("Precaching " + object.getPath() + " in " + precacheHost.hostname);
            requester.execute(producer, new BasicAsyncResponseConsumer(), pool, httpContext, new FutureCallback<HttpResponse>() {
                @Override
                public void completed(HttpResponse result) {
                    try {
                        if (log.isDebugEnabled()) {
                            long precacheElapsedMs = (System.nanoTime() - httpContext.getRequestStartTime()) / 1000000;
                            log.debug("Precache of " + object.getPath() + " at " + precacheHost.hostname + " completed in " + precacheElapsedMs + "ms");
                        }
                        int status = result.getStatusLine().getStatusCode();
                        if (status != 200) {
                            failed(new WebDataStoreException(status));
                        } else if (md5Sum != null) {
                            /* Due to a chicken and egg problem of nobody following the HTTP standard with
                             * respect to properly using Transfer-Encoding to signal on-the-fly compression,
                             * NGINX simply strips the standard ETag header when compressing on-the-fly. Since
                             * we control the WDS server we return the ETag as WDS-MD5 and rely on a
                             * Last-Modified header to guide proxies in between the server and client */
                            Header etagHeader = result.getLastHeader("WDS-MD5");
                            String cachedMd5Sum = (etagHeader != null) ? etagHeader.getValue() : null;
                            if (etagHeader == null || cachedMd5Sum == null || cachedMd5Sum.isEmpty()) {
                                failed(new Exception("Cached object did not return etag for " + object.getPath()));
                                return;
                            } else if (!cachedMd5Sum.equals(md5Sum)) {
                                // NGINX shouldn't return stale objects after precache anymore, but just in case retry a few times
                                if (retryCount < 2) {
                                    log.warn("Retrying to cache object " + object.getPath() + " (attempt #" + (retryCount+2) + ")");
                                    wdsExecutor.execute(new Runnable() {
                                        public void run() {
                                            try {
                                                precache(object, md5Sum, async, precacheHosts, doneSignal, failures, retryCount+1);
                                            } catch (Exception e) {
                                                log.error("Error precaching " + object.getPath() + ": " + e.getMessage(), e);
                                            }
                                        }
                                    });
                                } else
                                    failed(new Exception("Cached object does not match uploaded object for " + object.getPath() + " expected " + md5Sum + " got " + cachedMd5Sum));
                                return;
                            }
                        }
                        if (async)
                            wdsExecutor.execute(new CallbackRunnable(object));
                        else if (doneSignal != null)
                            doneSignal.countDown();
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                    }
                }
                @Override
                public void failed(Exception ex) {
                    try {
                        //log.error("Failed to precache object " + object.getPath(), ex);
                        if (log.isDebugEnabled()) {
                            long precacheElapsedMs = (System.nanoTime() - httpContext.getRequestStartTime()) / 1000000;
                            log.debug("Precache of " + object.getPath() + " at " + precacheHost.hostname + " failed in " + precacheElapsedMs + "ms", ex);
                        }
                        if (async) {
                            if (!(ex instanceof WebDataStoreException))
                                ex = new WebDataStoreException(ex.getMessage(), ex);
                            wdsExecutor.execute(new CallbackRunnable(object, WebDataStoreCallbackType.FAILURE, (WebDataStoreException)ex));
                        }
                        if (failures != null)
                            failures.put(precacheHost, ex);
                        if (doneSignal != null)
                            doneSignal.countDown();
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                    }
                }
                @Override
                public void cancelled() {
                    try {
                        if (log.isDebugEnabled()) {
                            long precacheElapsedMs = (System.nanoTime() - httpContext.getRequestStartTime()) / 1000000;
                            log.debug("Precache of " + object.getPath() + " at " + precacheHost.hostname + " cancelled in " + precacheElapsedMs + "ms");
                        }
                        if (doneSignal != null)
                            doneSignal.countDown();
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                    }
                }
            });
        }
    
    }

//    public static void main(String[] args) throws KeyException, IOException, InterruptedException, WebDataStoreException {
//        //System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.SimpleLog");
//        //System.setProperty("org.apache.commons.logging.simplelog.defaultlog", "debug");
//        //System.setProperty("org.apache.commons.logging.simplelog.defaultlog", "debug");
//        //System.setProperty("org.apache.commons.logging.simplelog.log.org.apache", "debug");
//
//        //System.setProperty("log4j.defaultInitOverride",        "true");
//        //Properties log4jProperties = new Properties();
//        //log4jProperties.setProperty("log4j.rootLogger",                               "DEBUG, stdout");
//        //log4jProperties.setProperty("log4j.appender.stdout",                          "org.apache.log4j.ConsoleAppender");
//        //log4jProperties.setProperty("log4j.appender.stdout.layout",                   "org.apache.log4j.PatternLayout");
//        //log4jProperties.setProperty("log4j.appender.stdout.layout.ConversionPattern", "%d{MM/dd HH:mm:ss} %-5p [%c] %m%n");
//        //log4jProperties.setProperty("log4j.logger.org.apache.http.wire",                "DEBUG");
//        //log4jProperties.setProperty("log4j.logger.org.apache.httpclient",                "DEBUG");
//        //PropertyConfigurator.configure(log4jProperties);
//
//        System.setProperty("meinc.server.properties.file", "/opt/meinc/meinc.properties");
//        //ServerPropertyHolder.startServerPropertyMonitor();
//        
//        WebDataStoreObject o = new WebDataStoreObject();
//        o.setExpirationDate(new Date());
//        o.setPath("asdf/foo.json");
//        o.setData("XThis is data foo.txThis is data foo.txThis is data foo.txThis is data foo.txThis is data foo.txThis is data foo.txThis is data foo.txThis is data foo.txThis is data foo.txThis is data foo.txThis is data foo.txThis is data foo.txThis is data foo.txThis is data foo.txThis is data foo.txThis is data foo.txThis is data foo.txThis is data foo.txThis is data foo.txThis is data foo.txttttttttttttttttttttThis is data foo.txtThis is data foo.txThis is data foo.txThis is data foo.txThis is data foo.txThis is data foo.txThis is data foo.txThis is data foo.txThis is data foo.txThis is data foo.txThis is data foo.txThis is data foo.txThis is data foo.txThis is data foo.txThis is data foo.txThis is data foo.txThis is data foo.txThis is data foo.txThis is data foo.txThis is data foo.txThis is data foo.txThis is data foo.txThis is data foo.txThis is data foo.txThis is data foo.txThis is data foo.txThis is data foo.txThis is data foo.txThis is data foo.txThis is data foo.txThis is data foo.txThis is data foo.txThis is data foo.txThis is data foo.txThis is data foo.txThis is data foo.txThis is data foo.txThis is data foo.txThis is data foo.txThis is data foo.txThis is data foo.txThis is data foo.txThis is data foo.txThis is data foo.txThis is data foo.txThis is data foo.txThis is data foo.txThis is data foo.txThis is data foo.txThis is data foo.txThis is data foo.txThis is data foo.txThis is data foo.txThis is data foo.txThis is data foo.txThis is data foo.txThis is data foo.txThis is data foo.txThis is data foo.txThis is data foo.txThis is data foo.txThis is data foo.txThis is data foo.txThis is data foo.txThis is data foo.txtttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttt".getBytes());
//        o.addMetadata("foo", "bar");
//        o.addMetadata("xing", "xang");
//        o.addResponseHeader("Cow", "Moo");
//        //o.addResponseHeader("Content-Type", "text/plain");
//        
//        ShoutWebStore sws = new ShoutWebStore();
//        sws.upload(o, false, IWebDataStoreService.OPFLAG_PRECACHE);
//    }
    
    public static abstract class PropertyKeySource {
        public abstract String getOriginHostsPropertyKey();
        public abstract String getCacheHostsPropertyKey();
        public abstract String getAliasPrefixPropertyKey();
        public boolean propertiesExist() {
            return ServerPropertyHolder.getProperty(getOriginHostsPropertyKey()) != null &&
                   ServerPropertyHolder.getProperty(getCacheHostsPropertyKey()) != null;
        }
    }
}
