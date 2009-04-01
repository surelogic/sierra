package com.surelogic.sierra.tool;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
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
	public static final String SIERRA_TOOLS_DIR = "sierra.tools.dir";
	private static final String TOOLS_SUBDIR = "tools";
	private static final String RECKONER = "reckoner";
	private static final String PMD = "pmd";
	private static final String CPD = "cpd";
	private static final String FINDBUGS = "findbugs";

	private static final String[] TOOLS = {
		RECKONER, PMD, CPD, FINDBUGS
	};
	
	/** The logger */
	protected static final Logger LOG = SLLogger.getLogger("sierra");
	
	private static final List<IToolFinder> finders = new ArrayList<IToolFinder>();
	static {
		addToolFinder(new IToolFinder() {
			public List<File> findToolDirectories() {
		        /*
				for(File plugin : findToolPlugins()) {
					System.out.println("Found plugin @ "+plugin.getAbsolutePath());
				}
				*/
				return findToolPlugins(new File(getSierraToolDirectory(), TOOLS_SUBDIR));
			}			
		});
	}
	
	public static File getSierraToolDirectory() {
		String path = System.getProperty(SIERRA_TOOLS_DIR);
		if (path != null) {
			File dir = new File(path);
			if (dir.exists() && dir.isDirectory()) {			
				// TODO should it check for anything else?
				return dir;
			} else {
				LOG.warning("Invalid tools directory: "+path);
			}
		}
		return FileUtility.getSierraDataDirectory();		
	}
	
	public static void addToolFinder(IToolFinder f) {
		synchronized (finders) {
			finders.add(f);
		}
	}
	
	public static Set<File> findToolDirs() {
		Set<File> dirs = new HashSet<File>();
		for(IToolFinder f : finders) {
			for(File dir : f.findToolDirectories()) {
				if (!dirs.contains(dir)) {
					dirs.add(dir);
				}
			}
		}
		return dirs;
	}
	
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
			final String fbDir = getSierraToolDirectory().getAbsolutePath();
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
	
	private static String MANIFEST = "META-INF"+File.separatorChar+"MANIFEST.MF";
	private static String CLASSPATH = "Bundle-ClassPath";
	private static String TOOL_ID = "Sierra-Tool";
	
	// FIX how to identify tools?
	// By ITool?  How to make it consistent with a plugin.xml?
	private static Attributes readManifest(File pluginDir) throws IOException {
		File manifest = new File(pluginDir, MANIFEST);
		if (!manifest.exists() || !manifest.isFile()) {
			return null;
		}		
		return new Manifest(new FileInputStream(manifest)).getMainAttributes();
	}
	
	private static List<File> findToolPlugins(File pluginsDir) {
		List<File> tools = new ArrayList<File>();
		for(File f : pluginsDir.listFiles()) {
			if (f.isDirectory()) {
				try {
					Attributes attrs = readManifest(f);
					if (attrs == null) {
						continue;
					}
					List<IToolFactory> factories = instantiateToolFactories(f, attrs);
					if (!factories.isEmpty()) {
						tools.add(f);
					}
				} catch (IOException e) {
					LOG.log(Level.INFO, "Couldn't read manifest for "+f.getAbsolutePath(), e);
				}
			}
		}
		return tools;
	}
	
	// FIX Note that this only allows for one tool per plugin
	// FIX loading properties doesn't allow multi-line
	private static List<IToolFactory> instantiateToolFactories(File pluginDir, Attributes attrs) {
		String ids = attrs.getValue(TOOL_ID);
		if (ids != null) {
			StringTokenizer st = new StringTokenizer(ids, ",");
			if (!st.hasMoreTokens()) {
				return Collections.emptyList();
			}
			List<File> path = getRequiredJars(pluginDir, attrs);
			if (!path.isEmpty()) {
				ClassLoader cl = computeClassLoader(path);			
				String id = null;
				try {
					List<IToolFactory> factories = new ArrayList<IToolFactory>();
					while (st.hasMoreTokens()) {
						id = st.nextToken().trim();					
						
						Class<?> toolClass = cl.loadClass(id);
						Object o = toolClass.newInstance();
						if (o instanceof IToolFactory) {
							factories.add((IToolFactory) o);
						} else {
							LOG.info("Got a non-IToolFactory: "+id);
							return Collections.emptyList();
						}
					}
				} catch (ClassNotFoundException e) {
					LOG.log(Level.WARNING, "Couldn't load class "+id, e);
				} catch (InstantiationException e) {
					LOG.log(Level.WARNING, "Couldn't create class "+id, e);
				} catch (IllegalAccessException e) {
					LOG.log(Level.WARNING, "Couldn't access class "+id, e);
				}
			}
		}
		return Collections.emptyList();
	}
	
	public static List<File> getRequiredJars(File pluginDir) throws IOException {
		final Attributes attrs = readManifest(pluginDir);
		return getRequiredJars(pluginDir, attrs);
	}
		
	public static List<File> getRequiredJars(File pluginDir, Attributes attrs) {
		final String path = attrs.getValue(CLASSPATH);
		if (path == null) {
			return Collections.emptyList();
		}
		StringTokenizer st = new StringTokenizer(path, ",");
		if (!st.hasMoreTokens()) {
			return Collections.emptyList();
		}
		List<File> jars = new ArrayList<File>();				
		while (st.hasMoreTokens()) {
			String fragment = st.nextToken().trim();
			if (".".equals(fragment)) {
				File pluginBin = new File(pluginDir, "bin");
				if (pluginBin.exists() && pluginBin.isDirectory()) {
					// local binary
					jars.add(pluginBin);
					continue;
				}
				// otherwise, add the directory
			}
			// plugin-relative path
			jars.add(new File(pluginDir, fragment));			
		}
		return jars;
	}
	
	public static URLClassLoader computeClassLoader(List<File> plugins) {
		URL[] urls = new URL[plugins.size()];
		int i = 0;
		for(File jar : plugins) {
			try {
				urls[i] = jar.toURI().toURL();
			} catch (MalformedURLException e) {
				LOG.log(Level.WARNING, "Problem converting "+jar.getAbsolutePath()+" to URL", e);
			}
			i++;
		}
		return new URLClassLoader(urls, ToolUtil.class.getClassLoader());
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
