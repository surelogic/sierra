package com.surelogic.sierra.jdbc.run;

import java.sql.Connection;
import java.sql.SQLException;

import com.surelogic.sierra.jdbc.record.ArtifactRecord;
import com.surelogic.sierra.jdbc.record.ArtifactSourceRecord;
import com.surelogic.sierra.jdbc.record.BaseMapper;
import com.surelogic.sierra.jdbc.record.CompilationUnitRecord;
import com.surelogic.sierra.jdbc.record.ProjectRecord;
import com.surelogic.sierra.jdbc.record.QualifierRecord;
import com.surelogic.sierra.jdbc.record.RecordMapper;
import com.surelogic.sierra.jdbc.record.QualifierRunRecord;
import com.surelogic.sierra.jdbc.record.RunRecord;
import com.surelogic.sierra.jdbc.record.SourceRecord;
import com.surelogic.sierra.jdbc.record.UpdateBaseMapper;
import com.surelogic.sierra.jdbc.record.UpdateRecordMapper;

public class RunRecordFactory {

	private static final String COMPILATION_UNIT_INSERT = "INSERT INTO SIERRA.COMPILATION_UNIT (PATH,CLASS_NAME,PACKAGE_NAME) VALUES (?,?,?)";
	private static final String COMPILATION_UNIT_SELECT = "SELECT ID FROM SIERRA.COMPILATION_UNIT CU WHERE CU.PATH = ? AND CU.CLASS_NAME = ? AND CU.PACKAGE_NAME = ?";
	private static final String SOURCE_LOCATION_INSERT = "INSERT INTO SIERRA.SOURCE_LOCATION (COMPILATION_UNIT_ID,HASH,LINE_OF_CODE,END_LINE_OF_CODE,LOCATION_TYPE,IDENTIFIER) VALUES (?,?,?,?,?,?)";
	private static final String SOURCE_LOCATION_SELECT = "SELECT ID FROM SIERRA.SOURCE_LOCATION SL WHERE SL.COMPILATION_UNIT_ID = ? AND SL.HASH = ? AND SL.LINE_OF_CODE = ? AND SL.END_LINE_OF_CODE = ? AND SL.LOCATION_TYPE = ? AND SL.IDENTIFIER = ?";
	private static final String ARTIFACT_INSERT = "INSERT INTO SIERRA.ARTIFACT (RUN_ID,FINDING_TYPE_ID,PRIMARY_SOURCE_LOCATION_ID,PRIORITY,SEVERITY,MESSAGE) VALUES (?,?,?,?,?,?)";
	private static final String ARTIFACT_SOURCE_RELATION_INSERT = "INSERT INTO SIERRA.ARTIFACT_SOURCE_LOCATION_RELTN (ARTIFACT_ID,SOURCE_LOCATION_ID) VALUES (?,?)";
	private static final String PROJECT_SELECT = "SELECT ID FROM PROJECT WHERE NAME = ?";
	private static final String PROJECT_INSERT = "INSERT INTO PROJECT (NAME,REVISION) VALUES (?,0)";
	private static final String RUN_INSERT = "INSERT INTO RUN (USER_ID,PROJECT_ID,UID,JAVA_VERSION,JAVA_VENDOR,RUN_DATE_TIME,STATUS) VALUES (?,?,?,?,?,?,?)";
	private static final String RUN_SELECT = "SELECT ID, USER_ID, PROJECT_ID, JAVA_VERSION, JAVA_VENDOR, RUN_DATE_TIME, STATUS FROM RUN WHERE UID = ?";
	private static final String RUN_DELETE = "DELETE FROM RUN WHERE ID = ?";
	private static final String RUN_UPDATE = "UPDATE RUN SET STATUS = ? WHERE ID = ?";
	private static final String QUALIFIER_SELECT = "SELECT ID FROM QUALIFIER WHERE NAME = ?";
	private static final String RUN_QUALIFIER_INSERT = "INSERT INTO QUALIFIER_RUN_RELTN (QUALIFIER_ID,RUN_ID) VALUES(?,?)";

	private final Connection conn;

	private final RecordMapper compUnitMapper;
	private final RecordMapper sourceMapper;
	private final RecordMapper artMapper;
	private final RecordMapper artSourceMapper;
	private final RecordMapper projectMapper;
	private final UpdateRecordMapper runMapper;
	private RecordMapper qualifierMapper;
	private RecordMapper runQualMapper;

	private RunRecordFactory(Connection conn) throws SQLException {
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
		runMapper = new UpdateBaseMapper(conn, RUN_INSERT, RUN_SELECT,
				RUN_DELETE, RUN_UPDATE);
	}

	public static RunRecordFactory getInstance(Connection conn)
			throws SQLException {
		return new RunRecordFactory(conn);
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

	public RunRecord newRun() {
		return new RunRecord(runMapper);
	}

	public QualifierRecord newQualifier() throws SQLException {
		if (qualifierMapper == null) {
			qualifierMapper = new BaseMapper(conn, null, QUALIFIER_SELECT, null);
		}
		return new QualifierRecord(qualifierMapper);
	}

	public QualifierRunRecord newRunQualiferRelation() throws SQLException {
		if (runQualMapper == null) {
			runQualMapper = new BaseMapper(conn, RUN_QUALIFIER_INSERT, null,
					null);
		}
		return new QualifierRunRecord(runQualMapper);
	}
}
