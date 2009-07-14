package com.surelogic.sierra.client.eclipse.wizards;

import java.io.*;
import java.util.*;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IWorkbench;

import com.surelogic.sierra.jdbc.settings.CategoryDO;
import com.surelogic.sierra.jdbc.tool.FindingTypeDO;
import com.surelogic.sierra.tool.ArtifactType;
import com.surelogic.sierra.tool.ToolUtil;

public class ArtifactTypeSetupWizard extends Wizard {
	private final Collection<ArtifactType> types;
	private final List<FindingTypeDO> findingTypes;
	private final List<CategoryDO> categories;
	private ArtifactTypeMappingPage f_mappingPage;
	private FindingTypeSetupPage f_findingTypePage;
	private ManifestLocationPage f_manifestLocationPage;
	
	
	public ArtifactTypeSetupWizard(List<ArtifactType> t, List<FindingTypeDO> ft, 
			                       List<CategoryDO> cats) {
		types = t;
		findingTypes = ft;
		categories = cats;
	}

	public void init(IWorkbench workbench, IStructuredSelection selection) {
		setWindowTitle("Setup Artifact Types");
	}
	
	@Override
	public void addPages() {
		f_mappingPage = new ArtifactTypeMappingPage(types, findingTypes);
		f_findingTypePage = new FindingTypeSetupPage(types, categories);
		f_manifestLocationPage = new ManifestLocationPage();
		addPage(f_mappingPage);
		addPage(f_findingTypePage);
		addPage(f_manifestLocationPage);
	}
	
	@Override
	public boolean performFinish() {
		/*
		System.out.println("Location: "+f_manifestLocationPage.getLocation());
		PrintStream out = System.out;
		*/
		try {
			FileWriter w = new FileWriter(f_manifestLocationPage.getLocation());
			PrintWriter out = new PrintWriter(w);
			out.println("Manifest-Version: 1.0\n");
			out.println("Name: "+ToolUtil.FINDING_TYPE_MAPPING_KEY);
			List<ArtifactType> unmapped = new ArrayList<ArtifactType>();
			for(final ArtifactType t : types) {
				final String fType = t.getFindingType();
				if (mappedToFindingType(fType)) {
					out.println(t.type+": "+fType);
				} else {
					unmapped.add(t);
				}
			}
			out.println();
			out.println("Name: "+ToolUtil.CATEGORY_MAPPING_KEY);
			for(final ArtifactType t : unmapped) {
				out.println(t.type+": "+t.getCategory());			
			}
			out.println();
			out.println("Name: "+ToolUtil.SCAN_FILTER_BLACKLIST_KEY);
			for(final ArtifactType t : unmapped) {
				if (!t.includeInScan()) {
					out.println(t.type+": ignore");			
				}
			}
			out.println();
			out.close();
		} catch(IOException e) {
			return false;
		}	
		return true;
	}

	private boolean mappedToFindingType(final String fType) {
		return fType != null && !AbstractArtifactTypePage.DEFAULT.equals(fType);
	}
}
