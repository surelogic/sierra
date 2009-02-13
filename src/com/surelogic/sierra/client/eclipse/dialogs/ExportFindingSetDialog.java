package com.surelogic.sierra.client.eclipse.dialogs;

import java.io.File;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.surelogic.common.CommonImages;
import com.surelogic.common.eclipse.SLImages;
import com.surelogic.sierra.client.eclipse.jobs.ExportFindingSetInCSVFormatJob;
import com.surelogic.sierra.client.eclipse.jobs.ExportFindingSetInXMLFormatJob;
import com.surelogic.sierra.client.eclipse.jobs.ExportFindingSetJob;

public final class ExportFindingSetDialog extends Dialog {

	private final String f_listOfFindingsQuery;

	private Text f_exportFilenameText;
	private Button f_csvFormat;
	private Button f_xmlFormat;

	public ExportFindingSetDialog(Shell shell, String listOfFindingsQuery) {
		super(shell);
		assert listOfFindingsQuery != null;
		f_listOfFindingsQuery = listOfFindingsQuery;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setImage(SLImages.getImage(CommonImages.IMG_EXPORT));
		newShell.setText("Export Findings");
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite panel = (Composite) super.createDialogArea(parent);
		final GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 3;
		panel.setLayout(gridLayout);

		final Label directions = new Label(panel, SWT.NONE);
		directions.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false,
				false, 3, 1));
		directions.setText("Select the desired export"
				+ " format and the destination filename");

		final Group g = new Group(panel, SWT.NONE);
		g.setText("Export Format");
		g.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false, 3, 1));
		final RowLayout rl = new RowLayout(SWT.VERTICAL);
		rl.fill = true;
		rl.wrap = false;
		g.setLayout(rl);

		f_csvFormat = new Button(g, SWT.RADIO);
		f_csvFormat.setText("Comma Separated Values (CSV)");
		f_csvFormat.setSelection(true);
		f_csvFormat.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				changeFileExtension("xml", "csv");
			}
		});

		f_xmlFormat = new Button(g, SWT.RADIO);
		f_xmlFormat.setText("XML for Excel 2007");
		f_xmlFormat.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				changeFileExtension("csv", "xml");
			}
		});

		Label buildfilenameLabel = new Label(panel, SWT.NONE);
		buildfilenameLabel.setText("Export file:");

		f_exportFilenameText = new Text(panel, SWT.SINGLE | SWT.BORDER);
		f_exportFilenameText.setText(System.getProperty("user.home")
				+ System.getProperty("file.separator") + "findings.csv");
		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL
				| GridData.GRAB_HORIZONTAL);
		f_exportFilenameText.setLayoutData(data);

		final Button browseButton = new Button(panel, SWT.PUSH);
		browseButton.setText("Browse...");
		browseButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false,
				false));
		browseButton.addListener(SWT.Selection, new Listener() {
			private FileDialog fd;

			public void handleEvent(Event event) {
				if (fd == null) {
					fd = new FileDialog(getShell(), SWT.SAVE);
					fd.setText("Destination File");
					fd.setFilterExtensions(new String[] { "*.csv", "*.xml",
							"*.*" });
					fd.setFilterNames(new String[] { "CSV Files (*.csv)",
							"XML Files (*.xml)", "All Files (*.*)" });
				}
				final String fileName = f_exportFilenameText.getText();
				int i = fileName.lastIndexOf(System
						.getProperty("file.separator"));
				if (i != -1) {
					final String path = fileName.substring(0, i);
					fd.setFilterPath(path);
					if (i + 1 < fileName.length()) {
						final String file = fileName.substring(i + 1);
						fd.setFileName(file);
					}
				}
				final String selectedFilename = fd.open();
				if (selectedFilename != null) {
					f_exportFilenameText.setText(selectedFilename);
				}
			}
		});
		return panel;
	}

	@Override
	protected void okPressed() {
		final File exportfile = new File(f_exportFilenameText.getText());
		if (exportfile != null) {
			ExportFindingSetJob job;
			if (f_csvFormat.getSelection()) {
				// CSV format
				job = new ExportFindingSetInCSVFormatJob(f_listOfFindingsQuery,
						exportfile);
			} else {
				// XML format
				job = new ExportFindingSetInXMLFormatJob(f_listOfFindingsQuery,
						exportfile);
			}
			job.setUser(true);
			job.schedule();
		}
		super.okPressed();
	}

	private void changeFileExtension(String from, String to) {
		StringBuilder b = new StringBuilder(f_exportFilenameText.getText());
		if (b.toString().endsWith(from)) {
			b.replace(b.length() - from.length(), b.length(), to);
		}
		f_exportFilenameText.setText(b.toString());
	}

}
