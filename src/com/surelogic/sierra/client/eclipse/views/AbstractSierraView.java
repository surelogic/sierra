package com.surelogic.sierra.client.eclipse.views;

import java.util.logging.Logger;

import org.eclipse.jface.action.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.part.ViewPart;

import com.surelogic.common.eclipse.PageBook;
import com.surelogic.common.eclipse.SLImages;
import com.surelogic.common.i18n.I18N;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.client.eclipse.actions.PreferencesAction;

public abstract class AbstractSierraView<M extends IViewMediator> extends ViewPart 
implements IViewCallback {
	public static final Logger LOG = SLLogger.getLogger();
	public static final String VIEW_GROUP = "com.surelogic.sierra.client.eclipse.views";

	protected static MenuItem createMenuItem(Menu menu, String name, Image image) {
		MenuItem item = new MenuItem(menu, SWT.PUSH);
		item.setImage(image);
		item.setText(name);
		return item;
	}

	protected static MenuItem createMenuItem(Menu menu, String name, String imgName) {
		return createMenuItem(menu, name, SLImages.getImage(imgName));
	}
	
	private PageBook f_pages;
	private Control f_noDataPage;
	private Control f_dataPage;
	protected M f_mediator;
	
	@Override
	public final void createPartControl(Composite parent) {
		final PageBook pages = f_pages = new PageBook(parent, SWT.NONE);
		final Link noDataPage = new Link(pages, SWT.WRAP);
		final Composite actualPage = new Composite(pages, SWT.NO_FOCUS);
		actualPage.setLayout(new FillLayout());
		f_noDataPage = noDataPage;
		f_dataPage = actualPage;
		
		/*
		 * Allow direct access to the preferences from the view.
		 */
		final IActionBars bars = getViewSite().getActionBars();
		final IMenuManager menu = bars.getMenuManager();
		menu.add(new GroupMarker(VIEW_GROUP));
		menu.add(new Separator());
		menu.add(new PreferencesAction("Preferences..."));
		
		bars.getToolBarManager().add(new GroupMarker(VIEW_GROUP));
		
		hasData(false);
		final M mediator = f_mediator = createMorePartControls(actualPage);
		mediator.init();		
		
		noDataPage.setText(I18N.msg(mediator.getNoDataId()));
		noDataPage.addListener(SWT.Selection, mediator.getNoDataListener());

		/*
		 * Allow access to help via the F1 key.
		 */
		getSite().getWorkbenchWindow().getWorkbench().getHelpSystem().setHelp(
				parent,
				mediator.getHelpId());
		//parent.layout(true, true);
	}
	
	protected abstract M createMorePartControls(Composite parent);
	
	public final void hasData(boolean data) {
		if (data) {
			f_pages.showPage(f_dataPage);
		} else {
			f_pages.showPage(f_noDataPage);
		}
	}
	
	public final boolean showingData() {
		return f_pages.getPage() == f_dataPage;
	}
	
	public final void setGlobalActionHandler(String id, IAction action) {
		getViewSite().getActionBars().setGlobalActionHandler(id, action);
	}
	
	private IMenuManager getMenuManager() {
		return getViewSite().getActionBars().getMenuManager();
	}
	
	public final void addToViewMenu(IAction action) {
		final IMenuManager menu = getMenuManager();
		menu.prependToGroup(VIEW_GROUP, action);
	}
	
	public final void addToViewMenu(IContributionItem item) {
		final IMenuManager menu = getMenuManager();
		menu.prependToGroup(VIEW_GROUP, item);
	}
	
	private IToolBarManager getToolBarManager() {
		return getViewSite().getActionBars().getToolBarManager();
	}
	
	public final void addToActionBar(IAction action) {
		final IToolBarManager bar = getToolBarManager();
		bar.appendToGroup(VIEW_GROUP, action);
	}
	
	public final void addToActionBar(IContributionItem item) {
		final IToolBarManager bar = getToolBarManager();
		bar.appendToGroup(VIEW_GROUP, item);
	}
	
	@Override
	public final void setFocus() {
		if (!showingData()) {
			f_noDataPage.setFocus();
		}
		if (f_mediator != null)
			f_mediator.setFocus();
	}
	
	@Override
	public final void dispose() {
		if (f_mediator != null)
			f_mediator.dispose();
		super.dispose();
	}
}
