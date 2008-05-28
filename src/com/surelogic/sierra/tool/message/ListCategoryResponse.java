package com.surelogic.sierra.tool.message;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class ListCategoryResponse {

	protected List<ServerRevision> servers;

	protected List<FilterSet> filterSets;

	protected List<String> deletions;

	public List<ServerRevision> getServerRevisions() {
		if (servers == null) {
			servers = new ArrayList<ServerRevision>();
		}
		return servers;
	}

	public List<FilterSet> getFilterSets() {
		if (filterSets == null) {
			filterSets = new ArrayList<FilterSet>();
		}
		return filterSets;
	}

	public List<String> getDeletions() {
		if (deletions == null) {
			deletions = new ArrayList<String>();
		}
		return deletions;
	}

}
