package com.meinc.urlshorten.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.amazonaws.AmazonServiceException;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.meinc.launcher.serverprops.ServerPropertyHolder;
import com.meinc.mrsoa.service.ServiceEndpoint;
import com.meinc.mrsoa.service.annotation.OnStart;
import com.meinc.mrsoa.service.annotation.OnStop;
import com.meinc.mrsoa.service.annotation.Service;
import com.meinc.mrsoa.service.annotation.ServiceMethod;
import com.meinc.urlshorten.dao.UrlShortenerDao;
import com.meinc.urlshorten.domain.ShortenedUrl;
import com.meinc.urlshorten.exception.NoSuchShortUrlException;
import com.meinc.urlshorten.exception.ShortUrlAlreadyExistsException;
import com.meinc.urlshorten.exception.UrlShortenerException;
import com.meinc.webcollector.message.CollectorMessage;
import com.meinc.webcollector.message.IWebCollectorMessageBuffer;
import com.meinc.webcollector.message.handler.BadRequestException;
import com.meinc.webcollector.message.handler.CollectorMessageResult;
import com.meinc.webcollector.message.handler.IMessageTypeHandler;
import com.meinc.webcollector.message.handler.SyncRequest.HttpRequest;
import com.meinc.webcollector.message.handler.SyncRequest.HttpResponse;
import com.meinc.webcollector.service.IWebCollectorService;
import com.meinc.webcollector.service.IWebCollectorService.CollectorEndpoint;
import com.meinc.webcollector.service.IWebCollectorService.CollectorEndpoint.ConnectionType;
import com.meinc.webcollector.service.IWebCollectorService.CollectorEndpoint.HandlerStyle;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

@Service(
    namespace=       IUrlShortenerService.SERVICE_NAMESPACE,
    name=            IUrlShortenerService.SERVICE_NAME,
    interfaces=      IUrlShortenerService.SERVICE_INTERFACE,
    version=         IUrlShortenerService.SERVICE_VERSION,
    exposeAs=        IUrlShortenerService.class
)
public class UrlShortenerService
implements IUrlShortenerService, IMessageTypeHandler {
    private static final String CollectorHandlerMessageType = "URL_LENGTHEN";

    private static Logger _logger = LoggerFactory.getLogger(UrlShortenerService.class);

    public static final ServiceEndpoint SERVICE_ENDPOINT = new ServiceEndpoint(IUrlShortenerService.SERVICE_NAMESPACE,
                                                                               IUrlShortenerService.SERVICE_NAME,
                                                                               IUrlShortenerService.SERVICE_VERSION);

    private List<Random> randomGenerators;
    private Random randomGeneratrsRand = new Random();

    private Set<String> legalLongUrlDomains = new HashSet<String>();

    private int shortUrlPathLength;
    private byte[] base36 = new byte[] {'0','1','2','3','4','5','6','7','8','9','a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z'};
    private String shortUrlPrefix;

    private ObjectMapper jsonMapper = new ObjectMapper();

    @Autowired
    private UrlShortenerDao _dao;

    @Autowired
    private IWebCollectorService _webCollectorService;

    @Resource(name="shortenedUrlCache")
    private Cache _shortenedUrlCache;

    @Override
    @ServiceMethod
    @OnStart
    public void start()
    {
        shortUrlPrefix = ServerPropertyHolder.getProperty("shorten.url.short.url.prefix");
        if (shortUrlPrefix == null)
            throw new IllegalStateException("missing required property 'shorten.url.short.url.prefix'");
        if (!shortUrlPrefix.endsWith("/"))
            shortUrlPrefix = shortUrlPrefix + "/";

        String shortUrlPathLengthString = ServerPropertyHolder.getProperty("shorten.url.short.path.length", "6");
        shortUrlPathLength = Integer.parseInt(shortUrlPathLengthString);

        //TODO use the proper random number generator service (?)
        String generatorCountString = ServerPropertyHolder.getProperty("shorten.url.random.generators.count", "3");
        int generatorCount = Integer.parseInt(generatorCountString);
        randomGenerators = new ArrayList<Random>(generatorCount);
        for (int i = 0; i < generatorCount; i++) {
            Random rand = new Random();
            randomGenerators.add(rand);
            try {
                Thread.sleep(rand.nextInt(500)+1);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        String shortUrlLegalDomainsString = ServerPropertyHolder.getProperty("shorten.url.legal.long.url.domains");
        if (shortUrlLegalDomainsString == null)
            throw new IllegalStateException("missing required property 'shorten.url.legal.long.url.domains'");
        String[] shortUrlLegalDomainsParts = shortUrlLegalDomainsString.split(",");
        for (String shortUrlLegalDomain : shortUrlLegalDomainsParts)
            legalLongUrlDomains.add(shortUrlLegalDomain.trim());

        _logger.info("Registering web collector endpoint");
        CollectorEndpoint lengthenEndpoint = new CollectorEndpoint("/lengthen/", true, ConnectionType.ANY, HandlerStyle.SYNC_REQUEST);
        _webCollectorService.registerMessageTypeHandler(lengthenEndpoint, CollectorHandlerMessageType, SERVICE_ENDPOINT);

        _logger.info("UrlShortenerService started");
    }

    @Override
    @ServiceMethod
    @OnStop
    public void stop()
    {
        _logger.info("UrlShortenerService stopped");
    }

    @Override
    @ServiceMethod
    public String makeShortUrl(String requestHost, String longUrl) throws ShortUrlAlreadyExistsException, UrlShortenerException {
        StringBuffer result = new StringBuffer(shortUrlPathLength);
        byte[] randBytes = new byte[shortUrlPathLength];

        Random rand = randomGenerators.get(Math.abs(randomGeneratrsRand.nextInt() % randomGenerators.size()));
        rand.nextBytes(randBytes);

        for (int i = 0; i < randBytes.length; i++)
            result.append((char)base36[Math.abs(randBytes[i] % base36.length)]);

        String shortUrlCode = result.toString();

        ShortenedUrl shortenedUrl = new ShortenedUrl();
        shortenedUrl.setShortUrlCode(shortUrlCode);
        shortenedUrl.setShortUrlOrigin(requestHost);
        shortenedUrl.setLongUrl(longUrl);

        _dao.createShortUrl(shortenedUrl);

        cacheShortUrl(shortUrlCode, longUrl);

        return shortUrlPrefix + shortUrlCode;
    }

    @Override
    @ServiceMethod
    public String getLongUrl(String shortUrlCode) throws NoSuchShortUrlException, UrlShortenerException {
        Element element = _shortenedUrlCache.get(shortUrlCode);
        if (element != null) {
            String longUrl = (String) element.getObjectValue();
            return longUrl;
        } else {
            ShortenedUrl shortenedUrl = _dao.getShortenedUrl(shortUrlCode);
            if (shortenedUrl == null)
                throw new NoSuchShortUrlException(shortUrlCode);
            element = new Element(shortUrlCode, shortenedUrl.getLongUrl());
            _shortenedUrlCache.put(element);
            return shortenedUrl.getLongUrl();
        }
    }

    @Override
    @ServiceMethod
    public String flushShortUrlCache(String shortUrlCode) {
        Element element = _shortenedUrlCache.removeAndReturnElement(shortUrlCode);
        return (element == null) ? null : (String)element.getObjectValue();
    }

    private boolean cacheShortUrl(String shortUrlCode, String longUrl) {
        if (shortUrlCode == null || longUrl == null)
            return false;
        Element element = new Element(shortUrlCode, longUrl);
        return _shortenedUrlCache.putIfAbsent(element) == null;
    }

    @Override
    @ServiceMethod
    public CollectorMessageResult createMessage(String requestPath, Map<String,String> requestHeaders, Map<String,String> requestParameters)
    throws BadRequestException {
        throw new UnsupportedOperationException();
    }

    @Override
    @ServiceMethod
    public String getHandlerMessageType() {
        return CollectorHandlerMessageType;
    }

    @Override
    @ServiceMethod
    public CollectorEndpoint[] getCollectorEndpoints() {
        throw new UnsupportedOperationException();
    }

    @Override
    @ServiceMethod
    public void handleMessages(List<CollectorMessage> messages, IWebCollectorMessageBuffer messageBuffer) throws BadRequestException {
        throw new UnsupportedOperationException();
    }

    @Override
    @ServiceMethod
    public HttpResponse handleSyncRequest(HttpRequest request)
    throws BadRequestException {
        HttpResponse response = new HttpResponse();

        if (!request.getPath().startsWith("/lengthen/")) {
            response.setError(400);
            return response;
        }

        String shortCode = request.getPath().substring("/lengthen/".length());

        String longUrl;
        try {
            longUrl = getLongUrl(shortCode);
        } catch (NoSuchShortUrlException e) {
            _logger.warn("No such short code: " + shortCode + " (" + request.getPath() + ")");
            response.setError(404);
            return response;
        } catch (UrlShortenerException e) {
            _logger.error("Error lengthening short code: " + shortCode + " (" + request.getPath() + ")", e);
            response.setError(404);
            return response;
        } catch (AmazonServiceException e) {
            if (shortCode == null || shortCode.trim().length() == 0) {
                _logger.warn("shortCode: " + shortCode + ", path: " + request.getPath() + ", amazon error message: " + e.getErrorMessage());
            } else {
                _logger.error("shortCode: " + shortCode + ", path: " + request.getPath() + ", amazon error message: " + e.getErrorMessage());
            }
            response.setError(e.getStatusCode() >= 400 ? e.getStatusCode() : 404);
            return response;
        }

        String expectValue = request.getHeader("Pragma");
        if (expectValue != null && "content-type=json".equals(expectValue.trim())) {
            Map<String,String> jsonMap = Collections.singletonMap("longUrl", longUrl);
            try {
                jsonMapper.writeValue(response.getOutputStream(), jsonMap);
                response.setContentType("application/json");
            } catch (JsonGenerationException e) {
                _logger.error("Error creating JSON response for long-url: " + longUrl, e);
                response.setError(500);
            } catch (JsonMappingException e) {
                _logger.error("Error creating JSON response for long-url: " + longUrl, e);
                response.setError(500);
            } catch (IOException e) {
                _logger.error("Error creating JSON response for long-url: " + longUrl, e);
                response.setError(500);
            }
        } else {
            response.setRedirect(longUrl);
        }

        return response;
    }
}
