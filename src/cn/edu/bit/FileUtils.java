package cn.edu.bit;


import sun.misc.resources.Messages_es;

import java.io.*;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Scanner;

/**
 * utils for file operation
 * save to file for testing @now
 * Created by wuxu92 on 3/25/2015.
 */
public class FileUtils {

    // public final static MessageDigest md5Digest = getMd();
    static Calendar cal = Calendar.getInstance();
    private Path file;
    final static String BASE_DIR = "pages";
    private String content;
    private String name;

    // private Scanner scanner;
    private PrintWriter writer;

    /**
     * constructor use file only
     * @param fileName String
     */
    public FileUtils(String fileName) throws IOException {

        if (fileName.equals("")) return;
        this.name = fileName;

        String baseDirStr = BASE_DIR + File.separator +cal.get(Calendar.YEAR) + "_" + cal.get(Calendar.MONTH) + "_"
                + cal.get(Calendar.DAY_OF_MONTH);
        Path baseDir = Paths.get(baseDirStr);
        if (Files.notExists(baseDir)) {
            Files.createDirectories(baseDir);
        }
        Path pathWithName = Paths.get(baseDirStr, this.name);
        if (Files.notExists(pathWithName)) {
            Files.createFile(pathWithName);
        }
        writer = new PrintWriter(new FileWriter(pathWithName.toFile()));
    }

    // constructor create directory if not exist
    public FileUtils(String content, String fileName) throws IOException {
        this(fileName);

        this.content = content;
        //this.file = Files.createFile(Paths.get(baseDirStr, this.name));

    }
    
    public static MessageDigest getMd() {
        try {
            return  MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            System.out.println("init messageDigest error");
            return null;
        }
    }

    /**
     * blank constructor
     */
    public FileUtils() {

    }

    public int saveToFile() {
        writer.println(this.content);
        writer.flush();
        return 1;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int saveStrToFile(String str) {
        writer.println(str);
        writer.flush();
        return 1;
    }

    public void setName(String name)  {
        this.name = name;
        String baseDirStr = BASE_DIR + File.separator +cal.get(Calendar.YEAR) + "_" + cal.get(Calendar.MONTH) + "_"
                + cal.get(Calendar.DAY_OF_MONTH);
        Path baseDir = Paths.get(baseDirStr);
        if (Files.notExists(baseDir)) {
            try {
                Files.createDirectories(baseDir);
            } catch (IOException e) {
                Main.mainLogger.info("create dir:" + baseDirStr + " already exist");
            }
        }
        Path pathWithName = Paths.get(baseDirStr, this.name);
        
        // create file if not exist
        if (Files.notExists(pathWithName)) {
            try {
                Files.createFile(pathWithName);
            } catch (IOException e) {
                Main.mainLogger.info("File " + pathWithName + " already exist");
            }
        }
        // writer.close();
        try {
            writer = new PrintWriter(new FileWriter(pathWithName.toFile()));
        } catch (IOException e) {
            Main.mainLogger.info("new printWriter exception");
        }
    }

    /**
     * get string s's md5 hashcode
     * @param s String to be hashed
     * @return String s's md5 code, "" if failed
     */
    public static String md5(String s) {
        byte[] digbytes;
        String hashStr;
        try {
            digbytes = MessageDigest.getInstance("MD5").digest(s.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            return "1111111111";
        } catch (NoSuchAlgorithmException e) {
            return "1111111111";
        } finally {
            System.out.println(s + "md5 fialed");
        }
        StringBuilder fileName = new StringBuilder();
        for (byte b : digbytes) {
            fileName.append(String.format("%02x", b & 0xff));
        }
        hashStr = fileName.toString();
        return hashStr;
    }

    /**
     * return the string s's md5 value's first 8 characters
     * @param s string to calculate
     * @return md5 string's first 8 chars
     */
    public static String shortMd5(String s) {
        return md5(s).substring(0, 12);
    }

    /**
     * get a formatted date string,
     * @return String
     */
    public static String nowTime() {
        return new SimpleDateFormat("HH-mm-ss_SSS").format(new Date());
    }

    /**
     * clean a file, delete all its content
     * use RandomAccessFile.setLength(0)
     * @param f file to be cleaned
     */
    public static void cleanFile(File f) {
        try {
            RandomAccessFile raf = new RandomAccessFile(f, "rw");
            raf.setLength(0);
            raf.close();
        } catch (FileNotFoundException e) {
            System.out.println("Cannot clean: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("clean file error:" + e.getMessage());
        }
    }

    /**
     * close the writer instance
     */
    public void close() {
        if (this.writer != null) this.writer.close();
    }

}
