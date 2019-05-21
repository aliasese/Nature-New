package com.cnebula.nature.configuration;

import com.cnebula.nature.dto.Affiliation;
import com.cnebula.nature.dto.Article;
import com.cnebula.nature.dto.AuthAff;
import com.cnebula.nature.dto.Author;
import com.microsoft.sqlserver.jdbc.SQLServerException;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class HibernateConfiguration {

    private final static Properties PROPERTIES = com.cnebula.nature.entity.Configuration.getProperties();
    public static SessionFactory sessionFactory;

    static {
        Properties properties = new Properties();
        InputStream systemResourceAsStream = ClassLoader.getSystemResourceAsStream("hibernate.properties");
        try {
            properties.load(systemResourceAsStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        properties.setProperty("url", "jdbc:sqlserver://" + PROPERTIES.getProperty("dbHost") + ";DatabaseName=" + PROPERTIES.getProperty("dbName"));
        properties.setProperty("username", PROPERTIES.getProperty("username"));
        properties.setProperty("password", PROPERTIES.getProperty("password"));
        System.out.println("The real properties configurations will be injected to hibernate are: " + PROPERTIES);
        //1.加载hibernate.cfg.xml配置
        Configuration config=new Configuration().setProperties(properties)
                //.addPackage("com.houshenglory.hibernate.dto")
                .addAnnotatedClass(Author.class)
                .addAnnotatedClass(AuthAff.class)
                .addAnnotatedClass(Affiliation.class)
                .addAnnotatedClass(Article.class);
        //2.获取SessionFactory
        try {
            sessionFactory = config.buildSessionFactory();
        } catch (Throwable e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

}
