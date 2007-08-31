package com.surelogic.sierra.jdbc.scan;

import java.sql.Connection;
import java.sql.SQLException;

import com.surelogic.sierra.jdbc.record.ArtifactRecord;
import com.surelogic.sierra.jdbc.record.ArtifactSourceRecord;
import com.surelogic.sierra.jdbc.record.BaseMapper;
import com.surelogic.sierra.jdbc.record.ClassMetricRecord;
import com.surelogic.sierra.jdbc.record.CompilationUnitRecord;
import com.surelogic.sierra.jdbc.record.ProjectRecord;
import com.surelogic.sierra.jdbc.record.QualifierRecord;
import com.surelogic.sierra.jdbc.record.QualifierScanRecord;
import com.surelogic.sierra.jdbc.record.RecordMapper;
import com.surelogic.sierra.jdbc.record.ScanRecord;
import com.surelogic.sierra.jdbc.record.SourceRecord;
import com.surelogic.sierra.jdbc.record.UpdateBaseMapper;
import com.surelogic.sierra.jdbc.record.UpdateRecordMapper;

public class ScanRecordFactory {

	private static final String COMPILATION_UNIT_INSERT = "INSERT INTO SIERRA.COMPILATION_UNIT (PATH,CLASS_NAME,PACKAGE_NAME) VALUES (?,?,?)";
	private static final String COMPILATION_UNIT_SELECT = "SELECT ID FROM SIERRA.COMPILATION_UNIT CU WHERE CU.PATH = ? AND CU.CLASS_NAME = ? AND CU.PACKAGE_NAME = ?";
	private static final String SOURCE_LOCATION_INSERT = "INSERT INTO SIERRA.SOURCE_LOCATION (COMPILATION_UNIT_ID,HASH,LINE_OF_CODE,END_LINE_OF_CODE,LOCATION_TYPE,IDENTIFIER) VALUES (?,?,?,?,?,?)";
	private static final String SOURCE_LOCATION_SELECT = "SELECT ID FROM SIERRA.SOURCE_LOCATION SL WHERE SL.COMPILATION_UNIT_ID = ? AND SL.HASH = ? AND SL.LINE_OF_CODE = ? AND SL.END_LINE_OF_CODE = ? AND SL.LOCATION_TYPE = ? AND SL.IDENTIFIER = ?";
	private static final String ARTIFACT_INSERT = "INSERT INTO SIERRA.ARTIFACT (SCAN_ID,FINDING_TYPE_ID,PRIMARY_SOURCE_LOCATION_ID,PRIORITY,SEVERITY,MESSAGE) VALUES (?,?,?,?,?,?)";
	private static final String ARTIFACT_SOURCE_RELATION_INSERT = "INSERT INTO SIERRA.ARTIFACT_SOURCE_LOCATION_RELTN (ARTIFACT_ID,SOURCE_LOCATION_ID) VALUES (?,?)";
	private static final String PROJECT_SELECT = "SELECT ID FROM PROJECT WHERE NAME = ?";
	private static final String PROJECT_INSERT = "INSERT INTO PROJECT (NAME,SETTINGS_REVISION) VALUES (?,0)";
	private static final String SCAN_INSERT = "INSERT INTO SCAN (USER_ID,PROJECT_ID,UID,JAVA_VERSION,JAVA_VENDOR,SCAN_DATE_TIME,STATUS) VALUES (?,?,?,?,?,?,?)";
	private static final String SCAN_SELECT = "SELECT ID, USER_ID, PROJECT_ID, JAVA_VERSION, JAVA_VENDOR, SCAN_DATE_TIME, STATUS FROM SCAN WHERE UID = ?";
	private static final String SCAN_DELETE = "DELETE FROM SCAN WHERE ID = ?";
	private static final String SCAN_UPDATE = "UPDATE SCAN SET STATUS = ? WHERE ID = ?";
	private static final String QUALIFIER_SELECT = "SELECT ID FROM QUALIFIER WHERE NAME = ?";
	private static final String SCAN_QUALIFIER_INSERT = "INSERT INTO QUALIFIER_SCAN_RELTN (QUALIFIER_ID,SCAN_ID) VALUES(?,?)";
	private static final String CLASS_METRIC_INSERT = "INSERT INTO CLASS_METRIC (SCAN_ID, COMPILATION_UNIT_ID, LINES_OF_CODE) VALUES (?,?,?)";
	private final Connection conn;

	private final RecordMapper compUnitMapper;
	private final RecordMapper sourceMapper;
	private final RecordMapper artMapper;
	private final RecordMapper artSourceMapper;
	private final RecordMapper projectMapper;
	private final UpdateRecordMapper scanMapper;
	private UpdateRecordMapper qualifierMapper;
	private RecordMapper scanQualMapper;
	private RecordMapper classMetricMapper;

	private ScanRecordFactory(Connection conn) throws SQLException {
		this.conn = conn;
		compUnitMapper = new BaseMapper(conn, COMPILATION_UNIT_INSERT,
				COMPILATION_UNIT_SELECT, null);
		sourceMapper = new BaseMapper(conn, SOURCE_LOCATION_INSERT,
				SOURCE_LOCATION_SELECT, null);
		artMapper = new BaseMapper(conn, ARTIFACT_INSERT, null, null);
		artSourceMapper = new BaseMapper(conn, ARTIFACT_SOURCE_RELATION_INSERT,
				null, null);
		projectMapper = new BaseMapper(conn, PROJECT_INSERT, PROJECT_SELECT,
				null);
		scanMapper = new UpdateBaseMapper(conn, SCAN_INSERT, SCAN_SELECT,
				SCAN_DELETE, SCAN_UPDATE);
		this.classMetricMapper = new BaseMapper(conn, CLASS_METRIC_INSERT,
				null, null);
	}

	public static ScanRecordFactory getInstance(Connection conn)
			throws SQLException {
		return new ScanRecordFactory(conn);
	}

	public CompilationUnitRecord newCompilationUnit() {
		return new CompilationUnitRecord(compUnitMapper);
	}

	public SourceRecord newSource() {
		return new SourceRecord(sourceMapper);
	}

	public ArtifactRecord newArtifact() {
		return new ArtifactRecord(artMapper);
	}

	public ArtifactSourceRecord newArtifactSourceRelation() {
		return new ArtifactSourceRecord(artSourceMapper);
	}

	public ProjectRecord newProject() {
		return new ProjectRecord(projectMapper);
	}

	public ScanRecord newScan() {
		return new ScanRecord(scanMapper);
	}

	public ClassMetricRecord newClassMetric() {
		return new ClassMetricRecord(classMetricMapper);
	}

	public QualifierRecord newQualifier() throws SQLException {
		if (qualifierMapper == null) {
			qualifierMapper = new UpdateBaseMapper(conn, null,
					QUALIFIER_SELECT, null, null);
		}
		return new QualifierRecord(qualifierMapper);
	}

	public QualifierScanRecord newScanQualifierRelation() throws SQLException {
		if (scanQualMapper == null) {
			scanQualMapper = new BaseMapper(conn, SCAN_QUALIFIER_INSERT, null,
					null);
		}
		return new QualifierScanRecord(scanQualMapper);
	}
}
