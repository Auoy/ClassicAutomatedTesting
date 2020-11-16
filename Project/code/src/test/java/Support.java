import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;

public class Support {
    public static ArrayList<String> readTxt(String path) {
        File file = new File(path);
        BufferedReader reader = null;
        String temp;
        ArrayList<String> res = new ArrayList<>();
        try {
            reader = new BufferedReader(new FileReader(file));
            while ((temp = reader.readLine()) != null) {
                res.add(temp);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return res;
    }

    public static ArrayList<String> trim(ArrayList<String> arr) {
        ArrayList<String> res = new ArrayList<>();
        for (String s : arr) {
            if (s.length() != 0) {
                res.add(s);
            }
        }
        return res;
    }

    public static void printRes(ArrayList<String> expect, ArrayList<String> actual) {
        int i = 0;
        System.out.println("expect:");
        for (String s : expect) {
            System.out.println(i++ + ": " + s);  //打印正确结果
        }
        System.out.println("---------------------------------------------------------------");
        i = 0;
        System.out.println("actual:");
        for (String s : actual) {
            System.out.println(i++ + ": " + s);  //打印所得结果
        }
    }
}
