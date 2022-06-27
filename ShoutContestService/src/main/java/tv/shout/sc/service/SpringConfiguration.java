package tv.shout.sc.service;

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
import com.meinc.commons.encryption.IEncryption;
import com.meinc.commons.postoffice.service.IPostOffice;
import com.meinc.identity.service.IIdentityService;
import com.meinc.launcher.serverprops.ServerPropertyHolder;
import com.meinc.push.service.IPushService;
import com.meinc.store.service.IStoreService;
import com.meinc.trigger.service.ITriggerService;
import com.meinc.webcollector.service.IWebCollectorService;
import com.meinc.webdatastore.service.IWebDataStoreService;

import clientproxy.encryptionservice.EncryptionServiceClientProxy;
import clientproxy.identityservice.IdentityServiceClientProxy;
import clientproxy.mockstoreservice.MockStoreServiceClientProxy;
import clientproxy.postofficeservice.PostOfficeServiceClientProxy;
import clientproxy.pushservice.PushServiceClientProxy;
import clientproxy.storeservice.StoreServiceClientProxy;
import clientproxy.syncservice.SyncServiceClientProxy;
import clientproxy.triggerservice.TriggerServiceClientProxy;
import clientproxy.webcollectorservice.WebCollectorServiceFastClientProxy;
import clientproxy.webdatastoreservice.WebDataStoreServiceClientProxy;
import clientproxy.webmediastoreservice.WebMediaStoreServiceClientProxy;
import tv.shout.collector.ICollectorMessageHandler;
import tv.shout.collector.SubscriberUtil;
import tv.shout.sc.collector.AcraHandler;
import tv.shout.sc.collector.StoreHandler;
import tv.shout.sc.collector.SubscriberManagementHandler;
import tv.shout.sc.dao.IContestDaoMapper;
import tv.shout.sync.service.ISyncService;

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
        SqlSessionFactory sqlSessionFactory = sqlSessionFactoryBean.getObject();
        org.apache.ibatis.session.Configuration sqlSessionFactoryConfig = sqlSessionFactory.getConfiguration();
        sqlSessionFactoryConfig.setMapUnderscoreToCamelCase(true);

        /* Add Java Mappers Here */
        sqlSessionFactoryConfig.addMapper(IContestDaoMapper.class);

        return sqlSessionFactory;
    }

    @Bean
    SqlSessionTemplate sqlSessionTemplate()
    throws Exception
    {
        return new SqlSessionTemplate(sqlSessionFactory());
    }

    @Bean
    public IShoutContestService shoutContestService()
    {
        return new ShoutContestService();
    }

    @Bean
    public IContestDaoMapper contestDaoMapper()
    throws Exception
    {
        //return new InMemoryDailyMillionaireDao();

        return sqlSessionTemplate().getMapper(IContestDaoMapper.class);
    }

    @Bean
    public IWebCollectorService webCollectorService()
    {
        return new WebCollectorServiceFastClientProxy();
    }

//    @Bean
//    public AppHelper appHelper()
//    {
//        AppHelper appHelper = new AppHelper();
//        DistributedMap<String, App> appByNameMap = DistributedMap.getMap("appByName");
//        appHelper.setAppMap(appByNameMap);
//        return appHelper;
//    }

    @Bean
    public IWebDataStoreService webDataStoreService()
    {
        return new WebDataStoreServiceClientProxy();
    }

    @Bean
    public IWebDataStoreService webMediaStoreService()
    {
        return new WebMediaStoreServiceClientProxy();
    }

    @Bean
    public IIdentityService identityService()
    {
        return new IdentityServiceClientProxy();
    }

    @Bean
    public ISyncService syncService()
    {
        return new SyncServiceClientProxy();
    }

    @Bean
    public SubscriberUtil subscriberUtil()
    {
        return new SubscriberUtil();
    }

    @Bean
    public IPushService pushService()
    {
        return new PushServiceClientProxy();
    }

//    @Bean
//    public INotificationService notificationService()
//    {
//        return new NotificationServiceClientProxy();
//    }

    @Bean
    public IEncryption encryptionService()
    {
        return new EncryptionServiceClientProxy();
    }

    @Bean
    public IPostOffice postOfficeService()
    {
        return new PostOfficeServiceClientProxy();
    }

    @Bean
    public IStoreService storeService()
    {
        return new StoreServiceClientProxy();
    }

    @Bean
    public IStoreService mockStoreService()
    {
        return new MockStoreServiceClientProxy();
    }

    @Bean
    public ITriggerService triggerService()
    {
        return new TriggerServiceClientProxy();
    }

//    @Bean
//    public IShoutContestAwardService shoutContestAwardService() {
//        return new ShoutContestAwardServiceClientProxy();
//    }

    @Bean
    public ICollectorMessageHandler subscriberManagementHandler() {
        return new SubscriberManagementHandler();
    }

    @Bean
    public ICollectorMessageHandler storeHandler() {
        return new StoreHandler();
    }

    @Bean
    public ICollectorMessageHandler acraHandler()
    {
        return new AcraHandler();
    }

}
