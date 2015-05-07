package cn.edu.bit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashSet;

public class Main {


    public static Logger mainLogger = LogManager.getLogger(Main.class.getName());
    public static Logger doneLogger = LogManager.getLogger("cn.edu.bit.UrlsDone");
    public static Logger todoLogger = LogManager.getLogger("cn.edu.bit.UrlsTo");
    public static Property config = Property.getInstance();

    public static int pageSize = 0;
    public final static int THREAD_SIZE = 100;
    public final static int FULL_PAGE_SIZE = 50;
    public static int currentThreadNum = 0;

    public static HashSet<String> urlFetched = new HashSet<String>();

    public static void main(String[] args) throws IOException {

        // logger = LogManager.getLogger(Main.class.getName());
//        for (int i=0; i<50; i++) {
//            logger.info("info:: new info log" + " @ " + i);
//            logger.debug("debug:: new debug log" + " @ " + i);
//            logger.warn("warn:: new warn log" + " @ " + i);
//        }

        Calendar cal = Calendar.getInstance();
        System.out.println("Start at :: " + cal.getTime());
        mainLogger.info("Start at :: " + cal.getTime());

        // write your code here
        final String seedFile = "conf" + File.separator + "seeds.conf";
        HashSet<String> urlSet = FileParser.parseSeedUrls(seedFile);

        if (urlSet == null) return;

        for (String anUrl : urlSet) {
            // System.out.println("seed: " + anUrl);
            FetchPageThread fpt = new FetchPageThread(anUrl);
            new Thread(fpt).start();
        }

        System.out.println(config);
    }

    public synchronized static void addFetchedUrl(String urlHash) {
        urlFetched.add(urlHash);
    }
}
