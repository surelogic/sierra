package com.surelogic.sierra.jdbc.tool;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.surelogic.common.jdbc.Query;
import com.surelogic.common.jdbc.Queryable;
import com.surelogic.common.jdbc.Row;
import com.surelogic.common.jdbc.RowHandler;
import com.surelogic.common.jdbc.SingleRowHandler;
import com.surelogic.common.jdbc.StringRowHandler;

public class FindingTypes {

	private final Query q;

	public FindingTypes(final Query q) {
		this.q = q;
	}

	public List<FindingTypeDO> listFindingTypes() {
		return getFindingTypes(q.statement("FindingTypes.listFindingTypes",
				new StringRowHandler()).call());
	}

	public List<FindingTypeDO> getFindingTypes(final Collection<String> uids) {
		final List<FindingTypeDO> list = new ArrayList<FindingTypeDO>();
		final Queryable<FindingTypeDO> getType = q.prepared(
				"FindingTypes.findByUid", new FindingTypeDOHandler());
		final Queryable<List<ArtifactTypeDO>> getArts = q.prepared(
				"FindingTypes.findArtifactTypeById",
				new ArtifactTypeDOHandler());
		for (final String uid : uids) {
			if (uid == null) {
				throw new IllegalArgumentException("May not be null");
			}
			final FindingTypeDO t = getType.call(uid);
			t.getArtifactTypes().addAll(getArts.call(t.getId()));
			list.add(t);
		}
		return list;
	}

	public FindingTypeDO getFindingType(final String uid) {
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

	public FindingTypeDO getFindingType(final long id) {
		return q.prepared("FindingTypes.findById", new FindingTypeDOHandler())
				.call(id);
	}

	private static class FindingTypeDOHandler extends
			SingleRowHandler<FindingTypeDO> {
		@Override
		public FindingTypeDO handleRow(final Row r) {
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
		public ArtifactTypeDO handle(final Row r) {
			return new ArtifactTypeDO(r.nextLong(), r.nextString(), r
					.nextString());
		}

	}

}
