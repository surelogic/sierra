package com.surelogic.sierra.jdbc.finding;

import com.surelogic.sierra.jdbc.record.AuditRecord;
import com.surelogic.sierra.jdbc.record.FindingRecord;
import com.surelogic.sierra.jdbc.record.LongRelationRecord;
import com.surelogic.sierra.jdbc.record.MatchRecord;

public interface FindingRecordFactory {

	MatchRecord newMatch();
	
	AuditRecord newAudit();
	
	FindingRecord newFinding();
	
	LongRelationRecord newArtifactFinding();
}
