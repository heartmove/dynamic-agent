package com.heartmove;

import java.net.URL;
import java.net.URLClassLoader;

/**
 * 自定义类加载器
 *
 * @author user
 * @date 2019/7/19 17:44
 * @version 1.0.0
 * @copyright wonhigh.cn
 */
public class MyClassLoader extends URLClassLoader {
	public MyClassLoader(URL[] urls) {
		super(urls, ClassLoader.getSystemClassLoader().getParent());
	}

	@Override
	protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
		final Class<?> loadedClass = findLoadedClass(name);
		if (loadedClass != null) {
			return loadedClass;
		}

		// 优先从parent（SystemClassLoader）里加载系统类，避免抛出ClassNotFoundException
		if (name != null && (name.startsWith("sun.") || name.startsWith("java."))) {
			return super.loadClass(name, resolve);
		}
		try {
			Class<?> aClass = findClass(name);
			if (resolve) {
				resolveClass(aClass);
			}
			return aClass;
		} catch (Exception e) {
			// ignore
		}
		return super.loadClass(name, resolve);
	}
}
