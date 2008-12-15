package com.surelogic.sierra.gwt.client.ui.link;

import com.google.gwt.user.client.ui.Hyperlink;
import com.surelogic.sierra.gwt.client.Context;
import com.surelogic.sierra.gwt.client.content.ContentComposite;

public class ContentLink extends Hyperlink {

	public ContentLink() {
		super();
		addStyleName("sl-ContentLink");
	}

	public ContentLink(final String title, final ContentComposite content,
			final String uuid) {
		super();
		addStyleName("sl-ContentLink");
		setTarget(title, content, uuid);
	}

	public final void setTarget(final String title,
			final ContentComposite content, final String uuid) {
		setText(title);
		final String contextLink = new Context(content, uuid).toString();
		setTargetHistoryToken(contextLink);
	}
}
