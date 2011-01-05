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

import com.surelogic.common.jobs.remote.*;
import com.surelogic.sierra.tool.analyzer.ILazyArtifactGenerator;
import com.surelogic.sierra.tool.message.*;
import com.surelogic.sierra.tool.targets.*;

final class LocalTool extends AbstractLocalSLJob implements IToolInstance {
	private static final JAXBContext ctx = createContext();

	private static final Marshaller marshaller = createMarshaller(ctx);

	private static final IToolFactory factory = 
		new DummyToolFactory("Local", "1.0", "Local",
		"Local tool for running other tools in another JVM");

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

	private final Config config;
	
	public LocalTool(Config config) {
		super("Sierra tool", ToolUtil.getNumTools(config), 
				TestCode.getTestCode(config.getTestCode()), 
				config.getMemorySize(), /*true ||*/ config.isVerbose());
		this.config = config;
	}

	protected RemoteSLJobException newException(int number, Object... args) {
		throw new ToolException(number, args);
	}

	public void addTarget(IToolTarget target) {
		if (verbose) {
			System.out.println("Currently ignoring, since already in config: "+ target.getLocation());
		}
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
		if (verbose) {
			System.out.println("Currently ignoring, since already in config: "+ key);
		}
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
		cmdj.createVmArgument().setValue("-D"+ToolUtil.TOOLS_PATH_PROP_NAME+"="+
				                         ToolUtil.getSierraToolDirectory());
	}

	@Override 
	protected void setupClassPath(boolean debug, Project proj, Path path) {			
		final Set<File> jars = new HashSet<File>();
		final ConfigUtil util = new ConfigUtil(config);
		util.addPluginToPath(debug, jars, SierraToolConstants.COMMON_PLUGIN_ID);

		// sierra-tool needs special handling since it is unpacked, due to
		// Reckoner (and other tools)
		util.addPluginToPath(debug, jars, SierraToolConstants.TOOL_PLUGIN_ID, true);
		util.addPluginToPath(debug, jars, SierraToolConstants.MESSAGE_PLUGIN_ID);

		// JAXB is included in Java 6 and beyond
		if (SystemUtils.IS_JAVA_1_5) {
			util.addAllPluginJarsToPath(debug, jars, SierraToolConstants.JAVA5_PLUGIN_ID, "lib/jaxb");
		} else {
			// FIX
			// Called just to mark it as "used";
			util.getPluginDir(debug, SierraToolConstants.JAVA5_PLUGIN_ID, false);
		}

		// FIX which tool needs this?
		if (util.addPluginJarsToPath(debug, jars,
				SierraToolConstants.JUNIT4_PLUGIN_ID, true, "junit.jar",
		"junit-4.1.jar")) {
			// Called just to mark it as "used";
			util.getPluginDir(debug, SierraToolConstants.JUNIT_PLUGIN_ID, false);
		} else {
			util.addPluginJarsToPath(debug, jars,
					SierraToolConstants.JUNIT_PLUGIN_ID, "junit.jar");
		}			
		addToolPluginJars(debug, jars);

		for(File jar : jars) {
			addToPath(proj, path, jar, true);
		}			

		if (false) {
			for(String elt : path.list()) {
				System.out.println("Path: "+elt);
			}
		}
	}

	private void addToolPluginJars(boolean debug, Set<File> path) {
		// TODO need to deactivate some?
		for(IToolFactory f : ToolUtil.findToolFactories()) {
			for(File jar : f.getRequiredJars(config)) {
				path.add(jar);		
			}
		}
	}

	public IToolInstance create() {
		throw new UnsupportedOperationException();
	}

	public IToolInstance create(String name, ILazyArtifactGenerator generator) {
		throw new UnsupportedOperationException();
	}

	public Set<ArtifactType> getArtifactTypes() {
		return Collections.emptySet();
	}

	public List<File> getRequiredJars() {
		return Collections.emptyList();
	}

	public String getHTMLInfo() {
		return factory.getHTMLInfo();
	}

	public String getId() {
		return factory.getId();
	}

	public String getVersion() {
		return factory.getVersion();
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
