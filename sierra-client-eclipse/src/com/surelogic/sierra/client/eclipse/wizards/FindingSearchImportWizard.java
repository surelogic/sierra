package com.surelogic.sierra.client.eclipse.wizards;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;

import com.surelogic.common.CommonImages;
import com.surelogic.common.ui.SLImages;

public class FindingSearchImportWizard extends Wizard implements IImportWizard {
	private FindingSearchImportPage f_mainPage;

	@Override
	public boolean performFinish() {
		return f_mainPage.importSearches();
	}

	@Override
	public void addPages() {
		f_mainPage = new FindingSearchImportPage();
		addPage(f_mainPage);
	}

	@Override
  public void init(IWorkbench workbench, IStructuredSelection selection) {
		setWindowTitle("Import");
		setDefaultPageImageDescriptor(SLImages
				.getImageDescriptor(CommonImages.IMG_EXPORT_WEB));
	}
}