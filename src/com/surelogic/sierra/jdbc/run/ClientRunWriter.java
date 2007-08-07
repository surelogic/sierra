package com.surelogic.sierra.jdbc.run;

import java.io.File;
import java.io.FilenameFilter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.surelogic.sierra.tool.SierraLogger;
import com.surelogic.sierra.tool.analyzer.RunGenerator;
import com.surelogic.sierra.tool.message.MessageWarehouse;

public class ClientRunWriter {

	private static final Logger log = SierraLogger
			.getLogger(ClientRunWriter.class.getName());

	private static final String TIGER_RESULTS = ".TigerResult";
	private static final String PARSED_FILE_SUFFIX = ".parsed";

	private final String name;
	private final Connection conn;


	public ClientRunWriter(Connection conn, String name) {
		this.name = name;
		this.conn = conn;
	}

	public void write() {

		try {
			conn.setAutoCommit(false);
	
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
				RunGenerator generator = JDBCRunGenerator.getInstance(conn);
				mw.parseRunDocument(f, generator, null);
				
			}
			log.log(Level.INFO, "Run persisted");
			conn.commit();
		} catch (SQLException e) {
			throw new RunPersistenceException(e);
		}
	}

}
