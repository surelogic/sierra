package com.surelogic.sierra.client.eclipse.views;

import org.eclipse.jface.action.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.part.ViewPart;

import com.surelogic.common.eclipse.PageBook;
import com.surelogic.common.i18n.I18N;
import com.surelogic.sierra.client.eclipse.actions.PreferencesAction;

public abstract class AbstractSierraView<M extends IViewMediator> extends ViewPart 
implements IViewCallback {
	public static final String VIEW_GROUP = "com.surelogic.sierra.client.eclipse.views";

	private PageBook f_pages;
	private Control f_noDataPage;
	private Control f_dataPage;
	protected M f_mediator;
	
	@Override
	public final void createPartControl(Composite parent) {
		final PageBook pages = f_pages = new PageBook(parent, SWT.NONE);
		final Link noDataPage = new Link(pages, SWT.WRAP);
		final Composite actualPage = new Composite(pages, SWT.NONE);
		actualPage.setLayout(new FillLayout());
		f_noDataPage = noDataPage;
		f_dataPage = actualPage;
		
		/*
		 * Allow direct access to the preferences from the view.
		 */
		final IMenuManager menu = getViewSite().getActionBars()
				.getMenuManager();
		menu.add(new GroupMarker(VIEW_GROUP));
		menu.add(new Separator());
		menu.add(new PreferencesAction("Preferences..."));
		
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
	}
	
	protected abstract M createMorePartControls(Composite parent);
	
	public void hasData(boolean data) {
		if (data) {
			f_pages.showPage(f_dataPage);
		} else {
			f_pages.showPage(f_noDataPage);
		}
	}
	
	protected final void addToViewMenu(IAction action) {
		final IMenuManager menu = 
			getViewSite().getActionBars().getMenuManager();
		menu.appendToGroup(VIEW_GROUP, action);
	}
	
	@Override
	public final void setFocus() {
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
