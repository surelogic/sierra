package com.surelogic.sierra.jdbc.scan;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.surelogic.common.jdbc.ConnectionQuery;
import com.surelogic.common.jdbc.JDBCUtils;
import com.surelogic.common.jdbc.QB;
import com.surelogic.common.jobs.NullSLProgressMonitor;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.jdbc.project.ProjectRecordFactory;
import com.surelogic.sierra.jdbc.record.ProjectRecord;
import com.surelogic.sierra.jdbc.record.RecordRelationRecord;
import com.surelogic.sierra.jdbc.record.ScanRecord;
import com.surelogic.sierra.jdbc.record.TimeseriesRecord;
import com.surelogic.sierra.jdbc.record.TimeseriesScanRecord;
import com.surelogic.sierra.jdbc.tool.ExtensionDO;
import com.surelogic.sierra.jdbc.tool.FindingFilter;
import com.surelogic.sierra.jdbc.tool.FindingTypes;
import com.surelogic.sierra.jdbc.user.ClientUser;
import com.surelogic.sierra.tool.message.ArtifactGenerator;
import com.surelogic.sierra.tool.message.ScanGenerator;

class JDBCScanGenerator implements ScanGenerator {

	private static final Logger log = SLLogger
			.getLoggerFor(JDBCScanGenerator.class);

	private final Connection conn;
	private final FindingFilter filter;
	private final ScanRecordFactory factory;
	private final ScanManager manager;
	private final boolean partial;
	private JDBCArtifactGenerator generator;
	private String projectName;
	private String javaVendor;
	private String javaVersion;
	private String uid;
	private String user;
	private final Set<String> timeseries;
	private final Map<String, String> extensions;
	private ScanRecord scan;

	JDBCScanGenerator(final Connection conn, final ScanRecordFactory factory,
			final ScanManager manager, final FindingFilter filter) {
		this.conn = conn;
		this.factory = factory;
		this.manager = manager;
		timeseries = new TreeSet<String>();
		extensions = new HashMap<String, String>();
		partial = false;
		this.filter = filter;
	}

	JDBCScanGenerator(final Connection conn, final ScanRecordFactory factory,
			final ScanManager manager, final FindingFilter filter,
			final boolean partial) {
		this.conn = conn;
		this.factory = factory;
		this.manager = manager;
		timeseries = new TreeSet<String>();
		extensions = new HashMap<String, String>();
		this.partial = partial;
		this.filter = filter;
	}

	public ArtifactGenerator build() {
		ProjectRecord p;
		try {
			p = ProjectRecordFactory.getInstance(conn).newProject();
		} catch (final SQLException e) {
			throw new ScanPersistenceException(e);
		}
		p.setName(projectName);
		try {
			if (!p.select()) {
				p.insert();
			}
			scan = factory.newScan();
			scan.setProjectId(p.getId());
			scan.setUid(uid);
			scan.setTimestamp(JDBCUtils.now());
			scan.setJavaVersion(javaVersion);
			scan.setJavaVendor(javaVendor);
			scan.setStatus(ScanStatus.LOADING);
			scan.setPartial(partial);
			if (user != null) {
				scan.setUserId(ClientUser.getUser(user, conn).getId());
			}
			if (scan.select()) {
				throw new IllegalArgumentException("Scan with uid " + uid
						+ " already exists.");
			}
			scan.insert();
			for (final String name : timeseries) {
				final TimeseriesRecord q = factory.newTimeseries();
				q.setName(name);
				if (q.select()) {
					final TimeseriesScanRecord rq = factory
							.newScanTimeseriesRelation();
					rq
							.setId(new RecordRelationRecord.PK<TimeseriesRecord, ScanRecord>(
									q, scan));
					rq.insert();
				} else {
					scan.delete();
					throw new IllegalArgumentException(
							"Invalid timeseries name: " + name);
				}
			}
			// TODO When the extension code is fully in place, we will have the
			// set of extensions set externally
			for (final ExtensionDO ext : new FindingTypes(new ConnectionQuery(
					conn)).getExtensions()) {
				extensions.put(ext.getName(), ext.getVersion());
			}
			final PreparedStatement st = conn.prepareStatement(QB
					.get("Scans.insertExtension"));
			try {
				for (final Entry<String, String> ext : extensions.entrySet()) {
					st.setLong(1, scan.getId());
					st.setString(2, ext.getKey());
					st.setString(3, ext.getValue());
					st.execute();
				}
			} finally {
				st.close();
			}
			conn.commit();
			generator = new JDBCArtifactGenerator(conn, factory, manager,
					projectName, scan, filter);
			return generator;
		} catch (final SQLException e) {
			try {
				conn.rollback();
				manager.deleteScan(uid, null);
				conn.commit();
			} catch (final SQLException e1) {
				// Quietly do nothing, we already have an exception
			}
			throw new ScanPersistenceException(e);
		}
	}

	public ScanGenerator uid(final String uid) {
		this.uid = uid;
		return this;
	}

	public ScanGenerator javaVendor(final String vendor) {
		javaVendor = vendor;
		return this;
	}

	public ScanGenerator javaVersion(final String version) {
		javaVersion = version;
		return this;
	}

	public ScanGenerator project(final String projectName) {
		this.projectName = projectName;
		return this;
	}

	public ScanGenerator timeseries(final Collection<String> timeseries) {
		if (timeseries != null && !timeseries.isEmpty()) {
			this.timeseries.addAll(timeseries);
		}
		return this;
	}

	public ScanGenerator user(final String user) {
		this.user = user;
		return this;
	}

	public String finished() {
		generator.finished(new NullSLProgressMonitor());
		scan.setStatus(ScanStatus.GENERATED);
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
