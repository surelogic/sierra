package com.surelogic.sierra.gwt.client.ui.link;

import com.google.gwt.user.client.ui.Hyperlink;
import com.surelogic.sierra.gwt.client.Context;
import com.surelogic.sierra.gwt.client.content.ContentComposite;

public class ContentLink extends Hyperlink {

	public ContentLink(final String title, final ContentComposite content,
			final String uuid) {
		super();
		setText(title);
		final String contextLink = new Context(content, uuid).toString();
		setTargetHistoryToken(contextLink);
		addStyleName("sl-ContentLink");
	}
}
