package com.surelogic.sierra.client.eclipse;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.window.Window;

import com.surelogic.common.eclipse.jobs.SLUIJob;
import com.surelogic.common.jdbc.NullDBQuery;
import com.surelogic.common.jdbc.Query;
import com.surelogic.common.jdbc.TransactionException;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.client.eclipse.dialogs.ArtifactTypeMappingDialog;
import com.surelogic.sierra.jdbc.settings.Categories;
import com.surelogic.sierra.jdbc.settings.CategoryDO;
import com.surelogic.sierra.jdbc.tool.ArtifactTypeDO;
import com.surelogic.sierra.jdbc.tool.ExtensionDO;
import com.surelogic.sierra.jdbc.tool.FindingTypeDO;
import com.surelogic.sierra.jdbc.tool.FindingTypes;
import com.surelogic.sierra.tool.ArtifactType;
import com.surelogic.sierra.tool.IToolExtension;
import com.surelogic.sierra.tool.IToolFactory;
import com.surelogic.sierra.tool.IToolFinder;
import com.surelogic.sierra.tool.ToolUtil;

public final class Tools {
	private static final Logger LOG = SLLogger.getLogger();

	public static final String TOOL_PLUGIN_ID = "com.surelogic.sierra.tool";

	/**
	 * The Sierra tool module extension point identifier <i>must</i> match the
	 * plugin manifest.
	 */
	public static final String TOOL_EXTENSION_POINT_ID = "sierraTool";

	static {
		ToolUtil.addToolFinder(new IToolFinder() {
			// FIX what about duplicate finders?
			public List<File> findToolDirectories() {
				// Look up locations of these plugins
				final List<File> tools = new ArrayList<File>();
				for (final String id : getToolPluginIds()) {
					final String path = com.surelogic.common.eclipse.Activator
							.getDefault().getDirectoryOf(id);
					final File dir = new File(path);

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
		final Set<String> ids = new HashSet<String>();
		/*
		 * for(String id : defaultTools) { ids.add(id); }
		 */
		addProductionToolDirectories(ids);
		return ids;
	}

	public static void checkForNewArtifactTypes() {
		for (final File plugin : ToolUtil.findToolDirs()) {
			LOG.fine("Found plugin @ " + plugin.getPath());
		}
		final List<IToolFactory> factories = ToolUtil.findToolFactories();
		for (final IToolFactory f : factories) {
			try {
				LOG.fine("Found tool: " + f.getName() + " v" + f.getVersion());
				LOG.fine(f.getHTMLInfo());
			} catch (final NullPointerException npe) {
				LOG.fine("Ignored tool: " + f.getClass().getName());
			}
		}

		try {
			// Get known artifact types
			Data.getInstance().withTransaction(new NullDBQuery() {

				@Override
				public void doPerform(final Query q) {
					final FindingTypes ft = new FindingTypes(q);
					/*
					 * final Config config = new Config();
					 * config.putPluginDir(SierraToolConstants.FB_PLUGIN_ID,
					 * com.surelogic
					 * .common.eclipse.Activator.getDefault().getDirectoryOf
					 * (SierraToolConstants.FB_PLUGIN_ID));
					 */

					final Set<ArtifactType> knownTypes = new HashSet<ArtifactType>();
					for (final IToolFactory t : factories) {
						final List<ArtifactTypeDO> temp = ft
								.getToolArtifactTypes(t.getId(), t.getVersion());
						for (final ArtifactTypeDO a : temp) {
							knownTypes.add(ArtifactType.create(t, null, "", a.getMnemonic(), ""));
						}
					}

					final Map<IToolExtension, List<ArtifactType>> newExtensions = new HashMap<IToolExtension, List<ArtifactType>>();
					for (final IToolFactory t : factories) {
						for (final IToolExtension e : t.getExtensions()) {
							final List<ArtifactType> unknown = new ArrayList<ArtifactType>();
							for (final ArtifactType a : e.getArtifactTypes()) {
								if (!knownTypes.contains(a)) {
									unknown.add(a);
								}
							}
							if (!unknown.isEmpty()) {
								newExtensions.put(e, unknown);
							}
						}
					}
					
					if (newExtensions.isEmpty()) {
						System.out.println("No new artifact types");
					} else {
						final List<ArtifactType> types = new ArrayList<ArtifactType>();
						for(Map.Entry<IToolExtension,List<ArtifactType>> e : newExtensions.entrySet()) {
							types.addAll(e.getValue());
						}
						Collections.sort(types);
	 					
						// Map to finding types
						final List<FindingTypeDO> findingTypes = ft.listFindingTypes(); 						
						SLUIJob job = new SLUIJob() {
							@Override
							public IStatus runInUIThread(IProgressMonitor monitor) {
								if (false) {
									ArtifactTypeMappingDialog d = 
										new ArtifactTypeMappingDialog(null, types, findingTypes);
									if (d.open() != Window.OK) {
										// Cancelled, so clear finding type info
										for(ArtifactType t : types) {
											t.setFindingType(null);
										}
									}
								}								
								Data.getInstance().withTransaction(new NullDBQuery() {
									@Override
									public void doPerform(final Query q) {
										final FindingTypes ft = new FindingTypes(q);
										for(Map.Entry<IToolExtension,List<ArtifactType>> e : newExtensions.entrySet()) {
											setupDatabase(q, ft, e.getKey(), e.getValue());
										}
									}
								});
								return Status.OK_STATUS;
							}
						};
						job.schedule();
						

					}

				}
			});
			// FIX Show dialog
		} catch (final TransactionException e) {
			e.printStackTrace();
		}
	}

	private static void setupDatabase(final Query q, final FindingTypes ft,
			final IToolExtension te, final List<ArtifactType> unknown) {
		Collections.sort(unknown);
		final ExtensionDO ext = new ExtensionDO();
		ext.setName(te.getId());
		ext.setVersion(te.getVersion());
		for (final ArtifactType a : unknown) {
			// SLLogger.getLogger().warning("Couldn't find "+a.type+" for "+a.tool+", v"+a.version);
			System.out.println("Couldn't find " + a.type + " for " + a.tool
					+ ", v" + a.toolVersion);
			final FindingTypeDO ftDO = new FindingTypeDO();
			ftDO.setName(a.type);
			ftDO.setUid(a.type);
			ftDO.setShortMessage(a.type);
			ftDO.setInfo(a.type);
			ext.addFindingType(ftDO);
			final ArtifactTypeDO aDO = new ArtifactTypeDO(a.tool, a.type,
					a.type, a.toolVersion);
			ext.addType(a.type, aDO);
		}
		ft.registerExtension(ext);
		System.out.println("Registered extension: " + ext.getName() + " "
				+ ext.getVersion());
		// Find/define finding types
		final List<FindingTypeDO> ftypes = ft.listFindingTypes();
		for (final FindingTypeDO f : ftypes) {
			// Search?
			f.getName();
		}

		// Find/create categories -- can be modified later
		final Categories categories = new Categories(q);
		final List<CategoryDO> cats = categories.listCategories();
		for (final CategoryDO cdo : cats) {
			cdo.getName();
		}
	}

	// //////////////////////////////////////////////////////////////////////
	//
	// TOOL EXTENSION POINT METHODS
	//
	// //////////////////////////////////////////////////////////////////////

	/**
	 * @return all defined tool extension points defined in the plugin
	 *         manifests.
	 */
	private static IExtension[] readToolExtensionPoints() {
		final IExtensionRegistry pluginRegistry = Platform
				.getExtensionRegistry();
		final IExtensionPoint extensionPoint = pluginRegistry
				.getExtensionPoint(TOOL_PLUGIN_ID, TOOL_EXTENSION_POINT_ID);
		return extensionPoint.getExtensions();
	}

	/**
	 * Builds an array of all tool extension points that are marked in the XML
	 * as being production (i.e., production="true").
	 */
	private static List<IExtension> findProductionToolExtensionPoints() {
		final IExtension[] tools = readToolExtensionPoints();
		final List<IExtension> active = new ArrayList<IExtension>();
		for (final IExtension tool : tools) {
			// final String uid = tool.getUniqueIdentifier();
			boolean add = true;
			final IConfigurationElement[] configElements = tool
					.getConfigurationElements();
			for (int j = 0; j < configElements.length; j++) {
				final String production = configElements[j]
						.getAttribute("production");
				if (production != null && production.equals("false")) {
					/*
					 * if (LOG.isLoggable(Level.FINE))
					 * LOG.fine("analysis module extension point " + uid +
					 * " is not production and is being excluded");
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

	private static void addProductionToolDirectories(
			final Collection<String> ids) {
		for (final IExtension tool : findProductionToolExtensionPoints()) {
			ids.add(tool.getContributor().getName());
		}
	}

	public static List<IToolFactory> findToolFactories() {
		return ToolUtil.findToolFactories();
	}
}
