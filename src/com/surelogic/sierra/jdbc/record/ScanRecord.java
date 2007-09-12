package com.surelogic.sierra.jdbc.record;

import static com.surelogic.sierra.jdbc.JDBCUtils.*;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

import com.surelogic.sierra.jdbc.scan.ScanStatus;

public final class ScanRecord extends LongUpdatableRecord {

	private Long userId;
	private Long projectId;
	private String uid;
	private String javaVersion;
	private String javaVendor;
	private Date timestamp;
	private ScanStatus status;

	public ScanRecord(UpdateRecordMapper mapper) {
		super(mapper);
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public Long getProjectId() {
		return projectId;
	}

	public void setProjectId(Long projectId) {
		this.projectId = projectId;
	}

	public String getJavaVersion() {
		return javaVersion;
	}

	public void setJavaVersion(String javaVersion) {
		this.javaVersion = javaVersion;
	}

	public String getJavaVendor() {
		return javaVendor;
	}

	public void setJavaVendor(String javaVendor) {
		this.javaVendor = javaVendor;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	public ScanStatus getStatus() {
		return status;
	}

	public void setStatus(ScanStatus status) {
		this.status = status;
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	@Override
	protected int fill(PreparedStatement st, int idx) throws SQLException {
		setNullableLong(idx++, st, userId);
		st.setLong(idx++, projectId);
		st.setString(idx++, uid);
		setNullableString(idx++, st, javaVersion);
		setNullableString(idx++, st, javaVendor);
		st.setTimestamp(idx++, new Timestamp(timestamp.getTime()));
		st.setString(idx++, status.toString());
		return idx;
	}

	@Override
	protected int fillWithNk(PreparedStatement st, int idx) throws SQLException {
		st.setString(idx++, uid);
		return idx;
	}

	@Override
	protected int readAttributes(ResultSet set, int idx) throws SQLException {
		this.userId = set.getLong(idx++);
		this.projectId = set.getLong(idx++);
		this.javaVendor = set.getString(idx++);
		this.javaVendor = set.getString(idx++);
		this.timestamp = set.getTimestamp(idx++);
		this.status = ScanStatus.valueOf(set.getString(idx++));
		return idx;
	}

	@Override
	protected int fillUpdatedFields(PreparedStatement st, int idx)
			throws SQLException {
		st.setString(idx++, status.toString());
		return idx;
	}

}
