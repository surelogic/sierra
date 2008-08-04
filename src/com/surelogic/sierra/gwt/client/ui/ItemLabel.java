package com.surelogic.sierra.gwt.client.ui;

import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.MouseListener;
import com.google.gwt.user.client.ui.Widget;
import com.surelogic.sierra.gwt.client.util.LangUtil;

public class ItemLabel<T> extends FocusPanel implements HasItem<T> {
	private static final String PRIMARY_STYLE = "sl-ItemLabel";
	private final HorizontalPanel rootPanel = new HorizontalPanel();
	private final Label label = new Label();
	private SelectionTracker<ItemLabel<T>> selectionTracker;
	private T item;
	private boolean mouseOver;
	private boolean selected;

	public ItemLabel(String text, T item, final ClickListener listener) {
		super();
		this.item = item;

		setWidget(rootPanel);

		rootPanel.addStyleName(PRIMARY_STYLE);
		rootPanel.setWidth("100%");

		label.setText(text);
		label.addStyleName(PRIMARY_STYLE + "-text");
		rootPanel.add(label);

		label.addClickListener(new ClickListener() {

			public void onClick(Widget sender) {
				setSelected(true);
				if (listener != null) {
					listener.onClick(sender);
				}
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

	public T getItem() {
		return item;
	}

	public void setItem(T item) {
		this.item = item;
	}

	public void setDecorator(Widget decorator, boolean alignRight) {
		final int panelCount = rootPanel.getWidgetCount();
		final int labelIndex = rootPanel.getWidgetIndex(label);
		if (alignRight) {
			if (labelIndex < panelCount - 1) {
				rootPanel.remove(panelCount - 1);
			}
			if (decorator != null) {
				rootPanel.add(decorator);
				rootPanel.setCellHorizontalAlignment(decorator,
						HasHorizontalAlignment.ALIGN_RIGHT);
			}
		} else {
			if (labelIndex > 0) {
				rootPanel.remove(0);
			}
			if (decorator != null) {
				rootPanel.insert(decorator, 0);
			}
		}
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
					final ItemLabel<T> lastSelection = selectionTracker
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

	public SelectionTracker<ItemLabel<T>> getSelectionTracker() {
		return selectionTracker;
	}

	public void setSelectionTracker(
			SelectionTracker<ItemLabel<T>> selectionTracker) {
		this.selectionTracker = selectionTracker;
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
