package com.cnebula.nature.util;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.jdbc.ScriptRunner;

import java.io.IOException;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.SQLException;

public class DBUtil {

    public static void initDB(Connection connection) throws IOException, SQLException {
        ScriptRunner sr = new ScriptRunner(connection);
        sr.setAutoCommit(true);
        sr.setEscapeProcessing(false);
        sr.setStopOnError(true);
        sr.setSendFullScript(false);
        sr.setDelimiter("GO"); // The default delimiter for each command of SQLServer is GO, however it's ; for MySQL etc.
        sr.setRemoveCRs(true);
        sr.setFullLineDelimiter(true);
        Resources.setCharset(Charset.forName("UTF8"));
        sr.runScript(Resources.getResourceAsReader("sql/template_create_procedure.sql"));
        sr.closeConnection();
        connection.close();
    }
}
