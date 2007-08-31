package com.surelogic.sierra.tool.message;

import static javax.xml.stream.XMLStreamConstants.CHARACTERS;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
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
import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.tool.analyzer.ArtifactGenerator;
import com.surelogic.sierra.tool.analyzer.MetricBuilder;
import com.surelogic.sierra.tool.analyzer.ScanGenerator;
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

	private static final Logger log = SLLogger.getLogger(MessageWarehouse.class
			.getName());

	private static final MessageWarehouse INSTANCE = new MessageWarehouse();

	private final JAXBContext ctx;
	private final Marshaller marshaller;
	private final Unmarshaller unmarshaller;

	private MessageWarehouse() {
		try {
			this.ctx = JAXBContext.newInstance(Scan.class, Settings.class);
			marshaller = ctx.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			unmarshaller = ctx.createUnmarshaller();
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
			marshaller.marshal(to, out);
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
	 * Write a {@link ClassMetric} object to the specified output
	 * 
	 * @param metric
	 * @param out
	 */
	public void writeClassMetric(ClassMetric metric, OutputStream out) {
		try {
			marshaller.marshal(metric, out);
		} catch (JAXBException e) {
			log.log(Level.SEVERE, "Error marshalling parser output to file "
					+ e);
		}
	}

	/**
	 * Write a {@link ClassMetric} object to the specified output
	 * 
	 * @param metric
	 * @param out
	 */
	public void writeClassMetric(ClassMetric metric, Writer out) {
		try {
			marshaller.marshal(metric, out);
		} catch (JAXBException e) {
			log.log(Level.SEVERE, "Error marshalling parser output to file "
					+ e);
		}
	}

	/**
	 * Write a {@link Error} object to the specified output..
	 * 
	 * @param error
	 * @param out
	 */
	public void writeError(Error error, OutputStream out) {
		try {
			marshaller.marshal(error, out);
		} catch (JAXBException e) {
			log.log(Level.SEVERE, "Error marshalling parser output to file "
					+ e);
		}

	}

	/**
	 * Write a {@link Artifact} object to the specified output..
	 * 
	 * @param a
	 * @param out
	 */
	public void writeArtifact(Artifact a, OutputStream out) {
		try {
			marshaller.marshal(a, out);
		} catch (JAXBException e) {
			log.log(Level.SEVERE, "Error marshalling parser output to file "
					+ e);
		}
	}

	/**
	 * Write a {@link Artifact} object to the specified output..
	 * 
	 * @param a
	 * @param out
	 */
	public void writeArtifact(Artifact a, Writer out) {
		try {
			marshaller.marshal(a, out);
		} catch (JAXBException e) {
			log.log(Level.SEVERE, "Error marshalling parser output to file "
					+ e);
		}
	}

	public void writeConfig(Config config, OutputStream artOut) {
		try {
			marshaller.marshal(config, artOut);
		} catch (JAXBException e) {
			log.log(Level.SEVERE, "Error marshalling parser output to file "
					+ e);
		}
	}

	public void writeConfig(Config config, Writer artOut) {
		try {
			marshaller.marshal(config, artOut);
		} catch (JAXBException e) {
			log.log(Level.SEVERE, "Error marshalling parser output to file "
					+ e);
		}
	}

	public void writeSettings(Settings settings, OutputStream out) {
		try {
			marshaller.marshal(settings, out);
		} catch (JAXBException e) {
			log.log(Level.SEVERE, "Error marshalling parser output to file "
					+ e);
		}
	}

	public void writeSettings(Settings settings, Writer out) {
		try {
			marshaller.marshal(settings, out);
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
			return (ToolOutput) unmarshaller.unmarshal(in);
		} catch (JAXBException e) {
			log.log(Level.WARNING, "Could not fetch tool output.", e);
		}
		return null;
	}

	public ToolOutput fetchToolOutput(Reader in) {
		try {
			return (ToolOutput) unmarshaller.unmarshal(in);
		} catch (JAXBException e) {
			log.log(Level.WARNING, "Could not fetch tool output.", e);
		}
		return null;
	}

	/**
	 * Return the {@link Scan} object located at src.
	 * 
	 * @param src
	 *            a path name
	 * @return a {@link Scan} object, or null if none can be parsed at src.
	 */
	public Scan fetchScan(String src) {
		try {
			return fetchScan(new FileInputStream(src));
		} catch (FileNotFoundException e) {
			throw new IllegalArgumentException(e);
		}
	}

	public Scan fetchScan(InputStream in) {
		try {
			return (Scan) unmarshaller.unmarshal(in);
		} catch (JAXBException e) {
			log.log(Level.WARNING, "Could not fetch tool output.", e);
		}
		return null;
	}

	public Scan fetchScan(Reader in) {
		try {
			return (Scan) unmarshaller.unmarshal(in);
		} catch (JAXBException e) {
			log.log(Level.WARNING, "Could not fetch tool output.", e);
		}
		return null;
	}

	public Settings fetchSettings(String src) {
		try {
			return (Settings) fetchSettings(new FileInputStream(src));
		} catch (FileNotFoundException e) {
			log.log(Level.WARNING, "Could not fecth settings output", e);
		}
		return null;
	}

	public Settings fetchSettings(InputStream in) {
		try {
			return (Settings) unmarshaller.unmarshal(in);
		} catch (JAXBException e) {
			log.log(Level.WARNING, "Could not fecth settings output", e);
		}
		return null;
	}

	public Settings fetchSettings(Reader reader) {
		try {
			return (Settings) unmarshaller.unmarshal(reader);
		} catch (JAXBException e) {
			log.log(Level.WARNING, "Could not fecth settings output", e);
		}
		return null;
	}

	public void parseScanDocument(final File runDocument,
			ScanGenerator generator, SLProgressMonitor monitor) {
		try {
			// set up a parser
			XMLInputFactory xmlif = XMLInputFactory.newInstance();
			XMLStreamReader xmlr = xmlif.createXMLStreamReader(new FileReader(
					runDocument));
			try {
				// move to the root element and check its name.
				xmlr.nextTag();
				xmlr.require(START_ELEMENT, null, "scan");
				xmlr.nextTag(); // move to uid element
				xmlr.require(START_ELEMENT, null, "uid");
				generator.uid(unmarshaller.unmarshal(xmlr, String.class)
						.getValue());
				xmlr.nextTag(); // move to toolOutput element.
				xmlr.nextTag(); // move to artifacts (or config, if no
				// artifacts, errors, or classMetrics)
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
				readConfig(unmarshaller.unmarshal(xmlr, Config.class)
						.getValue(), generator);

			} catch (JAXBException e) {
				throw new IllegalArgumentException("File with name"
						+ runDocument.getName() + " is not a valid document", e);
			}
			xmlr.close();
		} catch (FileNotFoundException e) {
			throw new IllegalArgumentException("File with name "
					+ runDocument.getName() + " does not exist.", e);
		} catch (XMLStreamException e) {
			throw new IllegalArgumentException(e);
		}
		parseScanDocument(runDocument, generator.build(), monitor);
	}

	private void parseScanDocument(final File runDocument,
			ArtifactGenerator generator, SLProgressMonitor monitor) {
		try {
			// set up a parser
			XMLInputFactory xmlif = XMLInputFactory.newInstance();
			XMLStreamReader xmlr = xmlif.createXMLStreamReader(new FileReader(
					runDocument));

			try {
				// move to the root element and check its name.
				xmlr.nextTag();
				xmlr.require(START_ELEMENT, null, "scan");
				xmlr.nextTag(); // move to uid element
				xmlr.require(START_ELEMENT, null, "uid");
				unmarshaller.unmarshal(xmlr, String.class);
				xmlr.nextTag(); // move to toolOutput element.
				xmlr.next(); // move to metrics
				xmlr.require(START_ELEMENT, null, "metrics");
				xmlr.nextTag(); // move to classMetric
				// Unmarshal classMetric
				MetricBuilder mBuilder = generator.metric();
				while (xmlr.getEventType() == START_ELEMENT
						&& xmlr.getLocalName().equals("class")) {
					readClassMetric(unmarshaller.unmarshal(xmlr,
							ClassMetric.class).getValue(), mBuilder);
					if (monitor != null) {
						monitor.worked(1);
					}
					if (xmlr.getEventType() == CHARACTERS) {
						xmlr.next(); // skip the whitespace between
						// <artifacts>s.
					}
				}

				xmlr.nextTag();
				xmlr.require(START_ELEMENT, null, "artifacts");
				xmlr.nextTag();
				// Unmarshal artifacts
				ArtifactBuilder aBuilder = generator.artifact();
				while (xmlr.getEventType() == START_ELEMENT
						&& xmlr.getLocalName().equals("artifact")) {
					readArtifact(unmarshaller.unmarshal(xmlr, Artifact.class)
							.getValue(), aBuilder);
					if (monitor != null) {
						monitor.worked(1);
					}
					if (xmlr.getEventType() == CHARACTERS) {
						xmlr.next(); // skip the whitespace between
						// <artifacts>s.
					}
				}
				xmlr.nextTag();
				xmlr.require(START_ELEMENT, null, "errors");
				xmlr.nextTag();
				// Unmarshal errors
				ErrorBuilder eBuilder = generator.error();
				while (xmlr.getEventType() == START_ELEMENT
						&& xmlr.getLocalName().equals("errors")) {
					readError(unmarshaller.unmarshal(xmlr, Error.class)
							.getValue(), eBuilder);
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
						+ runDocument.getName() + " is not a valid document", e);
			}
			xmlr.close();
		} catch (FileNotFoundException e) {
			throw new IllegalArgumentException("File with name"
					+ runDocument.getName() + " does not exist.", e);
		} catch (XMLStreamException e) {
			throw new IllegalArgumentException(e);
		}
	}

	public static void readScan(Scan scan, ScanGenerator generator) {
		generator.uid(scan.getUid());
		readConfig(scan.getConfig(), generator);
		ArtifactGenerator aGen = generator.build();
		readMetrics(scan.getToolOutput().getMetrics().getClassMetric(), aGen);
		readArtifacts(scan.getToolOutput().getArtifacts().getArtifact(), aGen);
		readErrors(scan.getToolOutput().getErrors().getErrors(), aGen);
		aGen.finished();
	}

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

	private static void readMetrics(Collection<ClassMetric> metrics,
			ArtifactGenerator generator) {
		if (metrics != null) {
			MetricBuilder mBuilder = generator.metric();
			for (ClassMetric m : metrics) {
				readClassMetric(m, mBuilder);
			}
		}
	}

	private static void readConfig(Config config, ScanGenerator builder) {
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

	private static void readClassMetric(ClassMetric metric,
			MetricBuilder builder) {
		builder.className(metric.getName()).packageName(metric.getPackage())
				.linesOfCode(metric.getLoc()).path(metric.getPath()).build();
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
