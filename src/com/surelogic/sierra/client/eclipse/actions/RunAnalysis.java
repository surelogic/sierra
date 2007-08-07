package com.surelogic.sierra.client.eclipse.actions;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.ant.core.AntRunner;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

import com.surelogic.common.eclipse.BalloonUtility;
import com.surelogic.sierra.client.eclipse.SLog;
import com.surelogic.sierra.client.eclipse.data.RunDocumentUtility;
import com.surelogic.sierra.tool.SierraLogger;
import com.surelogic.sierra.tool.analyzer.BuildFileGenerator;
import com.surelogic.sierra.tool.config.Config;

public final class RunAnalysis implements IObjectActionDelegate {

	private IStructuredSelection f_selection = null;

	private File resultRoot;

	private final String javaVendor;

	private final String javaVersion;

	private File buildFile;

	private File runDocument;

	// private static final Date dateRunTime = Calendar.getInstance().getTime();

	private static final Logger log = SierraLogger.getLogger("Sierra");

	// The default location for storing results
	private static final String SIERRA_RESULTS = ".SierraResults";

	private static final String PARSED_FILE_SUFFIX = ".xml.parsed";

	public RunAnalysis() {

		String tmpDir = System.getProperty("java.io.tmpdir");
		String resultsFolder = tmpDir + File.separator + SIERRA_RESULTS;
		resultRoot = new File(resultsFolder);

		if ((!resultRoot.exists()) || (resultRoot.exists())
				&& (!resultRoot.isDirectory())) {

			resultRoot.mkdir();
		}
		javaVendor = System.getProperty("java.vendor");
		javaVersion = System.getProperty("java.version");

	}

	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		// Nothing to do
	}

	public void run(IAction action) {
		List<IJavaProject> selectedProjects = new ArrayList<IJavaProject>();
		if (f_selection != null) {
			for (Object selection : f_selection.toArray()) {
				if (selection instanceof IJavaProject) {
					selectedProjects.add((IJavaProject) selection);
				}
			}
			for (IJavaProject project : selectedProjects) {
				// log.info("Generating XML...");

				String projectPath = project.getResource().getLocation()
						.toString();
				runDocument = new File(resultRoot + File.separator
						+ project.getProject().getName() + PARSED_FILE_SUFFIX);

				Config config = new Config();

				config.setBaseDirectory(projectPath);
				config.setProject(project.getProject().getName());
				config.setDestDirectory(resultRoot);
				config.setRunDocument(runDocument);
				config.setJavaVendor(javaVendor);
				config.setJavaVersion(javaVersion);

				BuildFileGenerator bfg = new BuildFileGenerator(config);
				buildFile = bfg.writeBuildFile();

				// TODO: FIX THIS - Currently progress monitors are not handled
				// properly, output from ant task is also lost
				if (buildFile != null) {

					Job runBuildfile = new Job("Running ANT...") {

						@Override
						protected IStatus run(IProgressMonitor monitor) {
							try {
								AntRunner runner = new AntRunner();
								runner.setBuildFileLocation(buildFile
										.getAbsolutePath());
								runner
										.setArguments("-Dmessage=Building -verbose");
								runner.run();

							} catch (CoreException e) {
								log.log(Level.SEVERE,
										"Core exception while trying to excute ant build task"
												+ e);
							}
							return Status.OK_STATUS;
						}

					};

					runBuildfile.setPriority(Job.SHORT);
					runBuildfile.addJobChangeListener(new JobChangeAdapter() {

						@Override
						public void done(IJobChangeEvent event) {
							RunDocumentUtility.loadRunDocument(runDocument,
									null);
							BalloonUtility
									.showMessage("Sierra Analysis Completed",
											"You may now examine the analysis results.");

						}

					});
					runBuildfile.schedule();
					BalloonUtility
							.showMessage(
									"Sierra Analysis Started",
									"You may continue to work as the analysis runs. "
											+ "You will be notified when the analysis has been completed.");

				}

			}
		}
	}

	public void selectionChanged(IAction action, ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			action.setEnabled(true);
			f_selection = (IStructuredSelection) selection;
		} else {
			f_selection = null;
			action.setEnabled(false);
			SLog.logWarning("Selection is not an IStructuredSelection",
					new Exception());
		}
	}
}
