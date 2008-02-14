package com.surelogic.sierra.client.eclipse.actions;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.Preferences.IPropertyChangeListener;
import org.eclipse.core.runtime.Preferences.PropertyChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
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
import org.eclipse.ui.progress.UIJob;

import com.surelogic.common.eclipse.jobs.DatabaseJob;
import com.surelogic.common.eclipse.jobs.SLUIJob;
import com.surelogic.common.i18n.I18N;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.client.eclipse.Data;
import com.surelogic.sierra.client.eclipse.model.AbstractDatabaseObserver;
import com.surelogic.sierra.client.eclipse.model.DatabaseHub;
import com.surelogic.sierra.client.eclipse.preferences.PreferenceConstants;
import com.surelogic.sierra.jdbc.finding.FindingOverview;
import com.surelogic.sierra.tool.SierraToolConstants;
import com.surelogic.sierra.tool.message.Importance;

/**
 * Class to handle sierra markers Currently keeps markers only for the current
 * active editor
 * 
 * @author Tanmay.Sinha
 * @author Edwin.Chan
 */
public final class MarkersHandler extends AbstractDatabaseObserver implements
		IPropertyChangeListener {
	private static final boolean debug = false;

	private static final boolean keepMarkersForAllVisibleEditors = false;

	/**
	 * Supertype for the other (5) marker types
	 */
	public static final String SIERRA_MARKER = "com.surelogic.sierra.client.eclipse.sierraMarker";

	public static final String SIERRA_MARKER_CRITICAL = "com.surelogic.sierra.client.eclipse.sierraMarkerCritical";
	public static final String SIERRA_MARKER_HIGH = "com.surelogic.sierra.client.eclipse.sierraMarkerHigh";
	public static final String SIERRA_MARKER_MEDIUM = "com.surelogic.sierra.client.eclipse.sierraMarkerMedium";
	public static final String SIERRA_MARKER_LOW = "com.surelogic.sierra.client.eclipse.sierraMarkerLow";
	public static final String SIERRA_MARKER_IRRELEVANT = "com.surelogic.sierra.client.eclipse.sierraMarkerIrrelevant";

	private static final Logger LOG = SLLogger.getLogger();

	private IFile f_selectedFile = null;
	private final MarkerListener f_listener = new MarkerListener();

	private static MarkersHandler INSTANCE = null;

	public static synchronized MarkersHandler getInstance() {
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
		final UIJob job = new RefreshMarkersUIJob();
		job.schedule();

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
		long startDelete = !debug ? 0 : System.currentTimeMillis();
		try {
			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			root.deleteMarkers(MarkersHandler.SIERRA_MARKER, true,
					IResource.DEPTH_INFINITE);
		} catch (CoreException e) {
			LOG.log(Level.SEVERE, "Error while deleting all markers.", e);
		}
		if (debug)
			System.out.println("Delete all markers: "
					+ (System.currentTimeMillis() - startDelete));
	}

	private MarkersHandler() {
		// Nothing to do
	}

	private void queryAndSetMarkers(IEditorPart editor) {
		IResource resource = extractResource(editor);

		if (resource != null) {
			if (resource instanceof IFile && resource.exists()) {
				if (f_selectedFile != null) {
					long startDelete = !debug ? 0 : System.currentTimeMillis();
					clearMarkers(f_selectedFile, SIERRA_MARKER);
					if (debug)
						System.out.println("Delete last markers: "
								+ (System.currentTimeMillis() - startDelete));
				}

				if (PreferenceConstants.showMarkers()) {
					f_selectedFile = (IFile) resource;
					if (!f_selectedFile.getFileExtension().equalsIgnoreCase(
							"java")) {
						f_selectedFile = null;
					} else {

						ICompilationUnit cu = JavaCore
								.createCompilationUnitFrom(f_selectedFile);
						try {
							IPackageDeclaration[] packageDeclarations = cu
									.getPackageDeclarations();

							String packageName = SierraToolConstants.DEFAULT_PACKAGE_PARENTHESIS;
							if (packageDeclarations.length > 0) {
								packageName = packageDeclarations[0]
										.getElementName();
							}

							String elementName = cu.getElementName();
							String className = cu.getElementName().substring(0,
									elementName.length() - 5);
							String projectName = f_selectedFile.getProject()
									.getName();
							Job queryMarkersJob = new QueryMarkersJob(
									"Querying markers for " + className,
									projectName, className, packageName,
									f_selectedFile);
							queryMarkersJob.schedule();

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
	 * this method clear all the markers of given type (and its subtypes) in the
	 * workspace
	 * 
	 * @param file
	 * @param type
	 */
	private void clearMarkers(IFile file, String type) {
		if (type != null && file != null) {
			try {
				if (file.exists()) {
					file.deleteMarkers(type, true, IResource.DEPTH_ZERO);
				}
			} catch (CoreException e) {
				LOG.log(Level.SEVERE, "Error while deleting markers.", e);
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
		long startDelete = !debug ? 0 : System.currentTimeMillis();
		try {
			file.deleteMarkers(SIERRA_MARKER, true, IResource.DEPTH_ZERO);
		} catch (CoreException e) {
			LOG.log(Level.SEVERE, "Error while deleting markers.", e);
		}
		if (debug)
			System.out.println("Delete markers: "
					+ (System.currentTimeMillis() - startDelete));

		long startCreate = !debug ? 0 : System.currentTimeMillis();
		IMarker marker;
		try {
			for (FindingOverview o : overview) {

				if (o.getImportance().equals(Importance.MEDIUM)) {
					marker = file.createMarker(SIERRA_MARKER_MEDIUM);
				} else if (o.getImportance().equals(Importance.LOW)) {
					marker = file.createMarker(SIERRA_MARKER_LOW);
				} else if (o.getImportance().equals(Importance.HIGH)) {
					marker = file.createMarker(SIERRA_MARKER_HIGH);
				} else if (o.getImportance().equals(Importance.IRRELEVANT)) {
					marker = file.createMarker(SIERRA_MARKER_IRRELEVANT);
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
		if (debug)
			System.out.println("Create " + overview.size() + " markers: "
					+ (System.currentTimeMillis() - startCreate));
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
		/**
		 * May not actually set markers, if already set
		 * 
		 * @param partRef
		 */
		private void ensureMarkersSetForPart(IWorkbenchPartReference partRef) {
			if (JavaUI.ID_CU_EDITOR.equals(partRef.getId())) {
				if (keepMarkersForAllVisibleEditors) {
					// FIX do only if it's not already done
					// the active editor may be different ...
				}
				IEditorPart editor = partRef.getPage().getActiveEditor();
				if (editor != null) {
					queryAndSetMarkers(editor);
				}
			}
		}

		public void partBroughtToTop(IWorkbenchPartReference partRef) {
			ensureMarkersSetForPart(partRef);
		}

		public void partActivated(IWorkbenchPartReference partRef) {
			if (keepMarkersForAllVisibleEditors) {
				ensureMarkersSetForPart(partRef);
			}
		}

		public void partClosed(IWorkbenchPartReference partRef) {
			if (JavaUI.ID_CU_EDITOR.equals(partRef.getId())) {
				IEditorPart editor = partRef.getPage().getActiveEditor();
				if (editor == null) {
					// When we close an editor, if there are no more editors
					// open clear
					// all the sierra markers
					clearAllMarkers();
				} else if (keepMarkersForAllVisibleEditors) {
					// FIX Ensure that markers are cleared for this editor
				}
			}
		}

		public void partDeactivated(IWorkbenchPartReference partRef) {
			// Nothing to do, since it could still be visible, just not selected
		}

		public void partHidden(IWorkbenchPartReference partRef) {
			if (keepMarkersForAllVisibleEditors) {
				if (JavaUI.ID_CU_EDITOR.equals(partRef.getId())) {
					IEditorPart editor = partRef.getPage().getActiveEditor();
					if (editor != null) {
						// FIX Ensure that markers are cleared for this editor
					}
				}
			}
		}

		public void partInputChanged(IWorkbenchPartReference partRef) {
			// Nothing to do
		}

		public void partOpened(IWorkbenchPartReference partRef) {
			if (keepMarkersForAllVisibleEditors) {
				ensureMarkersSetForPart(partRef);
			}

		}

		public void partVisible(IWorkbenchPartReference partRef) {
			if (keepMarkersForAllVisibleEditors) {
				ensureMarkersSetForPart(partRef);
			}
		}
	}

	private class RefreshMarkersUIJob extends SLUIJob {
		@Override
		public IStatus runInUIThread(IProgressMonitor monitor) {
			/*
			 * Try to get the active editor, ensure we check along the way to
			 * avoid NullPointerException being thrown.
			 */

			final IWorkbench workbench = PlatformUI.getWorkbench();
			if (workbench == null)
				return Status.OK_STATUS;
			final IWorkbenchWindow activeWindow = workbench
					.getActiveWorkbenchWindow();
			if (activeWindow == null)
				return Status.OK_STATUS;
			final IWorkbenchPartReference ref = activeWindow.getPartService()
					.getActivePartReference();
			if (ref == null)
				return Status.OK_STATUS;
			final IWorkbenchPage page = ref.getPage();
			if (page == null)
				return Status.OK_STATUS;

			final IEditorPart editor = page.getActiveEditor();
			if (editor != null) {
				queryAndSetMarkers(editor);
			}
			return Status.OK_STATUS;
		}
	}

	private class QueryMarkersJob extends DatabaseJob {

		private List<FindingOverview> f_overview;
		private final String f_projectName;
		private final String f_packageName;
		private final String f_className;
		private final IFile f_currentFile;

		public QueryMarkersJob(String name, String projectName,
				String className, String packageName, IFile currentFile) {
			super(name);
			f_className = className;
			f_packageName = packageName;
			f_projectName = projectName;
			f_currentFile = currentFile;
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			try {
				Connection conn = Data.readOnlyConnection();
				try {
					long start = !debug ? 0 : System.currentTimeMillis();
					Importance level = PreferenceConstants
							.showMarkersAtOrAboveImportance();
					if (level == null) {
						f_overview = FindingOverview.getView()
								.showFindingsForClass(conn, f_projectName,
										f_packageName, f_className);
					} else {
						f_overview = FindingOverview.getView()
								.showImportantEnoughFindingsForClass(conn,
										f_projectName, f_packageName,
										f_className, level);
					}
					if (debug)
						System.out.println("DB: "
								+ (System.currentTimeMillis() - start));

					if (f_overview != null) {
						final UIJob job = new SLUIJob() {
							@Override
							public IStatus runInUIThread(
									IProgressMonitor monitor) {
								setMarker(f_currentFile, f_overview);
								return Status.OK_STATUS;
							}
						};
						job.schedule();
					}
				} finally {
					conn.close();
				}

				monitor.done();
				return Status.OK_STATUS;
			} catch (SQLException e) {
				LOG.log(Level.SEVERE, I18N.err(36), e);
			}
			monitor.done();
			return Status.CANCEL_STATUS;
		}

	}

	/**
	 * Refersh markers on property change
	 */
	public void propertyChange(PropertyChangeEvent event) {

		if (event
				.getProperty()
				.equals(
						PreferenceConstants.P_SIERRA_SHOW_MARKERS_AT_OR_ABOVE_IMPORTANCE)) {
			final UIJob job = new RefreshMarkersUIJob();
			job.schedule();
		}
	}
}
