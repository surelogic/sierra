package com.surelogic.sierra.message.srpc;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

class MethodInvocation {

  private final Method m;

  private final Object[] args;

  public MethodInvocation(final Method m, final Object[] args) {
    this.m = m;
    this.args = args;
  }

  public Method getMethod() {
    return m;
  }

  public Object[] getArgs() {
    return args;
  }

  public Object invoke(final Object target) throws InvocationTargetException {
    try {
      return m.invoke(target, args);
    } catch (final InvocationTargetException e) {
      throw e;
    } catch (final Exception e) {
      throw new SRPCException(e);
    }
  }

  @Override
  public String toString() {
    return m.getDeclaringClass().getSimpleName() + "." + m.getName();
  }
}
