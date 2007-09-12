package com.surelogic.sierra.client.eclipse.wizards;

import java.util.List;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IExportWizard;
import org.eclipse.ui.IWorkbench;

import com.surelogic.common.eclipse.SLImages;
import com.surelogic.sierra.client.eclipse.model.SierraServer;

public class ServerExportWizard extends Wizard implements IExportWizard {

	private IStructuredSelection fSelection;
	private ServerExportPage fMainPage;

	/** Creates buildfile. */
	@Override
	public boolean performFinish() {
		return fMainPage.exportServers();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void addPages() {
		fMainPage = new ServerExportPage();
		List<SierraServer> projects = fSelection.toList();
		fMainPage.setSelectedProjects(projects);
		addPage(fMainPage);
	}

	public void init(IWorkbench workbench, IStructuredSelection selection) {
		setWindowTitle("Export");
		setDefaultPageImageDescriptor(SLImages
				.getImageDescriptor(SLImages.IMG_EXPORT_WEB));
		fSelection = selection;
	}
}
