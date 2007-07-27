package com.surelogic.sierra.client.eclipse.views;

import java.util.List;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;

import com.surelogic.common.eclipse.SLImages;
import com.surelogic.sierra.client.eclipse.model.CategoryHolder;
import com.surelogic.sierra.client.eclipse.model.ClassHolder;
import com.surelogic.sierra.client.eclipse.model.PackageHolder;
import com.surelogic.sierra.client.eclipse.model.PriorityHolder;
import com.surelogic.sierra.entity.Artifact;

class FindingsLabelProvider implements ILabelProvider {

	public Image getImage(Object element) {
		if (element instanceof PackageHolder) {
			return SLImages
					.getJDTImage(org.eclipse.jdt.ui.ISharedImages.IMG_OBJS_PACKAGE);
		} else if (element instanceof ClassHolder) {
			return SLImages
					.getJDTImage(org.eclipse.jdt.ui.ISharedImages.IMG_OBJS_CLASS);
		} else if (element instanceof CategoryHolder) {
			return SLImages.getImage(SLImages.IMG_CATEGORY);
		} else if (element instanceof PriorityHolder) {
			return SLImages.getImage(SLImages.IMG_PRIORITY);
		} else if (element instanceof Artifact) {
			Artifact ar = (Artifact) element;
			final String toolName = ar.getFindingType().getTool().getName();
			if ("FindBugs".equals(toolName)) {
				return SLImages.getImage(SLImages.IMG_FINDBUGS_FINDING);
			} else if ("PMD".equals(toolName)) {
				return SLImages.getImage(SLImages.IMG_PMD_FINDING);
			}
		}
		return SLImages.getImage(SLImages.IMG_PRIORITY);
	}

	public String getText(Object element) {
		if (element instanceof PackageHolder) {
			PackageHolder ph = (PackageHolder) element;

			List<ClassHolder> classHolder = ph.getClasses();

			int numberOfFindings = 0;
			for (ClassHolder ch : classHolder) {
				numberOfFindings = numberOfFindings + ch.getFindings().size();
			}

			return ph.getName() + " (" + numberOfFindings + ")";
		} else if (element instanceof ClassHolder) {
			ClassHolder ch = (ClassHolder) element;
			return ch.getName() + " (" + ch.getFindings().size() + ")";
		} else if (element instanceof CategoryHolder) {
			CategoryHolder categoryHolder = (CategoryHolder) element;
			return categoryHolder.getCategory() + " ("
					+ categoryHolder.getFindings().size() + ")";
		} else if (element instanceof Artifact) {
			Artifact ar = (Artifact) element;
			return ar.getMessage();
		} else if (element instanceof PriorityHolder) {
			PriorityHolder priorityHolder = (PriorityHolder) element;
			return priorityHolder.getPriority().name() + " ("
					+ priorityHolder.getFindings().size() + ")";
		} else {
			return null;
		}
	}

	public void addListener(ILabelProviderListener listener) {
		// Nothing to do
	}

	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	public void removeListener(ILabelProviderListener listener) {
		// Nothing to do
	}

	public void dispose() {
		// Nothing to do
	}
}
