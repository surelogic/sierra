package com.surelogic.sierra.gwt.client.content.categories;

import java.util.ArrayList;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.PopupListener;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.surelogic.sierra.gwt.client.Context;
import com.surelogic.sierra.gwt.client.ContextManager;
import com.surelogic.sierra.gwt.client.content.ListContentComposite;
import com.surelogic.sierra.gwt.client.data.Category;
import com.surelogic.sierra.gwt.client.data.Status;
import com.surelogic.sierra.gwt.client.data.cache.CategoryCache;
import com.surelogic.sierra.gwt.client.service.ServiceHelper;
import com.surelogic.sierra.gwt.client.service.callback.ResultCallback;
import com.surelogic.sierra.gwt.client.service.callback.StatusCallback;
import com.surelogic.sierra.gwt.client.ui.FormButton;
import com.surelogic.sierra.gwt.client.ui.link.RemoteServerLink;
import com.surelogic.sierra.gwt.client.util.LangUtil;

public class CategoriesContent extends
		ListContentComposite<Category, CategoryCache> {
	private static final CategoriesContent instance = new CategoriesContent();
	private final CategoryView categoryView = new CategoryView();
	private final CategoryEditor categoryEditor = new CategoryEditor();
	private Widget duplicateAction;
	private Widget editAction;
	private Widget deleteAction;

	public static CategoriesContent getInstance() {
		return instance;
	}

	private CategoriesContent() {
		super(CategoryCache.getInstance());
		// singleton
	}

	@Override
	protected void onInitialize(final DockPanel rootPanel,
			final VerticalPanel selectionPanel) {
		setCaption("Categories");

		addAction(new CreateCategoryForm());

		categoryView.initialize();
		duplicateAction = categoryView.addAction("Duplicate",
				new ClickListener() {

					public void onClick(final Widget sender) {
						promptDuplicateCategory(categoryView.getCategory());
					}
				});
		editAction = categoryView.addAction("Edit", new ClickListener() {

			public void onClick(final Widget sender) {
				setCategory(categoryView.getCategory(), true);
			}
		});
		deleteAction = categoryView.addAction("Delete", new ClickListener() {

			public void onClick(final Widget sender) {
				deleteCategory(categoryView.getCategory());
			}
		});

		final CategoryCache cache = getCache();
		categoryEditor.initialize();
		categoryEditor.addAction("Save", new ClickListener() {

			public void onClick(final Widget sender) {
				ContextManager.unlockContext();
				cache.save(categoryEditor.getUpdatedCategory());
			}
		});
		categoryEditor.addAction("Cancel", new ClickListener() {

			public void onClick(final Widget sender) {
				ContextManager.unlockContext();
				setCategory(cache.getItem(categoryEditor.getCategory()
						.getUuid()), false);
			}
		});
	}

	@Override
	protected String getItemText(final Category item) {
		return item.getName();
	}

	@Override
	protected Widget getItemDecorator(final Category item) {
		if (!item.isLocal()) {
			return new RemoteServerLink(item.getName()
					+ " is from "
					+ (item.getOwnerLabel() == null ? "unknown" : item
							.getOwnerLabel()), item.getOwnerURL());
		}
		return null;
	}

	@Override
	protected boolean isItemVisible(final Category item, final String query) {
		return LangUtil.containsIgnoreCase(item.getName(), query);
	}

	@Override
	protected void onSelectionChanged(final Category item) {
		setCategory(item, false);
	}

	private void setCategory(final Category cat, final boolean edit) {
		final VerticalPanel selectionPanel = getSelectionPanel();
		if (edit && (cat != null)) {
			categoryEditor.setCategory(cat);
			categoryEditor.setActionsVisible(true);
			if (selectionPanel.getWidgetIndex(categoryEditor) == -1) {
				selectionPanel.clear();
				selectionPanel.add(categoryEditor);
			}
			ContextManager
					.lockContext("Your category changes are not saved and will be lost. Continue?");
		} else {
			categoryView.setCategory(cat);
			categoryView.setActionVisible(duplicateAction, cat != null);
			final boolean isLocal = cat == null ? false : cat.isLocal();
			categoryView.setActionVisible(editAction, isLocal);
			categoryView.setActionVisible(deleteAction, isLocal);
			if (selectionPanel.getWidgetIndex(categoryView) == -1) {
				selectionPanel.clear();
				selectionPanel.add(categoryView);
			}
		}
	}

	private void promptDuplicateCategory(final Category cat) {
		final CategoryNameDialog dialog = new CategoryNameDialog();
		dialog.setName(cat.getName() + " Copy");
		dialog.addPopupListener(new PopupListener() {

			public void onPopupClosed(final PopupPanel sender,
					final boolean autoClosed) {
				final Status s = dialog.getStatus();
				if (s != null && s.isSuccess()) {
					duplicateCategory(dialog.getName(), cat);
				}
			}
		});
		dialog.center();
	}

	private void duplicateCategory(final String newName, final Category cat) {
		ServiceHelper.getSettingsService().duplicateCategory(newName, cat,
				new ResultCallback<String>() {

					@Override
					protected void doFailure(final String message,
							final String result) {
						Window.alert("Category duplication failed: " + message);
					}

					@Override
					protected void doSuccess(final String message,
							final String result) {
						getCache().refresh();
						Context.current().setUuid(result).submit();
					}

				});
	}

	private void deleteCategory(final Category cat) {
		final CategoryCache cache = getCache();
		final int currentItemIndex = cache.getItemIndex(cat);
		final int catCount = cache.getItemCount();
		int nextItemIndex = currentItemIndex + 1;
		if (nextItemIndex >= catCount) {
			nextItemIndex = catCount - 1;
		}
		final String nextUuid = nextItemIndex < 0 ? "" : cache.getItem(
				nextItemIndex).getUuid();

		ServiceHelper.getSettingsService().deleteCategory(cat.getUuid(),
				new StatusCallback() {

					@Override
					protected void doStatus(final Status result) {
						cache.refresh();
						Context.current().setUuid(nextUuid).submit();
					}
				});
	}

	private class CreateCategoryForm extends FormButton {
		private final TextBox categoryName = new TextBox();

		public CreateCategoryForm() {
			super("Create a Category", "Create");
			getForm().addField("Name:", categoryName);
		}

		@Override
		protected void onOpen() {
			categoryName.setText("");
		}

		@Override
		protected void doOkClick() {
			final String name = categoryName.getText();

			if (LangUtil.notEmpty(name)) {
				setWaitStatus();

				ServiceHelper.getSettingsService().createCategory(name,
						new ArrayList<String>(), new ArrayList<String>(),
						new ResultCallback<String>() {

							@Override
							protected void doFailure(final String message,
									final String result) {
								clearWaitStatus();
								Window.alert("Category creation failed: "
										+ message);
							}

							@Override
							protected void doSuccess(final String message,
									final String result) {
								clearWaitStatus();
								setOpen(false);

								getCache().refresh();
								Context.current().setUuid(result).submit();
							}
						});
			} else {
				Window.alert("Please enter a category name");
			}
		}

	}

}
