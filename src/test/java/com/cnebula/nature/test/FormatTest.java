package com.cnebula.nature.test;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.XML;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;

/**
 * Created by calis on 2019/6/12.
 */
public class FormatTest {

    @Test
    public void testJson2String() {
        JSONArray jsonArray = new JSONArray();
        jsonArray.put(1).put("22a");
        System.out.println(jsonArray);
        System.out.println(jsonArray.toString());
    }

    @Test
    public void testXML2Json() {
        FileInputStream ipss = null;
        try {
            ipss = new FileInputStream("C:\\develop\\WorkSpace-IDEA-NATURE\\Nature-New\\src\\main\\resources\\41612_2018_Article_34_nlm.xml.Meta");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        JSONObject jsonObject = XML.toJSONObject(new InputStreamReader(ipss));
        System.out.println(jsonObject);
    }
}
