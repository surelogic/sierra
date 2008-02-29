package com.surelogic.sierra.eclipse.teamserver.views;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Level;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Scrollable;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;
import org.eclipse.ui.progress.UIJob;

import com.surelogic.common.eclipse.SLImages;
import com.surelogic.common.eclipse.jobs.SLUIJob;
import com.surelogic.common.i18n.I18N;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.eclipse.teamserver.dialogs.ServerStaysRunningWarning;
import com.surelogic.sierra.eclipse.teamserver.model.IServerLogObserver;
import com.surelogic.sierra.eclipse.teamserver.model.ITeamServerObserver;
import com.surelogic.sierra.eclipse.teamserver.model.JettyRequestLog;
import com.surelogic.sierra.eclipse.teamserver.model.ServerLog;
import com.surelogic.sierra.eclipse.teamserver.model.SierraPortalLog;
import com.surelogic.sierra.eclipse.teamserver.model.SierraServicesLog;
import com.surelogic.sierra.eclipse.teamserver.model.TeamServer;
import com.surelogic.sierra.eclipse.teamserver.preferences.PreferenceConstants;

public final class TeamServerMediator implements ITeamServerObserver {

	final Button f_command;
	final Link f_status;
	final Text f_host;
	final Text f_port;
	final Canvas f_trafficLight;
	final ToolItem f_jettyRequestLogItem;
	final ToolItem f_portalLogItem;
	final ToolItem f_servicesLogItem;
	final Text f_logText;

	final TeamServer f_teamServer;
	final ServerLog f_jettyRequestLog;
	final IServerLogObserver f_jettyRequestLogObserver;
	final ServerLog f_portalLog;
	final IServerLogObserver f_portalLogObserver;
	final ServerLog f_servicesLog;
	final IServerLogObserver f_servicesLogObserver;

	private class LogObserver implements IServerLogObserver {
		private final ToolItem f_item;

		LogObserver(final ToolItem item) {
			f_item = item;
		}

		public void notify(final ServerLog log) {
			final UIJob job = new SLUIJob() {
				@Override
				public IStatus runInUIThread(IProgressMonitor monitor) {
					if (f_item.getSelection()) {
						updateLogText(log.getText());
					}
					return Status.OK_STATUS;
				}
			};
			job.schedule();
		}
	}

	private class LogSelectionListener implements Listener {

		private final ServerLog f_log;

		LogSelectionListener(ServerLog log) {
			f_log = log;
		}

		public void handleEvent(Event event) {
			updateLogText(f_log.getText());
		}
	}

	private final ScheduledExecutorService f_executor = Executors
			.newScheduledThreadPool(2);

	TeamServerMediator(Button command, Link status, Text host, Text port,
			Canvas trafficLight, ToolItem jettyRequestLogItem,
			ToolItem portalLogItem, ToolItem servicesLogItem, Text log) {
		f_command = command;
		f_status = status;
		f_host = host;
		f_port = port;
		f_trafficLight = trafficLight;
		f_jettyRequestLogItem = jettyRequestLogItem;
		f_portalLogItem = portalLogItem;
		f_servicesLogItem = servicesLogItem;
		f_logText = log;

		f_teamServer = new TeamServer(getPort(), f_executor);
		f_jettyRequestLog = new JettyRequestLog(f_executor);
		f_jettyRequestLogObserver = new LogObserver(f_jettyRequestLogItem);
		f_portalLog = new SierraPortalLog(f_executor);
		f_portalLogObserver = new LogObserver(f_portalLogItem);
		f_servicesLog = new SierraServicesLog(f_executor);
		f_servicesLogObserver = new LogObserver(f_servicesLogItem);
	}

	void init() {
		f_host.setText(getHostAddress());
		f_status.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				final String urlString = getURLString();
				try {
					final URL url = new URL(urlString);
					openInBrowser(url);
				} catch (MalformedURLException e) {
					SLLogger.getLogger().log(Level.SEVERE,
							I18N.err(41, urlString), e);
				}
			}
		});
		f_trafficLight.addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent e) {
				final Image trafficLightImage = getTrafficLightImage();
				if (e.widget instanceof Scrollable) {
					final Rectangle r = ((Scrollable) e.widget).getClientArea();
					final int lightWidth = trafficLightImage.getBounds().width;
					final int lightHeight = trafficLightImage.getBounds().height;
					int drawWidth = r.width;
					int drawHeight = (int) ((double) drawWidth * ((double) lightHeight / (double) lightWidth));
					int drawX = 0;
					int drawY = (r.height - drawHeight) / 2;
					if (drawY < 0) {
						drawHeight = r.height;
						drawWidth = (int) ((double) drawHeight * ((double) lightWidth / (double) lightHeight));
						drawX = (r.width - drawWidth) / 2;
						drawY = 0;
					}
					e.gc.drawImage(trafficLightImage, 0, 0, lightWidth,
							lightHeight, drawX, drawY, drawWidth, drawHeight);
				}
			}
		});
		f_command.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				doCommand();
			}
		});

		f_jettyRequestLogItem.addListener(SWT.Selection,
				new LogSelectionListener(f_jettyRequestLog));
		f_portalLogItem.addListener(SWT.Selection, new LogSelectionListener(
				f_portalLog));
		f_servicesLogItem.addListener(SWT.Selection, new LogSelectionListener(
				f_servicesLog));
		
		f_jettyRequestLogItem.setSelection(true);

		f_teamServer.init();
		f_teamServer.addObserver(this);

		f_jettyRequestLog.init();
		f_jettyRequestLog.addObserver(f_jettyRequestLogObserver);
		f_portalLog.init();
		f_portalLog.addObserver(f_portalLogObserver);
		f_servicesLog.init();
		f_servicesLog.addObserver(f_servicesLogObserver);

		adjustControlState();
	}

	private Image getTrafficLightImage() {
		if (f_teamServer.isRunning()) {
			return SLImages.getImage(SLImages.IMG_TRAFFIC_LIGHT_GREEN);
		} else if (f_teamServer.isNotRunning()) {
			return SLImages.getImage(SLImages.IMG_TRAFFIC_LIGHT_RED);
		} else {
			return SLImages.getImage(SLImages.IMG_TRAFFIC_LIGHT_YELLOW);
		}
	}

	private int getPort() {
		return Integer.parseInt(f_port.getText());
	}

	private String getURLString() {
		return "http://localhost:" + getPort();
	}

	private String getHostAddress() {
		try {
			InetAddress addr = InetAddress.getLocalHost();
			return addr.getHostAddress();
		} catch (UnknownHostException e) {
			SLLogger.getLogger().log(Level.SEVERE, I18N.err(76), e);
			return "<unknown IP address>";
		}
	}

	/**
	 * Refreshes the contents of this view. This method does not need to be
	 * called from the SWT thread.
	 */
	public void refresh() {
		UIJob job = new SLUIJob() {
			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				adjustControlState();
				return Status.OK_STATUS;
			}
		};
		job.schedule();
	}

	private void adjustControlState() {
		f_trafficLight.redraw();
		if (f_teamServer.isRunning()) {
			f_command.setText("Stop Server");
			f_command.setEnabled(true);

			f_status.setText("A <a href=\"open\">team server</a> is running.");
		} else if (f_teamServer.isNotRunning()) {
			f_command.setText("Start Server");
			f_command.setEnabled(true);

			f_status.setText("A team server is not running.");
		} else {
			f_command.setText("...");
			f_command.setEnabled(false);

			f_status.setText("Checking...");
			f_command.getParent().layout();
		}
		f_command.getParent().layout();
	}

	private void doCommand() {
		if (f_teamServer.isRunning()) {
			f_teamServer.stop();
		} else if (f_teamServer.isNotRunning()) {
			if (PreferenceConstants.warnAboutServerStaysRunning()) {
				final ServerStaysRunningWarning dialog = new ServerStaysRunningWarning();
				dialog.open();
			}
			f_teamServer.start();
		} else {
			SLLogger.getLogger().log(Level.SEVERE, I18N.err(64));
		}
	}

	void dispose() {
		f_teamServer.removeObserver(this);
		f_jettyRequestLog.removeObserver(f_jettyRequestLogObserver);
		f_portalLog.removeObserver(f_portalLogObserver);
		f_servicesLog.removeObserver(f_servicesLogObserver);

		f_executor.shutdown();

		f_teamServer.dispose();
		f_jettyRequestLog.dispose();
		f_portalLog.dispose();
		f_servicesLog.dispose();
	}

	void setFocus() {
		f_port.setFocus();
	}

	public void notify(TeamServer server) {
		/*
		 * We are not being called from the SWT thread.
		 */
		refresh();
	}

	private void openInBrowser(final URL url) {
		final String name = "Sierra Server";

		try {
			final IWebBrowser browser = PlatformUI.getWorkbench()
					.getBrowserSupport().createBrowser(
							IWorkbenchBrowserSupport.LOCATION_BAR
									| IWorkbenchBrowserSupport.NAVIGATION_BAR
									| IWorkbenchBrowserSupport.STATUS, name,
							name, name);
			browser.openURL(url);
		} catch (Exception e) {
			SLLogger.getLogger().log(Level.SEVERE, I18N.err(26), e);
		}
	}

	private void updateLogText(final String text) {
		f_logText.setText(text);
		f_logText.setTopIndex(f_logText.getLineCount());
	}
}
