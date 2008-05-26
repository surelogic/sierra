package com.surelogic.sierra.gwt.client.rules;

import java.util.ArrayList;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.surelogic.sierra.gwt.client.ContentComposite;
import com.surelogic.sierra.gwt.client.Context;
import com.surelogic.sierra.gwt.client.ContextManager;
import com.surelogic.sierra.gwt.client.data.Cache;
import com.surelogic.sierra.gwt.client.data.CacheListenerAdapter;
import com.surelogic.sierra.gwt.client.data.Cacheable;
import com.surelogic.sierra.gwt.client.data.Category;
import com.surelogic.sierra.gwt.client.data.Status;
import com.surelogic.sierra.gwt.client.service.ResultCallback;
import com.surelogic.sierra.gwt.client.service.ServiceHelper;
import com.surelogic.sierra.gwt.client.ui.ActionPanel;
import com.surelogic.sierra.gwt.client.ui.BlockPanel;
import com.surelogic.sierra.gwt.client.ui.EditableListenerAdapter;
import com.surelogic.sierra.gwt.client.ui.StyledButton;
import com.surelogic.sierra.gwt.client.util.ImageHelper;
import com.surelogic.sierra.gwt.client.util.UI;

public class CategoriesContent extends ContentComposite {
	private static final CategoriesContent instance = new CategoriesContent();
	private final CategoryCache categories = new CategoryCache();
	private final ActionBlock actionBlock = new ActionBlock();
	private final SearchBlock searchBlock = new SearchBlock(categories);
	private final CategoryBlock categoryBlock = new CategoryBlock();

	public static CategoriesContent getInstance() {
		return instance;
	}

	private CategoriesContent() {
		// singleton
	}

	@Override
	protected void onInitialize(DockPanel rootPanel) {
		final Label title = UI.h2("Categories");
		rootPanel.add(title, DockPanel.NORTH);
		rootPanel.setCellHorizontalAlignment(title, DockPanel.ALIGN_LEFT);

		final VerticalPanel westPanel = new VerticalPanel();
		westPanel.setWidth("100%");
		rootPanel.add(westPanel, DockPanel.WEST);
		rootPanel.setCellWidth(westPanel, "25%");

		actionBlock.initialize();
		westPanel.add(actionBlock);

		searchBlock.initialize();
		westPanel.add(searchBlock);

		categoryBlock.initialize();
		categoryBlock.addListener(new EditableListenerAdapter() {

			@Override
			public void onSave(Widget sender, Object item) {
				categories.save((Category) item);
			}
		});
		rootPanel.add(categoryBlock, DockPanel.CENTER);
		rootPanel.setCellWidth(categoryBlock, "75%");

		categories.addListener(new CacheListenerAdapter() {

			@Override
			public void onRefresh(Cache cache, Throwable failure) {
				searchBlock.refresh();
				refreshSelection(ContextManager.getContext());
			}

			@Override
			public void onItemUpdate(Cache cache, Cacheable item,
					Status status, Throwable failure) {
				categories.refresh();

				if (failure == null && status.isSuccess()) {
					new CategoriesContext((Category) item).updateContext();
				} else if (!status.isSuccess()) {
					Window.alert("Save rejected: " + status.getMessage());
				} else if (failure != null) {
					Window.alert("Save failed: " + failure.getMessage());
				}
			}

		});
	}

	@Override
	protected void onUpdate(Context context) {
		if (!isActive()) {
			categories.refresh();
		} else {
			refreshSelection(context);
		}
	}

	@Override
	protected void onDeactivate() {
		searchBlock.clear();
	}

	private void refreshSelection(Context context) {
		final CategoriesContext rulesCtx = new CategoriesContext(context);
		final Category cat = (Category) categories.getItem(rulesCtx
				.getCategory());
		if (cat != null) {
			searchBlock.setSelection(cat);
			categoryBlock.setSelection(cat);
		} else if (categories.getItemCount() > 0) {
			new CategoriesContext((Category) categories.getItem(0))
					.updateContext();
		}
	}

	private class ActionBlock extends BlockPanel {
		private final StyledButton createCategoryButton = new StyledButton(
				"Create a Category");
		private final VerticalPanel categoryCreatePanel = new VerticalPanel();
		private final ActionPanel categoryActions = new ActionPanel();
		private final FlexTable fieldTable = new FlexTable();
		private final TextBox categoryName = new TextBox();
		private final Image waitImage = ImageHelper.getWaitImage(16);

		@Override
		protected void onInitialize(VerticalPanel contentPanel) {
			createCategoryButton.addClickListener(new ClickListener() {

				public void onClick(Widget sender) {
					toggleCreateCategory();
				}
			});
			contentPanel.add(createCategoryButton);

			categoryCreatePanel.setWidth("100%");
			fieldTable.setWidth("100%");
			fieldTable.setText(0, 0, "Name:");
			fieldTable.setWidget(0, 1, categoryName);
			categoryCreatePanel.add(fieldTable);

			categoryActions.addAction("Save", new ClickListener() {

				public void onClick(Widget sender) {
					createCategory(categoryName.getText());
				}
			});
			categoryActions.addAction("Cancel", new ClickListener() {

				public void onClick(Widget sender) {
					toggleCreateCategory();
				}
			});
			categoryCreatePanel.add(categoryActions);
			categoryCreatePanel.setCellHorizontalAlignment(categoryActions,
					HasHorizontalAlignment.ALIGN_RIGHT);
		}

		private void toggleCreateCategory() {
			final VerticalPanel contentPanel = getContentPanel();
			if (contentPanel.getWidgetIndex(categoryCreatePanel) != -1) {
				contentPanel.remove(categoryCreatePanel);
			} else {
				categoryName.setText("");
				if (categoryCreatePanel.getWidgetIndex(categoryActions) == -1) {
					categoryCreatePanel.add(categoryActions);
				}
				if (categoryCreatePanel.getWidgetIndex(waitImage) != -1) {
					categoryCreatePanel.remove(waitImage);
				}
				int panelIndex = contentPanel
						.getWidgetIndex(createCategoryButton);
				contentPanel.insert(categoryCreatePanel, panelIndex + 1);
			}
		}

		private void createCategory(String name) {
			categoryActions.removeFromParent();
			categoryCreatePanel.add(waitImage);

			ServiceHelper.getSettingsService().createCategory(name,
					new ArrayList(), new ArrayList(),
					new ResultCallback<String>() {

						protected void doFailure(String message, String result) {
							Window
									.alert("Category creation failed: "
											+ message);
							categoryCreatePanel.remove(waitImage);
							categoryCreatePanel.add(categoryActions);
						}

						protected void doSuccess(String message, String result) {
							toggleCreateCategory();
							categories.refresh();
							new CategoriesContext(result).updateContext();
						}
					});
		}
	}

}
