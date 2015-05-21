package cn.edu.bit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.Calendar;
import java.util.HashSet;
import java.util.concurrent.*;

public class Main {


    public static Logger mainLogger = LogManager.getLogger(Main.class.getName());
    public static Logger doneLogger = LogManager.getLogger("cn.edu.bit.UrlsDone");
    public static Logger todoLogger = LogManager.getLogger("cn.edu.bit.UrlsTo");
    public static Logger mapLogger = LogManager.getLogger("cn.edu.bit.HashUrlMap");
    public static Property config = Property.getInstance();

    // pages count
    public static int pageCount = 0;
    public final static int THREAD_SIZE = 100;

    // 总共要爬取的数量
    public final static int FULL_PAGE_SIZE = config.pagesToFetch;

    // 当前已有爬取线程数量
    public static int currentThreadNum = 0;

    // 已爬取页面集合,保存的是url的hash值
    public static ConcurrentSkipListSet<String> urlFetched = new ConcurrentSkipListSet<String>();

    // 线程的待爬取队列，Thread.getName与待爬取队列映射
    public static ConcurrentHashMap<String, BlockingQueue<String>> threadUrlMap = new ConcurrentHashMap<String, BlockingQueue<String>>();

    public static ExecutorService executor;

    public static void main(String[] args) throws IOException {

        Calendar cal = Calendar.getInstance();
        System.out.println("Start at :: " + cal.getTime());
        mainLogger.info("Start at :: " + cal.getTime());

        // init a thread pool upto 100
        executor = Executors.newFixedThreadPool(100);
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                System.out.println("Thread " + t.getName() + " end cuz " + e.getMessage() + " restarting now.");
                String oldThreadName = t.getName();
                BlockingQueue<String> oldQueue = threadUrlMap.get(oldThreadName);
                if (oldQueue == null) return;
                // new thread
                new Thread(new FetchPageThread(oldQueue)).start();

                // remove old key-value pair
                // just remove the reference, will not delete the object
                threadUrlMap.remove(oldThreadName);
            }
        });
        // resume process
        // start processes to fetch pages, do not use the seed file
        if (config.isResume) {
            // get url-to and url-done
            String statusDir = "status";
            File dir = new File(statusDir);
            File[] listOfFiles = dir.listFiles();
            int newThreadCount = 0;
            for (File f : listOfFiles != null ? listOfFiles : new File[0]) {
                if (f.isFile()) {
                    String fileName = f.getName();
                    if (fileName.startsWith("url-to") && f.canRead()) {
                        BlockingQueue<String> urlToFetch = new ArrayBlockingQueue<String>(FetchPageThread.URL_QUEUE_SIZE);
                        String line;
                        BufferedReader br = new BufferedReader(new FileReader(f));
                        while ((line = br.readLine()) != null) {
                            try {
                                urlToFetch.put(line);
                            } catch (InterruptedException e) {
                                Main.mainLogger.info("add url failed");
                            }
                        }

                        // start a new fetching thread
                        new Thread(new FetchPageThread(urlToFetch)).start();
                        newThreadCount++;
                        System.out.println("resuming a new thread");
                    } else if (f.isFile() && f.getName().startsWith("url-done") && f.canRead()) {
                        // load urls that has done
                        BufferedReader br = new BufferedReader(new FileReader(f));
                        Main.urlFetched = new ConcurrentSkipListSet<String>();
                        String url;
                        while ((url = br.readLine()) != null) {
                            urlFetched.add(url);
                        }
                        System.out.println("load url-done list done");
                        Main.mainLogger.info("load url-done list done " + urlFetched.size() + " loaded");
                    }
                }
            }

            mainLogger.info("new thread resumed: " + newThreadCount);

        } else {
            // read seed file to start crawling
            // write your code here
            final String seedFile = "conf" + File.separator + "seeds.conf";
            HashSet<String> urlSet = FileParser.parseSeedUrls(seedFile);

            if (urlSet == null) return;

            for (String anUrl : urlSet) {
                // System.out.println("seed: " + anUrl);
                FetchPageThread fpt = new FetchPageThread(anUrl);
                new Thread(fpt).start();
            }
        }

        System.out.println(config);
    }

    public synchronized static void addFetchedUrl(String urlHash) {
        urlFetched.add(urlHash);
    }
}
