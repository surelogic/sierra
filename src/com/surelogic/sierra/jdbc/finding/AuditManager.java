package com.surelogic.sierra.jdbc.finding;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import com.surelogic.sierra.jdbc.user.User;
import com.surelogic.sierra.tool.message.Importance;
import com.surelogic.sierra.tool.message.AuditEvent;
import static com.surelogic.sierra.jdbc.JDBCUtils.*;

public class AuditManager {

	private static final String AUDIT_INSERT = "INSERT INTO AUDIT (USER_ID,TRAIL_ID,TIMESTAMP,VALUE,EVENT) VALUES (?,?,?,?,?)";

	private static final String FINDING_SELECT = "SELECT ID,TRAIL_ID,IMPORTANCE FROM FINDING WHERE ID = ?";

	private final Connection conn;

	private final PreparedStatement selectFinding;
	private final PreparedStatement insertAudit;

	private AuditManager(Connection conn) throws SQLException {
		this.conn = conn;
		this.selectFinding = conn.prepareStatement(FINDING_SELECT);
		this.insertAudit = conn.prepareStatement(AUDIT_INSERT);
	}

	public static AuditManager getInstance(Connection conn) throws SQLException {
		return new AuditManager(conn);
	}

	public void comment(Long findingId, String comment) throws SQLException {
		FindingView f = getFinding(findingId);
		if (f == null)
			throw new IllegalArgumentException(findingId
					+ " is not a valid finding id.");
		insert(insertAudit, newAudit(f.getTrailId(), comment,
				AuditEvent.COMMENT));
	}

	public void setImportance(Long findingId, Importance importance)
			throws SQLException {
		FindingView f = getFinding(findingId);
		if (f == null)
			throw new IllegalArgumentException(findingId
					+ " is not a valid finding id.");
		insert(insertAudit, newAudit(f.getTrailId(), importance.toString(), AuditEvent.IMPORTANCE));
	}

	private FindingView getFinding(Long findingId) throws SQLException {
		selectFinding.setLong(1, findingId);
		ResultSet set = selectFinding.executeQuery();
		if (set.next()) {
			FindingView f = new FindingView();
			f.read(set, 1);
			return f;
		} else {
			return null;
		}
	}

	private AuditRecord newAudit(Long trailId, String value,
			AuditEvent event) throws SQLException {
		AuditRecord record = new AuditRecord();
		record.setUserId(User.getUser(conn).getId());
		record.setTimestamp(new Date());
		record.setEvent(event);
		record.setValue(value);
		return record;
	}
}
