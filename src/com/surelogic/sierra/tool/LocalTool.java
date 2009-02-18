/*
 * Created on Jan 11, 2008
 */
package com.surelogic.sierra.tool;

import java.io.*;
import java.net.URI;
import java.util.*;
import java.util.logging.*;

import javax.xml.bind.*;

import org.apache.commons.lang.SystemUtils;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.*;

import com.surelogic.common.jobs.remote.RemoteSLJobException;
import com.surelogic.common.jobs.remote.TestCode;
import com.surelogic.sierra.tool.jobs.AbstractLocalSLJob;
import com.surelogic.sierra.tool.message.*;
import com.surelogic.sierra.tool.targets.*;

public class LocalTool extends AbstractTool {
	private static final JAXBContext ctx = createContext();

	private static final Marshaller marshaller = createMarshaller(ctx);

	private static JAXBContext createContext() {
		try {
			return JAXBContext.newInstance(Config.class, FileTarget.class,
					JarTarget.class, FullDirectoryTarget.class,
					FilteredDirectoryTarget.class);
		} catch (JAXBException e) {
			LOG.log(Level.SEVERE, "Couldn't create JAXB context", e);
			return null;
		}
	}

	private static Marshaller createMarshaller(JAXBContext ctx) {
		if (ctx == null) {
			LOG.severe("No JAXB context to create marshaller with");
			return null;
		}
		try {
			Marshaller marshaller = ctx.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			return marshaller;
		} catch (JAXBException e) {
			LOG.log(Level.SEVERE, "Couldn't create JAXB marshaller", e);
			return null;
		}
	}

	public LocalTool(boolean debug) {
		super("Local", "1.0", "Local",
				"Local tool for running other tools in another JVM", debug);
	}

	public Set<String> getArtifactTypes() {
		return Collections.emptySet();
	}

	@Override
	public IToolInstance create(final Config config) {
		return new LocalInstance(debug, config);
	}

	public IToolInstance create(String name, ArtifactGenerator generator) {
		throw new UnsupportedOperationException(
				"Generators can't be sent remotely");
	}

	protected IToolInstance create(String name, ArtifactGenerator generator,
			boolean close) {
		throw new UnsupportedOperationException(
				"Generators can't be sent remotely");
	}

	private class LocalInstance extends AbstractLocalSLJob implements
			IToolInstance {
		final boolean debug;
		final Config config;

		LocalInstance(boolean debug, Config c) {
			super("Local", ToolUtil.getNumTools(c), 
					       TestCode.getTestCode(c.getTestCode()), 
					       c.getMemorySize());
			this.debug = debug;
			config = c;
		}

		protected RemoteSLJobException newException(int number, Object... args) {
			throw new ToolException(number, args);
		}
		
		public void addTarget(IToolTarget target) {
			config.addTarget((ToolTarget) target);
		}

		public void addToClassPath(URI loc) {
			config.addToClassPath(loc);
		}

		public ArtifactGenerator getGenerator() {
			// TODO Auto-generated method stub
			return null;
		}

		public void reportError(String msg, Throwable t) {
			// TODO Auto-generated method stub

		}

		public void reportError(String msg) {
			// TODO Auto-generated method stub

		}

		public void setOption(String key, String option) {
			System.out.println("Currently ignoring, since already in config: "
					+ key);
		}

		private Set<String> usedPlugins = new HashSet<String>();

		private String getPluginDir(final boolean debug, final String pluginId) {
			return getPluginDir(debug, pluginId, true);
		}

		private String getPluginDir(final boolean debug, final String pluginId,
				boolean required) {
			final String pluginDir = config.getPluginDir(pluginId, required);
			if (debug) {
				System.out.println(pluginId + " = " + pluginDir);
			}
			usedPlugins.add(pluginId);
			return pluginDir;
		}

		private void addPluginToPath(final boolean debug, Project proj,
				Path path, final String pluginId) {
			addPluginToPath(debug, proj, path, pluginId, false);
		}

		private void addPluginToPath(final boolean debug, Project proj,
				Path path, final String pluginId, boolean unpacked) {
			final String pluginDir = getPluginDir(debug, pluginId);
			if (unpacked) {
				boolean workspaceExists = addToPath(proj, path, pluginDir
						+ "/bin", false); // in workspace
				if (!workspaceExists) {
					addToPath(proj, path, pluginDir); // as plugin
				}
			} else if (pluginDir.endsWith(".jar")) {
				addToPath(proj, path, pluginDir); // as plugin
			} else {
				addToPath(proj, path, pluginDir + "/bin"); // in workspace
			}
		}

		/**
		 * @return true if found
		 */
		private boolean addPluginJarsToPath(final boolean debug, Project proj,
				Path path, final String pluginId, String... jars) {
			return addPluginJarsToPath(debug, proj, path, pluginId, false, jars);
		}

		/**
		 * @param exclusive
		 *            If true, try each of the jars in sequence until one exists
		 * @return true if found
		 */
		private boolean addPluginJarsToPath(final boolean debug, Project proj,
				Path path, final String pluginId, boolean exclusive,
				String... jars) {
			boolean rv = true;
			final String pluginDir = getPluginDir(debug, pluginId);
			for (String jar : jars) {
				boolean exists = addToPath(proj, path, pluginDir + '/' + jar,
						!exclusive);
				if (exclusive && exists) {
					return true;
				}
				rv = rv && exists;
			}
			return rv;
		}

		private void addAllPluginJarsToPath(final boolean debug, Project proj,
				Path path, final String pluginId, String libPath) {
			final String pluginDir = getPluginDir(debug, pluginId);
			findJars(proj, path, pluginDir + '/' + libPath);
		}

		@SuppressWarnings("unused")
		private void setupCustomClassLoader(final boolean debug,
				CommandlineJava cmdj) {
			// String tools = getPluginDir(debug,
			// SierraToolConstants.TOOL_PLUGIN_ID);
			File commonLoading = new File(config.getToolsDirectory(),
					"common-loading.jar");
			cmdj.createVmArgument().setValue(
					"-Xbootclasspath/a:" + commonLoading.getAbsolutePath());
			cmdj
					.createVmArgument()
					.setValue(
							"-Djava.system.class.loader=com.surelogic.common.loading.CustomClassLoader");
			try {
				if (TestCode.BAD_AUX_PATH.equals(testCode)) {
					throw new IOException("Testing error with aux path");
				}
				File auxPathFile = File.createTempFile("auxPath", ".txt");
				PrintWriter pw = new PrintWriter(auxPathFile);
				cmdj.createVmArgument().setValue(
						"-D" + SierraToolConstants.AUX_PATH_PROPERTY + "="
								+ auxPathFile.getAbsolutePath());

				// FIX to support PMD's type resolution
				for (IToolTarget t : config.getTargets()) {
					if (t.getType() == IToolTarget.Type.AUX) {
						// path.add(new Path(proj, new
						// File(t.getLocation()).getAbsolutePath()));
						pw.println(t.getLocation().toURL());
					}
				}
				pw.close();
				auxPathFile.deleteOnExit();
			} catch (IOException e) {
				throw new ToolException(
						SierraToolConstants.ERROR_CREATING_AUX_PATH, e);
			}
		}

		private File setupConfigFile(CommandlineJava cmdj) {
			try {
				if (TestCode.BAD_CONFIG.equals(testCode)) {
					throw new JAXBException("Testing error with config file");
				}
				File file = File.createTempFile("config", ".xml");
				file.deleteOnExit();

				OutputStream out = new FileOutputStream(file);
				if (marshaller == null) {
					System.out.println("Couldn't create config file");
					return null;
				}
				marshaller.marshal(config, out);
				out.close();

				cmdj.createVmArgument().setValue(
						"-D" + SierraToolConstants.CONFIG_PROPERTY + "="
								+ file.getAbsolutePath());
				return file;
			} catch (IOException e) {
				throw new ToolException(
						SierraToolConstants.ERROR_CREATING_CONFIG, e);
			} catch (JAXBException e) {
				throw new ToolException(
						SierraToolConstants.ERROR_CREATING_CONFIG, e);
			}
		}
		
		@Override
		protected Class<?> getRemoteClass() {
			return RemoteTool.class;
		}
		
		@Override
		protected void finishSetupJVM(final boolean debug, final CommandlineJava cmdj) {
			setupConfigFile(cmdj);
			// setupCustomClassLoader(debug, cmdj);
			// cmdj.createBootclasspath(proj);
			
			//cmdj.createVmArgument().setValue("-Dfindbugs.debug.PluginLoader=true");
			//cmdj.createVmArgument().setValue("-Dfindbugs.verbose=true");
			//cmdj.createVmArgument().setValue("-Dfindbugs.debug=true");
			//cmdj.createVmArgument().setValue("-Dfindbugs.execplan.debug=true");			
		}
		
		@Override 
		protected void setupClassPath(boolean debug, Project proj, Path path) {					
			addPluginToPath(debug, proj, path,
					SierraToolConstants.COMMON_PLUGIN_ID);
			// sierra-tool needs special handling since it is unpacked, due to
			// Reckoner (and other tools)
			addPluginToPath(debug, proj, path,
					SierraToolConstants.TOOL_PLUGIN_ID, true);
			addPluginToPath(debug, proj, path,
					SierraToolConstants.MESSAGE_PLUGIN_ID);

			// JAXB is included in Java 6 and beyond
			if (SystemUtils.IS_JAVA_1_5) {
				addAllPluginJarsToPath(debug, proj, path,
						SierraToolConstants.JAVA5_PLUGIN_ID, "lib/jaxb");
			} else {
				// Called just to mark it as "used";
				getPluginDir(debug, SierraToolConstants.JAVA5_PLUGIN_ID, false);
			}
			addAllPluginJarsToPath(debug, proj, path,
					SierraToolConstants.PMD_PLUGIN_ID, "lib");
			addAllPluginJarsToPath(debug, proj, path,
					SierraToolConstants.FB_PLUGIN_ID, "lib");
			findJars(proj, path, new File(config.getToolsDirectory(),
					"reckoner/lib"));
			path.add(new Path(proj, new File(config.getToolsDirectory(),
					"reckoner/reckoner.jar").getAbsolutePath()));
			if (addPluginJarsToPath(debug, proj, path,
					SierraToolConstants.JUNIT4_PLUGIN_ID, true, "junit.jar",
					"junit-4.1.jar")) {
				// Called just to mark it as "used";
				getPluginDir(debug, SierraToolConstants.JUNIT_PLUGIN_ID, false);
			} else {
				addPluginJarsToPath(debug, proj, path,
						SierraToolConstants.JUNIT_PLUGIN_ID, "junit.jar");
			}
			// Add all the plugins needed by Reckoner (e.g. JDT Core and
			// company)
			for (String id : config.getPluginDirs().keySet()) {
				if (usedPlugins.contains(id)) {
					continue;
				}
				addPluginToPath(debug, proj, path, id);
			}
			if (false) {
				for(String elt : path.list()) {
					System.out.println("Path: "+elt);
				}
			}
		}

		public IToolInstance create(Config config) {
			throw new UnsupportedOperationException();
		}

		public IToolInstance create(String name, ArtifactGenerator generator) {
			throw new UnsupportedOperationException();
		}

		public Set<String> getArtifactTypes() {
			return Collections.emptySet();
		}

		public String getHtmlDescription() {
			return LocalTool.this.getHtmlDescription();
		}

		public String getTitle() {
			return LocalTool.this.getTitle();
		}

		public String getVersion() {
			return LocalTool.this.getVersion();
		}
	}

	public static void main(String[] args) {
		try {
			JAXBContext ctx = JAXBContext.newInstance(Config.class);
			Config c = new Config();
			File file = File.createTempFile("sdfsda", "xml");
			file.deleteOnExit();

			OutputStream out = new FileOutputStream(file);
			ctx.createMarshaller().marshal(c, out);
			out.close();

			Config c2 = (Config) ctx.createUnmarshaller().unmarshal(file);
			System.out.println(c.equals(c2));
		} catch (JAXBException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
