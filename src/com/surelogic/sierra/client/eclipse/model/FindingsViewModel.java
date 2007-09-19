package com.surelogic.sierra.client.eclipse.model;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public final class FindingsViewModel {

	public FindingsViewModel(final File saveFile) {
		assert saveFile != null;
		f_saveFile = saveFile;
		FindingsViewOrganization p = new FindingsViewOrganization(this);
		p.getMutableTreePart().add(FindingsViewColumn.IMPORTANCE);
		p.getMutableTreePart().add(FindingsViewColumn.PACKAGE_NAME);
		p.getMutableTreePart().add(FindingsViewColumn.CLASS_NAME);
		p.getMutableTreePart().add(FindingsViewColumn.SUMMARY);
		p.getMutableTablePart().add(FindingsViewColumn.LOC);
		p.getMutableTablePart().add(FindingsViewColumn.TOOL);
		p.getMutableTablePart().add(FindingsViewColumn.CATEGORY);
		p.getMutableTablePart().add(FindingsViewColumn.MNEMONIC);
		f_viewOrganizations.put("Importance", p);
		p = new FindingsViewOrganization(this);
		p.getMutableTreePart().add(FindingsViewColumn.PACKAGE_NAME);
		p.getMutableTreePart().add(FindingsViewColumn.CLASS_NAME);
		p.getMutableTreePart().add(FindingsViewColumn.SUMMARY);
		p.getMutableTablePart().add(FindingsViewColumn.IMPORTANCE);
		p.getMutableTablePart().add(FindingsViewColumn.LOC);
		p.getMutableTablePart().add(FindingsViewColumn.TOOL);
		p.getMutableTablePart().add(FindingsViewColumn.CATEGORY);
		p.getMutableTablePart().add(FindingsViewColumn.MNEMONIC);
		f_viewOrganizations.put("Package", p);
		p = new FindingsViewOrganization(this);
		p.getMutableTreePart().add(FindingsViewColumn.TOOL);
		p.getMutableTreePart().add(FindingsViewColumn.IMPORTANCE);
		p.getMutableTreePart().add(FindingsViewColumn.PACKAGE_NAME);
		p.getMutableTreePart().add(FindingsViewColumn.CLASS_NAME);
		p.getMutableTreePart().add(FindingsViewColumn.SUMMARY);
		p.getMutableTablePart().add(FindingsViewColumn.LOC);
		p.getMutableTablePart().add(FindingsViewColumn.CATEGORY);
		p.getMutableTablePart().add(FindingsViewColumn.MNEMONIC);
		f_viewOrganizations.put("Tool", p);
	}

	/**
	 * Initializes this model. This must be invoked before for the model to
	 * function properly.
	 */
	public void init() {
		load();
		Projects.getInstance().addObserver(f_prjObs);
		//f_prjObs.notify(Projects.getInstance());
	}

	public void dispose() {
		Projects.getInstance().removeObserver(f_prjObs);
		f_observers.clear();
		save();
	}

	/*
	 * The view organizations managed by this model.
	 */

	private final Map<String, FindingsViewOrganization> f_viewOrganizations = new HashMap<String, FindingsViewOrganization>();

	public FindingsViewOrganization getViewOrganization() {
		return f_viewOrganizations.get(f_viewOrganizationFocus);
	}

	public FindingsViewOrganization getViewOrganization(final String key) {
		return f_viewOrganizations.get(key);
	}

	public String[] getViewOrganizationKeys() {
		return f_viewOrganizations.keySet().toArray(
				new String[f_viewOrganizations.keySet().size()]);
	}

	/**
	 * Only invoked by {@link FindingsViewOrganization}.
	 */
	void findingsOrganizationChanged() {
		for (IFindingsViewModelObserver o : f_observers)
			o.findingsOrganizationChanged(this);
	}

	/**
	 * The organization of focus, should not be <code>null</code> and it
	 * should be true that
	 * 
	 * <pre>
	 * f_viewOrganizations.keySet().contains(f_organizationFocus)
	 * </pre>
	 */
	private String f_viewOrganizationFocus = "Importance";

	/**
	 * Gets the organization of focus, should not be <code>null</code>.
	 * 
	 * @return The organization of focus,or <code>null</code>.
	 */
	public String getViewOrganizationFocus() {
		return f_viewOrganizationFocus;
	}

	/**
	 * Sets the organization focus of this model to to the passed name. The
	 * organization must exist within this model.
	 * 
	 * @param organizationName
	 *            the organization name.
	 * @throws IllegalStateException
	 *             if the organization name does not exist within this model.
	 */
	public void setViewOrganizationFocus(String organizationName) {
		if (!f_viewOrganizations.keySet().contains(organizationName))
			throw new IllegalStateException("'" + organizationName
					+ "' not the name of an organization");
		if (!f_viewOrganizationFocus.equals(organizationName)) {
			f_viewOrganizationFocus = organizationName;
			for (IFindingsViewModelObserver o : f_observers)
				o.findingsOrganizationFocusChanged(this);
		}
	}

	/*
	 * The view filter of this model.
	 */

	/**
	 * A model that reflects what findings the user wants to be displayed.
	 */
	private final FindingsViewFilter f_filter = new FindingsViewFilter(this);

	/**
	 * Gets the findings filter for this model.
	 * 
	 * @return the findings filter for this model.
	 */
	public FindingsViewFilter getFilter() {
		return f_filter;
	}

	/**
	 * Only invoked by {@link FindingsViewFilter}.
	 */
	void findingsFilterChanged() {
		for (IFindingsViewModelObserver o : f_observers)
			o.findingsFilterChanged(this);
	}

	/*
	 * The project focus of this model.
	 */

	/**
	 * The project of focus, if a project exists in the database then the view
	 * is shows a project. If no project exists in the database than this field
	 * will be <code>null</code>.
	 */
	private String f_projectFocus = null;

	/**
	 * Gets the project of focus, if a project exists in the database then the
	 * view is shows a project. If no project exists in the database than this
	 * field will be <code>null</code>.
	 * 
	 * @return The project of focus, or <code>null</code>.
	 */
	public String getProjectFocus() {
		return f_projectFocus;
	}

	/**
	 * Sets the project focus of this model to the passed project name. It is an
	 * invariant that the passed project name must be in the set of projects
	 * within the database, thus
	 * 
	 * <pre>
	 * Projects.getInstance()..contains(projectName)
	 * </pre>
	 * 
	 * must be true.
	 * 
	 * @param projectName
	 *            the project name to focus on.
	 * @throws IllegalStateException
	 *             if the project name is not in the database.
	 */
	public void setProjectFocus(String projectName) {
		if (!Projects.getInstance().contains(projectName))
			throw new IllegalStateException("'" + projectName
					+ "' not in the database");
		if (f_projectFocus == null || !f_projectFocus.equals(projectName)) {
			f_projectFocus = projectName;
			for (IFindingsViewModelObserver o : f_observers)
				o.projectFocusChanged(this);
		}
	}

	private final IProjectsObserver f_prjObs = new IProjectsObserver() {
		public void notify(Projects p) {
			/*
			 * Something has changed in the list of projects.
			 */
			final boolean atLeastOneProject = !Projects.getInstance().isEmpty();
			if (atLeastOneProject) {
				projectNamesChanged();
			} else {
				noProjects();
			}
		}
	};

	private void projectNamesChanged() {
		for (IFindingsViewModelObserver o : f_observers)
			o.projectListChanged(this);
		if (!Projects.getInstance().contains(f_projectFocus)) {
			setProjectFocus(Projects.getInstance().getFirst());
		}
	}

	private void noProjects() {
		if (f_projectFocus != null) {
			f_projectFocus = null;
			for (IFindingsViewModelObserver o : f_observers)
				o.noProjects(this);
		}
	}

	/*
	 * Observers of this model.
	 */

	private final Set<IFindingsViewModelObserver> f_observers = new CopyOnWriteArraySet<IFindingsViewModelObserver>();

	/**
	 * Adds an observer of this model.
	 * 
	 * @param o
	 *            the observer to add.
	 */
	public void addObserver(final IFindingsViewModelObserver o) {
		f_observers.add(o);
	}

	/**
	 * Removes an observer of this model. If the passed object is not an
	 * observer of this model then this method has no effect.
	 * 
	 * @param o
	 *            the observer to remove.
	 */
	public void removeObserver(final IFindingsViewModelObserver o) {
		f_observers.remove(o);
	}

	/*
	 * Persistence of this model.
	 */

	/**
	 * The save file for this model.
	 */
	private final File f_saveFile;

	private void save() {
		// TODO: persist this model
	}

	private void load() {
		// TODO: load this model.
	}
}
