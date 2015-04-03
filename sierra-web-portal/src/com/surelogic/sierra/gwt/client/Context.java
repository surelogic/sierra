package com.surelogic.sierra.gwt.client;

import java.util.HashMap;
import java.util.Map;

import com.surelogic.sierra.gwt.client.content.ContentComposite;
import com.surelogic.sierra.gwt.client.content.ContentRegistry;
import com.surelogic.sierra.gwt.client.data.cache.Cacheable;
import com.surelogic.sierra.gwt.client.util.LangUtil;

/**
 * Represents a navigable location within the web client. Typically a
 * {@link Context} is constructed with a {@link ContentComposite} and optional
 * parameters such as a uuid. The context is then submitted to the
 * {@link ContextManager}, which updates the browser address and the web client
 * content.
 * 
 * @see ContextManager
 * @see ContentComposite
 * @see #current()
 * @see #submit()
 */
public final class Context {
	/**
	 * The standard UUID parameter key for a context
	 */
	private static final String PARAM_UUID = "uuid";

	/**
	 * The content refered to by this context, usually a tab.
	 */
	private ContentComposite content;

	/**
	 * A map of parameters that will be appended to this context, such as UUID.
	 */
	private final Map<String, String> parameters = new HashMap<String, String>();

	/**
	 * A new instance of the current web client context.
	 * 
	 * @return the current context
	 */
	public static final Context current() {
		return ContextManager.getContext();
	}

	/**
	 * Constructs an empty context.
	 */
	public Context() {
		super();
	}

	/**
	 * Constructs a context, and parses the {@code initialContext} value to set
	 * the initial content and/or context parameters.
	 * 
	 * @param initialContext
	 *            initial context values to set up
	 */
	public Context(final String initialContext) {
		super();
		set(initialContext);
	}

	/**
	 * Constructs a context, setting the initial content value.
	 * 
	 * @param content
	 *            the initial content value
	 */
	public Context(final ContentComposite content) {
		super();
		this.content = content;
	}

	/**
	 * Constructs a context, setting the initial content and UUID parameter
	 * values.
	 * 
	 * @param content
	 *            the initial content value
	 * @param uuid
	 *            the initial UUID value
	 */
	public Context(final ContentComposite content, final String uuid) {
		super();
		this.content = content;
		setUuid(uuid);
	}

	/**
	 * Constructs a context, setting the initial content and UUID parameter
	 * values.
	 * 
	 * @param content
	 *            the initial content value
	 * @param item
	 *            the initial UUID, from {@link Cacheable#getUuid()}
	 */
	public Context(final ContentComposite content, final Cacheable item) {
		super();
		this.content = content;
		setUuid(item == null ? null : item.getUuid());
	}

	/**
	 * Returns this context's current content composite.
	 * 
	 * @return the content composite
	 */
	public ContentComposite getContent() {
		return content;
	}

	/**
	 * Sets this context's current content composite.
	 * 
	 * @param content
	 *            the content composite
	 * @return this context, for chained operations
	 */
	public Context setContent(final ContentComposite content) {
		this.content = content;
		return this;
	}

	/**
	 * Returns the value associated with the given name for this context.
	 * 
	 * @param name
	 *            the parameter's name
	 * @return the parameter's value
	 */
	public String getParameter(final String name) {
		return parameters.get(name);
	}

	/**
	 * Sets the value of a parameter in this context.
	 * 
	 * @param name
	 *            the parameter name
	 * @param value
	 *            the parameter value
	 * @return this context, for chained operations
	 */
	public Context setParameter(final String name, final String value) {
		parameters.put(name, value);
		return this;
	}

	/**
	 * Sets a group of parameter values.
	 * 
	 * @param parameters
	 *            the parameters to set
	 * @return this context, for chained operations
	 */
	public Context setParameters(final Map<String, String> parameters) {
		if (parameters != null) {
			parameters.putAll(parameters);
		}
		return this;
	}

	/**
	 * Returns the value of the UUID parameter.
	 * 
	 * @return the UUID parameter value
	 */
	public String getUuid() {
		return getParameter(PARAM_UUID);
	}

	/**
	 * Sets the value of the UUID parameter.
	 * 
	 * @param uuid
	 *            the UUID value
	 * @return this context, for chained operations
	 */
	public Context setUuid(final String uuid) {
		setParameter(PARAM_UUID, uuid);
		return this;
	}

	/**
	 * Sets the value of the UUID parameter to the item's UUID. The item's UUID
	 * is obtained from {@link Cacheable#getUuid()}.
	 * 
	 * @param item
	 *            copy this item's UUID
	 * @return this context, for chained operations
	 */
	public Context setUuid(final Cacheable item) {
		setUuid(item == null ? null : item.getUuid());
		return this;
	}

	/**
	 * Sets the web client context and browser url to this context's values.
	 * 
	 * @see ContextManager#setContext(Context)
	 */
	public void submit() {
		ContextManager.setContext(this);
	}

	/**
	 * Clears this context, and copies all values from the passed in context.
	 * 
	 * @param context
	 *            the context to copy
	 * @return this context, for chained operations
	 */
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
		if (obj == this) {
			return true;
		}
		if (obj instanceof Context) {
			final Context objCtx = (Context) obj;
			return (LangUtil.equals(objCtx.content, content))
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
