package com.surelogic.sierra.jdbc.finding;

import java.util.List;

import com.surelogic.sierra.tool.message.Audit;

public class FindingAudits {
    private final long finding;
    private final List<Audit> audits;
    
    FindingAudits(long id, List<Audit> events) {
    	finding = id;
    	audits = events;
    }

    public long getFindingId() {
        return finding;
    }

    public List<Audit> getAudits() {
        return audits;
    }
}
