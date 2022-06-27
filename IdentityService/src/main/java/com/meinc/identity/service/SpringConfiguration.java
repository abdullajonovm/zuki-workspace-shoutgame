package com.meinc.identity.service;

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

import clientproxy.encryptionservice.EncryptionServiceClientProxy;
import clientproxy.facebookservice.FacebookServiceClientProxy;
import clientproxy.postofficeservice.PostOfficeServiceClientProxy;

import com.meinc.bonecp.BoneCpDataSource;
import com.meinc.commons.encryption.IEncryption;
import com.meinc.commons.postoffice.service.IPostOffice;
import com.meinc.facebook.service.IFacebookService;
import com.meinc.identity.dao.ISubscriberDaoMapper;
import com.meinc.identity.dao.IXmlSubscriberDaoMapper;
import com.meinc.identity.helper.PasswordGenerator;
import com.meinc.identity.helper.UsernameGenerator;
import com.meinc.launcher.serverprops.ServerPropertyHolder;

@Configuration
@EnableTransactionManagement
public class SpringConfiguration
{
    //so that mrsoa.properties value can be injected via @Value(xxx)
    @Bean
    public static PropertySourcesPlaceholderConfigurer propertyPlaceholderConfigurer() {
        PropertySourcesPlaceholderConfigurer bean = new PropertySourcesPlaceholderConfigurer();
        bean.setProperties(ServerPropertyHolder.getProps());
        return bean;
    }

    @Bean
    public PlatformTransactionManager transactionManager() {
        return new DataSourceTransactionManager(dataSource());
    }
    
    @Bean
    SqlSessionTemplate sqlSessionTemplate() throws Exception {
        return new SqlSessionTemplate(sqlSessionFactory());
    }
    
    @Bean
    DataSource dataSource() {
        BoneCpDataSource boneCpDataSource = BoneCpDataSource.getInstance();
        return boneCpDataSource;
    }
    
    @Bean
    public SqlSessionFactory sqlSessionFactory() throws Exception {
        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
        sqlSessionFactoryBean.setDataSource(dataSource());
        
        /* Add XML Mappers Here */
        Resource[] mapperXmlPaths = new Resource[] {
            new ClassPathResource("/com/meinc/identity/dao/IXmlSubscriberDaoMapper.xml")
        };
            
        sqlSessionFactoryBean.setMapperLocations(mapperXmlPaths);
        SqlSessionFactory sqlSessionFactory = (SqlSessionFactory) sqlSessionFactoryBean.getObject();
        org.apache.ibatis.session.Configuration sqlSessionFactoryConfig = sqlSessionFactory.getConfiguration();
        sqlSessionFactoryConfig.setMapUnderscoreToCamelCase(true);
        
        /* Add Java Mappers Here */
        sqlSessionFactoryConfig.addMapper(ISubscriberDaoMapper.class);
        
        return sqlSessionFactory;
    }
    
    @Bean
    public ISubscriberDaoMapper subscriberDaoMapper() throws Exception {
        return sqlSessionTemplate().getMapper(ISubscriberDaoMapper.class);
    }
    
    @Bean
    public IXmlSubscriberDaoMapper xmlSubscriberDaoMapper() throws Exception {
        return sqlSessionTemplate().getMapper(IXmlSubscriberDaoMapper.class);
    }
    
    @Bean
    public IEncryption encryptionService() {
        return new EncryptionServiceClientProxy();
    }
    
    @Bean
    public IFacebookService facebookService() {
        return new FacebookServiceClientProxy();
    }
    
    @Bean
    public IPostOffice postofficeService() {
        return new PostOfficeServiceClientProxy();
    }
    
    @Bean
    public PasswordGenerator passwordGenerator() {
        return new PasswordGenerator();
    }
    
    @Bean
    public UsernameGenerator usernameGenerator() {
        return new UsernameGenerator();
    }
    
    @Bean
    public IIdentityService identityService() {
        return new IdentityService();
    }
}
