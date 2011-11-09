package com.surelogic.sierra.tool.findbugs;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.jar.*;
import java.util.logging.Level;
import java.util.zip.*;

import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.tool.*;
import com.surelogic.sierra.tool.analyzer.ILazyArtifactGenerator;
import com.surelogic.sierra.tool.message.Config;

import edu.umd.cs.findbugs.BugPattern;
import edu.umd.cs.findbugs.DetectorFactoryCollection;
import edu.umd.cs.findbugs.Plugin;

public class FindBugsToolFactory extends AbstractToolFactory {	
	private static final String CORE = "edu.umd.cs.findbugs.plugins.core";
	
//	@Override
//	public String getVersion() {
//		return "1.3.7"/*Version.RELEASE_BASE*/;
//	}
	
	@Override
	public void init(File toolHome, File pluginDir) {
		super.init(toolHome, pluginDir);
		AbstractFindBugsTool.init(toolHome.getAbsolutePath());
	}

	static <T> Iterable<T> iterable(final Iterator<T> it) {
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
			final Manifest manifest = findSierraManifest(plugin);
			Set<ArtifactType> types = new HashSet<ArtifactType>();
			/*
			for(BugCode code : iterable(plugin.bugCodeIterator())) {				
			}
			*/
			for(BugPattern pattern : iterable(plugin.bugPatternIterator())) {
				ArtifactType t = ArtifactType.create(this, manifest, pluginId, 
		                                          pattern.getType(), pattern.getCategory());
				types.add(t);
			}
			/*
			for(DetectorFactory factory : iterable(plugin.detectorFactoryIterator())) {
				// Actual detector
			}
			*/	
			File loc = null;
			try {
				URL url = plugin.getPluginLoader().getURL();
				if (url != null) {
					loc = new File(url.toURI());
				}
			} catch (URISyntaxException e) {
				e.printStackTrace(); // FIX ignored
				continue;
			}
			final boolean isCore = CORE.equals(plugin.getPluginId());
			extensions.add(new AbstractToolExtension(getId(), plugin.getPluginId(), loc, types) {
				public boolean isCore() {
					return isCore;
				};
			});
			
		}
		return extensions;
	}

	private Manifest findSierraManifest(Plugin plugin) {
		URL url = plugin.getPluginLoader().getURL();
		if (url == null) {
			return null; // Core plugin
		}		
		try {
			ZipInputStream zis = new ZipInputStream(url.openStream());
			ZipEntry ze;
			while ((ze = zis.getNextEntry()) != null) {
				if (ToolUtil.SIERRA_MANIFEST.equals(ze.getName())) {
					break;
				}
			}
			if (ze == null) {
				return null;
			}			
			/*
			InputStreamReader r = new InputStreamReader(zis);
			int c;
			while ((c = r.read()) != -1) {
				System.out.print((char) c);
			}			
			*/
			Manifest props = new Manifest();
			props.read(zis);
			zis.close();
			return props;
		} catch(IOException e) {
			SLLogger.getLogger().log(Level.WARNING, "Couldn't load finding type mapping for "+plugin.getPluginId(), e);
			return null;		
		}
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
