package com.surelogic.sierra.gwt.client.data;

import java.io.Serializable;

import com.surelogic.sierra.gwt.client.data.cache.Cacheable;
import com.surelogic.sierra.gwt.client.util.LangUtil;

public class ScanFilterEntry implements Serializable, Cacheable,
		Comparable<ScanFilterEntry> {
	private static final long serialVersionUID = 6408580757889309477L;

	private String uuid;
	private String name;
	private String shortMessage;
	private ImportanceView importance;
	private boolean category;

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getShortMessage() {
		return shortMessage;
	}

	public void setShortMessage(String shortMessage) {
		this.shortMessage = shortMessage;
	}

	public boolean isCategory() {
		return category;
	}

	public void setCategory(boolean category) {
		this.category = category;
	}

	public ImportanceView getImportance() {
		return importance;
	}

	public void setImportance(ImportanceView importance) {
		this.importance = importance;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((uuid == null) ? 0 : uuid.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj != null && obj instanceof ScanFilterEntry) {
			return LangUtil.equals(uuid, ((ScanFilterEntry) obj).uuid);
		}
		return false;
	}

	public ScanFilterEntry copy() {
		final ScanFilterEntry e = new ScanFilterEntry();
		e.category = category;
		e.importance = importance;
		e.name = name;
		e.shortMessage = shortMessage;
		e.uuid = uuid;
		return e;
	}

	public int compareTo(ScanFilterEntry that) {
		if (this == that) {
			return 0;
		}
		if (that == null) {
			return 1;
		}
		return name.compareTo(that.name);
	}

}
