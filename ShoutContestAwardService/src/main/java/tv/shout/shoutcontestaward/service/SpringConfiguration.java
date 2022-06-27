package tv.shout.shoutcontestaward.service;

import javax.sql.DataSource;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.meinc.bonecp.BoneCpDataSource;
import com.meinc.identity.service.IIdentityService;
import com.meinc.launcher.serverprops.ServerPropertyHolder;
import com.meinc.notification.service.INotificationService;
import com.meinc.trigger.service.ITriggerService;

import clientproxy.identityservice.IdentityServiceClientProxy;
import clientproxy.notificationservice.NotificationServiceClientProxy;
import clientproxy.triggerservice.TriggerServiceClientProxy;
import tv.shout.shoutcontestaward.dao.IShoutContestAwardServiceDao;
import tv.shout.shoutcontestaward.dao.IXmlEventProcessorDaoMapper;
import tv.shout.shoutcontestaward.eventprocessor.EventProcessorFilter;
import tv.shout.shoutcontestaward.eventprocessor.EventProcessorHelper;

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
            new ClassPathResource("/tv/shout/shoutcontestaward/dao/IXmlEventProcessorDaoMapper.xml")
        };
            
        sqlSessionFactoryBean.setMapperLocations(mapperXmlPaths);
        SqlSessionFactory sqlSessionFactory = (SqlSessionFactory) sqlSessionFactoryBean.getObject();
        org.apache.ibatis.session.Configuration sqlSessionFactoryConfig = sqlSessionFactory.getConfiguration();
        sqlSessionFactoryConfig.setMapUnderscoreToCamelCase(true);
        
        /* Add Java Mappers Here */
        sqlSessionFactoryConfig.addMapper(IShoutContestAwardServiceDao.class);
        
        return sqlSessionFactory;
    }
    
    @Bean
    SqlSessionTemplate sqlSessionTemplate() 
    throws Exception 
    {
        return new SqlSessionTemplate(sqlSessionFactory());
    }
    
    @Bean
    public IShoutContestAwardServiceDao shoutContestAwardServiceDao() 
    throws Exception 
    {
        return sqlSessionTemplate().getMapper(IShoutContestAwardServiceDao.class);
    }
    
    @Bean
    public IShoutContestAwardService shoutContestAwardService()
    {
        return new ShoutContestAwardService();
    }
    
    @Bean
    public IXmlEventProcessorDaoMapper xmlEventProcessorDaoMapper() throws Exception {
        return sqlSessionTemplate().getMapper(IXmlEventProcessorDaoMapper.class);
    }    
    
    @Bean
    public EventProcessorFilter eventProcessorFilter(){
        return new EventProcessorFilter(); 
    }    
    
    @Bean
    public EventProcessorHelper eventProcessorHelper()
    {
        return new EventProcessorHelper();
    }
    
//    @Bean
//    public IWebCollectorService webCollectorService()
//    {
//        return new WebCollectorServiceFastClientProxy();
//    }
    
//    @Bean
//    public IWebDataStoreService webDataStoreService()
//    {
//        return new WebDataStoreServiceClientProxy();
//    }

//    @Bean
//    public IPushService pushService()
//    {
//        return new PushServiceClientProxy();
//    }

    @Bean
    public INotificationService notificationService()
    {
        return new NotificationServiceClientProxy();
    }
    
    @Bean
    public IIdentityService identityService() 
    {
        return new IdentityServiceClientProxy();
    }
    
//    @Bean
//    public IStoreService storeService() 
//    {
//        return new StoreServiceClientProxy();
//    }
    
    @Bean
    public ITriggerService triggerService()
    {
        return new TriggerServiceClientProxy();
    }

}
