package com.surelogic.sierra.client.eclipse.views;

import java.util.ArrayList;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
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
		GridLayout layout = new GridLayout(1, false);
		GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
		composite.setLayout(layout);
		composite.setLayoutData(layoutData);

		final SashForm sashForm = new SashForm(composite, SWT.NONE);
		layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
		sashForm.setLayoutData(layoutData);

		final List syncTimesList = new List(sashForm, SWT.NONE);
		layoutData = new GridData(SWT.FILL, SWT.FILL, false, true);
		syncTimesList.setLayoutData(layoutData);
		syncTimesList.add("Date-Time 1 >");
		syncTimesList.add("Date-Time 2 >");
		syncTimesList.add("Date-Time 3 >");
		syncTimesList.add("Date-Time 4 >");
		syncTimesList.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				List holder = (List) e.widget;
				// get data
				// refresh table

			}

		});

		f_tableViewer = new TableViewer(sashForm);
		layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
		f_tableViewer.setContentProvider(new SynchronizationContentProvider());
		f_tableViewer.setLabelProvider(new SynchronizationLabelProvider());

		java.util.List<SampleModel> data = new ArrayList<SampleModel>();

		data.add(new SampleModel("11", "12"));
		data.add(new SampleModel("21", "22"));
		data.add(new SampleModel("31", "32"));

		f_tableViewer.setInput(data);

		Table table = f_tableViewer.getTable();

		new TableColumn(table, SWT.LEFT, 0).setText("User");
		new TableColumn(table, SWT.LEFT, 1).setText("Message");

		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		sashForm.setWeights(new int[] { 1, 3 });

		refreshTable();

	}

	private void refreshTable() {
		// System.out.println("Table updated");
		f_tableViewer.refresh();
		Table table = f_tableViewer.getTable();
		for (int i = 0, n = table.getColumnCount(); i < n; i++) {
			table.getColumn(i).pack();
		}
	}

	@Override
	public void setFocus() {
		// Nothing for now

	}

	private class SynchronizationLabelProvider extends LabelProvider implements
			ITableLabelProvider {

		public String getColumnText(Object obj, int index) {
			if (obj instanceof SampleModel) {
				SampleModel model = (SampleModel) obj;
				switch (index) {
				case 0:
					return model.getColumn1();
				case 1:
					return model.getColumn2();
				default:
					return null;
				}
			}

			return null;
		}

		public Image getColumnImage(Object obj, int index) {
			switch (index) {
			case 0:
				return getImage(obj);
			default:
				return null;
			}
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
			if (parent instanceof java.util.List) {
				return ((java.util.List<?>) parent).toArray();
			}
			return null;
		}
	}

	private class SampleModel {
		private String column1;
		private String column2;

		public SampleModel(String column1, String column2) {
			super();
			this.column1 = column1;
			this.column2 = column2;
		}

		public String getColumn1() {
			return column1;
		}

		public void setColumn1(String column1) {
			this.column1 = column1;
		}

		public String getColumn2() {
			return column2;
		}

		public void setColumn2(String column2) {
			this.column2 = column2;
		}

	}

}
