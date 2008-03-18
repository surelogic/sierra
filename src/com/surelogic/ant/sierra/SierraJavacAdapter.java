/*
 * Created on Jan 18, 2008
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package com.surelogic.ant.sierra;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.zip.GZIPInputStream;

import org.apache.tools.ant.*;
import org.apache.tools.ant.taskdefs.compilers.*;
import org.apache.tools.ant.types.*;
import org.apache.tools.ant.util.StringUtils;

import com.surelogic.common.SLProgressMonitor;
import com.surelogic.sierra.tool.*;
import com.surelogic.sierra.tool.message.Config;
import com.surelogic.sierra.tool.message.MessageWarehouse;
import com.surelogic.sierra.tool.message.ScanVersionException;
import com.surelogic.sierra.tool.message.TimeseriesRequest;
import com.surelogic.sierra.tool.message.Scan;
import com.surelogic.sierra.tool.message.SierraServerLocation;
import com.surelogic.sierra.tool.message.SierraService;
import com.surelogic.sierra.tool.message.SierraServiceClient;
import com.surelogic.sierra.tool.targets.*;
import com.surelogic.sierra.tool.targets.IToolTarget.Type;

public class SierraJavacAdapter extends DefaultCompilerAdapter {
	boolean keepRunning = true;

	Path sourcepath = null;
	final SierraScan scan;

	public SierraJavacAdapter(SierraScan sierraScan) {
		scan = sierraScan;
	}

	public boolean execute() throws BuildException {
		try {
			Config config = createConfig();
			ToolUtil.scan(config, new Monitor(), true);

			if (scan.getServer() != null && !"".equals(scan.getServer())) {
				uploadRunDocument(config);
			}
		} catch (Throwable t) {
			t.printStackTrace();
			throw new BuildException("Exception while scanning", t);
		}
		return true;
	}

	private Config createConfig() throws IOException {
		Config config = new Config();
		setupConfig(config, false);
		logAndAddFilesToCompile(config);

		String libHome = "C:/work/workspace/sierra-ant/lib/";
		config.setExcludedToolsList("checkstyle");
		config.setToolsDirectory(new File(libHome+"reckoner"));
		config.putPluginDir(SierraToolConstants.COMMON_PLUGIN_ID,
				libHome+"common.jar");
		config.putPluginDir(SierraToolConstants.MESSAGE_PLUGIN_ID,
				libHome+"sierra-message.jar");
		config.putPluginDir(SierraToolConstants.PMD_PLUGIN_ID,
				libHome+"pmd");
		config.putPluginDir(SierraToolConstants.FB_PLUGIN_ID,
				libHome+"findbugs");
		config.putPluginDir(SierraToolConstants.TOOL_PLUGIN_ID,
				libHome+"sierra-tool.jar");		
		config.putPluginDir(SierraToolConstants.JUNIT4_PLUGIN_ID,
				"C:/eclipse/plugins/org.junit4_4.3.1");

		String project = "sierra-tool";
		File tmp = File.createTempFile("sierra", ".xml");
		File scanDocument = new File(tmp.getParent() + File.separator + project
				+ " - " + ToolUtil.getTimeStamp()
				+ SierraToolConstants.PARSED_FILE_SUFFIX);
		config.setScanDocument(scanDocument);
		return config;
	}

	private void addPath(Config config, Type type, Path path) {
		for (String elt : path.list()) {
			File f = new File(elt);
			if (f.exists()) {
				if (f.isDirectory()) {
					System.out.println(type+": "+elt);
					config.addTarget(new FullDirectoryTarget(type, f.toURI()));
				} else {
					//System.out.println("Ignored: "+elt);
				}
			} 
		}
	}

	/**
	 * Originally based on
	 * DefaultCompilerAdapter.setupJavacCommandlineSwitches()
	 */
	protected Config setupConfig(Config cmd, boolean useDebugLevel) {
		Path classpath = getCompileClasspath();
		// For -sourcepath, use the "sourcepath" value if present.
		// Otherwise default to the "srcdir" value.
		Path sourcepath;
		if (compileSourcepath != null) {
			sourcepath = compileSourcepath;
		} else {
			sourcepath = src;
		}

		/*
		 * if (memoryMaximumSize != null) { if (!attributes.isForkedJavac()) {
		 * attributes.log("Since fork is false, ignoring " + "memoryMaximumSize
		 * setting.", Project.MSG_WARN); } else {
		 * cmd.createArgument().setValue(memoryParameterPrefix + "mx" +
		 * memoryMaximumSize); } }
		 */

		if (destDir != null) {
			cmd.addTarget(new FullDirectoryTarget(Type.BINARY, destDir
							.toURI()));
		}
		
		//addPath(cmd, Type.AUX, classpath);
		
		// If the buildfile specifies sourcepath="", then don't
		// output any sourcepath.
		if (sourcepath.size() > 0) {
			//addPath(cmd, Type.SOURCE, sourcepath);
			this.sourcepath = sourcepath;
		}		

		/*
		 * Path bp = getBootClassPath(); if (bp.size() > 0) { addPath(cmd,
		 * Type.AUX, bp); }
		 */

		/*
		 * if (verbose) { cmd.createArgument().setValue("-verbose"); }
		 */

		return cmd;
	}

	/**
	 * Based on DefaultCompilerAdapter.logAndAddFilesToCompile()
	 */
	protected void logAndAddFilesToCompile(Config config) {
		attributes.log("Compilation for " + config.getProject(),
				Project.MSG_VERBOSE);

		StringBuffer niceSourceList = new StringBuffer("File");
		if (compileList.length != 1) {
			niceSourceList.append('s');
		}
		niceSourceList.append(" to be compiled:");

		niceSourceList.append(StringUtils.LINE_SEP);

		for (int i = 0; i < compileList.length; i++) {
			String arg = compileList[i].getAbsolutePath();
			config.addTarget(new FileTarget(Type.SOURCE, new File(arg).toURI(),
					findSrcDir(arg)));
			niceSourceList.append("    ");
			niceSourceList.append(arg);
			niceSourceList.append(StringUtils.LINE_SEP);
		}
		/*
		 * 
		 * if (attributes.getSourcepath() != null) { addPath(config,
		 * Type.SOURCE, attributes.getSourcepath()); } else { addPath(config,
		 * Type.SOURCE, attributes.getSrcdir()); } addPath(config, Type.AUX,
		 * attributes.getClasspath());
		 */

		attributes.log(niceSourceList.toString(), Project.MSG_VERBOSE);
	}

	private URI findSrcDir(String arg) {
		for (String src : sourcepath.list()) {
			if (arg.startsWith(src)) {
				return new File(src).toURI();
			}
		}
		return null;
	}

	class Monitor implements SLProgressMonitor {
		public void beginTask(String name, int totalWork) {
			// TODO Auto-generated method stub

		}

		public void done() {
			// TODO Auto-generated method stub

		}

		public void error(String msg) {
			// TODO Auto-generated method stub

		}

		public void error(String msg, Throwable t) {
			// TODO Auto-generated method stub

		}

		public void failed(String msg) {
			System.err.println(msg);
		}

		public void failed(String msg, Throwable t) {
			System.err.println(msg);
			t.printStackTrace(System.err);
		}

		public Throwable getFailureTrace() {
			// TODO Auto-generated method stub
			return null;
		}

		public void internalWorked(double work) {
			// TODO Auto-generated method stub

		}

		public boolean isCanceled() {
			return !keepRunning;
		}

		public void setCanceled(boolean value) {
			keepRunning = false;
		}

		public void setTaskName(String name) {
			// TODO Auto-generated method stub

		}

		public void subTask(String name) {
			//System.out.println("Subtask: "+name);
		}

		public void worked(int work) {
			// TODO Auto-generated method stub

		}

	}

	/**
	 * Modified from SierraAnalysis.uploadRunDocument()
	 * 
	 * Optional action. Uploads the generated scan document to the desired
	 * server.
	 * 
	 * @param config
	 */
	private void uploadRunDocument(final Config config) {
		if (keepRunning) {
			scan.log("Uploading the Run document to " + scan.getServer()
					+ "...", org.apache.tools.ant.Project.MSG_INFO);
			MessageWarehouse warehouse = MessageWarehouse.getInstance();
			Scan run;
			try {
				run = warehouse.fetchScan(new GZIPInputStream(
						new FileInputStream(config.getScanDocument()
								.getAbsolutePath())));

				SierraServerLocation location = new SierraServerLocation(scan
						.getServer(), scan.getUser(), scan.getPassword());

				SierraService ts = SierraServiceClient.create(location);

				// Verify the timeseries
				List<String> list = ts.getTimeseries(new TimeseriesRequest())
						.getTimeseries();
				if (list == null || list.isEmpty()) {
					throw new BuildException(
							"The target build server does not have any valid timeseries to publish to.");
				}
				if (!list.containsAll(scan.getTimeseries())) {
					StringBuilder sb = new StringBuilder();
					sb.append("Invalid timeseries. Valid timeseries are:\n");
					for (String string : list) {
						sb.append(string);
						sb.append("\n");
					}
					throw new BuildException(sb.toString());
				}
				// FIXME utilize the return value once Bug 867 is resolved
				ts.publishRun(run);
			} catch (FileNotFoundException e) {
				throw new IllegalStateException(config.getScanDocument()
						+ " is not a valid document", e);
			} catch (IOException e) {
				throw new IllegalStateException(config.getScanDocument()
						+ " is not a valid document", e);
			} catch (ScanVersionException e) {
				throw new IllegalStateException(config.getScanDocument()
						+ " is not the same version as the server.", e);
			}
		}
	}
}
