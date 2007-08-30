package com.surelogic.sierra.client.eclipse.actions;

import java.util.List;

import org.eclipse.jdt.core.IJavaProject;

public class RunAnalysisAction extends ProjectSelectedMenuAction {

	@Override
	protected void run(List<IJavaProject> selectedProjects) {
		new RunAnalysis(selectedProjects).execute();
	}
}
