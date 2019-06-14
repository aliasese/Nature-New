package com.cnebula.nature;

import com.cnebula.nature.configuration.DefaultConfiguration;
import com.cnebula.nature.configuration.HibernateConfiguration;
import com.cnebula.nature.entity.Configuration;
import com.cnebula.nature.util.CheckParameterUtil;
import com.cnebula.nature.util.ExtractZipUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Properties;

public class AppMain {

    private final static Logger log = LoggerFactory.getLogger(AppMain.class);

    public static void main(String[] agrs) throws Throwable {
        log.info("=======================================Begin=====================================");
        log.info("Main thread begin to start for 'Nature data importing tool'");
        String userDir = System.getProperty("user.dir");
        log.info("Begin to read configuration file from file system: " + userDir);
        File file = null;
        try {
            file = new File(DefaultConfiguration.CONFIG);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Fail to read " + DefaultConfiguration.CONFIG +  ", caused: " + e.getLocalizedMessage(), e);
        }
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            log.error("Fail to read " + DefaultConfiguration.CONFIG +  ", caused: " + e.getLocalizedMessage(), e);
        }
        Properties properties = new Properties();
        properties.load(fileInputStream);



        // Check necessary parameters read from external configuration file.
        String attention = CheckParameterUtil.checkParameter(properties);
        if (attention != null) {
            log.error(attention);
            System.exit(1);
            return;
        }

        // Reset all parameters in properties read from external configuration file in case there is null value.
        CheckParameterUtil.resetProperties(properties);

        // =========Store configuration to internal inner memory===========
        Configuration.setProperties(properties);

        try {
            Object o = ExtractZipUtil.unZip();
            log.info(o.toString());
        } catch (Throwable e) {
            e.printStackTrace();
            log.error("Error to parse zip, caused: " + e.getLocalizedMessage(), e);
            //throw e;
        } finally {
            HibernateConfiguration.sessionFactory = null;
        }

        log.info("=======================================Success=======================================");
        log.info("=======================================End=======================================");
    }
}
