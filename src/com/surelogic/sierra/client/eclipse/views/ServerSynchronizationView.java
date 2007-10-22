package com.surelogic.sierra.client.eclipse.views;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.List;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

/**
 * The Server Synchronization view
 * 
 * @author Tanmay.Sinha
 * 
 */
public class ServerSynchronizationView extends ViewPart {

	private TableViewer f_tableViewer;

	@Override
	public void createPartControl(Composite parent) {
		final Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
		composite.setLayout(layout);
		composite.setLayoutData(layoutData);

		final List syncTimesList = new List(composite, SWT.H_SCROLL
				| SWT.V_SCROLL);
		layoutData = new GridData(SWT.FILL, SWT.FILL, false, true);
		syncTimesList.setLayoutData(layoutData);
		syncTimesList.add("Date-Time 1 >");
		syncTimesList.add("Date-Time 2 >");
		syncTimesList.add("Date-Time 3 >");
		syncTimesList.add("Date-Time 4 >");
		syncTimesList.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				refreshTable();
			}

		});

		f_tableViewer = new TableViewer(composite);
		layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
		f_tableViewer.setContentProvider(new SynchronizationContentProvider());
		f_tableViewer.setLabelProvider(new SynchronizationLabelProvider());
		f_tableViewer.setInput(getViewSite());

	}

	private void refreshTable() {
		// refresh viewer
		// f_tableViewer.setInput(input);
		System.out.println("Table updated");
		f_tableViewer.setInput(new Object());
	}

	@Override
	public void setFocus() {
		// Nothing for now

	}

	private class SynchronizationLabelProvider extends LabelProvider implements
			ITableLabelProvider {

		public String getColumnText(Object obj, int index) {
			return getText(obj);
		}

		public Image getColumnImage(Object obj, int index) {
			return getImage(obj);
		}

		@Override
		public Image getImage(Object obj) {
			return PlatformUI.getWorkbench().getSharedImages().getImage(
					ISharedImages.IMG_OBJ_ELEMENT);
		}

	}

	private class SynchronizationContentProvider implements
			IStructuredContentProvider {

		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
			// Nothing for now
		}

		public void dispose() {

			// Nothing for now
		}

		public Object[] getElements(Object parent) {
			return new String[] { "One", "Two", "Three" };
		}

	}

}
