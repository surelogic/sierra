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
	private Map<String,List<ArtifactType>> f_plugins;
	
	
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
		
		f_plugins = organizeTypes();
		f_manifestLocationPage = new ManifestLocationPage(f_plugins);
		addPage(f_mappingPage);
		addPage(f_findingTypePage);
		addPage(f_manifestLocationPage);
	}
	
	private Map<String,List<ArtifactType>> organizeTypes() {
		Map<String,List<ArtifactType>> plugins = new HashMap<String,List<ArtifactType>>();
		for(ArtifactType t : types) {
			List<ArtifactType> at = plugins.get(t.plugin);
			if (at == null) {
				at = new ArrayList<ArtifactType>();
				plugins.put(t.plugin, at);
			}
			at.add(t);
		}
		return plugins;
	}
	
	@Override
	public boolean performFinish() {
		Map<String,String> locations = f_manifestLocationPage.getLocationMap();
		for(Map.Entry<String,List<ArtifactType>> plugin : f_plugins.entrySet()) {
			try {
				FileWriter w = new FileWriter(locations.get(plugin.getKey()));
				PrintWriter out = new PrintWriter(w);
				out.println("Manifest-Version: 1.0\n");
				out.println("Name: "+ToolUtil.FINDING_TYPE_MAPPING_KEY);
				List<ArtifactType> unmapped = new ArrayList<ArtifactType>();
				for(final ArtifactType t : plugin.getValue()) {
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
		}
		return true;
	}

	private boolean mappedToFindingType(final String fType) {
		return fType != null && !AbstractArtifactTypePage.DEFAULT.equals(fType);
	}
}
