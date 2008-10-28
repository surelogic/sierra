package com.surelogic.sierra.tool;

import com.surelogic.common.jobs.remote.JobException;

public class ToolException extends JobException {  
  private static final long serialVersionUID = 1L;

  ToolException(final int number, Object... args) {
	super(number, args);
  }
  
  ToolException(final int number, Throwable t) {
    super(number, t);
  }
}
