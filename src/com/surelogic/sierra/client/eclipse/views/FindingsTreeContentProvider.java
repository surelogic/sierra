package com.surelogic.sierra.client.eclipse.views;

import java.util.Vector;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import com.surelogic.sierra.client.eclipse.model.CategoryHolder;
import com.surelogic.sierra.client.eclipse.model.ClassHolder;
import com.surelogic.sierra.client.eclipse.model.PackageHolder;
import com.surelogic.sierra.client.eclipse.model.PriorityHolder;

class FindingsTreeContentProvider implements ITreeContentProvider {

	public Object[] getChildren(Object parentElement) {

		if (parentElement instanceof PackageHolder) {
			PackageHolder packages = (PackageHolder) parentElement;
			return packages.getClasses().toArray();
		} else if (parentElement instanceof ClassHolder) {
			ClassHolder classes = (ClassHolder) parentElement;
			return classes.getFindings().toArray();
		} else if (parentElement instanceof CategoryHolder) {
			CategoryHolder categoryHolder = (CategoryHolder) parentElement;
			return categoryHolder.getFindings().toArray();
		} else if (parentElement instanceof PriorityHolder) {
			PriorityHolder priorityHolder = (PriorityHolder) parentElement;
			return priorityHolder.getFindings().toArray();
		} else {
			return null;
		}
	}

	public Object getParent(Object element) {

		// if (element instanceof PackageHolder) {
		// System.out.println("Package");
		// } else if (element instanceof ClassHolder) {
		// System.out.println("Class");
		// } else if (element instanceof CategoryHolder) {
		// System.out.println("Category");
		// } else if (element instanceof PriorityHolder) {
		// System.out.println("Priority");
		// } else {
		// System.out.println("Finding");
		// }

		return null;
	}

	public boolean hasChildren(Object element) {
		if (element instanceof PackageHolder) {
			return true;
		} else if (element instanceof ClassHolder) {
			return true;
		} else if (element instanceof CategoryHolder) {
			return true;
		} else if (element instanceof PriorityHolder) {
			return true;
		} else {
			return false;
		}
	}

	public Object[] getElements(Object inputElement) {

		if (inputElement instanceof Vector) {
			Vector<?> phs = (Vector<?>) inputElement;
			return phs.toArray();
		}

		return null;
	}

	public void dispose() {
		// Nothing to do

	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		// Nothing to do

	}

}