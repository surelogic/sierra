package com.surelogic.sierra.client.eclipse.views;

import java.io.File;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.Preferences.PropertyChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.ViewPart;

import com.surelogic.common.eclipse.SLImages;
import com.surelogic.sierra.client.eclipse.actions.RunTools;
import com.surelogic.sierra.client.eclipse.dialogs.PublishRunDialog;
import com.surelogic.sierra.client.eclipse.model.CategoryHolder;
import com.surelogic.sierra.client.eclipse.model.ModelCreators;
import com.surelogic.sierra.client.eclipse.model.PackageHolder;
import com.surelogic.sierra.client.eclipse.model.PriorityHolder;
import com.surelogic.sierra.entity.Artifact;
import com.surelogic.sierra.tool.SierraLogger;
import com.surelogic.sps.client.facade.SPSClient;

/**
 * The tree viewer for showing the findings.
 * 
 * @author Tanmay.Sinha
 * 
 */
public class FindingsView extends ViewPart {

	private Action categorySort;

	private Action packageSort;

	private Action uninterestingSortCategory;

	private Action uninterestingSortPackage;

	private Action uninterestingSortPriority;

	private Action prioritySort;

	// private Action severitySort;
	//
	// private Action uninterestingSortSeverity;

	private Action publishRun;

	private IStructuredSelection currentSelection;

	private static TreeViewer findingsTree;

	private IProject project;

	private Artifact selectedArtifact;

	private Vector<CategoryHolder> categoryHolder;

	private Vector<PackageHolder> packageHolder;

	private Vector<PriorityHolder> priorityHolder;

	private static final Logger log = SierraLogger.getLogger("Sierra");

	@SuppressWarnings("deprecation")
	@Override
	public void createPartControl(Composite parent) {

		Composite mainView = new Composite(parent, SWT.NONE);
		FillLayout layout = new FillLayout();
		mainView.setLayout(layout);

		findingsTree = new TreeViewer(mainView);

		GridData leftSideGridData = new GridData(GridData.FILL_BOTH);
		leftSideGridData.grabExcessVerticalSpace = true;

		findingsTree.getTree().setLayoutData(leftSideGridData);
		findingsTree.setContentProvider(new FindingsTreeContentProvider());
		findingsTree.setLabelProvider(new FindingsLabelProvider());

		findingsTree.getTree().addMouseListener(new MouseListener() {

			public void mouseDoubleClick(MouseEvent e) {

				if (selectedArtifact != null) {

					String completePath = selectedArtifact
							.getPrimarySourceLocation().getCompilationUnit()
							.getPath();

					String className = selectedArtifact
							.getPrimarySourceLocation().getCompilationUnit()
							.getClassName();

					if (className.contains("$")) {
						int dollarLocation = className.indexOf("$");
						className = className.substring(0, dollarLocation);

					}
					className = className + ".java";
					completePath = completePath + File.separator + className;

					String projectPath = project.getLocation().toOSString();

					int length = projectPath.length();
					completePath = completePath.substring(length);

					Path filePath = new Path(completePath);
					IResource toOpen = project.getFile(filePath);

					if (toOpen != null) {
						IJavaElement element = JavaCore.create(toOpen);

						if (element != null) {
							try {
								IEditorPart editorPart = JavaUI
										.openInEditor(element);
								IMarker location = null;
								try {

									// TODO: [Bug 810] Fix the marker addition
									location = ResourcesPlugin.getWorkspace()
											.getRoot().createMarker(
													"com.surelogic.tiger");
									location.setAttribute(IMarker.LINE_NUMBER,
											selectedArtifact
													.getPrimarySourceLocation()
													.getLineOfCode());
								} catch (CoreException ce) {
									ce.printStackTrace();
								}
								if (location != null) {
									IDE.gotoMarker(editorPart, location);
								}
							} catch (PartInitException e1) {
								log.log(Level.SEVERE,
										"Error, file to open not found.", e);
							} catch (JavaModelException e1) {
								e1.printStackTrace();
							} catch (Exception e3) {
								e3.printStackTrace();
							}
						}
					}

				}
			}

			public void mouseDown(MouseEvent e) {
				// Nothing TO DO
			}

			public void mouseUp(MouseEvent e) {
				// Nothing TO DO
			}

		});

		findingsTree
				.addSelectionChangedListener(new ISelectionChangedListener() {

					public void selectionChanged(SelectionChangedEvent event) {
						if (event.getSelection().isEmpty()) {
							return;
						}
						if (event.getSelection() instanceof IStructuredSelection) {
							currentSelection = (IStructuredSelection) event
									.getSelection();

							if (currentSelection.getFirstElement() instanceof Artifact) {
								selectedArtifact = (Artifact) currentSelection
										.getFirstElement();
								project = ResourcesPlugin
										.getWorkspace()
										.getRoot()
										.getProject(
												selectedArtifact.getRun()
														.getProject().getName());
							}

						}
					}
				});

		getSite().setSelectionProvider(findingsTree);

		makeActions();
		contributeToActionBars();
		createContextMenu();

	}

	private void createContextMenu() {
		// Create menu manager.
		MenuManager menuMgr = new MenuManager();
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager mgr) {
				fillContextMenu(mgr);
			}
		});

		// Create menu.
		Menu menu = menuMgr.createContextMenu(findingsTree.getControl());
		findingsTree.getControl().setMenu(menu);

		// Register menu for extension.
		getSite().registerContextMenu(menuMgr, findingsTree);
	}

	private void fillContextMenu(IMenuManager mgr) {
		mgr.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalPullDown(IMenuManager manager) {
		manager.add(categorySort);
		manager.add(packageSort);
		manager.add(prioritySort);
		manager.add(new Separator());
		manager.add(uninterestingSortCategory);
		manager.add(uninterestingSortPackage);
		manager.add(uninterestingSortPriority);
		manager.add(new Separator());
		manager.add(publishRun);
	}

	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(categorySort);
		manager.add(packageSort);
		manager.add(prioritySort);
	}

	private void makeCategorySortAction() {
		categorySort = new Action("Category", IAction.AS_RADIO_BUTTON) {
			@Override
			public void run() {
				Job categoryJob = new Job("Sorting findings by category") {
					@Override
					protected IStatus run(IProgressMonitor monitor) {
						categoryHolder = ModelCreators
								.getCategoryModel(RunTools.getProjectName());
						ModelCreators.destroyModels();
						if (categoryHolder != null) {
							return Status.OK_STATUS;
						} else {
							return null;
						}
					}
				};

				categoryJob.addJobChangeListener(new JobChangeAdapter() {
					@Override
					public void done(IJobChangeEvent event) {
						if (event.getResult().equals(Status.OK_STATUS)) {
							PlatformUI.getWorkbench().getDisplay().asyncExec(
									new Runnable() {
										public void run() {
											if (categoryHolder.size() != 0) {
												findingsTree
														.setInput(categoryHolder);
												findingsTree.refresh();
												setPartName("Findings - "
														+ RunTools
																.getProjectName());
												ModelCreators.destroyModels();
											}
										}
									});
						}
					}
				});
				categoryJob.setPriority(Job.SHORT);
				categoryJob.schedule();
			}
		};

		categorySort.setText("By Category");
		categorySort.setToolTipText("Display findings by category");
		categorySort.setImageDescriptor(SLImages
				.getImageDescriptor(SLImages.IMG_CATEGORY));
	}

	private void makePackageSortAction() {
		packageSort = new Action("Package", IAction.AS_RADIO_BUTTON) {
			@Override
			public void run() {

				Job packageJob = new Job("Sorting findings by package") {

					@Override
					protected IStatus run(IProgressMonitor monitor) {
						packageHolder = ModelCreators.getPackageModel(RunTools
								.getProjectName());
						if (packageHolder != null) {
							return Status.OK_STATUS;
						} else {
							return null;
						}

					}

				};

				packageJob.addJobChangeListener(new JobChangeAdapter() {

					@Override
					public void done(IJobChangeEvent event) {
						if (event.getResult().equals(Status.OK_STATUS)) {
							PlatformUI.getWorkbench().getDisplay().asyncExec(
									new Runnable() {
										public void run() {
											if (packageHolder.size() != 0) {
												findingsTree
														.setInput(packageHolder);
												findingsTree.refresh();
												setPartName("Findings - "
														+ RunTools
																.getProjectName());
												ModelCreators.destroyModels();
											}
										}
									});
						}
					}
				});

				packageJob.setPriority(Job.SHORT);
				packageJob.schedule();

			}
		};

		packageSort.setText("By Package");
		packageSort.setToolTipText("Display findings by package");
		packageSort
				.setImageDescriptor(SLImages
						.getJDTImageDescriptor(org.eclipse.jdt.ui.ISharedImages.IMG_OBJS_PACKAGE));
	}

	private void makePrioritySortAction() {

		prioritySort = new Action("Priority", IAction.AS_RADIO_BUTTON) {
			@Override
			public void run() {

				Job priorityJob = new Job("Sorting findings by priority") {

					@Override
					protected IStatus run(IProgressMonitor monitor) {
						priorityHolder = ModelCreators
								.getPrioritizedModel(RunTools.getProjectName());
						if (priorityHolder != null) {
							return Status.OK_STATUS;
						} else {
							return null;
						}
					}

				};

				priorityJob.addJobChangeListener(new JobChangeAdapter() {
					@Override
					public void done(IJobChangeEvent event) {
						if (event.getResult().equals(Status.OK_STATUS)) {
							PlatformUI.getWorkbench().getDisplay().asyncExec(
									new Runnable() {
										public void run() {

											if (priorityHolder.size() != 0) {
												findingsTree
														.setInput(priorityHolder);
												findingsTree.refresh();
												setPartName("Findings - "
														+ RunTools
																.getProjectName());
												ModelCreators.destroyModels();
											}
										}
									});
						}
					}
				});

				priorityJob.setPriority(Job.SHORT);
				priorityJob.schedule();

			}
		};

		prioritySort.setText("By Priority");
		prioritySort.setToolTipText("Display findings by priority");
		prioritySort.setImageDescriptor(SLImages
				.getImageDescriptor(SLImages.IMG_PRIORITY));

	}

	private void makeUninterestingPrioritySortAction() {
		uninterestingSortPriority = new Action() {

			@Override
			public void run() {

				Job priorityJob = new Job("Priority sort") {

					@Override
					protected IStatus run(IProgressMonitor monitor) {
						priorityHolder = ModelCreators
								.getUninterestingPrioritizedModel(RunTools
										.getProjectName());
						if (priorityHolder != null) {
							return Status.OK_STATUS;
						} else {
							return null;
						}
					}

				};

				priorityJob.addJobChangeListener(new JobChangeAdapter() {
					@Override
					public void done(IJobChangeEvent event) {
						if (event.getResult().equals(Status.OK_STATUS)) {
							PlatformUI.getWorkbench().getDisplay().asyncExec(
									new Runnable() {
										public void run() {
											if (priorityHolder.size() != 0) {
												findingsTree
														.setInput(priorityHolder);
												findingsTree.refresh();
												setPartName("Findings - "
														+ RunTools
																.getProjectName());
												ModelCreators.destroyModels();
											}
										}
									});
						}
					}
				});

				priorityJob.setPriority(Job.SHORT);
				priorityJob.schedule();

			}

		};
		uninterestingSortPriority.setText("Uninteresting by Priority");
		uninterestingSortPriority
				.setToolTipText("Show uninteresting findings by priority");

	}

	private void makeUninterestingPackageSortAction() {
		uninterestingSortPackage = new Action() {

			@Override
			public void run() {

				Job packageJob = new Job("Package sort") {

					@Override
					protected IStatus run(IProgressMonitor monitor) {
						packageHolder = ModelCreators
								.getUninterestingPackageModel(RunTools
										.getProjectName());
						if (packageHolder != null) {
							return Status.OK_STATUS;
						} else {
							return null;
						}

					}

				};

				packageJob.addJobChangeListener(new JobChangeAdapter() {

					@Override
					public void done(IJobChangeEvent event) {
						if (event.getResult().equals(Status.OK_STATUS)) {
							PlatformUI.getWorkbench().getDisplay().asyncExec(
									new Runnable() {
										public void run() {
											if (packageHolder.size() != 0) {
												findingsTree
														.setInput(packageHolder);
												findingsTree.refresh();
												setPartName("Findings - "
														+ RunTools
																.getProjectName());
												ModelCreators.destroyModels();
											}
										}
									});
						}
					}
				});

				packageJob.setPriority(Job.SHORT);
				packageJob.schedule();

			}

		};
		uninterestingSortPackage.setText("Uninteresting by Package");
		uninterestingSortPackage
				.setToolTipText("Show uninteresting findings by package");

	}

	private void makeUninterestingCategorySortAction() {

		uninterestingSortCategory = new Action() {

			@Override
			public void run() {

				Job categoryJob = new Job("Category Sort") {
					@Override
					protected IStatus run(IProgressMonitor monitor) {
						categoryHolder = ModelCreators
								.getUninterestingCategoryModel(RunTools
										.getProjectName());
						ModelCreators.destroyModels();
						if (categoryHolder != null) {
							return Status.OK_STATUS;
						} else {
							return null;
						}
					}
				};

				categoryJob.addJobChangeListener(new JobChangeAdapter() {
					@Override
					public void done(IJobChangeEvent event) {
						if (event.getResult().equals(Status.OK_STATUS)) {
							PlatformUI.getWorkbench().getDisplay().asyncExec(
									new Runnable() {
										public void run() {
											if (categoryHolder.size() != 0) {
												findingsTree
														.setInput(categoryHolder);
												findingsTree.refresh();
												setPartName("Findings - "
														+ RunTools
																.getProjectName());
												ModelCreators.destroyModels();
											}
										}
									});
						}
					}
				});
				categoryJob.setPriority(Job.SHORT);
				categoryJob.schedule();
			}

		};
		uninterestingSortCategory.setText("Uninteresting by Category");
		uninterestingSortCategory
				.setToolTipText("Show uninteresting findings by category");

	}

	private void makePublishRunAction() {
		publishRun = new Action() {
			@Override
			public void run() {
				SPSClient spsClient = SPSClient.getInstance();
				// Run run = spsClient.getLatestRun(RunTools.getProjectName());
				List<String> qualifiers = spsClient.getQualifiers();

				PublishRunDialog prd = new PublishRunDialog(findingsTree
						.getControl().getShell(), qualifiers);

				int result = prd.open();

				if (result == 0) {
					Vector<String> qualifierNames = prd.getNames();
					spsClient.publishRun(spsClient.getLatestRun(RunTools
							.getProjectName()), qualifierNames);
				}
			}
		};

		publishRun.setText("Publish Run...");
		publishRun.setToolTipText("Publish a run to a Sierra server");
	}

	private void makeActions() {
		makeCategorySortAction();
		makePackageSortAction();
		makePrioritySortAction();

		makeUninterestingCategorySortAction();
		makeUninterestingPackageSortAction();
		makeUninterestingPrioritySortAction();

		makePublishRunAction();
	}

	@Override
	public void setFocus() {
		// Nothing TO DO

	}

	public void propertyChange(PropertyChangeEvent event) {
		// Nothing TO DO
	}
}
