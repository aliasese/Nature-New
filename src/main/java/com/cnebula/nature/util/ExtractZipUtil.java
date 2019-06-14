package com.cnebula.nature.util;

import com.cnebula.nature.ThreadTask.ParseXMLCallableImpl;
import com.cnebula.nature.configuration.DefaultConfiguration;
import com.cnebula.nature.configuration.HibernateConfiguration;
import com.cnebula.nature.entity.Configuration;
import com.cnebula.nature.entity.FileNameEntity;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

public class ExtractZipUtil {

    private final static Logger log = LoggerFactory.getLogger(ExtractZipUtil.class);

    /*private static int getFromIndex(String fullPath, String str,Integer count) {
        System.out.println(str);
        str = str.contains("\\") ? str += "\\" : str;
        System.out.println(str);
        Matcher slashMatcher = Pattern.compile(str).matcher(fullPath);
        int index = 0;
        while(slashMatcher.find()) {
            index++;
            if(index == count){
                break;
            }
        }
        return index;
    }*/

    public static Object unZip(File zipFile) throws Throwable {

        ZipFile zf = new ZipFile(zipFile);
        Enumeration<? extends ZipArchiveEntry> entries = zf.getEntries();

        //Save files of the same issue to a entity.
        List<List<String>> fileNames = new ArrayList<List<String>>();
        FileNameEntity fileNameEntity = new FileNameEntity(fileNames);
        //List<String> fileNameOfSameIssue = new ArrayList<>();
        String firstDirectPath = null;
        String secondDirectPath = null;
        while (entries.hasMoreElements()) {
            ZipArchiveEntry entry = entries.nextElement();
            String fullName = entry.getName();
            //String directPath = fullName.substring(0, getFromIndex(fullName, File.separator, 3));
            String directPath = fullName.substring(0, StringUtils.ordinalIndexOf(fullName, "/", 4));

            if (firstDirectPath == null) {
                firstDirectPath = directPath;
            } else {
                secondDirectPath = directPath;
                //List<String> fileNameOfSameIssue = new ArrayList<>();
            }

            if (secondDirectPath == null || firstDirectPath.contentEquals(secondDirectPath)) {
                if (secondDirectPath == null) {
                    List<String> fileNameOfSameIssue = new ArrayList<>();
                    fileNames.add(fileNameOfSameIssue);
                    fileNameOfSameIssue.add(fullName);
                } else {
                    fileNames.get(fileNames.size() - 1).add(fullName);
                }
            } else {
                firstDirectPath = secondDirectPath;
                List<String> fileNameOfSameIssue = new ArrayList<>();
                fileNames.add(fileNameOfSameIssue);
                fileNameOfSameIssue.add(fullName);
            }
        }

        // Start multiple threads to process single file inner zip
        /*Integer taskNum = 0;
        for (int i = 0; i < fileNames.size(); i++) {
            taskNum += fileNames.get(i).size();
        }*/
        HashMap<String, Object> resoults = null;
        int successCount = 0;
        try {
            int cpu = Runtime.getRuntime().availableProcessors();
            //LinkedBlockingQueue<Runnable> lbq = new LinkedBlockingQueue<Runnable>(fileNames.size());
            //ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(cpu, cpu, 200, TimeUnit.SECONDS, lbq);
            ExecutorService executorService = Executors.newFixedThreadPool(cpu);

            try {
                Class.forName(HibernateConfiguration.class.getName(), true, HibernateConfiguration.class.getClassLoader());
            } catch (Throwable e) {
                e.printStackTrace();
                throw e;
            }

            // Init PROCEDURE of SQLServer, then the process of removing duplication will use it.
            HibernateConfiguration.sessionFactory.openSession().doWork(connection -> {
                try {
                    DBUtil.initDB(connection);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            LinkedList<Future> futures = new LinkedList<>();
            // Parse XML then check duplication of Article
            for (List<String> fileNameIssue:fileNames){
                //threadPoolExecutor.execute(new ParseXMLRunableImpl(zf, fileNameIssue, Configuration.getProperties()));
                FutureTask<Object> future = new FutureTask<Object>(new ParseXMLCallableImpl(zf, fileNameIssue, Configuration.getProperties()));
                futures.add(future);
                executorService.submit(future);
            }

            executorService.shutdown();
            executorService.awaitTermination(1000L, TimeUnit.HOURS);

            resoults = new LinkedHashMap<>();
            resoults.put("TotalCount", fileNames.size());

            if (executorService.isTerminated()) {
                for (Future<Object> future:futures) {
                    if (future.isDone() && !future.isCancelled()) {
                        Object result = future.get();
                        if (result != null && String.valueOf(result).equalsIgnoreCase("SUCCESS")) {
                            successCount += 1;
                            System.out.println(result);
                            log.info(String.valueOf(result));
                        } else {
                            log.warn(String.valueOf(result));
                        }
                    } else if (future.isDone() && future.isCancelled()){
                        if (future.get() instanceof Throwable) {
                            throw (Throwable)future.get();
                        }
                    }
                }
            }
        } catch (RuntimeException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
            throw e;
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            //throw throwable;
        } finally {
            resoults.put("SuccessCount", successCount);
            resoults.put("FailCount", fileNames.size() - successCount);
        }


        return resoults;

        //threadPoolExecutor.shutdown();
        //threadPoolExecutor.awaitTermination(1000L, TimeUnit.HOURS);
        /*while (threadPoolExecutor.awaitTermination(1000L, TimeUnit.HOURS)) {
            System.out.println("================Completed===============");
        }*/
    }

    /**
     * 解压 zip 文件
     *
     * @return 返回 zip 压缩文件里的文件名的 list
     * @throws Exception
     */
    public static Object unZip() throws Throwable {
        return unZip(new File(Configuration.getProperties().getProperty(DefaultConfiguration.NAME_ZIPFILEDIR)));
    }

}