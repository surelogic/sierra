package com.surelogic.sierra.client.eclipse.actions;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
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
import com.surelogic.sierra.client.eclipse.Activator;
import com.surelogic.sierra.client.eclipse.SLog;
import com.surelogic.sierra.client.eclipse.data.RunDocumentUtility;
import com.surelogic.sierra.jdbc.run.RunPersistenceException;
import com.surelogic.sierra.tool.SierraLogger;
import com.surelogic.sierra.tool.analyzer.BuildFileGenerator;
import com.surelogic.sierra.tool.config.Config;

public final class RunAnalysis implements IObjectActionDelegate {

	private IStructuredSelection f_selection = null;

	private File resultRoot;

	private final String javaVendor;

	private final String javaVersion;

	private File buildFile;

	private BuildFileGenerator bfg;

	private Stack<File> runDocuments;

	private static final Status PROPER_TERMINATION = new Status(IStatus.OK,
			Activator.PLUGIN_ID, "Proper termination");

	private static final Status IMPROPER_TERMINATION = new Status(
			IStatus.ERROR, Activator.PLUGIN_ID, "Improper termination");

	// private static final Date dateRunTime = Calendar.getInstance().getTime();

	private static final Logger log = SierraLogger.getLogger("Sierra");

	// The default location for storing results
	private static final String SIERRA_RESULTS = ".SierraResults";

	private static final String PARSED_FILE_SUFFIX = ".xml.parsed";

	private static final String ANT_LOG_FILE = "sierra-ant.log";

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

			List<File> buildFiles = new ArrayList<File>();
			runDocuments = new Stack<File>();

			for (IJavaProject project : selectedProjects) {
				// log.info("Generating XML...");

				String projectPath = project.getResource().getLocation()
						.toString();
				File runDocument = new File(resultRoot + File.separator
						+ project.getProject().getName() + PARSED_FILE_SUFFIX);
				runDocuments.push(runDocument);

				Config config = new Config();

				config.setBaseDirectory(new File(projectPath));
				config.setProject(project.getProject().getName());
				config.setDestDirectory(resultRoot);
				config.setRunDocument(runDocument);
				config.setJavaVendor(javaVendor);
				config.setJavaVersion(javaVersion);

				bfg = new BuildFileGenerator(config);
				buildFile = bfg.writeBuildFile();
				buildFiles.add(buildFile);

			}

			if (buildFiles.size() > 1) {
				buildFile = bfg.writeMultipleProjectBuildFile(buildFiles);
			}

			// TODO: FIX THIS - Currently progress monitors are not handled
			// properly, output from ant task is also lost
			if (buildFile != null) {

				// Thread thread = new Thread(new Runnable() {
				//
				// public void run() {
				// try {
				// AntRunner runner = new AntRunner();
				// runner.setBuildFileLocation(buildFile
				// .getAbsolutePath());
				// runner
				// .addBuildLogger("org.apache.tools.ant.DefaultLogger");
				// // runner.setMessageOutputLevel(Project.MSG_DEBUG);
				// runner.setArguments("-logfile " + resultRoot
				// + File.separator + ANT_LOG_FILE);
				// // runner.addBuildListener(className)
				// BalloonUtility
				// .showMessage(
				// "Sierra Analysis Started",
				// "You may continue to work as the analysis runs. "
				// + "You will be notified when the analysis has been
				// completed.");
				// runner.run();
				//
				// while (!runDocuments.isEmpty()) {
				// File runDocumentHolder = runDocuments.pop();
				// RunDocumentUtility.loadRunDocument(
				// runDocumentHolder, null);
				// }
				// BalloonUtility
				// .showMessage("Sierra Analysis Completed",
				// "You may now examine the analysis results.");
				//
				// } catch (CoreException e) {
				// log.log(Level.SEVERE,
				// "Core exception while trying to excute ant build task"
				// + e);
				// }
				//
				// }
				//
				// });
				// thread.start();

				Job runBuildfile = new Job("Running Sierra...") {
					// Project p = new Project();
					// p.setUserProperty("ant.file", buildFile
					// .getAbsolutePath());
					// p.init();
					// ProjectHelper helper = ProjectHelper
					// .getProjectHelper();
					// p.addReference("ant.projecthelper", helper);
					// helper.parse(p, buildFile);
					// p.executeTarget(p.getDefaultTarget());
					// DefaultLogger consoleLogger = new
					// DefaultLogger();
					// consoleLogger.setOutputPrintStream(System.out);
					// consoleLogger.setErrorPrintStream(System.err);
					@Override
					protected IStatus run(IProgressMonitor monitor) {
						try {
							monitor.beginTask("Starting analysis...",
									IProgressMonitor.UNKNOWN);
							AntRunner runner = new AntRunner();
							runner.setBuildFileLocation(buildFile
									.getAbsolutePath());
							// runner.setMessageOutputLevel(Project.MSG_DEBUG);
							runner
									.addBuildLogger("org.apache.tools.ant.DefaultLogger");
							runner.setArguments("-logfile " + resultRoot
									+ File.separator + ANT_LOG_FILE);
							BalloonUtility
									.showMessage(
											"Sierra Analysis Started",
											"You may continue to work as the analysis runs. "
													+ "You will be notified when the analysis has been completed.");
							if (!AntRunner.isBuildRunning()) {
								runner.run(monitor);
							} else {
								BalloonUtility
										.showMessage(
												"One instance of same sierra analysis is already running",
												"Please wait for it to finish before restarting");

							}
							return PROPER_TERMINATION;
						} catch (CoreException e) {
							log.log(Level.SEVERE,
									"Core exception while trying to excute ant build task"
											+ e);
						} finally {
							monitor.done();
						}

						return IMPROPER_TERMINATION;

					}

				};

				runBuildfile.setPriority(Job.SHORT);
				runBuildfile.addJobChangeListener(new JobChangeAdapter() {

					@Override
					public void done(IJobChangeEvent event) {
						if (event.getResult().equals(PROPER_TERMINATION)) {
							log.info("Starting database entry...");
							while (!runDocuments.isEmpty()) {
								File runDocumentHolder = runDocuments.pop();
								try {
									RunDocumentUtility.loadRunDocument(
											runDocumentHolder, null);
									BalloonUtility
											.showMessage(
													"Sierra Analysis Completed",
													"You may now examine the analysis results.");
									log.info("Finished everything");

								} catch (RunPersistenceException rpe) {
									log.severe(rpe.getMessage());
									BalloonUtility
											.showMessage(
													"Sierra Analysis Completed with errors",
													"Check the logs.");
								} catch (Exception e) {
									log
											.log(
													Level.SEVERE,
													"Exception ocurred while persisting run to the database.",
													e);
								}
							}
						} else {
							BalloonUtility.showMessage(
									"Sierra Analysis Completed with errors",
									"Check the logs.");
						}

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
