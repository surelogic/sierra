package com.surelogic.sierra.client.eclipse.model;

public interface IFindingsViewModelObserver {

	void noProjects(FindingsViewModel model);

	void projectFocusChanged(FindingsViewModel model);

	void findingsFilterChanged(FindingsViewModel model);

	void findingsOrganizationChanged(FindingsViewModel model);

	void findingsOrganizationFocusChanged(FindingsViewModel model);
}
