package com.cnebula.nature.util;

import com.cnebula.nature.dto.Article;
import com.cnebula.nature.entity.FileNameEntity;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExtractZip {
    public static final int BUFFER_SIZE = 1024;

    private static int getFromIndex(String fullPath, String str,Integer count) {
        Matcher slashMatcher = Pattern.compile(str).matcher(fullPath);
        int index = 0;
        while(slashMatcher.find()) {
            index++;
            if(index == count){
                break;
            }
        }
        return slashMatcher.start();
    }

    public static void unZip2(File zipFile, String destDir) throws Exception {

        SAXReader saxReader = new SAXReader();
        saxReader.setValidation(false);
        /*saxReader.setEntityResolver(new EntityResolver() {
            @Override
            public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
                return new InputSource("/home/jihe/develop/IdeaProjects/nature-data-import/src/main/resources/PubMed.dtd");
            }
        });*/

        /*SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
        saxParserFactory.setValidating(true);
        saxParserFactory.setNamespaceAware(true);
        SAXParser saxParser = saxParserFactory.newSAXParser();
        saxParser.*/

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
            String directPath = fullName.substring(0, getFromIndex(fullName, File.separator, 3));

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
        Integer taskNum = 0;
        for (int i = 0; i < fileNames.size(); i++) {
            taskNum += fileNames.get(i).size();
        }
        int cpu = Runtime.getRuntime().availableProcessors();
        LinkedBlockingQueue<Runnable> lbq = new LinkedBlockingQueue<Runnable>(taskNum);
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(cpu, cpu, 200, TimeUnit.SECONDS, lbq);

        // Parse XML then check duplication of Article
        for (List<String> fileNameIssue:fileNames){
            //threadPoolExecutor.execute(new ParseXMLRunableImpl(zf, fileNameIssue));
        }

        threadPoolExecutor.shutdown();
    }

    /**
     * 解压 zip 文件
     *
     * @param zipFile zip 压缩文件
     * @param destDir zip 压缩文件解压后保存的目录
     * @return 返回 zip 压缩文件里的文件名的 list
     * @throws Exception
     */
    public static List<String> unZip(File zipFile, String destDir) throws Exception {
        // 如果 destDir 为 null, 空字符串, 或者全是空格, 则解压到压缩文件所在目录
        if (StringUtils.isBlank(destDir)) {
            destDir = zipFile.getParent();
        }

        destDir = destDir.endsWith(File.separator) ? destDir : destDir + File.separator;
        ZipArchiveInputStream is = null;
        List<String> fileNames = new ArrayList<String>();

        try {
            is = new ZipArchiveInputStream(new BufferedInputStream(new FileInputStream(zipFile), BUFFER_SIZE));
            ZipArchiveEntry entry = null;

            //is.getNextEntry();
            while ((entry = is.getNextZipEntry()) != null) {
                fileNames.add(entry.getName());

                ZipFile zf = new ZipFile(zipFile);
                InputStream ips = zf.getInputStream(entry);

                SAXReader sr = new SAXReader();
                Document doc = sr.read(ips);
                Element rootElement = doc.getRootElement();
                List<Element> list = rootElement.elements();
                for (Element element : list) {
                    element.attributes();
                }
                //ips.
                //entry.getExtra();
                //File file = new File(destDir, entry.);
                //if ()

                if (entry.isDirectory()) {
                    File directory = new File(destDir, entry.getName());
                    directory.mkdirs();
                } else {
                    OutputStream os = null;
                    try {
                        os = new BufferedOutputStream(new FileOutputStream(new File(destDir, entry.getName())), BUFFER_SIZE);
                        IOUtils.copy(is, os);
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        IOUtils.closeQuietly(os);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            IOUtils.closeQuietly(is);
        }

        return fileNames;
    }

    public static void unZip3(File zipFile, String destDir) throws Exception {
        ZipFile zf = new ZipFile(zipFile);
        Enumeration<ZipArchiveEntry> entries = zf.getEntries();
        while (entries.hasMoreElements()) {
            ZipArchiveEntry ze = entries.nextElement();
            InputStream is = zf.getInputStream(ze);
            SAXReader sr = new SAXReader();
            Document doc = null;
            try {
                doc = sr.read(is);
            } catch (DocumentException e) {
                e.printStackTrace();
            }


            Element rootElement = doc.getRootElement();
            List<Element> list = rootElement.elements();
            for (Element element : list) {
                element.asXML();
                element.getText();
                System.out.println(element.asXML());
                element.getData();
                element.getStringValue();
                element.getTextTrim();
                element.toString();
                element.getDocument().content().stream();
                element.attributes();
            }
        }
    }
    /**
     * 解压 zip 文件
     *
     * @param zipfile zip 压缩文件的路径
     * @param destDir zip 压缩文件解压后保存的目录
     * @return 返回 zip 压缩文件里的文件名的 list
     * @throws Exception
     */
    public static void unZip(String zipfile, String destDir) throws Exception {
        File zipFile = new File(zipfile);
        unZip3(zipFile, destDir);
    }

    public static void main(String[] args) throws Exception {
        unZip("/home/jihe/developFiles/nature/ftp_PUB_19-04-06_05-50-17.zip", "/home/jihe/developFiles/nature");
        //System.out.println(names);
        Article article = new Article();
        //article.
    }

    /*public static void main(String[] args) throws Exception {
        SAXReader saxReader = new SAXReader();
        saxReader.setValidation(false);
        saxReader.setEntityResolver(new EntityResolver() {
            @Override
            public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
                return new InputSource("/PubMed.dtd");
            }
        });
    }*/
}