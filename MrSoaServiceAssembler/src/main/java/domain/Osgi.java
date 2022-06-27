package domain;

import com.meinc.mrsoa.service.assembler.OsgiJarMojo;

/**
 * Simple object used to store this plugin's configuration as specified in a
 * project's pom.xml.
 * 
 * @author Matt
 * @see OsgiJarMojo
 */
public class Osgi {
    public String activator;
    public String require;
    public String export;
}
