package com.surelogic.sierra.jdbc.finding;

import java.util.*;

import com.surelogic.common.jdbc.*;

public class FindingRelationOverview {
	private static final FindingRelation[] emptyRelations = new FindingRelation[0];
	
	private final Long f_findingId;
	private final List<FindingRelation> f_relations;
	
	private FindingRelationOverview(Long id, List<FindingRelation> relations) {
		f_findingId = id;
		f_relations = relations;
	}
	
	public static FindingRelationOverview getOverviewOrNull(Query q, Long findingId, boolean getChildren) {
		final String query = getChildren ? "FindingRelationOverview.listForParent" :
			                               "FindingRelationOverview.listForChild";
		
		final List<FindingRelation> relations = new ArrayList<FindingRelation>();
		for (final FindingRelation fr : q.prepared(query, new FindingRelation.Handler()).call(findingId)) {
			relations.add(fr);
		}
		return new FindingRelationOverview(findingId, relations);
	}	
	
	public Long getId() {
		return f_findingId;
	}
	
	public Iterable<FindingRelation> getRelations() {
		return f_relations;
	}
	
	public FindingRelation[] toArray() {
		return f_relations.toArray(emptyRelations);
	}
	
	public void sort(Comparator<? super FindingRelation> c) {
		Collections.sort(f_relations, c);
	}
	
	public boolean isEmpty() {
		return f_relations.isEmpty();		
	}
	
	public void filter(Filter f) {
		Iterator<FindingRelation> it = f_relations.iterator();
		while (it.hasNext()) {
			FindingRelation r = it.next();
			if (f.filterOut(r)) {
				it.remove();
			}
		}
	}
	
	public interface Filter {
		boolean filterOut(FindingRelation r);
	}
}
