import com.ibm.wala.shrikeCT.InvalidClassFileException;
import com.ibm.wala.util.CancelException;
import com.ibm.wala.util.WalaException;
import org.junit.Assert;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class TestSelectByMethod {
    //粒度
    String by = "-m";
    //文件路径，指向待测项目的target文件夹
    String[] projectTarget = {
            "E:\\大三上\\自动化测试\\代码作业-11.21ddl\\Cases\\0-CMD\\target",
            "E:\\大三上\\自动化测试\\代码作业-11.21ddl\\Cases\\1-ALU\\target",
            "E:\\大三上\\自动化测试\\代码作业-11.21ddl\\Cases\\2-DataLog\\target",
            "E:\\大三上\\自动化测试\\代码作业-11.21ddl\\Cases\\3-BinaryHeap\\target",
            "E:\\大三上\\自动化测试\\代码作业-11.21ddl\\Cases\\4-NextDay\\target",
            "E:\\大三上\\自动化测试\\代码作业-11.21ddl\\Cases\\5-MoreTriangle\\target"
    };
    //文件路径，指向记录了变更信息的文本文件
    String[] changeInfo = {
            "E:\\大三上\\自动化测试\\代码作业-11.21ddl\\Cases\\0-CMD\\data\\change_info.txt",
            "E:\\大三上\\自动化测试\\代码作业-11.21ddl\\Cases\\1-ALU\\data\\change_info.txt",
            "E:\\大三上\\自动化测试\\代码作业-11.21ddl\\Cases\\2-DataLog\\data\\change_info.txt",
            "E:\\大三上\\自动化测试\\代码作业-11.21ddl\\Cases\\3-BinaryHeap\\data\\change_info.txt",
            "E:\\大三上\\自动化测试\\代码作业-11.21ddl\\Cases\\4-NextDay\\data\\change_info.txt",
            "E:\\大三上\\自动化测试\\代码作业-11.21ddl\\Cases\\5-MoreTriangle\\data\\change_info.txt",
    };
    //按方法粒度选择结果
    String[] methodAns = {
            "E:\\大三上\\自动化测试\\代码作业-11.21ddl\\Cases\\0-CMD\\data\\selection-method.txt",
            "E:\\大三上\\自动化测试\\代码作业-11.21ddl\\Cases\\1-ALU\\data\\selection-method.txt",
            "E:\\大三上\\自动化测试\\代码作业-11.21ddl\\Cases\\2-DataLog\\data\\selection-method.txt",
            "E:\\大三上\\自动化测试\\代码作业-11.21ddl\\Cases\\3-BinaryHeap\\data\\selection-method.txt",
            "E:\\大三上\\自动化测试\\代码作业-11.21ddl\\Cases\\4-NextDay\\data\\selection-method.txt",
            "E:\\大三上\\自动化测试\\代码作业-11.21ddl\\Cases\\5-MoreTriangle\\data\\selection-method.txt",
    };

    @Test
    public void test0_CMDByMethod() throws WalaException, CancelException, InvalidClassFileException, IOException {
        int choose = 0;

        Selector selector = new Selector();
        selector.prepare(projectTarget[choose]);
        //selector.makeDotFile(by);
        selector.select(changeInfo[choose], by);

        System.out.println("expect:");
        ArrayList<String> ans = Support.trim(Support.readTxt(methodAns[choose]));
        Collections.sort(ans);
        for (String s : ans) {
            System.out.println(s);  //打印正确结果
        }
        System.out.println("---------------------------------------------------------------");
        System.out.println("actual:");
        for (String s : selector.selectResByMethod) {
            System.out.println(s);  //打印所得结果
        }
        Assert.assertEquals(ans.size(), selector.selectResByMethod.size());  //先判断个数
        for (int i = 0; i < ans.size(); i++) {
            Assert.assertEquals(ans.get(i), selector.selectResByMethod.get(i));  //expect, actual
        }
    }

    @Test
    public void test1_ALUByMethod() throws WalaException, CancelException, InvalidClassFileException, IOException {
        int choose = 1;

        Selector selector = new Selector();
        selector.prepare(projectTarget[choose]);
        //selector.makeDotFile(by);
        selector.select(changeInfo[choose], by);

        ArrayList<String> ans = Support.trim(Support.readTxt(methodAns[choose]));
        Collections.sort(ans);

        Support.printRes(ans, selector.selectResByMethod);  //打印

        Assert.assertEquals(ans.size(), selector.selectResByMethod.size());  //先判断个数
        for (int i = 0; i < ans.size(); i++) {
            Assert.assertEquals(ans.get(i), selector.selectResByMethod.get(i));  //expect, actual
        }
    }

    @Test
    public void test2_DataLogByMethod() throws WalaException, CancelException, InvalidClassFileException, IOException {
        int choose = 2;

        Selector selector = new Selector();
        selector.prepare(projectTarget[choose]);
        //selector.makeDotFile(by);
        selector.select(changeInfo[choose], by);

        ArrayList<String> ans = Support.trim(Support.readTxt(methodAns[choose]));
        Collections.sort(ans);

        Support.printRes(ans, selector.selectResByMethod);  //打印

        Assert.assertEquals(ans.size(), selector.selectResByMethod.size());  //先判断个数
        for (int i = 0; i < ans.size(); i++) {
            Assert.assertEquals(ans.get(i), selector.selectResByMethod.get(i));  //expect, actual
        }
    }

    @Test
    public void test3_BinaryHeapByMethod() throws WalaException, CancelException, InvalidClassFileException, IOException {
        int choose = 3;

        Selector selector = new Selector();
        selector.prepare(projectTarget[choose]);
        //selector.makeDotFile(by);
        selector.select(changeInfo[choose], by);

        ArrayList<String> ans = Support.trim(Support.readTxt(methodAns[choose]));
        Collections.sort(ans);

        Support.printRes(ans, selector.selectResByMethod);  //打印

        Assert.assertEquals(ans.size(), selector.selectResByMethod.size());  //先判断个数
        for (int i = 0; i < ans.size(); i++) {
            Assert.assertEquals(ans.get(i), selector.selectResByMethod.get(i));  //expect, actual
        }
    }

    @Test
    public void test4_NextDayByMethod() throws WalaException, CancelException, InvalidClassFileException, IOException {
        int choose = 4;

        Selector selector = new Selector();
        selector.prepare(projectTarget[choose]);
        //selector.makeDotFile(by);
        selector.select(changeInfo[choose], by);

        System.out.println("expect:");
        ArrayList<String> ans = Support.trim(Support.readTxt(methodAns[choose]));
        Collections.sort(ans);
        for (String s : ans) {
            System.out.println(s);  //打印正确结果
        }
        System.out.println("---------------------------------------------------------------");
        System.out.println("actual:");
        for (String s : selector.selectResByMethod) {
            System.out.println(s);  //打印所得结果
        }
        System.out.println("=========================================================================================");
        System.out.println("不同部分：");
        System.out.println("ans:");
        for (String s : ans) {
            if (!selector.selectResByMethod.contains(s)) {
                System.out.println(s);
            }
        }
        System.out.println("---------------------------------------------------------------");
        System.out.println("select:");
        for (String s : selector.selectResByMethod) {
            if (!ans.contains(s)) {
                System.out.println(s);
            }
        }

        Assert.assertEquals(ans.size(), selector.selectResByMethod.size());  //先判断个数
        for (int i = 0; i < ans.size(); i++) {
            Assert.assertEquals(ans.get(i), selector.selectResByMethod.get(i));  //expect, actual
        }
    }

    @Test
    public void test5_MoreTriangleByMethod() throws WalaException, CancelException, InvalidClassFileException, IOException {
        int choose = 5;

        Selector selector = new Selector();
        selector.prepare(projectTarget[choose]);
        //selector.makeDotFile(by);
        selector.select(changeInfo[choose], by);

        ArrayList<String> ans = Support.trim(Support.readTxt(methodAns[choose]));
        Collections.sort(ans);

        Support.printRes(ans, selector.selectResByMethod);  //打印

        Assert.assertEquals(ans.size(), selector.selectResByMethod.size());  //先判断个数
        for (int i = 0; i < ans.size(); i++) {
            Assert.assertEquals(ans.get(i), selector.selectResByMethod.get(i));  //expect, actual
        }
    }

}
