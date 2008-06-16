package com.surelogic.sierra.gwt.client;

import java.util.HashMap;
import java.util.Map;

import com.surelogic.sierra.gwt.client.data.Cacheable;
import com.surelogic.sierra.gwt.client.util.LangUtil;

public final class Context {
	private static final String PARAM_UUID = "uuid";

	private ContentComposite content;
	private final Map<String, String> parameters = new HashMap<String, String>();

	public static Context create(String context) {
		Context ctx = new Context();
		ctx.initContext(context);
		return ctx;
	}

	public static Context create(Context baseContext,
			Map<String, String> parameters) {
		Context newContext = new Context();
		if (baseContext == null) {
			newContext.initContext(ContextManager.getContext());
		} else {
			newContext.initContext(baseContext);
		}
		newContext.initContext(parameters);
		return newContext;
	}

	public static Context create(ContentComposite content,
			Map<String, String> parameters) {
		Context newContext = new Context();
		newContext.initContext(content);
		newContext.initContext(parameters);
		return newContext;
	}

	public static Context createWithUuid(String uuid) {
		Map<String, String> params = new HashMap<String, String>();
		params.put(PARAM_UUID, uuid);
		return Context.create(ContextManager.getContext(), params);
	}

	public static Context createWithUuid(Cacheable item) {
		return createWithUuid(item.getUuid());
	}

	public static Context createWithUuid(ContentComposite content, String uuid) {
		Map<String, String> params = new HashMap<String, String>();
		params.put(PARAM_UUID, uuid);
		return Context.create(content, params);
	}

	public static Context createWithUuid(ContentComposite content,
			Cacheable item) {
		return createWithUuid(content, item.getUuid());
	}

	private Context() {
		// use static create methods
	}

	public void submit() {
		ContextManager.setContext(this);
	}

	public ContentComposite getContent() {
		return content;
	}

	public String getUuid() {
		return getParameter(PARAM_UUID);
	}

	public String getParameter(String name) {
		return parameters.get(name);
	}

	@Override
	public String toString() {
		final StringBuffer buf = new StringBuffer();
		if (content != null) {
			buf.append(ContentRegistry.getContentName(content));
		}
		if (!parameters.isEmpty()) {
			for (Map.Entry<String, String> parameter : parameters.entrySet()) {
				buf.append('/');
				buf.append(parameter.getKey());
				String paramValue = parameter.getValue();
				if (paramValue != null) {
					buf.append('=');
					buf.append(paramValue);
				}
			}
		}
		return buf.toString();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (obj == this) {
			return true;
		}
		if (obj instanceof Context) {
			Context objCtx = (Context) obj;
			return objCtx.content == this.content
					&& LangUtil.equals(objCtx.parameters, this.parameters);
		}
		return false;
	}

	@Override
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

	private void initContext(ContentComposite content) {
		this.content = content;
	}

	private void initContext(Map<String, String> parameters) {
		this.parameters.putAll(parameters);
	}

}
