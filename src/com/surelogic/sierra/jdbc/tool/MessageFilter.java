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

	private Set<Long> filtered;
	private Map<Long, Importance> importances;
	private Map<Long, Integer> deltas;

	MessageFilter(Map<Long, FindingTypeFilter> findingMap,
			Map<Long, FindingTypeFilter> artifactMap) {
		filtered = new HashSet<Long>(artifactMap.size());
		importances = new HashMap<Long, Importance>(findingMap.size());
		deltas = new HashMap<Long, Integer>(findingMap.size());
		for (Entry<Long, FindingTypeFilter> entry : findingMap.entrySet()) {
			FindingTypeFilter ftf = entry.getValue();
			Importance i;
			if ((i = ftf.getImportance()) != null) {
				importances.put(entry.getKey(), i);
			} else {
				deltas.put(entry.getKey(), ftf.getDelta());
			}
		}
		for (Entry<Long, FindingTypeFilter> entry : artifactMap.entrySet()) {
			FindingTypeFilter ftf = entry.getValue();
			if (ftf.isFiltered()) {
				filtered.add(entry.getKey());
			}
		}
	}

	/* (non-Javadoc)
	 * @see com.surelogic.sierra.jdbc.tool.FindingTypeFilter#accept(java.lang.Long)
	 */
	public boolean accept(Long artifactTypeId) {
		return !filtered.contains(artifactTypeId);
	}

	/* (non-Javadoc)
	 * @see com.surelogic.sierra.jdbc.tool.FindingTypeFilter#calculateImportance(java.lang.Long, com.surelogic.sierra.tool.message.Priority, com.surelogic.sierra.tool.message.Severity)
	 */
	public Importance calculateImportance(Long findingTypeId,
			Priority priority, Severity severity) {
		Importance i;
		if ((i = importances.get(findingTypeId)) == null) {
			Integer val = ((int) (((float) (severity.ordinal() + priority
					.ordinal())) / 2));
			Integer delta = deltas.get(findingTypeId);
			if (delta != null) {
				val += delta;
			}
			if (val > 3) {
				val = 3;
			} else if (val < 1) {
				val = 1;
			}
			i = Importance.values()[val];
		}
		return i;
	}
}
