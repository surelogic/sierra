package com.surelogic.sierra.eclipse.teamserver.views;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.part.ViewPart;

import com.surelogic.common.eclipse.SLImages;
import com.surelogic.common.images.CommonImages;
import com.surelogic.sierra.eclipse.teamserver.actions.PreferenceAction;

public class TeamServerView extends ViewPart {

	private TeamServerMediator f_mediator = null;

	public TeamServerView() {
	}

	public void createPartControl(Composite parent) {
		GridData data;

		final GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 3;
		parent.setLayout(gridLayout);

		final Button command = new Button(parent, SWT.NONE);
		data = new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 2);
		command.setLayoutData(data);

		final Link status = new Link(parent, SWT.NONE);
		data = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
		status.setLayoutData(data);

		final Label portLabel = new Label(parent, SWT.RIGHT);
		data = new GridData(SWT.FILL, SWT.CENTER, false, false);
		portLabel.setLayoutData(data);

		final Text port = new Text(parent, SWT.SINGLE);
		data = new GridData(SWT.FILL, SWT.CENTER, true, false);
		port.setLayoutData(data);

		final Group statusGroup = new Group(parent, SWT.NONE);
		statusGroup.setText("Status");
		data = new GridData(SWT.FILL, SWT.FILL, false, true);
		data.widthHint = 120;
		statusGroup.setLayoutData(data);
		statusGroup.setLayout(new FillLayout());

		final Canvas trafficLight = new Canvas(statusGroup, SWT.NONE);

		final Group logGroup = new Group(parent, SWT.NONE);
		logGroup.setText("Logs");
		data = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1);
		logGroup.setLayoutData(data);
		final GridLayout logLayout = new GridLayout();
		logLayout.numColumns = 2;
		logGroup.setLayout(logLayout);

		final ToolBar logBar = new ToolBar(logGroup, SWT.VERTICAL);
		data = new GridData(SWT.CENTER, SWT.TOP, false, true);
		logBar.setLayoutData(data);
		final ToolItem jettyConsoleLogItem = new ToolItem(logBar, SWT.RADIO);
		jettyConsoleLogItem.setImage(SLImages.getImage(CommonImages.IMG_CONSOLE));
		jettyConsoleLogItem.setToolTipText("Show Jetty Console Output");
		final ToolItem jettyRequestLogItem = new ToolItem(logBar, SWT.RADIO);
		jettyRequestLogItem.setImage(SLImages.getImage(CommonImages.IMG_JETTY_LOG));
		jettyRequestLogItem.setToolTipText("Show Jetty Request Log");

		final Text logText = new Text(logGroup, SWT.MULTI | SWT.H_SCROLL
				| SWT.V_SCROLL);
		logText.setEditable(false);
		data = new GridData(SWT.FILL, SWT.FILL, true, true);
		logText.setLayoutData(data);

		final String menuText = "Show Server Logs";

		final Menu logContextMenu = new Menu(logBar.getShell(), SWT.POP_UP);
		final MenuItem toggleLogVisibilityMenuItem = new MenuItem(
				logContextMenu, SWT.CHECK);
		toggleLogVisibilityMenuItem.setText(menuText);
		parent.setMenu(logContextMenu);
		logGroup.setMenu(logContextMenu);
		statusGroup.setMenu(logContextMenu);
		trafficLight.setMenu(logContextMenu);
		portLabel.setMenu(logContextMenu);

		final IMenuManager menu = getViewSite().getActionBars()
				.getMenuManager();
		final Action showLogAction = new Action(menuText, Action.AS_CHECK_BOX) {
			@Override
			public void run() {
				if (f_mediator != null) {
					f_mediator.toggleLogVisibility();
				}
			}
		};
		menu.add(showLogAction);
		menu.add(new Separator());
		menu.add(new PreferenceAction("Preferences..."));

		f_mediator = new TeamServerMediator(command, status, portLabel, port,
				trafficLight, jettyConsoleLogItem, jettyRequestLogItem,
				logGroup, logText, toggleLogVisibilityMenuItem, showLogAction);
		f_mediator.init();
	}

	@Override
	public void dispose() {
		if (f_mediator != null)
			f_mediator.dispose();
		super.dispose();
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	@Override
	public void setFocus() {
		if (f_mediator != null)
			f_mediator.setFocus();
	}
}