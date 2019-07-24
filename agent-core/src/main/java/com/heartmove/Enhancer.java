package com.heartmove;

import org.apache.commons.io.FileUtils;
import org.objectweb.asm.*;
import org.springframework.cglib.transform.ClassTransformer;

import java.io.File;
import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.security.ProtectionDomain;

import static org.objectweb.asm.ClassReader.EXPAND_FRAMES;
import static org.objectweb.asm.ClassWriter.COMPUTE_FRAMES;
import static org.objectweb.asm.ClassWriter.COMPUTE_MAXS;

/**
 * 增强class
 *
 * @author user
 * @date 2019/7/19 14:40
 * @version 1.0.0
 * @copyright wonhigh.cn
 */
public class Enhancer implements ClassFileTransformer {

	Instrumentation inst;

	public Enhancer(Instrumentation inst){
		this.inst = inst;
	}

	public void retransformClasses() throws Exception{
		try {
			inst.addTransformer(this, true);
			Class[] classes = inst.getAllLoadedClasses();
			for (Class clazz : classes) {
				if (clazz.getName().endsWith("Controller") && clazz.getName().contains("belle")) {
					inst.retransformClasses(clazz);
					System.out.println("success transform:" + clazz.getName());
				}
			}
		} finally {
			inst.removeTransformer(this);
		}
	}


	@Override
	public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
			ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {

		try {
			if(!className.endsWith("Controller") || !className.contains("belle")){
				return classfileBuffer;
			}
			System.out.println(this.getClass() + ":transform:" + className);
			//先读取class
			ClassReader classReader = new ClassReader(classfileBuffer);
			//构建classWriter
			ClassWriter classWriter = new ClassWriter(classReader, COMPUTE_FRAMES | COMPUTE_MAXS) {

				/*
				 * 注意，为了自动计算帧的大小，有时必须计算两个类共同的父类。
				 * 缺省情况下，ClassWriter将会在getCommonSuperClass方法中计算这些，通过在加载这两个类进入虚拟机时，使用反射API来计算。
				 * 但是，如果你将要生成的几个类相互之间引用，这将会带来问题，因为引用的类可能还不存在。
				 * 在这种情况下，你可以重写getCommonSuperClass方法来解决这个问题。
				 *
				 * 通过重写 getCommonSuperClass() 方法，更正获取ClassLoader的方式，改成使用指定ClassLoader的方式进行。
				 * 规避了原有代码采用Object.class.getClassLoader()的方式
				 */
				@Override
				protected String getCommonSuperClass(String type1, String type2) {
					Class<?> c, d;
					final ClassLoader classLoader = Object.class.getClassLoader();
					try {
						c = Class.forName(type1.replace('/', '.'), false, classLoader);
						d = Class.forName(type2.replace('/', '.'), false, classLoader);
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
					if (c.isAssignableFrom(d)) {
						return type1;
					}
					if (d.isAssignableFrom(c)) {
						return type2;
					}
					if (c.isInterface() || d.isInterface()) {
						return "java/lang/Object";
					} else {
						do {
							c = c.getSuperclass();
						} while (!c.isAssignableFrom(d));
						return c.getName().replace('.', '/');
					}
				}

			};
			AdviceWeaver adviceWeaver = new AdviceWeaver(Opcodes.ASM5, classWriter);
			classReader.accept(adviceWeaver, EXPAND_FRAMES);
			byte[] bytes = classWriter.toByteArray();
			try {
				String[] classNames = className.split("\\.");
				FileUtils.writeByteArrayToFile(new File("F:\\git_code\\dynamic-agent\\agent-core\\target\\classes2\\"+classNames[classNames.length-1]+".class"), bytes);
			} catch (IOException e) {
				e.printStackTrace();
			}
			return bytes;
		} catch (ClassTooLargeException e) {
			e.printStackTrace();
		} catch (MethodTooLargeException e) {
			e.printStackTrace();
		}
		return null;
	}


	public void resetTransformer() throws Exception{
		ClassFileTransformer resetClassTransformer = new ClassFileTransformer() {
			@Override
			public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
					ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
				if(!className.endsWith("Controller") || !className.contains("belle")){
					return classfileBuffer;
				}
				return null;
			}
		};

		inst.addTransformer(resetClassTransformer, true);

		Class[] classes = inst.getAllLoadedClasses();
		for (Class clazz : classes){
			if(clazz.getName().endsWith("Controller") && clazz.getName().contains("belle")){
				inst.retransformClasses(clazz);
			}
		}
		inst.removeTransformer(resetClassTransformer);
	}
}
