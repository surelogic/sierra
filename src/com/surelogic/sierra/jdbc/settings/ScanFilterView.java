package com.surelogic.sierra.jdbc.settings;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.surelogic.sierra.jdbc.tool.ArtifactTypeDO;
import com.surelogic.sierra.jdbc.tool.FindingFilter;
import com.surelogic.sierra.jdbc.tool.FindingTypeDO;
import com.surelogic.sierra.tool.message.Importance;
import com.surelogic.sierra.tool.message.Priority;
import com.surelogic.sierra.tool.message.Severity;

/**
 * Provides a deep view of a scan filter.
 * 
 * @author nathan
 * 
 */
public class ScanFilterView implements FindingFilter {

	private final ScanFilterDO filter;
	private final Map<String, FindingTypeDO> types;
	private final Map<String, CategoryGraph> categories;
	private final Set<Long> allowed;
	private final Map<Long, Importance> importances;

	ScanFilterView(ScanFilterDO filter, Map<String, FindingTypeDO> types,
			Map<String, CategoryGraph> categories) {
		this.filter = filter;
		this.types = types;
		this.categories = categories;
		allowed = new HashSet<Long>(types.size() * 3);
		importances = new HashMap<Long, Importance>(types.size());
		for (final CategoryFilterDO catDo : filter.getCategories()) {
			final CategoryGraph graph = categories.get(catDo.getUid());
			for (final String uid : graph.getFindingTypes()) {
				final FindingTypeDO type = types.get(uid);
				for (final ArtifactTypeDO art : type.getArtifactTypes()) {
					allowed.add(art.getId());
				}
				final Importance imp = importances.get(type);
				final Importance catImp = catDo.getImportance();
				if ((imp == null)
						|| ((catImp != null) && (imp.ordinal() < catDo
								.getImportance().ordinal()))) {
					importances.put(type.getId(), catImp);
				}
			}
		}
		for (final TypeFilterDO typeDo : filter.getFilterTypes()) {
			final FindingTypeDO type = types.get(typeDo.getFindingType());
			if (typeDo.isFiltered()) {
				for (final ArtifactTypeDO art : type.getArtifactTypes()) {
					allowed.remove(art.getId());
				}
			} else {
				for (final ArtifactTypeDO art : type.getArtifactTypes()) {
					allowed.add(art.getId());
				}
			}
			final Importance imp = importances.get(type.getId());
			final Importance typeImp = typeDo.getImportance();
			if ((imp == null)
					|| ((typeImp != null) && (imp.ordinal() < typeImp.ordinal()))) {
				importances.put(type.getId(), typeImp);
			}
		}
	}

	public Set<String> getIncludedFindingTypes() {
		final Set<String> set = new HashSet<String>(types.size());
		for (final CategoryFilterDO cat : filter.getCategories()) {
			set.addAll(categories.get(cat.getUid()).getFindingTypes());
		}
		for (final TypeFilterDO type : filter.getFilterTypes()) {
			if (type.isFiltered()) {
				set.remove(type.getFindingType());
			} else {
				set.add(type.getFindingType());
			}
		}
		return set;
	}

	public boolean accept(Long artifactTypeId) {
		return allowed.contains(artifactTypeId);
	}

	public Importance calculateImportance(Long findingTypeId,
			Priority priority, Severity severity) {
		Importance i = importances.get(findingTypeId);
		if (i == null) {
			Integer val = ((int) (((float) (severity.ordinal() + priority
					.ordinal())) / 2));
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
