package com.surelogic.sierra.tool.message;

import java.util.Collection;

import com.surelogic.sierra.tool.analyzer.ArtifactGenerator;
import com.surelogic.sierra.tool.analyzer.ArtifactGenerator.ArtifactBuilder;
import com.surelogic.sierra.tool.analyzer.ArtifactGenerator.ErrorBuilder;

/**
 * General utility class for working with the sps message layer.
 * 
 * @author nathan
 * 
 */
public class Messages {

	public static void readToolOutput(ToolOutput output,
			ArtifactGenerator generator) {
		readArtifacts(output.getArtifacts(), generator);
		readErrors(output.getErrors(), generator);
	}

	public static void readArtifacts(Collection<Artifact> artifacts,
			ArtifactGenerator generator) {
		if (artifacts != null) {
			ArtifactBuilder aBuilder = generator.artifact();
			for (Artifact a : artifacts) {
				aBuilder.severity(a.getSeverity()).priority(a.getPriority())
						.message(a.getMessage());
				aBuilder.findingType(a.getFindingType().getTool(), a
						.getFindingType().getMnemonic());
				readPrimarySource(aBuilder, a.getPrimarySourceLocation(),
						generator);
				Collection<SourceLocation> sources = a.getAdditionalSources();
				if (sources != null) {
					for (SourceLocation sl : sources) {
						readSource(aBuilder, sl, generator);
					}
				}
				aBuilder.build();
			}
		}
	}

	public static void readErrors(Collection<Error> errors,
			ArtifactGenerator generator) {
		if (errors != null) {
			ErrorBuilder eBuilder = generator.error();
			for (Error e : errors) {
				eBuilder.message(e.getMessage()).tool(e.getTool()).build();
			}
		}
	}

	private static void readPrimarySource(ArtifactBuilder aBuilder,
			SourceLocation s, ArtifactGenerator generator) {
		aBuilder.primarySourceLocation().path(s.getPathName()).className(
				s.getClassName()).packageName(s.getPackageName()).endLine(
				s.getEndLineOfCode()).lineOfCode(s.getLineOfCode()).type(
				s.getIdentifierType()).identifier(s.getIdentifier()).hash(
				s.getHash()).build();
	}

	private static void readSource(ArtifactBuilder aBuilder, SourceLocation s,
			ArtifactGenerator generator) {
		aBuilder.sourceLocation().path(s.getPathName()).className(
				s.getClassName()).packageName(s.getPackageName()).endLine(
				s.getEndLineOfCode()).lineOfCode(s.getLineOfCode()).type(
				s.getIdentifierType()).identifier(s.getIdentifier()).hash(
				s.getHash()).build();
	}

}
