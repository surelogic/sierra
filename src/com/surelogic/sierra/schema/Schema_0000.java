package com.surelogic.sierra.schema;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.surelogic.common.jdbc.SchemaAction;
import com.surelogic.sierra.findbugs1_2_1.FindBugs1_2_1ToolInfoGenerator;
import com.surelogic.sierra.jdbc.tool.FindingTypeManager;
import com.surelogic.sierra.pmd3_9.PMD3_9ToolInfoGenerator;
import com.surelogic.sierra.pmd4_0.PMD4_0ToolInfoGenerator;
import com.surelogic.sierra.tool.message.FindingTypes;
import com.surelogic.sierra.tool.message.MessageWarehouse;

public class Schema_0000 implements SchemaAction {

	public void run(Connection conn) throws SQLException {
		PMD3_9ToolInfoGenerator.generateTool(conn);
		PMD4_0ToolInfoGenerator.generateTool(conn);
		FindBugs1_2_1ToolInfoGenerator.generateTool(conn);
		conn.commit();
		MessageWarehouse mw = MessageWarehouse.getInstance();
		FindingTypeManager ftMan = FindingTypeManager.getInstance(conn);
		List<FindingTypes> types = new ArrayList<FindingTypes>(2);
		InputStream in = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream(
						"com/surelogic/sierra/tool/message/findbugs.xml");
		types.add(mw.fetchFindingTypes(in));
		in = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream(
						"com/surelogic/sierra/tool/message/pmd.xml");
		types.add(mw.fetchFindingTypes(in));
		ftMan.updateFindingTypes(types);
		conn.commit();
	}

}
