package com.surelogic.sierra.client.eclipse.dialogs;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.*;

import com.surelogic.common.eclipse.SLImages;
import com.surelogic.sierra.client.eclipse.model.selection.Selection;

public final class OpenSearchDialog extends AbstractSearchDialog {
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
	}

	@Override
	protected Control createDialogArea(Composite parent) {
	  Composite panel = setupDialogArea(parent, "Select a search to open", SWT.FULL_SELECTION);
		return panel;
	}

  @Override
  protected void createMediator(final Table projectList) {
    f_mediator = new Mediator(projectList) {
      @Override
      void okPressed() {
        if (f_searchTable.getSelectionCount() > 0) {
          f_result = f_manager.getSavedSelection(f_searchTable
              .getSelection()[0].getText());
        }
      }
    };
  }
	
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Open Search");
		newShell.setImage(SLImages.getImage(SLImages.IMG_SIERRA_INVESTIGATE));
	}
}
