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
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.client.eclipse.Data;
import com.surelogic.sierra.jdbc.finding.FindingOverview;
import com.surelogic.sierra.tool.SierraConstants;

public final class MarkersHandler {

	private static final String SIERRA_MARKER = "com.surelogic.sierra.client.eclipse.sierraMarker";
	private static final Logger LOG = SLLogger.getLogger("sierra");

	private IFile f_currentFile = null;
	private final MarkerListener f_listener = new MarkerListener();

	private static final MarkersHandler INSTANCE = new MarkersHandler();

	public static MarkersHandler getInstance() {
		return INSTANCE;
	}

	public void addMarkerListener() {

		/*
		 * Try to get the active editor, ensure we check along the way to avoid
		 * NullPointerException being thrown.
		 */
		final IWorkbench workbench = PlatformUI.getWorkbench();
		if (workbench == null)
			return;
		final IWorkbenchWindow activeWindow = workbench
				.getActiveWorkbenchWindow();
		if (activeWindow == null)
			return;
		final IWorkbenchPartReference ref = activeWindow.getPartService()
				.getActivePartReference();
		if (ref == null)
			return;
		final IWorkbenchPage page = ref.getPage();
		if (page == null)
			return;

		final IEditorPart editor = page.getActiveEditor();
		if (editor != null) {
			queryAndSetMarkers(editor);
		}
		page.addPartListener(f_listener);
	}

	public void removeMarkerListener() {
		final IWorkbench workbench = PlatformUI.getWorkbench();
		final IWorkbenchPage page = workbench.getActiveWorkbenchWindow()
				.getActivePage();
		page.removePartListener(f_listener);
	}

	private MarkersHandler() {
		// Nothing to do
	}

	private void queryAndSetMarkers(IEditorPart editor) {
		IResource resource = extractResource(editor);

		if (resource != null) {
			if (resource instanceof IFile) {

				if (f_currentFile != null) {
					clearMarkers(f_currentFile, SIERRA_MARKER);
				}

				f_currentFile = (IFile) resource;
				ICompilationUnit cu = JavaCore
						.createCompilationUnitFrom(f_currentFile);
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
					String projectName = f_currentFile.getProject().getName();

					// System.out.println("package :" + packageName + " class :"
					// + className + " project :" + projectName);

					// f_manager should never be null
					Connection conn = Data.getConnection();
					try {
						List<FindingOverview> overview = FindingOverview
								.getView().showFindingsForClass(conn,
										projectName, packageName, className);

						if (overview != null) {
							setMarker(f_currentFile, overview);
						}
					} finally {
						conn.close();
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
	 * Clear all the markers of the given type in the file
	 * 
	 * NOTE: This method will NOT delete the subtype markers
	 * 
	 * @param file
	 * @param type
	 */
	private void clearMarkers(IFile file, String type) {
		try {
			file.deleteMarkers(type, false, IResource.DEPTH_ONE);
		} catch (CoreException e) {
			LOG.log(Level.SEVERE, "Error while deleting markers.", e);
		}
	}

	/**
	 * Set the marker in the given file for the list of FindingOverview
	 * 
	 * @param file
	 * @param overview
	 */
	private void setMarker(IFile file, List<FindingOverview> overview) {
		try {
			file.deleteMarkers(SIERRA_MARKER, false, IResource.DEPTH_ONE);
		} catch (CoreException e) {
			LOG.log(Level.SEVERE, "Error while deleting markers.", e);
		}
		IMarker marker = null;
		try {
			for (FindingOverview o : overview) {
				marker = file.createMarker(SIERRA_MARKER);
				marker.setAttribute(IMarker.LINE_NUMBER, o.getLineOfCode());
				marker.setAttribute(IMarker.MESSAGE, o.getSummary());
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
	private IResource extractResource(IEditorPart editor) {
		IEditorInput input = editor.getEditorInput();
		if (!(input instanceof IFileEditorInput))
			return null;
		return ((IFileEditorInput) input).getFile();
	}

	private class MarkerListener implements IPartListener2 {
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
