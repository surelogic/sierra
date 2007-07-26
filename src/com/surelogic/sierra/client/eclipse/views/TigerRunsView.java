package com.surelogic.sierra.client.eclipse.views;

import java.util.Vector;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import com.surelogic.sierra.client.eclipse.model.ProjectHolder;
import com.surelogic.sierra.entity.Run;

public class TigerRunsView extends ViewPart {
	private TreeViewer tigerRunsTree;
	private Action loadRuns;
	private IStructuredSelection currentSelection;

	class TigerRunsViewContentProvider implements IStructuredContentProvider,
			ITreeContentProvider {

		public Object[] getElements(Object parent) {
			if (parent instanceof Vector) {
				Vector<?> phs = (Vector<?>) parent;
				return phs.toArray();
			}

			return null;

		}

		public Object getParent(Object child) {

			return null;
		}

		public Object[] getChildren(Object parent) {
			if (parent instanceof ProjectHolder) {
				ProjectHolder projectHolder = (ProjectHolder) parent;
				return projectHolder.getRuns().toArray();
			}
			return null;
		}

		public boolean hasChildren(Object parent) {
			if (parent instanceof ProjectHolder) {
				return true;
			} else {
				return false;
			}
		}

		public void dispose() {
			// Nothing to do

		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			// Nothing to do

		}
	}

	class TigerRunsViewLabelProvider extends LabelProvider {

		@Override
		public String getText(Object obj) {
			if (obj instanceof Run) {
				Run run = (Run) obj;
				return run.getRunDateTime().toString();
			} else if (obj instanceof ProjectHolder) {
				ProjectHolder projectHolder = (ProjectHolder) obj;
				return projectHolder.getProjectName();
			}
			return null;
		}

		@Override
		public Image getImage(Object obj) {
			String imageKey;

			if (obj instanceof ProjectHolder) {
				imageKey = ISharedImages.IMG_OBJ_FOLDER;
			} else {
				imageKey = ISharedImages.IMG_OBJ_ELEMENT;
			}

			return PlatformUI.getWorkbench().getSharedImages().getImage(
					imageKey);
		}
	}

	/**
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 */
	@Override
	public void createPartControl(Composite parent) {
		tigerRunsTree = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL
				| SWT.V_SCROLL);
		tigerRunsTree.setContentProvider(new TigerRunsViewContentProvider());
		tigerRunsTree.setLabelProvider(new TigerRunsViewLabelProvider());

		tigerRunsTree
				.addSelectionChangedListener(new ISelectionChangedListener() {

					public void selectionChanged(SelectionChangedEvent event) {
						if (event.getSelection().isEmpty()) {
							return;
						}
						if (event.getSelection() instanceof IStructuredSelection) {
							currentSelection = (IStructuredSelection) event
									.getSelection();

							System.out.println(currentSelection);

						}

					}

				});

		makeActions();
		hookContextMenu();
		contributeToActionBars();

		getSite().setSelectionProvider(tigerRunsTree);

	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				TigerRunsView.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(tigerRunsTree.getControl());
		tigerRunsTree.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, tigerRunsTree);
	}

	private void fillContextMenu(IMenuManager manager) {
		// manager.add(new Separator());
		// Other plug-ins can contribute there actions here
		// manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalPullDown(IMenuManager manager) {
		manager.add(loadRuns);
	}

	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(loadRuns);
	}

	private void makeActions() {
		loadRuns = new Action() {
			@Override
			public void run() {
				// tigerRunsTree.setInput(ModelCreators.getRunsByProject());
				tigerRunsTree.refresh();
			}
		};
		loadRuns.setText("Load Runs");
		loadRuns.setToolTipText("Get the runs from the database");
		// loadRuns.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages()
		// .getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));

	}

	//
	// private void hookDoubleClickAction() {
	// viewer.addDoubleClickListener(new IDoubleClickListener() {
	// public void doubleClick(DoubleClickEvent event) {
	// doubleClickAction.run();
	// }
	// });
	// }
	//
	// private void showMessage(String message) {
	// MessageDialog.openInformation(viewer.getControl().getShell(),
	// "Tiger Runs", message);
	// }

	/**
	 * Passing the focus request to the viewer's control.
	 */
	@Override
	public void setFocus() {
		tigerRunsTree.getControl().setFocus();
	}
}
