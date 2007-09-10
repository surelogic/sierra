package com.surelogic.sierra.client.eclipse.wizards;

import java.util.List;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IExportWizard;
import org.eclipse.ui.IWorkbench;

import com.surelogic.common.eclipse.SLImages;

/**
 * The Sierra Build File wizard
 * 
 * @see org.eclipse.ant.internal.ui.datatransfer.AntBuildfileExportWizard
 */
public class SierraBuildFileExportWizard extends Wizard implements
		IExportWizard {
	private IStructuredSelection fSelection;
	private SierraBuildFileExportPage fMainPage;

	/** Creates buildfile. */
	@Override
	public boolean performFinish() {
		return fMainPage.generateBuildfiles();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void addPages() {
		fMainPage = new SierraBuildFileExportPage();
		List<IJavaProject> projects = fSelection.toList();
		fMainPage.setSelectedProjects(projects);
		addPage(fMainPage);
	}

	public void init(IWorkbench workbench, IStructuredSelection selection) {
		setWindowTitle("Export");
		setDefaultPageImageDescriptor(SLImages
				.getImageDescriptor(SLImages.IMG_SIERRA_LOGO));
		fSelection = selection;
	}
}
