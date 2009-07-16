package com.surelogic.sierra.schema;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import com.surelogic.common.jdbc.ConnectionQuery;
import com.surelogic.common.jdbc.Query;
import com.surelogic.common.jdbc.Row;
import com.surelogic.common.jdbc.RowHandler;
import com.surelogic.common.jdbc.StringResultHandler;
import com.surelogic.common.jdbc.StringRowHandler;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.jdbc.settings.ScanFilterDO;
import com.surelogic.sierra.jdbc.settings.ScanFilters;
import com.surelogic.sierra.jdbc.settings.SettingQueries;
import com.surelogic.sierra.jdbc.settings.TypeFilterDO;
import com.surelogic.sierra.jdbc.tool.FindingTypeManager;
import com.surelogic.sierra.tool.message.FilterEntry;
import com.surelogic.sierra.tool.message.FilterSet;
import com.surelogic.sierra.tool.message.FindingTypes;
import com.surelogic.sierra.tool.message.ListCategoryResponse;
import com.surelogic.sierra.tool.message.MessageWarehouse;

public class SchemaUtil {

	private static final String BUGLINK = "9a997ac4-ec2b-4d02-869a-423999fecfed";

	static void updateFindingTypes(final Connection conn) throws SQLException {
		final FindingTypeManager ftMan = FindingTypeManager.getInstance(conn);
		final List<FindingTypes> types = new ArrayList<FindingTypes>(3);
		types.add(getFindingTypes("buglink-finding-types.xml"));
//		types.add(getFindingTypes("jsure-finding-types.xml"));
		ftMan.updateFindingTypes(types, 0);
	}

	private static FindingTypes getFindingTypes(final String file) {
		final InputStream in = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream(
						"com/surelogic/sierra/tool/message/data/" + file);
		return MessageWarehouse.getInstance().fetchFindingTypes(in);
	}

	static void setupCategories(final Connection c) throws SQLException {
		final Query q = new ConnectionQuery(c);
		try {
			final JAXBContext ctx = JAXBContext
					.newInstance(ListCategoryResponse.class);
			final Unmarshaller unmarshaller = ctx.createUnmarshaller();
			final InputStream in = Thread
					.currentThread()
					.getContextClassLoader()
					.getResourceAsStream(
							"com/surelogic/sierra/tool/message/data/buglink-categories.xml");
			final ListCategoryResponse response = (ListCategoryResponse) unmarshaller
					.unmarshal(in);
			SettingQueries.updateCategories(response, true).perform(q);
			final List<String> orphanedTypes = q.prepared(
					"SchemaUtil.checkFindingTypeCategories",
					new StringRowHandler()).call();
			if (!orphanedTypes.isEmpty()) {
				SLLogger
						.getLoggerFor(SchemaUtil.class)
						.warning(
								"The following finding types do not currently belong to a category of any sort: "
										+ orphanedTypes);
			}
		} catch (final JAXBException e) {
			throw new IllegalStateException(e);
		}
	}

	static void setupLocalScanFilter(final Connection c) throws SQLException {
		final Query q = new ConnectionQuery(c);
		final ScanFilters filters = new ScanFilters(q);
		if (filters.getScanFilter(SettingQueries.LOCAL_UUID) == null) {
			final Set<String> included = SettingQueries
					.getSureLogicDefaultScanFilters();
			final Set<String> exists = new HashSet<String>();
			final ScanFilterDO filter = new ScanFilterDO();
			filter.setName(SettingQueries.LOCAL_NAME);
			filter.setUid(SettingQueries.LOCAL_UUID);
			filter.setRevision(0L);
			final Set<TypeFilterDO> typeFs = filter.getFilterTypes();
			for (final String type : q.statement(
					"FindingTypes.listFindingTypes", new StringRowHandler())
					.call()) {
				if (included.contains(type)) {
					exists.add(type);
					typeFs.add(new TypeFilterDO(type, null, false));
				}
			}
			if (!exists.equals(included)) {
				included.removeAll(exists);
				throw new IllegalStateException(
						String
								.format(
										"The following finding types do not exist in the database: %s",
										included));
			}
			filters.writeScanFilter(filter);
		}
		if (q.prepared("ScanFilters.selectDefault", new StringResultHandler())
				.call() == null) {
			q.prepared("ScanFilters.insertDefault").call(
					SettingQueries.LOCAL_UUID);
		}
	}

	/*
	 * The client sets up a purely local copy of the settings. When we boot the
	 * server, we want to change this local copy to a server-owned copy.
	 */
	static void setupServerScanFilter(final Connection c,
			final String serverUuid) {
		final Query q = new ConnectionQuery(c);
		if (q.prepared("ScanFilters.selectDefault", new StringResultHandler())
				.call().equals(SettingQueries.LOCAL_UUID)) {
			final String newUuid = UUID.randomUUID().toString();
			q.prepared("ScanFilters.updateUuid").call(newUuid,
					SettingQueries.LOCAL_UUID);
			q.prepared("Definitions.insertDefinition")
					.call(newUuid, serverUuid);
			q.prepared("ScanFilters.updateDefault").call(newUuid);
		}
	}

	/*
	 * This method is for writing out an initial set of categories for each
	 * tool. It should not be called every time we update finding type
	 * information.
	 */
	static void generateBuglinkCategories(final Connection c) {
		final Query q = new ConnectionQuery(c);
		final ListCategoryResponse r = new ListCategoryResponse();
		String current = null;
		FilterSet cat = new FilterSet();
		for (final CatEntry entry : q
				.prepared("SchemaUtil.categoriesFromArtifactTypes",
						new CatEntryHandler()).call()) {
			final String name = entry.cat + " (" + entry.tool + ")";
			if (!name.equals(current)) {
				current = name;
				cat = new FilterSet();
				cat.setUid(current);
				cat.setName(current);
				cat.setOwner(BUGLINK);
				cat.setRevision(0L);
				cat.setInfo("The set of finding types reported by the '"
						+ entry.cat + "' category in " + entry.tool + ".");
				r.getFilterSets().add(cat);
			}
			final FilterEntry fe = new FilterEntry();
			fe.setFiltered(false);
			fe.setType(entry.ft);
			cat.getFilter().add(fe);
		}
		SettingQueries.updateCategories(r, true).perform(q);
		final List<String> orphanedTypes = q
				.prepared("SchemaUtil.checkFindingTypeCategories",
						new StringRowHandler()).call();
		if (!orphanedTypes.isEmpty()) {
			SLLogger
					.getLoggerFor(SchemaUtil.class)
					.warning(
							"The following finding types do not currently belong to a category of any sort: "
									+ orphanedTypes);
		}
	}

	private static class CatEntry {
		String ft;
		String cat;
		String tool;
	}

	private static class CatEntryHandler implements RowHandler<CatEntry> {
		public CatEntry handle(final Row r) {
			final CatEntry ce = new CatEntry();
			ce.tool = r.nextString();
			ce.cat = r.nextString();
			ce.ft = r.nextString();
			return ce;
		}

	}

}
