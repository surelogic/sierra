package com.surelogic.sierra.jdbc.run;

import static com.surelogic.sierra.jdbc.JDBCUtils.insert;

import java.io.File;
import java.io.FilenameFilter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.surelogic.sierra.db.Data;
import com.surelogic.sierra.jdbc.finding.FindingGenerator;
import com.surelogic.sierra.jdbc.user.User;
import com.surelogic.sierra.tool.SierraLogger;
import com.surelogic.sierra.tool.message.MessageWarehouse;

public class ClientRunWriter {

	private static final Logger log = SierraLogger
			.getLogger(ClientRunWriter.class.getName());

	private static final String TIGER_RESULTS = ".TigerResult";
	private static final String PARSED_FILE_SUFFIX = ".parsed";

	private static final String RUN_INSERT = "INSERT INTO RUN (USER_ID,PROJECT_ID,JAVA_VERSION,JAVA_VENDOR,RUN_DATE_TIME,STATUS) VALUES (?,?,?,?,?,?)";
	private static final String RUN_FINISH = "UPDATE RUN SET STATUS='FINISHED' WHERE ID = ?";
	private static final String PROJECT_SELECT = "SELECT ID FROM PROJECT WHERE NAME = ?";
	private static final String PROJECT_INSERT = "INSERT INTO PROJECT (NAME,REVISION) VALUES (?,0)";
	private final String name;
	private PreparedStatement insertRun;
	private PreparedStatement selectProject;
	private PreparedStatement insertProject;
	private PreparedStatement finishRun;

	public ClientRunWriter(String name) {
		this.name = name;
	}

	public void write() {
		Connection conn;
		try {
			conn = Data.getConnection();
			conn.setAutoCommit(false);
			insertRun = conn.prepareStatement(RUN_INSERT,
					Statement.RETURN_GENERATED_KEYS);
			selectProject = conn.prepareStatement(PROJECT_SELECT);
			finishRun = conn.prepareStatement(RUN_FINISH);
			RunRecord run = new RunRecord();
			selectProject.setString(1, name);
			ResultSet set = selectProject.executeQuery();
			if (!set.next()) {
				insertProject = conn.prepareStatement(PROJECT_INSERT,
						Statement.RETURN_GENERATED_KEYS);
				insertProject.setString(1, name);
				insertProject.executeUpdate();
				set = insertProject.getGeneratedKeys();
				set.next();
			}
			run.setProjectId(set.getLong(1));
			run.setTimestamp(new Date());
			run.setStatus(RunStatus.INPROGRESS);
			run.setUserId(User.getUser(conn).getId());
			insert(insertRun, run);
			conn.commit();
			JDBCArtifactGenerator generator = new JDBCArtifactGenerator(conn,
					run.getId());
			String resultsDir = System.getProperty("java.io.tmpdir")
					+ File.separator + TIGER_RESULTS + File.separator + name;
			log.log(Level.INFO, "Writing out results located at " + resultsDir);
			final MessageWarehouse mw = MessageWarehouse.getInstance();
			File resultRoot = new File(resultsDir);
			for (File f : resultRoot.listFiles(new FilenameFilter() {
				public boolean accept(File dir, String name) {
					return name.endsWith(PARSED_FILE_SUFFIX);
				}
			})) {
				log.log(Level.INFO, "Writing out file " + f);
				mw.parseRunDocument(f, generator);
			}
			generator.finish();
			log.log(Level.INFO, "All artifacts for run " + run.getId()
					+ " are persisted.");
			new FindingGenerator(conn).generate(run.getId());
			finishRun.setLong(1, run.getId());
			finishRun.executeUpdate();
			log.log(Level.INFO, "Findings for run " + run.getId()
					+ " have been generated.");
			conn.commit();
			conn.close();
		} catch (SQLException e) {
			throw new RunPersistenceException(e);
		}
	}

}
