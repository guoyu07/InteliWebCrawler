package cn.edu.bit;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * class HtmlParserThread
 * user Jsoup to parser html
 * Created by wuxu92 on 3/26/2015.
 */
public class HtmlParserThread implements Runnable {

    BlockingQueue<String> urlQueue;
    BlockingQueue<String> pageQueue;
    // String pageStr;
    Document doc;

    // public HtmlParserThread(BlockingQueue<String> queue) {
    //     this.urlQueue = queue;
    // }
    public HtmlParserThread(BlockingQueue<String> pageQueue, BlockingQueue<String> queue) {
        this.pageQueue = pageQueue;
        // doc = Jsoup.parse(pageStr);
        this.urlQueue = queue;
    }
    /**
     * simple version for just get all links to put to urlqueue
     */
    @SuppressWarnings("ReturnInsideFinallyBlock")
    @Override
    public void run() {
        // FileUtils fu = new FileUtils(Thread.currentThread().getName() + "-" + url.substring(url.lastIndexOf("/")+1) + "_1.html");
        // FileUtils fu = new FileUtils(Thread.currentThread().getName() + "-" + FileUtils.md5(url).substring(0, 8) + "_1.html");
        // fu.setName(Thread.currentThread().getName() + FileUtils.md5(url).substring(0, 8) + ".html");

        // use timestamp as filename instead of url's md5 code
        FileUtils fu = null;
        fu = new FileUtils();

        // fu.setName();
        Random rand = new Random();
        Main.currentThreadNumPlus();

        Main.mainLogger.info("Html page parsing @ " + Thread.currentThread().getName());

        String pageStr = null;
        try {
            pageStr = pageQueue.poll(10000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Main.mainLogger.info("queue take error " + e.getMessage());
        }
        while (pageStr != null) {
            ArrayList<String> urlArr = new ArrayList<String>();
            int urlIndex =  pageStr.indexOf(System.getProperty("line.separator"));
            String parentUrl = pageStr.substring(0, urlIndex);
            String baseUrl = getUrlHost(parentUrl);
            // String fileName = FileUtils.nowTime() + "-" + Thread.currentThread().getName() + ".html";
            String fileName = FileUtils.shortMd5(parentUrl) + ".html";
            // System.out.println( "new file: " + fileName);

            synchronized (this.getClass()) {
                fu.setName(fileName);
                fu.setContent(pageStr.substring(urlIndex + System.getProperty("line.separator").length()));
                fu.saveToFile();
                Main.fetchedCountPlus();
                fu.close();
            }

            // @todo if Main.urlFetched.size() is enough then do no more parse
            if (Main.isFetchedMapFull) {
                try {
                    // if (pageQueue.size() == 0) return;
                    pageStr = pageQueue.take();
                } catch (InterruptedException e) {
                    Main.mainLogger.info("page queue take error: " + e.getMessage());
                }
                continue;
            }


            doc = Jsoup.parse(pageStr);
            Elements links = doc.select("a[href]");
            for (Element link : links) {
                String hrefStr = (link.attr("href"));
                if (hrefStr.startsWith("//")) hrefStr = "http:" + hrefStr;
                else if (hrefStr.startsWith("/")) hrefStr = baseUrl + hrefStr;

                // remove hashTag
                if (hrefStr.indexOf("#") > 0) {
                    hrefStr = hrefStr.substring(hrefStr.indexOf("#"));
                }
                // check if it a available link
                if ( !isAvailableUrl(hrefStr) ) continue;

                // check if fetched
                // fetch only the first 8 letters as url's hash code
                String hashUrl = FileUtils.shortMd5(hrefStr);
                if (Main.urlFetched.contains(hashUrl)) {
                    // System.out.println("hash " + hashUrl + "has fetched. skip..");
                    continue;
                }
                urlArr.add(hrefStr);

                // add hashUrl to fetched url hashSet
                // Main.addFetchedUrl(hashUrl);
                // add hashUrl directly
                Main.urlFetched.add(hashUrl);
                /**
                 * there is a logic error here, for urlFetched map has lots of bad pages
                 */
                // if (Main.urlFetched.size() > Main.config.pagesToFetch) {
                //     Main.mainLogger.info("==============");
                //     Main.mainLogger.info("- count done -");
                //     Main.mainLogger.info("==============");
                //     System.out.println("==========fetched map full ==========");
                //     Main.isFetchedMapFull = true;
                // }
                // Main.mapLogger.info(hashUrl + "   " + parentUrl);
                // add to hashUrlMap, log when exit
                // Main.hashUrlMap.put(hashUrl, hrefStr);
                // log it directly
                Main.mapLogger.info(hashUrl + " " + hrefStr);
                // }
            }
            // add to urlQueue
            // if queue is full or cannot put all new urls to queue
            // then remove part of the new urls, and add the rest to the queue
//            try {
            /**
             * drop is not needed any more
             * use offer instead
             */
            // int restNum = FetchPageThread.URL_QUEUE_SIZE - urlQueue.size();
            // int oldSize = urlArr.size();
            // if (restNum > 0 && restNum < oldSize) {
            //     // remove some
            //     int removeNum = oldSize - restNum;
            //     int i=0;
            //     for (; i<removeNum; i++) {
            //         // always remove the first item
            //         urlArr.remove(1);
            //     }
            //
            //     Main.mainLogger.info("drop urls: " + (i + 1) + " :: add:" + urlArr.size());
            // } else if (restNum < 0) {
            //     urlArr.clear();
            //     Main.mainLogger.info("Drop all new urls");
            // }
            // urlQueue.addAll(urlArr);
            for (String u : urlArr) {
                try {
                    urlQueue.offer(u, 300, TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                    Main.mainLogger.info("put into url queue error " + e.getMessage());
                }
            }

            try {
                // wait for 10 seconds, if there is no page available after this time
                // then null will be assigned
                pageStr = pageQueue.poll(10000, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                Main.mainLogger.info("page queue take error: " + e.getMessage());
            }
        }
        
        System.out.println(Thread.currentThread().getName() + " quit for pageStr is null");
        Main.mainLogger.info(Thread.currentThread().getName() + " quit for pageStr is null");
    }

    /**
     * process url, especially for taobao to remove the spam part of it
     * @param url String url to be processed
     * @return String
     */
    public static String removeUrlSpm(String url) {
        int spamIndex = url.indexOf("spm");
        if (spamIndex != -1) {
            StringBuilder urlBuilder = new StringBuilder(url);
            int endOfSpm = url.indexOf("&", spamIndex);

            if (endOfSpm == -1) endOfSpm = url.length();
            urlBuilder.delete(spamIndex, endOfSpm);
            return urlBuilder.toString();
        } else return url;
    }

    /**
     * for detail.taobao.com remove all other params
     * leave id only is ok
     * @param url String
     * @return String
     */
    public static String sliceUrlForId(String url) {
//        int htmIndex = url.indexOf("item.htm?");
//        if (htmIndex == -1) return url;
//        int idIndex = url.indexOf("id=");
//        if (idIndex != -1) {
//            StringBuilder urlBuilder = new StringBuilder(url);
//            int endOfId = url.indexOf("&", idIndex);
//
//            if (endOfId == -1) endOfId = url.length();
//            // urlBuilder.delete(idIndex, endOfId);
//            String idStr = urlBuilder.substring(idIndex, endOfId);
//            urlBuilder.delete(htmIndex + 9, urlBuilder.length());
//            urlBuilder.append(idStr);
//            return urlBuilder.toString();
//        } else
            return url;
    }

    /**
     * slice list/search url, leave cat param only is ok
     * @param url String
     * @return String
     */
    public static String sliceListUrl(String url) {
//        if (url.startsWith("http://list.tmall.com/")) {
//            int htmIndex = url.indexOf("htm?");
//            // String newQuery = "";
//
//            int catIndex = url.indexOf("cat=");
//            if (catIndex != -1) {
//                StringBuilder urlBuilder = new StringBuilder(url);
//                int endOfCat = urlBuilder.indexOf("&", catIndex);
//
//                if (endOfCat == -1) endOfCat = urlBuilder.length();
//                String catStr = urlBuilder.substring(catIndex, endOfCat);
//                urlBuilder.delete(htmIndex + 4, urlBuilder.length());
//                urlBuilder.append(catStr);
//                return urlBuilder.toString();
//            }
//        }
        return url;
    }


    public static boolean isAvailableUrl(String href) {

        /**
         * if href out of seeds domains, return false
         */
        if (!(href.contains("csdn") || href.contains("cnblogs") || href.contains("51cto") || href.contains("iteye") ) ) {
            // System.out.println("bad href out of domains: " + href);
            return false;
        }
        // 登录页面则跳过
        if (href.contains("signin") || href.contains("login")) return false;

        // final String[] postfix = {"jpg", "jpeg", "pdf", "apk", "zip", "rar", "7z", "tar", "gz", "2z", "", "gif", "ttf", "swf", "doc"};
        final List<String> postfixList = Main.config.excludeType;

        // @todo url like q.cnblogs.com/u/554118/bestanswer is good
        int suffixIndex = href.lastIndexOf(".");
        if (suffixIndex != -1) {
            String suffix = href.substring(suffixIndex+1).toLowerCase();
            // Boolean isUrlOk = href.startsWith("http://") && !(postfixList.contains(suffix));
            // remove protocol limit for some a-tag's href has not protocol part
            // @2015-05-23
            Boolean isUrlOk = !(postfixList.contains(suffix));
            if (!isUrlOk) {
                Main.mainLogger.info("Bad Url: " + href);
            }
            return isUrlOk;
        }
        return true;
    }

    public static String getUrlHost(String url) {
        URI uri;
        try {
            uri =  new URI(url);
        } catch (URISyntaxException e) {
            return "";
        }
        return uri.getHost();
    }
}
