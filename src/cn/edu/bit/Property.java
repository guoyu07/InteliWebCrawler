package cn.edu.bit;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * config property class
 * Created by wuxu92 on 5/7/2015.
 */
public class Property {

    private static final String configFile = "conf/config.property";
    private static Property prop;
    private static Properties props;

    /**
     * properties
     */
    public int maxFetchingThread;
    public int maxParseThread;

    public String dbHost;
    public int dbPort;
    public String dbUsername;
    public String dbPassword;
    public String dbName;

    public boolean isTrimTags;
    public boolean isAutoChangeIp;
    public boolean useProxy;
    public String ProxyHost;
    public int ProxyPort;
    public String ProxyUsername;
    public String ProxyPassword;
    public boolean useFileLog;

    public boolean isSaveStatus;

    private Property() {
        props = new Properties();

        // load from config file
        try {
            InputStream in = new FileInputStream(configFile);
            props.load(in);
            maxFetchingThread   = Integer.parseInt(props.getProperty("maxFetchingThread", "100"));
            maxParseThread      = Integer.parseInt(props.getProperty("maxParseThread", "100"));
            dbHost = props.getProperty("dbHost");
            dbPort = Integer.parseInt(props.getProperty("dbPort", "3306"));
            dbUsername  = props.getProperty("dbUsername", "");
            dbName      = props.getProperty("dbName", "");
            isTrimTags  = Boolean.parseBoolean(props.getProperty("isTrimTags", "1"));
            isAutoChangeIp  = Boolean.parseBoolean(props.getProperty("isAutoChangeIp", "1"));
            useProxy    = Boolean.parseBoolean(props.getProperty("useProxy", "1"));
            ProxyHost   = props.getProperty("ProxyHost", "");
            ProxyPort   = Integer.parseInt(props.getProperty("ProxyPort", "3128"));
            ProxyUsername   = props.getProperty("ProxyUsername", "");
            ProxyPassword   = props.getProperty("ProxyPassword", "");
            useFileLog    = Boolean.parseBoolean(props.getProperty("useFileLog", "1"));
            isSaveStatus    = Boolean.parseBoolean(props.getProperty("isSaveStatus", "1"));
        } catch (FileNotFoundException e) {
            System.out.println("=============\r\nFile config.property not found\rr\n=============");
        } catch (IOException e) {
            System.out.println("=============\r\nLoad from props from config.property failed\rr\n=============");
        }
    }

    public static Property getInstance() {
        if (prop != null) return prop;
        else {
            return new Property();
        }
    }

    public Properties getProperties() {
        return props;
    }

    @Override
    public String toString() {
        return
                "maxFetchingThread:" + maxFetchingThread + "-" +
                "maxParseThread:" + maxParseThread + "-" +
                "dbHost:" + dbHost + "-" +
                "dbPort:" + dbPort + "-" +
                "dbUsername:" + dbUsername + "-" +
                "dbName:" + dbName + "-" +
                "isTrimTags:" + isTrimTags + "-" +
                "isAutoChangeIp:" + isAutoChangeIp + "-" +
                "useProxy:" + useProxy + "-" +
                "ProxyHost:" + ProxyHost + "-" +
                "ProxyPort:" + ProxyPort + "-" +
                "ProxyUsername:" + ProxyUsername + "-" +
                "ProxyPassword:" + ProxyPassword + "-" +
                "useFileLog:" + useFileLog + "-" +
                "useFileLog:" + useFileLog + "-" +
                "isSaveStatus:" + isSaveStatus;
    }
}
