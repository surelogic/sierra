package com.surelogic.sierra.client.eclipse.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.part.PageBook;
import org.eclipse.ui.part.ViewPart;

import com.surelogic.adhoc.Activator;

public final class FindingsGraphView extends ViewPart {

	static final String NO_FINDINGS = "No findings selected ... please select a set of findings in the <A HREF=\"iv\">Investigate Findings</A> view";

	static final Listener NO_FINDINGS_LISTENER = new Listener() {
		public void handleEvent(Event event) {
			final String name = event.text;
			if (name != null) {
				if (name.equals("iv")) {
					Activator.showView(FindingsFinderView.class.getName());
				}
			}
		}
	};

	@Override
	public void createPartControl(Composite parent) {
		final PageBook pages = new PageBook(parent, SWT.NONE);

		final Link noFindingsPage = new Link(pages, SWT.NONE);
		noFindingsPage.setText(NO_FINDINGS);
		noFindingsPage.addListener(SWT.Selection, NO_FINDINGS_LISTENER);

		pages.showPage(noFindingsPage);
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}
}
