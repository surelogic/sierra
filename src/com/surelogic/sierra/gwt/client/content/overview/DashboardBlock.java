package com.surelogic.sierra.gwt.client.content.overview;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.surelogic.sierra.gwt.client.ui.Direction;
import com.surelogic.sierra.gwt.client.ui.ImageHelper;
import com.surelogic.sierra.gwt.client.ui.panel.BlockPanel;

public class DashboardBlock extends BlockPanel {
	private final HorizontalPanel movementActions = new HorizontalPanel();
	private final List<DashboardListener> listeners = new ArrayList<DashboardListener>();

	@Override
	protected void onInitialize(final VerticalPanel contentPanel) {
		movementActions.add(createArrowImage(Direction.UP));
		movementActions.add(createArrowImage(Direction.DOWN));
		movementActions.add(createArrowImage(Direction.LEFT));
		movementActions.add(createArrowImage(Direction.RIGHT));
	}

	public void setEditMode(final boolean editing) {
		removeActions();
		if (editing) {
			addAction(movementActions);
			addAction("Remove", new ClickListener() {

				public void onClick(final Widget sender) {
					for (final DashboardListener listener : listeners) {
						listener.onRemove(DashboardBlock.this);
					}
				}
			});
		}
		for (final DashboardListener listener : listeners) {
			listener.onEditingChange(this, editing);
		}
	}

	private Image createArrowImage(final Direction direction) {
		return ImageHelper.getArrowImage(direction, new ClickListener() {

			public void onClick(final Widget sender) {
				for (final DashboardListener listener : listeners) {
					listener.onMove(DashboardBlock.this, direction);
				}
			}
		});
	}

	public static interface DashboardListener {

		void onMove(DashboardBlock block, Direction direction);

		void onRemove(DashboardBlock block);

		void onEditingChange(DashboardBlock block, boolean editing);

	}
}
