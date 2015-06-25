package com.surelogic.sierra.tool;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.logging.Level;

import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.tool.analyzer.ILazyArtifactGenerator;
import com.surelogic.sierra.tool.analyzer.LazyZipArtifactGenerator;
import com.surelogic.sierra.tool.analyzer.LazyZipDirArtifactGenerator;
import com.surelogic.sierra.tool.analyzer.MessageArtifactFileGenerator;
import com.surelogic.sierra.tool.message.Config;
import com.surelogic.sierra.tool.targets.ToolTarget;

/**
 * An abstract class designed to simplify implementation of IToolFactory
 * 
 * Tries to load the tool metadata from the tool manifest file
 * 
 * @author edwin
 */
public abstract class AbstractToolFactory implements IToolFactory {
	
	private File pluginDir;
	private ToolInfo info;
	private Throwable initError;
	
	public boolean isProduction() {
		return isActive();
	}

	public void deactivate(Throwable e) {
		initError = e;
	}
	
	protected boolean isActive() {
		return initError == null;
	}
	
	public void init(File toolHome, File pluginDir) {
		this.pluginDir = pluginDir;
		try {
			info = ToolUtil.getToolInfo(pluginDir, this.getClass().getName());
			if (info == null) {
				info = new ToolInfo();
			}
		} catch (IOException e) {
			SLLogger.getLogger().log(Level.SEVERE, "Couldn't get tool metadata", e);
		}
	}
	
	public final File getPluginDir() {
		return pluginDir;
	}
	
	public String getId() {
		return info.id;
	}
	
	public String getVersion() {
		return info.version;
	}
	
	public String getName() {
		return info.name;
	}
	
	public String getHTMLInfo() {
		return info.description;
	}
	
	@Override
	public int hashCode() {
		return this.getClass().getName().hashCode();
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof AbstractToolFactory) {
			return this.getClass().getName().equals(o.getClass().getName());
		}
		return false;
	}
	
	/**
	 * Initializes the IToolInstance from the Config
	 */
	public static void setupToolForProject(IToolInstance ti, Config config) {
		for(ToolTarget t : config.getTargets()) {
			ti.addTarget(t);
		}
		for(URI path : config.getPaths()) {
			ti.addToClassPath(path);
		}
		ti.setOption(IToolInstance.COMPLIANCE_LEVEL, config.getComplianceLevel());
		ti.setOption(IToolInstance.SOURCE_LEVEL, config.getSourceLevel());
		ti.setOption(IToolInstance.TARGET_LEVEL, config.getTargetLevel());
	}
	
	public final IToolInstance create(Config config) {
		IToolInstance ti =  create(config, createGenerator(config), true);
		setupToolForProject(ti, config);
		return ti;
	}

	/**
	 * Create an ILazyArtifactGenerator appropriate to the Config
	 */
	public static ILazyArtifactGenerator createGenerator(Config config) {
		final File doc = config.getScanDocument();	
		if (doc.getName().endsWith(SierraToolConstants.PARSED_ZIP_FILE_SUFFIX)) {
			if (SierraToolConstants.CREATE_ZIP_DIRECTLY) {
				return new LazyZipArtifactGenerator(config);
			} else {
				return new LazyZipDirArtifactGenerator(config);  
			}
		} else {
			final boolean compress = doc.getName().endsWith(SierraToolConstants.PARSED_FILE_SUFFIX);
			return new MessageArtifactFileGenerator(doc, config, compress);
		}
	}
	
	public final IToolInstance create(Config config, ILazyArtifactGenerator generator) {
		return create(config, generator, false);
	}

	protected abstract IToolInstance create(Config config, ILazyArtifactGenerator generator, boolean close);
	
	public Collection<File> getRequiredJars(Config config) {
		try {
			return ToolUtil.getRequiredJars(getPluginDir());
		} catch (IOException e) {
			SLLogger.getLogger().log(Level.SEVERE, "Couldn't get required jars for "+getName(), e);
			return Collections.emptyList();
		}
	}
}
