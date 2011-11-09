package com.surelogic.sierra.jdbc.settings;

import java.util.*;

import com.surelogic.sierra.tool.message.ScanFilter;

public class ServerScanFilterInfo {
	protected final List<ScanFilter> scanFilters;
	protected final Collection<ScanFilter> updated;
	protected final List<String> deletions;

	ServerScanFilterInfo(List<ScanFilter> filters,
			             Collection<ScanFilter> updated,
			             List<String> dels) {
		scanFilters = filters;
		this.updated = updated;
		deletions = dels;
	}
	
	public List<ScanFilter> getScanFilters() {
		return scanFilters;
	}
	
	public boolean isChanged(ScanFilter f) {
		return updated.contains(f);
	}

	public int numChanged() {
		return updated.size();
	}
	
	public List<String> getDeletions() {
		return deletions;
	}
}

