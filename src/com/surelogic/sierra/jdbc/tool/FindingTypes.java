package com.surelogic.sierra.jdbc.tool;

import com.surelogic.sierra.jdbc.Query;
import com.surelogic.sierra.jdbc.Row;
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
		return q.prepared("FindingTypes.findByUid", new FindingTypeDOHandler())
				.call(uid);
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

}
