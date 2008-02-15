package com.surelogic.sierra.jdbc.scan;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.jdbc.EmptyProgressMonitor;
import com.surelogic.sierra.jdbc.JDBCUtils;
import com.surelogic.sierra.jdbc.record.ScanRecord;
import com.surelogic.sierra.jdbc.tool.FindingFilter;
import com.surelogic.sierra.tool.message.ArtifactGenerator;
import com.surelogic.sierra.tool.message.ScanGenerator;

/**
 * Implements the artifact generation and makes the approprate call to the
 * FindingManager api's when we create a partial scan on the client. The uid of
 * the scan document being written to the database will be ignored, and all
 * artifacts and metrics will instead be written to an existing scan, which will
 * be marked as partial and have its timestamp updated. If the partial scan
 * fails for some reason, we delete the entire scan.
 * 
 * @author nathan
 * 
 */
class JDBCPartialScanGenerator implements ScanGenerator {

	private static final Logger log = SLLogger
			.getLoggerFor(JDBCPartialScanGenerator.class);

	private final Connection conn;
	private final ScanRecordFactory factory;
	private final ScanManager manager;
	private final ScanRecord scan;
	private final FindingFilter filter;
	private String projectName;
	private JDBCArtifactGenerator generator;

	JDBCPartialScanGenerator(Connection conn, ScanRecordFactory factory,
			ScanManager manager, ScanRecord scan, FindingFilter filter) {
		this.conn = conn;
		this.factory = factory;
		this.manager = manager;
		this.scan = scan;
		this.filter = filter;
	}

	public ArtifactGenerator build() {
		try {
			scan.setTimestamp(JDBCUtils.now());
			scan.setPartial(true);
			scan.setStatus(ScanStatus.LOADING);
			scan.update();
			conn.commit();
			generator = new JDBCArtifactGenerator(conn, factory, manager,
					projectName, scan, filter);
			return generator;
		} catch (SQLException e) {
			try {
				conn.rollback();
				manager.deleteScan(scan.getUid(), null);
				conn.commit();
			} catch (SQLException e1) {
				// Quietly do nothing, we already have an exception
			}
			throw new ScanPersistenceException(e);
		}
	}

	public ScanGenerator uid(String uid) {
		return this;
	}

	public ScanGenerator javaVendor(String vendor) {
		return this;
	}

	public ScanGenerator javaVersion(String version) {
		return this;
	}

	public ScanGenerator project(String projectName) {
		this.projectName = projectName;
		return this;
	}

	public ScanGenerator qualifiers(Collection<String> qualifiers) {
		return this;
	}

	public ScanGenerator user(String user) {
		return this;
	}

	public String finished() {
		generator.finished(EmptyProgressMonitor.instance());
		scan.setStatus(ScanStatus.FINISHED);
		try {
			scan.update();
			if (log.isLoggable(Level.FINE)) {
			  log.fine("Scan " + scan.getUid() + " for project " + projectName
			      + " persisted to database, starting finding generation.");
			}
			conn.commit();
			return scan.getUid();
		} catch (SQLException e) {
			throw new ScanPersistenceException(e);
		}
	}

}
