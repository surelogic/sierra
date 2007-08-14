package com.surelogic.sierra.tool.message;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlType
@XmlRootElement
public class CommitAuditResponse {

	Long revision;

	List<String> uid;

	public Long getRevision() {
		return revision;
	}

	public void setRevision(Long revision) {
		this.revision = revision;
	}

	public List<String> getUid() {
		return uid;
	}

	public void setUid(List<String> uid) {
		this.uid = uid;
	}

}
