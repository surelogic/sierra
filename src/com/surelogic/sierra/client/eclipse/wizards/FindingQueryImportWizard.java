package com.surelogic.sierra.client.eclipse.wizards;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;

import com.surelogic.common.eclipse.SLImages;

public class FindingQueryImportWizard extends Wizard implements IImportWizard {
	private FindingQueryImportPage f_mainPage;

	@Override
	public boolean performFinish() {
		return f_mainPage.importQueries();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void addPages() {
		f_mainPage = new FindingQueryImportPage();
		addPage(f_mainPage);
	}

	public void init(IWorkbench workbench, IStructuredSelection selection) {
		setWindowTitle("Import");
		setDefaultPageImageDescriptor(SLImages
				.getImageDescriptor(SLImages.IMG_EXPORT_WEB));
	}

}