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

/**
 * This singleton manages all interaction with the web client context. It
 * handles browser navigation events, user interaction, and can optionally block
 * navigation until the user confirms a prompt. Navigation blocking is generally
 * used when in an "edit" mode that requires a save or cancel confirmation.
 * 
 * @see Context
 * @see ContextListener
 */
public final class ContextManager {
	/**
	 * A list of context listeners that will be notified of context changes.
	 */
	private static List<ContextListener> contextListeners = new ArrayList<ContextListener>();

	/**
	 * If not null, this context will remain the current context until cleared
	 * via {@link #unlockContext()} or user confirmation.
	 */
	private static Context lockedContext;

	/**
	 * The prompt to show the user if the current context is locked via
	 * {@link #lockContext(String)}.
	 */
	private static String lockedMessage;

	/**
	 * Singleton
	 */
	private ContextManager() {
		// Not instantiable
	}

	/**
	 * Called immediately on GWT module loading to start browser history
	 * monitoring.
	 */
	public static void initialize() {
		History.addHistoryListener(new HistoryListener() {

			public void onHistoryChanged(final String historyToken) {
				notifyContextListeners();
			}
		});
	}

	/**
	 * Returns true if the current context matches a given context.
	 * 
	 * @param context
	 *            the context to compare
	 * @return true if the context is current
	 */
	public static boolean isContext(final String context) {
		final Context ctx = new Context(context);
		return ctx.equals(getContext());
	}

	/**
	 * Returns true if the current content matches a given content.
	 * 
	 * @param content
	 *            the content to compare
	 * @return true if the content is current
	 */
	public static boolean isContent(final ContentComposite content) {
		return content == getContext().getContent();
	}

	/**
	 * Returns the current web client context as a string.
	 * 
	 * @return the current context
	 */
	public static String getContextAsString() {
		return History.getToken();
	}

	/**
	 * Returns the current web client context.
	 * 
	 * @return the current context
	 */
	public static Context getContext() {
		return new Context(getContextAsString());
	}

	/**
	 * Attempts to update the current context to a new value. This will trigger
	 * a notification to context listeners if the current context has not been
	 * locked, or the user confirms that the lock should be released.
	 * 
	 * @param context
	 *            the new context
	 * @see #addContextListener(ContextListener)
	 * @see #lockContext(String)
	 */
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

	/**
	 * Forces a notification to all context listeners if the current context is
	 * not locked, or if the user confirms that the lock should be released.
	 * 
	 * @see #addContextListener(ContextListener)
	 * @see #lockContext(String)
	 */
	public static void refreshContext() {
		notifyContextListeners();
	}

	/**
	 * Registers a context listener to receive context change notifications.
	 * 
	 * @param listener
	 *            the listener to notify of context changes.
	 * @see #removeContextListener(ContextListener)
	 */
	public static void addContextListener(final ContextListener listener) {
		contextListeners.add(listener);
	}

	/**
	 * Removes a context listener so it will no longer be notified of context
	 * changes.
	 * 
	 * @param listener
	 *            the listener to remove
	 * @see #addContextListener(ContextListener)
	 */
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

	/**
	 * Notifies context listeners of a change or forced refresh of the current
	 * web client context. If the current context is locked, this method will
	 * prompt the user to confirm or cancel navigation away from the current
	 * context. If the user confirms the navigation, the context will be
	 * unlocked and update to the new value.
	 * 
	 * @see #lockContext(String)
	 * @see #unlockContext()
	 * @see #addContextListener(ContextListener)
	 * @see #removeContextListener(ContextListener)
	 */
	private static void notifyContextListeners() {
		final Context context = getContext();

		// if lockedContext isn't null, the current context is locked
		if (lockedContext != null) {
			// only prompt the user if trying to change from the current context
			if (!lockedContext.equals(context)) {
				// build the prompt dialog
				final MessageDialog dlg = new MessageDialog("Warning", null,
						lockedMessage);
				dlg.addPopupListener(new PopupListener() {

					public void onPopupClosed(final PopupPanel sender,
							final boolean autoClosed) {
						// the prompt dialog has closed
						final Status s = dlg.getStatus();
						if (s != null && s.isSuccess()) {
							// the user has confirmed the navigation away from
							// the current context
							// unlock the context and set the new value
							unlockContext();
							setContext(context);
						} else {
							// the user has cancelled the navigation, revert the
							// current context back to the locked value
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

}
