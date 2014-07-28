package com.surelogic.sierra.tool;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
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
import com.surelogic.common.jobs.remote.AbstractLocalSLJob;
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
		return findToolDirs(null);
	}
	
	public static Set<File> findToolDirs(PrintStream out) {
		Set<File> dirs = new HashSet<File>();
		for (IToolFinder f : finders) {
			if (out != null) {
				out.println("Using finder: "+f.toString());
			}
			for (File dir : f.findToolDirectories()) {
				if (!dirs.contains(dir)) {
					if (out != null) {
						out.println("\tFound tool dir: "+dir);
					}
					dirs.add(dir);
				}
			}
		}
		return dirs;
	}

	public static List<IToolFactory> findToolFactories() {
		return findToolFactories(null);
	}
	
	public static List<IToolFactory> findToolFactories(PrintStream out) {
		return findToolFactories(out, XUtil.useExperimental);
	}

	public static List<IToolFactory> findToolFactories(PrintStream out, boolean all) {
		if (out != null) {
			out.println("Finding tool factories ...");
		}
		final File home = getSierraToolDirectory();
		List<IToolFactory> factories = new ArrayList<IToolFactory>();
		for (File dir : findToolDirs(out)) {
			try {
				Attributes manifest = readManifest(dir);
				for (IToolFactory f : instantiateToolFactories(dir, manifest)) {
					if (out != null) {
						out.println("Considering factory "+f);
					}
					if (all || f.isProduction()) {
						try { 
							f.init(home, dir);
						} catch(Throwable e) {
							f.deactivate(e);
						}
						factories.add(f);
						if (out != null) {
							out.println("Added factory "+f.getName());
						}
					}
				}
			} catch (IOException e) {
				LOG.log(Level.INFO, "Couldn't read manifest for "
						+ dir.getAbsolutePath(), e);
			}
		}
		return factories;
	}

	public static IToolInstance create(PrintStream out, Config config, boolean runRemotely) {
		// runRemotely = false;
		if (runRemotely) {
			if (SierraToolConstants.RUN_TOGETHER) {
				return new LocalTool(config);
			} else {
				// Alternately, run in each in separate JVMs
				out.println("Finding tools ...");
				final MultiTool t = new MultiTool(config);
				for (IToolFactory f : findToolFactories(out)) {
					out.println("Checking for "+f.getId());
					if (config.isToolIncluded(f.getId())) {
						out.println("Adding tool: "+f.getId());
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
		for (IToolFactory f : findToolFactories(out)) {
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
		temp.add(AbstractLocalSLJob.COMMON_PLUGIN_ID);
		temp.add(SierraToolConstants.TOOL_PLUGIN_ID);
		temp.add(SierraToolConstants.MESSAGE_PLUGIN_ID);
		temp.add(SierraToolConstants.JDT_CORE_PLUGIN_ID);
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
		final boolean isSierraTool = SierraToolConstants.TOOL_PLUGIN_ID.equals(pluginId);
		return getItems(path, new AbstractItemProcessor<File>() {
			public File process(String fragment) {
				if (".".equals(fragment)) {
					if (isSierraTool) {
						return null; // To avoid duplicating tool classes 
					}
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

	public static ClassLoader computeClassLoader(ClassLoader cl,
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
		//return new URLClassLoader(urls, cl);
		//return new ParentLastURLClassLoader(urls, cl);
		return new ChildFirstURLClassLoader(urls, cl);
	}

    // From http://stackoverflow.com/questions/5445511/how-do-i-create-a-parent-last-child-first-classloader-in-java-or-how-to-overr
	public static class ChildFirstURLClassLoader extends URLClassLoader {

	    private ClassLoader system;

	    public ChildFirstURLClassLoader(URL[] classpath, ClassLoader parent) {
	        super(classpath, parent);
	        system = getSystemClassLoader();
	    }

	    @Override
	    protected synchronized Class<?> loadClass(String name, boolean resolve)
	            throws ClassNotFoundException {
	        // First, check if the class has already been loaded
	        Class<?> c = findLoadedClass(name);
	        if (c == null) {
	            if (system != null) {
	                try {
	                    // checking system: jvm classes, endorsed, cmd classpath, etc.
	                    c = system.loadClass(name);
	                }
	                catch (ClassNotFoundException ignored) {
	                }
	            }
	            if (c == null) {
	                try {
	                    // checking local
	                    c = findClass(name);
	                } catch (ClassNotFoundException e) {
	                    // checking parent
	                    // This call to loadClass may eventually call findClass again, in case the parent doesn't find anything.
	                    c = super.loadClass(name, resolve);
	                }
	            }
	        }
	        if (resolve) {
	            resolveClass(c);
	        }
	        return c;
	    }

	    @Override
	    public URL getResource(String name) {
	        URL url = null;
	        if (system != null) {
	            url = system.getResource(name); 
	        }
	        if (url == null) {
	            url = findResource(name);
	            if (url == null) {
	                // This call to getResource may eventually call findResource again, in case the parent doesn't find anything.
	                url = super.getResource(name);
	            }
	        }
	        return url;
	    }

	    @Override
	    public Enumeration<URL> getResources(String name) throws IOException {
	        /**
	        * Similar to super, but local resources are enumerated before parent resources
	        */
	        Enumeration<URL> systemUrls = null;
	        if (system != null) {
	            systemUrls = system.getResources(name);
	        }
	        Enumeration<URL> localUrls = findResources(name);
	        Enumeration<URL> parentUrls = null;
	        if (getParent() != null) {
	            parentUrls = getParent().getResources(name);
	        }
	        final List<URL> urls = new ArrayList<URL>();
	        if (systemUrls != null) {
	            while(systemUrls.hasMoreElements()) {
	                urls.add(systemUrls.nextElement());
	            }
	        }
	        if (localUrls != null) {
	            while (localUrls.hasMoreElements()) {
	                urls.add(localUrls.nextElement());
	            }
	        }
	        if (parentUrls != null) {
	            while (parentUrls.hasMoreElements()) {
	                urls.add(parentUrls.nextElement());
	            }
	        }
	        return new Enumeration<URL>() {
	            Iterator<URL> iter = urls.iterator();

	            public boolean hasMoreElements() {
	                return iter.hasNext(); 
	            }
	            public URL nextElement() {
	                return iter.next();
	            }
	        };
	    }

	    @Override
	    public InputStream getResourceAsStream(String name) {
	        URL url = getResource(name);
	        try {
	            return url != null ? url.openStream() : null;
	        } catch (IOException e) {
	        }
	        return null;
	    }
	}
	
	/**
	 * A parent-last classloader that will try the child classloader first and then the parent.
	 * This takes a fair bit of doing because java really prefers parent-first.
	 * 
	 * For those not familiar with class loading trickery, be wary
	 */
	@SuppressWarnings("unused")
	private static class ParentLastURLClassLoader extends ClassLoader 
	{
	    private ChildURLClassLoader childClassLoader;

	    /**
	     * This class allows me to call findClass on a classloader
	     */
	    private static class FindClassClassLoader extends ClassLoader
	    {
	        public FindClassClassLoader(ClassLoader parent)
	        {
	            super(parent);
	        }

	        @Override
	        public Class<?> findClass(String name) throws ClassNotFoundException
	        {
	            return super.findClass(name);
	        }
	    }

	    /**
	     * This class delegates (child then parent) for the findClass method for a URLClassLoader.
	     * We need this because findClass is protected in URLClassLoader
	     */
	    private static class ChildURLClassLoader extends URLClassLoader
	    {
	        private FindClassClassLoader realParent;

	        public ChildURLClassLoader( URL[] urls, FindClassClassLoader realParent )
	        {
	            super(urls, null);

	            this.realParent = realParent;
	        }

	        @Override
	        public Class<?> findClass(String name) throws ClassNotFoundException
	        {
	            try
	            {
	                // first try to use the URLClassLoader findClass
	                return super.findClass(name);
	            }
	            catch( ClassNotFoundException e )
	            {
	                // if that fails, we ask our real parent classloader to load the class (we give up)
	                return realParent.loadClass(name);
	            }
	        }
	    }

	    public ParentLastURLClassLoader(URL[] urls, ClassLoader parent)
	    {
	    	super(parent);
	    	//super(Thread.currentThread().getContextClassLoader());

	    	//URL[] urls = classpath.toArray(new URL[classpath.size()]);

	        childClassLoader = new ChildURLClassLoader( urls, new FindClassClassLoader(this.getParent()) );
	    }

	    @Override
	    protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException
	    {
	        try
	        {
	            // first we try to find a class inside the child classloader
	            return childClassLoader.findClass(name);
	        }
	        catch( ClassNotFoundException e )
	        {
	            // didn't find it, try the parent
	            return super.loadClass(name, resolve);
	        }
	    }
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

	public static SLStatus scan(PrintStream out, Config config, SLProgressMonitor mon,
			boolean runRemotely) {
		final boolean fineIsLoggable = LOG.isLoggable(Level.FINE);
		final IToolInstance ti = ToolUtil.create(out, config, runRemotely);

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
