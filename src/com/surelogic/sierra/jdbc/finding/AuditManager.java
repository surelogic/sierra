package com.surelogic.sierra.jdbc.finding;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import com.surelogic.sierra.jdbc.record.AuditRecord;
import com.surelogic.sierra.jdbc.record.MatchRecord;
import com.surelogic.sierra.jdbc.user.User;
import com.surelogic.sierra.tool.message.Audit;
import com.surelogic.sierra.tool.message.AuditEvent;
import com.surelogic.sierra.tool.message.AuditTrailRequest;
import com.surelogic.sierra.tool.message.AuditTrailResponse;
import com.surelogic.sierra.tool.message.AuditTrailUpdate;
import com.surelogic.sierra.tool.message.Importance;
import com.surelogic.sierra.tool.message.Match;
import com.surelogic.sierra.tool.message.TigerServiceClient;
import com.surelogic.sierra.tool.message.TrailObsoletion;

public class AuditManager {

	private static final String AUDIT_INSERT = "INSERT INTO AUDIT (USER_ID,FINDING_ID,TIMESTAMP,VALUE,EVENT) VALUES (?,?,?,?,?)";

	private static final String FINDING_SELECT = "SELECT ID,FINDING_ID,IMPORTANCE FROM FINDING WHERE ID = ?";

	private static final String PROJECT_SELECT = "SELECT ID FROM PROJECT WHERE NAME = ?";

	private static final String TRAIL_SELECT = "SELECT ID FROM FINDING WHERE UID = ? AND PROJECT_ID = ?";
	private static final String UPDATE_OBSOLETE_TRAIL = "UPDATE LOCATION_MATCH SET FINDING_ID = (SELECT ID FROM FINDING WHERE UID = ?) WHERE FINDING_ID = (SELECT ID FROM FINDING WHERE UID = ?)";

	private final Connection conn;

	private final PreparedStatement selectFinding;
	private final PreparedStatement insertAudit;
	private final PreparedStatement selectProject;

	private final PreparedStatement selectTrail;
	private final PreparedStatement updateObsoleteTrail;

	private final Long userId;

	private final FindingRecordFactory fact;

	private AuditManager(Connection conn) throws SQLException {
		this.conn = conn;
		this.selectFinding = conn.prepareStatement(FINDING_SELECT);
		this.insertAudit = conn.prepareStatement(AUDIT_INSERT);
		this.selectProject = conn.prepareStatement(PROJECT_SELECT);
		this.selectTrail = conn.prepareStatement(TRAIL_SELECT);

		this.updateObsoleteTrail = conn.prepareStatement(UPDATE_OBSOLETE_TRAIL);
		this.fact = ClientFindingRecordFactory.getInstance(conn);
		userId = User.getUser(conn).getId();
	}

	public static AuditManager getInstance(Connection conn) throws SQLException {
		return new AuditManager(conn);
	}

	public void comment(Long findingId, String comment) throws SQLException {
		FindingView f = getFinding(findingId);
		if (f == null)
			throw new IllegalArgumentException(findingId
					+ " is not a valid finding id.");
		newAudit(f.getTrailId(), comment, AuditEvent.COMMENT).insert();
	}

	public void setImportance(Long findingId, Importance importance)
			throws SQLException {
		FindingView f = getFinding(findingId);
		if (f == null)
			throw new IllegalArgumentException(findingId
					+ " is not a valid finding id.");
		newAudit(f.getTrailId(), importance.toString(), AuditEvent.IMPORTANCE)
				.insert();
	}

	public void commit(String project, String qualifier) throws SQLException {

	}

	public void update(String project, String qualifier) throws SQLException {
		selectProject.setString(1, project);
		ResultSet set = selectProject.executeQuery();
		if (!set.next()) {
			throw new IllegalArgumentException(project
					+ " is not a valid project name");
		}
		Long projectId = set.getLong(1);
		AuditTrailRequest request = new AuditTrailRequest(project, qualifier,
				set.getLong(2));
		AuditTrailResponse response = new TigerServiceClient()
				.getTigerServicePort().getAuditTrails(request);
		for (TrailObsoletion obsoletion : response.getObsolete()) {
			updateObsoleteTrail.setString(1, obsoletion.getTrail());
			updateObsoleteTrail.setString(2, obsoletion.getObsoletedTrail());
			updateObsoleteTrail.executeUpdate();
		}
		for (AuditTrailUpdate update : response.getUpdate()) {
			String trail = update.getTrail();
			selectTrail.setString(1, trail);
			selectTrail.setLong(2, projectId);
			set = selectTrail.executeQuery();
			Long trailId = set.getLong(1);
			for (Match m : update.getMatch()) {
				MatchRecord mRec = fact.newMatch();

			}
			for (Audit a : update.getAudit()) {
				AuditRecord aRec = fact.newAudit();
				aRec.setEvent(a.getEvent());
				aRec.setTimestamp(a.getTimestamp());
				aRec.setFindingId(trailId);
				aRec.setUserId(userId);
				aRec.setValue(a.getValue());
			}
		}
		conn.commit();
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

	private AuditRecord newAudit(Long trailId, String value, AuditEvent event)
			throws SQLException {
		AuditRecord record = fact.newAudit();
		record.setUserId(User.getUser(conn).getId());
		record.setTimestamp(new Date());
		record.setEvent(event);
		record.setValue(value);
		return record;
	}

}
