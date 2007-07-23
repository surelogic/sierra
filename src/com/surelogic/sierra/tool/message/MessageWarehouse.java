package com.surelogic.sierra.tool.message;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import com.surelogic.sierra.tool.SierraLogger;
import com.surelogic.sierra.tool.analyzer.ArtifactGenerator;
import com.surelogic.sierra.tool.analyzer.ArtifactGenerator.ArtifactBuilder;
import com.surelogic.sierra.tool.analyzer.ArtifactGenerator.ErrorBuilder;

/**
 * General utility class for working with the sps message layer.
 * 
 * @author nathan
 * 
 */
public class MessageWarehouse {

	private static final Logger log = SierraLogger
			.getLogger(MessageWarehouse.class.getName());

	private static final MessageWarehouse INSTANCE = new MessageWarehouse();

	private final JAXBContext ctx;

	private MessageWarehouse() {
		try {
			this.ctx = JAXBContext.newInstance(ToolOutput.class);
		} catch (JAXBException e) {
			throw new IllegalStateException(e);
		}
	}

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

	public static MessageWarehouse getInstance() {
		return INSTANCE;
	}

	/**
	 * Write a {@link ToolOutput} object to the specified file destination.
	 * 
	 * @param to
	 * @param dest
	 *            a path name
	 */
	public void writeToolOutput(ToolOutput to, String dest) {
		FileWriter out;
		try {
			out = new FileWriter(dest);
			ctx.createMarshaller().marshal(to, out);
			out.close();
		} catch (IOException e) {
			log.log(Level.SEVERE,
					"Error writing parser output to file " + dest, e);
		} catch (JAXBException e) {
			log.log(Level.SEVERE, "Error marshalling parser output to file "
					+ dest, e);
		}
	}

	/**
	 * Return the {@link ToolOutput} object located at src.
	 * 
	 * @param src
	 *            a path name
	 * @return a {@link ToolOutput} object, or null if none can be parsed at
	 *         src.
	 */
	public ToolOutput fetchToolOutput(String src) {
		try {
			Unmarshaller unmarshaller = ctx.createUnmarshaller();
			return (ToolOutput) unmarshaller.unmarshal(new File(src));
		} catch (JAXBException e) {
			log.log(Level.WARNING, "Could not fetch " + src, e);
		}
		return null;
	}

	/**
	 * Parse the tool output at the specified source.
	 * 
	 * @param src
	 */
	public void parseToolOutput(String src, ArtifactGenerator generator) {
		try {
			Unmarshaller unmarshaller = ctx.createUnmarshaller();
			try {
				readToolOutput((ToolOutput) unmarshaller
						.unmarshal(new File(src)), generator);
			} catch (JAXBException e) {
				log.log(Level.WARNING, "Could not unmarshal " + src, e);
			}
		} catch (JAXBException e) {
			throw new IllegalStateException(e);
		}
	}

	/**
	 * Parse the tool output at the specified sources.
	 * 
	 * @param src
	 */
	public void parseToolOutput(Collection<String> sources,
			ArtifactGenerator generator) {
		Unmarshaller unmarshaller;
		try {
			unmarshaller = ctx.createUnmarshaller();
			for (String src : sources) {
				try {
					readToolOutput((ToolOutput) unmarshaller
							.unmarshal(new File(src)), generator);
				} catch (JAXBException e) {
					log.log(Level.WARNING, "Could not unmarshal " + src, e);
				}
			}
		} catch (JAXBException e) {
			throw new IllegalStateException(e);
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
