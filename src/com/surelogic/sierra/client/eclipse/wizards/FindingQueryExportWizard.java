package com.surelogic.sierra.client.eclipse.wizards;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IExportWizard;
import org.eclipse.ui.IWorkbench;

import com.surelogic.common.eclipse.SLImages;

public class FindingQueryExportWizard extends Wizard implements IExportWizard {
	private FindingQueryExportPage fMainPage;

	@Override
	public boolean performFinish() {
		return fMainPage.exportQueries();
	}

	@Override
	public void addPages() {
		fMainPage = new FindingQueryExportPage();
		addPage(fMainPage);
	}

	public void init(IWorkbench workbench, IStructuredSelection selection) {
		setWindowTitle("Export");
		setDefaultPageImageDescriptor(SLImages
				.getImageDescriptor(SLImages.IMG_EXPORT_WEB));
	}
}