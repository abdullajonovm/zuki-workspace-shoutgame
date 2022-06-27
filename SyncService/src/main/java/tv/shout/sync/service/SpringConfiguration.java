package tv.shout.sync.service;

import javax.sql.DataSource;

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
import com.meinc.identity.service.IIdentityService;
import com.meinc.launcher.serverprops.ServerPropertyHolder;
import com.meinc.trigger.service.ITriggerService;
import com.meinc.webcollector.service.IWebCollectorService;
import com.meinc.webdatastore.service.IWebDataStoreService;

import clientproxy.identityservice.IdentityServiceClientProxy;
import clientproxy.triggerservice.TriggerServiceClientProxy;
import clientproxy.webcollectorservice.WebCollectorServiceFastClientProxy;
import clientproxy.webdatastoreservice.WebDataStoreServiceClientProxy;
import tv.shout.sync.collector.ICollectorMessageHandler;
import tv.shout.sync.collector.SubscriberUtil;
import tv.shout.sync.collector.SyncMessageHandler;
import tv.shout.sync.dao.ISyncServiceDao;

@Configuration
@EnableTransactionManagement
//@EnableScheduling
public class SpringConfiguration
{
    //so that mrsoa.properties value can be injected via @Value(xxx)
    @Bean
    public static PropertySourcesPlaceholderConfigurer propertyPlaceholderConfigurer() 
    {
        PropertySourcesPlaceholderConfigurer bean = new PropertySourcesPlaceholderConfigurer();
        bean.setProperties(ServerPropertyHolder.getProps());
        return bean;
    }
    
    @Bean
    DataSource dataSource()
    {
        BoneCpDataSource boneCpDataSource = BoneCpDataSource.getInstance();
        return boneCpDataSource;
    }
    
    @Bean
    public PlatformTransactionManager transactionManager()
    {
        return new DataSourceTransactionManager(dataSource());
    }
    
    @Bean
    public SqlSessionFactory sqlSessionFactory() 
    throws Exception 
    {
        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
        sqlSessionFactoryBean.setDataSource(dataSource());
        
        /* Add XML Mappers Here */
        Resource[] mapperXmlPaths = new Resource[] {
            //new ClassPathResource("/the/classpath/dao/xxxDaoMapper.xml")
        };
            
        sqlSessionFactoryBean.setMapperLocations(mapperXmlPaths);
        SqlSessionFactory sqlSessionFactory = (SqlSessionFactory) sqlSessionFactoryBean.getObject();
        org.apache.ibatis.session.Configuration sqlSessionFactoryConfig = sqlSessionFactory.getConfiguration();
        sqlSessionFactoryConfig.setMapUnderscoreToCamelCase(true);
        
        /* Add Java Mappers Here */
        sqlSessionFactoryConfig.addMapper(ISyncServiceDao.class);
        
        return sqlSessionFactory;
    }
    
    @Bean
    SqlSessionTemplate sqlSessionTemplate() 
    throws Exception 
    {
        return new SqlSessionTemplate(sqlSessionFactory());
    }
    
    //private ISyncServiceDao _inMemorySyncDao;
    @Bean
    public ISyncServiceDao syncServiceDao() 
    throws Exception
    {
        //return dynamoDbProxy().wrapDynamoDbQueryInterface(ISyncServiceDao.class, dynamoDb());
        
        //if (_inMemorySyncDao == null) {
        //    _inMemorySyncDao = new InMemorySyncDao();
        //}
        //return _inMemorySyncDao;

        return sqlSessionTemplate().getMapper(ISyncServiceDao.class);
    }
    
    @Bean
    public ISyncService syncService()
    {
        return new SyncService();
    }

    @Bean
    public IWebCollectorService webCollectorService()
    {
        return new WebCollectorServiceFastClientProxy();
    }
    
    @Bean
    public SubscriberUtil subscriberUtil()
    {
        return new SubscriberUtil();
    }
    
//    @Bean
//    public AppHelper appHelper()
//    {
//        return new AppHelper();
//    }
    
    @Bean
    public IWebDataStoreService webDataStoreService()
    {
        return new WebDataStoreServiceClientProxy();
    }

    @Bean
    public IIdentityService identityService() 
    {
        return new IdentityServiceClientProxy();
    }
    
    @Bean
    public ITriggerService triggerService()
    {
        return new TriggerServiceClientProxy();
    }
    
    @Bean
    public ICollectorMessageHandler syncMessageHandler()
    {
        return new SyncMessageHandler();
    }
    
}
