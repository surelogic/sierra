package com.surelogic.sierra.gwt.client;

import java.util.HashMap;
import java.util.Map;

import com.surelogic.sierra.gwt.client.content.ContentComposite;
import com.surelogic.sierra.gwt.client.content.ContentRegistry;
import com.surelogic.sierra.gwt.client.data.cache.Cacheable;
import com.surelogic.sierra.gwt.client.util.LangUtil;

public final class Context {
	private static final String PARAM_UUID = "uuid";

	private ContentComposite content;
	private final Map<String, String> parameters = new HashMap<String, String>();

	public static Context create(final String context) {
		final Context ctx = new Context();
		ctx.initContext(context);
		return ctx;
	}

	public static Context create(final Context baseContext,
			final Map<String, String> parameters) {
		final Context newContext = new Context();
		if (baseContext == null) {
			newContext.initContext(ContextManager.getContext());
		} else {
			newContext.initContext(baseContext);
		}
		newContext.initContext(parameters);
		return newContext;
	}

	public static Context create(final ContentComposite content,
			final Map<String, String> parameters) {
		final Context newContext = new Context();
		newContext.initContext(content);
		newContext.initContext(parameters);
		return newContext;
	}

	public static Context createWithUuid(final String uuid) {
		final Map<String, String> params = new HashMap<String, String>();
		params.put(PARAM_UUID, uuid);
		return Context.create(ContextManager.getContext(), params);
	}

	public static Context createWithUuid(final Cacheable item) {
		return createWithUuid(item.getUuid());
	}

	public static Context createWithUuid(final ContentComposite content,
			final String uuid) {
		final Map<String, String> params = new HashMap<String, String>();
		params.put(PARAM_UUID, uuid);
		return Context.create(content, params);
	}

	public static Context createWithUuid(final ContentComposite content,
			final Cacheable item) {
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

	public String getParameter(final String name) {
		return parameters.get(name);
	}

	/**
	 * Return a new context identical to the existing one, but with the added
	 * parameter.
	 * 
	 * @param param
	 * @param value
	 * @return
	 */
	public Context withParam(final String param, final String value) {
		final Map<String, String> params = new HashMap<String, String>();
		params.putAll(parameters);
		params.put(param, value);
		return Context.create(content, params);
	}

	@Override
	public String toString() {
		final StringBuffer buf = new StringBuffer();
		if (content != null) {
			buf.append(ContentRegistry.getContentName(content));
		}
		if (!parameters.isEmpty()) {
			for (final Map.Entry<String, String> parameter : parameters
					.entrySet()) {
				buf.append('/');
				buf.append(parameter.getKey());
				final String paramValue = parameter.getValue();
				if (paramValue != null) {
					buf.append('=');
					buf.append(paramValue);
				}
			}
		}
		return buf.toString();
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == null) {
			return false;
		}
		if (obj == this) {
			return true;
		}
		if (obj instanceof Context) {
			final Context objCtx = (Context) obj;
			return (objCtx.content == content)
					&& LangUtil.equals(objCtx.parameters, parameters);
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

	private void initContext(final String context) {
		content = null;
		parameters.clear();

		String contentName;
		if ((context != null) && (context.length() != 0)) {
			final int split = context.indexOf('/');
			String argString;
			if (split != -1) {
				contentName = context.substring(0, split).toLowerCase();
				argString = context.substring(split + 1);
			} else {
				contentName = context.toLowerCase();
				argString = null;
			}

			if ((argString != null) && (argString.length() != 0)) {
				final String[] argArray = argString.split("/");
				for (final String element : argArray) {
					final String[] argPair = element.split("\\=");
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

	private void initContext(final Context context) {
		content = context.content;
		parameters.putAll(context.parameters);
	}

	private void initContext(final ContentComposite content) {
		this.content = content;
	}

	private void initContext(final Map<String, String> parameters) {
		if (parameters != null) {
			this.parameters.putAll(parameters);
		}
	}

}
