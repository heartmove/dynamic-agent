package com.heartmove;

import org.objectweb.asm.ClassReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.instrument.ClassDefinition;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.List;
import java.util.Scanner;

/**
 * Hello world!
 *
 */
public class AppAgent {


    private static final MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
    private static final OperatingSystemMXBean operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();
    private static final List<GarbageCollectorMXBean> garbageCollectorMXBeanList =  ManagementFactory.getGarbageCollectorMXBeans();
    /**
     * 启动后的 vm.attach
     * @param args
     * @param inst
     * @throws Exception
     */
    public static void agentmain(String args, Instrumentation inst) throws Exception{
        System.out.println("agentmain args:" + args);

        String coreJarPath = "F:\\github_code\\dynamic-agent\\agent-core\\target\\agent-core-1.0.0-SNAPSHOT-jar-with-dependencies.jar";
        //防止与应用的jar冲突，通过自定义类加载器加载 agent-core.jar,并且不委派给父类加载器加载
        MyClassLoader myClassLoader = new MyClassLoader(new URL[]{new File(coreJarPath).toURI().toURL()});
        //加载 Enhancer.class
        Class<?> enhancerClass = myClassLoader.loadClass("com.heartmove.Enhancer");
        //构造 enhancer 实例
        ClassFileTransformer enhancer = (ClassFileTransformer)enhancerClass.getConstructor(Instrumentation.class).newInstance(inst);

        Class[] classes = inst.getAllLoadedClasses();

        while(true){
            System.out.println("--------------------------------------------------------------------------");
            String command = new Scanner(System.in).nextLine();

            if(null == command || command.trim().length() == 0){
                continue;
            }
            String[] commands = command.split("\\s+");
            if(command.equals("class")){
                for (Class clazz : classes){
                    System.out.println(clazz.getName());
                }
            }else if(command.equals("memory")){
                System.out.println("当前堆使用情况："+ memoryMXBean.getHeapMemoryUsage());
                System.out.println("当前非堆使用情况："+ memoryMXBean.getNonHeapMemoryUsage());
            }else if(command.equals("system")){
                System.out.println("名称："+operatingSystemMXBean.getName());
                System.out.println("版本："+operatingSystemMXBean.getVersion());
                System.out.println("处理器数："+operatingSystemMXBean.getAvailableProcessors());
                System.out.println("负载："+operatingSystemMXBean.getSystemLoadAverage());
            }else if(command.equals("gc")){
                for(GarbageCollectorMXBean mxBean : garbageCollectorMXBeanList){
                    System.out.println(String.format("名称：%s, 次数：%s, 耗时：%s", mxBean.getName(), mxBean.getCollectionCount(), mxBean.getCollectionTime()));
                }
            }else if(commands[0].equals("redefine") && commands.length == 2){
                //重定义class
                byte[] bytes = toByteArrayNIO(commands[1]);
                ClassReader classReader = new ClassReader(bytes);
                System.out.println(classReader.getClassName().replaceAll("/","."));
                ClassDefinition classDefinition = new ClassDefinition(Class.forName(classReader.getClassName().replaceAll("/",".")), bytes);
                inst.redefineClasses(classDefinition);
                //inst.retransformClasses();
            }else if(command.equals("unload")){
                //取消class增强
                Method method = enhancerClass.getMethod("resetTransformer");
                method.invoke(enhancer);
            }else if(command.equals("load")){
                //增强class
                Method retransformClassesMethod = enhancerClass.getMethod("retransformClasses");
                retransformClassesMethod.invoke(enhancer);
            }
        }
    }


    /**
     *
     * <p>Title: toByteArrayNIO</p>
     * <p>Description: NIO way</p>
     * @param filename
     * @return
     * @throws IOException
     */
    public static byte[] toByteArrayNIO(String filename) throws IOException {
        File file = new File(filename);
        if (!file.exists()) {
            throw new FileNotFoundException(filename);
        }
        FileChannel channel = null;
        FileInputStream fs = null;
        try {
            fs = new FileInputStream(file);
            channel = fs.getChannel();
            ByteBuffer byteBuffer = ByteBuffer.allocate((int) channel.size());
            while (channel.read(byteBuffer) > 0) {
                // do nothing
                // System.out.println("reading");
            }
            return byteBuffer.array();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            try {
                channel.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                fs.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 启动前的，--javaagent
     * @param args
     * @param inst
     * @throws Exception
     */
    public static void premain(String args, Instrumentation inst) throws Exception {
        System.out.println("Pre Args:" + args);
        Class[] classes = inst.getAllLoadedClasses();
        for (Class clazz : classes){
            System.out.println(clazz.getName());
        }
    }
}
