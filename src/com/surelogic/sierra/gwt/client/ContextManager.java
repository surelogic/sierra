package com.surelogic.sierra.gwt.client;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.History;
import com.google.gwt.user.client.HistoryListener;
import com.surelogic.sierra.gwt.client.content.ContentComposite;

public final class ContextManager {
	private static List<ContextListener> contextListeners = new ArrayList<ContextListener>();

	private ContextManager() {
		// Not instantiable
	}

	public static void initialize() {
		History.addHistoryListener(new ContextHistoryListener());
	}

	public static boolean isContext(final String context) {
		final Context ctx = new Context(context);
		return ctx.equals(getContext());
	}

	public static boolean isContent(final ContentComposite content) {
		return content == getContext().getContent();
	}

	public static String getContextAsString() {
		return History.getToken();
	}

	public static Context getContext() {
		return new Context(getContextAsString());
	}

	public static void setContext(final Context context) {
		final String token = context.toString();
		// Note: newItem calls onHistoryChanged, which calls
		// notifyContextListeners only if the token changes
		if (!isContext(token)) {
			History.newItem(token);
		} else {
			notifyContextListeners();
		}
	}

	public static void refreshContext() {
		notifyContextListeners();
	}

	public static void addContextListener(final ContextListener listener) {
		contextListeners.add(listener);
	}

	public static void removeContextListener(final ContextListener listener) {
		contextListeners.remove(listener);
	}

	private static void notifyContextListeners() {
		final Context context = getContext();
		for (final ContextListener listener : contextListeners) {
			listener.onChange(context);
		}
	}

	private static class ContextHistoryListener implements HistoryListener {

		public void onHistoryChanged(final String historyToken) {
			notifyContextListeners();
		}

	}

}
