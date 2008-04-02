package com.surelogic.sierra.schema;

import java.sql.Connection;
import java.sql.SQLException;

import com.surelogic.common.jdbc.SchemaAction;
import com.surelogic.sierra.jdbc.ConnectionQuery;
import com.surelogic.sierra.jdbc.settings.FilterSetDO;
import com.surelogic.sierra.jdbc.settings.FilterSets;

public class Schema_0026 implements SchemaAction {

	public void run(Connection c) throws SQLException {
		final FilterSets sets = new FilterSets(new ConnectionQuery(c));
		for (final FilterSetDO set : sets.listFilterSets()) {
			sets.deleteFilterSet(set.getUid());
		}
		SchemaUtil.updateFindingTypes(c);
		SchemaUtil.setupFilters(c);
	}

}
