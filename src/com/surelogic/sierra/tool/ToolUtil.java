package com.surelogic.sierra.tool;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
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
import com.surelogic.common.i18n.I18N;
import com.surelogic.common.jobs.SLProgressMonitor;
import com.surelogic.common.jobs.SLStatus;
import com.surelogic.common.jobs.remote.AbstractRemoteSLJob;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.tool.analyzer.LazyZipDirArtifactGenerator;
import com.surelogic.sierra.tool.message.Config;
import com.surelogic.sierra.tool.message.MessageWarehouse;

public class ToolUtil {
	public static final String TOOLS_PATH_PROP_NAME = "sierra.tools.dir";
	public static final String CUSTOM_TOOLS_PATH_PROP_NAME = "custom.sierra.tools.dir";
	
	/**
	 * A Manifest that:
	 * 1. primarily maps artifact types to existing finding types,
	 *    (otherwise, omit to generate a new finding type)
	 * 2. optionally sets the default for the scan filter 
	 *    (otherwise, set to )
	 */
	public static final String SIERRA_MANIFEST = "sierra.mf";
	public static final String FINDING_TYPE_MAPPING_KEY = "FindingTypeMappings";
	public static final String CATEGORY_MAPPING_KEY = "CategoryMappings";
	public static final String SCAN_FILTER_BLACKLIST_KEY = "ScanFilterBlacklist";
	
	/** The logger */
	protected static final Logger LOG = SLLogger.getLogger("sierra");

	private static final List<IToolFinder> finders = new ArrayList<IToolFinder>();
	static {
		addToolFinder(new IToolFinder() {
			public List<File> findToolDirectories() {
				return findToolPlugins(new File(getSierraToolDirectory(),
						FileUtility.TOOLS_PATH_FRAGMENT));
			}
		});
	}

	public static File getSierraToolDirectory() {
		String path = System.getProperty(TOOLS_PATH_PROP_NAME);
		if (path == null)
			throw new IllegalStateException(I18N.err(165, TOOLS_PATH_PROP_NAME));

		final File toolsDir = new File(path);
		final boolean validToolsDir = toolsDir.exists()
				&& toolsDir.isDirectory();
		if (!validToolsDir)
			throw new IllegalStateException(I18N.err(166, toolsDir
					.getAbsolutePath()));
		/*
		 * Ensure that the tools subdirectory exists
		 */
		File subDir = new File(toolsDir, FileUtility.TOOLS_PATH_FRAGMENT);
		FileUtility.createDirectory(subDir);

		return toolsDir;
	}

	public static void addToolFinder(IToolFinder f) {
		synchronized (finders) {
			// First check if already added
			for (IToolFinder finder : finders) {
				if (finder == f || finder.getClass() == f.getClass()) {
					return;
				}
			}
			finders.add(f);
		}
	}

	public static Set<File> findToolDirs() {
		Set<File> dirs = new HashSet<File>();
		for (IToolFinder f : finders) {
			for (File dir : f.findToolDirectories()) {
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
		for (File dir : findToolDirs()) {
			try {
				Attributes manifest = readManifest(dir);
				for (IToolFactory f : instantiateToolFactories(dir, manifest)) {
					if (all || f.isProduction()) {
						f.init(home, dir);
						factories.add(f);
					}
				}
			} catch (IOException e) {
				LOG.log(Level.INFO, "Couldn't read manifest for "
						+ dir.getAbsolutePath(), e);
			}
		}
		return factories;
	}

	public static IToolInstance create(Config config, boolean runRemotely) {
		// runRemotely = false;
		if (runRemotely) {
			if (SierraToolConstants.RUN_TOGETHER) {
				return new LocalTool(config);
			} else {
				// Alternately, run in each in separate JVMs
				final MultiTool t = new MultiTool(config);
				for (IToolFactory f : findToolFactories()) {
					if (config.isToolIncluded(f.getId())) {
						final Config c = updateForTool(config, f);
						t.addTool(new LocalTool(c));
					}
				}
				AbstractToolFactory.setupToolForProject(t, config);
				/*
				 * if (t.size() > 0) { final File tempDir =
				 * LazyZipDirArtifactGenerator
				 * .computeTempDir(config.getScanDocument());
				 * LazyZipDirArtifactGenerator.createConfigStream(tempDir,
				 * config); }
				 */
				return t;
			}
		}
		final MultiTool t = new MultiTool(config);
		for (IToolFactory f : findToolFactories()) {
			if (config.isToolIncluded(f.getId())) {
				// System.out.println("Creating "+f.getId());
				t.addTool(f);
			}
		}
		AbstractToolFactory.setupToolForProject(t, config);
		return t;
	}

	private static Config updateForTool(Config orig, IToolFactory factory) {
		final Config copy = orig.clone();
		// Create an uncompressed result
		final File tempDir = LazyZipDirArtifactGenerator.computeTempDir(orig
				.getScanDocument());
		tempDir.mkdir();
		copy.setScanDocument(new File(tempDir, factory.getId()
				+ MessageWarehouse.TOOL_STREAM_SUFFIX));
		copy.setLogPath(new File(tempDir, factory.getId()+AbstractRemoteSLJob.LOG_SUFFIX).getAbsolutePath());
		
		// Set it to only run this one tool
		StringBuilder sb = new StringBuilder();
		List<IToolFactory> factories = findToolFactories();
		for (IToolFactory tf : factories) {
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

	/*
	public static Set<ArtifactType> getArtifactTypes() {
		Set<ArtifactType> types = new HashSet<ArtifactType>();
		for (IToolFactory f : findToolFactories()) {
			for (IToolExtension e : f.getExtensions()) {
				types.addAll(e.getArtifactTypes());
			}
		}
		return types;
	}
	*/

	public static final String MANIFEST = "META-INF" + File.separatorChar
			+ "MANIFEST.MF";
	public static final String CLASSPATH = "Bundle-ClassPath";
	public static final String TOOL_ID = "Sierra-Id";
	public static final String TOOL_FACTORIES = "Sierra-Tool";
	public static final String TOOL_WEBSITE = "Sierra-Website";
	public static final String TOOL_DESCRIPTION = "Sierra-Description";
	public static final String PLUGIN_VERSION = "Bundle-Version";
	public static final String DEPENDENCIES = "Require-Bundle";
	public static final String PLUGIN_ID = "Bundle-SymbolicName";
	public static final String PLUGIN_NAME = "Bundle-Name";

	private static final Set<String> EXPECTED_DEPS;
	static {
		Set<String> temp = new HashSet<String>(4);
		temp.add(SierraToolConstants.COMMON_PLUGIN_ID);
		temp.add(SierraToolConstants.TOOL_PLUGIN_ID);
		temp.add(SierraToolConstants.MESSAGE_PLUGIN_ID);
		EXPECTED_DEPS = Collections.unmodifiableSet(temp);
	}

	// FIX how to identify tools?
	// By ITool? How to make it consistent with a plugin.xml?
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

	static class IdentityItemProcessor extends AbstractItemProcessor<String> {
		public String process(String item) {
			return item;
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

	private static int countItems(String items) {
		return getItems(items, new IdentityItemProcessor()).size();
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
		for (File f : pluginsDir.listFiles()) {
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
				List<IToolFactory> factories = instantiateToolFactories(f,
						attrs);
				if (!factories.isEmpty()) {
					return true;
				}
			} catch (IOException e) {
				LOG.log(Level.INFO, "Couldn't read manifest for "
						+ f.getAbsolutePath(), e);
			}
		}
		return false;
	}

	@SuppressWarnings("unused")
	private static int numToolFactories(Attributes attrs) {
		return countItems(attrs.getValue(TOOL_FACTORIES));
	}

	private static List<IToolFactory> instantiateToolFactories(File pluginDir,
			Attributes attrs) {
		if (attrs == null) {
			return Collections.emptyList();
		}
		final Collection<File> path = getRequiredJars(pluginDir, attrs);
		if (path.isEmpty()) {
			return Collections.emptyList();
		}
		if (hasExtraDependencies(pluginDir, attrs)) {
			return Collections.emptyList();
		}
		/*
		 * for(File f : path) { System.out.println("Required: "+f); }
		 */
		final ClassLoader cl = computeClassLoader(ToolUtil.class
				.getClassLoader(), path);

		String ids = attrs.getValue(TOOL_FACTORIES);
		return getItems(ids, new AbstractItemProcessor<IToolFactory>() {
			public IToolFactory process(String id) {
				try {
					Class<?> toolClass = cl.loadClass(id);
					Object o = toolClass.newInstance();
					if (o instanceof IToolFactory) {
						return (IToolFactory) o;
					} else {
						LOG.info("Got a non-IToolFactory: " + id);
						return cancel();
					}
				} catch (ClassNotFoundException e) {
					LOG.log(Level.WARNING, "Couldn't load class " + id, e);
				} catch (InstantiationException e) {
					LOG.log(Level.WARNING, "Couldn't create class " + id, e);
				} catch (IllegalAccessException e) {
					LOG.log(Level.WARNING, "Couldn't access class " + id, e);
				}
				// Should only get here due to exception
				return cancel();
			}
		});
	}

	/**
	 * Tries to get the metadata for a given tool factory from: 1. A
	 * factory-specific properties file, e.g. tool/Factory.properties 2. The
	 * bundle manifest
	 * 
	 * @return null if no id, or there's more than one tool factory
	 */
	public static ToolInfo getToolInfo(File pluginDir, String factoryName)
			throws IOException {
		final Attributes mainAttrs = readManifest(pluginDir);
		Attributes attrs = mainAttrs;
		// First, try the specific props file
		final Collection<File> path = getRequiredJars(pluginDir, mainAttrs);
		if (!path.isEmpty()) {
			final ClassLoader cl = computeClassLoader(ToolUtil.class
					.getClassLoader(), path);
			String resPath = factoryName.replace('.', '/') + ".manifest";
			InputStream stream = cl.getResourceAsStream(resPath);
			if (stream != null) {
				attrs = new Manifest(stream).getMainAttributes();
			}
		}
		// Otherwise, use the bundle manifest
		String id = attrs.getValue(TOOL_ID);
		if (id == null) {
			return null;
		}
		String version = getPluginVersion(attrs);
		String name = attrs.getValue(PLUGIN_NAME);
		String website = attrs.getValue(TOOL_WEBSITE);
		String description = attrs.getValue(TOOL_DESCRIPTION);
		return new ToolInfo(id, version, name, website, description);
	}

	private static String getPluginVersion(Attributes attrs) {
		final String version = attrs.getValue(PLUGIN_VERSION);
		int numDots = 0; // Only keep first 3 segments
		
		// Find the first non-underscore/digit
		int i = 0;
		for (; i < version.length(); i++) {
			final char ch = version.charAt(i);
			if (ch == '.') {
				numDots++;
				if (numDots >= 3) {
					break; // last dot omitted below by substring()
				}
			}
			// Not a dot or a digit
			else if (ch != '.' && !Character.isDigit(ch)) {
				// Eliminate trailing dots
				while (i > 0 && version.charAt(i - 1) == '.') {
					i--;
				}
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
		for (String id : getDependencies(attrs)) {
			if (!EXPECTED_DEPS.contains(id)) {
				LOG.warning(pluginDir + " requires non-standard dependency: "
						+ id);
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

	public static Collection<File> getRequiredJars(File pluginDir) throws IOException {
		final Attributes attrs = readManifest(pluginDir);
		return getRequiredJars(pluginDir, attrs);
	}

	public static Collection<File> getRequiredJars(final File pluginDir,
			Attributes attrs) {
		final String path = attrs.getValue(CLASSPATH);
		final String pluginId = trimValue(attrs.getValue(PLUGIN_ID));
		return getItems(path, new AbstractItemProcessor<File>() {
			public File process(String fragment) {
				if (".".equals(fragment)) {
					File pluginBin = new File(pluginDir, "bin");
					if (pluginBin.exists() && pluginBin.isDirectory()) {
						// local binary
						return pluginBin;
					}
					// look for a plugin jar
					File pluginJar = new File(pluginDir, pluginId+".jar");
					if (pluginJar.exists() && pluginJar.isFile()) {						
						return pluginJar;
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

	public static URLClassLoader computeClassLoader(ClassLoader cl,
			Collection<File> plugins) {
		URL[] urls = new URL[plugins.size()];
		int i = 0;
		for (File jar : plugins) {
			try {
				urls[i] = jar.toURI().toURL();
			} catch (MalformedURLException e) {
				LOG.log(Level.WARNING, "Problem converting "
						+ jar.getAbsolutePath() + " to URL", e);
			}
			i++;
		}
		return new URLClassLoader(urls, cl);
	}

	public static int getNumTools(Config config) {
		int count = 0;
		for (IToolFactory tf : findToolFactories()) {
			if (config.isToolIncluded(tf.getId())) {
				count++;
			}
		}
		return count;
	}

	public static SLStatus scan(Config config, SLProgressMonitor mon,
			boolean runRemotely) {
		final boolean fineIsLoggable = LOG.isLoggable(Level.FINE);
		final IToolInstance ti = ToolUtil.create(config, runRemotely);

		if (fineIsLoggable) {
			LOG.fine("Excluded: " + config.getExcludedToolsList());
			LOG.fine("Java version: " + config.getJavaVersion());
			LOG.fine("Rules file: " + config.getPmdRulesFile());
		}
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
