package com.surelogic.sierra.cache;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import javax.servlet.ServletException;

import com.surelogic.common.FileUtility;
import com.surelogic.common.Sweepable;
import com.surelogic.common.i18n.I18N;
import com.surelogic.common.jdbc.ConnectionQuery;
import com.surelogic.common.jdbc.Query;
import com.surelogic.common.jdbc.Result;
import com.surelogic.common.jdbc.ResultHandler;
import com.surelogic.common.jdbc.Row;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.gwt.client.data.ReportSettings;
import com.surelogic.sierra.gwt.client.data.ReportTable;
import com.surelogic.sierra.jdbc.server.ConnectionFactory;
import com.surelogic.sierra.jdbc.server.Server;
import com.surelogic.sierra.jdbc.server.ServerQuery;
import com.surelogic.sierra.jdbc.server.ServerTransaction;
import com.surelogic.sierra.table.IDatabaseTable;

public class TableCache implements Sweepable {

	public ReportTable getReportTable(final Ticket ticket) throws IOException,
			ServletException {
		final File table = getTableFileFor(ticket);
		checkAndUpdateCache(ticket);
		return readTable(table);
	}

	private static final String TABLE_CACHE_FILE_PREFIX = "table-";

	private File getTableFileFor(final Ticket ticket) {
		return new File(FileUtility.getSierraTeamServerCacheDirectory()
				+ File.separator + TABLE_CACHE_FILE_PREFIX
				+ ticket.getUUID().toString() + ".table");
	}

	private File getRevFileFor(final Ticket ticket) {
		return new File(FileUtility.getSierraTeamServerCacheDirectory()
				+ File.separator + TABLE_CACHE_FILE_PREFIX
				+ ticket.getUUID().toString() + ".rev");
	}

	private static ReportTable readTable(final File file) throws IOException,
			ServletException {
		final ObjectInputStream in = new ObjectInputStream(new FileInputStream(
				file));
		try {
			return (ReportTable) in.readObject();
		} catch (final ClassNotFoundException e) {
			throw new ServletException(e);
		} finally {
			in.close();
		}
	}

	private static void writeTable(final ReportTable table, final File file)
			throws IOException {
		final ObjectOutputStream out = new ObjectOutputStream(
				new FileOutputStream(file));
		try {
			out.writeObject(table);
		} finally {
			out.close();
		}
	}

	private void checkAndUpdateCache(final Ticket ticket)
			throws ServletException {
		boolean createOrUpdateCacheFiles = true;
		final File file = getRevFileFor(ticket);
		/*
		 * Does a cached file exist?
		 */
		if (file.exists()) {
			/*
			 * Is it OK to use cached file?
			 */
			try {
				final BufferedReader reader = new BufferedReader(
						new FileReader(file));
				final long rev = Long.valueOf(reader.readLine());
				final long lastRevision = ConnectionFactory.getInstance()
						.withReadUncommitted(new RevisionQuery());

				createOrUpdateCacheFiles = lastRevision > rev;
			} catch (final Exception e) {
				throw new IllegalStateException(e);
			}
		}
		if (createOrUpdateCacheFiles) {
			createCacheFiles(ticket);
		}
	}

	private void createCacheFiles(final Ticket ticket) throws ServletException {
		SLLogger.getLogger().log(Level.FINE,
				"Creating table files for ticket " + ticket);
		final ReportSettings report = ticket.getReport();
		final String type = report.getReportUuid();
		if (type == null) {
			throw new ServletException(I18N.err(96, ticket.toString()));
		}
		final IDatabaseTable generator = getGenerator(type);

		ConnectionFactory.getInstance().withReadOnly(
				new ServerTransaction<Void>() {
					public Void perform(final Connection conn,
							final Server server) throws SQLException {
						try {
							/*
							 * Output table file
							 */
							final ReportTable table = generator.generate(
									report, conn);
							final File tableFile = getTableFileFor(ticket);
							writeTable(table, tableFile);
							/*
							 * Output revision file.
							 */
							final File revFile = getRevFileFor(ticket);
							final PrintWriter revWriter = new PrintWriter(
									revFile);
							try {
								revWriter.println(new RevisionQuery().perform(
										new ConnectionQuery(conn), server));

							} finally {
								revWriter.close();
							}
							return null;
						} catch (final IOException e) {
							final SQLException sqle = new SQLException();
							sqle.initCause(e);
							throw sqle;
						}
					}
				});
	}

	private static class RevisionQuery implements ServerQuery<Long> {
		public Long perform(final Query q, final Server s) {
			return q.statement("Revision.maxRevision",
					new ResultHandler<Long>() {
						public Long handle(final Result r) {
							for (final Row row : r) {
								return row.nextLong();
							}
							return -1L;
						}
					}).call();
		}
	}

	/**
	 * All access to this map is protected by a lock on the object.
	 */
	private final Map<String, IDatabaseTable> f_typeToGenerator = new HashMap<String, IDatabaseTable>();

	private IDatabaseTable getGenerator(final String type)
			throws ServletException {
		assert type != null;

		IDatabaseTable generator;
		synchronized (f_typeToGenerator) {
			generator = f_typeToGenerator.get(type);
			if (generator == null) {
				/*
				 * Try to find the plotter, either it doesn't exist (in which
				 * case we throw an exception) or this is the first time it has
				 * been requested.
				 */
				final String plotClassName = "com.surelogic.sierra.table."
						+ type;
				try {
					generator = (IDatabaseTable) Class.forName(plotClassName)
							.newInstance();
					f_typeToGenerator.put(type, generator);
				} catch (final ClassCastException e) {
					throw new ServletException(I18N
							.err(82, type, plotClassName), e);
				} catch (final InstantiationException e) {
					throw new ServletException(I18N
							.err(82, type, plotClassName), e);
				} catch (final IllegalAccessException e) {
					throw new ServletException(I18N
							.err(82, type, plotClassName), e);
				} catch (final ClassNotFoundException e) {
					throw new ServletException(I18N
							.err(82, type, plotClassName), e);
				}
			}
		}
		return generator;
	}

	/**
	 * The singleton instance.
	 */
	private static final TableCache INSTANCE = new TableCache();

	/**
	 * Gets the singleton instance of this class.
	 * 
	 * @return the singleton instance of this class.
	 */
	public static TableCache getInstance() {
		return INSTANCE;
	}

	private TableCache() {
		// singleton
	}

	public void periodicSweep() {
		// TODO clear out the file cache and our references to it.
	}

}
