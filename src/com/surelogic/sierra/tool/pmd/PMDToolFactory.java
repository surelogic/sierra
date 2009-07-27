package com.surelogic.sierra.tool.pmd;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import net.sourceforge.pmd.Language;
import net.sourceforge.pmd.Rule;
import net.sourceforge.pmd.RuleSet;
import net.sourceforge.pmd.RuleSetFactory;
import net.sourceforge.pmd.RuleSetNotFoundException;

import com.surelogic.common.logging.SLLogger;
import com.surelogic.common.xml.Entities;
import com.surelogic.sierra.tool.AbstractToolExtension;
import com.surelogic.sierra.tool.AbstractToolFactory;
import com.surelogic.sierra.tool.ArtifactType;
import com.surelogic.sierra.tool.IToolExtension;
import com.surelogic.sierra.tool.IToolInstance;
import com.surelogic.sierra.tool.ToolUtil;
import com.surelogic.sierra.tool.analyzer.ILazyArtifactGenerator;
import com.surelogic.sierra.tool.message.*;

public class PMDToolFactory extends AbstractToolFactory {
	private static final String PMD_LIB = "pmd-lib";
	static final String rulesets = "all.xml"; // location of the XML rule file

	static final Iterable<RulePair> ERROR = new Iterable<RulePair>() {
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

	static class RuleSetInfo {
		final File location;
		final RuleSet ruleset;
		final boolean isCore;
		final Manifest props;

		RuleSetInfo(File jar, final RuleSet rs, final boolean core, final Manifest props) {
			location = jar;
			ruleset = rs;
			isCore = core;
			this.props = props;
		}

		RuleSetInfo(final RuleSet rs) {
			this(null, rs, true, null);
		}
	}

	// @Override
	// public String getVersion() {
	// return "4.2.4"/*PMD.VERSION*/;
	// }

	@Override
	protected IToolInstance create(final Config config,
			final ILazyArtifactGenerator generator, final boolean close) {
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

	public Collection<IToolExtension> getExtensions() {
		try {
			return extractArtifactTypes(getRuleSets());
		} catch (final RuleSetNotFoundException e) {
			LOG.log(Level.SEVERE, "Couldn't find rulesets", e);
		}
		return Collections.emptySet();
	}

	private static boolean containsRuleSet(final List<RuleSetInfo> sets,
			final RuleSet set) {
		for (final RuleSetInfo info : sets) {
			if (info.ruleset.getFileName().equals(set.getFileName())) {
				return true;
			}
		}
		return false;
	}

	Collection<IToolExtension> extractArtifactTypes(
			final List<RuleSetInfo> rulesets) {
		final List<IToolExtension> extensions = new ArrayList<IToolExtension>();
		for (final RuleSetInfo info : rulesets) {
			final Set<ArtifactType> types = new HashSet<ArtifactType>();
			for (final Rule r : info.ruleset.getRules()) {
				final ArtifactType t = ArtifactType.create(this, info.props,
						info.ruleset.getFileName(), r.getName(), r
								.getRuleSetName());
				types.add(t);
			}			
			final boolean isCore = info.isCore;
			extensions.add(new AbstractToolExtension(getId(), info.ruleset.getName(),					
        					                         info.location, types) {
				@Override
				public boolean isCore() {
					return isCore;
				}
			});
		}
		return extensions;
	}

	static List<RuleSet> getDefaultRuleSets() throws RuleSetNotFoundException {
		final List<RuleSet> sets = new ArrayList<RuleSet>();

		// only the default rules
		final RuleSetFactory ruleSetFactory = new RuleSetFactory();
		final Iterator<RuleSet> it = ruleSetFactory.getRegisteredRuleSets();
		while (it.hasNext()) {
			final RuleSet ruleset = it.next();
			final Language lang = ruleset.getLanguage();
			if ("Android Rules".equals(ruleset.getName())) {
				continue;
			}
			if (lang == null || Language.JAVA.equals(lang)) {
				// System.out.println("Found "+ruleset.getName()+" in "+ruleset.getFileName());
				sets.add(ruleset);
			}
		}
		return sets;
	}

	static List<RuleSetInfo> getRuleSets() throws RuleSetNotFoundException {
		final List<File> plugins = findPluginJars(false);
		final URLClassLoader cl = ToolUtil.computeClassLoader(
				AbstractPMDTool.class.getClassLoader(), plugins);
		final RuleSetFactory ruleSetFactory = new RuleSetFactory();
		final List<RuleSetInfo> rulesets = new ArrayList<RuleSetInfo>();
		for (final RuleSet s : getDefaultRuleSets()) {
			rulesets.add(new RuleSetInfo(s));
		}

		// Add in plugin rulesets
		for (final File jar : findPluginJars(false)) {
			try {
				for (final RulePair pair : findRuleSetsInJar(jar)) {
					if (pair != null) {
						final RuleSet set = ruleSetFactory.createRuleSet(
								pair.stream, cl);
						if (!containsRuleSet(rulesets, set)) {
							if (set.getFileName() == null) {
								set.setFileName(pair.name);
							}
							rulesets
									.add(new RuleSetInfo(jar, set, false, pair.props));
						}
					}
				}
			} catch (final IOException e) {
				LOG.log(Level.WARNING, "Problem while processing "
						+ jar.getAbsolutePath(), e);
			}
		}
		// RuleSet ruleset = ruleSetFactory.createSingleRuleSet(rulesets);
		return rulesets;
	}

	static List<RuleSetInfo> getSelectedRuleSets(String tool, Config c) throws RuleSetNotFoundException {
		Set<String> selected = new HashSet<String>();
		for(ToolExtension e : c.getExtensions()) {
			if (tool.equals(e.getTool())) {
				selected.add(e.getId());
			}
		}
		// Remove unselected RuleSetInfos
		List<RuleSetInfo> rules = getRuleSets();
		Iterator<RuleSetInfo> it = rules.iterator();
		while (it.hasNext()) {
			RuleSetInfo r = it.next();
			if (!r.isCore && !selected.contains(r.ruleset.getName())) {
				it.remove();
			}
		}
		return rules;
	}
	
	/**
	 * @return the directory containing all.xml file
	 */
	static File createAllXml() {
		return createRulesXml(rulesets);
	}
	
	/**
	 * @return the directory containing the xml file
	 */
	static File createRulesXml(final String filename) {
		final File lib = new File(ToolUtil.getSierraToolDirectory(), PMD_LIB);
		if (!lib.exists()) {
			lib.mkdirs();
		} else if (!lib.isDirectory()) {
			LOG.severe(lib.getAbsolutePath()
					+ " exists, but is not a directory");
			return null;
		}
		try {
			final File allXml = new File(lib, filename);
			if (allXml.exists()) {
				allXml.delete();
			}
			final PrintWriter pw = new PrintWriter(allXml);
			generateRulesXML(pw, getRuleSets());
			pw.close();
			return lib;
		} catch (final RuleSetNotFoundException e) {
			LOG.log(Level.SEVERE, "Problem getting rulesets", e);
		} catch (final FileNotFoundException e) {
			LOG.log(Level.SEVERE, "Couldn't create " + rulesets, e);
		}
		return null;
	}

	static void generateRulesXML(final PrintWriter pw, List<RuleSetInfo> rulesets) {
		pw.println("<?xml version=\"1.0\"?>");
		final StringBuilder b = new StringBuilder();
		Entities.start("ruleset", b);
		Entities.addAttribute("name", "Sierra PMD", b);
		Entities.addAttribute("xmlns", "http://pmd.sf.net/ruleset/1.0.0", b);
		Entities.addAttribute("xmlns:xsi",
				"http://www.w3.org/2001/XMLSchema-instance", b);
		Entities
				.addAttribute(
						"xsi:schemaLocation",
						"http://pmd.sf.net/ruleset/1.0.0 http://pmd.sf.net/ruleset_xml_schema.xsd",
						b);
		Entities.addAttribute("xsi:noNamespaceSchemaLocation",
				"http://pmd.sf.net/ruleset_xml_schema.xsd", b);
		b.append(">\n");
		pw.append(b);
		b.setLength(0);

		Entities.createTag("description", "Custom Sierra RuleSet", b);
		for (final RuleSetInfo info : rulesets) {
			final RuleSet rs = info.ruleset;
			Entities.start("rule", b);
			if (rs.getFileName() == null) {
				System.out.println(rs.getName());
			}
			Entities.addAttribute("ref", rs.getFileName(), b);
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

	static Iterable<RulePair> findRuleSetsInJar(final File jar)
			throws IOException {
		// Check if ruleset.properties looks good
		final ZipFile zf = new ZipFile(jar);
		/*
		 * Enumeration<? extends ZipEntry> entries = zf.entries(); while
		 * (entries.hasMoreElements()) {
		 * System.out.println("Got "+entries.nextElement().getName()); }
		 */
		final ZipEntry ze = zf.getEntry("rulesets/rulesets.properties");
		if (ze == null) {
			return null;
		}
		final InputStream is = zf.getInputStream(ze);
		if (is == null) {
			return ERROR;
		}
		final Properties props = new Properties();
		props.load(is);
		is.close();
		final String names = props.getProperty("rulesets.filenames");
		if (names == null) {
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
					SLLogger.getLogger().log(Level.WARNING,
							"Couldn't load finding type mapping for " + jar, e);
					ft_props = null;
				}
			}
		}
		final Manifest findingTypeProps = ft_props;
		return new Iterable<RulePair>() {
			public Iterator<RulePair> iterator() {
				return new Iterator<RulePair>() {
					public boolean hasNext() {
						return st.hasMoreTokens();
					}

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
							LOG.log(Level.WARNING, "Problem while reading "
									+ ruleset, e);
						}
						if (stream == null) {
							return null;
						}
						return new RulePair(ruleset, stream, findingTypeProps);
					}

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
			public boolean accept(final File dir, final String name) {
				return name.endsWith(".jar") || name.endsWith(".zip");
			}
		});
		if (jars == null || jars.length == 0) {
			return Collections.emptyList();
		}
		final List<File> valid = new ArrayList<File>();
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
							LOG.warning("Missing a ruleset in "
									+ jar.getAbsolutePath());
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
				LOG.log(Level.WARNING, "Problem opening "
						+ jar.getAbsolutePath(), e);
			}
		}
		return valid;
	}
}
