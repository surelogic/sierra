package com.surelogic.sierra.gwt.client;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.surelogic.sierra.gwt.client.util.LangUtil;

public class Context {
	private final ContentComposite content;
	private final Map parameters = new HashMap();

	public static Context create(String context) {
		return new Context(context);
	}

	private Context(String context) {
		String contentName;
		if (context != null && (context.length() != 0)) {
			int split = context.indexOf('/');
			String argString;
			if (split != -1) {
				contentName = context.substring(0, split).toLowerCase();
				argString = context.substring(split + 1);
			} else {
				contentName = context.toLowerCase();
				argString = null;
			}

			parameters.clear();
			if (argString != null && argString.length() != 0) {
				String[] argArray = argString.split("/");
				for (int i = 0; i < argArray.length; i++) {
					String[] argPair = argArray[i].split("\\+");
					if (argPair.length < 2) {
						parameters.put(argPair[0], null);
					} else {
						parameters.put(argPair[0], argPair[1]);
					}
				}
			}
		} else {
			contentName = null;
			parameters.clear();
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

	public String getParameter(String name) {
		return (String) parameters.get(name);
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
					&& LangUtil.equals(objCtx.parameters, this.parameters);
		}
		return false;
	}

	public int hashCode() {
		int hash = 31;
		if (content != null) {
			hash += 31 * hash + content.hashCode();
		}
		if (parameters != null) {
			hash += 31 * hash + parameters.hashCode();
		}

		return hash;
	}

}
