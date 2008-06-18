package com.surelogic.sierra.gwt.client.ui;

import java.util.EventListener;

import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public class PagingPanel extends Composite {
	private final PageListener pageListener;
	private final DockPanel rootPanel = new DockPanel();
	private final Label pageText = new Label("", false);
	private final StyledButton previousPage;
	private final StyledButton nextPage;
	private int pageIndex;
	private int pageCount;

	public PagingPanel(PageListener pageListener) {
		super();
		this.pageListener = pageListener;
		initWidget(rootPanel);
		rootPanel.setWidth("100%");
		rootPanel.setVerticalAlignment(DockPanel.ALIGN_MIDDLE);

		pageText.addStyleName("sl-PagingPanel-text");
		rootPanel.add(pageText, DockPanel.CENTER);
		rootPanel.setCellHorizontalAlignment(pageText,
				HorizontalPanel.ALIGN_CENTER);

		previousPage = new StyledButton("<<", new ClickListener() {

			public void onClick(Widget sender) {
				previousPage();
			}
		});
		previousPage.addStyleName("sl-PagingPanel-button");
		rootPanel.add(previousPage, DockPanel.WEST);

		nextPage = new StyledButton(">>", new ClickListener() {

			public void onClick(Widget sender) {
				nextPage();
			}
		});
		nextPage.addStyleName("sl-PagingPanel-button");
		rootPanel.add(nextPage, DockPanel.EAST);

		updateState(0, 0);
	}

	public int getPageIndex() {
		return pageIndex;
	}

	public void setPageIndex(int pageIndex) {
		updateState(pageIndex, pageCount);
	}

	public int getPageCount() {
		return pageCount;
	}

	public void setPageCount(int pageCount) {
		updateState(pageIndex, pageCount);
	}

	public void setPaging(int pageIndex, int pageCount) {
		updateState(pageIndex, pageCount);
	}

	private void nextPage() {
		updateState(pageIndex + 1, pageCount);
	}

	private void previousPage() {
		updateState(pageIndex - 1, pageCount);
	}

	private void updateState(int pageIndex, int pageCount) {
		if (pageIndex == this.pageIndex && pageCount == this.pageCount) {
			return;
		}
		this.pageCount = pageCount;
		this.pageIndex = pageIndex;
		if (pageIndex < 0) {
			pageIndex = 0;
		} else if (pageIndex >= pageCount) {
			pageIndex = pageCount - 1;
		}

		final StringBuffer buf = new StringBuffer("Page ");
		buf.append(pageIndex + 1).append(" of ");
		if (pageCount < 1) {
			buf.append(1);
		} else {
			buf.append(pageCount);
		}
		pageText.setText(buf.toString());
		nextPage.setEnabled(pageIndex < pageCount - 1);
		previousPage.setEnabled(pageIndex > 0);
		pageListener.onPageChange(this, pageIndex, pageCount);
	}

	public static interface PageListener extends EventListener {

		void onPageChange(PagingPanel sender, int pageIndex, int pageCount);

	}
}
