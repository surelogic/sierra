package com.surelogic.sierra.client.eclipse.dialogs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

import com.surelogic.common.CommonImages;
import com.surelogic.common.eclipse.SLImages;
import com.surelogic.common.i18n.I18N;

public final class TimeseriesSelectionDialog extends Dialog {

	private final Set<String> f_timeseries;

	private final Set<String> f_selectedTimeseries = new HashSet<String>();

	public Set<String> getSelectedTimeseries() {
		return new HashSet<String>(f_selectedTimeseries);
	}

	private final String f_projectName;

	private final String f_serverLabel;

	private Table f_timeseriesTable;

	private boolean f_useForRemainingOnSameServer = true;

	public boolean useForAllOnSameServer() {
		return f_useForRemainingOnSameServer;
	}

	public TimeseriesSelectionDialog(Shell parentShell, Set<String> timeseries,
			String projectName, String serverLabel) {
		super(parentShell);
		setShellStyle(getShellStyle() | SWT.RESIZE | SWT.MAX);
		if (timeseries == null || timeseries.size() < 1)
			throw new IllegalArgumentException(
					"timeseries set must be non-null and contain at least one element");
		f_timeseries = new HashSet<String>(timeseries);
		if (projectName == null)
			throw new IllegalArgumentException(I18N.err(44, "projectName"));
		f_projectName = projectName;
		if (serverLabel == null)
			throw new IllegalArgumentException(I18N.err(44, "serverLabel"));
		f_serverLabel = serverLabel;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setImage(SLImages.getImage(CommonImages.IMG_SIERRA_SERVER));
		newShell.setText("Select Timeseries");
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite panel = (Composite) super.createDialogArea(parent);
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		panel.setLayout(gridLayout);

		Label banner = new Label(panel, SWT.NONE);
		banner.setLayoutData(new GridData(SWT.CENTER, SWT.TOP, false, true, 1,
				1));
		banner.setImage(SLImages
				.getImage(CommonImages.IMG_SIERRA_POWERED_BY_SURELOGIC));

		final Composite entryPanel = new Composite(panel, SWT.NONE);
		entryPanel.setLayoutData(new GridData(GridData.FILL_BOTH));
		gridLayout = new GridLayout();
		entryPanel.setLayout(gridLayout);

		final Label l = new Label(entryPanel, SWT.WRAP);
		l.setText("Select '" + f_serverLabel + "' timeseries to share '"
				+ f_projectName + "' to:");

		f_timeseriesTable = new Table(entryPanel, SWT.MULTI);
		f_timeseriesTable.setLayoutData(new GridData(GridData.FILL_BOTH));

		final List<String> timeseries = new ArrayList<String>(f_timeseries);
		Collections.sort(timeseries);

		for (String ts : timeseries) {
			TableItem item = new TableItem(f_timeseriesTable, SWT.NONE);
			item.setText(ts);
		}

		final Button useForAll = new Button(entryPanel, SWT.CHECK);
		useForAll.setText("Use for all remaining projects connected to '"
				+ f_projectName + "'");
		useForAll.setSelection(f_useForRemainingOnSameServer);
		useForAll.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				f_useForRemainingOnSameServer = useForAll.getSelection();
			}
		});

		f_timeseriesTable.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				f_selectedTimeseries.clear();
				final TableItem[] sa = f_timeseriesTable.getSelection();
				for (TableItem item : sa) {
					f_selectedTimeseries.add(item.getText());
				}
				setOKState();
			}
		});

		// add controls to composite as necessary
		return panel;
	}

	@Override
	protected Control createContents(Composite parent) {
		final Control contents = super.createContents(parent);
		setOKState();
		return contents;
	}

	private void setOKState() {
		getButton(IDialogConstants.OK_ID).setEnabled(
				!f_selectedTimeseries.isEmpty());
	}
}
