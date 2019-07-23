package com.heartmove;

import org.objectweb.asm.ClassReader;

import java.io.*;
import java.lang.instrument.ClassDefinition;
import java.lang.instrument.Instrumentation;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.FileChannel;
import java.util.Arrays;
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
		//inst.addTransformer(new Enhancer(), true);
        Class[] classes = inst.getAllLoadedClasses();
        for (Class clazz : classes){
           if(clazz.getName().endsWith("Controller")){
               inst.retransformClasses(clazz);
               System.out.println("success transform:" + clazz.getName());
           }
        }

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
                byte[] bytes = toByteArrayNIO(commands[1]);
                ClassReader classReader = new ClassReader(bytes);
                System.out.println(classReader.getClassName().replaceAll("/","."));
                ClassDefinition classDefinition = new ClassDefinition(Class.forName(classReader.getClassName().replaceAll("/",".")), bytes);
                inst.redefineClasses(classDefinition);
                //inst.retransformClasses();
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
}
