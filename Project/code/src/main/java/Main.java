import com.ibm.wala.shrikeCT.InvalidClassFileException;
import com.ibm.wala.util.CancelException;
import com.ibm.wala.util.WalaException;

import java.io.*;

class Main {

    public static void main(String[] args) throws CancelException, WalaException, InvalidClassFileException, IOException {
//        String[] cases = {"0-CMD", "1-ALU", "2-DataLog", "3-BinaryHeap", "4-NextDay", "5-MoreTriangle"};
//        int choose = 1;
//
//        //粒度
//        String by = "-m";
//        //文件路径，指向待测项目的target文件夹
//        String projectTarget = "E:\\大三上\\自动化测试\\代码作业-11.21ddl\\Cases\\" + cases[choose] + "\\target";
//        //文件路径，指向记录了变更信息的文本文件
//        String changeInfo = "E:\\大三上\\自动化测试\\代码作业-11.21ddl\\Cases\\" + cases[choose] + "\\data\\change_info.txt";
//        //按方法粒度选择结果
//        String methodAns = "E:\\大三上\\自动化测试\\代码作业-11.21ddl\\Cases\\" + cases[choose] + "\\data\\selection-method.txt";

        String by = args[0];
        String projectTarget = args[1];
        String changeInfo = args[2];

        Selector selector = new Selector();
        selector.prepare(projectTarget);
        selector.select(changeInfo, by);
//        selector.makeDotFile();
        selector.makeSelectFile(by);
        System.out.println(by);
        System.out.println(projectTarget);
        System.out.println(changeInfo);
    }

}
