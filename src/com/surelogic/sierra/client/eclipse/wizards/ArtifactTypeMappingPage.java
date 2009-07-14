package com.surelogic.sierra.client.eclipse.wizards;

import java.util.Collection;
import java.util.List;

import org.eclipse.swt.widgets.Combo;

import com.surelogic.sierra.jdbc.tool.FindingTypeDO;
import com.surelogic.sierra.tool.ArtifactType;

public class ArtifactTypeMappingPage extends AbstractArtifactTypePage {	
	private final List<FindingTypeDO> findingTypes;
	
	protected ArtifactTypeMappingPage(Collection<ArtifactType> t, List<FindingTypeDO> ft) {
		super("ArtifactTypeMappingPage", t, "Finding Type");
		findingTypes = ft;

		setTitle("Map Artifact Types");
		setDescription("Map each of the artifact types below to an appropriate finding type"+
				       " (or <create>).");
	}
	
	@Override
	protected void initCombo(Combo c) {
	    for(FindingTypeDO f : findingTypes) {
	    	c.add(f.getName());
	    }			
	}
	
	@Override
	protected String convertFromName(ArtifactType t, String name) {
		if (name == DEFAULT) {
			return t.setFindingType(t.type);
		}
		for(FindingTypeDO f : findingTypes) {
			if (f.getName().equals(name)) {
				return t.setFindingType(f.getUid());
			}
		}
		return t.setFindingType(name);
	}
	
	@Override
	protected String convertToName(ArtifactType t) {
		final String id = t.getFindingType();
		if (t.type.equals(id)) {
			return DEFAULT;
		}		
		for(FindingTypeDO f : findingTypes) {
			if (f.getUid().equals(id)) {
				return f.getName();
			}
		}
		return "";
	}
	
	@Override
	protected final void setOKState() {
		boolean allHaveFindingTypes = true;
		for(ArtifactType t : types) {
			if (t.getFindingType() == null) {
				allHaveFindingTypes = false;
				break;
			}
		}		
		setPageComplete(allHaveFindingTypes);
		if (allHaveFindingTypes) {
			((FindingTypeSetupPage) getNextPage()).update();
		}
	}
}
