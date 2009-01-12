package com.surelogic.sierra.gwt.server;

import java.util.Set;

import com.surelogic.sierra.gwt.client.data.ImportanceView;
import com.surelogic.sierra.gwt.client.data.ScanFilter;
import com.surelogic.sierra.gwt.client.data.ScanFilterEntry;
import com.surelogic.sierra.jdbc.settings.Categories;
import com.surelogic.sierra.jdbc.settings.CategoryDO;
import com.surelogic.sierra.jdbc.settings.CategoryFilterDO;
import com.surelogic.sierra.jdbc.settings.ScanFilterDO;
import com.surelogic.sierra.jdbc.settings.TypeFilterDO;
import com.surelogic.sierra.jdbc.tool.FindingTypeDO;
import com.surelogic.sierra.jdbc.tool.FindingTypes;
import com.surelogic.sierra.tool.message.Importance;

/**
 * A few utility methods that are useful in multiple services.
 * 
 * @author nathan
 * 
 */
public class ServiceUtil {

	static Importance importance(final ImportanceView i) {
		if (i == null) {
			return null;
		}
		if (i == ImportanceView.CRITICAL) {
			return Importance.CRITICAL;
		} else if (i == ImportanceView.HIGH) {
			return Importance.HIGH;
		} else if (i == ImportanceView.IRRELEVANT) {
			return Importance.IRRELEVANT;
		} else if (i == ImportanceView.LOW) {
			return Importance.LOW;
		} else if (i == ImportanceView.MEDIUM) {
			return Importance.MEDIUM;
		}
		throw new IllegalStateException();
	}

	static ImportanceView view(final Importance i) {
		if (i == null) {
			return null;
		}
		switch (i) {
		case CRITICAL:
			return ImportanceView.CRITICAL;
		case HIGH:
			return ImportanceView.HIGH;
		case IRRELEVANT:
			return ImportanceView.IRRELEVANT;
		case LOW:
			return ImportanceView.LOW;
		case MEDIUM:
			return ImportanceView.MEDIUM;
		}
		throw new IllegalStateException();
	}

	static ScanFilter getFilter(final ScanFilterDO fDO, final FindingTypes ft,
			final Categories cs) {
		final ScanFilter f = new ScanFilter();
		f.setName(fDO.getName());
		f.setRevision(fDO.getRevision());
		f.setUuid(fDO.getUid());
		Set<ScanFilterEntry> filters = f.getCategories();
		for (final CategoryFilterDO c : fDO.getCategories()) {
			final ScanFilterEntry e = new ScanFilterEntry();
			e.setCategory(true);
			e.setImportance(view(c.getImportance()));
			final CategoryDO catDO = cs.getCategory(c.getUid());
			e.setName(catDO.getName());
			e.setShortMessage(catDO.getInfo());
			e.setUuid(c.getUid());
			filters.add(e);
		}
		filters = f.getTypes();
		for (final TypeFilterDO t : fDO.getFilterTypes()) {
			final ScanFilterEntry e = new ScanFilterEntry();
			e.setCategory(false);
			e.setImportance(view(t.getImportance()));
			final FindingTypeDO tDO = ft.getFindingType(t.getFindingType());
			e.setName(tDO.getName());
			e.setShortMessage(tDO.getShortMessage());
			e.setUuid(tDO.getUid());
			filters.add(e);
		}
		return f;
	}

}
