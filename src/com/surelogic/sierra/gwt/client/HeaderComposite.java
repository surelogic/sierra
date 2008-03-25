package com.surelogic.sierra.gwt.client;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SourcesTabEvents;
import com.google.gwt.user.client.ui.TabBar;
import com.google.gwt.user.client.ui.TabListener;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.surelogic.sierra.gwt.client.data.UserAccount;
import com.surelogic.sierra.gwt.client.util.ImageHelper;

//TODO using a TabBar mucks up the context change handling code
//Change the TabBar to our own setup so we can change the tab style without 
//triggering a TabSelected event or similar
public abstract class HeaderComposite extends Composite {
	private static final String PRIMARY_STYLE = "sl-HeaderComposite";
	private static final String UTILITY_STYLE = PRIMARY_STYLE + "-utility";

	private final VerticalPanel rootPanel = new VerticalPanel();
	private final HorizontalPanel headerRow = new HorizontalPanel();
	private final HorizontalPanel utilityRow = new HorizontalPanel();
	private final TabBar mainBar = new TabBar();
	private final List tabContent = new ArrayList();
	private boolean uiCreated;

	public HeaderComposite() {
		super();
		initWidget(rootPanel);
		rootPanel.addStyleName(PRIMARY_STYLE);
		rootPanel.setWidth("100%");

		headerRow.addStyleName(PRIMARY_STYLE);
		headerRow.setWidth("100%");
		final Image sierraLogo = ImageHelper.getImage("surelogic.png");
		headerRow.add(sierraLogo);
		headerRow.setCellVerticalAlignment(sierraLogo,
				HorizontalPanel.ALIGN_MIDDLE);
		rootPanel.add(headerRow);

		utilityRow.addStyleName(PRIMARY_STYLE);

		mainBar.setWidth("100%");
		mainBar.addTabListener(new TabListener() {

			public boolean onBeforeTabSelected(SourcesTabEvents sender,
					int tabIndex) {
				return true;
			}

			public void onTabSelected(SourcesTabEvents sender, int tabIndex) {
				if (tabIndex >= 0 && tabIndex < tabContent.size()) {
					ContentComposite content = (ContentComposite) tabContent
							.get(tabIndex);
					if (!ClientContext.isContent(content)) {
						ClientContext.setContent(content);
					}
				}
			}
		});
	}

	public abstract String getName();

	public final void activate(Context context, UserAccount user) {
		if (!uiCreated) {
			uiCreated = true;
			onInitialize(rootPanel);
		}

		onActivate(context, user);
	}

	public final void updateUser(UserAccount user) {
		onUpdateUser(user);
	}

	public final void updateContext(Context context) {
		if (rootPanel.getWidgetIndex(mainBar) != -1) {
			int newIndex;
			if (context != null && context.getContent() != null) {
				newIndex = tabContent.indexOf(context.getContent());
			} else {
				newIndex = -1;
			}
			if (newIndex != mainBar.getSelectedTab()) {
				mainBar.selectTab(newIndex);
			}
		}
		onUpdateContext(context);
	}

	public final void deactivate() {
		onDeactivate();
	}

	protected final VerticalPanel getRootPanel() {
		return rootPanel;
	}

	protected final Label addUtilityItem(String text,
			ClickListener clickListener) {
		showUtilities();
		final Label lbl = new Label(text);
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
					HorizontalPanel.ALIGN_MIDDLE);
		}
	}

	protected final void addTab(String title, ContentComposite content) {
		showTabs();
		if (tabContent.indexOf(content) == -1) {
			tabContent.add(content);
			mainBar.addTab(title);
		}
	}

	protected final void removeTab(ContentComposite content) {
		int tabIndex = tabContent.indexOf(content);
		if (tabIndex != -1) {
			tabContent.remove(tabIndex);
			mainBar.removeTab(tabIndex);
		}
	}

	protected final void showTabs() {
		if (rootPanel.getWidgetIndex(mainBar) == -1) {
			rootPanel.add(mainBar);
		}
	}

	protected abstract void onInitialize(VerticalPanel rootPanel);

	protected abstract void onActivate(Context context, UserAccount user);

	protected abstract void onUpdateUser(UserAccount user);

	protected abstract void onUpdateContext(Context context);

	protected abstract void onDeactivate();
}
