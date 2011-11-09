package com.surelogic.sierra.jdbc.scan;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.surelogic.common.jdbc.JDBCUtils;
import com.surelogic.common.jdbc.QB;
import com.surelogic.common.jobs.NullSLProgressMonitor;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.jdbc.record.ScanRecord;
import com.surelogic.sierra.jdbc.tool.FindingFilter;
import com.surelogic.sierra.tool.message.ArtifactGenerator;
import com.surelogic.sierra.tool.message.ScanGenerator;

/**
 * Implements the artifact generation and makes the appropriate call to the
 * FindingManager api's when we create a partial scan on the client. The uid of
 * the scan document being written to the database will be ignored, and all
 * artifacts and metrics will instead be written to an existing scan, which will
 * be marked as partial and have its timestamp updated. If the partial scan
 * fails for some reason, we delete the entire scan.
 * 
 * @author nathan
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
	private final Map<String, String> extensions;
	private JDBCArtifactGenerator generator;

	JDBCPartialScanGenerator(final Connection conn,
			final ScanRecordFactory factory, final ScanManager manager,
			final ScanRecord scan, final FindingFilter filter) {
		this.conn = conn;
		this.factory = factory;
		this.manager = manager;
		this.scan = scan;
		this.filter = filter;
		extensions = new HashMap<String, String>();
	}

	public ArtifactGenerator build() {
		try {
			scan.setTimestamp(JDBCUtils.now());
			scan.setPartial(true);
			scan.setStatus(ScanStatus.LOADING);
			scan.update();

			// FIXME We should be writing in all extensions from the config
			final PreparedStatement insertSt = conn.prepareStatement(QB
					.get("Scans.insertExtension"));
			try {
				final PreparedStatement selectSt = conn.prepareStatement(QB
						.get("Scans.selectExtension"));
				try {
					for (final Entry<String, String> ext : extensions
							.entrySet()) {
						selectSt.setString(1, ext.getKey());
						selectSt.setString(2, ext.getValue());
						final ResultSet set = selectSt.executeQuery();
						if (set.next()) {
							final long extId = set.getLong(1);
							insertSt.setLong(1, scan.getId());
							insertSt.setLong(2, extId);
							insertSt.execute();
						}
					}
				} finally {
					selectSt.close();
				}
			} finally {
				insertSt.close();
			}
			conn.commit();
			generator = new JDBCArtifactGenerator(conn, factory, manager,
					projectName, scan, filter);
			return generator;
		} catch (final SQLException e) {
			try {
				conn.rollback();
				manager.deleteScan(scan.getUid(), null);
				conn.commit();
			} catch (final SQLException e1) {
				// Quietly do nothing, we already have an exception
			}
			throw new ScanPersistenceException(e);
		}
	}

	public ScanGenerator uid(final String uid) {
		return this;
	}

	public ScanGenerator javaVendor(final String vendor) {
		return this;
	}

	public ScanGenerator javaVersion(final String version) {
		return this;
	}

	public ScanGenerator project(final String projectName) {
		this.projectName = projectName;
		return this;
	}

	public ScanGenerator timeseries(final Collection<String> timeseries) {
		return this;
	}

	public ScanGenerator user(final String user) {
		return this;
	}

	public ScanGenerator extension(final String name, final String version) {
		extensions.put(name, version);
		return this;
	}

	public String finished() {
		generator.finished(new NullSLProgressMonitor());
		scan.setStatus(ScanStatus.FINISHED);
		try {
			scan.update();
			if (log.isLoggable(Level.FINE)) {
				log
						.fine("Scan "
								+ scan.getUid()
								+ " for project "
								+ projectName
								+ " persisted to database, starting finding generation.");
			}
			conn.commit();
			return scan.getUid();
		} catch (final SQLException e) {
			throw new ScanPersistenceException(e);
		}
	}

}
