package com.cnebula.nature.test;

import com.cnebula.nature.dto.Affiliation;
import org.json.JSONArray;
import org.junit.Test;

import java.io.*;

public class FileTest {

    public static void main(String[] agrs) throws IOException {
        /*File file = new File("/home/jihe/develop/IdeaProjects/nature-data-import", "JOU=41570/VOL=2019.3/ISU=4/ART=85/BodyRef/PDF/41570_2019_Article_85.pdf");
        file.getParentFile().mkdirs();
        file.createNewFile();*/

        /*String pdf = "41570_2019_Article_84.pdf";

        System.out.println(pdf.substring(0, pdf.lastIndexOf(".pdf")));*/

        //String dir = "JOU=41570/VOL=2019.3/ISU=4";
        //String jtl = dir.substring(0, dir.indexOf(File.separator)).split("=")[1];
        //System.out.println(jtl);
        testOutputPDF();
    }

    public static void testOutputPDF() throws FileNotFoundException {
        FileInputStream isPdf = new FileInputStream("/home/jihe/developFiles/nature/tmp/ftp_PUB_19-04-06_05-50-17 (复件)/JOU=41570/VOL=2019.3/ISU=4/ART=84/BodyRef/PDF/41570_2019_Article_84.pdf");
        FileOutputStream bos = null;
        try {
            bos = new FileOutputStream("/home/jihe/developFiles/nature/tmp/" + "test".concat(".pdf"), false);
            //bos = new BufferedOutputStream(new FileOutputStream(pdfBaseDir + File.separator + pdfDirChild + File.separator + at.getPips().concat(".pdf"), false));
            byte[] bt = new byte[1024];
            int length = 0;
            while ((length = isPdf.read(bt)) != -1) {
                bos.write(bt);
                bos.flush();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                bos.close();
                isPdf.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void testNumeric() {
        String code = "4157.0";
        String[] split = code.split("\\.");
        System.out.println(code.split(".")[0]);
    }

    @Test
    public void testString() {
        StringBuilder aff = new StringBuilder();
        aff.append("");
        Affiliation affiliation = new Affiliation();
        affiliation.setAff(aff.toString());
        System.out.println(affiliation.getAff());
        aff.append("UK");
        aff.append("UAS");
        //affiliation.setAff(aff.toString());

        System.out.println(affiliation.getAff());
    }

    @Test
    public void testContinue() {
        int[] arr = {1,2,3,4,5};
        for (int i = 0; i < arr.length; i++) {
            if (i == 1) continue;
            System.out.println(arr[i]);
        }
    }

    @Test
    public void testJsonArray() {
        JSONArray jsonArray = new JSONArray();
        System.out.println(jsonArray.toString());
    }
}
