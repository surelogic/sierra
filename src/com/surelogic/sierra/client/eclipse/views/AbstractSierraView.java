package com.surelogic.sierra.client.eclipse.views;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.part.ViewPart;

import com.surelogic.common.eclipse.PageBook;
import com.surelogic.common.eclipse.SLImages;
import com.surelogic.common.i18n.I18N;
import com.surelogic.sierra.client.eclipse.actions.PreferencesAction;

public abstract class AbstractSierraView<M extends IViewMediator> extends
		ViewPart implements IViewCallback {
	public static final String PREFERENCES_MSG = "Preferences...";
	public static final String VIEW_GROUP = "com.surelogic.sierra.client.eclipse.views";

	protected static MenuItem createMenuItem(Menu menu, String name, Image image) {
		MenuItem item = new MenuItem(menu, SWT.PUSH);
		item.setImage(image);
		item.setText(name);
		return item;
	}

	protected static MenuItem createMenuItem(Menu menu, String name,
			String imgName) {
		return createMenuItem(menu, name, SLImages.getImage(imgName));
	}

	private PageBook f_pages;
	private Control f_noDataPage;
	private Control f_waitingForDataPage;
	private Control f_dataPage;
	private Composite[] f_dataPages;
	protected M f_mediator;

	protected AbstractSierraView(int numDataPages) {
	  f_dataPages = new Composite[numDataPages];
	}
	protected AbstractSierraView() {
	  this(1);
	}
	
	@Override
	public final void createPartControl(Composite parent) {
		final PageBook pages = f_pages = new PageBook(parent, SWT.NONE);
		final Link noDataPage = new Link(pages, SWT.WRAP);
		final Link waitingForDataPage = new Link(pages, SWT.WRAP);
		for(int i=f_dataPages.length-1; i>=0; i--) {
		  final Composite actualPage = new Composite(pages, SWT.NO_FOCUS);
		  actualPage.setLayout(new FillLayout());
		  f_dataPages[i] = actualPage;
		}
		f_waitingForDataPage = waitingForDataPage;	
		f_noDataPage = noDataPage;
		f_dataPage = f_dataPages[0];

		/*
		 * Allow direct access to the preferences from the view.
		 */
		final IActionBars bars = getViewSite().getActionBars();
		final IMenuManager menu = bars.getMenuManager();
		menu.add(new GroupMarker(VIEW_GROUP));
		menu.add(new Separator());
		menu.add(createPreferencesAction());

		bars.getToolBarManager().add(new GroupMarker(VIEW_GROUP));

		setStatus(Status.WAITING_FOR_DATA);
		final M mediator = f_mediator = createMorePartControls(f_dataPages);
		mediator.init();

		waitingForDataPage.setText("Waiting for data ...");
		
		noDataPage.setText(I18N.msg(mediator.getNoDataI18N()));
		final Listener noDataListener = mediator.getNoDataListener();
		if (noDataListener != null)
			noDataPage.addListener(SWT.Selection, noDataListener);

		/*
		 * Allow access to help via the F1 key.
		 */
		getSite().getWorkbenchWindow().getWorkbench().getHelpSystem().setHelp(
				parent, mediator.getHelpId());
		// parent.layout(true, true);
	}

	protected Action createPreferencesAction() {
		return new PreferencesAction(PREFERENCES_MSG);
	}

	protected M createMorePartControls(Composite[] parents) {
	  return createMorePartControls(parents[0]);
	}
	
	protected abstract M createMorePartControls(Composite parent);

	public final void hasData(boolean data) {
		if (data) {
			f_pages.showPage(f_dataPage);
		} else {
			f_pages.showPage(f_noDataPage);
		}
	}
	
	/**
	 * Note that this should be negated, since it doesn't
	 * take the wait state into account
	 */
	public final boolean matchesStatus(boolean showing) {
	  return matchesStatus(showing, 0);
	}
	
	public final boolean matchesStatus(boolean showing, int dataIndex) {
	  if (showing) {
	    /*
	    Control actualPage = f_pages.getPage();
	    for(Control page : f_dataPages) {
	      if (actualPage == page) {
	        return true;
	      }
	    }
	    return false;
	    */
	    return f_pages.getPage() == f_dataPages[dataIndex];
	  }
		return f_pages.getPage() == f_noDataPage;
	}
	 
  public final void setDataPage(int i) {
    final Control oldPage = f_dataPage;
    final Control newPage = f_dataPages[i];
    if (oldPage != newPage) {
      f_dataPage = newPage;
      if (f_pages.getPage() == oldPage) {
        f_pages.showPage(newPage);
      }
    }
  }
	public final void setStatus(Status s) {
		switch (s) {
		default:
		case NO_DATA:
			f_pages.showPage(f_noDataPage);
			break;
		case WAITING_FOR_DATA:
			f_pages.showPage(f_waitingForDataPage);
			break;
		case DATA_READY:
			f_pages.showPage(f_dataPage);
			break;
		}
	}
	
	public final Status getStatus() {
		if (f_pages.getPage() == f_noDataPage) {
			return Status.NO_DATA;
		}
		if (f_pages.getPage() == f_waitingForDataPage) {
			return Status.WAITING_FOR_DATA;
		}
		return Status.DATA_READY;
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
		if (getStatus() == Status.NO_DATA) {
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
