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
import com.surelogic.common.XUtil;
import com.surelogic.common.jobs.*;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.tool.analyzer.LazyZipDirArtifactGenerator;
import com.surelogic.sierra.tool.message.Config;
import com.surelogic.sierra.tool.message.MessageWarehouse;

public class ToolUtil {
	public static final String SIERRA_TOOLS_DIR = "sierra.tools.dir";
	private static final String TOOLS_SUBDIR = "tools";
	
	/** The logger */
	protected static final Logger LOG = SLLogger.getLogger("sierra");
	
	private static final List<IToolFinder> finders = new ArrayList<IToolFinder>();
	static {
		addToolFinder(new IToolFinder() {
			public List<File> findToolDirectories() {
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
	
	public static List<IToolFactory> findToolFactories() {
		return findToolFactories(XUtil.useExperimental());
	}
	
	public static List<IToolFactory> findToolFactories(boolean all) {
		final File home = getSierraToolDirectory();
		List<IToolFactory> factories = new ArrayList<IToolFactory>();
		for(File dir : findToolDirs()) {
			try {
				Attributes manifest = readManifest(dir);
				for(IToolFactory f : instantiateToolFactories(dir, manifest)) {
					if (all || f.isProduction()) {
						f.init(home, dir);
						factories.add(f);
					}
				}
			} catch (IOException e) {
				LOG.log(Level.INFO, "Couldn't read manifest for "+dir.getAbsolutePath(), e);
			}
		}
		return factories;
	}
	
	public static ITool create(Config config, boolean runRemotely) {
		if (runRemotely) {
			if (SierraToolConstants.RUN_TOGETHER) {
				return new LocalTool(config);
			} else {
				// Alternately, run in each in separate JVMs
				final MultiTool t = new MultiTool(config);
				for(IToolFactory f : findToolFactories()) {
					if (config.isToolIncluded(f.getId())) {
						final Config c = updateForTool(config, f);
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
	
	private static Config updateForTool(Config orig, IToolFactory factory) {
		final Config copy  = orig.clone();
		// Create an uncompressed result
		final File tempDir = LazyZipDirArtifactGenerator.computeTempDir(orig.getScanDocument());
		tempDir.mkdir();
		copy.setScanDocument(new File(tempDir, factory.getId() + MessageWarehouse.TOOL_STREAM_SUFFIX));
		
		// Set it to only run this one tool
		StringBuilder sb = new StringBuilder();
		for(IToolFactory tf : findToolFactories()) {
			if (!tf.equals(factory)) {
				// Set as excluded
				if (sb.length() > 0) {
					sb.append(',');
				}
				sb.append(tf.getId());
			}
		}
		copy.setExcludedToolsList(sb.toString());
		return copy;
	}
	
	public static MultiTool createTools(Config config) {        
		final MultiTool t = new MultiTool(config);
		for(IToolFactory f : findToolFactories()) {
			//System.out.println("Creating "+f.getId());
			t.addTool(f.create(config));
		}
		/*
		if (config.isToolIncluded(RECKONER)) {
			t.addTool(new Reckoner1_0Tool(config));
		}
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
		*/
		return t;
	}
	
	public static Set<ArtifactType> getArtifactTypes(Config config) {
		return createTools(config).getArtifactTypes();
	}
	
	private static final String MANIFEST = "META-INF"+File.separatorChar+"MANIFEST.MF";
	private static final String CLASSPATH = "Bundle-ClassPath";
	private static final String TOOL_ID = "Sierra-Tool";
	private static final String PLUGIN_VERSION = "Bundle-Version";
	private static final String DEPENDENCIES = "Require-Bundle";
	private static final String PLUGIN_ID = "Bundle-SymbolicName";	

	private static final Set<String> EXPECTED_DEPS;
	static {
		Set<String> temp = new HashSet<String>(4);
		temp.add(SierraToolConstants.COMMON_PLUGIN_ID);
		temp.add(SierraToolConstants.TOOL_PLUGIN_ID);
		temp.add(SierraToolConstants.MESSAGE_PLUGIN_ID);
		EXPECTED_DEPS = Collections.unmodifiableSet(temp);
	}
	
	// FIX how to identify tools?
	// By ITool?  How to make it consistent with a plugin.xml?
	private static Attributes readManifest(File pluginDir) throws IOException {
		File manifest = new File(pluginDir, MANIFEST);
		if (!manifest.exists() || !manifest.isFile()) {
			return null;
		}		
		return new Manifest(new FileInputStream(manifest)).getMainAttributes();
	}
	
	interface ItemProcessor<T> {
		boolean isCancelled();
		T process(String item);
	}
	
	abstract static class AbstractItemProcessor<T> implements ItemProcessor<T> {
		private boolean cancel = false;
		
		public boolean isCancelled() {
			return cancel;
		}
		
		protected T cancel() {
			cancel = true;
			return null;
		}
	}
	
	/**
	 * Get items from a comma-separated list
	 */
	private static <T> List<T> getItems(String items, ItemProcessor<T> p) {
		if (items == null) {
			return Collections.emptyList();
		}
		StringTokenizer st = new StringTokenizer(items, ",");
		if (!st.hasMoreTokens()) {
			return Collections.emptyList();
		}
		List<T> rv = new ArrayList<T>();				
		while (!p.isCancelled() && st.hasMoreTokens()) {
			String label = st.nextToken().trim();
			T item = p.process(label);
			if (item != null) {
				rv.add(item);
			}
		}
		if (p.isCancelled()) {
			return Collections.emptyList();
		}
		return rv;
	}
	
	/**
	 * Remove any whitespace and extra attributes
	 */
	private static String trimValue(String value) {
		if (value == null) {
			return null;
		}
		int semi = value.indexOf(';');
		// Check for attributes
		if (semi >= 0) {
			return value.substring(0, semi).trim();
		}
		return value;
	}
	
	static List<File> findToolPlugins(File pluginsDir) {
		if (pluginsDir == null || !pluginsDir.exists()) {
			return Collections.emptyList();
		}
		List<File> tools = new ArrayList<File>();
		for(File f : pluginsDir.listFiles()) {
			if (isToolPlugin(f)) {
				tools.add(f);
			}
		}
		return tools;
	}
	
	public static boolean isToolPlugin(File f) {
		if (f.isDirectory()) {
			try {
				Attributes attrs = readManifest(f);
				List<IToolFactory> factories = instantiateToolFactories(f, attrs);
				if (!factories.isEmpty()) {
					return true;
				}
			} catch (IOException e) {
				LOG.log(Level.INFO, "Couldn't read manifest for "+f.getAbsolutePath(), e);
			}
		}
		return false;
	}
	
	// FIX Note that this only allows for one tool per plugin
	// FIX loading properties doesn't allow multi-line
	private static List<IToolFactory> instantiateToolFactories(File pluginDir, Attributes attrs) {
		if (attrs == null) {
			return Collections.emptyList();
		}
		final List<File> path = getRequiredJars(pluginDir, attrs);
		if (path.isEmpty()) {
			return Collections.emptyList();
		}
		if (hasExtraDependencies(pluginDir, attrs)) {
			return Collections.emptyList();
		}
		/*
		for(File f : path) {
			System.out.println("Required: "+f);
		}
		*/
		final ClassLoader cl = computeClassLoader(ToolUtil.class.getClassLoader(), path);		
		
		String ids = attrs.getValue(TOOL_ID);
		return getItems(ids, new AbstractItemProcessor<IToolFactory>() {
			public IToolFactory process(String id) {
				try {			
					Class<?> toolClass = cl.loadClass(id);
					Object o = toolClass.newInstance();
					if (o instanceof IToolFactory) {
						return (IToolFactory) o;
					} else {
						LOG.info("Got a non-IToolFactory: "+id);
						return cancel();
					}				
				} catch (ClassNotFoundException e) {
					LOG.log(Level.WARNING, "Couldn't load class "+id, e);
				} catch (InstantiationException e) {
					LOG.log(Level.WARNING, "Couldn't create class "+id, e);
				} catch (IllegalAccessException e) {
					LOG.log(Level.WARNING, "Couldn't access class "+id, e);
				}
				// Should only get here due to exception
				return cancel();
			}			
		});
	}
	
	public static String getPluginVersion(File pluginDir) throws IOException {
		final Attributes attrs = readManifest(pluginDir);
		final String version = attrs.getValue(PLUGIN_VERSION);
		// Find the first non-underscore/digit
		int i = 0;
		for(; i<version.length(); i++) {
			final char ch = version.charAt(i);
			if (ch != '.' && !Character.isDigit(ch)) {
				break;
			}
		}		
		return version.substring(0, i);
	}
	
	private static boolean hasExtraDependencies(File pluginDir, Attributes attrs) {
		String pluginId = trimValue(attrs.getValue(PLUGIN_ID));
		if (SierraToolConstants.TOOL_PLUGIN_ID.equals(pluginId)) {
			// OK for sierra-tool to have more, since we wrote it
			return false;
		}		
		boolean extra = false;
		for(String id : getDependencies(attrs)) {
			if (!EXPECTED_DEPS.contains(id)) {
				LOG.warning(pluginDir+" requires non-standard dependency: "+id);
				extra = true;
			}
		}
		return extra;
	}
	
	private static List<String> getDependencies(Attributes attrs) {
		final String path = attrs.getValue(DEPENDENCIES);
		return getItems(path, new AbstractItemProcessor<String>() {
			public String process(String id) {
				return trimValue(id);
			}
		});
	}
	
	public static List<File> getRequiredJars(File pluginDir) throws IOException {
		final Attributes attrs = readManifest(pluginDir);
		return getRequiredJars(pluginDir, attrs);
	}
		
	public static List<File> getRequiredJars(final File pluginDir, Attributes attrs) {
		final String path = attrs.getValue(CLASSPATH);
		return getItems(path, new AbstractItemProcessor<File>() {
			public File process(String fragment) {
				if (".".equals(fragment)) {
					File pluginBin = new File(pluginDir, "bin");
					if (pluginBin.exists() && pluginBin.isDirectory()) {
						// local binary
						return pluginBin;
					}
					// otherwise, add the directory
				}
				// plugin-relative path
				File jar = new File(pluginDir, fragment);					
				if (!jar.exists()) {
					return cancel();
				}
				return jar;
			}
		});
	}
	
	public static URLClassLoader computeClassLoader(ClassLoader cl, List<File> plugins) {
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
		return new URLClassLoader(urls, cl);
	}
	
	public static int getNumTools(Config config) {
		int count = 0;
		for(IToolFactory tf : findToolFactories()) {
			if (config.isToolIncluded(tf.getId())) {
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
