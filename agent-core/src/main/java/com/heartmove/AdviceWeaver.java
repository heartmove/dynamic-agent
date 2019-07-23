package com.heartmove;

import org.objectweb.asm.*;
import org.objectweb.asm.commons.AdviceAdapter;
import org.objectweb.asm.commons.JSRInlinerAdapter;

/**
 * TODO: 增加描述
 *
 * @author user
 * @date 2019/7/19 15:38
 * @version 1.0.0
 * @copyright wonhigh.cn
 */
public class AdviceWeaver extends ClassVisitor implements Opcodes {

	public AdviceWeaver(int api) {
		super(api);
	}

	public AdviceWeaver(int api, ClassVisitor classVisitor) {
		super(api, classVisitor);
		System.out.println("AdviceWeaver!");
	}

	@Override
	public MethodVisitor visitMethod(int access, final String name, String descriptor, String signature,
			String[] exceptions) {
		final MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);
		AdviceAdapter adviceAdapter = new AdviceAdapter(Opcodes.ASM5, new JSRInlinerAdapter(mv, access, name, descriptor, signature, exceptions), access, name, descriptor) {
			@Override
			protected void onMethodEnter() {
				mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
				mv.visitLdcInsn("methodEnter:" + name);
				mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);

				mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
				//加载参数数组到栈顶
				loadArgArray();
				mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/util/Arrays", "asList",
						"([Ljava/lang/Object;)Ljava/util/List;", false);
				mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/Object;)V", false);
			}

			@Override
			protected void onMethodExit(int opcode) {
				System.out.println(name + " onMethodExit");
				mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
				mv.visitLdcInsn("methodExit:" + name);
				mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
				//如果未出现异常，就输出返回值
				if(opcode != ATHROW){
					System.out.println(name + " onMethodExit");
					mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
					mv.visitLdcInsn("methodExit:" + name + ":");
					mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "print", "(Ljava/lang/String;)V", false);

					//将返回值从操作数栈中复制一份放在栈顶
					loadReturn(opcode);
					/*
						mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
						swap();
						mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
					*/
					mv.visitMethodInsn(Opcodes.INVOKESTATIC, "com/heartmove/AspectUtil", "beforeReturn",
							"(Ljava/lang/Object;)V", false);
				}
			}


			/**
			 * 加载返回值
			 * @param opcode 操作码
			 */
			private void loadReturn(int opcode) {
				switch (opcode) {

					case RETURN: {
						push((Type) null);
						break;
					}

					case ARETURN: {
						dup();
						break;
					}

					//对于基本数据类型都需要进行装箱操作
					//long以及double都是用两个slot来存储的，所以是dup2
					case LRETURN:
					case DRETURN: {
						dup2();
						box(Type.getReturnType(methodDesc));
						break;
					}

					default: {
						dup();
						box(Type.getReturnType(methodDesc));
						break;
					}

				}
			}
		};
		System.out.println(this.getClass() + ":visitMethod:adviceAdapter");
		return adviceAdapter;
	}
}
