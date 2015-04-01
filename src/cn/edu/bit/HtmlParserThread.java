package cn.edu.bit;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.BlockingQueue;

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
        Main.currentThreadNum++;

        System.out.println("Html page parsing @ " + Thread.currentThread().getName());

        String pageStr = null;
        try {
            pageStr = pageQueue.take();
        } catch (InterruptedException e) {
            System.out.println("queue take error " + e.getMessage());
        }
        while (pageStr != null) {
            ArrayList<String> urlArr = new ArrayList<String>();

            try {
                String fileName = FileUtils.nowTime() + "-" + Thread.currentThread().getName() + ".html";
                // System.out.println( "new file: " + fileName);
                fu.setName(fileName);
                fu.setContent(pageStr);
                fu.saveToFile();
                fu.close();
            } catch (IOException e) {
                System.out.println("save one file error, for " + e.getMessage());
            }


            doc = Jsoup.parse(pageStr);
            Elements links = doc.select("a[href]");
            for (Element link : links) {
                String hrefStr = link.attr("href");
                // if (hrefStr.startsWith("http://item.jd.com") || hrefStr.startsWith("http://channel.jd.com") || hrefStr.startsWith("http://list.jd.com") ) {
                if (hrefStr.startsWith("http://book.douban.com") || hrefStr.startsWith("http://music.douban.com") || hrefStr.startsWith("http://movie.douban.com") ) {
                    // check if fetched
                    String hashUrl = FileUtils.md5(hrefStr);
                    if (Main.urlFetched.contains(hashUrl)) {
                        continue;
                    }
                    urlArr.add(hrefStr);
                    Main.urlFetched.add(hashUrl);
                }
            }
            // add to urlQueue
            // if queue is full or cannot put all new urls to queue
            // then remove part of the new urls, and add the rest to the queue
//            try {
                int restNum = FetchPageThread.URL_QUEUE_SIZE - urlQueue.size();
                int oldSize = urlArr.size();
                if (restNum > 0 && restNum < oldSize) {
                    // remove some
                    int removeNum = oldSize - restNum;
                    int i=0;
                    for (; i<removeNum; i++) {
                        urlArr.remove(i);
                    }
                    
                    System.out.println("drop urls: " + (i+1) + " :: add:" + urlArr.size() );
                } else if (restNum < 0) {
                    urlArr.clear();
                }
                urlQueue.addAll(urlArr);
//            } catch (InterruptedException e) {
//                System.out.println("put to queue error" + e.getMessage());
//            }
            try {
                pageStr = pageQueue.take();
            } catch (InterruptedException e) {
                System.out.println("page queue take error: " + e.getMessage());
            }
        }
    }
}
