package com.surelogic.sierra.server.lifecycle;

import java.io.File;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.FileHandler;
import java.util.logging.Level;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.surelogic.common.FileUtility;
import com.surelogic.common.i18n.I18N;
import com.surelogic.common.jdbc.SchemaUtility;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.jdbc.scan.ScanQueries;
import com.surelogic.sierra.jdbc.server.ConnectionFactory;
import com.surelogic.sierra.jdbc.server.Server;
import com.surelogic.sierra.jdbc.server.ServerFiles;
import com.surelogic.sierra.jdbc.server.ServerTransaction;
import com.surelogic.sierra.jdbc.settings.ConnectedServer;
import com.surelogic.sierra.jdbc.settings.ServerLocations;
import com.surelogic.sierra.jdbc.settings.SettingQueries;
import com.surelogic.sierra.schema.SierraSchemaData;
import com.surelogic.sierra.tool.message.ExtensionName;
import com.surelogic.sierra.tool.message.ServerInfoReply;
import com.surelogic.sierra.tool.message.ServerInfoRequest;
import com.surelogic.sierra.tool.message.ServerInfoServiceClient;
import com.surelogic.sierra.tool.message.ServerLocation;
import com.surelogic.sierra.tool.message.SierraServiceClientException;

/**
 * This servlet context listener gets the Sierra team server code ready to run.
 * It sets up SLLogger based upon the web.xml context parameters on server
 * startup. It also checks that the database schema is up to date with the code.
 * <p>
 * The parameter <tt>SLLogger</tt> may be set to <tt>serverdir</tt> to cause the
 * logger to log under the ~/Sierra/Server directory. If <tt>SLLogger</tt> is
 * set to <tt>tempdir</tt> the logging will go into the temporary directory. The
 * default (no value) is to log only to the console.
 * <p>
 * The parameter <tt>SLLoggerTag</tt> is set to a string to include in the
 * middle of the log file name. If this parameter is not set a default value of
 * <tt>team-server</tt> is used.
 */
public class BootUpServletContextListener implements ServletContextListener {

  public static final int DELAY = 300;
  public static final TimeUnit UNIT = TimeUnit.SECONDS;

  public void contextDestroyed(final ServletContextEvent sce) {
    ConnectionFactory.INSTANCE.dispose();
  }

  public void contextInitialized(final ServletContextEvent sce) {
    /*
     * Configure SLLogger based upon the web.xml context parameters.
     */
    try {
      final String loggerOption = sce.getServletContext().getInitParameter("SLLogger");
      final String loggerTag = sce.getServletContext().getInitParameter("SLLoggerTag");
      final String contextName = sce.getServletContext().getServletContextName();
      ConnectionFactory.INSTANCE.init();
      bootLogging(loggerOption, loggerTag, contextName);
      bootDatabase();
      clearCache();
    } catch (final Error e) {
      SLLogger.getLogger().log(Level.SEVERE, e.getMessage(), e);
    } catch (final Exception e) {
      SLLogger.getLogger().log(Level.SEVERE, e.getMessage(), e);
    }

    /*
     * Start buglink
     */
    try {
      ConnectionFactory.INSTANCE.lookupTimerService().scheduleWithFixedDelay(new Runnable() {
        public void run() {
          try {
            final List<ConnectedServer> servers = new ArrayList<ConnectedServer>(ConnectionFactory.INSTANCE.withReadOnly(
                ServerLocations.fetchQuery(null)).keySet());
            SLLogger.getLogger().info("Updating scan filters and categories from " + servers + " at " + new Date());
            ConnectedServer server;
            for (final Iterator<ConnectedServer> i = servers.iterator(); i.hasNext();) {
              try {
                server = i.next();
                final ServerInfoReply reply = ServerInfoServiceClient.create(server.getLocation()).getServerInfo(
                    new ServerInfoRequest());
                ServerLocations.updateServerIdentities(reply.getServers());
              } catch (final SierraServiceClientException e) {
                SLLogger.getLogger().log(Level.INFO, e.getMessage(), e);
                i.remove();
              }
            }
            for (final ConnectedServer s : servers) {
              final ServerLocation location = s.getLocation();
              final List<ExtensionName> localExtensions = ConnectionFactory.INSTANCE.withTransaction(SettingQueries
                  .localExtensions());
              ConnectionFactory.INSTANCE.withTransaction(SettingQueries.retrieveCategories(location,
                  ConnectionFactory.INSTANCE.withReadOnly(SettingQueries.categoryRequest()), localExtensions));
              ConnectionFactory.INSTANCE.withTransaction(SettingQueries.retrieveScanFilters(location,
                  ConnectionFactory.INSTANCE.withReadOnly(SettingQueries.scanFilterRequest()), localExtensions));
            }
          } catch (final Error e) {
            SLLogger.getLogger().log(Level.SEVERE, e.getMessage(), e);
            throw e;
          } catch (final RuntimeException e) {
            SLLogger.getLogger().log(Level.SEVERE, e.getMessage(), e);
            throw e;
          }
        }
      }, DELAY, DELAY, UNIT);
      SLLogger.getLogger().info("Buglink update scheduled for every " + DELAY + " " + UNIT + ".");
    } catch (final Exception e) {
      SLLogger.getLogger().log(Level.SEVERE, e.getMessage(), e);
    }
  }

  private void bootLogging(final String loggerOption, String loggerTag, final String contextName) {
    if (loggerTag == null) {
      /*
       * Set a default
       */
      loggerTag = "team-server";
    }
    String toString = "";
    if (loggerOption != null) {
      final SimpleDateFormat dateFormat = new SimpleDateFormat("-yyyy_MM_dd");
      final File path;
      final String tail = "log-" + loggerTag + dateFormat.format(new Date()) + ".txt";
      if ("serverdir".equals(loggerOption)) {
        path = ServerFiles.getSierraLocalTeamServerDirectory();
      } else {
        path = new File(System.getProperty("java.io.tmpdir"));
      }
      final File logFile = new File(path, tail);
      try {
        final FileHandler fh = new FileHandler(logFile.getAbsolutePath(), true);
        SLLogger.addHandler(fh);
      } catch (final Exception e) {
        SLLogger.getLogger().log(Level.SEVERE, I18N.err(29, logFile.getAbsolutePath()), e);
      }
      toString = "to '" + logFile.getAbsolutePath() + "' ";
    }
    final Runtime rt = Runtime.getRuntime();
    final long maxMemoryMB = rt.maxMemory() / 1024L / 1024L;
    final long totalMemoryMB = rt.totalMemory() / 1024L / 1024L;
    final long freeMemoryMB = rt.freeMemory() / 1024L / 1024L;
    SLLogger.getLogger().info(
        contextName + " logging " + toString + "initialized : Java runtime: maxMemory=" + maxMemoryMB + " MB; totalMemory="
            + totalMemoryMB + " MB; freeMemory=" + freeMemoryMB + " MB; availableProcessors=" + rt.availableProcessors());
  }

  private void bootDatabase() {
    /*
     * Bootstrap or update up the database as necessary.
     */
    ConnectionFactory.INSTANCE.withTransaction(new ServerTransaction<Void>() {
      public Void perform(final Connection conn, final Server server) throws Exception {
        SchemaUtility.checkAndUpdate(conn, new SierraSchemaData(), true);
        return null;
      }
    });
    ConnectionFactory.INSTANCE.withTransaction(ScanQueries.deleteUnfinishedScans(null));
    SLLogger.getLogger().info(
        "Derby booted with derby.storage.pageSize=" + System.getProperty("derby.storage.pageSize")
            + " and derby.storage.pageCacheSize=" + System.getProperty("derby.storage.pageCacheSize") + " @ " + ConnectionFactory.INSTANCE.getDBName());
  }

  private void clearCache() {
    final File cacheDir = ServerFiles.getSierraTeamServerCacheDirectory();
    if (FileUtility.recursiveDelete(cacheDir)) {
      SLLogger.getLogger().log(Level.INFO, "Cache cleared from " + cacheDir.getAbsolutePath());
    } else {
      SLLogger.getLogger().log(Level.INFO, "Cache was not cleared (maybe it did not exist) from " + cacheDir.getAbsolutePath());
    }
  }
}
