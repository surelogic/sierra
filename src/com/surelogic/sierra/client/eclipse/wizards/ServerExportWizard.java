package com.surelogic.sierra.client.eclipse.wizards;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IExportWizard;
import org.eclipse.ui.IWorkbench;

import com.surelogic.common.eclipse.SLImages;
import com.surelogic.common.images.CommonImages;

public class ServerExportWizard extends Wizard implements IExportWizard {

	private ServerExportPage fMainPage;

	@Override
	public boolean performFinish() {
		return fMainPage.exportServers();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void addPages() {
		fMainPage = new ServerExportPage();
		addPage(fMainPage);
	}

	public void init(IWorkbench workbench, IStructuredSelection selection) {
		setWindowTitle("Export");
		setDefaultPageImageDescriptor(SLImages
				.getImageDescriptor(CommonImages.IMG_EXPORT_WEB));
	}
}
