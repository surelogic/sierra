package com.surelogic.sierra.tool.pmd;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.URLClassLoader;
import java.nio.charset.Charset;
import java.util.*;
import java.util.logging.Level;
import java.util.zip.*;

import net.sourceforge.pmd.*;
import net.sourceforge.pmd.Report.ProcessingError;
import net.sourceforge.pmd.renderers.Renderer;

import com.surelogic.common.*;
import com.surelogic.common.jobs.SLProgressMonitor;
import com.surelogic.common.jobs.SLStatus;
import com.surelogic.common.xml.Entities;
import com.surelogic.sierra.tool.*;
import com.surelogic.sierra.tool.ArtifactType;
import com.surelogic.sierra.tool.analyzer.ILazyArtifactGenerator;
import com.surelogic.sierra.tool.message.*;
import com.surelogic.sierra.tool.message.ArtifactGenerator.*;
import com.surelogic.sierra.tool.targets.IToolTarget;

public class AbstractPMDTool extends AbstractTool {
	private static final String PMD_LIB = "pmd-lib";	
	private static final String rulesets = "all.xml"; // location of the XML rule file
	
	public AbstractPMDTool(Config config) {
		super("PMD", "4.2.4"/*PMD.VERSION*/, "PMD", "", config);
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
	
	private static boolean containsRuleSet(List<RuleSet> sets, RuleSet set) {
		for(RuleSet inSet : sets) {
			if (inSet.getFileName().equals(set.getFileName())) {
				return true;
			}
		}
		return false;
	}

	Set<ArtifactType> extractArtifactTypes(List<RuleSet> rulesets) {
		Set<ArtifactType> types = new HashSet<ArtifactType>();	
		for(RuleSet ruleset : rulesets) {
			for(Rule r : ruleset.getRules()) {
				types.add(new ArtifactType(getName(), getVersion(), 
						ruleset.getFileName(), r.getName(), r.getRuleSetName()));					
			}
		}												
		return types;
	}
	
	public final Set<ArtifactType> getArtifactTypes() {
		/*
		Properties props = System.getProperties();
		for(Map.Entry<Object,Object> e : props.entrySet()) {
			System.out.println(e.getKey()+" = "+e.getValue());
		}
		for(File jar : findPluginJars()) {
			System.out.println("Found PMD jar: "+jar.getName());
		}
		*/
		try {		
			return extractArtifactTypes(getRuleSets());							
		} catch (RuleSetNotFoundException e) {
			LOG.log(Level.SEVERE, "Couldn't find rulesets", e);
		}
		return Collections.emptySet();
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
	
	@Override
	public List<File> getRequiredJars() {
		final List<File> jars = new ArrayList<File>();				
		final File all = createAllXml();
		if (all != null) {
			jars.add(all);
		}
		addAllPluginJarsToPath(debug, jars,	SierraToolConstants.PMD_PLUGIN_ID, "lib");
		jars.addAll(findPluginJars(true));	
		return jars;
	}
	
	protected final IToolInstance create(String name, final ILazyArtifactGenerator generator,
			boolean close) {
		return new AbstractToolInstance(debug, this, generator, close) {
			@Override
			protected SLStatus execute(SLProgressMonitor monitor)
					throws Exception {
				int cpus = Runtime.getRuntime().availableProcessors();
				String encoding = new InputStreamReader(System.in)
						.getEncoding();
				String altEncoding = Charset.defaultCharset().name();
				if (!encoding.equals(altEncoding)) {
					System.out.println("Encoding '" + encoding + "' != "
							+ altEncoding);
				}
				final String sourceLevel = getOption(SOURCE_LEVEL);
				final SourceType sourceType;
				if ("1.4".equals(sourceLevel)) {
					sourceType = SourceType.JAVA_14;
				} else if ("1.5".equals(sourceLevel)) {
					sourceType = SourceType.JAVA_15;
				} else if ("1.3".equals(sourceLevel)) {
					sourceType = SourceType.JAVA_13;
				} else if ("1.6".equals(sourceLevel)
						|| "1.7".equals(sourceLevel)) {
					sourceType = SourceType.JAVA_16;
				} else {
					sourceType = SourceType.JAVA_14;
				}
				RuleContext ctx = new RuleContext(); // info about what's
				// getting scanned
				RuleSetFactory ruleSetFactory = new RuleSetFactory(); // only
				// the
				// default
				// rules

				String excludeMarker = PMD.EXCLUDE_MARKER;

				// Added for PMD 4.2
				final File auxPathFile = File.createTempFile("auxPath", ".txt");
				if (auxPathFile.exists()) {
					PrintWriter pw = new PrintWriter(auxPathFile);
					for (IToolTarget t : getAuxTargets()) {
						pw.println(new File(t.getLocation()).getAbsolutePath());
					}
					pw.close();
				}
				final ClassLoader cl = PMD
						.createClasspathClassLoader(auxPathFile.toURI().toURL()
								.toString());

				final List<DataSource> files = new ArrayList<DataSource>();
				prepJavaFiles(new SourcePrep() {
					public void prep(File f) {
						files.add(new FileDataSource(f));
					}					
				});
				final List<Renderer> renderers = new ArrayList<Renderer>(); // output
				renderers.add(new Output(generator, monitor));

				monitor.begin(files.size() + 25);
				PMD.processFiles(cpus, ruleSetFactory, sourceType, files, ctx,
						renderers, rulesets, false, "", encoding,
						excludeMarker, cl);
				auxPathFile.delete();
				return SLStatus.OK_STATUS;
			}
		};
	}

	class Output implements Renderer {
		private final ArtifactGenerator generator;
		private final SLProgressMonitor monitor;
		private boolean first = true;

		public Output(ArtifactGenerator gen, SLProgressMonitor m) {
			generator = gen;
			monitor = m;
		}

		public Writer getWriter() {
			throw new UnsupportedOperationException();
		}

		public String render(Report report) {
			throw new UnsupportedOperationException();
		}

		public void render(Writer writer, Report report) throws IOException {
			throw new UnsupportedOperationException();
		}

		public void setWriter(Writer writer) {
			throw new UnsupportedOperationException();
		}

		public void showSuppressedViolations(boolean show) {
			// Do nothing
		}

		public void start() throws IOException {
			// Do nothing
		}

		public synchronized void startFileAnalysis(DataSource dataSource) {
			String msg = "Scanning " + dataSource.getNiceFileName(false, "");
			monitor.subTask(msg);
			if (first) {
				first = false;
			} else {
				monitor.worked(1);
			}
			if (LOG.isLoggable(Level.FINE))
				LOG.fine(msg);
		}

		private String getCompUnitName(String file) {
			int separator = file.lastIndexOf(File.separatorChar);
			if (separator < 0) {
				return file.substring(0, file.length() - JAVA_SUFFIX_LEN);
			}
			return file.substring(separator + 1, file.length()
					- JAVA_SUFFIX_LEN);
		}

		public synchronized void renderFileReport(Report report)
				throws IOException {
			Iterator<IRuleViolation> it = report.iterator();
			while (it.hasNext()) {
				IRuleViolation v = it.next();
				if (LOG.isLoggable(Level.FINE)) {
					System.out.println(v.getFilename() + ": "
							+ v.getDescription());
				}
				ArtifactBuilder artifact = generator.artifact();
				SourceLocationBuilder sourceLocation = artifact
						.primarySourceLocation();

				String file = v.getFilename();
				sourceLocation.packageName(v.getPackageName());
				String cuName = getCompUnitName(file);
				sourceLocation.compilation(cuName);

				if (v.getClassName() == null || "".equals(v.getClassName())) {
					// No class name, so use the main class for the compilation
					// unit
					sourceLocation.className(cuName);
				} else {
					sourceLocation.className(v.getClassName());
				}

				String method = v.getMethodName();
				String field = v.getVariableName();
				if ("".equals(method)) {
					sourceLocation.type(IdentifierType.CLASS);
					sourceLocation.identifier(v.getClassName());
				} else if ("".equals(field)) {
					sourceLocation.type(IdentifierType.METHOD);
					sourceLocation.identifier(method);
				} else {
					sourceLocation.type(IdentifierType.FIELD);
					sourceLocation.identifier(field);
				}

				HashGenerator hashGenerator = HashGenerator.getInstance();
				Long hashValue = hashGenerator.getHash(v.getFilename(), v
						.getBeginLine());
				// FIX use v.getBeginColumn();
				// FIX use v.getEndColumn();
				sourceLocation = sourceLocation.hash(hashValue).lineOfCode(
						v.getBeginLine());
				sourceLocation = sourceLocation.endLine(v.getEndLine());

				artifact.findingType(getName(), getVersion(), v.getRule()
						.getName());
				artifact.message(v.getDescription());

				int priority = v.getRule().getPriority();
				Priority assignedPriority = getPMDPriority(priority);
				Severity assignedSeverity = getPMDSeverity(priority);
				artifact.priority(assignedPriority).severity(assignedSeverity);

				sourceLocation.build();
				artifact.build();
			}

			Iterator<ProcessingError> errors = report.errors();
			while (errors.hasNext()) {
				ProcessingError error = errors.next();
				LOG.warning(error.getFile() + ": " + error.getMsg());
				ErrorBuilder eb = generator.error();
				eb.message(error.getMsg());
				eb.tool(getName() + " v." + getVersion());
				eb.build();
			}

			/*
			 * Iterator<Metric> metrics = report.metrics(); while
			 * (metrics.hasNext()) { Metric m = metrics.next();
			 * //System.out.println(m.getMetricName()+"(total) :
			 * "+m.getTotal()); if ("NcssTypeCount".equals(m.getMetricName())) {
			 * MetricBuilder mb = generator.metric(); String fileName =
			 * report.getSource().getNiceFileName(true, inputPath); int
			 * lastSeparator = fileName.lastIndexOf(File.separatorChar); if
			 * (lastSeparator > 0) { mb.packageName(fileName.substring(0,
			 * lastSeparator).replace(File.separatorChar, '.'));
			 * mb.compilation(fileName.substring(lastSeparator+1)); } else {
			 * mb.packageName(""); mb.compilation(fileName); }
			 * mb.linesOfCode((int) m.getTotal()); System.out.println(fileName+"
			 * : "+m.getTotal()+" LOC"); // mb.build(); } }
			 */
			// System.out.println("Done with report");
		}

		public void end() throws IOException {
			// Do nothing
		}
	}

	private static Severity getPMDSeverity(int priority) {
		switch (priority) {
		case 1:
		case 2:
			return Severity.ERROR;
		case 3:
		case 4:
			return Severity.WARNING;
		case 5:
		default:
			return Severity.INFO;
		}
	}

	private static Priority getPMDPriority(int priority) {
		switch (priority) {
		case 1:
		case 3:
			return Priority.HIGH;
		case 4:
		case 2:
			return Priority.MEDIUM;
		case 5:
		default:
			return Priority.LOW;
		}
	}
}
