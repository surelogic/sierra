package com.surelogic.sierra.client.eclipse.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import com.surelogic.common.eclipse.SLImages;

public final class SierraServersView extends ViewPart {

	public static final int INFO_WIDTH_HINT = 70;

	private SierraServersMediator f_mediator = null;

	@Override
	public void dispose() {
		if (f_mediator != null)
			f_mediator.dispose();
		super.dispose();
	}

	@Override
	public void createPartControl(Composite parent) {
		GridData data;

		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		parent.setLayout(gridLayout);

		Composite rhs = new Composite(parent, SWT.NONE);
		data = new GridData(SWT.FILL, SWT.FILL, false, true);
		rhs.setLayoutData(data);
		gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		rhs.setLayout(gridLayout);

		Table t = new Table(rhs, SWT.FULL_SELECTION | SWT.MULTI);
		data = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1);
		t.setLayoutData(data);

		Label l = new Label(rhs, SWT.NONE);
		l.setText("Server:");
		data = new GridData(SWT.LEFT, SWT.CENTER, false, false);
		l.setLayoutData(data);
		final Text nameText = new Text(rhs, SWT.SINGLE);
		data = new GridData(SWT.FILL, SWT.CENTER, true, false);
		nameText.setLayoutData(data);
		nameText.addListener(SWT.Verify, new Listener() {
			public void handleEvent(Event event) {
				String text = event.text;
				char[] chars = new char[text.length()];
				text.getChars(0, chars.length, chars, 0);
				for (char c : chars) {
					boolean number = '0' <= c && c <= '9';
					boolean alpha = 'A' <= c && c <= 'z';
					boolean spec = c == '?' || c == ' ' || c == '(' || c == ')';
					if (!(number || alpha || spec)) {
						event.doit = false;
						return;
					}
				}
			}
		});

		Composite c = new Composite(rhs, SWT.NONE);
		data = new GridData(SWT.CENTER, SWT.CENTER, false, false, 2, 1);
		c.setLayoutData(data);
		FillLayout fillLayout = new FillLayout(SWT.HORIZONTAL);
		c.setLayout(fillLayout);
		final ToolBar horizontalToolBar = new ToolBar(c, SWT.HORIZONTAL);
		final ToolItem newServer = new ToolItem(horizontalToolBar, SWT.PUSH);
		newServer.setImage(PlatformUI.getWorkbench().getSharedImages()
				.getImage(ISharedImages.IMG_TOOL_NEW_WIZARD));
		newServer.setToolTipText("New server location");
		final ToolItem duplicateServer = new ToolItem(horizontalToolBar,
				SWT.PUSH);
		duplicateServer.setImage(PlatformUI.getWorkbench().getSharedImages()
				.getImage(ISharedImages.IMG_TOOL_COPY));
		duplicateServer
				.setToolTipText("Duplicates the selected server location");
		final ToolItem deleteServer = new ToolItem(horizontalToolBar, SWT.PUSH);
		deleteServer.setImage(PlatformUI.getWorkbench().getSharedImages()
				.getImage(ISharedImages.IMG_TOOL_DELETE));
		deleteServer.setToolTipText("Deletes the selected server location");
		final Button runQuery = new Button(c, SWT.NONE);
		runQuery.setText("Open in Browser");
		runQuery.setToolTipText("Open the selected server in a Web browser");

		Label banner = new Label(rhs, SWT.NONE);
		banner.setLayoutData(new GridData(SWT.CENTER, SWT.BOTTOM, false, false,
				2, 1));
		banner.setImage(SLImages
				.getImage(SLImages.IMG_SIERRA_POWERED_BY_SURELOGIC));

		/*
		 * Server Information (left-hand side)
		 */

		final Group infoGroup = new Group(parent, SWT.NONE);
		infoGroup.setText("Server Information");
		infoGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		infoGroup.setLayout(gridLayout);

		final Label serverImg = new Label(infoGroup, SWT.NONE);
		serverImg.setImage(SLImages.getImage(SLImages.IMG_SIERRA_SERVER));
		serverImg.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false,
				false));
		final Label serverlabel = new Label(infoGroup, SWT.NONE);
		serverlabel.setText("https://fluid.surelogic.com on port 8080");
		serverlabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false,
				false));

		final Group locGroup = new Group(infoGroup, SWT.NONE);
		locGroup.setText("Location");
		locGroup.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 2,
				1));
		gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		locGroup.setLayout(gridLayout);

		final Label hostLabel = new Label(locGroup, SWT.RIGHT);
		hostLabel.setText("Host:");
		data = new GridData(SWT.RIGHT, SWT.CENTER, false, false);
		data.widthHint = INFO_WIDTH_HINT;
		hostLabel.setLayoutData(data);
		Text hostText = new Text(locGroup, SWT.SINGLE | SWT.BORDER);
		hostText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		final Label portLabel = new Label(locGroup, SWT.RIGHT);
		portLabel.setText("Port:");
		portLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false,
				false));
		Text portText = new Text(locGroup, SWT.SINGLE | SWT.BORDER);
		portText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		final Label protLabel = new Label(locGroup, SWT.RIGHT);
		protLabel.setText("Protocol:");
		protLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false,
				false));

		Composite radio = new Composite(locGroup, SWT.NONE);
		FillLayout fill = new FillLayout(SWT.VERTICAL);
		radio.setLayout(fill);
		Button http = new Button(radio, SWT.RADIO);
		http.setText("http");
		Button https = new Button(radio, SWT.RADIO);
		https.setText("https");

		final Group authGroup = new Group(infoGroup, SWT.NONE);
		authGroup.setText("Authentication");
		authGroup.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 2,
				1));
		gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		authGroup.setLayout(gridLayout);

		final Label userLabel = new Label(authGroup, SWT.RIGHT);
		userLabel.setText("User:");
		// userLabel.setForeground(getShell().getDisplay().getSystemColor(
		// SWT.COLOR_BLUE));
		data = new GridData(SWT.RIGHT, SWT.CENTER, false, false);
		data.widthHint = INFO_WIDTH_HINT;
		userLabel.setLayoutData(data);
		Text f_userText = new Text(authGroup, SWT.SINGLE | SWT.BORDER);
		f_userText
				.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		final Label passwordLabel = new Label(authGroup, SWT.RIGHT);
		passwordLabel.setText("Password:");
		// passwordLabel.setForeground(getShell().getDisplay().getSystemColor(
		// SWT.COLOR_BLUE));
		passwordLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false,
				false));
		Text f_passwordText = new Text(authGroup, SWT.SINGLE | SWT.BORDER);
		f_passwordText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false));
		f_passwordText.setText("");
		f_passwordText.setEchoChar('\u25CF');

		Button f_savePasswordButton = new Button(authGroup, SWT.CHECK);
		f_savePasswordButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER,
				false, false, 2, 1));
		f_savePasswordButton.setText("Save Password");
		f_savePasswordButton.setSelection(false/* TODO boolean field */);
		f_savePasswordButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				// TODO f_savePassword = f_savePasswordButton.getSelection();
			}
		});

		final Composite warning = new Composite(authGroup, SWT.NONE);
		warning.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2,
				1));
		gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		warning.setLayout(gridLayout);
		final Label saveWarningImg = new Label(warning, SWT.NONE);
		saveWarningImg.setImage(SLImages
				.getWorkbenchImage(ISharedImages.IMG_OBJS_WARN_TSK));
		saveWarningImg.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false,
				false));

		final Label saveWarning = new Label(warning, SWT.WRAP);
		data = new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1);
		saveWarning.setLayoutData(data);
		saveWarning
				.setText("Saved secret data is stored on your computer in a format that's difficult, but not impossible, for an intruder to read.");

		f_mediator = new SierraServersMediator();
		f_mediator.init();
	}

	@Override
	public void setFocus() {
		f_mediator.setFocus();
	}
}
