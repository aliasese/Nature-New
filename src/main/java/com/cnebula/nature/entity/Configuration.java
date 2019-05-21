package com.cnebula.nature.entity;

import java.util.Properties;

/**
 * @see Configuration params readed from outer config.properties will be stored in this entity object
 */
public class Configuration {

    public static Properties properties = null;

    public Configuration() {
    }

    public Configuration(Properties properties) {
        this.properties = properties;
    }

    public static Properties getProperties() {
        return properties;
    }

    public static void setProperties(Properties p) {
        if (properties == null) {
            synchronized (Configuration.class) {
                if (properties == null) {
                    properties = p;
                }
            }
        }
    }
}
