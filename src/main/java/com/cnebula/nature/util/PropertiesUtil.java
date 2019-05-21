package com.cnebula.nature.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesUtil {

    public static Properties getProperties() throws IOException {

        Properties properties = new Properties();
        properties.load(ClassLoader.class.getResourceAsStream("/config.properties"));
        /*String driver = properties.getProperty("driver");
        String url = properties.getProperty("url");
        String username = properties.getProperty("username");
        String password = properties.getProperty("password");*/

        return properties;

    }
}
