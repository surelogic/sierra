package com.surelogic.sierra.client.eclipse;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Logger;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;

import com.surelogic.common.jdbc.ConnectionQuery;
import com.surelogic.common.jdbc.Query;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.jdbc.settings.Categories;
import com.surelogic.sierra.jdbc.settings.CategoryDO;
import com.surelogic.sierra.jdbc.tool.ArtifactTypeDO;
import com.surelogic.sierra.jdbc.tool.FindingTypeDO;
import com.surelogic.sierra.jdbc.tool.FindingTypes;
import com.surelogic.sierra.tool.*;

public final class Tools {
	private static final Logger LOG = SLLogger.getLogger();
	
	public static final String TOOL_PLUGIN_ID = "com.surelogic.sierra.tool";
	
	/**
	 * The Sierra tool module extension point identifier <i>must</i> 
	 * match the plugin manifest.
	 */
	public static final String TOOL_EXTENSION_POINT_ID = "sierraTool";
	
	static {
		ToolUtil.addToolFinder(new IToolFinder() {
			// FIX what about duplicate finders?
			public List<File> findToolDirectories() {
				// Look up locations of these plugins
				List<File> tools = new ArrayList<File>();
				for(String id : getToolPluginIds()) {
					String path = 
						com.surelogic.common.eclipse.Activator.getDefault().getDirectoryOf(id);
					File dir = new File(path);

					// Sanity check for these directories
					if (dir.exists() && dir.isDirectory()) {						
						tools.add(dir);
					}
				}
				return tools;
			}			
		});
	}
	
	/**
	 * Collect plugins
	 */
	public static Set<String> getToolPluginIds() {
		Set<String> ids = new HashSet<String>();
		/*
		for(String id : defaultTools) {
			ids.add(id);
		}
		*/
		addProductionToolDirectories(ids);
		return ids;
	}
	
	
	
	public static void checkForNewArtifactTypes() {
		for(File plugin : ToolUtil.findToolDirs()) {
			LOG.fine("Found plugin @ "+plugin.getPath());
		}	
		final List<IToolFactory> factories = ToolUtil.findToolFactories();
		for(IToolFactory f : factories) {
			try {
				LOG.fine("Found tool: "+f.getName()+" v"+f.getVersion());
				LOG.fine(f.getHTMLInfo());
			} catch(NullPointerException npe) {
				LOG.fine("Ignored tool: "+f.getClass().getName());
			}
		}
		
		try {
			// Get known artifact types
			final Connection c = Data.getInstance().readOnlyConnection();
			final Query q = new ConnectionQuery(c);
			final FindingTypes ft = new FindingTypes(q);
			/*
			final Config config = new Config();					
			config.putPluginDir(SierraToolConstants.FB_PLUGIN_ID, 
					            com.surelogic.common.eclipse.Activator.getDefault().getDirectoryOf(SierraToolConstants.FB_PLUGIN_ID));
			*/
			
			final Set<ArtifactType> knownTypes = new HashSet<ArtifactType>();
			for(IToolFactory t : factories) {
				List<ArtifactTypeDO> temp = ft.getToolArtifactTypes(t.getId(), t.getVersion());
				for(ArtifactTypeDO a : temp) {
					knownTypes.add(new ArtifactType(a.getTool(), a.getVersion(), "", a.getMnemonic(), ""));
				}				                          
			}
			final List<ArtifactType> unknown = new ArrayList<ArtifactType>();
			for(IToolFactory t : factories) {
				for(ArtifactType a : t.getArtifactTypes()) {
					if (!knownTypes.contains(a)) {
						unknown.add(a);
					}
				}
			}

			if (unknown.isEmpty()) {
				LOG.fine("No new artifact types");
			} else {
				Collections.sort(unknown);
				for(ArtifactType a : unknown) {
					//SLLogger.getLogger().warning("Couldn't find "+a.type+" for "+a.tool+", v"+a.version);					
					LOG.fine("Couldn't find "+a.type+" for "+a.tool+", v"+a.version);	
				}				
				// Find/define finding types
				List<FindingTypeDO> ftypes = ft.listFindingTypes();
				for(FindingTypeDO f : ftypes) {
					// Search?
					f.getName();
				}
				
				// Find/create categories -- can be modified later				
				final Categories categories = new Categories(q);
				List<CategoryDO> cats = categories.listCategories();
				for(CategoryDO cdo : cats) {
					cdo.getName();
				}
			}
			// FIX Show dialog
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	// //////////////////////////////////////////////////////////////////////
	//
	// TOOL EXTENSION POINT METHODS
	//
	// //////////////////////////////////////////////////////////////////////
	
	/**
	 * @return all defined tool extension points defined in the plugin manifests.
	 */
	private static IExtension[] readToolExtensionPoints() {
		IExtensionRegistry pluginRegistry = Platform.getExtensionRegistry();
		IExtensionPoint extensionPoint = pluginRegistry.getExtensionPoint(
				TOOL_PLUGIN_ID,
				TOOL_EXTENSION_POINT_ID);
		return extensionPoint.getExtensions();
	}
	
	/**
	 * Builds an array of all tool extension points that are marked in
	 * the XML as being production (i.e., production="true").
	 */
	private static List<IExtension> findProductionToolExtensionPoints() {
		final IExtension[] tools = readToolExtensionPoints();
		List<IExtension> active = new ArrayList<IExtension>();
		for (IExtension tool : tools) {		
			//final String uid = tool.getUniqueIdentifier();		
			boolean add = true;
			IConfigurationElement[] configElements = tool.getConfigurationElements();
			for (int j = 0; j < configElements.length; j++) {
				String production = configElements[j].getAttribute("production");
				if (production != null && production.equals("false")) {
					/*
					if (LOG.isLoggable(Level.FINE))
						LOG.fine("analysis module extension point " + uid
								+ " is not production and is being excluded");
					*/
					// add to list of non-production
					add = false;
					break;
				}
			}
			if (add) {
				active.add(tool);
			}
		}
		return active;
	}
	
	private static void addProductionToolDirectories(Collection<String> ids) {
		for(IExtension tool : findProductionToolExtensionPoints()) {
			ids.add(tool.getContributor().getName());
		}
	}

	public static List<IToolFactory> findToolFactories() {
		return ToolUtil.findToolFactories();
	}
}
