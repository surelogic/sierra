package com.surelogic.sierra.client.eclipse.dialogs;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.*;

import com.surelogic.common.eclipse.SLImages;

public final class DeleteSearchDialog extends AbstractSearchDialog {
	public DeleteSearchDialog(Shell parentShell) {
		super(parentShell);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite panel = setupDialogArea(parent, "Select one or more searches to delete",
		    SWT.MULTI | SWT.FULL_SELECTION);
		return panel;
	}

  @Override
  protected void createMediator(final Table projectList) {
    f_mediator = new Mediator(projectList) {
		  @Override
      void okPressed() {
		    for (TableItem item : f_searchTable.getSelection()) {
		      if (!item.getChecked()) {
		        f_manager.removeSavedSelection(item.getText());
		      }
		    }
		  }
		};
  }

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Delete Search");
		newShell.setImage(SLImages.getImage(SLImages.IMG_GRAY_X));
	}
}
