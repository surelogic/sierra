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
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;

import com.surelogic.common.core.EclipseUtility;
import com.surelogic.common.jdbc.NullDBQuery;
import com.surelogic.common.jdbc.Query;
import com.surelogic.common.jdbc.TransactionException;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.common.ui.jobs.SLUIJob;
import com.surelogic.sierra.client.eclipse.preferences.SierraPreferencesUtility;
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

  public static void initializeToolDirectories() {
    File sierraDataDir = SierraPreferencesUtility.getSierraDataDirectory();

    // Check if tools dir setup yet
    final String toolsDirSet = System.getProperty(ToolUtil.CUSTOM_TOOLS_PATH_PROP_NAME);
    if (toolsDirSet == null) {
      final String origToolsDir = System.getProperty(ToolUtil.TOOLS_PATH_PROP_NAME);
      // set Sierra tools dir if unavailable
      if (origToolsDir == null) {
        System.setProperty(ToolUtil.CUSTOM_TOOLS_PATH_PROP_NAME, "");
        System.setProperty(ToolUtil.TOOLS_PATH_PROP_NAME, sierraDataDir.getAbsolutePath());
      } else {
        System.setProperty(ToolUtil.CUSTOM_TOOLS_PATH_PROP_NAME, origToolsDir);
      }
    }
  }

  public static final String TOOL_PLUGIN_ID = "com.surelogic.sierra.tool";

  /**
   * The Sierra tool module extension point identifier <i>must</i> match the
   * plugin manifest.
   */
  public static final String TOOL_EXTENSION_POINT_ID = "sierraTool";

  static {
    ToolUtil.addToolFinder(new IToolFinder() {
      // FIX what about duplicate finders?
      @Override
      public List<File> findToolDirectories() {
        // Look up locations of these plugins
        final List<File> tools = new ArrayList<File>();
        for (final String id : getToolPluginIds()) {
          final String path = EclipseUtility.getDirectoryOf(id);
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
   * Collect plug-ins
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
           * config.putPluginDir(SierraToolConstants.FB_PLUGIN_ID, com.surelogic
           * .common.eclipse.Activator.getDefault().getDirectoryOf
           * (SierraToolConstants.FB_PLUGIN_ID));
           */

          final Set<ArtifactType> knownTypes = new HashSet<ArtifactType>();
          final Set<ArtifactType> unknownTypes = new HashSet<ArtifactType>();
          for (final IToolFactory t : factories) {
            final List<ArtifactTypeDO> temp = ft.getToolArtifactTypes(t.getId(), t.getVersion());
            for (final ArtifactTypeDO a : temp) {
              knownTypes.add(ArtifactType.create(t, null, "", a.getMnemonic(), ""));
            }
          }

          final Map<IToolExtension, List<ArtifactType>> newExtensions = new HashMap<IToolExtension, List<ArtifactType>>();
          for (final IToolFactory t : factories) {
            for (final IToolExtension e : t.getExtensions()) {
              // System.out.println("Ext: "+e.getId());
              final List<ArtifactType> unknown = new ArrayList<ArtifactType>();
              for (final ArtifactType a : e.getArtifactTypes()) {
                if (!knownTypes.contains(a) && !unknownTypes.contains(a)) {
                  unknownTypes.add(a); // To eliminate
                                       // shared/duplicate
                                       // types
                  unknown.add(a);
                }
              }
              if (!unknown.isEmpty()) {
                newExtensions.put(e, unknown);
              }
            }
          }

          if (newExtensions.isEmpty()) {
            SLLogger.getLogger().log(Level.FINE, "No new artifact types");
          } else {
            final List<ArtifactType> types = new ArrayList<ArtifactType>();
            for (final Map.Entry<IToolExtension, List<ArtifactType>> e : newExtensions.entrySet()) {
              types.addAll(e.getValue());
            }
            Collections.sort(types);

            // Map to finding types
            final SLUIJob job = new SLUIJob() {
              @Override
              public IStatus runInUIThread(final IProgressMonitor monitor) {
                final List<ArtifactType> incompleteTypes = new ArrayList<ArtifactType>();
                for (final ArtifactType t : types) {
                  if (!t.isComplete()) {
                    incompleteTypes.add(t);
                  }
                }

                Data.getInstance().withTransaction(new NullDBQuery() {
                  @Override
                  public void doPerform(final Query q) {
                    final FindingTypes ft = new FindingTypes(q);
                    for (final Map.Entry<IToolExtension, List<ArtifactType>> e : newExtensions.entrySet()) {
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
      SLLogger.getLogger().log(Level.SEVERE, "Problem handling new artifact types", e);
    }
  }

  static void setupDatabase(final Query q, final FindingTypes ft, final IToolExtension te, final List<ArtifactType> unknown) {
    // System.out.println(te.getId());
    Collections.sort(unknown);
    String extPath = "";
    if (!te.isCore()) {
      File tef = te.getJar();
      do {
        tef = tef.getParentFile();
      } while (tef != null && tef.getParent() != null && !tef.getParentFile().equals(ToolUtil.getSierraToolDirectory()));
      if (tef == null) {
        throw new IllegalStateException("The extension" + te.getId() + " does not appear to be under the sierra data directory.");
      }
      extPath = tef.getPath();
    }

    final ExtensionDO ext = new ExtensionDO(te.getId(), te.getVersion(), extPath);
    for (final ArtifactType a : unknown) {
      // SLLogger.getLogger().warning("Couldn't find "+a.type+" for "+a.tool+", v"+a.toolVersion);
      /*
       * System.out.println("Couldn't find " + a.type + " for " + a.tool + ", v"
       * + a.toolVersion);
       */
      if (a.getFindingType() == null) {
        // Check if there's already a finding type with the same name
        if (ft.getFindingType(a.type) == null) {
          // System.out.println(a.type);

          final FindingTypeDO ftDO = new FindingTypeDO();
          ftDO.setName(a.type);
          ftDO.setUid(a.type);
          ftDO.setShortMessage(a.type);
          ftDO.setInfo(a.type);
          ext.addFindingType(ftDO);

        } else {
          // System.out.println("Exists: "+a.type);
        }
        a.setFindingType(a.type);
      }
      final ArtifactTypeDO aDO = new ArtifactTypeDO(a.tool, a.type, a.type, a.toolVersion);
      ext.addType(a.getFindingType(), aDO);
    }
    ft.registerExtension(ext);
    /*
     * System.out.println("Registered extension: " + ext.getName() + " " +
     * ext.getVersion());
     */
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
   * @return all defined tool extension points defined in the plugin manifests.
   */
  private static IExtension[] readToolExtensionPoints() {
    final IExtensionRegistry pluginRegistry = Platform.getExtensionRegistry();
    final IExtensionPoint extensionPoint = pluginRegistry.getExtensionPoint(TOOL_PLUGIN_ID, TOOL_EXTENSION_POINT_ID);
    return extensionPoint.getExtensions();
  }

  /**
   * Builds an array of all tool extension points that are marked in the XML as
   * being production (i.e., production="true").
   */
  private static List<IExtension> findProductionToolExtensionPoints() {
    final IExtension[] tools = readToolExtensionPoints();
    final List<IExtension> active = new ArrayList<IExtension>();
    for (final IExtension tool : tools) {
      boolean add = true;
      final IConfigurationElement[] configElements = tool.getConfigurationElements();
      for (int j = 0; j < configElements.length; j++) {
        final String production = configElements[j].getAttribute("production");
        if (production != null && production.equals("false")) {
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

  private static void addProductionToolDirectories(final Collection<String> ids) {
    for (final IExtension tool : findProductionToolExtensionPoints()) {
      ids.add(tool.getContributor().getName());
    }
  }

  public static List<IToolFactory> findToolFactories() {
    return ToolUtil.findToolFactories();
  }
}
