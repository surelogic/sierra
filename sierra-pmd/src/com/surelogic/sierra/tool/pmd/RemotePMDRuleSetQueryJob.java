package com.surelogic.sierra.tool.pmd;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import net.sourceforge.pmd.Rule;
import net.sourceforge.pmd.RuleSet;
import net.sourceforge.pmd.RuleSetFactory;
import net.sourceforge.pmd.RuleSetNotFoundException;
import net.sourceforge.pmd.RuleSetWriter;
import net.sourceforge.pmd.lang.java.JavaLanguageModule;

import com.surelogic.common.jobs.AbstractSLJob;
import com.surelogic.common.jobs.SLJob;
import com.surelogic.common.jobs.SLProgressMonitor;
import com.surelogic.common.jobs.SLStatus;
import com.surelogic.common.jobs.remote.AbstractRemoteSLJob;
import com.surelogic.sierra.tool.ArtifactType;
import com.surelogic.sierra.tool.IToolExtension;

public class RemotePMDRuleSetQueryJob extends AbstractRemoteSLJob {
	public static final String QUERY_RESULTS_DIR = "PMDQueryResultsDir";
	public static final String TOOL_EXTENSIONS = "toolExtensions";
	public static final String RULESET_LOCATIONS = "rulesetLocations";
	public static final String PMD_VERSION = "PMDVersion";
	
	public static void main(String[] args) {
		RemotePMDRuleSetQueryJob job = new RemotePMDRuleSetQueryJob();
		job.run();
	}
	
	@Override
	protected SLJob init(BufferedReader br, Monitor mon) throws Throwable {
		return new QueryJob(mon);
	}
	
	static class QueryJob extends AbstractSLJob {
		final Monitor mon;
		
		public QueryJob(Monitor m) {
			super("Query PMD rulesets");
			mon = m;
		}

		@Override
		public SLStatus run(SLProgressMonitor monitor) {
			Closeable out = null;
			File f = null;
			try {
				final String name = System.getProperty(QUERY_RESULTS_DIR);
				System.out.println(QUERY_RESULTS_DIR+" = "+name);
				if (name == null) {
					mon.failed("Unable to get value for -D"+QUERY_RESULTS_DIR);
					return SLStatus.CANCEL_STATUS;
				}
				System.out.println("Creating output stream");
				f = new File(name, TOOL_EXTENSIONS);
				
				final FileOutputStream os = new FileOutputStream(f);				
				//out = packageDefaultRuleSets(os);
				out = serializeToolExtensions(os);
				
				if (out != null) {
					out.close();
				}
				f = new File(name, RULESET_LOCATIONS);
				
				@SuppressWarnings("resource") // Closed below
				final PrintWriter pw = new PrintWriter(f);
				out = pw;
				
				for(RuleSet s : getDefaultRuleSets()) {
					if (s.getFileName() == null) {
						mon.failed("No file name for "+s.getName());
						return SLStatus.CANCEL_STATUS;
					}
					pw.println(s.getFileName());
				}
				return SLStatus.OK_STATUS;
			} catch (RuleSetNotFoundException e) {
				e.printStackTrace(System.out);
				return SLStatus.createErrorStatus(e);
			} catch (IOException e) {
				e.printStackTrace(System.out);
				return SLStatus.createErrorStatus(e);
			} finally {
				if (out != null) {
					try {
						System.out.println("Closing stream");
						out.close();
						
						if (f != null && !f.exists()) {
							System.out.println("Unable to find resulting file");
						} else {
							System.out.println("Created: "+f.getAbsolutePath()+" ("+f.length()+" bytes)");
						}
					} catch (IOException e) {
						return SLStatus.createErrorStatus(e);
					}
				}
			}
		}

		
	}
	
	static OutputStream serializeToolExtensions(final FileOutputStream os) throws RuleSetNotFoundException, IOException {
		final String version = System.getProperty(PMD_VERSION, "5.2.3");
		final ObjectOutputStream out = new ObjectOutputStream(os);
		
		for(RuleSet s : getDefaultRuleSets()) {
			System.out.println("Creating entry for "+s.getFileName());
			RuleSetInfo info = new RuleSetInfo(s);
			PMDToolExtension ext = createToolExtension(version, info);
			out.writeObject(ext);
		}
		return out;
	}
	
	static PMDToolExtension createToolExtension(String version, RuleSetInfo info) {
		final Set<ArtifactType> types = new HashSet<ArtifactType>();
		for (final Rule r : info.ruleset.getRules()) {
			final ArtifactType t = ArtifactType.create("PMD", version, info.props,
					info.ruleset.getFileName(), r.getName(), r
							.getRuleSetName());
			types.add(t);
		}			
		return new PMDToolExtension(info.ruleset.getName(), version, info.location, types, info.isCore);
	}
	
	static class RuleSetInfo {
		final File location;
		final RuleSet ruleset;
		final boolean isCore;
		final Manifest props;

		RuleSetInfo(File jar, final RuleSet rs, final boolean core, final Manifest props) {
			location = jar;
			ruleset = rs;
			isCore = core;
			this.props = props;
		}

		RuleSetInfo(final RuleSet rs) {
			this(null, rs, true, null);
		}
	}
	
	static OutputStream packageDefaultRuleSets(final FileOutputStream os) throws RuleSetNotFoundException, IOException {
		ZipOutputStream out = new ZipOutputStream(os);
		
		for(RuleSet s : getDefaultRuleSets()) {
			System.out.println("Creating entry for "+s.getFileName());
			final ZipEntry e = new ZipEntry(s.getFileName()); // TODO is this right?
			out.putNextEntry(e);
			
			RuleSetWriter w = new RuleSetWriter(out);
			w.write(s);
			//w.close();
			out.closeEntry();
		}
		return out;
	}
	
	// Code previously from PMDToolFactory.
	static List<RuleSet> getDefaultRuleSets() throws RuleSetNotFoundException {
		final List<RuleSet> sets = new ArrayList<RuleSet>();

		// only the default rules
        final ClassLoader cl = Thread.currentThread().getContextClassLoader();
        
		final RuleSetFactory ruleSetFactory = new RuleSetFactory();
		ruleSetFactory.setClassLoader(cl);
		final Iterator<RuleSet> it = ruleSetFactory.getRegisteredRuleSets();
		while (it.hasNext()) {
			final RuleSet ruleset = it.next();						
			/*
			if ("Android Rules".equals(ruleset.getName())) {
				continue;
			}
			*/
			//System.out.println("Looking at "+ruleset.getFileName());
			if (!hasJavaRules(ruleset)) {
				continue;
			}

			// System.out.println("Found "+ruleset.getName()+" in "+ruleset.getFileName());
			sets.add(ruleset);
		}
		return sets;
	}

	private static boolean hasJavaRules(RuleSet ruleset) {
		for(Rule r : ruleset.getRules()) {
			//System.out.println(r.getName()+" : "+r.getLanguage());
			if (r.getLanguage() instanceof JavaLanguageModule) {
				return true;
			}
 		}
		return false;
	}

	static List<String> getRuleSetFileNames() {
		List<String> names = new ArrayList<String>();
		try {
			for(RuleSet s : getDefaultRuleSets()) {
				names.add(s.getFileName());
			}
		} catch (RuleSetNotFoundException e) {
			return Collections.emptyList();
		}
		return names;
	}

	public static List<IToolExtension> getToolExtensions(String version) {
		List<IToolExtension> exts = new ArrayList<IToolExtension>();
		try {
			for(RuleSet s : getDefaultRuleSets()) {
				RuleSetInfo info = new RuleSetInfo(s);
				PMDToolExtension ext = createToolExtension(version, info);
				exts.add(ext);
			}
		} catch (RuleSetNotFoundException e) {
			return Collections.emptyList();
		}
		return exts;
	}	
}
