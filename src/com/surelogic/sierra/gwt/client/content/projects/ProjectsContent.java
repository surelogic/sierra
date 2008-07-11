package com.surelogic.sierra.gwt.client.content.projects;

import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.PopupListener;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.surelogic.sierra.gwt.client.Context;
import com.surelogic.sierra.gwt.client.ListContentComposite;
import com.surelogic.sierra.gwt.client.data.Project;
import com.surelogic.sierra.gwt.client.data.ScanFilter;
import com.surelogic.sierra.gwt.client.data.Status;
import com.surelogic.sierra.gwt.client.data.cache.Cache;
import com.surelogic.sierra.gwt.client.data.cache.CacheListener;
import com.surelogic.sierra.gwt.client.data.cache.CacheListenerAdapter;
import com.surelogic.sierra.gwt.client.data.cache.ProjectCache;
import com.surelogic.sierra.gwt.client.data.cache.ScanFilterCache;
import com.surelogic.sierra.gwt.client.util.LangUtil;

public class ProjectsContent extends
		ListContentComposite<Project, ProjectCache> {
	private static final ProjectsContent instance = new ProjectsContent();
	private final ProjectView projectView = new ProjectView();
	private CacheListener<ScanFilter> scanFilterListener;

	public static ProjectsContent getInstance() {
		return instance;
	}

	private ProjectsContent() {
		super(new ProjectCache());
		// singleton
	}

	@Override
	protected void onInitialize(DockPanel rootPanel,
			VerticalPanel selectionPanel) {
		setCaption("Projects");

		projectView.initialize();
		projectView.addScanFilterAction("Change Scan Filter",
				new ClickListener() {

					public void onClick(Widget sender) {
						promptForScanFilter();
					}
				});
		selectionPanel.add(projectView);

		scanFilterListener = new CacheListenerAdapter<ScanFilter>() {

			@Override
			public void onRefresh(Cache<ScanFilter> cache, Throwable failure) {
				updateSelection(getSelection());
			}

			@Override
			public void onItemUpdate(Cache<ScanFilter> cache, ScanFilter item,
					Status status, Throwable failure) {
				updateSelection(getSelection());
			}

		};
	}

	@Override
	protected void onUpdate(Context context) {
		if (!isActive()) {
			ScanFilterCache.getInstance().addListener(scanFilterListener);
		}
		super.onUpdate(context);
	}

	@Override
	protected void onDeactivate() {
		ScanFilterCache.getInstance().removeListener(scanFilterListener);

		super.onDeactivate();
	}

	@Override
	protected String getItemText(Project item) {
		return item.getName();
	}

	@Override
	protected boolean isMatch(Project item, String query) {
		return LangUtil.containsIgnoreCase(item.getName(), query);
	}

	@Override
	protected void onSelectionChanged(Project item) {
		ScanFilterCache.getInstance().refresh(false);

		updateSelection(item);
	}

	private void updateSelection(Project item) {
		projectView.setSelection(item);
	}

	private void promptForScanFilter() {
		final ScanFilterDialog dialog = new ScanFilterDialog();
		dialog.addPopupListener(new PopupListener() {

			public void onPopupClosed(PopupPanel sender, boolean autoClosed) {
				final Status s = dialog.getStatus();
				if (s != null && s.isSuccess()) {
					final Project project = projectView.getSelection();
					project.setScanFilter(dialog.getSelectedFilter());
					getCache().save(project);
					getCache().refresh();
				}
			}

		});
		dialog.center();
		dialog.setScanFilter(projectView.getScanFilter());
	}
}
