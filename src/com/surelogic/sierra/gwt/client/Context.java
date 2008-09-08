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

	public static final Context current() {
		return ContextManager.getContext();
	}

	public Context() {
		super();
	}

	public Context(final String initialContext) {
		super();
		set(initialContext);
	}

	public Context(final ContentComposite content) {
		super();
		this.content = content;
	}

	public Context(final ContentComposite content, final String uuid) {
		super();
		this.content = content;
		setUuid(uuid);
	}

	public Context(final ContentComposite content, final Cacheable item) {
		super();
		this.content = content;
		setUuid(item.getUuid());
	}

	public ContentComposite getContent() {
		return content;
	}

	public Context setContent(final ContentComposite content) {
		this.content = content;
		return this;
	}

	public String getParameter(final String name) {
		return parameters.get(name);
	}

	public Context setParameter(final String name, final String value) {
		parameters.put(name, value);
		return this;
	}

	public Context setParameters(final Map<String, String> parameters) {
		if (parameters != null) {
			parameters.putAll(parameters);
		}
		return this;
	}

	public String getUuid() {
		return getParameter(PARAM_UUID);
	}

	public Context setUuid(final String uuid) {
		setParameter(PARAM_UUID, uuid);
		return this;
	}

	public Context setUuid(final Cacheable item) {
		setUuid(item.getUuid());
		return this;
	}

	public void submit() {
		ContextManager.setContext(this);
	}

	public Context set(final String context) {
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
		return this;
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

}
