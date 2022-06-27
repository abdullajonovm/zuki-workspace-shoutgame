package tv.shout.ecommerce;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Util
{
    public static Properties getProperties(String propFile)
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
