package com.surelogic.sierra.jdbc.tool;

import java.util.ArrayList;
import java.util.List;

import com.surelogic.common.jdbc.Query;
import com.surelogic.common.jdbc.Row;
import com.surelogic.common.jdbc.RowHandler;
import com.surelogic.common.jdbc.SingleRowHandler;
import com.surelogic.common.jdbc.StringRowHandler;

public class FindingTypes {

	private final Query q;

	public FindingTypes(Query q) {
		this.q = q;
	}

	public List<FindingTypeDO> listFindingTypes() {
		final List<FindingTypeDO> list = new ArrayList<FindingTypeDO>();
		for (final String uid : q.statement("FindingTypes.listFindingTypes",
				new StringRowHandler()).call()) {
			list.add(getFindingType(uid));
		}
		return list;
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
