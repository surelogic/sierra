package com.surelogic.sierra.jdbc.tool;

import com.surelogic.sierra.jdbc.Query;
import com.surelogic.sierra.jdbc.Row;
import com.surelogic.sierra.jdbc.RowHandler;
import com.surelogic.sierra.jdbc.SingleRowHandler;

public class FindingTypes {

	private final Query q;

	public FindingTypes(Query q) {
		this.q = q;
	}

	public FindingTypeDO getFindingType(String uid) {
		if (uid == null) {
			throw new IllegalArgumentException("May not be null");
		}
		final FindingTypeDO t = q.prepared("FindingTypes.findByUid",
				new FindingTypeDOHandler()).call(uid);
		t.getArtifactTypes().addAll(
				q.prepared("FindingTypes.findArtifactTypeById",
						new ArtifactTypeDOHandler()).call(t.getId()));
		return t;
	}

	public FindingTypeDO getFindingType(long id) {
		return q.prepared("FindingTypes.findById", new FindingTypeDOHandler())
				.call(id);
	}

	private static class FindingTypeDOHandler extends
			SingleRowHandler<FindingTypeDO> {
		public FindingTypeDO handleRow(Row r) {
			final FindingTypeDO ft = new FindingTypeDO();
			ft.setId(r.nextLong());
			ft.setUid(r.nextString());
			ft.setName(r.nextString());
			ft.setShortMessage(r.nextString());
			ft.setInfo(r.nextString());
			return ft;
		}
	}

	private static class ArtifactTypeDOHandler implements
			RowHandler<ArtifactTypeDO> {
		public ArtifactTypeDO handle(Row r) {
			return new ArtifactTypeDO(r.nextLong(), r.nextString());
		}

	}

}
