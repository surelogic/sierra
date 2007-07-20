package com.surelogic.sierra.message;

import java.util.Collection;

import com.surelogic.sierra.tool.analyzer.ArtifactGenerator;
import com.surelogic.sierra.tool.analyzer.ArtifactGenerator.ArtifactBuilder;

/**
 * General utility class for working with the sps message layer.
 * 
 * @author nathan
 * 
 */
public class Messages {

	public static void readArtifacts(Collection<Artifact> artifacts,
			ArtifactGenerator generator) {
		if (artifacts != null) {
			ArtifactBuilder aBuilder = generator.artifact();
			for (Artifact a : artifacts) {
				aBuilder.severity(a.getSeverity()).priority(a.getPriority())
						.message(a.getMessage());
				aBuilder.findingType(a.getFindingType().getTool(), a
						.getFindingType().getMnemonic());
				readSource(aBuilder, a.getPrimarySourceLocation(), generator);
				Collection<SourceLocation> sources = a.getAdditionalSources();
				if (sources != null) {
					for (SourceLocation sl : sources) {
						readSource(aBuilder, sl, generator);
					}
				}
			}
		}
	}

	private static void readSource(ArtifactBuilder aBuilder, SourceLocation s,
			ArtifactGenerator generator) {
		aBuilder.primarySourceLocation().path(s.getPathName()).className(
				s.getClassName()).packageName(s.getPackageName()).endLine(
				s.getEndLineOfCode()).lineOfCode(s.getLineOfCode()).type(
				s.getIdentifierType()).identifier(s.getIdentifier()).hash(
				s.getHash());
	}

}
