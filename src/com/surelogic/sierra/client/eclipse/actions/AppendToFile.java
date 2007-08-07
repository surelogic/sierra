package com.surelogic.sierra.client.eclipse.actions;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.Iterator;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

import com.surelogic.sierra.tool.SierraTool;

public class AppendToFile implements IObjectActionDelegate {

	private IStructuredSelection selection;

	// private static final String DEFAULT_PACKAGE = "Default Package";

	/**
	 * Constructor for AppendToFile action.
	 */
	public AppendToFile() {
		super();
	}

	/**
	 * @see IObjectActionDelegate#setActivePart(IAction, IWorkbenchPart)
	 */
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		// Nothing to do
	}

	/**
	 * @see IActionDelegate#run(IAction)
	 */
	public void run(IAction action) {
		try {

			String pluginDir = getPluginDirectory();

			File resultsFile = new File(pluginDir + File.separator
					+ "results.csv");

			BufferedWriter writer = new BufferedWriter(new FileWriter(
					resultsFile, true));
			Iterator<?> selectionIterator = selection.iterator();

			while (selectionIterator.hasNext()) {

				// Object o = selectionIterator.next();
				// TODO we need to re-implement this section
				// if (o instanceof Artifact) {
				// Artifact a = (Artifact) o;
				// String toWrite = a.getPrimarySourceLocation()
				// .getCompilationUnit().getClassName()
				// + ","
				// + a.getPrimarySourceLocation().getLineOfCode()
				// + ","
				// + String.valueOf(a.getFinding().getId() + ","
				// + a.getFindingType().getTool().getName()
				// + ","
				// + a.getRun().getRunDateTime().toString());
				// if (!DEFAULT_PACKAGE.equals(a.getPrimarySourceLocation()
				// .getCompilationUnit().getPackageName())) {
				// toWrite = a.getPrimarySourceLocation()
				// .getCompilationUnit().getPackageName()
				// + "." + toWrite;
				// }
				//
				// writer.write(toWrite);
				// writer.newLine();
				//
				// }
			}
			writer.close();

		} catch (IOException e) {

			e.printStackTrace();
		}

	}

	/**
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		this.selection = (IStructuredSelection) selection;
	}

	/**
	 * Helper method to get the current plugin directory, change the relativeURL
	 * to <your-plugin-activator>.getDefault().getBundle().getEntry("");
	 * 
	 * @return
	 */
	private static String getPluginDirectory() {

		String commonDirectory = "";

		URL relativeURL = SierraTool.getDefault().getBundle().getEntry("");

		try {

			URL commonPathURL = FileLocator.resolve(relativeURL);
			commonDirectory = commonPathURL.getPath();
			commonDirectory = commonDirectory.replace("/", File.separator);

			return commonDirectory;

		} catch (IOException e) {
			// Do nothing
		}

		return commonDirectory;

	}
}
