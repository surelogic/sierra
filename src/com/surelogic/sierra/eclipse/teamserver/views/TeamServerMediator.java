package com.surelogic.sierra.eclipse.teamserver.views;

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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Scrollable;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.progress.UIJob;

import com.surelogic.common.eclipse.SLImages;
import com.surelogic.common.eclipse.jobs.SLUIJob;
import com.surelogic.common.i18n.I18N;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.eclipse.teamserver.model.ITeamServerObserver;
import com.surelogic.sierra.eclipse.teamserver.model.TeamServer;

public final class TeamServerMediator implements ITeamServerObserver {

	final Button f_command;
	final Label f_status;
	final Text f_port;
	final Canvas f_trafficLight;
	final Text f_log;

	final TeamServer f_teamServer;

	TeamServerMediator(Button command, Label status, Text port,
			Canvas trafficLight, Text log) {
		f_command = command;
		f_status = status;
		f_port = port;
		f_trafficLight = trafficLight;
		f_log = log;

		f_teamServer = new TeamServer(getPort());
	}

	void init() {
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

		f_teamServer.init();
		f_teamServer.addObserver(this);

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

			f_status.setText("A local server is running...");
		} else if (f_teamServer.isNotRunning()) {
			f_command.setText("Start Server");
			f_command.setEnabled(true);

			f_status.setText("A local server is not running...");
		} else {
			f_command.setText("Please Wait...");
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
			f_teamServer.start();
		} else {
			SLLogger.getLogger().log(Level.SEVERE, I18N.err(64));
		}
	}

	void dispose() {
		f_teamServer.removeObserver(this);
		f_teamServer.dispose();
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
}
