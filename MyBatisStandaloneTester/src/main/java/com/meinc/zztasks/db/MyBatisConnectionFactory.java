package com.meinc.zztasks.db;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

public class MyBatisConnectionFactory
{
    private static SqlSessionFactory sqlSessionFactory;

    public static SqlSessionFactory getSqlSessionFactory()
    {
        return sqlSessionFactory;
    }

    private static final String CONFIG_DATA_PROP_FILE = "/Volumes/Encrypted2/zztasks_config.properties";
    private static final String MYBATIS_CONFIG_FILE = "SqlMapConfig.xml";

    static
    {
        try {
            //read in the properties file which contains the actual configuration data to use for zztasks
            Properties props = getProperties(CONFIG_DATA_PROP_FILE);

            //read in the MyBatis configuration xml file
            Path path = Paths.get(MyBatisConnectionFactory.class.getClassLoader().getResource(MYBATIS_CONFIG_FILE).toURI());
            Stream<String> lines = Files.lines(path);
            String xml = lines.collect(Collectors.joining("\n"));
            lines.close();

            //System.out.println("PRE:\n" + xml);

            //replace all of the placeholders with actual values
            xml = xml.replace("${driver}", props.getProperty("jdbc.driverClassName"));
            xml = xml.replace("${url}", props.getProperty("jdbc.url"));
            xml = xml.replace("${username}", props.getProperty("jdbc.username"));
            xml = xml.replace("${password}", props.getProperty("jdbc.password"));

            //System.out.println("POST:\n" + xml);

            //convert to a stream for consumption by the SqlSessionFactoryBuilder
            InputStream is = new ByteArrayInputStream(xml.getBytes());

            if (sqlSessionFactory == null) {
                sqlSessionFactory = new SqlSessionFactoryBuilder().build(is);
                sqlSessionFactory.getConfiguration().addMapper(ISubscriberDao.class);
                sqlSessionFactory.getConfiguration().addMapper(IProviderDao.class);
                sqlSessionFactory.getConfiguration().addMapper(IExchangeDao.class);
                sqlSessionFactory.getConfiguration().addMapper(IGoogleDao.class);
                sqlSessionFactory.getConfiguration().addMapper(IPromoDao.class);
                sqlSessionFactory.getConfiguration().addMapper(ArbitrarySqlDao.class);
                sqlSessionFactory.getConfiguration().addMapper(IErgoDao.class);
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static Properties getProperties(String propFile)
    {
        try {
            Properties prop = new Properties();
            InputStream is = new FileInputStream(new File(propFile));
            try {
                prop.load(is);
            } finally {
                if (is != null) {
                    is.close();
                }
            }

            return prop;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
