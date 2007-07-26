package com.surelogic.sierra.client.eclipse.actions;

import java.util.Collection;
import java.util.Iterator;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

import com.surelogic.sierra.client.eclipse.model.CategoryHolder;
import com.surelogic.sierra.client.eclipse.model.ClassHolder;
import com.surelogic.sierra.client.eclipse.model.PackageHolder;
import com.surelogic.sierra.client.eclipse.model.PriorityHolder;
import com.surelogic.sierra.entity.Artifact;
import com.surelogic.sps.client.facade.SPSClient;

public class MarkInteresting implements IObjectActionDelegate {

	private IStructuredSelection selection;
	private SPSClient spsClient;

	/**
	 * Constructor for MarkInteresting action
	 */
	public MarkInteresting() {
		super();
		spsClient = SPSClient.getInstance();
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

		Iterator<?> selectionIterator = selection.iterator();

		while (selectionIterator.hasNext()) {

			Object o = selectionIterator.next();

			if (o instanceof Artifact) {
				Artifact a = (Artifact) o;

				spsClient.toggleInteresting(a);

			} else if (o instanceof CategoryHolder) {
				CategoryHolder categoryHolder = (CategoryHolder) o;
				Collection<Artifact> artifacts = categoryHolder.getFindings();

				spsClient.toggleInteresting(artifacts);

			} else if (o instanceof PriorityHolder) {
				PriorityHolder priorityHolder = (PriorityHolder) o;
				Collection<Artifact> artifacts = priorityHolder.getFindings();

				spsClient.toggleInteresting(artifacts);
			} else if (o instanceof ClassHolder) {
				ClassHolder classHolder = (ClassHolder) o;
				Collection<Artifact> artifacts = classHolder.getFindings();

				spsClient.toggleInteresting(artifacts);
			} else if (o instanceof PackageHolder) {
				PackageHolder packageHolder = (PackageHolder) o;
				Collection<ClassHolder> classes = packageHolder.getClasses();

				Iterator<ClassHolder> classIterator = classes.iterator();

				while (classIterator.hasNext()) {

					ClassHolder classHolder = classIterator.next();
					Collection<Artifact> artifacts = classHolder.getFindings();

					spsClient.toggleInteresting(artifacts);
				}
			}

		}

	}

	/**
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		this.selection = (IStructuredSelection) selection;
	}

}
