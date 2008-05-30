package com.surelogic.sierra.schema;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import com.surelogic.common.jdbc.ConnectionQuery;
import com.surelogic.common.jdbc.Query;
import com.surelogic.common.jdbc.StringRowHandler;
import com.surelogic.sierra.jdbc.settings.ScanFilterDO;
import com.surelogic.sierra.jdbc.settings.ScanFilters;
import com.surelogic.sierra.jdbc.settings.SettingQueries;
import com.surelogic.sierra.jdbc.settings.TypeFilterDO;
import com.surelogic.sierra.jdbc.tool.FindingTypeManager;
import com.surelogic.sierra.tool.message.FindingTypes;
import com.surelogic.sierra.tool.message.ListCategoryResponse;
import com.surelogic.sierra.tool.message.MessageWarehouse;

public class SchemaUtil {
	static void updateFindingTypes(Connection conn) throws SQLException {
		final FindingTypeManager ftMan = FindingTypeManager.getInstance(conn);
		final List<FindingTypes> types = new ArrayList<FindingTypes>(3);

		types.add(getFindingTypes("findbugs.xml"));
		types.add(getFindingTypes("pmd.xml"));
		types.add(getFindingTypes("cpd.xml"));
		types.add(getFindingTypes("jsure.xml"));
		types.add(getFindingTypes("checkstyle.xml"));
		ftMan.updateFindingTypes(types, 0);
	}

	private static FindingTypes getFindingTypes(String file) {
		final InputStream in = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream(
						"com/surelogic/sierra/tool/message/" + file);
		return MessageWarehouse.getInstance().fetchFindingTypes(in);
	}

	static void setupCategories(Connection c) throws SQLException {
		final Query q = new ConnectionQuery(c);
		try {
			final JAXBContext ctx = JAXBContext
					.newInstance(ListCategoryResponse.class);
			final Unmarshaller unmarshaller = ctx.createUnmarshaller();
			final ListCategoryResponse response = (ListCategoryResponse) unmarshaller
					.unmarshal(Thread
							.currentThread()
							.getContextClassLoader()
							.getResourceAsStream(
									"com/surelogic/sierra/jdbc/settings/buglink-categories.xml"));
			SettingQueries.updateCategories(response, true).perform(q);
		} catch (final JAXBException e) {
			throw new IllegalStateException(e);
		}
	}

	static void setupScanFilters(Connection c) throws SQLException {
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
