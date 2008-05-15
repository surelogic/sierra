package com.surelogic.sierra.gwt.client.ui;

import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.MouseListener;
import com.google.gwt.user.client.ui.Widget;
import com.surelogic.sierra.gwt.client.util.LangUtil;

public class ItemLabel extends Label {
	private static final String PRIMARY_STYLE = "sl-ItemLabel";
	private final SelectionTracker selectionTracker;
	private Object item;
	private boolean mouseOver;
	private boolean selected;

	public ItemLabel(String text, Object item, ClickListener listener) {
		this(text, item, null, listener);
	}

	public ItemLabel(String text, Object item,
			SelectionTracker selectionTracker, final ClickListener listener) {
		super(text);
		this.item = item;
		this.selectionTracker = selectionTracker;
		setStyleName(PRIMARY_STYLE);

		addClickListener(new ClickListener() {

			public void onClick(Widget sender) {
				setSelected(true);
				listener.onClick(sender);
			}
		});

		addMouseListener(new MouseListener() {

			public void onMouseDown(Widget sender, int x, int y) {
				// nothing to do
			}

			public void onMouseEnter(Widget sender) {
				mouseOver();
			}

			public void onMouseLeave(Widget sender) {
				mouseNotOver();
			}

			public void onMouseMove(Widget sender, int x, int y) {
				mouseOver();
			}

			public void onMouseUp(Widget sender, int x, int y) {
				// nothing to do
			}
		});
	}

	public Object getItem() {
		return item;
	}

	public void setItem(Object item) {
		this.item = item;
	}

	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		if (!this.selected && selected) {
			addStyleName(PRIMARY_STYLE + "-selected");
		} else if (this.selected && !selected) {
			removeStyleName(PRIMARY_STYLE + "-selected");
		}
		this.selected = selected;

		if (selectionTracker != null) {
			if (selected) {
				if (!LangUtil.equals(this, selectionTracker.getSelected())) {
					ItemLabel lastSelection = (ItemLabel) selectionTracker
							.setSelected(this);
					if (lastSelection != null) {
						lastSelection.setSelected(false);
					}
				}
			} else {
				if (LangUtil.equals(this, selectionTracker.getSelected())) {
					selectionTracker.setSelected(null);
				}
			}
		}
	}

	private void mouseOver() {
		if (!mouseOver) {
			if (!selected) {
				addStyleName(PRIMARY_STYLE + "-over");
			}
			mouseOver = true;
		}
	}

	private void mouseNotOver() {
		if (mouseOver) {
			removeStyleName(PRIMARY_STYLE + "-over");
			mouseOver = false;
		}
	}
}
