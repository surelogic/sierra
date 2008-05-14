package com.surelogic.sierra.client.eclipse.views;

import org.eclipse.swt.widgets.Listener;

public interface IViewMediator {

	void init();

	/**
	 * Gets the identifier to query from the SureLogic I18N that will be used to
	 * indicated that no data should be shown. This identifier will be placed
	 * within a {@link org.eclipse.swt.widgets.Link} widget.
	 * 
	 * @return the identifier to query from the SureLogic I18N that will be used
	 *         to indicated that no data should be shown.
	 */
	String getNoDataI18N();

	/**
	 * Returns a listener to take action if the link text returned by
	 * {@link #getNoDataI18N()} contains hyperlinks.
	 * 
	 * @return a listener, or {@code null} if no listener is required.
	 */
	Listener getNoDataListener();

	/**
	 * Gets the help identifier for this view. Used for context sensitive help.
	 * 
	 * @return a help identifier.
	 */
	String getHelpId();

	void setFocus();

	void dispose();
}
