package com.surelogic.sierra.tool.pmd;

import java.io.File;
import java.net.URI;
import java.util.*;

import net.sourceforge.pmd.cpd.*;

import com.surelogic.common.jobs.SLProgressMonitor;
import com.surelogic.common.jobs.SLStatus;
import com.surelogic.sierra.tool.AbstractTool;
import com.surelogic.sierra.tool.AbstractToolInstance;
import com.surelogic.sierra.tool.ArtifactType;
import com.surelogic.sierra.tool.IToolInstance;
import com.surelogic.sierra.tool.SierraToolConstants;
import com.surelogic.sierra.tool.analyzer.ILazyArtifactGenerator;
import com.surelogic.sierra.tool.message.ArtifactGenerator;
import com.surelogic.sierra.tool.message.Config;
import com.surelogic.sierra.tool.message.Priority;
import com.surelogic.sierra.tool.message.Severity;
import com.surelogic.sierra.tool.message.ArtifactGenerator.ArtifactBuilder;
import com.surelogic.sierra.tool.targets.*;

public abstract class AbstractCPDTool extends AbstractTool {
	public AbstractCPDTool(String version, Config config) {
		super("CPD", version, "CPD", "", config);
	}

	public Set<ArtifactType> getArtifactTypes() {
	  return Collections.emptySet();
	}
	
	@Override
	public List<File> getRequiredJars() {
		final List<File> jars = new ArrayList<File>();				
		addAllPluginJarsToPath(debug, jars,	SierraToolConstants.PMD_PLUGIN_ID, "lib");
		return jars;
	}
	
	@Override
	protected IToolInstance create(String name, ILazyArtifactGenerator generator, boolean close) {
		return new AbstractToolInstance(debug, this, generator, close) {
			@Override
			protected SLStatus execute(SLProgressMonitor monitor)
					throws Exception {
				monitor.begin(10);
				monitor.subTask("Setting up");

				// Modified from CPD.main()
				boolean skipDuplicateFiles = true;
				String languageString = "java";
				String encodingString = System.getProperty("file.encoding");
				int minimumTokens = 100;

				LanguageFactory f = new LanguageFactory();
				Language language = f.createLanguage(languageString);
				CPD cpd = new CPD(minimumTokens, language);
				cpd.setEncoding(encodingString);
				if (skipDuplicateFiles) {
					cpd.skipDuplicates();
				}
				cpd.setCpdListener(new CPDListener() {
					public void addedFile(int fileCount, File file) {
						// System.out.println(fileCount+": "+file.getName());
					}

					public void phaseUpdate(int phase) {
						System.out.println("CPD Phase " + phase);
					}
				});

				Map<String, String> roots = new HashMap<String, String>();
				for (IToolTarget t : getSrcTargets()) {
					File location = new File(t.getLocation());
					String locName = location.getAbsolutePath();
					if (t instanceof FileTarget) {
						FileTarget ft = (FileTarget) t;
						if (!location.exists()
								|| !location.getName().endsWith(".java")) {
							continue;
						}
						if (ft.getRoot() != null) {
							String root = new File(ft.getRoot())
									.getAbsolutePath();
							roots.put(locName, root);
						} else {
							System.out.println("No root for " + locName);
						}
						cpd.add(location);
					} else
						for (URI loc : t.getFiles()) {
							File file = new File(loc);
							if (file.exists()
									&& file.getName().endsWith(".java")) {
								roots.put(file.getAbsolutePath(), locName);
								cpd.add(file);
							}
						}
				}
				monitor.worked(1);
				monitor.subTask("Scanning source files");
				cpd.go();
				monitor.worked(1);
				monitor.subTask("Creating artifacts based on matches");
				Iterator<Match> matches = cpd.getMatches();
				while (matches.hasNext()) {
					Match m = matches.next();
					createArtifact(roots, getGenerator(), m);
				}
				monitor.worked(1);
				return SLStatus.OK_STATUS;
			}

			protected void createArtifact(Map<String, String> roots,
					ArtifactGenerator generator, Match m) {
				ArtifactBuilder artifact = generator.artifact();
				TokenEntry firstMark = m.getFirstMark();
				setSourceLocation(artifact.primarySourceLocation(),
						new SrcInfo(roots, firstMark, m.getLineCount()));
				for (TokenEntry mark : m.getMarkSet()) {
					if (mark.getIndex() != firstMark.getIndex()) {
						setSourceLocation(artifact.sourceLocation(),
								new SrcInfo(roots, mark, m.getLineCount()));
					}
				}

				artifact.findingType(getName(), getVersion(), "DuplicatedCode");
				artifact.message("Found " + m.getMarkCount()
						+ " instances of the same " + m.getLineCount()
						+ " lines");
				artifact.priority(Priority.HIGH).severity(Severity.WARNING);
				artifact.build();
			}

			class SrcInfo extends SourceInfo {
				SrcInfo(Map<String, String> roots, TokenEntry mark,
						int lineCount) {
					fileName = mark.getTokenSrcID();

					final String root = roots.get(fileName);
					if (root == null) {
						throw new IllegalArgumentException(fileName
								+ " doesn't have a source root");
					}
					final String file;
					if (fileName.startsWith(root)) {
						file = fileName.substring(root.length() + 1);
					} else {
						throw new IllegalArgumentException(fileName
								+ " start with root " + root);
					}

					// Modified from AbstractPMDTool.getCompUnitName()
					int separator = file.lastIndexOf(File.separatorChar);
					if (separator < 0) {
						// Default package
						packageName = "";
						cuName = file.substring(0, file.length()
								- JAVA_SUFFIX_LEN);
					} else {
						packageName = file.substring(0, separator).replace(
								File.separatorChar, '.');
						cuName = file.substring(separator + 1, file.length()
								- JAVA_SUFFIX_LEN);
					}
					startLine = mark.getBeginLine();
					endLine = mark.getBeginLine() + lineCount;
				}
			}
		};
	}
}
