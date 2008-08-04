package com.surelogic.sierra.gwt.client.ui;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;

public class RemoteServerLink extends Widget {
	private static final String IMAGE_LINK = ImageHelper.IMAGE_BASE_URL
			+ "remote-link.gif";

	public RemoteServerLink(String text, String url) {
		super();
		final Element remoteLink = DOM.createAnchor();
		remoteLink.setAttribute("href", url);
		remoteLink.setAttribute("title", text);
		setElement(remoteLink);

		final Image linkImage = new Image(IMAGE_LINK);
		linkImage.setUrl(IMAGE_LINK);
		remoteLink.appendChild(linkImage.getElement());
	}
}
