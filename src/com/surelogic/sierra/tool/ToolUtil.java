package com.surelogic.sierra.tool;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.surelogic.common.FileUtility;
import com.surelogic.common.jobs.*;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.tool.findbugs.*;
import com.surelogic.sierra.tool.message.Config;
import com.surelogic.sierra.tool.pmd.*;
import com.surelogic.sierra.tool.reckoner.*;

public class ToolUtil {
	/** The logger */
	protected static final Logger LOG = SLLogger.getLogger("sierra");

	public static ITool create(Config config, boolean runRemotely) {
		if (runRemotely) {
			return new LocalTool(config);
		}
		return createTools(config);
	}
		
	public static MultiTool createTools(Config config) {
		final MultiTool t = new MultiTool(config);
		if (config.isToolIncluded("findbugs")) {
			//final String fbDir = config.getPluginDir(SierraToolConstants.FB_PLUGIN_ID);
			final String fbDir = FileUtility.getSierraDataDirectory().getAbsolutePath();
			AbstractFindBugsTool.init(fbDir);
			t.addTool(new AbstractFindBugsTool(fbDir, config));
		}
		if (config.isToolIncluded("pmd")) {
			t.addTool(new AbstractPMDTool(config));
			t.addTool(new CPD4_1Tool(config));
		}
		if (config.isToolIncluded("reckoner")) {
			t.addTool(new Reckoner1_0Tool(config));
		}
		return t;
	}
	
	public static Set<ArtifactType> getArtifactTypes(Config config) {
		return createTools(config).getArtifactTypes();
	}
	
	public static int getNumTools(Config config) {
		int count = 0;
		if (config.isToolIncluded("findbugs")) {
			count++;
		}
		if (config.isToolIncluded("pmd")) {
			count += 2;
		}
		if (config.isToolIncluded("reckoner")) {
			count++;
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
