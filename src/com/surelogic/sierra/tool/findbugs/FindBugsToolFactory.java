package com.surelogic.sierra.tool.findbugs;

import java.io.File;
import java.util.*;

import com.surelogic.sierra.tool.*;
import com.surelogic.sierra.tool.analyzer.ILazyArtifactGenerator;
import com.surelogic.sierra.tool.message.Config;

import edu.umd.cs.findbugs.BugPattern;
import edu.umd.cs.findbugs.DetectorFactoryCollection;
import edu.umd.cs.findbugs.Plugin;

public class FindBugsToolFactory extends AbstractToolFactory {	
//	@Override
//	public String getVersion() {
//		return "1.3.7"/*Version.RELEASE_BASE*/;
//	}
	
	@Override
	public void init(File toolHome, File pluginDir) {
		super.init(toolHome, pluginDir);
		AbstractFindBugsTool.init(toolHome.getAbsolutePath());
	}

	private static <T> Iterable<T> iterable(final Iterator<T> it) {
		return new Iterable<T>() {
			public Iterator<T> iterator() {
				return it;
			}			
		};
	}
	
//	@Override
	public final Collection<IToolExtension> getExtensions() {
		List<IToolExtension> extensions = new ArrayList<IToolExtension>();

		// Code to get meta-data from FindBugs
		for(Plugin plugin : iterable(DetectorFactoryCollection.instance().pluginIterator())) {
			final String pluginId = plugin.getPluginId();				
			Set<ArtifactType> types = new HashSet<ArtifactType>();
			/*
			for(BugCode code : iterable(plugin.bugCodeIterator())) {				
			}
			*/
			for(BugPattern pattern : iterable(plugin.bugPatternIterator())) {				
				types.add(new ArtifactType(getId(), getVersion(), pluginId, 
						                   pattern.getType(), pattern.getCategory()));
			}
			/*
			for(DetectorFactory factory : iterable(plugin.detectorFactoryIterator())) {
				// Actual detector
			}
			*/						
			extensions.add(new AbstractToolExtension(plugin.getPluginId(), types) {});
		}
		return extensions;
	}

	/*
	@Override
	public List<File> getRequiredJars() {
		final List<File> jars = new ArrayList<File>();	
		addAllPluginJarsToPath(debug, jars, SierraToolConstants.FB_PLUGIN_ID, "lib");
		return jars;
	}
	*/
	
	@Override
	protected IToolInstance create(Config config,
			ILazyArtifactGenerator generator, boolean close) {
		return new AbstractFindBugsTool(this, config, generator, close);
	}
}
