package com.surelogic.sierra.client.eclipse.wizards;

import java.util.Collection;
import java.util.List;

import org.eclipse.swt.widgets.Combo;

import com.surelogic.sierra.jdbc.tool.FindingTypeDO;
import com.surelogic.sierra.tool.ArtifactType;

public class ArtifactTypeMappingPage extends AbstractArtifactTypePage {
	private final List<FindingTypeDO> findingTypes;

	protected ArtifactTypeMappingPage(final Collection<ArtifactType> t,
			final List<FindingTypeDO> ft) {
		super("ArtifactTypeMappingPage", t, "Finding Type");
		findingTypes = ft;

		setTitle("Map Artifact Types");
		setDescription("Map each of the artifact types below to an appropriate finding type"
				+ " (or <create>).");
	}

	@Override
	protected void initCombo(final Combo c) {
		for (final FindingTypeDO f : findingTypes) {
			c.add(f.getName());
		}
	}

	@Override
	protected String convertFromName(final ArtifactType t, final String name) {
		if (name.equals(DEFAULT)) {
			return t.setFindingType(null);
		}
		for (final FindingTypeDO f : findingTypes) {
			if (f.getName().equals(name)) {
				return t.setFindingType(f.getUid());
			}
		}
		return t.setFindingType(name);
	}

	@Override
	protected String convertToName(final ArtifactType t) {
		final String id = t.getFindingType();
		if (t.type.equals(id)) {
			return DEFAULT;
		}
		for (final FindingTypeDO f : findingTypes) {
			if (f.getUid().equals(id)) {
				return f.getName();
			}
		}
		return "";
	}

	@Override
	protected final void setOKState() {
		setPageComplete(true);
		((FindingTypeSetupPage) getNextPage()).update();
	}
}
