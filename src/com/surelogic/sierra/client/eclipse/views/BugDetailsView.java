package com.surelogic.sierra.client.eclipse.views;
//package com.surelogic.spsToolPlugin.views;
//
//import org.eclipse.core.resources.IFile;
//import org.eclipse.core.resources.IMarker;
//import org.eclipse.core.resources.IProject;
//import org.eclipse.core.resources.IWorkspaceRoot;
//import org.eclipse.core.resources.ResourcesPlugin;
//import org.eclipse.jdt.core.ICompilationUnit;
//import org.eclipse.jdt.core.JavaCore;
//import org.eclipse.jdt.ui.JavaUI;
//import org.eclipse.jface.action.Action;
//import org.eclipse.jface.action.IMenuListener;
//import org.eclipse.jface.action.IMenuManager;
//import org.eclipse.jface.action.IToolBarManager;
//import org.eclipse.jface.action.MenuManager;
//import org.eclipse.jface.action.Separator;
//import org.eclipse.jface.dialogs.MessageDialog;
//import org.eclipse.jface.viewers.DoubleClickEvent;
//import org.eclipse.jface.viewers.IDoubleClickListener;
//import org.eclipse.jface.viewers.ISelection;
//import org.eclipse.jface.viewers.IStructuredContentProvider;
//import org.eclipse.jface.viewers.IStructuredSelection;
//import org.eclipse.jface.viewers.ITableLabelProvider;
//import org.eclipse.jface.viewers.LabelProvider;
//import org.eclipse.jface.viewers.TableViewer;
//import org.eclipse.jface.viewers.Viewer;
//import org.eclipse.jface.viewers.ViewerSorter;
//import org.eclipse.swt.SWT;
//import org.eclipse.swt.graphics.Image;
//import org.eclipse.swt.layout.GridData;
//import org.eclipse.swt.widgets.Composite;
//import org.eclipse.swt.widgets.Menu;
//import org.eclipse.swt.widgets.Table;
//import org.eclipse.swt.widgets.TableColumn;
//import org.eclipse.ui.IActionBars;
//import org.eclipse.ui.IEditorPart;
//import org.eclipse.ui.ISharedImages;
//import org.eclipse.ui.IWorkbenchActionConstants;
//import org.eclipse.ui.PartInitException;
//import org.eclipse.ui.PlatformUI;
//import org.eclipse.ui.ide.IDE;
//import org.eclipse.ui.part.ViewPart;
//
//import com.surelogic.sps.bug.BugEntry;
//
//public class BugDetailsView extends ViewPart {
//
//	private Action action1;
//
//	private Action action2;
//
//	private Action action3;
//
//	private Action doubleClickAction;
//
//	private TableViewer bugDetailsTable;
//
//	private Table table;
//
//	// Current hack for data
//	BugDetailsModel model;
//
//	class NameSorter extends ViewerSorter {
//	}
//
//	/**
//	 * The constructor.
//	 */
//	public BugDetailsView() {
//	}
//
//	/**
//	 * This is a callback that will allow us to create the bugDetailsTable and
//	 * initialize it.
//	 */
//	public void createPartControl(Composite parent) {
//
//		createTable(parent);
//		createBugDetailstable();
//
//		loadModel();
//
//		makeActions();
//		hookContextMenu();
//		hookDoubleClickAction();
//		contributeToActionBars();
//	}
//
//	private void hookContextMenu() {
//		MenuManager menuMgr = new MenuManager("#PopupMenu");
//		menuMgr.setRemoveAllWhenShown(true);
//		menuMgr.addMenuListener(new IMenuListener() {
//			public void menuAboutToShow(IMenuManager manager) {
//				BugDetailsView.this.fillContextMenu(manager);
//			}
//		});
//		Menu menu = menuMgr.createContextMenu(bugDetailsTable.getControl());
//		bugDetailsTable.getControl().setMenu(menu);
//		getSite().registerContextMenu(menuMgr, bugDetailsTable);
//	}
//
//	private void contributeToActionBars() {
//		IActionBars bars = getViewSite().getActionBars();
//		fillLocalPullDown(bars.getMenuManager());
//		fillLocalToolBar(bars.getToolBarManager());
//	}
//
//	private void fillLocalPullDown(IMenuManager manager) {
//		manager.add(action1);
//		manager.add(new Separator());
//		manager.add(action2);
//	}
//
//	private void fillContextMenu(IMenuManager manager) {
//		manager.add(action1);
//		manager.add(action2);
//		manager.add(action3);
//		// Other plug-ins can contribute there actions here
//		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
//	}
//
//	private void fillLocalToolBar(IToolBarManager manager) {
//		manager.add(action1);
//		manager.add(action2);
//		manager.add(action3);
//	}
//
//	private void makeActions() {
//		action1 = new Action() {
//			public void run() {
//				// FileDialog fd = new FileDialog(bugDetailsTable.getControl()
//				// .getShell(), SWT.OPEN | SWT.SINGLE);
//				// fd.setText("Select the jar files to exclude from the run");
//				// fd.setFilterExtensions(new String[] { "*.jar" });
//				// fd.open();
//				//
//				// String jarFile = fd.getFilterPath() + "\\" +
//				// fd.getFileName();
//				// jarFiles.add(jarFile);
//				//
//				// System.out.println(jarFile);
//
//				// rest of program goes here
//				// RuleSetDialog rd = new
//				// RuleSetDialog(bugDetailsTable.getControl().getShell());
//				// rd.open();
//			}
//		};
//		action1.setText("Set jars");
//		action1.setToolTipText("Set jar files to exclude");
//		action1.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages()
//				.getImageDescriptor(ISharedImages.IMG_OBJ_FOLDER));
//
//		action2 = new Action() {
//			public void run() {
//				// if (jarFiles != null) {
//				// Launcher.LaunchFB(fileName, jarFiles);
//				// }
//			}
//		};
//		action2.setText("Run Analysis");
//		action2.setToolTipText("Run all the analysis");
//		action2.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages()
//				.getImageDescriptor(ISharedImages.IMG_TOOL_UP));
//
//		action3 = new Action() {
//
//			public void run() {
//				// FileDialog fd = new FileDialog(bugDetailsTable.getControl()
//				// .getShell(), SWT.OPEN | SWT.SINGLE);
//				// fd.setText("Select resource to run analysis on");
//				// // fd.setFilterExtensions(new String[] { "*.jar" });
//				// fd.open();
//				//
//				// fileName = fd.getFilterPath() + "\\" + fd.getFileName();
//			}
//		};
//		action3.setText("Select resource to run analysis on");
//		action3.setToolTipText("Select a jar file");
//		action3.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages()
//				.getImageDescriptor(ISharedImages.IMG_OBJ_FILE));
//
//		doubleClickAction = new Action() {
//			public void run() {
//				ISelection selection = bugDetailsTable.getSelection();
//				Object obj = ((IStructuredSelection) selection)
//						.getFirstElement();
//
//				BugEntry entry = (BugEntry) obj;
//
//				IWorkspaceRoot wrk = ResourcesPlugin.getWorkspace().getRoot();
//				IProject prj = wrk.getProject(entry.getRunEntry().getProject()
//						.getName());
//
//				if (prj != null) {
//					// GET FILENAME FROM OBJECT - HACK AS OF NOW
//					IFile file1 = prj.getFile("\\sample\\fb\\"
//							+ entry.getClassName() + ".java");
//
//					ICompilationUnit icu = JavaCore
//							.createCompilationUnitFrom(file1);
//
//					try {
//						IEditorPart ep = JavaUI.openInEditor(icu);
//
//						IMarker marker = ResourcesPlugin.getWorkspace()
//								.getRoot().createMarker("com.surelogic.sps");
//
//						IDE.gotoMarker(ep, marker);
//					} catch (PartInitException e) {
//						showMessage("PartInitException was thrown");
//					} catch (org.eclipse.core.runtime.CoreException e) {
//						showMessage("CoreException was thrown");
//					}
//
//				}
//			}
//		};
//	}
//
//	private void hookDoubleClickAction() {
//		bugDetailsTable.addDoubleClickListener(new IDoubleClickListener() {
//			public void doubleClick(DoubleClickEvent event) {
//				doubleClickAction.run();
//			}
//		});
//	}
//
//	private void showMessage(String message) {
//		MessageDialog.openInformation(bugDetailsTable.getControl().getShell(),
//				"SPS Bugs View", message);
//	}
//
//	/**
//	 * Passing the focus request to the bugDetailsTable's control.
//	 */
//	public void setFocus() {
//		bugDetailsTable.getControl().setFocus();
//	}
//
//	/**
//	 * Create the Table
//	 */
//	private void createTable(Composite parent) {
//		int style = SWT.SINGLE | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL
//				| SWT.FULL_SELECTION | SWT.HIDE_SELECTION;
//
//		table = new Table(parent, style);
//
//		GridData gridData = new GridData(GridData.FILL_BOTH);
//		gridData.grabExcessVerticalSpace = true;
//		gridData.horizontalSpan = 3;
//		table.setLayoutData(gridData);
//
//		table.setLinesVisible(true);
//		table.setHeaderVisible(true);
//
//		TableColumn column = new TableColumn(table, SWT.CENTER, 0);
//		column.setText("Bug ID");
//		column.setWidth(60);
//
//		column = new TableColumn(table, SWT.CENTER, 1);
//		column.setText("Run ID");
//		column.setWidth(60);
//
//		column = new TableColumn(table, SWT.CENTER, 2);
//		column.setText("Bug");
//		column.setWidth(300);
//
//		column = new TableColumn(table, SWT.CENTER, 3);
//		column.setText("Package name");
//		column.setWidth(200);
//
//		column = new TableColumn(table, SWT.CENTER, 4);
//		column.setText("Class name");
//		column.setWidth(100);
//
//		column = new TableColumn(table, SWT.CENTER, 5);
//		column.setText("Line number");
//		column.setWidth(80);
//
//		column = new TableColumn(table, SWT.CENTER, 6);
//		column.setText("Priority");
//		column.setWidth(100);
//
//		column = new TableColumn(table, SWT.CENTER, 7);
//		column.setText("Severity");
//		column.setWidth(100);
//	}
//
//	/**
//	 * Create the instanceDetailstable
//	 */
//	private void createBugDetailstable() {
//		bugDetailsTable = new TableViewer(table);
//		bugDetailsTable.setUseHashlookup(true);
//
//		bugDetailsTable
//				.setContentProvider(new BugDetailsTableContentProvider());
//		bugDetailsTable.setLabelProvider(new BugDetailsTableLabelProvider());
//
//	}
//
//	class BugDetailsTableContentProvider implements IStructuredContentProvider {
//
//		public Object[] getElements(Object inputElement) {
//
//			return ((BugDetailsModel) inputElement).getDetails();
//
//		}
//
//		public void dispose() {
//		}
//
//		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
//		}
//
//	}
//
//	class BugDetailsTableLabelProvider extends LabelProvider implements
//			ITableLabelProvider {
//		public String getColumnText(Object obj, int index) {
//
//			BugEntry id = (BugEntry) obj;
//
//			String result = "";
//			switch (index) {
//			case 0:
//				result = "1";// id.getId());
//				break;
//			case 1:
//				result = "10";// id.getRunEntry().getId());
//				break;
//			case 2:
//				result = "Some Description"; // id.getDefinition().getDescription();
//				break;
//			case 3:
//				result = id.getPackageName();
//				break;
//			case 4:
//				result = id.getClassName();
//				break;
//			case 5:
//				result = String.valueOf(id.getLineNumber());
//				break;
//			case 6:
//				result = "High";// String.valueOf(id.getPriority());
//				break;
//			case 7:
//				result = "Medium"; // String.valueOf(id.getSeverity());
//				break;
//			default:
//				break;
//
//			}
//			return result;
//
//		}
//
//		public Image getColumnImage(Object obj, int index) {
//			return getImage(obj);
//		}
//
//		public Image getImage(Object obj) {
//			return null;
//		}
//	}
//
//	private void loadModel() {
//		model = new BugDetailsModel();
//
//		bugDetailsTable.setInput(model);
//	}
//
//}