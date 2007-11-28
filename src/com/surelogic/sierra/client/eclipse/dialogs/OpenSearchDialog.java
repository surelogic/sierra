package com.surelogic.sierra.client.eclipse.dialogs;

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

import com.surelogic.common.eclipse.SLImages;
import com.surelogic.sierra.client.eclipse.model.selection.Selection;
import com.surelogic.sierra.client.eclipse.model.selection.SelectionManager;

public final class OpenSearchDialog extends Dialog {

	private final SelectionManager f_manager = SelectionManager.getInstance();

	private Mediator f_mediator = null;

	private Selection f_result = null;

	/**
	 * Returns the selection chosen by the user, or <code>null</code> if
	 * nothing was selected.
	 * 
	 * @return the selection chosen by the user, or <code>null</code> if
	 *         nothing was selected.
	 */
	public Selection getSelection() {
		return f_result; // my be null
	}

	public OpenSearchDialog(Shell parentShell) {
		super(parentShell);
		setShellStyle(getShellStyle() | SWT.RESIZE | SWT.MAX);
	}

	@Override
	protected Control createContents(Composite parent) {
		final Control result = super.createContents(parent);
		f_mediator.setDialogState();
		return result;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite panel = (Composite) super.createDialogArea(parent);
		final GridLayout gridLayout = new GridLayout();
		panel.setLayout(gridLayout);

		final Label l = new Label(panel, SWT.WRAP);
		GridData data = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
		l.setLayoutData(data);
		l.setText("Select a search to open");

		final Group projectGroup = new Group(panel, SWT.NONE);
		data = new GridData(SWT.FILL, SWT.FILL, true, true);
		data.heightHint = 200;
		projectGroup.setLayoutData(data);
		projectGroup.setText("Saved Searches");
		projectGroup.setLayout(new FillLayout());

		final Table projectList = new Table(projectGroup, SWT.FULL_SELECTION);

		for (String projectName : f_manager.getSavedSelectionNames()) {
			TableItem item = new TableItem(projectList, SWT.NONE);
			item.setText(projectName);
			item.setImage(SLImages.getImage(SLImages.IMG_SIERRA_INVESTIGATE));
		}

		f_mediator = new Mediator(projectList);
		f_mediator.init();

		return panel;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Open Search");
		newShell.setImage(SLImages.getImage(SLImages.IMG_SIERRA_INVESTIGATE));
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

		private final Table f_searchTable;

		Mediator(Table searchTable) {
			f_searchTable = searchTable;
		}

		void init() {
			f_searchTable.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event event) {
					setDialogState();
				}
			});
		}

		private void setDialogState() {
			setOKEnabled(f_searchTable.getSelectionCount() > 0);
		}

		void okPressed() {
			if (f_searchTable.getSelectionCount() > 0) {
				f_result = f_manager.getSavedSelection(f_searchTable
						.getSelection()[0].getText());
			}
		}
	}
}
