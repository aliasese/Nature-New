package com.cnebula.nature.util;

import com.alibaba.druid.pool.DruidDataSource;

import java.util.Properties;

public class DruidUtil {

    public static DruidDataSource createDruidConnectionPool(Properties properties){
        DruidDataSource ds = new DruidDataSource();
        ds.setDriverClassName(properties.getProperty("driver"));
        ds.setUrl(properties.getProperty("url"));
        ds.setUsername(properties.getProperty("username"));
        ds.setPassword(properties.getProperty("password"));
        ds.setMaxActive(Integer.valueOf(properties.getProperty("maxActive")));
        ds.setMaxWait(Integer.valueOf(properties.getProperty("maxWait")));
        ds.setValidationQuery(properties.getProperty("validationQuery"));
        ds.setKeepAlive(true);
        return ds;
    }
}
