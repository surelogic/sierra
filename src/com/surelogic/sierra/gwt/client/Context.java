package com.surelogic.sierra.gwt.client;

public class Context {
	private final String content;
	private final String args;

	private Context(String context) {
		if (context != null && (context.length() != 0)) {
			int split = context.indexOf('/');
			if (split != -1) {
				content = context.substring(0, split).toLowerCase();
				args = context.substring(split + 1);
			} else {
				content = context.toLowerCase();
				args = null;
			}
		} else {
			content = null;
			args = null;
		}
	}

	public String getContent() {
		return content;
	}

	public String getArgs() {
		return args;
	}

	public static Context create(String context) {
		return new Context(context);
	}

}
