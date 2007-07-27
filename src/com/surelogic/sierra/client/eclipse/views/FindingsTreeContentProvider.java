package com.surelogic.sierra.client.eclipse.views;

import java.util.List;

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

		if (inputElement instanceof List) {
			List<?> phs = (List<?>) inputElement;
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