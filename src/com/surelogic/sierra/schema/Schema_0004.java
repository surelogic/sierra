package com.surelogic.sierra.schema;

import java.sql.Connection;

import com.surelogic.common.jdbc.SchemaAction;

public class Schema_0004 implements SchemaAction {

	public void run(Connection c) {
		// NOTE: moved to Schema_0006

		// MessageWarehouse mw = MessageWarehouse.getInstance();
		// FindingTypeManager ftMan = FindingTypeManager.getInstance(c);
		// List<FindingTypes> types = new ArrayList<FindingTypes>(3);
		// InputStream in = Thread.currentThread().getContextClassLoader()
		// .getResourceAsStream(
		// "com/surelogic/sierra/tool/message/findbugs.xml");
		// types.add(mw.fetchFindingTypes(in));
		// in = Thread.currentThread().getContextClassLoader()
		// .getResourceAsStream(
		// "com/surelogic/sierra/tool/message/pmd.xml");
		// types.add(mw.fetchFindingTypes(in));
		// in = Thread.currentThread().getContextClassLoader()
		// .getResourceAsStream(
		// "com/surelogic/sierra/tool/message/checkstyle.xml");
		// types.add(mw.fetchFindingTypes(in));
		// ftMan.updateFindingTypes(types);
		// c.commit();
	}

}
