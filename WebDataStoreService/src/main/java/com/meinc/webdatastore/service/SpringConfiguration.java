package com.meinc.webdatastore.service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import javax.sql.DataSource;

import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.meinc.bonecp.BoneCpDataSource;
import com.meinc.launcher.serverprops.ServerPropertyHolder;
import com.meinc.webdatastore.dao.IWebDataStoreDaoMapper;
import com.meinc.webdatastore.dao.WebDataStoreDao;
import com.meinc.webdatastore.store.ShoutWebStore;
import com.meinc.webdatastore.store.ShoutWebStore.PropertyKeySource;
import com.meinc.webdatastore.store.Store;

@Configuration
@EnableTransactionManagement
public class SpringConfiguration {
    @Bean
    public PropertySourcesPlaceholderConfigurer propertyPlaceholderConfigurer() {
        PropertySourcesPlaceholderConfigurer bean = new PropertySourcesPlaceholderConfigurer();
        bean.setProperties(ServerPropertyHolder.getProps());
        return bean;
    }
    
    @Bean
    public IWebDataStoreService webDataStoreService() {
        WebDataStoreService service = new WebDataStoreService();
        service.setStore(dataStore());
        return service;
    }

    @Bean
    public IWebDataStoreService webMediaStoreService() {
        WebDataStoreService service = new WebMediaStoreService();
        service.setStore(mediaStore());
        return service;
    }

    @Bean
    DataSource dataSource() {
        return BoneCpDataSource.getInstance();
    }

    @Bean
    public PlatformTransactionManager transactionManager() {
        return new DataSourceTransactionManager(dataSource());
    }

    @Bean
    public SqlSessionFactory sqlSessionFactory() throws Exception {
        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
        sqlSessionFactoryBean.setDataSource(dataSource());
        
        /* Add XML Mappers Here */
        Resource[] mapperXmlPaths = new Resource[] {
        };
            
        sqlSessionFactoryBean.setMapperLocations(mapperXmlPaths);
        SqlSessionFactory sqlSessionFactory = (SqlSessionFactory) sqlSessionFactoryBean.getObject();
        org.apache.ibatis.session.Configuration sqlSessionFactoryConfig = sqlSessionFactory.getConfiguration();
        sqlSessionFactoryConfig.setDefaultExecutorType(ExecutorType.REUSE);
        sqlSessionFactoryConfig.setMapUnderscoreToCamelCase(true);
        
        /* Add Java Mappers Here */
        sqlSessionFactoryConfig.addMapper(IWebDataStoreDaoMapper.class);
        
        return sqlSessionFactory;
    }
    
    @Bean
    SqlSessionTemplate sqlSessionTemplate() throws Exception {
        return new SqlSessionTemplate(sqlSessionFactory());
    }
    
    @Bean
    public IWebDataStoreDaoMapper webDataStoreDaoMapper() throws Exception {
        return sqlSessionTemplate().getMapper(IWebDataStoreDaoMapper.class);
    }
    
    @Bean
    public WebDataStoreDao webDataStoreDao() {
        return new WebDataStoreDao();
    }
    
    @Bean
    public Store dataStore() {
        PropertyKeySource dataStorePropertyKeySource = new PropertyKeySource() {
            public String getOriginHostsPropertyKey() {
                return "webdatastore.shoutweb.origin.hosts";
            }
            public String getCacheHostsPropertyKey() {
                return "webdatastore.shoutweb.cache.hosts";
            }
            public String getAliasPrefixPropertyKey() {
                return "webdatastore.shoutweb.alias";
            }
        };
        return new ShoutWebStore(dataStorePropertyKeySource);
    }
    
    @Bean
    public Store mediaStore() {
        PropertyKeySource mediaStorePropertyKeySource = new PropertyKeySource() {
            public String getOriginHostsPropertyKey() {
                return "webmediastore.shoutweb.origin.hosts";
            }
            public String getCacheHostsPropertyKey() {
                return "webmediastore.shoutweb.cache.hosts";
            }
            public String getAliasPrefixPropertyKey() {
                return "webmediastore.shoutweb.alias";
            }
        };
        return new ShoutWebStore(mediaStorePropertyKeySource);
    }
    
    @Bean
    public ExecutorService wdsExecutor() {
        return Executors.newFixedThreadPool(10, new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "web-data-store-executor-thread");
            }
        });
    }
}
