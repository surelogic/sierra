package com.surelogic.sierra.jdbc.scan;

import java.sql.Connection;
import java.sql.SQLException;

import com.surelogic.sierra.jdbc.record.ArtifactRecord;
import com.surelogic.sierra.jdbc.record.ArtifactSourceRecord;
import com.surelogic.sierra.jdbc.record.BaseMapper;
import com.surelogic.sierra.jdbc.record.ClassMetricRecord;
import com.surelogic.sierra.jdbc.record.CompilationUnitRecord;
import com.surelogic.sierra.jdbc.record.RecordMapper;
import com.surelogic.sierra.jdbc.record.ScanRecord;
import com.surelogic.sierra.jdbc.record.SourceRecord;
import com.surelogic.sierra.jdbc.record.TimeseriesRecord;
import com.surelogic.sierra.jdbc.record.TimeseriesScanRecord;
import com.surelogic.sierra.jdbc.record.UpdateBaseMapper;
import com.surelogic.sierra.jdbc.record.UpdateRecordMapper;

public final class ScanRecordFactory {

    private static final String COMPILATION_UNIT_INSERT = "INSERT INTO COMPILATION_UNIT (PACKAGE_NAME,CU) VALUES (?,?)";
    private static final String COMPILATION_UNIT_SELECT = "SELECT ID FROM COMPILATION_UNIT CU WHERE CU.PACKAGE_NAME = ? AND CU.CU = ?";
    private static final String SOURCE_LOCATION_INSERT = "INSERT INTO SOURCE_LOCATION (COMPILATION_UNIT_ID,CLASS_NAME,HASH,LINE_OF_CODE,END_LINE_OF_CODE,LOCATION_TYPE,IDENTIFIER) VALUES (?,?,?,?,?,?,?)";
    private static final String SOURCE_LOCATION_SELECT = "SELECT ID FROM SOURCE_LOCATION SL WHERE SL.COMPILATION_UNIT_ID = ? AND SL.CLASS_NAME = ? AND SL.HASH = ? AND SL.LINE_OF_CODE = ? AND SL.END_LINE_OF_CODE = ? AND SL.LOCATION_TYPE = ? AND SL.IDENTIFIER = ?";
    private static final String ARTIFACT_INSERT = "INSERT INTO ARTIFACT (SCAN_ID,ARTIFACT_TYPE_ID,PRIMARY_SOURCE_LOCATION_ID,PRIORITY,SEVERITY,MESSAGE,SCAN_NUMBER) VALUES (?,?,?,?,?,?,?)";
    private static final String ARTIFACT_SOURCE_RELATION_INSERT = "INSERT INTO ARTIFACT_SOURCE_LOCATION_RELTN (ARTIFACT_ID,SOURCE_LOCATION_ID) VALUES (?,?)";
    private static final String SCAN_INSERT = "INSERT INTO SCAN (USER_ID,PROJECT_ID,UUID,JAVA_VERSION,JAVA_VENDOR,SCAN_DATE_TIME,STATUS, IS_PARTIAL, EXTERNAL_FILTER) VALUES (?,?,?,?,?,?,?,?,?)";
    private static final String SCAN_SELECT = "SELECT ID, USER_ID, PROJECT_ID, JAVA_VERSION, JAVA_VENDOR, SCAN_DATE_TIME, STATUS, IS_PARTIAL, EXTERNAL_FILTER FROM SCAN WHERE UUID = ?";
    private static final String SCAN_DELETE = "DELETE FROM SCAN WHERE ID = ?";
    private static final String SCAN_UPDATE = "UPDATE SCAN SET SCAN_DATE_TIME = ?, STATUS = ?, IS_PARTIAL = ?, EXTERNAL_FILTER = ? WHERE ID = ?";
    private static final String TIMESERIES_SELECT = "SELECT ID FROM QUALIFIER WHERE NAME = ?";
    private static final String SCAN_TIMESERIES_INSERT = "INSERT INTO QUALIFIER_SCAN_RELTN (QUALIFIER_ID,SCAN_ID) VALUES(?,?)";
    private static final String CLASS_METRIC_INSERT = "INSERT INTO METRIC_CU (SCAN_ID, COMPILATION_UNIT_ID, LINES_OF_CODE) VALUES (?,?,?)";
    private final Connection conn;

    private final RecordMapper compUnitMapper;
    private final RecordMapper sourceMapper;
    private final RecordMapper artMapper;
    private final RecordMapper artSourceMapper;
    private final UpdateRecordMapper scanMapper;
    private UpdateRecordMapper timeseriesMapper;
    private RecordMapper scanQualMapper;
    private final RecordMapper classMetricMapper;

    private ScanRecordFactory(Connection conn) throws SQLException {
        this.conn = conn;
        compUnitMapper = new BaseMapper(conn, COMPILATION_UNIT_INSERT,
                COMPILATION_UNIT_SELECT, null);
        sourceMapper = new BaseMapper(conn, SOURCE_LOCATION_INSERT,
                SOURCE_LOCATION_SELECT, null);
        artMapper = new BaseMapper(conn, ARTIFACT_INSERT, null, null);
        artSourceMapper = new BaseMapper(conn, ARTIFACT_SOURCE_RELATION_INSERT,
                null, null, false);
        scanMapper = new UpdateBaseMapper(conn, SCAN_INSERT, SCAN_SELECT,
                SCAN_DELETE, SCAN_UPDATE);
        classMetricMapper = new BaseMapper(conn, CLASS_METRIC_INSERT, null,
                null, false);
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

    public ScanRecord newScan() {
        return new ScanRecord(scanMapper);
    }

    public ClassMetricRecord newClassMetric() {
        return new ClassMetricRecord(classMetricMapper);
    }

    public TimeseriesRecord newTimeseries() throws SQLException {
        if (timeseriesMapper == null) {
            timeseriesMapper = new UpdateBaseMapper(conn, null,
                    TIMESERIES_SELECT, null, null);
        }
        return new TimeseriesRecord(timeseriesMapper);
    }

    public TimeseriesScanRecord newScanTimeseriesRelation() throws SQLException {
        if (scanQualMapper == null) {
            scanQualMapper = new BaseMapper(conn, SCAN_TIMESERIES_INSERT, null,
                    null, false);
        }
        return new TimeseriesScanRecord(scanQualMapper);
    }
}
