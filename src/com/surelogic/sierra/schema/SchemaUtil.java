package com.surelogic.sierra.schema;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.surelogic.common.jdbc.ConnectionQuery;
import com.surelogic.common.jdbc.Query;
import com.surelogic.common.jdbc.StringRowHandler;
import com.surelogic.sierra.jdbc.settings.ScanFilterDO;
import com.surelogic.sierra.jdbc.settings.ScanFilters;
import com.surelogic.sierra.jdbc.settings.SettingQueries;
import com.surelogic.sierra.jdbc.settings.TypeFilterDO;
import com.surelogic.sierra.jdbc.tool.FindingTypeManager;
import com.surelogic.sierra.tool.message.FindingTypes;
import com.surelogic.sierra.tool.message.MessageWarehouse;

public class SchemaUtil {
	static void updateFindingTypes(Connection conn) throws SQLException {
		final FindingTypeManager ftMan = FindingTypeManager.getInstance(conn);
		final List<FindingTypes> types = new ArrayList<FindingTypes>(3);

		types.add(getFindingTypes("findbugs.xml"));
		types.add(getFindingTypes("pmd.xml"));
		types.add(getFindingTypes("cpd.xml"));
		types.add(getFindingTypes("checkstyle.xml"));
		ftMan.updateFindingTypes(types, 0);
	}

	private static FindingTypes getFindingTypes(String file) {
		final InputStream in = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream(
						"com/surelogic/sierra/tool/message/" + file);
		return MessageWarehouse.getInstance().fetchFindingTypes(in);
	}

	static void setupFilters(Connection c) throws SQLException {
		final Query q = new ConnectionQuery(c);
		final ScanFilters filters = new ScanFilters(q);
		if (filters.getScanFilter(SettingQueries.GLOBAL_UUID) == null) {
			final Set<String> excluded = SettingQueries
					.getSureLogicDefaultScanFilters();
			final ScanFilterDO filter = new ScanFilterDO();
			filter.setName(SettingQueries.GLOBAL_NAME);
			filter.setUid(SettingQueries.GLOBAL_UUID);
			filter.setRevision(0L);
			final Set<TypeFilterDO> typeFs = filter.getFilterTypes();
			for (final String type : q.statement(
					"FindingTypes.listFindingTypes", new StringRowHandler())
					.call()) {
				if (!excluded.contains(type)) {
					typeFs.add(new TypeFilterDO(type, null, false));
				}
			}
			filters.writeScanFilter(filter);
		}
	}
}
