package com.surelogic.sierra.jdbc.tool;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import com.surelogic.sierra.tool.message.FindingTypeFilter;
import com.surelogic.sierra.tool.message.Importance;
import com.surelogic.sierra.tool.message.Priority;
import com.surelogic.sierra.tool.message.Severity;

public class MessageFilter implements FindingFilter {

	private final Set<Long> filtered;
	private final Map<Long, Importance> importances;

	MessageFilter(final Map<Long, FindingTypeFilter> findingMap,
			final Map<Long, FindingTypeFilter> artifactMap) {
		filtered = new HashSet<Long>(artifactMap.size());
		importances = new HashMap<Long, Importance>(findingMap.size());
		for (final Entry<Long, FindingTypeFilter> entry : findingMap.entrySet()) {
			final FindingTypeFilter ftf = entry.getValue();
			final Importance i = ftf.getImportance();
			if (i != null) {
				importances.put(entry.getKey(), i);
			}
		}
		for (final Entry<Long, FindingTypeFilter> entry : artifactMap
				.entrySet()) {
			final FindingTypeFilter ftf = entry.getValue();
			if (ftf.isFiltered()) {
				filtered.add(entry.getKey());
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.surelogic.sierra.jdbc.tool.FindingTypeFilter#accept(java.lang.Long)
	 */
	public boolean accept(final Long artifactTypeId) {
		return !filtered.contains(artifactTypeId);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.surelogic.sierra.jdbc.tool.FindingTypeFilter#calculateImportance(
	 * java.lang.Long, com.surelogic.sierra.tool.message.Priority,
	 * com.surelogic.sierra.tool.message.Severity)
	 */
	public Importance calculateImportance(final Long findingTypeId,
			final Priority priority, final Severity severity) {
		switch (severity) {
		case ERROR:
			if (priority == Priority.HIGH) {
				return Importance.HIGH;
			} else {
				return Importance.MEDIUM;
			}
		case WARNING:
			if (priority == Priority.HIGH || priority == Priority.MEDIUM) {
				return Importance.MEDIUM;
			} else {
				return Importance.LOW;
			}
		default:
			return Importance.LOW;
		}
	}
}
