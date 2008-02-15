package com.surelogic.sierra.client.eclipse.views.selection;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;

import com.surelogic.common.eclipse.StringUtility;

public final class FilterSelectionReportLine {
  public static class Factory {
    private final Composite parent;
    private final Color f_barColorDark;
    private final Color f_barColorLight;
    
    Factory(Composite parent) {
      this.parent = parent;
      f_barColorDark = new Color(parent.getDisplay(), 255, 113, 18);
      f_barColorLight = new Color(parent.getDisplay(), 238, 216, 198);
    }
    
    FilterSelectionReportLine create(String text,
        Image image, int count, int total) {
      return new FilterSelectionReportLine(parent, text, image, count, total, f_barColorDark, f_barColorLight);
    }
    
    void dispose() {
      f_barColorDark.dispose();
      f_barColorLight.dispose();
    }
  }
  
	private final Composite f_lineComposite;

	private final Color f_barColorDark;

	private final Color f_barColorLight;

	public void dispose() {
		f_lineComposite.dispose();
		/* Handled by factory
		f_barColorDark.dispose();
		f_barColorLight.dispose();
		*/
	}

	private final Button f_check;

	public String getText() {
		return f_check.getText();
	}

	public void setText(String text) {
		f_check.setText(text);
	}

	public boolean getSelection() {
		return f_check.getSelection();
	}

	public void setSelection(boolean selected) {
		f_check.setSelection(selected);
	}

	private final Canvas f_countGraph;

	private boolean f_mouseOverGraph = false;

	private int f_count;

	public int getCount() {
		return f_count;
	}

	public void setCount(int count) {
		if (count != f_count) {
			f_count = count;
			f_countGraph.redraw();
		}
	}

	private int f_total;

	public int getTotal() {
		return f_total;
	}

	public void setTotal(int total) {
		if (total != f_total) {
			f_total = total;
			f_countGraph.redraw();
		}
	}

	FilterSelectionReportLine(Composite parent, String text,
			Image image, int count, int total, Color dark, Color light) {
		f_lineComposite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.numColumns = 2;
		f_lineComposite.setLayout(layout);

		f_barColorDark = dark; //new Color(f_lineComposite.getDisplay(), 255, 113, 18);
		f_barColorLight = light; //new Color(f_lineComposite.getDisplay(), 238, 216, 198);

		f_check = new Button(f_lineComposite, SWT.CHECK);
		f_check.setText(text);
		f_check.setImage(image);
		final Point bSize = f_check.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		f_check.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		f_check.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				f_countGraph.redraw();
				notifySelectionChanged();
			}
		});

		f_countGraph = new Canvas(f_lineComposite, SWT.NONE) {
			@Override
			public Point computeSize(int hint, int hint2, boolean changed) {
				return new Point(75, bSize.y);
			}
		};
		f_count = count;
		f_total = total;
		f_countGraph.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false,
				false));
		f_countGraph.addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent e) {
				final Canvas c = (Canvas) e.widget;
				final Display display = f_lineComposite.getDisplay();
				Point cSize = c.computeSize(SWT.DEFAULT, SWT.DEFAULT);
				GC gc = e.gc;
				Color foreground = gc.getForeground();
				Color background = gc.getBackground();
				int percent = (int) (((double) f_count / (double) f_total) * 100);
				int width = (cSize.x - 1) * percent / 100;
				if (width < 2 && f_count > 0)
					width = 2;
				if (f_mouseOverGraph) {
					gc.setBackground(display.getSystemColor(SWT.COLOR_WHITE));
					gc.fillRectangle(0, 0, cSize.x, cSize.y);
					gc.setBackground(f_barColorLight);
					gc.fillRectangle(0, 0, width, cSize.y);
				} else {
					gc.setBackground(f_barColorDark);
					gc.fillRectangle(0, 0, width, cSize.y);
				}
				Rectangle rect2 = new Rectangle(0, 0, cSize.x - 1, cSize.y - 1);
				gc.setForeground(display.getSystemColor(SWT.COLOR_GRAY));
				gc.drawRectangle(rect2);
				if (percent > 25) {
					int p = (cSize.x - 1) * 25 / 100;
					gc.drawLine(p, 0, p, cSize.y - 1);
				}
				if (percent > 50) {
					int p = (cSize.x - 1) * 50 / 100;
					gc.drawLine(p, 0, p, cSize.y - 1);
				}
				if (percent > 75) {
					int p = (cSize.x - 1) * 75 / 100;
					gc.drawLine(p, 0, p, cSize.y - 1);
				}
				String text = StringUtility.toCommaSepString(f_count);
				Point size = e.gc.textExtent(text);
				int offset = Math.max(0, (cSize.y - size.y) / 2);
				int rightJ = cSize.x - 2 - size.x;
				if (f_mouseOverGraph  || f_check.getSelection()) {
					gc.setForeground(display.getSystemColor(SWT.COLOR_BLACK));
				}
				gc.drawText(text, rightJ, 0 + offset, true);
				gc.setForeground(background);
				gc.setBackground(foreground);
			}
		});
		f_countGraph.addListener(SWT.MouseEnter, new Listener() {
			public void handleEvent(Event event) {
				f_mouseOverGraph = true;
				f_countGraph.redraw();
			}
		});
		f_countGraph.addListener(SWT.MouseExit, new Listener() {
			public void handleEvent(Event event) {
				f_mouseOverGraph = false;
				f_countGraph.redraw();
			}
		});
		f_countGraph.addListener(SWT.MouseDown, new Listener() {
			public void handleEvent(Event event) {
				/*
				 * Toggle the selection at this line, this doesn't send an event
				 * so we need to call the observers manually;
				 */
				f_check.setSelection(!f_check.getSelection());
				notifySelectionChanged();
			}
		});
	}

	public interface ISelectionChangedObserver {
		/**
		 * The selection of the filter selection report line changed. It was
		 * either checked or unchecked in the user interface.
		 * <p>
		 * This call will be made within the SWT thread.
		 * 
		 * @param line
		 *            the filter selection report line that had its selection
		 *            state changed.
		 */
		void selectionChanged(FilterSelectionReportLine line);
	}

	private final Set<ISelectionChangedObserver> f_observers = new CopyOnWriteArraySet<ISelectionChangedObserver>();

	public final void addObserver(ISelectionChangedObserver o) {
		if (o == null)
			return;
		f_observers.add(o);
	}

	public final void removeObserver(ISelectionChangedObserver o) {
		f_observers.remove(o);
	}

	protected void notifySelectionChanged() {
		for (ISelectionChangedObserver o : f_observers) {
			o.selectionChanged(this);
		}
	}

	public void setMenu(Menu menu) {
		f_lineComposite.setMenu(menu);
		f_check.setMenu(menu);
		f_countGraph.setMenu(menu);
	}
}
