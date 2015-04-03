package com.surelogic.sierra.gwt.client.content.extensions;

import java.util.List;

import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.surelogic.sierra.gwt.client.content.ContentComposite;
import com.surelogic.sierra.gwt.client.content.findingtypes.FindingTypesContent;
import com.surelogic.sierra.gwt.client.data.Extension;
import com.surelogic.sierra.gwt.client.data.Extension.ArtifactType;
import com.surelogic.sierra.gwt.client.data.Extension.FindingType;
import com.surelogic.sierra.gwt.client.ui.panel.BasicPanel;
import com.surelogic.sierra.gwt.client.ui.panel.ListPanel;

public class ExtensionView extends BasicPanel {

	private final Label description = new Label(
			"An extension adds new capabilities to the tool analysis engine, including additional artifact and finding types.");
	private final ArtifactTypePanel artifactTypes = new ArtifactTypePanel();
	private final FindingTypePanel findingTypes = new FindingTypePanel();

	private Extension extension;

	@Override
	protected void onInitialize(final VerticalPanel contentPanel) {
		description.addStyleName("padded");
		artifactTypes.initialize();
		findingTypes.initialize();
		contentPanel.add(description);
		contentPanel.add(artifactTypes);
		contentPanel.add(findingTypes);
	}

	public Extension getExtension() {
		return extension;
	}

	public void setExtension(final Extension extension) {
		setSummary(extension.getName() + " &ndash; " + extension.getVersion());
		this.extension = extension;
		update();
	}

	private void update() {
		artifactTypes.clear();
		findingTypes.clear();
		if (extension != null) {
			artifactTypes.addItems(extension.getArtifactTypes());
			findingTypes.addItems(extension.getFindingTypes());
		}
	}

	private class ArtifactTypePanel extends BasicPanel {

		@Override
		protected void onInitialize(final VerticalPanel contentPanel) {
			setTitle("New Artifact Types");
			setSubsectionStyle(true);
		}

		public void clear() {
			getContentPanel().clear();
		}

		public void addItems(final List<ArtifactType> info) {
			for (final ArtifactType art : info) {
				getContentPanel().add(
						new HTML(art.getTool() + ": " + art.getName()));
			}
		}
	}

	private class FindingTypePanel extends ListPanel<FindingType> {

		public FindingTypePanel() {
			super("New Finding Types");
		}

		@Override
		protected ContentComposite getItemContent() {
			return FindingTypesContent.getInstance();
		}

		@Override
		protected String getItemText(final FindingType item) {
			return item.getName();
		}

		@Override
		protected String getItemTooltip(final FindingType item) {
			return item.getName();
		}

	}
}
