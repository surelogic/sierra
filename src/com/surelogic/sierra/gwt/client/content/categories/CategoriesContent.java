package com.surelogic.sierra.gwt.client.content.categories;

import java.util.ArrayList;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.PopupListener;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.surelogic.sierra.gwt.client.ContentComposite;
import com.surelogic.sierra.gwt.client.Context;
import com.surelogic.sierra.gwt.client.ContextManager;
import com.surelogic.sierra.gwt.client.content.categories.CategoryEditor.FindingsEditor;
import com.surelogic.sierra.gwt.client.data.Cache;
import com.surelogic.sierra.gwt.client.data.CacheListenerAdapter;
import com.surelogic.sierra.gwt.client.data.Category;
import com.surelogic.sierra.gwt.client.data.Status;
import com.surelogic.sierra.gwt.client.service.ResultCallback;
import com.surelogic.sierra.gwt.client.service.ServiceHelper;
import com.surelogic.sierra.gwt.client.ui.ActionPanel;
import com.surelogic.sierra.gwt.client.ui.BlockPanel;
import com.surelogic.sierra.gwt.client.ui.SearchBlock;
import com.surelogic.sierra.gwt.client.ui.StyledButton;
import com.surelogic.sierra.gwt.client.util.ImageHelper;
import com.surelogic.sierra.gwt.client.util.LangUtil;

public class CategoriesContent extends ContentComposite {
	private static final CategoriesContent instance = new CategoriesContent();
	private final CategoryCache categories = new CategoryCache();
	private final ActionBlock actionBlock = new ActionBlock();
	private final CategorySearchBlock searchBlock = new CategorySearchBlock(
			categories);
	private final VerticalPanel selectionPanel = new VerticalPanel();
	private final CategoryView categoryView = new CategoryView();
	private final CategoryEditor categoryEditor = new CategoryEditor();

	public static CategoriesContent getInstance() {
		return instance;
	}

	private CategoriesContent() {
		// singleton
	}

	@Override
	protected void onInitialize(DockPanel rootPanel) {
		setCaption("Categories");

		final VerticalPanel westPanel = new VerticalPanel();
		westPanel.setWidth("100%");
		rootPanel.add(westPanel, DockPanel.WEST);
		rootPanel.setCellWidth(westPanel, "25%");

		actionBlock.initialize();
		westPanel.add(actionBlock);

		searchBlock.initialize();
		westPanel.add(searchBlock);

		categoryView.initialize();
		categoryView.addAction("Edit", new ClickListener() {

			public void onClick(Widget sender) {
				setCategory(categoryView.getCategory(), true);
			}
		});
		categoryView.addAction("Delete", new ClickListener() {

			public void onClick(Widget sender) {
				deleteCategory(categoryView.getCategory());
			}
		});

		categoryEditor.initialize();
		categoryEditor.addAction("Save", new ClickListener() {

			public void onClick(Widget sender) {
				categories.save(categoryEditor.getUpdatedCategory());
			}
		});
		categoryEditor.addAction("Cancel", new ClickListener() {

			public void onClick(Widget sender) {
				setCategory(categoryEditor.getCategory(), false);
			}
		});

		final FindingsEditor findingsEditor = categoryEditor
				.getFindingsEditor();
		findingsEditor.addAction("Add Finding", new ClickListener() {

			public void onClick(Widget sender) {
				promptForFindings(categoryEditor.getCategory());
			}
		});

		selectionPanel.setWidth("100%");
		rootPanel.add(selectionPanel, DockPanel.CENTER);
		rootPanel.setCellWidth(selectionPanel, "75%");

		categories.addListener(new CacheListenerAdapter<Category>() {

			@Override
			public void onRefresh(Cache<Category> cache, Throwable failure) {
				refreshContext(ContextManager.getContext());
			}

			@Override
			public void onItemUpdate(Cache<Category> cache, Category item,
					Status status, Throwable failure) {
				categories.refresh();

				if ((failure == null) && status.isSuccess()) {
					Context.createWithUuid(item).submit();
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
			refreshContext(context);
		}
	}

	@Override
	protected void onDeactivate() {
		searchBlock.clear();
	}

	private void refreshContext(Context context) {
		final String catUuid = context.getUuid();
		if (LangUtil.notEmpty(catUuid)) {
			final Category cat = categories.getItem(catUuid);
			if (cat != null) {
				searchBlock.setSelection(cat);
				setCategory(cat, false);
			} else {
				setCategory(null, false);
			}
		} else {
			if (!categories.isEmpty()) {
				Context.createWithUuid(categories.getItem(0)).submit();
			}
		}
	}

	private void setCategory(Category cat, boolean edit) {
		if (edit && cat != null) {
			categoryEditor.setCategory(cat);
			categoryEditor.setActionsVisible(true);
			if (selectionPanel.getWidgetIndex(categoryEditor) == -1) {
				selectionPanel.clear();
				selectionPanel.add(categoryEditor);
			}
		} else {
			categoryView.setCategory(cat);
			categoryView.setActionsVisible(cat != null);
			if (selectionPanel.getWidgetIndex(categoryView) == -1) {
				selectionPanel.clear();
				selectionPanel.add(categoryView);
			}
		}
	}

	private void deleteCategory(Category cat) {
		ServiceHelper.getSettingsService().deleteCategory(cat.getUuid(),
				new AsyncCallback<Status>() {
					public void onFailure(Throwable caught) {
						// TODO
					}

					public void onSuccess(Status result) {
						categories.refresh();
					}
				});
	}

	private void promptForFindings(final Category cat) {
		final FindingSelectionDialog dialog = new FindingSelectionDialog();
		dialog.addPopupListener(new PopupListener() {

			public void onPopupClosed(PopupPanel sender, boolean autoClosed) {
				Status s = dialog.getStatus();
				if (s != null && s.isSuccess()) {
					categoryEditor.addFindings(dialog.getSelectedCategories(),
							dialog.getExcludedFindings());
				}
			}

		});
		dialog.center();
		dialog.setCategories(categories, cat);
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
			categoryName.setWidth("100%");
			fieldTable.setWidget(0, 1, categoryName);
			categoryCreatePanel.add(fieldTable);

			categoryActions.addAction("Create", new ClickListener() {

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
				final int panelIndex = contentPanel
						.getWidgetIndex(createCategoryButton);
				contentPanel.insert(categoryCreatePanel, panelIndex + 1);
			}
		}

		private void createCategory(String name) {
			if (LangUtil.notEmpty(name)) {
				categoryActions.removeFromParent();
				categoryCreatePanel.add(waitImage);

				ServiceHelper.getSettingsService().createCategory(name,
						new ArrayList<String>(), new ArrayList<String>(),
						new ResultCallback<String>() {

							@Override
							protected void doFailure(String message,
									String result) {
								Window.alert("Category creation failed: "
										+ message);
								categoryCreatePanel.remove(waitImage);
								categoryCreatePanel.add(categoryActions);
							}

							@Override
							protected void doSuccess(String message,
									String result) {
								toggleCreateCategory();
								categories.refresh();
								Context.createWithUuid(result).submit();
							}
						});
			} else {
				Window.alert("Please enter a category name");
			}
		}
	}

	private class CategorySearchBlock extends
			SearchBlock<Category, CategoryCache> {

		public CategorySearchBlock(CategoryCache cache) {
			super(cache);
		}

		@Override
		protected boolean isMatch(Category item, String query) {
			return item.getName().toLowerCase().indexOf(query.toLowerCase()) >= 0;
		}

		@Override
		protected String getItemText(Category item) {
			return item.getName();
		}

		@Override
		protected void doItemClick(Category item) {
			Context.createWithUuid(item).submit();
		}

	}
}
