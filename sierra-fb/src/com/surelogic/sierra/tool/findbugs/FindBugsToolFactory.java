package com.surelogic.sierra.tool.findbugs;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.tool.AbstractToolExtension;
import com.surelogic.sierra.tool.AbstractToolFactory;
import com.surelogic.sierra.tool.ArtifactType;
import com.surelogic.sierra.tool.IToolExtension;
import com.surelogic.sierra.tool.IToolInstance;
import com.surelogic.sierra.tool.ToolUtil;
import com.surelogic.sierra.tool.analyzer.ILazyArtifactGenerator;
import com.surelogic.sierra.tool.message.Config;

import edu.umd.cs.findbugs.BugPattern;
import edu.umd.cs.findbugs.DetectorFactoryCollection;
import edu.umd.cs.findbugs.Plugin;

public class FindBugsToolFactory extends AbstractToolFactory {
  private static final String CORE = "edu.umd.cs.findbugs.plugins.core";

  @Override
  public void init(File toolHome, File pluginDir) {
    super.init(toolHome, pluginDir);
    AbstractFindBugsTool.init(toolHome.getAbsolutePath());
  }

  static <T> Iterable<T> iterable(final Iterator<T> it) {
    return new Iterable<T>() {
      @Override
      public Iterator<T> iterator() {
        return it;
      }
    };
  }

  // @Override
  @Override
  public final Collection<IToolExtension> getExtensions() {
    if (!isActive()) {
      return Collections.emptyList();
    }
    List<IToolExtension> extensions = new ArrayList<>();

    // Code to get meta-data from FindBugs
    for (Plugin plugin : iterable(DetectorFactoryCollection.instance().pluginIterator())) {
      final String pluginId = plugin.getPluginId();
      final Manifest manifest = findSierraManifest(plugin);
      Set<ArtifactType> types = new HashSet<>();

      for (BugPattern pattern : plugin.getBugPatterns()) {
        ArtifactType t = ArtifactType.create(this, manifest, pluginId, pattern.getType(), pattern.getCategory());
        types.add(t);
      }
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
      extensions.add(new AbstractToolExtension(getId(), plugin.getPluginId(), getVersion(), loc, types) {
        private static final long serialVersionUID = 7819226437367633410L;

        @Override
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
       * InputStreamReader r = new InputStreamReader(zis); int c; while ((c =
       * r.read()) != -1) { System.out.print((char) c); }
       */
      Manifest props = new Manifest();
      props.read(zis);
      zis.close();
      return props;
    } catch (IOException e) {
      SLLogger.getLogger().log(Level.WARNING, "Couldn't load finding type mapping for " + plugin.getPluginId(), e);
      return null;
    }
  }

  /*
   * @Override public List<File> getRequiredJars() { final List<File> jars = new
   * ArrayList<File>(); addAllPluginJarsToPath(debug, jars,
   * SierraToolConstants.FB_PLUGIN_ID, "lib"); return jars; }
   */

  @Override
  protected IToolInstance create(Config config, ILazyArtifactGenerator generator, boolean close) {
    return new AbstractFindBugsTool(this, config, generator, close);
  }
}
