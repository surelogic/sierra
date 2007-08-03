package com.surelogic.sierra.client.eclipse.actions;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.ILock;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

import com.surelogic.common.eclipse.BalloonUtility;
import com.surelogic.sierra.client.eclipse.jobs.SierraJobs;
import com.surelogic.sierra.entity.ClientRunWriter;
import com.surelogic.sierra.tool.SierraLogger;
import com.surelogic.sierra.tool.analyzer.EclipseLauncher;
import com.surelogic.sierra.tool.analyzer.Launcher;
import com.surelogic.sierra.tool.config.BaseConfig;

/**
 * The action executed when tools are run
 * 
 * @author Tanmay.Sinha
 * 
 */
public class RunTools implements IObjectActionDelegate {

	private ISelection selection;

	private String projectPath;

	private IJavaProject project;

	private static final ILock toolFindBugsLock = Job.getJobManager().newLock();

	private static final ILock toolPMDLock = Job.getJobManager().newLock();

	private volatile boolean successFB = false;

	private volatile boolean successPMD = false;

	private volatile boolean finished = false;

	private Launcher launcher;

	private BaseConfig baseConfig;

	private static String projectName;

	private static final Logger log = SierraLogger.getLogger("Sierra");

	/**
	 * Constructor for RunTools.
	 */
	public RunTools() {
		super();
	}

	public static String getProjectName() {
		return projectName;
	}

	/**
	 * @see IObjectActionDelegate#setActivePart(IAction, IWorkbenchPart)
	 */
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		// Nothing TO DO
	}

	/**
	 * @see IActionDelegate#run(IAction)
	 */
	public void run(IAction action) {
		if (!selection.isEmpty()) {

			if (selection instanceof IStructuredSelection) {

				IStructuredSelection structuredSelection = (IStructuredSelection) selection;
				Object element = structuredSelection.getFirstElement();

				if (element instanceof IJavaProject) {

					project = (IJavaProject) element;

					projectName = project.getElementName();

					projectPath = project.getResource().getLocation()
							.toOSString();

					// // Getting the source directories for the project
					// try {
					// int sourceFolders = 0;
					// sourceDirectory = new Vector<String>();
					// IPackageFragment pf[] = project.getPackageFragments();
					//
					// for (int i = 0; i < pf.length; i++) {
					// if (pf[i].containsJavaResources()) {
					//
					// if (pf[i].getResource() != null) {
					// String path = pf[i].getResource()
					// .getLocation().toOSString();
					// if ((!path.endsWith(".jar"))
					// && (!path.endsWith(".zip"))) {
					// sourceFolders++;
					// //
					// System.out.println(pf[i].getPath().removeFirstSegments(1));
					// sourceDirectory.add(path);
					// }
					// }
					// }
					// }
					//
					// } catch (JavaModelException jme) {
					// log
					// .log(Level.SEVERE,
					// "Error in identifying source directories"
					// + jme);
					// }
					//
					// final String[] sourceDirectories = sourceDirectory
					// .toArray(new String[sourceDirectory.size()]);

					try {

						baseConfig = new BaseConfig();
						baseConfig.setBaseDirectory(projectPath);
						baseConfig.setJdkVersion("1.5");
						baseConfig.setProjectName(project.getElementName());
						// baseConfig.setSourceDirectories(sosurceDirectories);

						launcher = new EclipseLauncher(project.getProject()
								.getDescription().getName(), baseConfig);

						Job launchFB = new SierraJobs(SierraJobs.SIERRA,
								"Running FindBugs") {

							@Override
							protected IStatus run(IProgressMonitor monitor) {

								try {
									monitor.beginTask("Running FindBugs",
											IProgressMonitor.UNKNOWN);
									toolFindBugsLock.acquire();
									launcher.launchFB();

									// TODO: [Bug 783] The cancel does not
									// cancel the ANT run
									// once it has been triggered

								} catch (Exception e) {
									e.printStackTrace();
								} finally {
									toolFindBugsLock.release();
									monitor.done();
								}

								return Status.OK_STATUS;
							}
						};

						Job launchPMD = new SierraJobs(SierraJobs.SIERRA, "PMD") {

							@Override
							protected IStatus run(IProgressMonitor monitor) {

								try {

									monitor.beginTask("Running PMD",
											IProgressMonitor.UNKNOWN);
									toolPMDLock.acquire();
									// launcher.launchPMD();
									launcher.launchPMDANT();

								} catch (Exception e) {
									e.printStackTrace();
								} finally {
									toolPMDLock.release();
									monitor.done();
								}

								return Status.OK_STATUS;
							}

						};

						/*
						 * Notify the user we are starting the analysis in the
						 * background.
						 */
						BalloonUtility
								.showMessage(
										"Sierra Analysis Started",
										"You may continue to work as the analysis runs. "
												+ "You will be notified when the analysis has been completed.");
						launchFB
								.addJobChangeListener(new TigerJobChangeAdapter(
										"FindBugs"));
						launchFB.schedule();

						launchPMD
								.addJobChangeListener(new TigerJobChangeAdapter(
										"PMD"));
						launchPMD.schedule();

					} catch (CoreException e1) {
						log.log(Level.SEVERE, "Unable to finish tool run jobs"
								+ e1);
					}
				}

			}
		}

	}

	/**
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		this.selection = selection;
	}

	class TigerJobChangeAdapter extends JobChangeAdapter {

		String jobName = "";

		public TigerJobChangeAdapter(String jobName) {
			this.jobName = jobName;
		}

		@Override
		public void aboutToRun(IJobChangeEvent event) {
			// Nothing to do
		}

		@Override
		public void done(IJobChangeEvent event) {
			if (event.getResult().equals(Status.OK_STATUS)) {

				if ("FindBugs".equals(jobName)) {
					successFB = true;
				} else if ("PMD".equals(jobName)) {
					successPMD = true;
				} else if ("Finishing".equals(jobName)) {
					finished = true;
				}

				if (successFB && successPMD) {
					Job finishRuns = new SierraJobs(SierraJobs.SIERRA,
							"Finishing Run") {

						@Override
						protected IStatus run(IProgressMonitor monitor) {

							try {

								monitor.beginTask("Generating findings",
										IProgressMonitor.UNKNOWN);
								launcher.parseFiles();
								new ClientRunWriter(project.getProject()
										.getDescription().getName()).write();
							} catch (CoreException e) {
								log.log(Level.SEVERE, "Unable to finish run.",
										e);
							} finally {
								monitor.done();
							}

							return Status.OK_STATUS;
						}

					};

					finishRuns.setPriority(Job.SHORT);
					finishRuns.addJobChangeListener(new TigerJobChangeAdapter(
							"Finishing"));
					finishRuns.schedule();

					// Tools finished runs
					successFB = false;
					successPMD = false;
				}

				if (finished) {
					BalloonUtility.showMessage("Sierra Analysis Completed",
							"You may now examine the analysis results.");
					finished = false;
				}
			}
		}

	}
}
