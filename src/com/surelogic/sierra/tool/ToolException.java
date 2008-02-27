package com.surelogic.sierra.tool;

import com.surelogic.common.i18n.I18N;

public class ToolException extends RuntimeException {
  private static final long serialVersionUID = 1L;
  private final int number;
  private final Object[] args;
  
  ToolException(final int number, Object... args) {
    this.number = number;
    this.args = args;
  }
  
  ToolException(final int number, Throwable t) {
    super(t);
    this.number = number;
    this.args = null; // FIX
  }
  
  public int getErrorNum() {
    return number;
  }

  public String getToolMessage(String name) {
    if (args == null || args.length == 0) {
      return I18N.err(number, name);
    }
    Object[] args2 = new Object[args.length+1];
    args2[0] = name;
    System.arraycopy(args, 0, args2, 1, args.length);
    return I18N.err(number, args2);
  }
}
