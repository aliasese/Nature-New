package com.cnebula.nature.configuration;

import java.io.File;

public class DefaultConfiguration {

    public static final String CONFIG = System.getProperty("user.dir") + File.separator + "config.properties";
    public static final String DEF_LOCALSITEXML = System.getProperty("user.dir") + File.separator + "local_site.xlsx";
    public static final String DEF_ZIP = System.getProperty("user.dir") + File.separator + "ftp_PUB_19-05-13_20-11-42.zip";
    //public static final String PDFBASE = System.getProperty("user.dir") + File.separator + "nature";
    public static final String DEF_PDFBASE = "Z:\\qikan-fulltext\\nature\\";
    public static final String DEF_DBHOST = "localhost:1433";
    public static final String DEF_DBNAME = "nature";
    public static final String DEF_USERNAME = "sa";
    public static final String DEF_PASSWORD = "sqlserver";

    public static final String NAME_DBHOST = "dbHost";
    public static final String NAME_DBNAME = "dbName";
    public static final String NAME_USERNAME = "username";
    public static final String NAME_PASSWORD = "password";
    public static final String NAME_ZIPFILEDIR = "zipFileDir";
    public static final String NAME_LOCALSITEINFOXML = "localSiteInfoXML";
    public static final String NAME_PDFBASEDIR = "pdfBaseDir";

}
