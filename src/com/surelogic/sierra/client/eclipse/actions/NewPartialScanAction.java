package com.surelogic.sierra.client.eclipse.actions;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;

import com.surelogic.common.eclipse.Activator;
import com.surelogic.common.eclipse.dialogs.ExceptionDetailsDialog;
import com.surelogic.common.logging.SLLogger;

/**
 * Action for running scans on compilation units and packages
 * 
 * @author Tanmay.Sinha
 * @author Edwin.Chan
 */
public class NewPartialScanAction implements IWorkbenchWindowActionDelegate,
		IEditorActionDelegate {
	private IStructuredSelection f_currentSelection = null;
	private IEditorPart f_currentEditor = null;

	public void dispose() {
		// Nothing for now
	}

	public void init(IWorkbenchWindow window) {
		// Nothing for now
	}

	public void run(IAction action) {
		final List<ICompilationUnit> selectedCompilationUnits = new ArrayList<ICompilationUnit>();
		final List<IPackageFragment> selectedPackageFragments = new ArrayList<IPackageFragment>();
		boolean inClassPath = false;

		/*
		 * We need to check if the file currently open in the editor is in
		 * project's class path. First we identify all the source folders in the
		 * the project, then we find whether the selected compilation unit in
		 * the editor is in those folders, if it is, then we can say that it's
		 * in the classpath for that project.
		 */
		if (f_currentEditor != null) {
			try {
				final IFile file = ((FileEditorInput) (f_currentEditor
						.getEditorInput())).getFile();
				ICompilationUnit compilationUnit = JavaCore
						.createCompilationUnitFrom(file);
				IContainer container = file.getParent();
				IJavaProject project = compilationUnit.getJavaProject();
				List<IContainer> classpathContainers = new ArrayList<IContainer>();
				IClasspathEntry[] entries = project.getRawClasspath();
				for (IClasspathEntry e : entries) {
					if (e.getEntryKind() == IClasspathEntry.CPE_SOURCE) {
						IResource resourceHolder = ResourcesPlugin
								.getWorkspace().getRoot().findMember(
										e.getPath());
						if (resourceHolder instanceof IContainer) {
							IContainer parent = (IContainer) resourceHolder;
							classpathContainers.add(parent);
						}
					}
				}

				if (container != null) {
					for (IContainer c : classpathContainers) {
						inClassPath = isMember(container, c);
						if (inClassPath) {
							break;
						}
					}
				}

				if (inClassPath) {
					selectedCompilationUnits.add(compilationUnit);
				} else {
					PlatformUI.getWorkbench().getDisplay().asyncExec(
							new Runnable() {
								public void run() {
									final StringBuilder msg = new StringBuilder();
									msg.append("The compilation unit ");
									msg.append(file.getName());
									msg.append(" is not in the classpath of");
									msg.append(" the project, please include");
									msg.append(" it in the class path and");
									msg.append(" re-scan it again.");
									final ExceptionDetailsDialog report = new ExceptionDetailsDialog(
											PlatformUI.getWorkbench()
													.getActiveWorkbenchWindow()
													.getShell(),
											"Not in Classpath", null, msg
													.toString(), null,
											Activator.getDefault());
									report.open();
								}
							});
				}
			} catch (JavaModelException jme) {
				SLLogger.getLogger().log(Level.SEVERE,
						"Error when trying to get compilation unit for class",
						jme);
			} catch (CoreException ce) {
				SLLogger.getLogger().log(Level.SEVERE,
						"Error when trying to get compilation unit for class",
						ce);
			}

		} else if (f_currentSelection != null) {

			/*
			 * If the selection is made from the package explorer we are
			 * guaranteed that it's in classpath.
			 */
			inClassPath = true;

			for (Object selection : f_currentSelection.toArray()) {
				if (selection instanceof ICompilationUnit) {
					final ICompilationUnit compilationUnit = (ICompilationUnit) selection;
					selectedCompilationUnits.add(compilationUnit);
				}

				if (selection instanceof IPackageFragment) {
					final IPackageFragment packageFragment = (IPackageFragment) selection;
					selectedPackageFragments.add(packageFragment);
				}
			}

			if (selectedPackageFragments.size() > 0) {
				for (IPackageFragment packageFragment : selectedPackageFragments) {
					try {
						for (ICompilationUnit compilationUnit : packageFragment
								.getCompilationUnits()) {
							selectedCompilationUnits.add(compilationUnit);
						}

					} catch (JavaModelException e) {
						SLLogger.getLogger("sierra").log(
								Level.SEVERE,
								"Error when trying to get compilation units for package "
										+ packageFragment.getElementName(), e);
					}
				}
			}
		}

		if ((f_currentEditor != null || f_currentSelection != null)
				&& inClassPath) {
			new NewPartialScan().scan(selectedCompilationUnits);
		}
	}

	/**
	 * Find a container inside another container recursively
	 * 
	 * @param folder
	 * @param parent
	 * 
	 * @return
	 * @throws CoreException
	 */
	private boolean isMember(IContainer folder, IContainer parent)
			throws CoreException {

		IResource[] resources = parent.members();

		for (IResource r : resources) {
			if (r.getType() == IResource.FILE) {
				IFile fileHolder = (IFile) r;
				IContainer holder = fileHolder.getParent();
				if (holder.equals(folder)) {
					return true;
				}

			}
			if (r.getType() == IResource.FOLDER) {
				IContainer holder = (IContainer) r;
				if (holder.equals(folder)) {
					return true;
				}

				else {
					if (isMember(folder, holder)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public void selectionChanged(IAction action, ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			f_currentSelection = (IStructuredSelection) selection;
		} else {
			f_currentSelection = null;
		}
	}

	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
		f_currentEditor = targetEditor;
	}
}
