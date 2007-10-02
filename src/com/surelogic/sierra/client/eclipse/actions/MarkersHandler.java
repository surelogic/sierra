package com.surelogic.sierra.client.eclipse.actions;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageDeclaration;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.PlatformUI;

import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.client.eclipse.Data;
import com.surelogic.sierra.jdbc.finding.ClientFindingManager;
import com.surelogic.sierra.jdbc.finding.FindingOverview;
import com.surelogic.sierra.tool.SierraConstants;

public final class MarkersHandler {

	private static final String SIERRA_MARKER = "com.surelogic.sierra.client.eclipse.sierraMarker";
	private static final Logger LOG = SLLogger.getLogger("sierra");

	private static final IWorkbench f_workbench = PlatformUI.getWorkbench();
	private static final MarkerListener f_listener = new MarkerListener();
	private static final IWorkbenchPage f_page = f_workbench
			.getActiveWorkbenchWindow().getActivePage();

	private static ClientFindingManager f_manager = null;

	public static void addMarkerListener() {

		setManager();

		IEditorPart editor = f_page.getActiveEditor();
		if (editor != null) {
			queryAndSetMarkers(editor);
		}
		f_page.addPartListener(f_listener);
	}

	public static void removeMarkerListener() {
		f_page.removePartListener(f_listener);
	}

	private MarkersHandler() {
		// Nothing to do
	}

	/**
	 * Get the {@link ClientFindingManager} instance only when it has not been
	 * initialized
	 */
	private static void setManager() {
		try {
			if (f_manager == null) {
				Connection connection = Data.getConnection();
				connection.setAutoCommit(false);
				f_manager = ClientFindingManager.getInstance(connection);
				LOG.fine("Got manager for the marker listener");
			}
			// We do not want to close this connection for the duration of
			// the session

		} catch (SQLException e) {
			// Could not get a valid connection
			throw new IllegalStateException(e);
		}
	}

	private static void queryAndSetMarkers(IEditorPart editor) {
		IResource resource = extractResource(editor);

		if (resource != null) {
			if (resource instanceof IFile) {
				IFile file = (IFile) resource;
				ICompilationUnit cu = JavaCore.createCompilationUnitFrom(file);
				try {
					IPackageDeclaration[] packageDeclarations = cu
							.getPackageDeclarations();

					String packageName = SierraConstants.DEFAULT_PACKAGE;
					if (packageDeclarations.length > 0) {
						packageName = packageDeclarations[0].getElementName();
					}

					String elementName = cu.getElementName();
					String className = cu.getElementName().substring(0,
							elementName.length() - 5);
					String projectName = file.getProject().getName();

					// System.out.println("package :" + packageName + " class :"
					// + className + " project :" + projectName);

					// f_manager should never be null
					List<FindingOverview> overview = f_manager
							.showFindingsForClass(projectName, packageName,
									className);

					if (overview != null) {
						setMarker(file, overview);
					}
				} catch (JavaModelException e) {
					LOG
							.log(
									Level.SEVERE,
									"Cannot get the package declarations from compilation unit.",
									e);
				} catch (SQLException e) {
					LOG
							.log(
									Level.SEVERE,
									"SQL Exception from occurred when getting findings.",
									e);
				}

			}
		}
	}

	/**
	 * Set the marker in the given file for the list of FindingOverview
	 * 
	 * @param file
	 * @param overview
	 */
	private static void setMarker(IFile file, List<FindingOverview> overview) {
		try {
			file.deleteMarkers(SIERRA_MARKER, false, IResource.DEPTH_ONE);
			// file.deleteMarkers(IMarker.PROBLEM, false, IResource.DEPTH_ONE);
		} catch (CoreException e) {
			LOG.log(Level.SEVERE, "Error while deleting markers.", e);
		}
		IMarker marker = null;
		try {
			for (FindingOverview o : overview) {
				marker = file.createMarker(SIERRA_MARKER);
				marker.setAttribute(IMarker.LINE_NUMBER, o.getLineOfCode());
				marker.setAttribute(IMarker.MESSAGE, o.getSummary());
				// marker.setAttribute(IMarker.SEVERITY,
				// IMarker.SEVERITY_WARNING);
			}
		} catch (CoreException e) {
			LOG.log(Level.SEVERE, "Error while creating markers.", e);
		}
	}

	/**
	 * Refer to
	 * 
	 * http://wiki.eclipse.org/FAQ_How_do_I_create_problem_markers_for_my_compiler%3F
	 * 
	 * @param editor
	 * @return
	 */
	private static IResource extractResource(IEditorPart editor) {
		IEditorInput input = editor.getEditorInput();
		if (!(input instanceof IFileEditorInput))
			return null;
		return ((IFileEditorInput) input).getFile();
	}

	private static class MarkerListener implements IPartListener2 {
		public void partBroughtToTop(IWorkbenchPartReference partRef) {
			IEditorPart editor = partRef.getPage().getActiveEditor();
			if (editor != null) {
				queryAndSetMarkers(editor);
			}
		}

		public void partActivated(IWorkbenchPartReference partRef) {
			// Nothing to do

		}

		public void partClosed(IWorkbenchPartReference partRef) {
			// Nothing to do
		}

		public void partDeactivated(IWorkbenchPartReference partRef) {
			// Nothing to do
		}

		public void partHidden(IWorkbenchPartReference partRef) {
			// Nothing to do
		}

		public void partInputChanged(IWorkbenchPartReference partRef) {
			// Nothing to do
		}

		public void partOpened(IWorkbenchPartReference partRef) {
			// Nothing to do

		}

		public void partVisible(IWorkbenchPartReference partRef) {
			// Nothing to do

		}
	}
}
