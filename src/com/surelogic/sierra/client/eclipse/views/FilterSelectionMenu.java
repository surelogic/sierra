package com.surelogic.sierra.client.eclipse.views;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;

import com.surelogic.common.eclipse.SLImages;
import com.surelogic.sierra.client.eclipse.model.selection.ISelectionFilterFactory;

public final class FilterSelectionMenu {

	private final Composite f_panel;

	public Composite getPanel() {
		return f_panel;
	}

	FilterSelectionMenu(List<ISelectionFilterFactory> choices, Composite panel) {
		assert choices != null;
		if (choices.size() < 1)
			throw new IllegalArgumentException(
					"a filter selection menu must have a choice");

		assert panel != null;
		f_panel = panel;
		final RowLayout layout = new RowLayout(SWT.VERTICAL);
		layout.fill = true;
		panel.setLayout(layout);

		for (ISelectionFilterFactory filter : choices) {
			constructFilterSelector(filter.getFilterLabel(), null, panel,
					filter);
		}
	}

	public boolean isEnabled() {
		return f_panel.isEnabled();
	}

	public void setEnabled(boolean enabled) {
		f_panel.setEnabled(enabled);
	}

	public interface ISelectionMenuObserver {
		void filterSelected(ISelectionFilterFactory filter,
				FilterSelectionMenu menu);
	}

	private final Set<ISelectionMenuObserver> f_selectionObservers = new CopyOnWriteArraySet<ISelectionMenuObserver>();

	public void addObserver(ISelectionMenuObserver o) {
		f_selectionObservers.add(o);
	}

	public void removeObserver(ISelectionMenuObserver o) {
		f_selectionObservers.remove(o);
	}

	private void notifyObservers(ISelectionFilterFactory filter) {
		for (ISelectionMenuObserver o : f_selectionObservers)
			o.filterSelected(filter, this);
	}

	private Composite constructFilterSelector(String text, Image image,
			Composite parent, final ISelectionFilterFactory filter) {
		final Composite result = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.numColumns = 3;
		result.setLayout(layout);

		final Label prefixImage = new Label(result, SWT.NONE);
		prefixImage.setImage(image);
		prefixImage.setLayoutData(new GridData(SWT.DEFAULT, SWT.CENTER, false,
				false));
		final Label textLabel = new Label(result, SWT.LEFT);
		textLabel.setText(text);
		textLabel
				.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false));
		final Label arrowImage = new Label(result, SWT.NONE);
		arrowImage.setImage(SLImages.getImage(SLImages.IMG_RIGHT_ARROW_SMALL));
		arrowImage.setLayoutData(new GridData(SWT.DEFAULT, SWT.CENTER, false,
				false));

		textLabel.addListener(SWT.MouseDown, new Listener() {
			public void handleEvent(Event event) {
				if (event.widget instanceof Control) {
					Control c = (Control) event.widget;
					selected(c.getParent(), filter);
				}
			}
		});
		return result;
	}

	private ISelectionFilterFactory f_selected = null;

	private void selected(Composite button, ISelectionFilterFactory filter) {
		if (f_selected != null) {
			if (f_selected == filter)
				return;
			// clear past menu choice (by clearing all choices)
			for (Control c : f_panel.getChildren()) {
				if (c instanceof Composite) {
					unhighlight((Composite) c);
				}
			}
		}
		f_selected = filter;
		highlight(button);
		notifyObservers(filter);
	}

	void clearSelection() {
		for (Control c : f_panel.getChildren()) {
			if (c instanceof Composite) {
				unhighlight((Composite) c);
			}
		}
		f_selected = null;
	}

	private void highlight(Composite button) {
		final Display display = button.getShell().getDisplay();
		button.setBackground(display.getSystemColor(SWT.COLOR_LIST_SELECTION));
		for (Control c : button.getChildren()) {
			c.setBackground(display.getSystemColor(SWT.COLOR_LIST_SELECTION));
		}
	}

	private void unhighlight(Composite button) {
		button.setBackground(null);
		for (Control c : button.getChildren()) {
			c.setBackground(null);
		}
	}
}
