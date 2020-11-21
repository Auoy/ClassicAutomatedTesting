import com.ibm.wala.classLoader.CallSiteReference;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.classLoader.ShrikeBTMethod;
import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.Entrypoint;
import com.ibm.wala.ipa.callgraph.cha.CHACallGraph;
import com.ibm.wala.ipa.callgraph.impl.AllApplicationEntrypoints;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.ipa.cha.ClassHierarchyFactory;
import com.ibm.wala.shrikeCT.InvalidClassFileException;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.util.CancelException;
import com.ibm.wala.util.WalaException;
import com.ibm.wala.util.config.AnalysisScopeReader;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

class Selector {

    private static class Class {
        String name;
        ArrayList<Class> pres = new ArrayList<>();  //前驱，即调用该类中方法的方法所在的类
        ArrayList<String> declaredMtds = new ArrayList<>();  //存该类和该类的父类中声明的所有方法的签名

        Class(String name) {
            this.name = name;
        }
    }

    private static class Method {
        String belong;
        String signature;
        ArrayList<Method> callers = new ArrayList<>();  //该方法的调用者，调用该方法的方法

        Method(String belong, String signature) {
            this.belong = belong;
            this.signature = signature;
        }
    }

    ArrayList<Class> AllClasses = new ArrayList<>();  //存所有类
    ArrayList<Method> AllMethods = new ArrayList<>();  //存所有方法
    ArrayList<String> selectResByClass = new ArrayList<>();  //按类选择结果
    ArrayList<String> selectResByMethod = new ArrayList<>();  //按方法选择结果

    /**
     * 读取分析target文件夹下所有.class文件，收集各类中各方法信息
     *
     * @param projectTarget 文件路径，指向待测项目的target文件夹
     */
    void prepare(String projectTarget) throws InvalidClassFileException, IOException, WalaException, CancelException {

        String scopePath = "scope.txt";
        String exPath = "exclusion.txt";

        ClassLoader classLoader = Selector.class.getClassLoader();
        AnalysisScope scope = AnalysisScopeReader.readJavaScope(scopePath, new File(exPath), classLoader);

        //测试代码.class文件所在文件夹路径
        String testClassesPath = projectTarget + "\\test-classes\\net\\mooctest";
        File testDir = new File(testClassesPath);
        File[] testClasses = testDir.listFiles();
        //把target文件夹下的测试代码的.class文件加进scope
        if (testClasses != null) {
            for (File test : testClasses) {
                scope.addClassFileToScope(ClassLoaderReference.Application, test);
            }
        }

        //生产代码.class文件所在文件夹路径
        String codeClassesPath = projectTarget + "\\classes\\net\\mooctest";
        File codeDir = new File(codeClassesPath);
        File[] codeClasses = codeDir.listFiles();
        //把target文件夹下的生产代码的.class文件加进scope
        if (codeClasses != null) {
            for (File code : codeClasses) {
                scope.addClassFileToScope(ClassLoaderReference.Application, code);
            }
        }

        //1.生成类层次关系对象
        ClassHierarchy cha = ClassHierarchyFactory.makeWithRoot(scope);
        //2.生成进入点
        Iterable<Entrypoint> eps = new AllApplicationEntrypoints(scope, cha);
        //3.利用CHA算法构建调用图
        CHACallGraph cg = new CHACallGraph(cha);
        cg.init(eps);
        //4.遍历cg中所有的节点
        for (CGNode node : cg) {
            //node中包含了很多信息，包括类加载器、方法信息等，这里只筛选出需要的信息
            if (node.getMethod() instanceof ShrikeBTMethod) {
                //node.getMethod()返回一个比较泛化的IMethod实例，不能获取到我们想要的信息
                //一般地，本项目中所有业务逻辑相关的方法都是ShrikeBTMethod对象
                ShrikeBTMethod method = (ShrikeBTMethod) node.getMethod();
                //获取该类和该类的父类中声明的全部方法
                Collection<? extends IMethod> imethods = method.getDeclaringClass().getAllMethods();
                //使用Primordial类加载器加载的类都属于Java原生类，我们一般不关心
                if ("Application".equals(method.getDeclaringClass().getClassLoader().toString())) {

                    //获取声明该方法的类的内部表示
                    String classInnerName = method.getDeclaringClass().getName().toString();
                    //获取方法签名
                    String signature = method.getSignature();
                    //向方法集合和类集合中添加该方法和其所属类
                    if (!classInnerName.contains("$")) {
                        if (classInnerName.startsWith("Lnet/mooctest/") && findThisClass(classInnerName) == -1) {
                            AllClasses.add(new Class(classInnerName));
                        }
                        if (signature.startsWith("net.mooctest.") && findThisMethod(signature) == -1) {
                            AllMethods.add(new Method(classInnerName, signature));
                        }
                        Class c = AllClasses.get(findThisClass(classInnerName));
                        c.declaredMtds = removeOverrided(c.name, imethods);
                    }
                    //获取该方法所调用的方法，并添加
                    Collection<CallSiteReference> callSites = method.getCallSites();
                    for (CallSiteReference csr : callSites) {
                        String clsInNm = csr.getDeclaredTarget().getDeclaringClass().getName().toString();
                        String sig = csr.getDeclaredTarget().getSignature();

                        if (!clsInNm.contains("$")) {
                            if (sig.startsWith("net.mooctest.") && findThisMethod(sig) == -1) {
                                AllMethods.add(new Method(clsInNm, sig));
                                if (clsInNm.startsWith("Lnet/mooctest/") && findThisClass(clsInNm) == -1) {
                                    AllClasses.add(new Class(clsInNm));
                                }
                            }
                        }

                        //添加调用关系
                        if (findThisMethod(sig) != -1 && findThisMethod(signature) != -1) {
                            Method caller = AllMethods.get(findThisMethod(signature));  //调用者
                            Method callee = AllMethods.get(findThisMethod(sig));  //被调用者
                            if (!callee.callers.contains(caller)) {
                                callee.callers.add(caller);
                            }
                        }

                        if (findThisClass(classInnerName) != -1 && findThisClass(clsInNm) != -1) {
                            Class pre = AllClasses.get(findThisClass(classInnerName));  //调用者
                            Class cur = AllClasses.get(findThisClass(clsInNm));  //被调用者
                            if (!cur.pres.contains(pre)) {
                                cur.pres.add(pre);
                            }
                        }

                    }
                }
            }
        }
    }

    /**
     * 生成并输出.dot文件
     *
     * @throws IOException
     */
    void makeDotFile() throws IOException {
        //生成.doc文件内容
        StringBuilder content = new StringBuilder();

        //粒度：方法级
        content.append("digraph cmd_method {\n");
        for (Method callee : AllMethods) {
            for (Method caller : callee.callers) {
                content.append("\t\"").append(callee.signature).append("\" -> \"").append(caller.signature).append("\";\n");
            }
        }
        content.append("}\n");
        //输出为.doc文件
        String docName;
        docName = "method.doc";
        BufferedWriter out = new BufferedWriter(new FileWriter(docName));
        out.write(content.toString());
        out.close();

        content = new StringBuilder();
        //粒度：类级
        content.append("digraph cmd_class {\n");
        for (Class cur : AllClasses) {
            for (Class pre : cur.pres) {
                content.append("\t\"").append(cur.name).append("\" -> \"").append(pre.name).append("\";\n");
            }
        }
        content.append("}\n");
        //输出为.doc文件
        docName = "class.doc";
        out = new BufferedWriter(new FileWriter(docName));
        out.write(content.toString());
        out.close();
    }


    /**
     * 根据变更信息输出测试用例选择结果
     *
     * @param changeInfo 文件路径，指向记录了变更信息的文本文件
     * @param by         粒度，-c为类级，-m为方法级
     */
    void select(String changeInfo, String by) {
        ArrayList<String> info = readChangeInfoTxt(changeInfo);  //读取到的变更信息

        for (String item : info) {
            String clsName = item.split(" ")[0];
            String mtdSig = item.split(" ")[1];

            //观察发现，按方法粒度选择结果是按类粒度选择结果的子集
            selectByMethod(mtdSig);  //按方法粒度选一遍

            for (Class c : AllClasses) {
                if (c.declaredMtds.contains(mtdSig) && !c.name.equals(clsName)) {
                    String subClsName = getClsName(c.name);
                    String superClsName = getClsName(clsName);
                    String subCall = mtdSig.replace(superClsName, subClsName);
                    selectByMethod(subCall);
                }
            }

            if (by.equals("-c")) {
                for (String s : selectResByMethod) {  //同步选择结果
                    if (!selectResByClass.contains(s)) {
                        selectResByClass.add(s);
                    }
                }
                selectByClass(clsName);  //若按类粒度，则只需在之前结果基础上进行补充
            }
        }
        Collections.sort(selectResByMethod);
        Collections.sort(selectResByClass);
    }

    private void selectByMethod(String sig) {
        if (findThisMethod(sig) != -1) {
            Method mtd = AllMethods.get(findThisMethod(sig));
            for (Method caller : mtd.callers) {
                String tmp = caller.belong + " " + caller.signature;
                if (caller.belong.contains("Test") && !caller.signature.contains("<init>") && !selectResByMethod.contains(tmp)) {
                    selectResByMethod.add(tmp);
                } else {
                    if (!caller.signature.equals(sig)) {  //避免因被分析方法有递归依赖情况而导致此处无限递归栈溢出
                        selectByMethod(caller.signature);
                    }
                }
            }
        }
    }

    private void selectByClass(String clsName) {
        Class cls = AllClasses.get(findThisClass(clsName));
        for (Class pre : cls.pres) {
            if (pre.name.contains("Test")) {  //如果是测试类就继续
                for (Method mtd : AllMethods) {
                    if (mtd.belong.equals(pre.name) && !mtd.signature.contains("init")) {  //遍历该测试类中的方法
                        String tmp = mtd.belong + " " + mtd.signature;
                        if (!selectResByClass.contains(tmp)) {
                            selectResByClass.add(tmp);
                        }
                    }
                }
            } else {
                if (!pre.name.equals(clsName) && !pre.pres.contains(AllClasses.get(findThisClass(clsName)))) {
                    //该if判断为了防止类自己是自己pre的情况，和两个类互相为pre的情况
                    selectByClass(pre.name);
                }
            }
        }
    }

    /**
     * 输出选择结果 txt文件
     */
    void makeSelectFile(String by) throws FileNotFoundException {
        StringBuilder res = new StringBuilder();
        String txtName;
        if (by.equals("-m")) {
            txtName = "selection-method.txt";
            for (String s : selectResByMethod) {
                res.append(s).append("\n");
            }
        } else {
            txtName = "selection-class.txt";
            for (String s : selectResByClass) {
                res.append(s).append("\n");
            }
        }
        File fp = new File(txtName);
        PrintWriter pfp = new PrintWriter(fp);
        pfp.print(res.toString());
        pfp.close();
    }

    /**
     * 读变更信息的txt文件
     *
     * @param path txt文件路径
     * @return 各行内容
     */
    private ArrayList<String> readChangeInfoTxt(String path) {
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

    /**
     * 在类集合AllClasses中找到指定类
     *
     * @param name 类声明
     * @return index，不存在则为-1
     */
    private int findThisClass(String name) {
        for (int i = 0; i < AllClasses.size(); i++) {
            if (AllClasses.get(i).name.equals(name)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * 在方法集合AllMethods中找到指定类
     *
     * @param sig 方法声明
     * @return index，不存在则为-1
     */
    private int findThisMethod(String sig) {
        for (int i = 0; i < AllMethods.size(); i++) {
            if (AllMethods.get(i).signature.equals(sig)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * 加入本类中和其父类中声明的方法的签名，不包括已经被重写的父类方法版本签名
     *
     * @param curClassName
     * @param mtds
     * @return
     */
    private ArrayList<String> removeOverrided(String curClassName, Collection<? extends IMethod> mtds) {
        ArrayList<String> res = new ArrayList<>();
        for (IMethod m : mtds) {
            if (m.getSignature().startsWith("net.mooctest.")) {
                if (m.getDeclaringClass().getName().toString().equals(curClassName)) {
                    res.add(m.getSignature());
                } else {
                    String name = getMtdName(m.getDeclaringClass().getName().toString(), m.getSignature());
                    boolean has = false;
                    for (String tmp : res) {
                        if (tmp.contains(name)) {
                            has = true;
                            break;
                        }
                    }
                    if (!has) {
                        res.add(m.getSignature());
                    }
                }
            }
        }
        return res;
    }

    //从类的声明里获得类名
    private String getClsName(String classInnerName) {
        String cPrefix = "Lnet/mooctest/";
        return classInnerName.substring(cPrefix.length());
    }

    //从方法签名里获得方法名
    private String getMtdName(String classInnerName, String methodSig) {
        String mPrifix = "net.mooctest.";
        String cName = getClsName(classInnerName);
        return methodSig.substring(mPrifix.length() + cName.length());
    }

}
