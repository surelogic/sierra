package com.surelogic.sierra.tool.message;

import static javax.xml.stream.XMLStreamConstants.CHARACTERS;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.Collection;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import com.surelogic.common.FileUtility;
import com.surelogic.common.jobs.SLProgressMonitor;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.tool.message.ArtifactGenerator.ArtifactBuilder;
import com.surelogic.sierra.tool.message.ArtifactGenerator.ErrorBuilder;
import com.surelogic.sierra.tool.targets.FileTarget;
import com.surelogic.sierra.tool.targets.FilteredDirectoryTarget;
import com.surelogic.sierra.tool.targets.FullDirectoryTarget;
import com.surelogic.sierra.tool.targets.JarTarget;
import com.surelogic.sierra.tool.targets.ToolTarget;

/**
 * General utility class for working with the sps message layer.
 *
 * @author nathan
 *
 */
public final class MessageWarehouse {
    public static final String TOOL_STREAM_SUFFIX = ".tool.xml";
    public static final String CONFIG_STREAM_NAME = "config.xml";

    private static final Logger log = SLLogger.getLogger(MessageWarehouse.class
            .getName());
    private static final MessageWarehouse INSTANCE = new MessageWarehouse();
    private static final int COUNT = 1000;
    private final JAXBContext ctx;
    private final Marshaller marshaller;
    private final Unmarshaller unmarshaller;

    private MessageWarehouse() {
        try {
            ctx = JAXBContext.newInstance(Scan.class, ScanFilter.class,
                    FilterSet.class, FindingTypes.class, Config.class,
                    KeyValuePair.class, ToolTarget.class, FileTarget.class,
                    JarTarget.class, Error.class, FullDirectoryTarget.class,
                    FilteredDirectoryTarget.class);
            marshaller = ctx.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            unmarshaller = ctx.createUnmarshaller();
        } catch (final JAXBException e) {
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
    public void writeToolOutput(final ToolOutput to, final String dest) {
        FileWriter out;
        try {
            out = new FileWriter(dest);
            marshaller.marshal(to, out);
            out.close();
        } catch (final IOException e) {
            log.log(Level.SEVERE,
                    "Error writing parser output to file " + dest, e);
        } catch (final JAXBException e) {
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
    public void writeClassMetric(final ClassMetric metric,
            final OutputStream out) {
        try {
            marshaller.marshal(metric, out);
        } catch (final JAXBException e) {
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
    public void writeClassMetric(final ClassMetric metric, final Writer out) {
        try {
            marshaller.marshal(metric, out);
        } catch (final JAXBException e) {
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
    public void writeError(final Error error, final OutputStream out) {
        try {
            marshaller.marshal(error, out);
        } catch (final JAXBException e) {
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
    public void writeArtifact(final Artifact a, final OutputStream out) {
        try {
            marshaller.marshal(a, out);
        } catch (final JAXBException e) {
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
    public void writeArtifact(final Artifact a, final Writer out) {
        try {
            marshaller.marshal(a, out);
        } catch (final JAXBException e) {
            log.log(Level.SEVERE, "Error marshalling parser output to file "
                    + e);
        }
    }

    public void writeConfig(final Config config, final OutputStream artOut) {
        try {
            marshaller.marshal(config, artOut);
        } catch (final JAXBException e) {
            log.log(Level.SEVERE, "Error marshalling parser output to file "
                    + e);
        }
    }

    public void writeConfig(final Config config, final Writer artOut) {
        try {
            marshaller.marshal(config, artOut);
        } catch (final JAXBException e) {
            log.log(Level.SEVERE, "Error marshalling parser output to file "
                    + e);
        }
    }

    public void writeFindingTypes(final FindingTypes types,
            final OutputStream out) {
        try {
            marshaller.marshal(types, out);
        } catch (final JAXBException e) {
            log.log(Level.SEVERE, "Error marshalling parser output to file "
                    + e);
        }
    }

    public void writeFindingTypes(final FindingTypes types, final Writer out) {
        try {
            marshaller.marshal(types, out);
        } catch (final JAXBException e) {
            log.log(Level.SEVERE, "Error marshalling parser output to file "
                    + e);
        }
    }

    /**
     * Write a message object to the output stream
     *
     * @param o
     * @param out
     * @throws JAXBException
     */
    public void writeXmlObject(final Object o, final OutputStream out)
            throws JAXBException {
        marshaller.marshal(o, out);
    }

    /**
     * Write a message object to the output stream
     *
     * @param o
     * @param out
     * @throws JAXBException
     */
    public void writeXmlObject(final Object o, final Writer out)
            throws JAXBException {
        marshaller.marshal(o, out);
    }

    /**
     * Read a message object from the input stream.
     *
     * @param in
     * @return
     * @throws JAXBException
     */
    public Object fetchXmlObject(final InputStream in) throws JAXBException {
        return unmarshaller.unmarshal(in);
    }

    /**
     * Read a message object from the reader.
     *
     * @param in
     * @return
     * @throws JAXBException
     */
    public Object fetchXmlObject(final Reader in) throws JAXBException {
        return unmarshaller.unmarshal(in);
    }

    /**
     * Return the {@link ToolOutput} object located at src.
     *
     * @param src
     *            a path name
     * @return a {@link ToolOutput} object, or null if none can be parsed at
     *         src.
     */
    public ToolOutput fetchToolOutput(final String src) {
        try {
            return fetchToolOutput(new FileInputStream(src));
        } catch (final FileNotFoundException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public ToolOutput fetchToolOutput(final InputStream in) {
        try {
            return (ToolOutput) unmarshaller.unmarshal(in);
        } catch (final JAXBException e) {
            log.log(Level.WARNING, "Could not fetch tool output.", e);
        }

        return null;
    }

    public ToolOutput fetchToolOutput(final Reader in) {
        try {
            return (ToolOutput) unmarshaller.unmarshal(in);
        } catch (final JAXBException e) {
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
    public Scan fetchScan(final String src) {
        try {
            return fetchScan(new FileInputStream(src));
        } catch (final FileNotFoundException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public Scan fetchScan(final File src, final boolean compressed) {
        try {
            final InputStream is = new FileInputStream(src);
            final Scan s = fetchScan(compressed ? new GZIPInputStream(is) : is);
            is.close();
            return s;
        } catch (final FileNotFoundException e) {
            throw new IllegalStateException(src + " is not a valid document", e);
        } catch (final IOException e) {
            throw new IllegalStateException(src + " is not a valid document", e);
        }
    }

    public Scan fetchScan(final InputStream in) {
        try {
            return (Scan) unmarshaller.unmarshal(in);
        } catch (final JAXBException e) {
            log.log(Level.WARNING, "Could not fetch tool output.", e);
        }

        return null;
    }

    public Scan fetchScan(final Reader in) {
        try {
            return (Scan) unmarshaller.unmarshal(in);
        } catch (final JAXBException e) {
            log.log(Level.WARNING, "Could not fetch tool output.", e);
        }

        return null;
    }

    public FindingTypes fetchFindingTypes(final InputStream in) {
        try {
            return (FindingTypes) unmarshaller.unmarshal(in);
        } catch (final JAXBException e) {
            log.log(Level.WARNING, "Could not fetch settings output", e);
        }

        return null;
    }

    public FindingTypes fetchFindingTypes(final Reader reader) {
        try {
            return (FindingTypes) unmarshaller.unmarshal(reader);
        } catch (final JAXBException e) {
            log.log(Level.WARNING, "Could not fetch settings output", e);
        }

        return null;
    }

    static class XMLStream {
        final String name;
        final InputStream stream;
        final XMLStreamReader xmlr;

        XMLStream(final File runDocument) throws XMLStreamException,
        IOException {
            name = runDocument.getName();
            stream = new FileInputStream(runDocument);

            // set up a parser
            final XMLInputFactory xmlif = XMLInputFactory.newInstance();
            if (runDocument.getName().endsWith(FileUtility.GZIP_SUFFIX)) {
                xmlr = xmlif.createXMLStreamReader(new GZIPInputStream(stream));
            } else {
                xmlr = xmlif.createXMLStreamReader(stream);
            }
        }

        public XMLStream(final String name, final InputStream stream)
                throws XMLStreamException {
            this.name = name;
            this.stream = stream;

            // set up a parser
            final XMLInputFactory xmlif = XMLInputFactory.newInstance();
            xmlr = xmlif.createXMLStreamReader(stream);
        }

        public void close() throws XMLStreamException, IOException {
            xmlr.close();
            stream.close();
        }
    }

    public String parseScanDocument(final File runDocument,
            final ScanGenerator generator, final SLProgressMonitor monitor) {
        if (runDocument.getName().endsWith(".zip")) {
            return parseZipScanDocument(runDocument, generator, monitor);
        }
        try {
            if (monitor != null) {
                monitor.subTask("Generating Artifacts");
            }

            final XMLStream xs = new XMLStream(runDocument);
            Config config = null;
            try {
            	config = parseScanMetadata(xs, generator, monitor);

                if (cancelled(monitor)) {
                    return null;
                }
            } finally {
                xs.close();
            }

            parseScanDocument(config, new XMLStream(runDocument), generator.build(),
                    monitor);
        } catch (final FileNotFoundException e) {
            throw new IllegalArgumentException("File with name "
                    + runDocument.getName() + " does not exist.", e);
        } catch (final XMLStreamException e) {
            throw new IllegalArgumentException(e);
        } catch (final IOException e) {
            log.severe("Error when trying to read compressed file " + e);
        }
        return generator.finished();
    }

    public String parseZipScanDocument(final File runDocument,
            final ScanGenerator generator, final SLProgressMonitor monitor) {
        try {
            final ZipFile zip = new ZipFile(runDocument);
            if (monitor != null) {
                monitor.subTask("Generating Artifacts");
            }

            final ZipEntry configEntry = zip.getEntry(CONFIG_STREAM_NAME);
            final String configName = runDocument.getName()
                    + File.separatorChar + CONFIG_STREAM_NAME;
            final XMLStream stream = new XMLStream(configName,
                    zip.getInputStream(configEntry));
            Config config = null;
            try {
                config = parseScanMetadata(stream, generator, monitor);

                if (cancelled(monitor)) {
                    return null;
                }
            } finally {
                stream.close();
            }
            if (config != null) {
                final ArtifactGenerator ag = generator.build();
                final Enumeration<? extends ZipEntry> entries = zip.entries();
                while (entries.hasMoreElements()) {
                    final ZipEntry ze = entries.nextElement();
                    final String name = ze.getName();
                    if (name.endsWith(TOOL_STREAM_SUFFIX)) {
                        final String tool = name.substring(0, name.length()
                                - TOOL_STREAM_SUFFIX.length());
                        if (config.isToolIncluded(tool)) {
                            final String id = runDocument.getName()
                                    + File.separatorChar + name;
                            final XMLStream xs = new XMLStream(id,
                                    zip.getInputStream(ze));
                            parseScanDocument(config, xs, ag, monitor);
                        }
                    }
                }
            }
            zip.close();
        } catch (final FileNotFoundException e) {
            throw new IllegalArgumentException("File with name "
                    + runDocument.getName() + " does not exist.", e);
        } catch (final XMLStreamException e) {
            throw new IllegalArgumentException(e);
        } catch (final IOException e) {
            log.log(Level.SEVERE, "Error when trying to read compressed file",
                    e);
        }
        return generator.finished();
    }

    private Config parseScanMetadata(final XMLStream xs,
            final ScanGenerator generator, final SLProgressMonitor monitor)
                    throws XMLStreamException {
        final XMLStreamReader xmlr = xs.xmlr;
        try {
            // move to the root element and check its name.
            xmlr.nextTag();
            xmlr.require(START_ELEMENT, null, "scan");
            xmlr.nextTag(); // move to uid element
            xmlr.require(START_ELEMENT, null, "uid");
            generator
            .uid(unmarshaller.unmarshal(xmlr, String.class).getValue());
            xmlr.nextTag(); // move to toolOutput element.
            xmlr.nextTag(); // move to artifacts (or config, if no
            // artifacts, errors, or classMetrics)
            // Count artifacts, so that we can estimate time until
            // completion

            while (xmlr.getEventType() != START_ELEMENT
                    || !xmlr.getLocalName().equals("config")) {
                xmlr.next();
            }

            final Config c = unmarshaller.unmarshal(xmlr, Config.class)
                    .getValue();
            readConfig(c, generator);

            if (cancelled(monitor)) {
                return null;
            } else {
                work(monitor);
            }
            return c;
        } catch (final JAXBException e) {
            /*
             * Throwable linked = e.getLinkedException(); while (linked
             * instanceof JAXBException) { JAXBException e2 = (JAXBException)
             * linked; linked = e2.getLinkedException(); }
             * linked.printStackTrace();
             */
            throw new IllegalArgumentException("File with name" + xs.name
                    + " is not a valid document", e);
        }
    }

    private void parseScanDocument(final Config config, final XMLStream xs,
            final ArtifactGenerator generator, final SLProgressMonitor monitor) {
        try {
            final XMLStreamReader xmlr = xs.xmlr;
            try {
                if (cancelled(monitor)) {
                    generator.rollback();

                    return;
                }

                try {
                    // move to the root element and check its name.
                    xmlr.nextTag();
                    xmlr.require(START_ELEMENT, null, "scan");
                    xmlr.nextTag(); // move to uid element
                    xmlr.require(START_ELEMENT, null, "uid");
                    unmarshaller.unmarshal(xmlr, String.class);
                    xmlr.nextTag(); // move to toolOutput element.
                    xmlr.next(); // move to metrics

                    int counter = 0;

                    if (xmlr.getEventType() == START_ELEMENT
                            && xmlr.getLocalName().equals("metrics")) {
                        xmlr.require(START_ELEMENT, null, "metrics");
                        xmlr.nextTag(); // move to classMetric
                        // Unmarshal classMetric

                        final MetricBuilder mBuilder = generator.metric();

                        while (xmlr.getEventType() == START_ELEMENT
                                && xmlr.getLocalName().equals("classMetric")) {
                            readClassMetric(
                                    unmarshaller.unmarshal(xmlr,
                                            ClassMetric.class).getValue(),
                                            mBuilder);

                            if (++counter == COUNT) {
                                if (cancelled(monitor)) {
                                    generator.rollback();

                                    return;
                                } else {
                                    work(monitor);
                                }

                                counter = 0;
                            }

                            if (xmlr.getEventType() == CHARACTERS) {
                                xmlr.next(); // skip the whitespace between
                                // <artifacts>s.
                            }
                        }

                        xmlr.nextTag();
                    }

                    xmlr.require(START_ELEMENT, null, "artifacts");
                    xmlr.nextTag();

                    // Unmarshal artifacts
                    final ArtifactBuilder aBuilder = generator.artifact();

                    while (xmlr.getEventType() == START_ELEMENT
                            && xmlr.getLocalName().equals("artifact")) {
                    	final boolean process = readArtifact(config, 
                                unmarshaller.unmarshal(xmlr, Artifact.class)
                                .getValue(), aBuilder);

                        if (xmlr.getEventType() == CHARACTERS) {
                            xmlr.next(); // skip the whitespace between
                            // <artifacts>s.
                        }

                        if (!process) {
                        	continue;
                        }
                        
                        if (++counter == COUNT) {
                            if (cancelled(monitor)) {
                                generator.rollback();

                                return;
                            } else {
                                work(monitor);
                            }

                            counter = 0;
                        }
                    }

                    xmlr.nextTag();

                    xmlr.require(START_ELEMENT, null, "errors");
                    xmlr.nextTag();

                    // Unmarshal errors
                    final ErrorBuilder eBuilder = generator.error();

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

                        if (++counter == COUNT) {
                            if (cancelled(monitor)) {
                                generator.rollback();

                                return;
                            } else {
                                work(monitor);
                            }

                            counter = 0;
                        }
                    }

                    if (monitor != null) {
                        monitor.subTask("Generating findings");
                    }
                } catch (final JAXBException e) {
                    throw new IllegalArgumentException("File with name"
                            + xs.name + " is not a valid document", e);
                }
            } finally {
                xs.close();
            }
        } catch (final FileNotFoundException e) {
            throw new IllegalArgumentException("File with name" + xs.name
                    + " does not exist.", e);
        } catch (final XMLStreamException e) {
            throw new IllegalArgumentException(e);
        } catch (final IOException e) {
            log.severe("Error when trying to read compressed file " + e);
        }
    }

    public static String readScan(final Scan scan, final ScanGenerator generator) {
        generator.uid(scan.getUid());
        readConfig(scan.getConfig(), generator);

        final ArtifactGenerator aGen = generator.build();
        readMetrics(scan.getToolOutput().getMetrics().getClassMetric(), aGen);
        readArtifacts(scan.getConfig(), scan.getToolOutput().getArtifacts().getArtifact(), aGen);
        readErrors(scan.getToolOutput().getErrors().getError(), aGen);

        return generator.finished();
    }

    private static void readArtifacts(final Config c, final Collection<Artifact> artifacts,
            final ArtifactGenerator generator) {
        if (artifacts != null) {
            final ArtifactBuilder aBuilder = generator.artifact();

            for (final Artifact a : artifacts) {
                readArtifact(c, a, aBuilder);
            }
        }
    }

    private static void readErrors(final Collection<Error> errors,
            final ArtifactGenerator generator) {
        if (errors != null) {
            final ErrorBuilder eBuilder = generator.error();

            for (final Error e : errors) {
                readError(e, eBuilder);
            }
        }
    }

    private static void readMetrics(final Collection<ClassMetric> metrics,
            final ArtifactGenerator generator) {
        if (metrics != null) {
            final MetricBuilder mBuilder = generator.metric();

            for (final ClassMetric m : metrics) {
                readClassMetric(m, mBuilder);
            }
        }
    }

    private static void readConfig(final Config config,
            final ScanGenerator builder) {
        builder.javaVendor(config.getJavaVendor());
        builder.javaVersion(config.getJavaVersion());
        builder.project(config.getProject());
        builder.timeseries(config.getTimeseries());
        builder.externalFilter(config.getExternalFilter());
        for (final ToolExtension te : config.getExtensions()) {
            builder.extension(te.getId(), te.getVersion());
        }
        // TODO read all config attributes
    }

    private static boolean readArtifact(final Config config, final Artifact artifact,
            final ArtifactBuilder builder) {
    	if (config.filterLocation(artifact.getPrimarySourceLocation())) {
    		return false;
    	}
        builder.severity(artifact.getSeverity())
        .priority(artifact.getPriority())
        .message(artifact.getMessage());
        builder.findingType(artifact.getArtifactType().getTool(), artifact
                .getArtifactType().getVersion(), artifact.getArtifactType()
                .getMnemonic());
        builder.scanNumber(artifact.getScanNumber());
        readPrimarySource(builder, artifact.getPrimarySourceLocation());

        final Collection<SourceLocation> sources = artifact
                .getAdditionalSources();

        if (sources != null) {
            for (final SourceLocation sl : sources) {
                readSource(builder, sl);
            }
        }

        builder.build();
        return true;
    }

    private static void readClassMetric(final ClassMetric metric,
            final MetricBuilder builder) {
        builder.compilation(metric.getName()).packageName(metric.getPackage())
        .linesOfCode(metric.getLoc()).build();
    }

    private static void readError(final Error e, final ErrorBuilder builder) {
        builder.message(e.getMessage()).tool(e.getTool()).build();
    }

    private static void readPrimarySource(final ArtifactBuilder aBuilder,
            final SourceLocation s) {
        aBuilder.primarySourceLocation().compilation(s.getCompilation())
        .className(s.getClassName()).packageName(s.getPackageName())
        .endLine(s.getEndLineOfCode()).lineOfCode(s.getLineOfCode())
        .type(s.getIdentifierType()).identifier(s.getIdentifier())
        .hash(s.getHash()).build();
    }

    private static void readSource(final ArtifactBuilder aBuilder,
            final SourceLocation s) {
        aBuilder.sourceLocation().compilation(s.getCompilation())
        .className(s.getClassName()).packageName(s.getPackageName())
        .endLine(s.getEndLineOfCode()).lineOfCode(s.getLineOfCode())
        .type(s.getIdentifierType()).identifier(s.getIdentifier())
        .hash(s.getHash()).build();
    }

    private static boolean cancelled(final SLProgressMonitor monitor) {
        if (monitor != null) {
            return monitor.isCanceled();
        } else {
            return false;
        }
    }

    private static void work(final SLProgressMonitor monitor) {
        if (monitor != null) {
            monitor.worked(1);
        }
    }

}
