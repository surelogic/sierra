package com.surelogic.sierra.tool;

import com.surelogic.common.jobs.remote.RemoteSLJobException;

public class ToolException extends RemoteSLJobException {

  private static final long serialVersionUID = -3979834328613977446L;

  ToolException(String label, final int number, Object... args) {
    super(label, number, args);
  }

  ToolException(String label, final int number, Throwable t) {
    super(label, number, t);
  }
}
