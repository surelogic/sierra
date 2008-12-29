package com.surelogic.sierra.gwt.client;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.History;
import com.google.gwt.user.client.HistoryListener;
import com.google.gwt.user.client.ui.PopupListener;
import com.google.gwt.user.client.ui.PopupPanel;
import com.surelogic.sierra.gwt.client.content.ContentComposite;
import com.surelogic.sierra.gwt.client.data.Status;
import com.surelogic.sierra.gwt.client.ui.dialog.MessageDialog;

public final class ContextManager {
	private static List<ContextListener> contextListeners = new ArrayList<ContextListener>();
	private static Context lockedContext;
	private static String lockedMessage;

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

	/**
	 * Locks the browser context so that it will not change unless the user
	 * clicks OK when prompted.
	 * 
	 * @param message
	 *            the text displayed to the user when confirming the context
	 *            unlock
	 * @see #unlockContext()
	 */
	public static void lockContext(final String message) {
		lockedContext = getContext();
		lockedMessage = message;
	}

	/**
	 * Unlocks the browser context, allow browser navigation without a
	 * confirmation prompt.
	 * 
	 * @see #lockContext(String)
	 */
	public static void unlockContext() {
		lockedContext = null;
		lockedMessage = null;
	}

	private static void notifyContextListeners() {
		final Context context = getContext();
		if (lockedContext != null) {
			if (!lockedContext.equals(context)) {
				final MessageDialog dlg = new MessageDialog("Warning", null,
						lockedMessage);
				dlg.addPopupListener(new PopupListener() {

					public void onPopupClosed(final PopupPanel sender,
							final boolean autoClosed) {
						final Status s = dlg.getStatus();
						if (s != null && s.isSuccess()) {
							unlockContext();
							setContext(context);
						} else {
							setContext(lockedContext);
						}
					}
				});
				dlg.center();
			}
		} else {
			for (final ContextListener listener : contextListeners) {
				listener.onChange(context);
			}
		}
	}

	private static class ContextHistoryListener implements HistoryListener {

		public void onHistoryChanged(final String historyToken) {
			notifyContextListeners();
		}

	}

}
