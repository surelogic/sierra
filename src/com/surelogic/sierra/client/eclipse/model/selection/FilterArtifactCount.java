package com.surelogic.sierra.client.eclipse.model.selection;

public final class FilterArtifactCount extends FilterNumberValue {

	public static final ISelectionFilterFactory FACTORY = new AbstractFilterFactory() {
		public Filter construct(FindingSearch selection, Filter previous) {
			return new FilterArtifactCount(selection, previous);
		}

		public String getFilterLabel() {
			return "Tool Artifacts";
		}
	};

	FilterArtifactCount(FindingSearch selection, Filter previous) {
		super(selection, previous);
		f_quote = false;
	}

	@Override
	public ISelectionFilterFactory getFactory() {
		return FACTORY;
	}

	@Override
	protected String getColumnName() {
		return "ARTIFACT_COUNT";
	}
}
