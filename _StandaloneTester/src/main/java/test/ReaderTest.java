package test;

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

public class ReaderTest
{
    private static final String CONFIG_DATA_PROP_FILE = "/Volumes/Encrypted2/zztasks_config.properties";
    private static final String MYBATIS_CONFIG_FILE = "SqlMapConfig.xml";

    public void init()
    {
        try {
            //read in the properties file which contains the actual configuration data to use for zztasks
            Properties props = getProperties(CONFIG_DATA_PROP_FILE);

            //read in the MyBatis configuration xml file
            Path path = Paths.get(ReaderTest.class.getClassLoader().getResource(MYBATIS_CONFIG_FILE).toURI());
            Stream<String> lines = Files.lines(path);
            String xml = lines.collect(Collectors.joining("\n"));
            lines.close();

            System.out.println("PRE:\n" + xml);

            //replace all of the placeholders with actual values
            xml = xml.replace("${driver}", props.getProperty("jdbc.driverClassName"));
            xml = xml.replace("${url}", props.getProperty("jdbc.url"));
            xml = xml.replace("${username}", props.getProperty("jdbc.username"));
            xml = xml.replace("${password}", props.getProperty("jdbc.password"));

            System.out.println("POST:\n" + xml);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        //InputStream is = new ByteArrayInputStream(xml.getBytes());
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

    public static void main(String[] args)
    {
        /*ReaderTest test = */new ReaderTest().init();
    }

}
