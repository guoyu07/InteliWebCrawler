package cn.edu.bit;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * config property class
 * Created by wuxu92 on 5/7/2015.
 */
public class Property {

    private static final String configFile = "conf/config.properties";
    private static Property prop;
    private static Properties props;

    /**
     * properties
     */
    public int maxFetchingThread;
    public int maxParseThread;
    public int pagesToFetch;

    public String dbHost;
    public int dbPort;
    public String dbUsername;
    public String dbPassword;
    public String dbName;

    public boolean isTrimTags;
    public boolean isAutoChangeIp;
    public boolean useProxy;
    public String   proxyHost;
    public int      proxyPort;
    public String   proxyUsername;
    public String   proxyPassword;
    public boolean  useFileLog;

    public boolean isSaveStatus;
    public boolean isResume;
    public boolean isResumeFromOneFile;

    public List<String> excludeType;

    private Property() {
        props = new Properties();

        // load from config file
        try {
            InputStream in = new FileInputStream(configFile);
            props.load(in);
            maxFetchingThread   = Integer.parseInt(props.getProperty("maxFetchingThread", "100"));
            maxParseThread      = Integer.parseInt(props.getProperty("maxParseThread", "100"));
            pagesToFetch        = Integer.parseInt(props.getProperty("pagesToFetch", "10000"));
            dbHost = props.getProperty("dbHost");
            dbPort = Integer.parseInt(props.getProperty("dbPort", "3306"));
            dbUsername  = props.getProperty("dbUsername", "");
            dbPassword  = props.getProperty("dbPassword", "");
            dbName      = props.getProperty("dbName", "");
            isTrimTags  = Boolean.parseBoolean(props.getProperty("isTrimTags", "1"));
            isAutoChangeIp  = Boolean.parseBoolean(props.getProperty("isAutoChangeIp", "1"));
            useProxy    = Boolean.parseBoolean(props.getProperty("useProxy", "1"));
            proxyHost = props.getProperty("proxyHost", "");
            proxyPort = Integer.parseInt(props.getProperty("proxyPort", "3128"));
            proxyUsername = props.getProperty("proxyUsername", "");
            proxyPassword = props.getProperty("proxyPassword", "");
            useFileLog    = Boolean.parseBoolean(props.getProperty("useFileLog", "1"));
            isSaveStatus    = Boolean.parseBoolean(props.getProperty("isSaveStatus", "1"));
            isResume        = Boolean.parseBoolean(props.getProperty("isResume", "1"));
            isResumeFromOneFile        = Boolean.parseBoolean(props.getProperty("isResumeFromOneFile", "1"));

            String excludeTypeStr = props.getProperty("excludeType", "");
            excludeType = Arrays.asList(excludeTypeStr.split(","));

        } catch (FileNotFoundException e) {
            System.out.println("=============\r\nFile config.properties not found\rr\n=============");
        } catch (IOException e) {
            System.out.println("=============\r\nLoad from props from config.properties failed\rr\n=============");
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
                        "proxyHost:" + proxyHost + "-" +
                        "proxyPort:" + proxyPort + "-" +
                        "proxyUsername:" + proxyUsername + "-" +
                        "proxyPassword:" + proxyPassword + "-" +
                        "useFileLog:" + useFileLog + "-" +
                        "useFileLog:" + useFileLog + "-" +
                        "isSaveStatus:" + isSaveStatus +
                        "excludeTypes: " + (excludeType);
    }
}
