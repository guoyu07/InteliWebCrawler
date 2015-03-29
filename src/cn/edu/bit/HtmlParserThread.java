package cn.edu.bit;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.concurrent.ArrayBlockingQueue;
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
    @Override
    public void run() {


        Main.currentThreadNum++;

        System.out.println("Html page parsing @ " + Thread.currentThread().getName());

        String pageStr = null;
        try {
            pageStr = pageQueue.take();
        } catch (InterruptedException e) {
            System.out.println("queue take error " + e.getMessage());
        }
        while (pageStr != null) {
            doc = Jsoup.parse(pageStr);
            Elements links = doc.select("a[href]");
            for (Element link : links) {
                String hrefStr = link.attr("href");
                if (hrefStr.startsWith("http://item.jd.com")) {
                    // check if fetched
                    String hashUrl = FileUtils.md5(hrefStr);
                    if (Main.urlFetched.contains(hashUrl)) {
                        continue;
                    }
                    try {
                        urlQueue.put(hrefStr);
                        Main.urlFetched.add(hashUrl);
                    } catch (InterruptedException e) {
                        System.out.println("put to queue error" + e.getMessage());
                    }
                }
            }
            try {
                pageStr = pageQueue.take();
            } catch (InterruptedException e) {
                System.out.println("page queue take error: " + e.getMessage());
            }
        }
    }
}
