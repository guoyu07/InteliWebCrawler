package cn.edu.bit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.util.Calendar;
import java.util.HashSet;
import java.util.concurrent.*;

public class Main {


    public static Logger mainLogger = LogManager.getLogger(Main.class.getName());
    public static Logger doneLogger = LogManager.getLogger("cn.edu.bit.UrlsDone");
    public static Logger todoLogger = LogManager.getLogger("cn.edu.bit.UrlsTo");
    public static Logger mapLogger = LogManager.getLogger("cn.edu.bit.HashUrlMap");
    public static Property config = Property.getInstance();

    public static String[] exclusives = FileParser.parseFileByLine("conf/exclusive.conf");

    public static String[] includes = FileParser.parseFileByLine("conf/include.conf");

    public static boolean hasLogedHash = false;

    // Is Main.urlFetched more then config.pagesToFetch
    public static boolean isFetchedMapFull = false;

    // pages count, start from 1
    public static int pageCount = 1;
    public final static int THREAD_SIZE = Main.config.maxFetchingThread;

    // 最大爬取网页数量
    public final static int FULL_PAGE_SIZE = config.pagesToFetch+100;

    // 当前线程数
    public static int currentThreadNum = 0;

    public static int currentParseThreadNum = 0;

    // 保存已经爬取的页面的url的hash的集合
    public static ConcurrentSkipListSet<String> urlFetched = new ConcurrentSkipListSet<String>();

    // 保存url的短md5值与原url的对应，最后一次写入日志
    // public static ConcurrentHashMap<String, String> hashUrlMap = new ConcurrentHashMap<String, String>();

    // 保存各线程待爬取队列的引用
    public static ConcurrentHashMap<String, BlockingQueue<String>> threadUrlMap = new ConcurrentHashMap<String, BlockingQueue<String>>();

    public static ExecutorService executor;

    public static Proxy proxy = Main.setProxy();

    public static void main(String[] args) throws IOException {

        Calendar cal = Calendar.getInstance();
        System.out.println("Start at :: " + cal.getTime());
        System.out.println(config);
        mainLogger.info("Start at :: " + cal.getTime());

        // init a thread pool up to 100
        // executor = Executors.newFixedThreadPool(100);
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                System.out.println("Thread " + t.getName() + " end cuz " + e.getMessage() + " restarting now.");
                e.printStackTrace();
                String oldThreadName = t.getName();
                BlockingQueue<String> oldQueue = threadUrlMap.get(oldThreadName);
                // return if oldQueue has no url left
                if (oldQueue == null || oldQueue.size() == 0) return;
                // or use new thread
                new Thread(new FetchPageThread(oldQueue)).start();
                System.out.println("thread restarted");

                // remove old key-value pair
                // just remove the reference, will not delete the object
                threadUrlMap.remove(oldThreadName);
            }
        });
        // resume process
        // start processes to fetch pages, do not use the seed file
        if (config.isResume) {
            // get url-to and url-done
            System.out.println("resuming starting");
            String statusDir = "status";
            File dir = new File(statusDir);
            File[] listOfFiles = dir.listFiles();
            int newThreadCount = 0;
            for (File f : listOfFiles != null ? listOfFiles : new File[0]) {
                if (f.isFile()) {
                    String fileName = f.getName();
                    if (fileName.startsWith("url-to") && f.canRead()) {
                        if ( Main.config.isResumeFromOneFile) {
                            int threadCount = Main.resumeThreadsFromFile(f);
                            System.out.println("resumed thread count: " + threadCount);
                            Main.mainLogger.info("resumed thread count: " + threadCount);
                        } else {
                            newThreadCount++;
                            Main.resumeOneThreadFromOneFile(f);
                        }
                        // clean the url-to file
                        FileUtils.cleanFile(f);
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

            if (newThreadCount != 0) mainLogger.info("new thread resumed: " + newThreadCount);

        } else {
            // read seed file to start crawling
            // write your code here
            int seedCount = Main.startFromSeedFile();
            Main.mainLogger.info("started form seed file:" + seedCount + " threads started");
            System.out.println("started form seed file:" + seedCount + " threads started");
        }

        // System.out.println(config);
    }

    public synchronized static void addFetchedUrl(String urlHash) {
        urlFetched.add(urlHash);
    }

    public synchronized static void fetchedCountPlus() {
        Main.pageCount++;
    }

    public synchronized static void currentThreadNumPlus() {
        Main.currentThreadNum++;
    }

    public synchronized static void currentThreadNumMinus() {
        Main.currentThreadNum--;
    }


    public synchronized static void currentParseThreadNumPlus() {
        Main.currentParseThreadNum++;
    }
    public synchronized static void currentParseThreadNumMinus() {
        Main.currentParseThreadNum--;
    }
    /**
     * save every thread's to-fetch urls to file (one file)
     */
    public static void saveUrlsToFetchToFile() {
        for (java.util.Map.Entry<String,BlockingQueue<String>> entry : Main.threadUrlMap.entrySet())  {
            BlockingQueue<String> urlQueue = entry.getValue();
            for (String url : urlQueue) {
                Main.todoLogger.info(url);
            }
            Main.todoLogger.info("===");
        }
    }

    /**
     * resume one fetching thread from one saved file
     * @param f file that saves the urls
     * @throws IOException
     */
    public static void resumeOneThreadFromOneFile(File f) throws IOException {
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
        System.out.println("resuming a new thread from one file");
    }

    /**
     * resume all fetching from one url-to file
     * each line obtains an url to fetching
     * use "===" line as separator of the threads
     * @param f File contains the urls
     * @return number of threads started by this function
     * @throws IOException
     */
    public static int resumeThreadsFromFile(File f) throws IOException {
        String line;
        int threadCount = 0;
        BufferedReader br = new BufferedReader(new FileReader(f));
        while ((line = br.readLine()) != null) {
            BlockingQueue<String> urlToFetch = new ArrayBlockingQueue<String>(FetchPageThread.URL_QUEUE_SIZE);
            // System.out.println("resuming url @ " + line);
            while (line != null && !line.equals("===")) {
                try {
                    urlToFetch.put(line);
                } catch (InterruptedException e) {
                    Main.mainLogger.info("add url failed");
                }
                line = br.readLine();
            }
            if (urlToFetch.size() > 0) {
                threadCount++;
                new Thread(new FetchPageThread(urlToFetch)).start();
                System.out.println("resuming a new thread");
            }
        }

        return threadCount;
    }

    /**
     * start crawling from seed file
     * each line obtains a seed url
     * @return number of thread/seed started
     */
    public static int startFromSeedFile() {
        int threadCount = 0;
        final String seedFile = "conf" + File.separator + "seeds.conf";
        HashSet<String> urlSet = FileParser.parseSeedUrls(seedFile);

        if (urlSet == null) return 0;

        for (String anUrl : urlSet) {
            // System.out.println("seed: " + anUrl);
            FetchPageThread fpt = new FetchPageThread(anUrl);
            new Thread(fpt).start();
            threadCount++;
        }

        return threadCount;
    }

    public static Proxy setProxy() {
        Proxy proxy = null;
        if (Main.config.useProxy) {
            proxy= new Proxy(Proxy.Type.HTTP,
                    new InetSocketAddress(Main.config.proxyHost, Main.config.proxyPort)
            );

            // if use authenticator for this proxy
            // set authenticator's default value
            if ( !Main.config.proxyUsername.equals("")) {
                Authenticator auth = new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(Main.config.proxyUsername, Main.config.proxyPassword.toCharArray());
                    }
                };

                Authenticator.setDefault(auth);
            }
        }
        return proxy;
    }
}
