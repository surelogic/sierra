package com.surelogic.sierra.client.eclipse.wizards;

import java.io.File;
import java.util.List;

import org.eclipse.jface.viewers.*;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.*;


import com.surelogic.common.CommonImages;
import com.surelogic.common.eclipse.SLImages;
import com.surelogic.sierra.client.eclipse.model.selection.SelectionManager;
import com.surelogic.sierra.client.eclipse.model.selection.SelectionPersistence;

public class FindingSearchExportPage extends AbstractExportWizardPage<String> {
	private final List<String> f_selectedSavedSearches = f_selections;

	public FindingSearchExportPage() {
		super("FindingSearchExportPage", "search"); //$NON-NLS-1$
		setPageComplete(false);
		setTitle("Export Finding Searches");
		setDescription("Export the selected finding searches");
	}

	public void createControl(Composite parent) {
    setupControl(parent, "Select the finding searches to export:",
        new FindingSearchContentProvider(), new FindingSearchLabelProvider(),
        String.class, "sierra-finding-searches.xml");
	}

	@Override
	protected void initializeTable() {
	  List<String> searchList = SelectionManager.getInstance()
	  .getSavedSelectionNames();

	  f_TableViewer.setInput(searchList);
	}

	public boolean exportSearches() {
		SelectionPersistence.save(SelectionManager.getInstance(),
				f_selectedSavedSearches, new File(f_exportFilenameText
						.getText()));
		return true;
	}

	private static class FindingSearchContentProvider implements
			IStructuredContentProvider {

		public Object[] getElements(Object inputElement) {
			if (inputElement instanceof List) {
				List<?> searchList = (List<?>) inputElement;
				return searchList.toArray();
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

	private static class FindingSearchLabelProvider implements ILabelProvider {
		public Image getImage(Object element) {
			if (element instanceof String) {
				return SLImages.getImage(CommonImages.IMG_SIERRA_INVESTIGATE);
			}
			return null;
		}

		public String getText(Object element) {
			if (element instanceof String) {
				String holder = (String) element;
				return holder;
			}
			return null;
		}

		public void addListener(ILabelProviderListener listener) {
			// Nothing to do
		}

		public void dispose() {
			// Nothing to do
		}

		public boolean isLabelProperty(Object element, String property) {
			// Nothing to do
			return false;
		}

		public void removeListener(ILabelProviderListener listener) {
			// Nothing to do
		}
	}
}