package cn.edu.bit;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashSet;

public class Main {


    public static int pageSize = 0;
    public final static int THREAD_SIZE = 100;
    public static int currentThreadNum = 0;

    public static HashSet<String> urlFetched = new HashSet<String>();

    public static void main(String[] args) throws IOException {

        Calendar cal = Calendar.getInstance();
        System.out.println("Start at :: " + cal.getTime());

        // write your code here
        final String seedFile = "conf" + File.separator + "seeds.conf";
        HashSet<String> urlSet = FileParser.parseSeedUrls(seedFile);

        if (urlSet == null) return;

        // String url = "www.baidu.com";
        // URL url = new URL("http://www.baidu.com");
        // HttpURLConnection conn = (HttpURLConnection)url.openConnection();
        // if (conn == null) {
        //     throw new IllegalArgumentException("url protocol must be http");
        // }
//
        // // set connect and read timeout both to just 1 second
        // conn.setConnectTimeout(5000);
        // conn.setReadTimeout(10000);
        // conn.setInstanceFollowRedirects(true);
        // conn.setRequestProperty("User-agent", "Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2272.101 Safari/537.36");
//
        // // send request
        // conn.connect();
        // InputStream is = (InputStream) conn.getContent();
        // Scanner sc = new Scanner(is);
        // while (sc.hasNextLine()) {
        //     System.out.println(sc.nextLine());
        // }
        for (String anUrl : urlSet) {
            // System.out.println("seed: " + anUrl);
            FetchPageThread fpt = new FetchPageThread(anUrl);
            new Thread(fpt).start();
        }
    }
}
