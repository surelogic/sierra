package com.surelogic.sierra.gwt.client.header;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.surelogic.sierra.gwt.client.Context;
import com.surelogic.sierra.gwt.client.content.ContentComposite;
import com.surelogic.sierra.gwt.client.content.ContentRegistry;
import com.surelogic.sierra.gwt.client.data.UserAccount;
import com.surelogic.sierra.gwt.client.ui.ImageHelper;

//TODO using a TabBar mucks up the context change handling code
//Change the TabBar to our own setup so we can change the tab style without 
//triggering a TabSelected event or similar
public abstract class HeaderComposite extends Composite {
	private static final String PRIMARY_STYLE = "sl-HeaderComposite";
	private static final String UTILITY_STYLE = PRIMARY_STYLE + "-utility";

	private final VerticalPanel rootPanel = new VerticalPanel();
	private final HorizontalPanel headerRow = new HorizontalPanel();
	private final HorizontalPanel utilityRow = new HorizontalPanel();
	private final HorizontalPanel tabStylingPanel = new HorizontalPanel();
	private final HorizontalPanel tabPanel = new HorizontalPanel();
	private final Map<ContentComposite, Hyperlink> tabContent = new HashMap<ContentComposite, Hyperlink>();
	private Hyperlink currentTab;
	private boolean uiCreated;

	public HeaderComposite() {
		super();
		initWidget(rootPanel);
		rootPanel.addStyleName(PRIMARY_STYLE);
		rootPanel.setWidth("100%");

		headerRow.addStyleName(PRIMARY_STYLE);
		headerRow.setWidth("100%");
		final Image sierraLogo = ImageHelper.getImage("surelogic-white.png");
		sierraLogo.addStyleName("logo");
		headerRow.add(sierraLogo);
		headerRow.setCellVerticalAlignment(sierraLogo,
				HorizontalPanel.ALIGN_MIDDLE);
		rootPanel.add(headerRow);

		tabStylingPanel.addStyleName("header-tab-panel");

		final HTML tabLeft = new HTML("&nbsp;", true);
		tabLeft.addStyleName("header-tab-left");
		tabStylingPanel.add(tabLeft);
		tabStylingPanel.setCellHeight(tabLeft, "100%");
		tabStylingPanel.setCellWidth(tabLeft, "10px");
		tabStylingPanel.setCellVerticalAlignment(tabLeft,
				HasVerticalAlignment.ALIGN_BOTTOM);

		tabStylingPanel.add(tabPanel);
		tabStylingPanel.setCellWidth(tabPanel, "1%");
		tabStylingPanel.setCellVerticalAlignment(tabPanel,
				HasVerticalAlignment.ALIGN_BOTTOM);

		final HTML tabRight = new HTML("&nbsp;", true);
		tabRight.addStyleName("header-tab-right");
		tabStylingPanel.add(tabRight);
		tabStylingPanel.setCellHeight(tabRight, "100%");
		tabStylingPanel.setCellHorizontalAlignment(tabRight,
				HorizontalPanel.ALIGN_LEFT);
		tabStylingPanel.setCellVerticalAlignment(tabRight,
				HasVerticalAlignment.ALIGN_BOTTOM);

		utilityRow.addStyleName(PRIMARY_STYLE);
	}

	public final void activate(final Context context, final UserAccount user) {
		if (!uiCreated) {
			uiCreated = true;
			onInitialize(rootPanel);
		}

		onActivate(context, user);
	}

	public final void updateUser(final UserAccount user) {
		onUpdateUser(user);
	}

	public final void updateContext(final Context context) {
		if (rootPanel.getWidgetIndex(tabStylingPanel) != -1) {
			Hyperlink newLink;
			if (context != null) {
				newLink = tabContent.get(context.getContent());
			} else {
				newLink = null;
			}
			if (newLink == null) {
				newLink = (Hyperlink) tabPanel.getWidget(0);
			}
			selectTab(newLink);
		}
		onUpdateContext(context);
	}

	private void selectTab(final Hyperlink newLink) {
		if (currentTab == newLink) {
			return;
		}
		if (currentTab != null) {
			currentTab.removeStyleName("header-tab-selected");
		}
		currentTab = newLink;
		currentTab.addStyleName("header-tab-selected");
	}

	public final void deactivate() {
		onDeactivate();
	}

	protected final VerticalPanel getRootPanel() {
		return rootPanel;
	}

	protected final Label addUtilityItem(final String text,
			final ClickListener clickListener) {
		showUtilities();
		final Label lbl = new Label(text, false);
		lbl.addStyleName(UTILITY_STYLE);
		if (clickListener != null) {
			lbl.addStyleName(UTILITY_STYLE + "-clickable");
			lbl.addClickListener(clickListener);
		}
		utilityRow.add(lbl);
		return lbl;
	}

	protected final Label addUtilitySeparator() {
		return addUtilityItem("|", null);
	}

	protected final void showUtilities() {
		if (headerRow.getWidgetIndex(utilityRow) == -1) {
			headerRow.add(utilityRow);
			headerRow.setCellHorizontalAlignment(utilityRow,
					HorizontalPanel.ALIGN_RIGHT);
			headerRow.setCellVerticalAlignment(utilityRow,
					HorizontalPanel.ALIGN_TOP);
		}
	}

	protected final void addTab(final ContentComposite content,
			final String tabStyleName) {
		showTabs();
		if (tabContent.get(content) == null) {
			final String contentUrl = new Context(content).toString();
			final Hyperlink contentLink = new Hyperlink(ContentRegistry
					.getContentTitle(content), contentUrl);
			contentLink.addStyleName("header-tab");
			contentLink.addStyleName("header-tab-" + tabStyleName);
			tabContent.put(content, contentLink);
			tabPanel.add(contentLink);
			tabPanel.setCellVerticalAlignment(contentLink,
					HasVerticalAlignment.ALIGN_BOTTOM);
		}
	}

	protected final void addTabSpacer() {
		final HTML spacer = new HTML("&nbsp;", true);
		spacer.addStyleName("header-tab-spacer");
		spacer.setHeight("100%");
		tabPanel.add(spacer);
		tabPanel.setCellVerticalAlignment(spacer,
				HasVerticalAlignment.ALIGN_BOTTOM);
	}

	protected final void removeTab(final ContentComposite content) {
		final Hyperlink contentLink = tabContent.get(content);
		if (contentLink != null) {
			tabContent.remove(content);
			tabPanel.remove(contentLink);
		}
	}

	protected final void showTabs() {
		if (rootPanel.getWidgetIndex(tabStylingPanel) == -1) {
			rootPanel.add(tabStylingPanel);
			rootPanel.setCellVerticalAlignment(tabStylingPanel,
					HasVerticalAlignment.ALIGN_BOTTOM);
		}
	}

	protected abstract void onInitialize(VerticalPanel rootPanel);

	protected abstract void onActivate(Context context, UserAccount user);

	protected abstract void onUpdateUser(UserAccount user);

	protected abstract void onUpdateContext(Context context);

	protected abstract void onDeactivate();
}
