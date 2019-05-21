package com.cnebula.nature.util;

import com.cnebula.nature.configuration.DefaultConfiguration;
import org.apache.commons.lang3.StringUtils;

import java.util.LinkedList;
import java.util.Properties;
import java.util.Set;

public class CheckParameterUtil {

    private final static LinkedList<String> PARAMETERS = new LinkedList<String>();

    private static String attention = null;

    static {
        //PARAMETERS = new LinkedList<>();
        PARAMETERS.add(DefaultConfiguration.NAME_DBHOST);
        PARAMETERS.add(DefaultConfiguration.NAME_DBNAME);
        PARAMETERS.add(DefaultConfiguration.NAME_USERNAME);
        PARAMETERS.add(DefaultConfiguration.NAME_PASSWORD);
        PARAMETERS.add(DefaultConfiguration.NAME_ZIPFILEDIR);
        PARAMETERS.add(DefaultConfiguration.NAME_LOCALSITEINFOXML);
        PARAMETERS.add(DefaultConfiguration.NAME_PDFBASEDIR);
    }

    public static String checkParameter(Properties properties) {
        Set<Object> keySet = properties.keySet();
        if (keySet.size() == 0 || !PARAMETERS.containsAll(keySet)) {
            attention = DefaultConfiguration.CONFIG + " Can not be null, and the correct format is: " + PARAMETERS.toString();
            return attention;
        }

        for (int i = 0; i < PARAMETERS.size(); i++) {
            if (!keySet.contains(PARAMETERS.get(i)) || properties.get(PARAMETERS.get(i)) == null) {
                attention = PARAMETERS.get(i) + " can not be null";
                return attention;
            }
        }
        return attention;
    }

    public static void resetProperties(Properties properties) {
        String dbHost = !StringUtils.isEmpty(properties.getProperty(DefaultConfiguration.NAME_DBHOST)) ? properties.getProperty(DefaultConfiguration.NAME_DBHOST) : DefaultConfiguration.DEF_DBHOST;
        String dbName = !StringUtils.isEmpty(properties.getProperty(DefaultConfiguration.NAME_DBNAME)) ? properties.getProperty(DefaultConfiguration.NAME_DBNAME) : DefaultConfiguration.DEF_DBNAME;
        String username = !StringUtils.isEmpty(properties.getProperty(DefaultConfiguration.NAME_USERNAME)) ? properties.getProperty(DefaultConfiguration.NAME_USERNAME) : DefaultConfiguration.DEF_USERNAME;
        String password = !StringUtils.isEmpty(properties.getProperty(DefaultConfiguration.NAME_PASSWORD)) ? properties.getProperty(DefaultConfiguration.NAME_PASSWORD) : DefaultConfiguration.DEF_PASSWORD;
        String zipFileDir = !StringUtils.isEmpty(properties.getProperty(DefaultConfiguration.NAME_ZIPFILEDIR)) ? properties.getProperty(DefaultConfiguration.NAME_ZIPFILEDIR) : DefaultConfiguration.DEF_ZIP;
        String localSiteInfoXML = !StringUtils.isEmpty(properties.getProperty(DefaultConfiguration.NAME_LOCALSITEINFOXML)) ? properties.getProperty(DefaultConfiguration.NAME_LOCALSITEINFOXML) : DefaultConfiguration.DEF_LOCALSITEXML;
        String pdfBaseDir = !StringUtils.isEmpty(properties.getProperty(DefaultConfiguration.NAME_PDFBASEDIR)) ? properties.getProperty(DefaultConfiguration.NAME_PDFBASEDIR) : DefaultConfiguration.DEF_PDFBASE;

        properties.setProperty(DefaultConfiguration.NAME_DBHOST, dbHost);
        properties.setProperty(DefaultConfiguration.NAME_DBNAME, dbName);
        properties.setProperty(DefaultConfiguration.NAME_USERNAME, username);
        properties.setProperty(DefaultConfiguration.NAME_PASSWORD, password);
        properties.setProperty(DefaultConfiguration.NAME_ZIPFILEDIR, zipFileDir);
        properties.setProperty(DefaultConfiguration.NAME_LOCALSITEINFOXML, localSiteInfoXML);
        properties.setProperty(DefaultConfiguration.NAME_PDFBASEDIR, pdfBaseDir);
    }
}
