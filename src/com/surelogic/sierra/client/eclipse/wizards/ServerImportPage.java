package com.surelogic.sierra.client.eclipse.wizards;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.surelogic.common.eclipse.SLImages;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.client.eclipse.model.ImportPageServerHolder;
import com.surelogic.sierra.client.eclipse.model.SierraServer;
import com.surelogic.sierra.client.eclipse.model.SierraServerManager;

/**
 * The server import page.
 * 
 * see org.eclipse.ui.internal.wizards.preferences.WizardPreferencesExportPage1
 * see org.eclipse.ui.internal.wizards.preferences.WizardPreferencesPage
 * 
 * @author Tanmay.Sinha
 * 
 */
public class ServerImportPage extends WizardPage {

	public static final String SIERRA_SERVERS = "sierra-servers";
	public static final String CONNECTED_PROJECT = "connected-project";
	public static final String HOST = "host";
	public static final String LABEL = "label";
	public static final String NAME = "name";
	public static final String PORT = "port";
	public static final String PROTOCOL = "protocol";
	public static final String SAVE_PASSWORD = "save-password";
	public static final String SERVER = "server";
	public static final String USER = "user";
	public static final String VERSION = "version";

	private Text f_importFilenameField;
	private Button f_importFileBrowseButton;
	private Group f_tableGroup;
	private Table f_transfersTable;
	private Text f_tableItemDescription;
	private Composite f_selectButtonsComposite;
	private boolean f_invalidFile = true;
	private final List<String> f_existingServers;

	public ServerImportPage() {
		super("SierraServerImportWizardPage"); //$NON-NLS-1$
		f_existingServers = new ArrayList<String>();
		setPageComplete(false);
		setTitle("Import Sierra Servers");
		setDescription("Select a Sierra Server file to import");
	}

	/*
	 * @see IDialogPage#createControl(Composite)
	 */
	public void createControl(Composite parent) {

		initializeDialogUnits(parent);

		Composite workArea = new Composite(parent, SWT.NONE);
		setControl(workArea);

		workArea.setLayout(new GridLayout());
		workArea.setLayoutData(new GridData(GridData.FILL_BOTH
				| GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL));

		createImportFileGroup(workArea);
		createServersTable(workArea);
		setControl(workArea);
		updateEnablement();
		Dialog.applyDialogFont(parent);
	}

	/**
	 * Import all the servers
	 * 
	 * @return
	 */
	public boolean importServers() {

		TableItem[] items = f_transfersTable.getItems();

		SierraServerManager manager = SierraServerManager.getInstance();
		for (TableItem ti : items) {

			if (ti.getChecked()) {
				SierraServer server = manager.create();
				if (ti.getData() instanceof ImportPageServerHolder) {
					ImportPageServerHolder holder = (ImportPageServerHolder) ti
							.getData();
					server.setHost(holder.getHost());
					server.setLabel(holder.getLabel());
					server.setPort(holder.getPort());
					server.setSecure(holder.isSecure());
					server.setUser(holder.getUser());
					server.setSavePassword(false);
					List<String> connectedProjects = holder
							.getConnectedProjects();

					for (String s : connectedProjects) {
						manager.connect(s, server);
					}
				}
			}
		}

		return true;
	}

	private void updateEnablement() {
		boolean complete = true;

		// TODO: Implement file name check. Filenames with invalid characters
		// are still permitted

		if (f_invalidFile) {
			setErrorMessage("Invalid import file");
			f_tableItemDescription.setText("");
			complete = false;
		}

		if (f_importFilenameField.getText().length() == 0) {
			setErrorMessage("Import file name is required");
			complete = false;
		}

		if (f_transfersTable.getItemCount() > 0 && !hasChecked()) {
			setErrorMessage("No Sierra Team Server locations selected to import");
			complete = false;
		}

		if (complete) {
			setErrorMessage(null);
		}

		setPageComplete(complete);
	}

	private void createServersTable(Composite composite) {

		f_tableGroup = new Group(composite, SWT.NONE);
		f_tableGroup.setText("Available Sierra Team Server Locations");
		GridData data = new GridData(GridData.FILL_BOTH);
		data.horizontalSpan = 2;
		f_tableGroup.setLayoutData(data);

		GridLayout layout = new GridLayout();
		f_tableGroup.setLayout(layout);

		f_transfersTable = new Table(f_tableGroup, SWT.CHECK | SWT.BORDER);
		f_transfersTable.setLayoutData(new GridData(GridData.FILL_BOTH));

		Label description = new Label(f_tableGroup, SWT.NONE);
		description.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		description.setText("Description");

		f_tableItemDescription = new Text(f_tableGroup, SWT.V_SCROLL
				| SWT.READ_ONLY | SWT.BORDER | SWT.WRAP);
		f_tableItemDescription.setLayoutData(new GridData(GridData.FILL_BOTH));

		SelectionListener selection = new SelectionListener() {

			public void widgetSelected(SelectionEvent e) {
				if (e.widget == f_transfersTable) {
					updateDescription();
				}
				updateEnablement();
			}

			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}

			private void updateDescription() {
				if (f_transfersTable.getSelectionCount() > 0) {
					TableItem item = f_transfersTable.getSelection()[0];
					f_tableItemDescription.setText(item.getData().toString());
				} else {
					f_tableItemDescription
							.setText("Select a Sierra Team Server location to get more information"); //$NON-NLS-1$
				}
			}
		};

		f_transfersTable.addSelectionListener(selection);
		addSelectionButtons(f_tableGroup);

	}

	/**
	 * Check if any item is selected
	 * 
	 * @return
	 */
	private boolean hasChecked() {
		TableItem[] items = f_transfersTable.getItems();
		for (TableItem ti : items) {
			if (ti.getChecked()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Select all or none items in the table
	 * 
	 * @param bool
	 */
	private void setAllChecked(boolean bool) {
		TableItem[] items = f_transfersTable.getItems();
		for (int i = 0; i < items.length; i++) {
			TableItem item = items[i];
			item.setChecked(bool);
		}
	}

	/**
	 * Add selection buttons
	 * 
	 * @param composite
	 */
	private void addSelectionButtons(Composite composite) {
		Font parentFont = composite.getFont();
		f_selectButtonsComposite = new Composite(composite, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		f_selectButtonsComposite.setLayout(layout);
		GridData data = new GridData(GridData.GRAB_HORIZONTAL);
		data.grabExcessHorizontalSpace = true;
		f_selectButtonsComposite.setLayoutData(data);
		f_selectButtonsComposite.setFont(parentFont);

		Button selectButton = createButton(f_selectButtonsComposite,
				IDialogConstants.SELECT_ALL_ID, "Select All", false);

		SelectionListener listener = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setAllChecked(true);
				updateEnablement();
			}
		};
		selectButton.addSelectionListener(listener);
		selectButton.setFont(parentFont);

		Button deselectButton = createButton(f_selectButtonsComposite,
				IDialogConstants.DESELECT_ALL_ID, "Deselect All", false);

		listener = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setAllChecked(false);
				updateEnablement();
			}
		};
		deselectButton.addSelectionListener(listener);
		deselectButton.setFont(parentFont);
	}

	private void createImportFileGroup(Composite parent) {

		// import file selection group
		Composite importFileSelectionGroup = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		importFileSelectionGroup.setLayout(layout);
		importFileSelectionGroup.setLayoutData(new GridData(
				GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_FILL));

		Label dest = new Label(importFileSelectionGroup, SWT.NONE);
		dest.setText("From file:");

		// import filename entry field
		f_importFilenameField = new Text(importFileSelectionGroup, SWT.SINGLE
				| SWT.BORDER);
		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL
				| GridData.GRAB_HORIZONTAL);
		f_importFilenameField.setLayoutData(data);

		// import file browse button
		f_importFileBrowseButton = new Button(importFileSelectionGroup,
				SWT.PUSH);
		f_importFileBrowseButton.setText("Browse");
		f_importFileBrowseButton.setLayoutData(new GridData(
				GridData.HORIZONTAL_ALIGN_FILL));
		f_importFileBrowseButton.addListener(SWT.Selection, new Listener() {
			private FileDialog fd;

			public void handleEvent(Event event) {
				if (fd == null) {
					fd = new FileDialog(getShell(), SWT.OPEN);
					fd.setText("Destination File");
					fd.setFilterExtensions(new String[] { "*.xml", "*.*" });
					fd.setFilterNames(new String[] { "XML Files (*.xml)",
							"All Files (*.*)" });
				}

				final String selectedFilename = fd.open();
				if (selectedFilename != null) {
					f_importFilenameField.setText(selectedFilename);
				}
			}
		});

		new Label(parent, SWT.NONE); // vertical spacer

		ModifyListener listener = new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				File holder = new File(f_importFilenameField.getText());

				if (holder.exists() && !holder.isDirectory()) {
					getServers(holder);
				}
				updateEnablement();
			}
		};

		f_importFilenameField.addModifyListener(listener);
	}

	/**
	 * Parse the provided server document and obtain the information about the
	 * servers. It uses exceptions to notify validity of the page.
	 * 
	 * @param file
	 */
	private void getServers(File file) {

		SAXParserFactory factory = SAXParserFactory.newInstance();
		ServerFileReader handler = new ServerFileReader();
		try {
			// Parse the input
			SAXParser saxParser = factory.newSAXParser();
			saxParser.parse(file, handler);

			f_invalidFile = false;
			List<ImportPageServerHolder> servers = handler.getServers();

			// Clear the table and description, table.clearAll() does not work
			// properly
			if (f_transfersTable.getItemCount() > 0) {
				TableItem[] items = f_transfersTable.getItems();
				for (TableItem ti : items) {
					ti.dispose();
				}
				f_tableItemDescription
						.setText("Select a Sierra Team Server location to get more information");
			}
			f_existingServers.clear();

			if (!handler.isValid()) {
				throw new IllegalStateException();
			}

			Set<SierraServer> existingServers = SierraServerManager
					.getInstance().getServers();
			Set<String> serverLabels = new HashSet<String>();
			for (SierraServer s : existingServers) {
				serverLabels.add(s.getLabel());
			}

			// Load new servers
			for (ImportPageServerHolder ssh : servers) {
				if (!serverLabels.contains(ssh.getLabel())) {
					TableItem t = new TableItem(f_transfersTable, SWT.CHECK);
					t.setText(ssh.getLabel());
					t.setData(ssh);
					t.setImage(SLImages.getImage(SLImages.IMG_SERVER));
				} else {
					f_existingServers.add(ssh.getLabel());
				}

			}

			if (!f_existingServers.isEmpty()) {
				StringBuilder builder = new StringBuilder();
				for (String s : f_existingServers) {
					builder.append("'" + s + "' ");
				}
				builder.append("already exist and cannot be imported. "
						+ "In order to import, first delete the server(s) "
						+ "using the Sierra Server view.");
				f_tableItemDescription.setText(builder.toString());
			} else {
				f_tableItemDescription
						.setText("Select a Sierra Team Server location to get more information");
			}

		} catch (SAXException e) {
			f_invalidFile = true;
		} catch (ParserConfigurationException e) {
			f_invalidFile = true;
		} catch (IOException e) {
			f_invalidFile = true;
		} catch (Exception e) {
			f_invalidFile = true;
		}
	}

	/**
	 * Taken from the referred file, creates buttons for specified values
	 * 
	 * @param parent
	 * @param id
	 * @param label
	 * @param defaultButton
	 * @return
	 */
	private Button createButton(Composite parent, int id, String label,
			boolean defaultButton) {
		// increment the number of columns in the button bar
		((GridLayout) parent.getLayout()).numColumns++;

		Button button = new Button(parent, SWT.PUSH);
		button.setFont(parent.getFont());

		GridData buttonData = new GridData(GridData.FILL_HORIZONTAL);
		button.setLayoutData(buttonData);

		button.setData(Integer.valueOf(id));
		button.setText(label);

		if (defaultButton) {
			Shell shell = parent.getShell();
			if (shell != null) {
				shell.setDefaultButton(button);
			}
			button.setFocus();
		}
		return button;
	}

	/**
	 * SAX reader for the server save file.
	 */
	static class ServerFileReader extends DefaultHandler {

		private final List<ImportPageServerHolder> f_servers = new ArrayList<ImportPageServerHolder>();
		private ImportPageServerHolder f_server = null;
		private List<String> f_connectedProjects = null;
		private boolean f_isValid = false;

		@Override
		public void startElement(String uri, String localName, String name,
				Attributes attributes) throws SAXException {

			if (name.equals(SIERRA_SERVERS)) {
				f_isValid = true;
			}

			if (name.equals(SERVER)) {
				final String label = attributes.getValue(LABEL);
				final String host = attributes.getValue(HOST);
				final String protocol = attributes.getValue(PROTOCOL);
				final String portString = attributes.getValue(PORT);
				final int port = Integer.parseInt(portString);
				final String user = attributes.getValue(USER);
				f_server = new ImportPageServerHolder();
				f_connectedProjects = new ArrayList<String>();
				f_server.setLabel(label);
				f_server.setHost(host);
				f_server.setSecure("https".equals(protocol));
				f_server.setPort(port);
				f_server.setUser(user);

			} else if (name.equals(CONNECTED_PROJECT)) {
				final String projectName = attributes.getValue(NAME);
				if (f_server == null) {
					SLLogger.getLogger().log(
							Level.SEVERE,
							"connected project " + projectName
									+ " not associated with a server",
							new Exception("XML Format Error"));
				} else {
					f_connectedProjects.add(projectName);
				}
			}
		}

		@Override
		public void endElement(String uri, String localName, String name)
				throws SAXException {
			if (name.equals(SERVER)) {
				f_server.setConnectedProjects(f_connectedProjects);
				f_servers.add(f_server);
				f_connectedProjects = null;
				f_server = null;
			}

		}

		public List<ImportPageServerHolder> getServers() {
			return f_servers;
		}

		public boolean isValid() {
			return f_isValid;
		}
	}
}
