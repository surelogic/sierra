package com.surelogic.sierra.client.eclipse.wizards;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;

import com.surelogic.common.eclipse.SLImages;
import com.surelogic.common.images.CommonImages;

public class FindingSearchImportWizard extends Wizard implements IImportWizard {
	private FindingSearchImportPage f_mainPage;

	@Override
	public boolean performFinish() {
		return f_mainPage.importSearches();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void addPages() {
		f_mainPage = new FindingSearchImportPage();
		addPage(f_mainPage);
	}

	public void init(IWorkbench workbench, IStructuredSelection selection) {
		setWindowTitle("Import");
		setDefaultPageImageDescriptor(SLImages
				.getImageDescriptor(CommonImages.IMG_EXPORT_WEB));
	}
}