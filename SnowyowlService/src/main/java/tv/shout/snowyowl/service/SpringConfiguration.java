package tv.shout.snowyowl.service;

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
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.meinc.bonecp.BoneCpDataSource;
import com.meinc.commons.encryption.IEncryption;
import com.meinc.commons.postoffice.service.IPostOffice;
import com.meinc.identity.service.IIdentityService;
import com.meinc.launcher.serverprops.ServerPropertyHolder;
import com.meinc.notification.service.INotificationService;
import com.meinc.push.service.IPushService;
import com.meinc.store.service.IStoreService;
import com.meinc.trigger.service.ITriggerService;
import com.meinc.urlshorten.service.IUrlShortenerService;
import com.meinc.webcollector.service.IWebCollectorService;
import com.meinc.webdatastore.service.IWebDataStoreService;

import clientproxy.encryptionservice.EncryptionServiceFastClientProxy;
import clientproxy.identityservice.IdentityServiceClientProxy;
import clientproxy.mockstoreservice.MockStoreServiceClientProxy;
import clientproxy.notificationservice.NotificationServiceClientProxy;
import clientproxy.postofficeservice.PostOfficeServiceClientProxy;
import clientproxy.pushservice.PushServiceClientProxy;
import clientproxy.shoutcontestservice.ShoutContestServiceClientProxy;
import clientproxy.storeservice.StoreServiceClientProxy;
import clientproxy.syncservice.SyncServiceClientProxy;
import clientproxy.triggerservice.TriggerServiceClientProxy;
import clientproxy.urlshortenerservice.UrlShortenerServiceClientProxy;
import clientproxy.webcollectorservice.WebCollectorServiceFastClientProxy;
import clientproxy.webdatastoreservice.WebDataStoreServiceClientProxy;
import clientproxy.webmediastoreservice.WebMediaStoreServiceClientProxy;
import tv.shout.collector.ICollectorMessageHandler;
import tv.shout.collector.SubscriberUtil;
import tv.shout.sc.service.IShoutContestService;
import tv.shout.simplemessagebus.MessageBus;
import tv.shout.simplemessagebus.MessageProcessor;
import tv.shout.snowyowl.collector.AdminHandler;
import tv.shout.snowyowl.collector.AffiliateHandler;
import tv.shout.snowyowl.collector.CommonBusinessLogic;
import tv.shout.snowyowl.collector.GameHandler;
import tv.shout.snowyowl.collector.PayoutModelAdminHandler;
import tv.shout.snowyowl.collector.SubscriberHandler;
import tv.shout.snowyowl.collector.SuperuserHandler;
import tv.shout.snowyowl.collector.SyncHandler;
import tv.shout.snowyowl.common.GameStatsHandler;
import tv.shout.snowyowl.common.SubscriberStatsHandler;
import tv.shout.snowyowl.dao.IDaoMapper;
import tv.shout.snowyowl.dao.IGameStatsMapper;
import tv.shout.snowyowl.dao.IXmlDaoMapper;
import tv.shout.snowyowl.engine.BotEngine;
import tv.shout.snowyowl.engine.EngineCoordinator;
import tv.shout.snowyowl.engine.MMECommon;
import tv.shout.snowyowl.engine.PayoutManager;
import tv.shout.snowyowl.engine.QuestionSupplier;
import tv.shout.snowyowl.engine.SponsorEngine;
import tv.shout.snowyowl.engine.fixedround.PayoutManagerFixedRoundMultiLife;
import tv.shout.snowyowl.engine.fixedround.PayoutManagerFixedRoundSingleLife;
import tv.shout.snowyowl.forensics.ForensicsDao;
import tv.shout.snowyowl.reactiveengine.fixedround.HandlerAnswer;
import tv.shout.snowyowl.reactiveengine.fixedround.HandlerBeginBracketPlay;
import tv.shout.snowyowl.reactiveengine.fixedround.HandlerBeginMatch;
import tv.shout.snowyowl.reactiveengine.fixedround.HandlerBeginPoolPlay;
import tv.shout.snowyowl.reactiveengine.fixedround.HandlerCloseGame;
import tv.shout.snowyowl.reactiveengine.fixedround.HandlerDocPublisher;
import tv.shout.snowyowl.reactiveengine.fixedround.HandlerScoreBracketFixedLife;
import tv.shout.snowyowl.reactiveengine.fixedround.HandlerScoreBracketMultiLife;
import tv.shout.snowyowl.reactiveengine.fixedround.HandlerScoreQuestionFixedLife;
import tv.shout.snowyowl.reactiveengine.fixedround.HandlerScoreQuestionMultiLife;
import tv.shout.snowyowl.reactiveengine.fixedround.HandlerSendQuestion;
import tv.shout.snowyowl.reactiveengine.fixedround.MatchMaker;
import tv.shout.sync.service.ISyncService;

@Configuration
@EnableTransactionManagement
@EnableScheduling
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
            new ClassPathResource("/tv/shout/snowyowl/dao/IXmlDaoMapper.xml")
        };

        sqlSessionFactoryBean.setMapperLocations(mapperXmlPaths);
        SqlSessionFactory sqlSessionFactory = sqlSessionFactoryBean.getObject();
        org.apache.ibatis.session.Configuration sqlSessionFactoryConfig = sqlSessionFactory.getConfiguration();
        sqlSessionFactoryConfig.setMapUnderscoreToCamelCase(true);

        /* Add Java Mappers Here */
        sqlSessionFactoryConfig.addMapper(IDaoMapper.class);
        sqlSessionFactoryConfig.addMapper(ForensicsDao.class);
        sqlSessionFactoryConfig.addMapper(IGameStatsMapper.class);

        return sqlSessionFactory;
    }

    @Bean
    SqlSessionTemplate sqlSessionTemplate()
    throws Exception
    {
        return new SqlSessionTemplate(sqlSessionFactory());
    }

    @Bean
    IDaoMapper daoMapper()
    throws Exception
    {
        return sqlSessionTemplate().getMapper(IDaoMapper.class);
    }

    @Bean
    IGameStatsMapper gameStatsMapper()
    throws Exception
    {
        return sqlSessionTemplate().getMapper(IGameStatsMapper.class);
    }

    @Bean
    IXmlDaoMapper xmlDaoMapper()
    throws Exception
    {
        return sqlSessionTemplate().getMapper(IXmlDaoMapper.class);
    }

    @Bean
    ForensicsDao forensicsDao()
    throws Exception
    {
        return sqlSessionTemplate().getMapper(ForensicsDao.class);
    }

    @Bean
    ISnowyowlService snowyowlService()
    {
        return new SnowyowlService();
    }

    @Bean
    IShoutContestService shoutContestService()
    {
        return new ShoutContestServiceClientProxy();
    }

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
    public IWebCollectorService webCollectorService()
    {
        return new WebCollectorServiceFastClientProxy();
    }

    @Bean
    public IEncryption encryptionService()
    {
        return new EncryptionServiceFastClientProxy();
    }

    @Bean
    public INotificationService notificationService()
    {
        return new NotificationServiceClientProxy();
    }

    @Bean
    public IPostOffice postOfficeService()
    {
        return new PostOfficeServiceClientProxy();
    }

    @Bean
    public ISyncService syncService()
    {
        return new SyncServiceClientProxy();
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
    public IIdentityService identityService()
    {
        return new IdentityServiceClientProxy();
    }

    @Bean
    public IPushService pushService()
    {
        return new PushServiceClientProxy();
    }

    @Bean
    public ITriggerService triggerService()
    {
        return new TriggerServiceClientProxy();
    }

    @Bean
    public IUrlShortenerService urlShortenerService()
    {
        return new UrlShortenerServiceClientProxy();
    }

    @Bean
    public BotEngine botEngine()
    {
        return new BotEngine();
    }

    @Bean
    public SponsorEngine sponsorEngine()
    {
        return new SponsorEngine();
    }

    @Bean
    public SubscriberUtil subscriberUtil()
    {
        return new SubscriberUtil();
    }

    @Bean
    public CommonBusinessLogic commonBusinessLogic()
    {
        return new CommonBusinessLogic();
    }

    @Bean
    public CurrentRankCalculator currentRankCalculator()
    {
        return new CurrentRankCalculator();
    }

    @Bean
    public EngineCoordinator engineCoordinator()
    {
        return new EngineCoordinator();
    }

    @Bean
    public SubscriberStatsHandler subscriberStatsHandler()
    {
        return new SubscriberStatsHandler();
    }

    @Bean
    public GameStatsHandler gameStatsHandler()
    {
        return new GameStatsHandler();
    }

    @Bean
    public PayoutManager payoutManagerFixedRoundSingleLife()
    {
        return new PayoutManagerFixedRoundSingleLife();
    }

    @Bean
    public PayoutManager payoutManagerFixedRoundMultiLife()
    {
        return new PayoutManagerFixedRoundMultiLife();
    }

    @Bean
    public QuestionSupplier questionSupplier()
    {
        return new QuestionSupplier();
    }

    @Bean
    public LogFileAnalyzer logFileAnalyzer()
    {
        return new LogFileAnalyzer();
    }

    @Bean
    public MMECommon mmeCommon()
    {
        return new MMECommon();
    }

    // handlers >>>

    @Bean
    public ICollectorMessageHandler gameHandler()
    {
        return new GameHandler();
    }

    @Bean
    public ICollectorMessageHandler adminHandler()
    {
        return new AdminHandler();
    }

    @Bean
    public ICollectorMessageHandler superuserHandler()
    {
        return new SuperuserHandler();
    }

    @Bean
    public ICollectorMessageHandler payoutModelAdminHandler()
    {
        return new PayoutModelAdminHandler();
    }

    @Bean
    public ICollectorMessageHandler subscriberHandler()
    {
        return new SubscriberHandler();
    }

    @Bean
    public ICollectorMessageHandler affiliateHandler()
    {
        return new AffiliateHandler();
    }

    @Bean
    public ICollectorMessageHandler syncHandler()
    {
        return new SyncHandler();
    }

    // <<< handlers

    // >>> MessageBus + MessageProcessors

    @Bean
    public MessageBus messageBus()
    {
        return new MessageBus();
    }

    @Bean
    public MessageProcessor handlerAnswer()
    {
        return new HandlerAnswer();
    }

    @Bean
    public MessageProcessor handlerBeginBracketPlay()
    {
        return new HandlerBeginBracketPlay();
    }

    @Bean
    public MessageProcessor handlerBeginMatch()
    {
        return new HandlerBeginMatch();
    }

    @Bean
    public MessageProcessor handlerBeginPoolPlay()
    {
        return new HandlerBeginPoolPlay();
    }

    @Bean
    public MessageProcessor handlerCloseGame()
    {
        return new HandlerCloseGame();
    }

    @Bean
    public MessageProcessor handlerScoreQuestionFixedLife()
    {
        return new HandlerScoreQuestionFixedLife();
    }

    @Bean
    public MessageProcessor handlerScoreQuestionMultiLife()
    {
        return new HandlerScoreQuestionMultiLife();
    }

    @Bean
    public MessageProcessor handlerScoreBracketFixedLife()
    {
        return new HandlerScoreBracketFixedLife();
    }

    @Bean
    public MessageProcessor handlerScoreBracketMultiLife()
    {
        return new HandlerScoreBracketMultiLife();
    }

    @Bean
    public MessageProcessor handlerSendQuestion()
    {
        return new HandlerSendQuestion();
    }

    @Bean
    public MessageProcessor handlerDocPublisher()
    {
        return new HandlerDocPublisher();
    }

    @Bean
    public MessageProcessor matchMaker()
    {
        return new MatchMaker();
    }

    // <<< MessageBus
}
