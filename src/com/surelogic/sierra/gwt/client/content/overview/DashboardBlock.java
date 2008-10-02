package com.surelogic.sierra.gwt.client.content.overview;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.HasHorizontalAlignment.HorizontalAlignmentConstant;
import com.surelogic.sierra.gwt.client.ui.ImageHelper;
import com.surelogic.sierra.gwt.client.ui.block.ContentBlock;
import com.surelogic.sierra.gwt.client.ui.type.Direction;

public class DashboardBlock extends ContentBlock<ContentBlock<?>> {

	private final HorizontalPanel movementActions = new HorizontalPanel();
	private final Image moveUp = createArrowImage(Direction.UP);
	private final Image moveDown = createArrowImage(Direction.DOWN);
	private final Image moveLeft = createArrowImage(Direction.LEFT);
	private final Image moveRight = createArrowImage(Direction.RIGHT);
	private final List<DashboardListener> dashboardListeners = new ArrayList<DashboardListener>();
	private boolean editing;

	public DashboardBlock(final ContentBlock<?> content) {
		super(content);

		movementActions.add(moveLeft);
		movementActions.add(moveRight);
		movementActions.add(moveUp);
		movementActions.add(moveDown);

		content.addListener(new ContentBlockListener() {

			public void onRefresh(final ContentBlock<?> sender) {
				setEditMode(editing);
			}
		});
	}

	@Override
	public String getName() {
		return getRoot().getName();
	}

	@Override
	public String getSummary() {
		return getRoot().getSummary();
	}

	@Override
	public HorizontalAlignmentConstant getHorizontalAlignment() {
		return getRoot().getHorizontalAlignment();
	}

	public void setEditMode(final boolean editing) {
		this.editing = editing;

		removeActions();

		if (editing) {
			addAction(movementActions);
			addAction("Remove", new ClickListener() {

				public void onClick(final Widget sender) {
					for (final DashboardListener listener : dashboardListeners) {
						listener.onRemove(DashboardBlock.this);
					}
				}
			});
		} else {
			for (final Widget action : getRoot().getActions()) {
				addAction(action);
			}
		}

		fireRefresh();
	}

	public void updateMovementActions(final Set<Direction> allowedMovements) {
		moveUp.setVisible(allowedMovements.contains(Direction.UP));
		moveDown.setVisible(allowedMovements.contains(Direction.DOWN));
		moveLeft.setVisible(allowedMovements.contains(Direction.LEFT));
		moveRight.setVisible(allowedMovements.contains(Direction.RIGHT));
	}

	private Image createArrowImage(final Direction direction) {
		return ImageHelper.getArrowImage(direction, new ClickListener() {

			public void onClick(final Widget sender) {
				for (final DashboardListener listener : dashboardListeners) {
					listener.onMove(DashboardBlock.this, direction);
				}
			}
		});
	}

	public void addDashboardListener(final DashboardListener listener) {
		dashboardListeners.add(listener);
	}

	public void removeDashboardListener(final DashboardListener listener) {
		dashboardListeners.remove(listener);
	}

	public static interface DashboardListener {

		void onMove(DashboardBlock block, Direction direction);

		void onRemove(DashboardBlock block);

	}

}
