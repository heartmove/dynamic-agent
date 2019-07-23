package com.heartmove;

/**
 * TODO: 增加描述
 *
 * @author user
 * @date 2019/7/22 14:47
 * @version 1.0.0
 * @copyright wonhigh.cn
 */
public class AspectUtil {

	public static void beforeReturn(Object object){
		try {
			System.out.println(object);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	public static void test(){
		Object obj = new Object();
		System.out.println("test");

		AspectUtil.beforeReturn(obj);
	}
}
