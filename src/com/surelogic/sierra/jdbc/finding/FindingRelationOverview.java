package com.surelogic.sierra.jdbc.finding;

import java.util.*;

import com.surelogic.common.jdbc.*;

public class FindingRelationOverview {
	private final List<FindingRelation> f_relations;
	
	private FindingRelationOverview(List<FindingRelation> relations) {
		f_relations = Collections.unmodifiableList(relations);
	}
	
	public static FindingRelationOverview getOverviewOrNull(Query q, Long findingId, boolean getChildren) {
		final String query = getChildren ? "FindingRelationOverview.listForParent" :
			                               "FindingRelationOverview.listForChild";
		
		final List<FindingRelation> relations = new ArrayList<FindingRelation>();
		for (final FindingRelation fr : q.prepared(query, new FindingRelation.Handler()).call(findingId)) {
			relations.add(fr);
		}
		return new FindingRelationOverview(relations);
	}	
	
	public Iterable<FindingRelation> getRelations() {
		return f_relations;
	}
	
	public Object[] toArray() {
		return f_relations.toArray();
	}
	
	public boolean isEmpty() {
		return f_relations.isEmpty();		
	}
}
