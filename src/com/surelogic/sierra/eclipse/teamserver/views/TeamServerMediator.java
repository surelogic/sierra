package com.surelogic.sierra.eclipse.teamserver.views;

import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Scrollable;
import org.eclipse.swt.widgets.Text;

import com.surelogic.common.eclipse.SLImages;

public final class TeamServerMediator {

	final Button f_command;
	final Label f_status;
	final Text f_port;
	final Canvas f_trafficLight;
	final Text f_log;

	TeamServerMediator(Button command, Label status, Text port,
			Canvas trafficLight, Text log) {
		f_command = command;
		f_status = status;
		f_port = port;
		f_trafficLight = trafficLight;
		f_log = log;
	}

	void init() {
		f_trafficLight.addPaintListener(new PaintListener() {
			@Override
			public void paintControl(PaintEvent e) {
				final Image trafficLightImage = getTrafficLightImage();
				if (e.widget instanceof Scrollable) {
					final Rectangle r = ((Scrollable) e.widget).getClientArea();
					System.out.println(r);
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
					System.out.println("dw=" + drawWidth + " dh=" + drawHeight
							+ " dY=" + drawY);
					e.gc.drawImage(trafficLightImage, 0, 0, lightWidth,
							lightHeight, drawX, drawY, drawWidth, drawHeight);
				}
			}
		});

	}

	private Image getTrafficLightImage() {
		return SLImages.getImage(SLImages.IMG_TRAFFIC_LIGHT_YELLOW);
	}

	void dispose() {
		// nothing to do
	}

	void setFocus() {
		f_port.setFocus();
	}
}
