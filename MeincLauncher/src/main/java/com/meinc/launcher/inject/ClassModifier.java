package com.meinc.launcher.inject;

import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public class ClassModifier extends ClassAdapter implements Opcodes{

  private String methodName;
  private int methodArgCount;

  public ClassModifier(ClassVisitor cv, String methodName, int methodArgCount) {
    super(cv);
    this.methodName = methodName;
    this.methodArgCount = methodArgCount;
  }

  @Override
  public MethodVisitor visitMethod(int access, String name, String desc,
      String signature, String[] exceptions) {
    MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
    
    if (methodName.equals(name)
        && methodArgCount == Type.getArgumentTypes(desc).length
        /*&& (access & ACC_PUBLIC) != 0*/
        /*&& (access & ACC_STATIC) != 0*/) {
      System.out.println("Entering method " + name + desc);
      return new MethodModifier(mv);
    }
    
    return mv;
  }
}
