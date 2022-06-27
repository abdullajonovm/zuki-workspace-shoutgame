package com.meinc.webcollector.service;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.meinc.launcher.serverprops.ServerPropertyHolder;
import com.meinc.webcollector.message.MessageProcessorDaemon;
import com.meinc.webcollector.message.WebCollectorMessageBuffer;
import com.meinc.webcollector.message.handler.MessageTypeHandlerRegistry;

@Configuration
public class SpringConfiguration {
    
    @Bean
    public static PropertySourcesPlaceholderConfigurer propertyPlaceholderConfigurer() {
        PropertySourcesPlaceholderConfigurer bean = new PropertySourcesPlaceholderConfigurer();
        bean.setProperties(ServerPropertyHolder.getProps());
        return bean;
    }
    
    @Bean
    public IWebCollectorService webCollectorService() {
        return new WebCollectorService();
    }
    
    @Bean
    public WebCollectorJettyHandler webCollectorJettyHandler() {
        return new WebCollectorJettyHandler();
    }
    
    @Bean
    public MessageTypeHandlerRegistry messageTypeHandlerRegistry() {
        return new MessageTypeHandlerRegistry();
    }
    
    @Bean
    public JsonFactory jsonFactory() {
        return new JsonFactory();
    }
    
    @Bean
    public WebCollectorMessageBuffer webCollectorMessageBuffer() {
        return new WebCollectorMessageBuffer();
    }
    
    @Bean
    public MessageProcessorDaemon messageProcessorDaemon() {
        return new MessageProcessorDaemon();
    }
    
    @Bean
    public ObjectMapper jsonMapper() {
        return new ObjectMapper();
    }
}
