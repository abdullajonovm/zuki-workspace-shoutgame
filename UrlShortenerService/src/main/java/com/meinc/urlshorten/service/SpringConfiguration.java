package com.meinc.urlshorten.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

import com.meinc.launcher.serverprops.ServerPropertyHolder;
import com.meinc.urlshorten.dao.UrlShortenerDao;
import com.meinc.webcollector.service.IWebCollectorService;

import clientproxy.webcollectorservice.WebCollectorServiceFastClientProxy;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.config.CacheConfiguration;

@Configuration
public class SpringConfiguration
{
    @Value("${shorten.url.dynamodb.accesskey}")
    private String _dynamoDbAccessKey;
    
    @Value("${shorten.url.dynamodb.secretkey}")
    private String _dynamoDbSecretKey;
    
    @Value("${shorten.url.dynamodb.table_name}")
    private String _dynamoDbTableName;
    
    private int maxCacheEntries;
    
    public SpringConfiguration() {
        String maxCacheEntriesString = ServerPropertyHolder.getProperty("shorten.url.cache.max.entries", "10000");
        maxCacheEntries = Integer.parseInt(maxCacheEntriesString);
    }

    //so that mrsoa.properties value can be injected via @Value(xxx)
    @Bean
    public static PropertySourcesPlaceholderConfigurer propertyPlaceholderConfigurer() 
    {
        PropertySourcesPlaceholderConfigurer bean = new PropertySourcesPlaceholderConfigurer();
        bean.setProperties(ServerPropertyHolder.getProps());
        return bean;
    }

    //main service bean
    @Bean
    public IUrlShortenerService urlShortenerService()
    {
        return new UrlShortenerService();
    }
    
    @Bean
    public UrlShortenerDao urlShortenerDao()
    {
        return new UrlShortenerDao(_dynamoDbTableName, _dynamoDbAccessKey, _dynamoDbSecretKey);
    }
    
    @Bean
    public IWebCollectorService webCollectorService()
    {
        return new WebCollectorServiceFastClientProxy();
    }
    
    @Bean
    public CacheManager cacheManager()
    {
        return CacheManager.create();
    }
    
    @Bean
    public Cache shortenedUrlCache()
    {
        Cache shortenedUrlCache = new Cache(new CacheConfiguration()
                .name("shortenedUrlCache")
                .maxEntriesLocalHeap(maxCacheEntries)
                .memoryStoreEvictionPolicy("LFU")
                .eternal(true)
        );
        cacheManager().addCache(shortenedUrlCache);
        return shortenedUrlCache;
    }
}
