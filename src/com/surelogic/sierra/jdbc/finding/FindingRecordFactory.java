package com.surelogic.sierra.jdbc.finding;

import com.surelogic.sierra.jdbc.record.AuditRecord;
import com.surelogic.sierra.jdbc.record.FindingRecord;
import com.surelogic.sierra.jdbc.record.MatchRecord;
import com.surelogic.sierra.jdbc.record.TrailRecord;

public interface FindingRecordFactory {

	MatchRecord newMatch();
	
	AuditRecord newAudit();
	
	TrailRecord newTrail();
	
	FindingRecord newFinding();
	
}
