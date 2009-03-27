package com.surelogic.sierra.tool;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.surelogic.common.FileUtility;
import com.surelogic.common.jobs.*;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.tool.analyzer.LazyZipDirArtifactGenerator;
import com.surelogic.sierra.tool.findbugs.*;
import com.surelogic.sierra.tool.message.Config;
import com.surelogic.sierra.tool.message.MessageWarehouse;
import com.surelogic.sierra.tool.pmd.*;
import com.surelogic.sierra.tool.reckoner.*;

public class ToolUtil {
	private static final String RECKONER = "reckoner";
	private static final String PMD = "pmd";
	private static final String CPD = "cpd";
	private static final String FINDBUGS = "findbugs";
	private static final String[] TOOLS = {
		RECKONER, PMD, CPD, FINDBUGS
	};
	
	/** The logger */
	protected static final Logger LOG = SLLogger.getLogger("sierra");

	public static ITool create(Config config, boolean runRemotely) {
		if (runRemotely) {
			if (SierraToolConstants.RUN_TOGETHER) {
				return new LocalTool(config);
			} else {
				// Alternately, run in each in separate JVMs
				final MultiTool t = new MultiTool(config);
				for(String tool : TOOLS) {
					if (config.isToolIncluded(tool)) {
						final Config c = updateForTool(config, tool);
						t.addTool(new LocalTool(c));
					}
				}
				/*
				if (t.size() > 0) {
					final File tempDir = LazyZipDirArtifactGenerator.computeTempDir(config.getScanDocument());
					LazyZipDirArtifactGenerator.createConfigStream(tempDir, config);
				}
				*/
				return t;
			}
		}
		return createTools(config);
	}
	
	private static Config updateForTool(Config orig, String tool) {
		final Config copy  = orig.clone();
		// Create an uncompressed result
		final File tempDir = LazyZipDirArtifactGenerator.computeTempDir(orig.getScanDocument());
		tempDir.mkdir();
		copy.setScanDocument(new File(tempDir, tool + MessageWarehouse.TOOL_STREAM_SUFFIX));
		
		// Set it to only run this one tool
		StringBuilder sb = new StringBuilder();
		for(String t : TOOLS) {
			if (!t.equals(tool)) {
				// Set as excluded
				if (sb.length() > 0) {
					sb.append(',');
				}
				sb.append(t);
			}
		}
		copy.setExcludedToolsList(sb.toString());
		return copy;
	}
	
	public static MultiTool createTools(Config config) {
		final MultiTool t = new MultiTool(config);
		if (config.isToolIncluded(FINDBUGS)) {
			//final String fbDir = config.getPluginDir(SierraToolConstants.FB_PLUGIN_ID);
			final String fbDir = FileUtility.getSierraDataDirectory().getAbsolutePath();
			AbstractFindBugsTool.init(fbDir);
			t.addTool(new AbstractFindBugsTool(fbDir, config));
		}
		if (config.isToolIncluded(PMD)) {
			t.addTool(new AbstractPMDTool(config));
		}
		if (config.isToolIncluded(CPD)) {
			t.addTool(new CPD4_1Tool(config));
		}
		if (config.isToolIncluded(RECKONER)) {
			t.addTool(new Reckoner1_0Tool(config));
		}
		return t;
	}
	
	public static Set<ArtifactType> getArtifactTypes(Config config) {
		return createTools(config).getArtifactTypes();
	}
	
	public static int getNumTools(Config config) {
		int count = 0;
		for(String tool : TOOLS) {
			if (config.isToolIncluded(tool)) {
				count++;
			}
		}
		return count;
	}

	public static SLStatus scan(Config config, SLProgressMonitor mon,
			boolean runRemotely) {
		final boolean fineIsLoggable = LOG.isLoggable(Level.FINE);
		final ITool t = ToolUtil.create(config, runRemotely);

		if (fineIsLoggable) {
			LOG.fine("Excluded: " + config.getExcludedToolsList());
			LOG.fine("Java version: " + config.getJavaVersion());
			LOG.fine("Rules file: " + config.getPmdRulesFile());
		}
		IToolInstance ti = t.create();
		if (fineIsLoggable) {
			LOG.fine("Created " + ti.getClass().getSimpleName());
		}
		return ti.run(mon);
	}

	public static String getTimeStamp() {
		final Date date = Calendar.getInstance().getTime();
		final SimpleDateFormat dateFormat = new SimpleDateFormat(
				"yyyy.MM.dd-'at'-HH.mm.ss.SSS");
		return dateFormat.format(date);
	}
}
