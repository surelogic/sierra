package com.surelogic.sierra.tool.pmd;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.surelogic.common.FileUtility.TempFileFilter;
import com.surelogic.common.core.jobs.EclipseLocalConfig;
import com.surelogic.common.jobs.NullSLProgressMonitor;
import com.surelogic.common.jobs.SLStatus;
import com.surelogic.common.jobs.remote.RemoteSLJobConstants;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.common.xml.Entities;
import com.surelogic.sierra.tool.AbstractToolFactory;
import com.surelogic.sierra.tool.IToolExtension;
import com.surelogic.sierra.tool.IToolInstance;
import com.surelogic.sierra.tool.ToolUtil;
import com.surelogic.sierra.tool.analyzer.ILazyArtifactGenerator;
import com.surelogic.sierra.tool.message.*;

public class PMDToolFactory extends AbstractToolFactory {
  static final String PMD_LIB = "pmd-lib";
  static final String RULESETS = "all.xml"; // location of the XML rule file

  static final Iterable<RulePair> ERROR = new Iterable<RulePair>() {
    @Override
    public Iterator<RulePair> iterator() {
      return null;
    }
  };

  static class RulePair {
    final String name;
    final InputStream stream;
    final Manifest props;

    RulePair(final String id, final InputStream is, final Manifest props) {
      name = id;
      stream = is;
      this.props = props;
    }
  }

  // @Override
  // public String getVersion() {
  // return "4.2.4"/*PMD.VERSION*/;
  // }

  @Override
  protected IToolInstance create(final Config config, final ILazyArtifactGenerator generator, final boolean close) {
    return new AbstractPMDTool(this, config, generator, close);
  }

  @Override
  public Collection<File> getRequiredJars(final Config config) {
    final Collection<File> jars = super.getRequiredJars(config);
    final File all = createAllXml();
    if (all != null) {
      jars.add(all);
    }
    // addAllPluginJarsToPath(debug, jars,
    // SierraToolConstants.PMD_PLUGIN_ID, "lib");
    jars.addAll(findPluginJars(true));
    return jars;
  }

  private static final List<IToolExtension> extensions = new ArrayList<>();
  private static final List<String> rulesetFilenames = new ArrayList<>();

  @Override
  public Collection<IToolExtension> getExtensions() {
    synchronized (PMDToolFactory.class) {
      return new ArrayList<>(extensions);
    }
    /*
     * try { return extractArtifactTypes(getRuleSets()); } catch (final
     * RuleSetNotFoundException e) { LOG.log(Level.SEVERE,
     * "Couldn't find rulesets", e); } return Collections.emptySet();
     */
  }

  // private static boolean containsRuleSet(final List<RuleSetInfo> sets,
  // final RuleSet set) {
  // for (final RuleSetInfo info : sets) {
  // if (info.ruleset.getFileName().equals(set.getFileName())) {
  // return true;
  // }
  // }
  // return false;
  // }

  // static List<RuleSetInfo> getRuleSets() {
  // final List<File> plugins = findPluginJars(false);
  // final ClassLoader cl = ToolUtil.computeClassLoader(
  // AbstractPMDTool.class.getClassLoader(), plugins);
  // final RuleSetFactory ruleSetFactory = new RuleSetFactory();
  // final List<RuleSetInfo> rulesets = new ArrayList<RuleSetInfo>();
  // for (final RuleSet s : getDefaultRuleSets()) {
  // rulesets.add(new RuleSetInfo(s));
  // }
  //
  // // Add in plugin rulesets
  // for (final File jar : findPluginJars(false)) {
  // try {
  // for (final RulePair pair : findRuleSetsInJar(jar)) {
  // if (pair != null) {
  // final RuleSet set = ruleSetFactory.createRuleSet(
  // pair.stream, cl);
  // if (!containsRuleSet(rulesets, set)) {
  // if (set.getFileName() == null) {
  // set.setFileName(pair.name);
  // }
  // rulesets
  // .add(new RuleSetInfo(jar, set, false, pair.props));
  // }
  // }
  // }
  // } catch (final IOException e) {
  // LOG.log(Level.WARNING, "Problem while processing "
  // + jar.getAbsolutePath(), e);
  // }
  // }
  // // RuleSet ruleset = ruleSetFactory.createSingleRuleSet(rulesets);
  // return rulesets;
  // }

  // static List<RuleSetInfo> getSelectedRuleSets(String tool, Config c) {
  // Set<String> selected = new HashSet<String>();
  // for(ToolExtension e : c.getExtensions()) {
  // if (tool.equals(e.getTool())) {
  // selected.add(e.getId());
  // }
  // }
  // // Remove unselected RuleSetInfos
  // List<RuleSetInfo> rules = getRuleSets();
  // Iterator<RuleSetInfo> it = rules.iterator();
  // while (it.hasNext()) {
  // RuleSetInfo r = it.next();
  // if (!r.isCore && !selected.contains(r.ruleset.getName())) {
  // it.remove();
  // }
  // }
  // return rules;
  // }

  /**
   * @return the directory containing all.xml file
   */
  File createAllXml() {
    initToolExtensions(getVersion());
    return createRulesXml(RULESETS);
  }

  /**
   * @return the directory containing the xml file
   */
  static synchronized File createRulesXml(final String filename) {
    final File lib = new File(ToolUtil.getSierraToolDirectory(), PMD_LIB);
    if (!lib.exists()) {
      lib.mkdirs();
    } else if (!lib.isDirectory()) {
      SLLogger.getLogger().severe(lib.getAbsolutePath() + " exists, but is not a directory");
      return null;
    }
    try {
      final File allXml = new File(lib, filename);
      if (allXml.exists()) {
        allXml.delete();
      }
      final PrintWriter pw = new PrintWriter(allXml);
      generateRulesXML(pw, rulesetFilenames);
      pw.close();
      return lib;
    } catch (final FileNotFoundException e) {
      SLLogger.getLogger().log(Level.SEVERE, "Couldn't create " + filename, e);
    }
    return null;
  }

  static void generateRulesXML(final PrintWriter pw, List<String> ruleSetFileNames) {
    pw.println("<?xml version=\"1.0\"?>");
    final StringBuilder b = new StringBuilder();
    Entities.start("ruleset", b);
    Entities.addAttribute("name", "Sierra PMD", b);
    Entities.addAttribute("xmlns", "http://pmd.sf.net/ruleset/1.0.0", b);
    Entities.addAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance", b);
    Entities.addAttribute("xsi:schemaLocation", "http://pmd.sf.net/ruleset/1.0.0 http://pmd.sf.net/ruleset_xml_schema.xsd", b);
    Entities.addAttribute("xsi:noNamespaceSchemaLocation", "http://pmd.sf.net/ruleset_xml_schema.xsd", b);
    b.append(">\n");
    pw.append(b);
    b.setLength(0);

    Entities.createTag("description", "Custom Sierra RuleSet", b);
    for (final String fileName : ruleSetFileNames) {
      Entities.start("rule", b);
      Entities.addAttribute("ref", fileName, b);
      b.append("/>\n");
      pw.append(b);
      b.setLength(0);
    }
    b.append("\n</ruleset>");
    pw.append(b);
    b.setLength(0);

    // Somehow needs to be a resource on the PMD classpath
    // -- Not so much a problem for the remote VM, but hard/impossible to do
    // locally (for debugging)
  }

  static Iterable<RulePair> findRuleSetsInJar(final File jar) throws IOException {
    // Check if ruleset.properties looks good
    final ZipFile zf = new ZipFile(jar);
    /*
     * Enumeration<? extends ZipEntry> entries = zf.entries(); while
     * (entries.hasMoreElements()) {
     * System.out.println("Got "+entries.nextElement().getName()); }
     */
    final ZipEntry ze = zf.getEntry("rulesets/rulesets.properties");
    if (ze == null) {
      zf.close();
      return null;
    }
    final InputStream is = zf.getInputStream(ze);
    if (is == null) {
      zf.close();
      return ERROR;
    }
    final Properties props = new Properties();
    props.load(is);
    is.close();
    final String names = props.getProperty("rulesets.filenames");
    if (names == null) {
      zf.close();
      return ERROR;
    }
    final StringTokenizer st = new StringTokenizer(names, ",");

    // Check for finding type properties
    Manifest ft_props = null;
    final ZipEntry ze2 = zf.getEntry(ToolUtil.SIERRA_MANIFEST);
    if (ze2 != null) {
      final InputStream is2 = zf.getInputStream(ze2);
      if (is2 != null) {
        ft_props = new Manifest();
        try {
          ft_props.read(is2);
          is2.close();
        } catch (final IOException e) {
          SLLogger.getLogger().log(Level.WARNING, "Couldn't load finding type mapping for " + jar, e);
          ft_props = null;
        }
      }
    }
    zf.close();

    final Manifest findingTypeProps = ft_props;
    return new Iterable<RulePair>() {
      @Override
      public Iterator<RulePair> iterator() {
        return new Iterator<RulePair>() {
          @Override
          public boolean hasNext() {
            return st.hasMoreTokens();
          }

          @Override
          public RulePair next() {
            final String ruleset = st.nextToken();
            final ZipEntry entry = zf.getEntry(ruleset);
            if (entry == null) {
              return null;
            }
            InputStream stream = null;
            try {
              stream = zf.getInputStream(entry);
            } catch (final IOException e) {
              SLLogger.getLogger().log(Level.WARNING, "Problem while reading " + ruleset, e);
            }
            if (stream == null) {
              return null;
            }
            return new RulePair(ruleset, stream, findingTypeProps);
          }

          @Override
          public void remove() {
            throw new UnsupportedOperationException();
          }

        };
      }
    };
  }

  static List<File> findPluginJars(final boolean includeRequired) {
    final File lib = new File(ToolUtil.getSierraToolDirectory(), PMD_LIB);
    if (!lib.exists() || !lib.isDirectory()) {
      return Collections.emptyList();
    }
    final File[] jars = lib.listFiles(new FilenameFilter() {
      @Override
      public boolean accept(final File dir, final String name) {
        return name.endsWith(".jar") || name.endsWith(".zip");
      }
    });
    if (jars == null || jars.length == 0) {
      return Collections.emptyList();
    }
    final List<File> valid = new ArrayList<>();
    for (final File jar : jars) {
      try {
        final Iterable<RulePair> rulesets = findRuleSetsInJar(jar);

        // Check if ruleset.properties looks good
        if (rulesets == ERROR) {
          continue;
        }
        if (rulesets != null) {
          // Check that the named rulesets all exist
          boolean ok = true;
          for (final RulePair is : rulesets) {
            if (is == null) {
              SLLogger.getLogger().warning("Missing a ruleset in " + jar.getAbsolutePath());
              ok = false;
              break;
            }
          }
          if (ok) {
            valid.add(jar);
          }
        } else if (includeRequired) {
          // Not a PMD plugin, but probably a jar needed for a plugin
          // Check if classes are duplicated on the class path?
          valid.add(jar);
        }
      } catch (final IOException e) {
        SLLogger.getLogger().log(Level.WARNING, "Problem opening " + jar.getAbsolutePath(), e);
      }
    }
    return valid;
  }

  public static final TempFileFilter filter = new TempFileFilter("pmd", ".rulesets");

  @Override
  public void init(File toolHome, File pluginDir) {
    super.init(toolHome, pluginDir);
    initToolExtensions(getVersion());
  }

  private static synchronized void initToolExtensions(String version) {
    List<IToolExtension> exts = null;
    List<String> names = null;
    try {
      if (System.getProperty(RemoteSLJobConstants.RUNNING_REMOTELY) != null) {
        // Initialize directly
        exts = RemotePMDRuleSetQueryJob.getToolExtensions(version);
        names = RemotePMDRuleSetQueryJob.getRuleSetFileNames();
      } else {
        File resultsDir = filter.createTempFolder();
        EclipseLocalConfig cfg = new EclipseLocalConfig(1024, resultsDir);
        SLStatus s = new LocalPMDRuleSetQueryJob("Querying PMD about its rulesets", 100, cfg, version)
            .run(new NullSLProgressMonitor());
        if (s.getCode() == SLStatus.OK) {
          exts = readToolExtensions(resultsDir);
          names = readRuleSetFilenames(resultsDir);
        } else if (s.getException() != null) {
          SLLogger.getLogger().log(Level.SEVERE, "Got exception while querying PMD", s.getException());
        } else {
          SLLogger.getLogger().log(Level.SEVERE, s.getMessage());
        }
      }
    } catch (IOException e) {
      SLLogger.getLogger().log(Level.SEVERE, "Unable to query PMD about its rulesets", e);
    }
    if (exts != null) {
      extensions.clear();
      extensions.addAll(exts);
    }
    if (names != null) {
      rulesetFilenames.clear();
      rulesetFilenames.addAll(names);
    }
  }

  private static synchronized List<IToolExtension> readToolExtensions(File resultsDir) {
    File results = new File(resultsDir, RemotePMDRuleSetQueryJob.TOOL_EXTENSIONS);
    List<IToolExtension> exts = null;
    if (results.isFile()) {
      try {
        ObjectInputStream in = new ObjectInputStream(new FileInputStream(results));
        exts = new ArrayList<>();
        while (in.available() > 0) {
          exts.add((IToolExtension) in.readObject());
        }
        in.close();
      } catch (IOException e) {
        SLLogger.getLogger().log(Level.SEVERE, "Unable to read resulting tool extensions", e);
        exts = null;
      } catch (ClassNotFoundException e) {
        SLLogger.getLogger().log(Level.SEVERE, "Unable to read resulting tool extensions, due to missing class", e);
        exts = null;
      }
    } else {
      SLLogger.getLogger().severe("Unable to find " + results);
    }
    return exts;
  }

  private static synchronized List<String> readRuleSetFilenames(File resultsDir) {
    File results = new File(resultsDir, RemotePMDRuleSetQueryJob.RULESET_LOCATIONS);
    List<String> names = null;
    if (results.isFile()) {
      try {
        BufferedReader in = new BufferedReader(new FileReader(results));
        String line = null;
        names = new ArrayList<>();
        while ((line = in.readLine()) != null) {
          names.add(line);
        }
        in.close();
      } catch (IOException e) {
        SLLogger.getLogger().log(Level.SEVERE, "Unable to read resulting filenames", e);
        names = null;
      }
    } else {
      SLLogger.getLogger().severe("Unable to find " + results);
    }
    return names;
  }
}
