package com.meinc.launcher.inject;

import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class MethodModifier extends MethodAdapter implements Opcodes {
  private static boolean injected;
  
  public MethodModifier(MethodVisitor mv) {
    super(mv);
  }

  @Override
  public void visitCode() {
    System.out.println("Injecting bytecode");
    mv.visitMethodInsn(INVOKESTATIC, "com/meinc/launcher/serverprops/ServerPropertyHolder", "startServerPropertyMonitor", "()V");
    injected = true;
    super.visitCode();
  }

  public static boolean wasInjected() {
    return injected;
  }
}
