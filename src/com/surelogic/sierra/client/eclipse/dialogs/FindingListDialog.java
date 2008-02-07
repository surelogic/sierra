package com.surelogic.sierra.client.eclipse.dialogs;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.dialogs.PopupDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;

import com.surelogic.common.eclipse.ViewUtility;
import com.surelogic.sierra.client.eclipse.views.FindingDetailsView;

/**
 * @see QuickOutlinePopupDialog
 */
public class FindingListDialog extends PopupDialog {

	private Map<Long, String> f_findingsMap = new HashMap<Long, String>();
	private List f_findingsList;

	public FindingListDialog(Shell parent, Map<Long, String> findingsMap) {
		super(parent, SWT.RESIZE, true, true, true, true, "Quick Select",
				"Multiple findings found on this line");
		f_findingsMap = findingsMap;
		create();
	}

	private long getValue(String text) {
		if (text != null) {
			Set<Long> findingIds = f_findingsMap.keySet();
			for (Long l : findingIds) {
				String holder = f_findingsMap.get(l);
				if (holder.equals(text)) {
					return l;
				}
			}
		}

		return -1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.text.IInformationControl#addDisposeListener(org.eclipse.swt.events.DisposeListener)
	 */
	public void addDisposeListener(DisposeListener listener) {
		getShell().addDisposeListener(listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.text.IInformationControl#addFocusListener(org.eclipse.swt.events.FocusListener)
	 */
	public void addFocusListener(FocusListener listener) {
		getShell().addFocusListener(listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.text.IInformationControl#removeDisposeListener(org.eclipse.swt.events.DisposeListener)
	 */
	public void removeDisposeListener(DisposeListener listener) {
		getShell().removeDisposeListener(listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.text.IInformationControl#setFocus()
	 */
	public void setFocus() {
		getShell().forceFocus();
		f_findingsList.setFocus();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.text.IInformationControl#setLocation(org.eclipse.swt.graphics.Point)
	 */
	public void setLocation(Point location) {
		if ((getPersistBounds() == false) || (getDialogSettings() == null)) {
			getShell().setLocation(location);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.text.IInformationControl#setSize(int, int)
	 */
	public void setSize(int width, int height) {
		getShell().setSize(width, height);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.swt.events.DisposeListener#widgetDisposed(org.eclipse.swt.events.DisposeEvent)
	 */
	public void widgetDisposed(DisposeEvent e) {
		// Note: We do not reuse the dialog
		f_findingsList = null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.text.IInformationControl#computeSizeHint()
	 */
	public Point computeSizeHint() {
		// Return the shell's size
		// Note that it already has the persisted size if persisting is enabled.
		return getShell().getSize();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.text.IInformationControl#setVisible(boolean)
	 */
	public void setVisible(boolean visible) {
		if (visible) {
			open();
		} else {
			saveDialogBounds(getShell());
			getShell().setVisible(false);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.text.IInformationControl#removeFocusListener(org.eclipse.swt.events.FocusListener)
	 */
	public void removeFocusListener(FocusListener listener) {
		getShell().removeFocusListener(listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.text.IInformationControl#dispose()
	 */
	public void dispose() {
		close();
	}

	/**
	 * refer {@link QuickOutlinePopupDialog}
	 */
	@Override
	public boolean close() {
		// If already closed, there is nothing to do.
		// See https://bugs.eclipse.org/bugs/show_bug.cgi?id=127505
		if (getShell() == null || getShell().isDisposed()) {
			return true;
		}

		saveDialogBounds(getShell());

		return super.close();
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		if (f_findingsMap != null) {
			final Composite composite = new Composite(parent, SWT.NONE);
			composite.setLayout(new GridLayout(1, false));
			composite
					.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

			final Label multipleMarker = new Label(composite, SWT.NONE);
			multipleMarker.setText("Multiple findings on the line, select "
					+ "one to view details :");
			f_findingsList = new List(composite, SWT.WRAP);

			f_findingsList.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
					true));
			Set<Long> findingIds = f_findingsMap.keySet();
			for (Long l : findingIds) {
				String text = f_findingsMap.get(l);
				f_findingsList.add(text);
			}

			f_findingsList.setSelection(0);

			f_findingsList.addMouseListener(new MouseListener() {

				public void mouseDoubleClick(MouseEvent e) {
					if (f_findingsList != null) {
						String summary = f_findingsList.getSelection()[0];
						long id = getValue(summary);

						if (id != -1) {
							FindingDetailsView view = (FindingDetailsView) ViewUtility
									.showView("com.surelogic.sierra.client.eclipse.views.FindingsDetailsView");
							view.findingSelected(id);
							close();

						}
					}
				}

				public void mouseDown(MouseEvent e) {
					// Nothing to do

				}

				public void mouseUp(MouseEvent e) {
					// Nothing to do

				}

			});
		}
		return super.createDialogArea(parent);
	}
}
