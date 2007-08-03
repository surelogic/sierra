package com.surelogic.sierra.jdbc.run;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

import com.surelogic.sierra.jdbc.Record;
import static com.surelogic.sierra.jdbc.JDBCUtils.*;

public class RunRecord implements Record<Long> {

	private Long id;
	private Long userId;
	private Long projectId;
	private String javaVersion;
	private String javaVendor;
	private Date timestamp;
	private RunStatus status;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
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

	public RunStatus getStatus() {
		return status;
	}

	public void setStatus(RunStatus status) {
		this.status = status;
	}

	public int fill(PreparedStatement st, int idx) throws SQLException {
		st.setLong(idx++, userId);
		st.setLong(idx++, projectId);
		setNullableString(idx++, st, javaVersion);
		setNullableString(idx++, st, javaVendor);
		st.setTimestamp(idx++, new Timestamp(timestamp.getTime()));
		st.setString(idx++, status.toString());
		return idx;
	}

}
