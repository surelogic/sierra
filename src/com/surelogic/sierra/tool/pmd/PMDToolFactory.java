package com.surelogic.sierra.tool.pmd;

import java.io.*;
import java.net.URLClassLoader;
import java.util.*;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import net.sourceforge.pmd.*;

import com.surelogic.common.xml.Entities;
import com.surelogic.sierra.tool.*;
import com.surelogic.sierra.tool.analyzer.ILazyArtifactGenerator;
import com.surelogic.sierra.tool.message.Config;

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
		
		RulePair(String id, InputStream is) {
			name = id;
			stream = is;
		}
	}
	
//	@Override
//	public String getVersion() {
//		return "4.2.4"/*PMD.VERSION*/;
//	}

	@Override
	protected IToolInstance create(Config config, ILazyArtifactGenerator generator, boolean close) {
		return new AbstractPMDTool(this, config, generator, close);
	}
	
	@Override
	public Collection<File> getRequiredJars(Config config) {
		final Collection<File> jars = super.getRequiredJars(config);		
		final File all = createAllXml();
		if (all != null) {
			jars.add(all);
		}
		//addAllPluginJarsToPath(debug, jars, SierraToolConstants.PMD_PLUGIN_ID, "lib");
		jars.addAll(findPluginJars(true));	
		return jars;
	}
	
	public Collection<IToolExtension> getExtensions() {
		try {		
			return extractArtifactTypes(getRuleSets());							
		} catch (RuleSetNotFoundException e) {
			LOG.log(Level.SEVERE, "Couldn't find rulesets", e);
		}
		return Collections.emptySet();
	}
	
	private static boolean containsRuleSet(List<RuleSet> sets, RuleSet set) {
		for(RuleSet inSet : sets) {
			if (inSet.getFileName().equals(set.getFileName())) {
				return true;
			}
		}
		return false;
	}

	Collection<IToolExtension> extractArtifactTypes(List<RuleSet> rulesets) {
		List<IToolExtension> extensions = new ArrayList<IToolExtension>();
		for(RuleSet ruleset : rulesets) {
			Set<ArtifactType> types = new HashSet<ArtifactType>();	
			for(Rule r : ruleset.getRules()) {
				types.add(new ArtifactType(getName(), getVersion(), 
						ruleset.getFileName(), r.getName(), r.getRuleSetName()));					
			}
			extensions.add(new AbstractToolExtension(ruleset.getName(), types) {});
		}												
		return extensions;
	}
	
	static List<RuleSet> getDefaultRuleSets() throws RuleSetNotFoundException {
		List<RuleSet> sets = new ArrayList<RuleSet>();
		
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
				//System.out.println("Found "+ruleset.getName()+" in "+ruleset.getFileName());
				sets.add(ruleset);
			}
		}
		return sets;
	}
	
	static List<RuleSet> getRuleSets() throws RuleSetNotFoundException {
		List<File> plugins = findPluginJars(false);
		final URLClassLoader cl = ToolUtil.computeClassLoader(AbstractPMDTool.class.getClassLoader(), plugins);
		final RuleSetFactory ruleSetFactory = new RuleSetFactory();
		final List<RuleSet> sets = getDefaultRuleSets();

		// Add in plugin rulesets
		for(File jar : findPluginJars(false)) {
			try {
				for(RulePair pair : findRuleSetsInJar(jar)) {
					if (pair != null) {
						RuleSet set = ruleSetFactory.createRuleSet(pair.stream, cl);
						if (!containsRuleSet(sets, set)) {
							if (set.getFileName() == null) {
								set.setFileName(pair.name);
							}
							sets.add(set);
						}
					}
				}
			} catch (IOException e) {
				LOG.log(Level.WARNING, "Problem while processing "+jar.getAbsolutePath(), e);
			}
		}
		//RuleSet ruleset = ruleSetFactory.createSingleRuleSet(rulesets);	
		return sets;
	}
	
	/**
	 * @return the directory containing all.xml
	 */
	static File createAllXml() {
		final File lib = new File(ToolUtil.getSierraToolDirectory(), PMD_LIB);
		if (!lib.exists()) {
			lib.mkdirs();
		}
		else if (!lib.isDirectory()) {
			LOG.severe(lib.getAbsolutePath()+" exists, but is not a directory");
			return null;
		}
		try {
			File allXml = new File(lib, rulesets);
			if (allXml.exists()) {
				allXml.delete();
			}
			PrintWriter pw = new PrintWriter(allXml);
			generateRulesXML(pw);
			pw.close();
			return lib;
		} catch (FileNotFoundException e) {
			LOG.log(Level.SEVERE, "Couldn't create "+rulesets, e);
		}
		return null;
	}
	
	static void generateRulesXML(PrintWriter pw) {
		pw.println("<?xml version=\"1.0\"?>");
		StringBuilder b = new StringBuilder();
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
		try {
			for(RuleSet rs : getRuleSets()) {
				Entities.start("rule", b);
				if (rs.getFileName() == null) {
					System.out.println(rs.getName());
				}
				Entities.addAttribute("ref", rs.getFileName(), b);
				b.append("/>\n");
				pw.append(b);
				b.setLength(0);
			}
		} catch (RuleSetNotFoundException e) {
			e.printStackTrace();
		}
		b.append("\n</ruleset>");
		pw.append(b);
		b.setLength(0);
		
		// Somehow needs to be a resource on the PMD classpath
		// -- Not so much a problem for the remote VM, but hard/impossible to do locally (for debugging)
	}
	
	static Iterable<RulePair> findRuleSetsInJar(final File jar) throws IOException {
		// Check if ruleset.properties looks good
		final ZipFile zf  = new ZipFile(jar);
		/*
		Enumeration<? extends ZipEntry> entries = zf.entries();
		while (entries.hasMoreElements()) {
			System.out.println("Got "+entries.nextElement().getName());
		}		
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
		final String names = props.getProperty("rulesets.filenames");
		if (names == null) {
			return ERROR;
		}				
		final StringTokenizer st = new StringTokenizer(names, ",");
		return new Iterable<RulePair>() {
			public Iterator<RulePair> iterator() {
				return new Iterator<RulePair>() {				
					public boolean hasNext() {
						return st.hasMoreTokens();
					}

					public RulePair next() {
						String ruleset = st.nextToken();
						ZipEntry entry = zf.getEntry(ruleset);
						if (entry == null) {
							return null;
						}
						InputStream stream = null;
						try {
							stream = zf.getInputStream(entry);
						} catch (IOException e) {
							LOG.log(Level.WARNING, "Problem while reading "+ruleset, e);
						}
						if (stream == null) {
							return null;
						}						
						return new RulePair(ruleset, stream);
					}
					
					public void remove() {
						throw new UnsupportedOperationException();
					}
					
				};
			}
		};	
	}
	
	static List<File> findPluginJars(boolean includeRequired) {
		final File lib = new File(ToolUtil.getSierraToolDirectory(), PMD_LIB);
		if (!lib.exists() || !lib.isDirectory()) {
			return Collections.emptyList();
		}
		final File[] jars = lib.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.endsWith(".jar") || name.endsWith(".zip");
			}			
		});
		if (jars == null || jars.length == 0) {
			return Collections.emptyList();
		}
		final List<File> valid = new ArrayList<File>();
		for(final File jar : jars) {
			try {
				final Iterable<RulePair> rulesets = findRuleSetsInJar(jar);
				
				// Check if ruleset.properties looks good
				if (rulesets == ERROR) {
					continue;
				}
				if (rulesets != null) {
					// Check that the named rulesets all exist				
					boolean ok = true;
					for(RulePair is : rulesets) {						
						if (is == null) {
							LOG.warning("Missing a ruleset in "+jar.getAbsolutePath());
							ok = false;
							break; 
						}						
					}
					if (ok) {
						valid.add(jar);
					}
				} 
				else if (includeRequired) { 
					// Not a PMD plugin, but probably a jar needed for a plugin
					// Check if classes are duplicated on the class path?
					valid.add(jar);
				}
			} catch (IOException e) {
				LOG.log(Level.WARNING, "Problem opening "+jar.getAbsolutePath(), e);
			}
		}
		return valid;
	}
}
