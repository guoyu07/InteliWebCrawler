package cn.edu.bit;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Scanner;

/**
 * class file parser
 * provides static functions for file content parse
 * e.g. config file parse
 * Created by wuxu92 on 3/26/2015.
 */
public class FileParser {

    public static HashSet<String> parseSeedUrls(String urlFileName) {
        String fileName = urlFileName;
        System.out.println("conf file name:" + fileName);
        
        // check if file exist
        if (Files.notExists(Paths.get(fileName))) {
            System.out.println("Seeds file given by filename not exist");
            throw new IllegalArgumentException("file :" + fileName +" not exist");
        }
        Scanner scanner = null;
        try {
            scanner = new Scanner(new File(fileName));
        } catch (FileNotFoundException e) {
            System.out.println("file not exist");
            return null;
        }
        HashSet<String> urlSet = new HashSet<String>();

        while (scanner.hasNextLine()) {
            String s = scanner.nextLine();
            // System.out.println(s);
            // check string is in a url format
            try {
                new URL(s);
            } catch (MalformedURLException e) {
                System.out.println(s + " is not an url format");
                continue;
            }
            urlSet.add(s);
        }

        return urlSet;
    }

    public static String[] parseFileByLine(String fileName) {
        if (Files.notExists(Paths.get(fileName))) {
            System.out.println("file [ " + fileName + " ] is not found");
            throw new IllegalArgumentException("file:" + fileName + " not exist");
        }

        HashSet<String> result = new HashSet<String>();

        try {
            BufferedReader br = new BufferedReader(new FileReader(fileName));
            String line;
            while ((line = br.readLine()) != null) {
                result.add(line);
            }
        } catch (FileNotFoundException e) {
            // do nothing
        } catch (IOException e) {
            // do nothing
        }
        String[] rs = new String[result.size()];
        result.toArray(rs);
        return rs;
    }
}
