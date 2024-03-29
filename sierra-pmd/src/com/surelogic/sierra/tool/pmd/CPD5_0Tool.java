package com.surelogic.sierra.tool.pmd;

import java.io.File;
import java.io.IOException;
import java.util.*;

import net.sourceforge.pmd.cpd.*;
import net.sourceforge.pmd.cpd.Match;

import com.surelogic.common.jobs.SLProgressMonitor;
import com.surelogic.sierra.tool.*;
import com.surelogic.sierra.tool.ArtifactType;
import com.surelogic.sierra.tool.analyzer.ILazyArtifactGenerator;
import com.surelogic.sierra.tool.message.*;
import com.surelogic.sierra.tool.message.ArtifactGenerator.ArtifactBuilder;

public final class CPD5_0Tool extends AbstractToolInstance {
	public static final String DUPLICATED_CODE = "DuplicatedCode";

	public CPD5_0Tool(CPDToolFactory f, Config config, ILazyArtifactGenerator generator, boolean close) {
		super(f, config, generator, close);
	}

	public Set<ArtifactType> getArtifactTypes() {
		return Collections.emptySet();
	}

	@Override
	protected void execute(SLProgressMonitor monitor)
	throws Exception {
		monitor.begin(10);
		monitor.subTask("Setting up");

		// Modified from CPD.main()
		boolean skipDuplicateFiles = true;
		String languageString = "java";
		String encodingString = System.getProperty("file.encoding");
		int minimumTokens = 100;

		Language language = LanguageFactory.createLanguage(languageString);
		final CPDConfiguration config = new CPDConfiguration(); //minimumTokens, language, encodingString);					
		config.setMinimumTileSize(minimumTokens);
		config.setLanguage(language);
		config.setEncoding(encodingString);
		config.setSkipDuplicates(skipDuplicateFiles);
		final CPD cpd = new CPD(config);

		cpd.setCpdListener(new CPDListener() {
			@Override
      public void addedFile(int fileCount, File file) {
				// System.out.println(fileCount+": "+file.getName());
			}

			@Override
      public void phaseUpdate(int phase) {
				System.out.println("CPD Phase " + phase);
			}
		});
		final SourceRoots roots = collectSourceRoots(new TargetPrep() {
			@Override
      public void prep(File f) throws IOException {				
				cpd.add(f);
			}}
		);

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
	}

	protected void createArtifact(SourceRoots roots,
			ArtifactGenerator generator, Match m) {
		ArtifactBuilder artifact = generator.artifact();
		Mark firstMark = m.getFirstMark();
		setSourceLocation(artifact.primarySourceLocation(),
				createSrcInfo(roots, firstMark.getToken(), m.getLineCount()));
		for (Mark mark : m.getMarkSet()) {
			if (mark.getToken().getIndex() != firstMark.getToken().getIndex()) {
				setSourceLocation(artifact.sourceLocation(),
						createSrcInfo(roots, mark.getToken(), m.getLineCount()));
			}
		}

		artifact.findingType(getName(), getVersion(), DUPLICATED_CODE);
		artifact.message("Found " + m.getMarkCount()
				+ " instances of the same " + m.getLineCount()
				+ " lines");
		artifact.priority(Priority.HIGH).severity(Severity.WARNING);
		artifact.build();
	}

	private SourceInfo createSrcInfo(SourceRoots roots, TokenEntry mark, int lineCount) {
		SourceInfo info = new SourceInfo();
		roots.initSourceInfo(info, mark.getTokenSrcID());
		info.startLine = mark.getBeginLine();
		info.endLine = mark.getBeginLine() + lineCount;
		return info;
	}
}
