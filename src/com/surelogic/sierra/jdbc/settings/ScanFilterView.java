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

	ScanFilterView(final ScanFilterDO filter,
			final Map<String, FindingTypeDO> types,
			final Map<String, CategoryGraph> categories) {
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

	public String getName() {
		return filter.getName();
	}

	public String getUuid() {
		return filter.getUid();
	}

	public long getRevision() {
		return filter.getRevision();
	}

	/**
	 * Return the set of finding types that are allowed by this scan filter.
	 * 
	 * @return a set of finding type uuid's
	 */
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

	/**
	 * Returns a map of all finding types allowed by this scan filter, as well
	 * as their importances. The value will be <code>null</code> if no
	 * importance is specified explicitly.
	 * 
	 * @return
	 */
	public Map<String, Importance> getIncludedFindingTypesAndImportances() {
		final Map<String, Importance> map = new HashMap<String, Importance>(
				types.size());
		for (final CategoryFilterDO cat : filter.getCategories()) {
			for (final String ft : categories.get(cat.getUid())
					.getFindingTypes()) {
				map.put(ft, cat.getImportance());
			}
		}
		for (final TypeFilterDO type : filter.getFilterTypes()) {
			if (type.isFiltered()) {
				map.remove(type.getFindingType());
			} else {
				map.put(type.getFindingType(), type.getImportance());
			}
		}
		return map;
	}

	/**
	 * Whether or not an artifact type is allowed by this filter.
	 */
	public boolean accept(final Long artifactTypeId) {
		return allowed.contains(artifactTypeId);
	}

	/**
	 * Calculate the importance of a finding based on finding type as well as
	 * artifact priority and severity
	 */
	public Importance calculateImportance(final Long findingTypeId,
			final Priority priority, final Severity severity) {
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
