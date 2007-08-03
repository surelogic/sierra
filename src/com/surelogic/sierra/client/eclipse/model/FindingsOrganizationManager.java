package com.surelogic.sierra.client.eclipse.model;

import java.util.HashMap;
import java.util.Map;

public final class FindingsOrganizationManager {

	private final Map<String, FindingsOrganization> f_organizations = new HashMap<String, FindingsOrganization>();

	public FindingsOrganizationManager() {
		FindingsOrganization p = new FindingsOrganization();
		p.getMutableTreePart().add(FindingsColumn.IMPORTANCE);
		p.getMutableTreePart().add(FindingsColumn.PACKAGE_NAME);
		p.getMutableTreePart().add(FindingsColumn.CLASS_NAME);
		p.getMutableTreePart().add(FindingsColumn.SUMMARY);
		p.getMutableTablePart().add(FindingsColumn.LOC);
		p.getMutableTablePart().add(FindingsColumn.TOOL);
		p.getMutableTablePart().add(FindingsColumn.CATEGORY);
		p.getMutableTablePart().add(FindingsColumn.MNEMONIC);
		f_organizations.put("Importance", p);
		p = new FindingsOrganization();
		p.getMutableTreePart().add(FindingsColumn.PACKAGE_NAME);
		p.getMutableTreePart().add(FindingsColumn.CLASS_NAME);
		p.getMutableTreePart().add(FindingsColumn.SUMMARY);
		p.getMutableTablePart().add(FindingsColumn.IMPORTANCE);
		p.getMutableTablePart().add(FindingsColumn.LOC);
		p.getMutableTablePart().add(FindingsColumn.TOOL);
		p.getMutableTablePart().add(FindingsColumn.CATEGORY);
		p.getMutableTablePart().add(FindingsColumn.MNEMONIC);
		f_organizations.put("Package", p);
		p = new FindingsOrganization();
		p.getMutableTreePart().add(FindingsColumn.TOOL);
		p.getMutableTreePart().add(FindingsColumn.IMPORTANCE);
		p.getMutableTreePart().add(FindingsColumn.PACKAGE_NAME);
		p.getMutableTreePart().add(FindingsColumn.CLASS_NAME);
		p.getMutableTreePart().add(FindingsColumn.SUMMARY);
		p.getMutableTablePart().add(FindingsColumn.LOC);
		p.getMutableTablePart().add(FindingsColumn.CATEGORY);
		p.getMutableTablePart().add(FindingsColumn.MNEMONIC);
		f_organizations.put("Tool", p);
	}

	public FindingsOrganization get(final String key) {
		return f_organizations.get(key);
	}

	public String[] getKeys() {
		return f_organizations.keySet().toArray(
				new String[f_organizations.keySet().size()]);
	}
}
