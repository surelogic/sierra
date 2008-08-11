package com.surelogic.sierra.client.eclipse.dialogs;

import java.util.Collections;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

import com.surelogic.common.eclipse.JDTUtility;
import com.surelogic.common.eclipse.SLImages;
import com.surelogic.common.images.CommonImages;
import com.surelogic.sierra.client.eclipse.model.SierraServer;
import com.surelogic.sierra.client.eclipse.model.SierraServerManager;

public final class ConnectProjectsDialog extends Dialog {

	private final SierraServerManager f_manager = SierraServerManager
			.getInstance();

	private final SierraServer f_server = f_manager.getFocus();

	private final List<String> f_unconnectedProjects;

	private Mediator f_mediator = null;

	public ConnectProjectsDialog(Shell parentShell) {
		super(parentShell);
		setShellStyle(getShellStyle() | SWT.RESIZE | SWT.MAX);
		if (f_server == null)
			throw new IllegalStateException(
					"server of focus must be non-null (bug)");
		List<String> projectNames = JDTUtility.getJavaProjectNames();
		projectNames.removeAll(f_manager.getConnectedProjects());
		Collections.sort(projectNames);
		f_unconnectedProjects = projectNames;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite panel = (Composite) super.createDialogArea(parent);
		final GridLayout gridLayout = new GridLayout();
		panel.setLayout(gridLayout);

		final Label l = new Label(panel, SWT.WRAP);
		GridData data = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
		l.setLayoutData(data);
		l.setText("Select projects to connect the Sierra server '"
				+ f_server.getLabel() + "':");

		final Group projectGroup = new Group(panel, SWT.NONE);
		data = new GridData(SWT.FILL, SWT.FILL, true, true);
		data.heightHint = 300;
		projectGroup.setLayoutData(data);
		projectGroup.setText("Unconnected Projects");
		projectGroup.setLayout(new FillLayout());

		final Table projectList = new Table(projectGroup, SWT.CHECK);

		for (String projectName : f_unconnectedProjects) {
			TableItem item = new TableItem(projectList, SWT.NONE);
			item.setText(projectName);
			item.setImage(SLImages.getImage(CommonImages.IMG_PROJECT));
		}

		final Button exportAllToggle = new Button(panel, SWT.CHECK);
		exportAllToggle.setText("Connect all projects to '"
				+ f_server.getLabel() + "'");
		exportAllToggle.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false,
				false));

		f_mediator = new Mediator(exportAllToggle, projectGroup, projectList);
		f_mediator.init();

		return panel;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Connect Projects");
		newShell.setImage(SLImages.getImage(CommonImages.IMG_SIERRA_SERVER));
	}

	@Override
	protected void okPressed() {
		if (f_mediator != null)
			f_mediator.okPressed();
		super.okPressed();
	}

	public void setOKEnabled(boolean enabled) {
		Button ok = getButton(IDialogConstants.OK_ID);
		ok.setEnabled(enabled);
	}

	private class Mediator {

		private final Button f_exportAllToggle;

		private final Group f_projectGroup;

		private final Table f_queryTable;

		private boolean f_connectAll;

		Mediator(Button exportAllToggle, Group projectGroup, Table queryTable) {
			f_exportAllToggle = exportAllToggle;
			f_connectAll = f_exportAllToggle.getSelection();
			f_projectGroup = projectGroup;
			f_queryTable = queryTable;
			setDialogState();
		}

		void init() {
			f_exportAllToggle.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event event) {
					f_connectAll = f_exportAllToggle.getSelection();
					if (f_connectAll) {
						// FIX what about unselect all?
						for (TableItem item : f_queryTable.getItems()) {
							item.setChecked(true);
						}
					}
					setDialogState();
				}
			});
		}

		private void setDialogState() {
			f_projectGroup.setEnabled(!f_connectAll);
			f_queryTable.setEnabled(!f_connectAll);
		}

		void okPressed() {
			if (!f_connectAll) {
				for (TableItem item : f_queryTable.getItems()) {
					if (!item.getChecked()) {
						f_unconnectedProjects.remove(item.getText());
					}
				}
			}
			for (String projectName : f_unconnectedProjects) {
				f_manager.connect(projectName, f_server);
			}
		}
	}
}
