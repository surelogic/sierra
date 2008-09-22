package com.surelogic.sierra.gwt.client.ui.block;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.HasHorizontalAlignment.HorizontalAlignmentConstant;
import com.surelogic.sierra.gwt.client.ui.StyleHelper;
import com.surelogic.sierra.gwt.client.ui.Status;

public abstract class ContentBlock<T extends Widget> extends Composite {
	private final T root;
	private final List<Widget> actions = new ArrayList<Widget>();
	private Status status;
	private final List<ContentBlockListener> listeners = new ArrayList<ContentBlockListener>();

	public ContentBlock(final T root) {
		super();
		this.root = root;
		initWidget(root);
	}

	public abstract String getName();

	public abstract String getSummary();

	public HorizontalAlignmentConstant getHorizontalAlignment() {
		return HasHorizontalAlignment.ALIGN_LEFT;
	}

	public List<Widget> getActions() {
		return Collections.unmodifiableList(actions);
	}

	public final Status getStatus() {
		return status;
	}

	public boolean isState(final Status.State state) {
		return status != null && status.getState() == state;
	}

	public final void addListener(final ContentBlockListener listener) {
		listeners.add(listener);
	}

	public final void removeListener(final ContentBlockListener listener) {
		listeners.remove(listener);
	}

	protected final T getRoot() {
		return root;
	}

	protected final void addAction(final Widget w) {
		actions.add(w);
	}

	protected final void addAction(final String text,
			final ClickListener clickListener) {
		actions.add(StyleHelper
				.clickable(new Label(text, false), clickListener));
	}

	protected final void removeAction(final Widget w) {
		actions.remove(w);
	}

	protected final void clearActions() {
		actions.clear();
	}

	protected final void setStatus(final Status status) {
		this.status = status;
	}

	protected final void fireRefresh() {
		for (final ContentBlockListener listener : listeners) {
			listener.onRefresh(this);
		}
	}

	public static interface ContentBlockListener {

		void onRefresh(ContentBlock<?> sender);

	}

}
