package com.surelogic.sierra.gwt.client;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gwt.core.client.GWT;
import com.surelogic.sierra.gwt.client.util.LangUtil;

public class Context {
	private ContentComposite content;
	private final Map parameters = new HashMap();

	public static Context create(String context) {
		Context ctx = new Context();
		ctx.initContext(context);
		return ctx;
	}

	public static Context create(Context context, Map parameters) {
		Context ctx = new Context();
		ctx.initContext(context);
		ctx.initContext(parameters);
		return ctx;
	}

	private Context() {
		// use static create methods
	}

	public ContentComposite getContent() {
		return content;
	}

	public String getParameter(String name) {
		return (String) parameters.get(name);
	}

	public String toString() {
		final StringBuffer buf = new StringBuffer();
		if (content != null) {
			buf.append(ContentRegistry.getContentName(content));
		}
		if (!parameters.isEmpty()) {
			for (Iterator it = parameters.entrySet().iterator(); it.hasNext();) {
				Map.Entry parameter = (Entry) it.next();
				buf.append('/');
				buf.append(parameter.getKey());
				String paramValue = (String) parameter.getValue();
				if (paramValue != null) {
					buf.append('=');
					buf.append(paramValue);
				}
			}
		}
		return buf.toString();
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

	private void initContext(String context) {
		content = null;
		parameters.clear();

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

			if (argString != null && argString.length() != 0) {
				String[] argArray = argString.split("/");
				for (int i = 0; i < argArray.length; i++) {
					String[] argPair = argArray[i].split("\\=");
					if (argPair.length < 2) {
						parameters.put(argPair[0], null);
					} else {
						parameters.put(argPair[0], argPair[1]);
					}
				}
			}
		} else {
			contentName = null;
		}
		if (contentName != null) {
			content = ContentRegistry.getContent(contentName);
		}
	}

	private void initContext(Context context) {
		content = context.content;
		parameters.putAll(context.parameters);
	}

	private void initContext(Map parameters) {
		this.parameters.putAll(parameters);
	}

}
