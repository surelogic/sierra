package com.surelogic.sierra.tool.message;

import static javax.xml.stream.XMLStreamConstants.CHARACTERS;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import com.surelogic.common.SLProgressMonitor;
import com.surelogic.sierra.tool.SierraLogger;
import com.surelogic.sierra.tool.analyzer.ArtifactGenerator;
import com.surelogic.sierra.tool.analyzer.RunGenerator;
import com.surelogic.sierra.tool.analyzer.ArtifactGenerator.ArtifactBuilder;
import com.surelogic.sierra.tool.analyzer.ArtifactGenerator.ErrorBuilder;
import com.surelogic.sierra.tool.config.Config;

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
			this.ctx = JAXBContext.newInstance(Run.class);
		} catch (JAXBException e) {
			throw new IllegalStateException(e);
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
			Marshaller m = ctx.createMarshaller();
			m.setProperty("jaxb.formatted.output", true);// TODO make this
			// configurable
			m.marshal(to, out);
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
	 * Write a {@link Artifact} object to the specified file destination.
	 * 
	 * @param to
	 * @param dest
	 *            a path name
	 */
	public void writeArtifact(Artifact a, FileOutputStream artOut) {

		try {
			Marshaller marshaller = ctx.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FRAGMENT, new Boolean(true));
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT,
					new Boolean(true));
			marshaller.marshal(a, artOut);
		} catch (JAXBException e) {
			log.log(Level.SEVERE, "Error marshalling parser output to file "
					+ e);
		}
	}

	public void writeConfig(Config config, FileOutputStream artOut) {
		try {
			Marshaller marshaller = ctx.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FRAGMENT, new Boolean(true));
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT,
					new Boolean(true));
			marshaller.marshal(config, artOut);
		} catch (JAXBException e) {
			log.log(Level.SEVERE, "Error marshalling parser output to file "
					+ e);
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
			return fetchToolOutput(new FileInputStream(src));
		} catch (FileNotFoundException e) {
			throw new IllegalArgumentException(e);
		}
	}

	public ToolOutput fetchToolOutput(InputStream in) {
		try {
			Unmarshaller unmarshaller = ctx.createUnmarshaller();
			return (ToolOutput) unmarshaller.unmarshal(in);
		} catch (JAXBException e) {
			log.log(Level.WARNING, "Could not fetch tool output.", e);
		}
		return null;
	}

	/**
	 * Return the {@link Run} object located at src.
	 * 
	 * @param src
	 *            a path name
	 * @return a {@link Run} object, or null if none can be parsed at src.
	 */
	public Run fetchRun(String src) {
		try {
			return fetchRun(new FileInputStream(src));
		} catch (FileNotFoundException e) {
			throw new IllegalArgumentException(e);
		}
	}

	public Run fetchRun(InputStream in) {
		try {
			Unmarshaller unmarshaller = ctx.createUnmarshaller();
			return (Run) unmarshaller.unmarshal(in);
		} catch (JAXBException e) {
			log.log(Level.WARNING, "Could not fetch tool output.", e);
		}
		return null;
	}

	public void parseRunDocument(final File runDocument,
			RunGenerator generator, SLProgressMonitor monitor) {
		try {
			Unmarshaller um = ctx.createUnmarshaller();
			try {
				// set up a parser
				XMLInputFactory xmlif = XMLInputFactory.newInstance();
				XMLStreamReader xmlr = xmlif
						.createXMLStreamReader(new FileReader(runDocument));
				try {
					// move to the root element and check its name.
					xmlr.nextTag();
					xmlr.require(START_ELEMENT, null, "run");
					xmlr.nextTag(); // move to uid element
					xmlr.require(START_ELEMENT, null, "uid");
					generator.uid(um.unmarshal(xmlr, String.class).getValue());
					xmlr.nextTag(); // move to toolOutput element.
					xmlr.nextTag(); // move to artifacts (or config, if no
					// artifacts or errors)
					// Count artifacts, so that we can estimate time until
					// completion
					int counter = 0;
					while ((xmlr.getEventType() != START_ELEMENT)
							|| !xmlr.getLocalName().equals("config")) {
						if (xmlr.getEventType() == START_ELEMENT) {
							counter++;
						}
						xmlr.next();
					}
					if (monitor != null) {
						monitor.beginTask("Generating Artifacts", counter);
					}
					readConfig(um.unmarshal(xmlr, Config.class).getValue(),
							generator);

				} catch (JAXBException e) {
					throw new IllegalArgumentException("File with name"
							+ runDocument.getName()
							+ " is not a valid document", e);
				}
				xmlr.close();
			} catch (FileNotFoundException e) {
				throw new IllegalArgumentException("File with name"
						+ runDocument.getName() + " does not exist.", e);
			} catch (XMLStreamException e) {
				throw new IllegalArgumentException(e);
			}
		} catch (JAXBException e) {
			throw new IllegalStateException(e);
		}
		parseRunDocument(runDocument, generator.build(), monitor);
	}

	public void parseRunDocument(final File runDocument,
			ArtifactGenerator generator, SLProgressMonitor monitor) {
		try {
			Unmarshaller um = ctx.createUnmarshaller();
			try {
				// set up a parser
				XMLInputFactory xmlif = XMLInputFactory.newInstance();
				XMLStreamReader xmlr = xmlif
						.createXMLStreamReader(new FileReader(runDocument));

				try {
					// move to the root element and check its name.
					xmlr.nextTag();
					xmlr.require(START_ELEMENT, null, "run");
					xmlr.nextTag(); // move to uid element
					xmlr.require(START_ELEMENT, null, "uid");
					um.unmarshal(xmlr, String.class);
					xmlr.nextTag(); // move to toolOutput element.
					xmlr.nextTag(); // move to artifacts
					// Unmarshal artifacts
					ArtifactBuilder aBuilder = generator.artifact();
					while (xmlr.getEventType() == START_ELEMENT
							&& xmlr.getLocalName().equals("artifact")) {
						readArtifact(um.unmarshal(xmlr, Artifact.class)
								.getValue(), aBuilder);
						if (monitor != null) {
							monitor.worked(1);
						}
						if (xmlr.getEventType() == CHARACTERS) {
							xmlr.next(); // skip the whitespace between
							// <artifacts>s.
						}
					}
					// Unmarshal errors
					ErrorBuilder eBuilder = generator.error();
					while (xmlr.getEventType() == START_ELEMENT
							&& xmlr.getLocalName().equals("errors")) {
						readError(um.unmarshal(xmlr, Error.class).getValue(),
								eBuilder);
						if (monitor != null) {
							monitor.worked(1);
						}
						if (xmlr.getEventType() == CHARACTERS) {
							xmlr.next(); // skip the whitespace between
							// <event>s.
						}
					}
					generator.finished();
					if (monitor != null) {
						monitor.done();
					}
				} catch (JAXBException e) {
					throw new IllegalArgumentException("File with name"
							+ runDocument.getName()
							+ " is not a valid document", e);
				}
				xmlr.close();
			} catch (FileNotFoundException e) {
				throw new IllegalArgumentException("File with name"
						+ runDocument.getName() + " does not exist.", e);
			} catch (XMLStreamException e) {
				throw new IllegalArgumentException(e);
			}
		} catch (JAXBException e) {
			throw new IllegalStateException(e);
		}
	}

	public static void readRun(Run run, RunGenerator generator) {
		readConfig(run.getConfig(), generator);
		ArtifactGenerator aGen = generator.build();
		readArtifacts(run.getToolOutput().getArtifact(), aGen);
		readErrors(run.getToolOutput().getErrors(), aGen);
		aGen.finished();
	}

	// TODO Having these methods be public static is probably not the best way
	// to do this for RunManager, we need rework MessageWarehouse to work on
	// in-memory runs as well.
	private static void readArtifacts(Collection<Artifact> artifacts,
			ArtifactGenerator generator) {
		if (artifacts != null) {
			ArtifactBuilder aBuilder = generator.artifact();
			for (Artifact a : artifacts) {
				readArtifact(a, aBuilder);
			}
		}
	}

	private static void readErrors(Collection<Error> errors,
			ArtifactGenerator generator) {
		if (errors != null) {
			ErrorBuilder eBuilder = generator.error();
			for (Error e : errors) {
				readError(e, eBuilder);
			}
		}
	}

	private static void readConfig(Config config, RunGenerator builder) {
		builder.javaVendor(config.getJavaVendor());
		builder.javaVersion(config.getJavaVersion());
		builder.project(config.getProject());
		builder.qualifiers(config.getQualifiers());
		// TODO read all config attributes
	}

	private static void readArtifact(Artifact artifact, ArtifactBuilder builder) {
		builder.severity(artifact.getSeverity()).priority(
				artifact.getPriority()).message(artifact.getMessage());
		builder.findingType(artifact.getFindingType().getTool(), artifact
				.getFindingType().getVersion(), artifact.getFindingType()
				.getMnemonic());
		readPrimarySource(builder, artifact.getPrimarySourceLocation());
		Collection<SourceLocation> sources = artifact.getAdditionalSources();
		if (sources != null) {
			for (SourceLocation sl : sources) {
				readSource(builder, sl);
			}
		}
		builder.build();
	}

	private static void readError(Error e, ErrorBuilder builder) {
		builder.message(e.getMessage()).tool(e.getTool()).build();
	}

	private static void readPrimarySource(ArtifactBuilder aBuilder,
			SourceLocation s) {
		aBuilder.primarySourceLocation().path(s.getPathName()).className(
				s.getClassName()).packageName(s.getPackageName()).endLine(
				s.getEndLineOfCode()).lineOfCode(s.getLineOfCode()).type(
				s.getIdentifierType()).identifier(s.getIdentifier()).hash(
				s.getHash()).build();
	}

	private static void readSource(ArtifactBuilder aBuilder, SourceLocation s) {
		aBuilder.sourceLocation().path(s.getPathName()).className(
				s.getClassName()).packageName(s.getPackageName()).endLine(
				s.getEndLineOfCode()).lineOfCode(s.getLineOfCode()).type(
				s.getIdentifierType()).identifier(s.getIdentifier()).hash(
				s.getHash()).build();
	}

}
