package com.surelogic.sierra.client.eclipse.actions;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Preferences.IPropertyChangeListener;
import org.eclipse.core.runtime.Preferences.PropertyChangeEvent;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageDeclaration;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IPartService;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.client.eclipse.Data;
import com.surelogic.sierra.client.eclipse.model.AbstractDatabaseObserver;
import com.surelogic.sierra.client.eclipse.model.DatabaseHub;
import com.surelogic.sierra.client.eclipse.preferences.PreferenceConstants;
import com.surelogic.sierra.jdbc.finding.FindingOverview;
import com.surelogic.sierra.tool.SierraConstants;
import com.surelogic.sierra.tool.message.Importance;

/**
 * Class to handle sierra markers
 * 
 * @author Tanmay.Sinha
 * 
 */
public final class MarkersHandler extends AbstractDatabaseObserver implements
		IPropertyChangeListener {

	public static final String SIERRA_MARKER = "com.surelogic.sierra.client.eclipse.sierraMarker";
	public static final String SIERRA_MARKER_CRITICAL = "com.surelogic.sierra.client.eclipse.sierraMarkerCritical";
	public static final String SIERRA_MARKER_HIGH = "com.surelogic.sierra.client.eclipse.sierraMarkerHigh";
	public static final String SIERRA_MARKER_MEDIUM = "com.surelogic.sierra.client.eclipse.sierraMarkerMedium";
	public static final String SIERRA_MARKER_LOW = "com.surelogic.sierra.client.eclipse.sierraMarkerLow";
	public static final String SIERRA_MARKER_IRRELEVANT = "com.surelogic.sierra.client.eclipse.sierraMarkerIrrelevant";

	private static final Logger LOG = SLLogger.getLogger("sierra");

	private IFile f_currentFile = null;
	private final MarkerListener f_listener = new MarkerListener();

	private final Executor f_executor = Executors.newSingleThreadExecutor();
	private String f_packageName;
	private String f_className;
	private String f_projectName;

	private static MarkersHandler INSTANCE = null;

	public static MarkersHandler getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new MarkersHandler();
			DatabaseHub.getInstance().addObserver(INSTANCE);
		}
		return INSTANCE;
	}

	/**
	 * Refresh the view if database changed
	 */
	@Override
	public void changed() {
		PlatformUI.getWorkbench().getDisplay().asyncExec(
				new RefreshMarkersRunnable());

		super.changed();
	}

	/**
	 * This method exists for the case when plugin is being loaded at startup, a
	 * file is open and the database contains some data for it
	 */
	public void addMarkerListener() {
		/*
		 * Try to get the active editor, ensure we check along the way to avoid
		 * NullPointerException being thrown.
		 */
		final IWorkbench workbench = PlatformUI.getWorkbench();
		if (workbench == null) {
			return;
		}

		final IWorkbenchWindow activeWindow = workbench
				.getActiveWorkbenchWindow();
		if (activeWindow == null) {
			return;
		}

		final IPartService partService = activeWindow.getPartService();
		if (partService == null) {
			return;
		}

		// TODO: This is not the perfect way, the this will register the
		// listener but will not update the current open file if the file was
		// not in focus when the eclipse was closed

		partService.addPartListener(f_listener);
		LOG.fine("Marker listener added for Sierra");

		final IWorkbenchPartReference ref = partService
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

	}

	public void removeMarkerListener() {
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
		page.removePartListener(f_listener);
	}

	public void clearAllMarkers() {
		clearMarkers(null, MarkersHandler.SIERRA_MARKER);
		clearMarkers(null, MarkersHandler.SIERRA_MARKER_CRITICAL);
		clearMarkers(null, MarkersHandler.SIERRA_MARKER_HIGH);
		clearMarkers(null, MarkersHandler.SIERRA_MARKER_MEDIUM);
		clearMarkers(null, MarkersHandler.SIERRA_MARKER_LOW);
		clearMarkers(null, MarkersHandler.SIERRA_MARKER_IRRELEVANT);
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
					clearMarkers(f_currentFile, SIERRA_MARKER_CRITICAL);
					clearMarkers(f_currentFile, SIERRA_MARKER_HIGH);
					clearMarkers(f_currentFile, SIERRA_MARKER_MEDIUM);
					clearMarkers(f_currentFile, SIERRA_MARKER_LOW);
					clearMarkers(f_currentFile, SIERRA_MARKER_IRRELEVANT);
				}

				if (PreferenceConstants.showMarkers()) {
					f_currentFile = (IFile) resource;
					if (!f_currentFile.getFileExtension().equalsIgnoreCase(
							"java")) {
						f_currentFile = null;
					} else {

						ICompilationUnit cu = JavaCore
								.createCompilationUnitFrom(f_currentFile);
						try {
							IPackageDeclaration[] packageDeclarations = cu
									.getPackageDeclarations();

							f_packageName = SierraConstants.DEFAULT_PACKAGE_PARENTHESIS;
							if (packageDeclarations.length > 0) {
								f_packageName = packageDeclarations[0]
										.getElementName();
							}

							String elementName = cu.getElementName();
							f_className = cu.getElementName().substring(0,
									elementName.length() - 5);
							f_projectName = f_currentFile.getProject()
									.getName();
							f_executor.execute(new QueryMarkers());

						} catch (JavaModelException e) {
							LOG
									.log(
											Level.SEVERE,
											"Cannot get the package declarations from compilation unit.",
											e);
						}
					}
				}
			}
		}
	}

	/**
	 * Clear all the markers of the given type in the file If the file is null,
	 * this method clear all the markers of given type in the workspace
	 * 
	 * NOTE: This method will NOT delete the subtype markers
	 * 
	 * @param file
	 * @param type
	 */
	private void clearMarkers(IFile file, String type) {
		if (type != null) {
			if (file == null) {
				try {
					IWorkspaceRoot root = ResourcesPlugin.getWorkspace()
							.getRoot();
					IProject[] projects = root.getProjects();

					for (IProject p : projects) {
						if (p.isOpen()) {
							List<IFile> files = getJavaFiles(p);
							for (IFile f : files) {
								IMarker[] markers = f.findMarkers(type, true,
										IResource.DEPTH_ONE);
								for (IMarker m : markers) {
									m.delete();
								}
							}

						}
					}

				} catch (CoreException e) {
					LOG.log(Level.SEVERE, "Error while deleting markers.", e);
				}

			} else
				try {
					if (file.exists()) {
						file.deleteMarkers(type, false, IResource.DEPTH_ONE);
					}
				} catch (CoreException e) {
					LOG.log(Level.SEVERE, "Error while deleting markers.", e);
				}
		}

	}

	/**
	 * Returns a list of all the java files in a given project
	 * 
	 * @param project
	 * @return
	 * @throws CoreException
	 */
	private List<IFile> getJavaFiles(IProject project) throws CoreException {

		List<IFile> files = new ArrayList<IFile>();
		IResource[] resources = project.members();

		for (IResource r : resources) {
			if (r.getType() == IResource.FILE) {
				IFile f = (IFile) r;
				if (f.getFileExtension() != null
						&& f.getFileExtension().equals("java")) {
					files.add((IFile) r);
				}
			}

			if (r.getType() == IResource.FOLDER) {

				getJavaFilesInFolder((IFolder) r, files);
			}
		}
		return files;
	}

	/**
	 * Recursively add java files in the provided list
	 * 
	 * @param folder
	 * @param files
	 * @throws CoreException
	 */
	private void getJavaFilesInFolder(IFolder folder, List<IFile> files)
			throws CoreException {

		IResource[] resources = folder.members();

		for (IResource r : resources) {
			if (r.getType() == IResource.FILE) {
				IFile f = (IFile) r;
				if (f.getFileExtension() != null
						&& f.getFileExtension().equalsIgnoreCase("java")) {
					files.add((IFile) r);
				}
			}

			if (r.getType() == IResource.FOLDER) {
				getJavaFilesInFolder((IFolder) r, files);
			}
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
			file.deleteMarkers(SIERRA_MARKER_CRITICAL, false,
					IResource.DEPTH_ONE);
			file.deleteMarkers(SIERRA_MARKER_HIGH, false, IResource.DEPTH_ONE);
			file
					.deleteMarkers(SIERRA_MARKER_MEDIUM, false,
							IResource.DEPTH_ONE);
			file.deleteMarkers(SIERRA_MARKER_LOW, false, IResource.DEPTH_ONE);
			file.deleteMarkers(SIERRA_MARKER_IRRELEVANT, false,
					IResource.DEPTH_ONE);
		} catch (CoreException e) {
			LOG.log(Level.SEVERE, "Error while deleting markers.", e);
		}
		IMarker marker = null;
		try {
			for (FindingOverview o : overview) {

				if (o.getImportance().equals(Importance.IRRELEVANT)) {
					marker = file.createMarker(SIERRA_MARKER_IRRELEVANT);
				} else if (o.getImportance().equals(Importance.LOW)) {
					marker = file.createMarker(SIERRA_MARKER_LOW);
				} else if (o.getImportance().equals(Importance.MEDIUM)) {
					marker = file.createMarker(SIERRA_MARKER_MEDIUM);
				} else if (o.getImportance().equals(Importance.HIGH)) {
					marker = file.createMarker(SIERRA_MARKER_HIGH);
				} else if (o.getImportance().equals(Importance.CRITICAL)) {
					marker = file.createMarker(SIERRA_MARKER_CRITICAL);
				} else {
					marker = file.createMarker(SIERRA_MARKER);
				}
				marker.setAttribute(IMarker.LINE_NUMBER, o.getLineOfCode());
				marker.setAttribute(IMarker.MESSAGE, "("
						+ o.getImportance().toStringSentenceCase() + ") "
						+ o.getSummary());
				marker.setAttribute("findingid", String.valueOf(o
						.getFindingId()));
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
			// When we close an editor, if there are no more editors open clear
			// all the sierra markers

			if (JavaUI.ID_CU_EDITOR.equals(partRef.getId())) {
				IEditorPart editor = partRef.getPage().getActiveEditor();
				if (editor == null) {
					clearMarkers(null, SIERRA_MARKER);
					clearMarkers(null, SIERRA_MARKER_CRITICAL);
					clearMarkers(null, SIERRA_MARKER_HIGH);
					clearMarkers(null, SIERRA_MARKER_MEDIUM);
					clearMarkers(null, SIERRA_MARKER_LOW);
					clearMarkers(null, SIERRA_MARKER_IRRELEVANT);
				}

			}

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

	private class RefreshMarkersRunnable implements Runnable {
		public void run() {
			/*
			 * Try to get the active editor, ensure we check along the way to
			 * avoid NullPointerException being thrown.
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
		}
	}

	private class QueryMarkers implements Runnable {
		private List<FindingOverview> f_overview;

		public void run() {
			try {
				Connection conn = Data.getConnection();
				try {

					if (PreferenceConstants.showLowestImportance()) {
						f_overview = FindingOverview.getView()
								.showFindingsForClass(conn, f_projectName,
										f_packageName, f_className);
					} else {
						f_overview = FindingOverview.getView()
								.showRelevantFindingsForClass(conn,
										f_projectName, f_packageName,
										f_className);
					}

					if (f_overview != null) {

						PlatformUI.getWorkbench().getDisplay().asyncExec(
								new Runnable() {
									public void run() {
										setMarker(f_currentFile, f_overview);
									}

								});
					}
				} finally {
					conn.close();
				}
			} catch (SQLException e) {
				LOG
						.log(
								Level.SEVERE,
								"SQL Exception from occurred when getting findings.",
								e);
			}
		}

	}

	/**
	 * Refersh markers on property change
	 */
	public void propertyChange(PropertyChangeEvent event) {

		if (event.getProperty().equals(
				PreferenceConstants.P_SIERRA_SHOW_LOWEST_FLAG)) {
			PlatformUI.getWorkbench().getDisplay().asyncExec(
					new RefreshMarkersRunnable());
		}

	}

}
