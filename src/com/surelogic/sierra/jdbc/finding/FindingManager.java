package com.surelogic.sierra.jdbc.finding;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;

import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.jdbc.record.FindingRecord;
import com.surelogic.sierra.jdbc.record.LongRelationRecord;
import com.surelogic.sierra.jdbc.record.MatchRecord;
import com.surelogic.sierra.jdbc.record.RelationRecord;
import com.surelogic.sierra.jdbc.record.ScanRecord;
import com.surelogic.sierra.jdbc.scan.ScanRecordFactory;
import com.surelogic.sierra.jdbc.tool.MessageFilter;
import com.surelogic.sierra.tool.message.Priority;
import com.surelogic.sierra.tool.message.Severity;

public abstract class FindingManager {

	protected static final Logger log = SLLogger
			.getLoggerFor(FindingManager.class);

	private static final int CHUNK_SIZE = 1000;

	protected final Connection conn;

	FindingManager(Connection conn) throws SQLException {
		this.conn = conn;
	}

	protected abstract ResultSet getUnassignedArtifacts(ScanRecord scan)
			throws SQLException;

	protected abstract FindingRecordFactory getFactory();

	/**
	 * Generate findings for the scan with the given uid
	 * 
	 * @param uid
	 */
	public void generateFindings(String uid, MessageFilter filter) {
		try {

			FindingRecordFactory factory = getFactory();
			ScanRecord scan = ScanRecordFactory.getInstance(conn).newScan();
			scan.setUid(uid);
			if (!scan.select()) {
				throw new IllegalArgumentException("No scan with uid " + uid
						+ " exists in the database");
			}
			Long projectId = scan.getProjectId();

			ResultSet result = getUnassignedArtifacts(scan);

			int counter = 0;
			while (result.next()) {
				ArtifactResult art = new ArtifactResult();
				int idx = 1;
				art.id = result.getLong(idx++);
				art.p = Priority.values()[result.getInt(idx++)];
				art.s = Severity.values()[result.getInt(idx++)];
				art.m = factory.newMatch();
				// R.PROJECT_ID,S.HASH,CU.CLASS_NAME,CU.PACKAGE_NAME,A.FINDING_TYPE_ID
				MatchRecord.PK pk = new MatchRecord.PK();
				pk.setProjectId(result.getLong(idx++));
				pk.setHash(result.getLong(idx++));
				pk.setClassName(result.getString(idx++));
				pk.setPackageName(result.getString(idx++));
				pk.setFindingTypeId(result.getLong(idx++));
				art.m.setId(pk);
				Long findingId;
				if (!art.m.select()) {
					// We don't have a match, so we need to produce an entirely
					// new finding
					MatchRecord m = art.m;
					FindingRecord f = factory.newFinding();
					f.setProjectId(projectId);
					f.setImportance(filter.calculateImportance(art.m.getId()
							.getFindingTypeId(), art.p, art.s));
					f.insert();
					m.setFindingId(f.getId());
					m.insert();
					findingId = f.getId();
				} else {
					findingId = art.m.getFindingId();
				}
				LongRelationRecord afr = factory.newArtifactFinding();
				afr.setId(new RelationRecord.PK<Long, Long>(art.id, findingId));
				afr.insert();
				if (++counter == CHUNK_SIZE) {
					conn.commit();
					counter = 0;
				}
			}
			conn.commit();
			result.close();
			log.info("All new findings persisted.");
		} catch (SQLException e) {
			sqlError(e);
		}
	}

	private void sqlError(SQLException e) {
		throw new FindingGenerationException(e);
	}

	private static class ArtifactResult {
		Long id;
		Priority p;
		Severity s;
		MatchRecord m;
	}

	// TODO we only have one finding manager
	public static FindingManager getInstance(Connection conn)
			throws SQLException {
		return new ClientFindingManager(conn);
	}

}
