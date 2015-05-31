package cn.edu.bit;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import sun.reflect.misc.FieldUtil;

import java.io.File;
import java.sql.Time;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.Assert.*;

/**
 * test file
 * Created by wuxu92 on 5/21/2015.
 */
public class MainTest {

    @Test
    @Ignore
    public void TestBlockingQueueCopy() throws InterruptedException {
        ConcurrentHashMap<String, BlockingQueue<String>> threadUrlMap = new ConcurrentHashMap<String, BlockingQueue<String>>();
        BlockingQueue<String> blockQ = new ArrayBlockingQueue<String>(10);
        blockQ.put("aaa");
        blockQ.put("bbb");
        blockQ.put("ccc");

        threadUrlMap.put("url-1", blockQ);
        BlockingQueue<String> bq2 = threadUrlMap.get("url-1");
        Assert.assertEquals(blockQ, bq2);

        bq2.put("ddd");
        Assert.assertEquals(blockQ, bq2);

        threadUrlMap.remove("url-1");
        System.out.println(bq2);
        System.out.println(threadUrlMap.get("url-1"));
        Assert.assertEquals(blockQ, bq2);

    }

    @Test
    @Ignore
    public void TestShortMd5() {
        Assert.assertEquals(FileUtils.shortMd5("http://home.cnblogs.com/u/Dawn----123/"), FileUtils.shortMd5("http://home.cnblogs.com/u/Dawn----123/"));
        System.out.println(FileUtils.shortMd5("http://home.cnblogs.com/u/Dawn----123/"));
    }

    @Test
    @Ignore
    public void TestUrlShortMd5WithParser() {
        String url = "http://www.cnblogs.com";
        String pageStr = url + System.getProperty("line.separator") + "<html><head><title>hahah</title></head><body><h1>nihao</h1></body></html>";

        int urlIndex =  pageStr.indexOf(System.getProperty("line.separator"));
        String parentUrl = pageStr.substring(0, urlIndex);

        Assert.assertEquals(FileUtils.shortMd5(url), FileUtils.shortMd5(parentUrl));
    }

    @Test
    @Ignore
    public void testShortMd5Right() {
        Assert.assertEquals("157535e5", FileUtils.shortMd5("http://www.csdn.net/tag/%E4%BA%A7%E5%93%81/news"));
    }

    @Test
    @Ignore
    public void testCleanFile() {
        File f = new File("status/url-done.txt");
        FileUtils.cleanFile(f);
        Assert.assertEquals(0, f.length());
    }

    @Test
    public void testTimePrint() {
        System.out.println(new Time(1432522393).getTime());
    }

    @Test
    public void testStrMatch() {
        String s = "http://bbs.tianya.cn/post-travel-711574-1.shtml";
        Assert.assertEquals(s.matches("tianya"), true);
    }
}