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

        for (String anUrl : urlSet) {
            // System.out.println("seed: " + anUrl);
            FetchPageThread fpt = new FetchPageThread(anUrl);
            new Thread(fpt).start();
        }
    }

    public synchronized static void addFetchedUrl(String urlHash) {
        urlFetched.add(urlHash);
    }
}
