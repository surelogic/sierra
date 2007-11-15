package com.surelogic.sierra.schema;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.surelogic.common.jdbc.SchemaAction;
import com.surelogic.sierra.findbugs1_3_0.FindBugs1_3_0ToolInfoGenerator;
import com.surelogic.sierra.jdbc.tool.FindingTypeManager;
import com.surelogic.sierra.tool.message.FindingTypes;
import com.surelogic.sierra.tool.message.MessageWarehouse;

public class Schema_0006 implements SchemaAction {

	public void run(Connection conn) throws SQLException {
		FindBugs1_3_0ToolInfoGenerator.generateTool(conn);

		MessageWarehouse mw = MessageWarehouse.getInstance();
		FindingTypeManager ftMan = FindingTypeManager.getInstance(conn);
		List<FindingTypes> types = new ArrayList<FindingTypes>(3);
		InputStream in = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream(
						"com/surelogic/sierra/tool/message/findbugs.xml");
		types.add(mw.fetchFindingTypes(in));
		in = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream(
						"com/surelogic/sierra/tool/message/pmd.xml");
		types.add(mw.fetchFindingTypes(in));
		in = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream(
						"com/surelogic/sierra/tool/message/checkstyle.xml");
		types.add(mw.fetchFindingTypes(in));
		ftMan.updateFindingTypes(types);

		conn.commit();
	}
}
