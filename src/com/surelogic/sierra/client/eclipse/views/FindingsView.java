package com.surelogic.sierra.client.eclipse.views;

import org.eclipse.jdt.ui.ISharedImages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.ExpandBar;
import org.eclipse.swt.widgets.ExpandItem;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.part.ViewPart;

import com.surelogic.common.eclipse.SLImages;

public final class FindingsView extends ViewPart {

	private FindingsMediator f_mediator = null;

	@Override
	public void createPartControl(final Composite parent) {
		parent.setLayout(new GridLayout());

		final Composite projectSelector = new Composite(parent, SWT.NONE);
		projectSelector.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false));
		final GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 3;
		projectSelector.setLayout(gridLayout);

		Label label = new Label(projectSelector, SWT.NONE);
		label.setAlignment(SWT.RIGHT);
		label.setImage(SLImages
				.getJDTImage(ISharedImages.IMG_OBJS_PACKFRAG_ROOT));
		label = new Label(projectSelector, SWT.NONE);
		label.setAlignment(SWT.RIGHT);
		label.setText("Project:");

		final Combo projectCombo = new Combo(projectSelector, SWT.DROP_DOWN
				| SWT.READ_ONLY);
		projectCombo.setItems(new String[] { "Project 1", "Project 2",
				"Project 3", "Project 4" });
		projectCombo.select(0);
		projectCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false));

		/*
		 * Findings for the project
		 */
		final Group findingsGroup = new Group(parent, SWT.NONE);
		findingsGroup.setText("Analysis Findings");
		findingsGroup
				.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		findingsGroup.setLayout(new FillLayout());

		SashForm sf = new SashForm(findingsGroup, SWT.VERTICAL | SWT.SMOOTH);

		final Composite topSash = new Composite(sf, SWT.NONE);
		topSash.setLayout(new GridLayout());

		/*
		 * Toolbar for analysis findings
		 */

		final ToolBar toolBar = new ToolBar(topSash, SWT.HORIZONTAL
				| SWT.SHADOW_OUT);
		toolBar.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false));

		Label groupBy = new Label(toolBar, SWT.NONE);
		groupBy.setText("Group by:");

		ToolItem sep = new ToolItem(toolBar, SWT.SEPARATOR);
		sep.setControl(groupBy);
		sep.setWidth(groupBy.computeSize(SWT.DEFAULT, SWT.DEFAULT).x);

		final ToolItem byPriority = new ToolItem(toolBar, SWT.RADIO);
		byPriority.setImage(SLImages.getImage(SLImages.IMG_PRIORITY));
		byPriority.setText("Priority");

		final ToolItem byPackage = new ToolItem(toolBar, SWT.RADIO);
		byPackage
				.setImage(SLImages
						.getJDTImage(org.eclipse.jdt.ui.ISharedImages.IMG_OBJS_PACKAGE));
		byPackage.setText("Package");

		final ToolItem byCategory = new ToolItem(toolBar, SWT.RADIO);
		byCategory.setImage(SLImages.getImage(SLImages.IMG_CATEGORY));
		byCategory.setText("Category");

		new ToolItem(toolBar, SWT.SEPARATOR);

		final ToolItem filter = new ToolItem(toolBar, SWT.PUSH);
		filter.setImage(SLImages.getImage(SLImages.IMG_FILTER));
		filter
				.setToolTipText("Configure the filters to be applied to this view");
		filter.setText("Filter");

		final Menu toolBarMenu = new Menu(parent.getShell(), SWT.POP_UP);
		final MenuItem showText = new MenuItem(toolBarMenu, SWT.CHECK);
		showText.setText("Show Text");
		showText.setSelection(true);
		showText.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				if (showText.getSelection()) {
					byPriority.setText("Priority");
					byPackage.setText("Package");
					byCategory.setText("Category");
					filter.setText("Filter");
				} else {
					byPriority.setText("");
					byPackage.setText("");
					byCategory.setText("");
					filter.setText("");
				}

				parent.layout();
			}
		});
		toolBar.setMenu(toolBarMenu);
		groupBy.setMenu(toolBarMenu);

		/*
		 * Analysis findings
		 */

		Tree tree = new Tree(topSash, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL
				| SWT.FULL_SELECTION);
		tree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		ExpandBar bar = new ExpandBar(sf, SWT.V_SCROLL);
		int barIndex = 0;
		// Second item
		final Composite detailsComp = new Composite(bar, SWT.NONE);
		final ExpandItem detailsItem = new ExpandItem(bar, SWT.NONE, barIndex++);
		detailsItem.setText("Details");
		detailsItem.setControl(detailsComp);
		detailsItem.setImage(SLImages.getImage(SLImages.IMG_DETAILS));

		// Second item
		final Composite commentsComp = new Composite(bar, SWT.NONE);
		final ExpandItem commentsItem = new ExpandItem(bar, SWT.NONE,
				barIndex++);
		commentsItem.setText("Comments");
		commentsItem.setControl(commentsComp);
		commentsItem.setImage(SLImages.getImage(SLImages.IMG_COMMENT));

		sf.setWeights(new int[] { 3, 1 });
		bar.setSpacing(2);

		f_mediator = new FindingsMediator(projectCombo, topSash, detailsItem,
				detailsComp, commentsItem, commentsComp);
	}

	@Override
	public void setFocus() {
		if (f_mediator != null) {
			f_mediator.setFocus();
		}
	}
}
