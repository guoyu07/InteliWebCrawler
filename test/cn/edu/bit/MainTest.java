package cn.edu.bit;

import org.junit.Assert;
import org.junit.Test;

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
}