package com.surelogic.sierra.gwt.client;

import com.google.gwt.core.client.GWT;
import com.surelogic.sierra.gwt.client.util.LangUtil;

public class Context {
	private final ContentComposite content;
	private final String args;

	public static Context create(String context) {
		return new Context(context);
	}

	private Context(String context) {
		String contentName;
		if (context != null && (context.length() != 0)) {
			int split = context.indexOf('/');
			if (split != -1) {
				contentName = context.substring(0, split).toLowerCase();
				args = context.substring(split + 1);
			} else {
				contentName = context.toLowerCase();
				args = null;
			}
		} else {
			contentName = null;
			args = null;
		}
		if (contentName != null) {
			content = ContentRegistry.getContent(contentName);
		} else {
			content = null;
		}
	}

	public ContentComposite getContent() {
		return content;
	}

	public String getArgs() {
		return args;
	}

	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (obj == this) {
			return true;
		}
		if (GWT.getTypeName(obj).equals(GWT.getTypeName(this))) {
			Context objCtx = (Context) obj;
			return objCtx.content == this.content
					&& LangUtil.equals(objCtx.args, this.args);
		}
		return false;
	}

	public int hashCode() {
		int hash = 31;
		if (content != null) {
			hash += 31 * hash + content.hashCode();
		}
		if (args != null) {
			hash += 31 * hash + args.hashCode();
		}

		return hash;
	}

}
