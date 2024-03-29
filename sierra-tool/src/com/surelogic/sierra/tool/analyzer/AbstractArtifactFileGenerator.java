package com.surelogic.sierra.tool.analyzer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.MessageFormat;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.surelogic.common.SLUtility;
import com.surelogic.common.i18n.I18N;
import com.surelogic.common.jobs.SLProgressMonitor;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.tool.message.Artifact;
import com.surelogic.sierra.tool.message.ClassMetric;
import com.surelogic.sierra.tool.message.Config;
import com.surelogic.sierra.tool.message.Error;
import com.surelogic.sierra.tool.message.IdentifierType;
import com.surelogic.sierra.tool.message.MessageWarehouse;
import com.surelogic.sierra.tool.message.MetricBuilder;
import com.surelogic.sierra.tool.message.Priority;
import com.surelogic.sierra.tool.message.Severity;
import com.surelogic.sierra.tool.message.SourceLocation;

/**
 * The scan document generator
 * 
 * This class generates the run document. It generates 3 separate temporary
 * files and stores the artifacts, errors and config in them. It finally
 * combines all of them with proper xml tags and generates a run document.
 * 
 * @author Tanmay.Sinha
 * @author Edwin.Chan
 */
abstract class AbstractArtifactFileGenerator extends DefaultArtifactGenerator {

  private static final String XML_START = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>";
  private static final String TOOL_OUTPUT_START = "<toolOutput>";
  private static final String TOOL_OUTPUT_END = "</toolOutput>";

  private static final Logger log = SLLogger.getLogger("sierra");
  private static final String RUN_START = "<scan version=\"{0}\" >";
  private static final String RUN_END = "</scan>";
  private static final String UID_START = "<uid>";
  private static final String UID_END = "</uid>";
  private static final String ARTIFACTS_START = "<artifacts>";
  private static final String ARTIFACTS_END = "</artifacts>";
  private static final String ERROR_START = "<errors>";
  private static final String ERROR_END = "</errors>";
  private static final String METRICS_START = "<metrics>";
  private static final String METRICS_END = "</metrics>";
  private static final String ENCODING = "UTF-8";

  ArtifactBuilderAdapter artifactAdapter;

  FileOutputStream artOut;

  final Config config;
  File artifactsHolder;
  File metricsHolder;
  File errorsHolder;
  FileOutputStream errOut;
  FileOutputStream metricsOut;

  public AbstractArtifactFileGenerator(Config config) {
    this.config = config;

    try {

      artifactsHolder = File.createTempFile("artifacts", "tmp");
      artOut = new FileOutputStream(artifactsHolder, true);
      artifactAdapter = new ArtifactBuilderAdapter();

      errorsHolder = File.createTempFile("errors", "tmp");
      errOut = new FileOutputStream(errorsHolder, true);

      metricsHolder = File.createTempFile("metrics", "tmp");
      metricsOut = new FileOutputStream(metricsHolder, true);
    } catch (final FileNotFoundException e) {
      log.log(Level.SEVERE, "Unable to locate the file", e);
    } catch (final IOException e) {
      log.log(Level.SEVERE, "Unable to read/write from/to the file", e);
    }

  }

  @Override
  public ArtifactBuilder artifact() {
    return artifactAdapter;
  }

  @Override
  public MetricBuilder metric() {
    return new MessageMetricBuilder();
  }

  @Override
  public ErrorBuilder error() {
    return new MessageErrorBuilder();
  }

  protected abstract OutputStream openOutputStream() throws IOException;

  protected abstract void closeOutputStream() throws IOException;

  @Override
  public void finished(SLProgressMonitor monitor) {
    monitor.begin(20);
    try {
      artOut.close();
      errOut.close();
      metricsOut.close();

      monitor.subTask("Writing header");
      final OutputStream stream = openOutputStream();
      final OutputStreamWriter osw = new OutputStreamWriter(stream, ENCODING);
      final PrintWriter finalFile = new PrintWriter(osw);
      finalFile.write(XML_START);
      finalFile.write('\n');
      finalFile.write(MessageFormat.format(RUN_START, I18N.msg("sierra.teamserver.version")));
      finalFile.write('\n');
      finalFile.write(UID_START);
      finalFile.write(UUID.randomUUID().toString());
      finalFile.write(UID_END);
      finalFile.write('\n');

      finalFile.write(TOOL_OUTPUT_START);
      monitor.worked(1);

      if (metricsHolder.exists()) {
        monitor.subTask("Writing metrics: " + metricsHolder.length());

        finalFile.write(METRICS_START);
        copyContents(metricsHolder, finalFile);
        finalFile.flush();
        finalFile.write(METRICS_END);
        monitor.worked(1);
        metricsHolder.delete();
      }

      if (artifactsHolder.exists()) {
        monitor.subTask("Writing artifacts: " + artifactsHolder.length());

        finalFile.write(ARTIFACTS_START);
        copyContents(artifactsHolder, finalFile);
        /*
         * BufferedReader in = new BufferedReader(new
         * FileReader(artifactsHolder)); String line = null; while ((line =
         * in.readLine()) != null) { finalFile.write(line);
         * finalFile.write("\n"); } in.close();
         */
        finalFile.flush();
        finalFile.write(ARTIFACTS_END);
        monitor.worked(1);
        artifactsHolder.delete();
      }

      if (errorsHolder.exists()) {
        monitor.subTask("Writing errors: " + errorsHolder.length());

        finalFile.write(ERROR_START);
        copyContents(errorsHolder, finalFile);
        finalFile.write(ERROR_END);
        monitor.worked(1);
        errorsHolder.delete();
      }
      finalFile.write(TOOL_OUTPUT_END);

      monitor.subTask("Writing config");
      final File configOutput = File.createTempFile("config", "tmp");
      final FileOutputStream fos = new FileOutputStream(configOutput);
      MessageWarehouse.getInstance().writeConfig(config, fos);
      fos.close();

      copyContents(configOutput, finalFile);
      configOutput.delete();
      finalFile.write(RUN_END);
      finalFile.flush();
      closeOutputStream();
      monitor.worked(1);
    } catch (final FileNotFoundException e) {
      log.log(Level.SEVERE, "Unable to locate the file", e);
      throw new RuntimeException(e.getMessage());
    } catch (final IOException e) {
      log.log(Level.SEVERE, "Unable to read/write from/to the file", e);
      throw new RuntimeException(e.getMessage());
    } catch (final Exception e) {
      log.log(Level.SEVERE, "Error when trying to generate the scan document", e);
      throw new RuntimeException(e.getMessage());
    }

  }

  private static void copyContents(File file, PrintWriter out) throws FileNotFoundException, IOException {
    final BufferedReader in = new BufferedReader(new FileReader(file));
    /*
     * String line;
     * 
     * while ((line = in.readLine()) != null) { out.write(line);
     * out.write("\n"); } in.close(); out.flush();
     */
    // Transfer bytes from in to out
    final char[] buf = new char[4096];
    int len;
    while ((len = in.read(buf)) > 0) {
      out.write(buf, 0, len);
    }
    in.close();
  }

  class MessageErrorBuilder implements ErrorBuilder {

    private String message;
    private String tool;
    private final MessageWarehouse mw = MessageWarehouse.getInstance();

    @Override
    public void build() {

      final Error error = new Error();
      error.setMessage(message);
      error.setTool(tool);
      mw.writeError(error, errOut);
    }

    @Override
    public ErrorBuilder message(String message) {
      this.message = message;
      return this;
    }

    @Override
    public ErrorBuilder tool(String tool) {
      this.tool = tool;
      return this;
    }

  }

  class MessageMetricBuilder implements MetricBuilder {

    private String clazz;
    private String pakkage;
    private int linesOfCode;
    private final MessageWarehouse mw = MessageWarehouse.getInstance();

    @Override
    public void build() {
      final ClassMetric metric = new ClassMetric();
      metric.setName(clazz);
      metric.setPackage(pakkage);
      metric.setLoc(linesOfCode);
      mw.writeClassMetric(metric, metricsOut);
    }

    @Override
    public MetricBuilder compilation(String name) {
      clazz = name;
      return this;
    }

    @Override
    public MetricBuilder linesOfCode(int line) {
      linesOfCode = line;
      return this;
    }

    @Override
    public MetricBuilder packageName(String name) {
      if (name == null || "".equals(name)) {
        name = SLUtility.JAVA_DEFAULT_PACKAGE;
      }
      pakkage = name;
      return this;
    }

  }

  class ArtifactBuilderAdapter implements ArtifactBuilder {
    final Artifact.Builder artBuilder;
    final MessageWarehouse mw = MessageWarehouse.getInstance();

    ArtifactBuilderAdapter() {
      artBuilder = new Artifact.Builder();
    }

    @Override
    public void build() {
      final Artifact a = artBuilder.build();
      mw.writeArtifact(a, artOut);
    }

    @Override
    public ArtifactBuilder findingType(String tool, String version, String mnemonic) {
      artBuilder.findingType(tool, version, mnemonic);
      return this;
    }

    @Override
    public ArtifactBuilder message(String message) {
      artBuilder.message(message);
      return this;
    }

    @Override
    public SourceLocationBuilder primarySourceLocation() {
      return new SourceLocationAdapter(true);
    }

    @Override
    public ArtifactBuilder priority(Priority priority) {
      artBuilder.priority(priority);
      return this;
    }

    @Override
    public ArtifactBuilder severity(Severity severity) {
      artBuilder.severity(severity);
      return this;
    }

    @Override
    public ArtifactBuilder scanNumber(Integer number) {
      artBuilder.scanNumber(number);
      return this;
    }

    @Override
    public SourceLocationBuilder sourceLocation() {
      return new SourceLocationAdapter(false);
    }

    private class SourceLocationAdapter implements SourceLocationBuilder {
      private final boolean isPrimary;
      private final SourceLocation.Builder sourceBuilder;

      public SourceLocationAdapter(boolean isPrimary) {
        this.isPrimary = isPrimary;
        sourceBuilder = new SourceLocation.Builder();
      }

      @Override
      public void build() {
        if (isPrimary) {
          artBuilder.primarySourceLocation(sourceBuilder.build());
        } else {
          artBuilder.sourceLocation(sourceBuilder.build());
        }
      }

      @Override
      public SourceLocationBuilder className(String className) {
        sourceBuilder.className(className);
        return this;
      }

      @Override
      public SourceLocationBuilder endLine(int line) {
        sourceBuilder.endLine(line);
        return this;
      }

      @Override
      public SourceLocationBuilder hash(Long hash) {
        sourceBuilder.hash(hash);
        return this;
      }

      @Override
      public SourceLocationBuilder identifier(String name) {
        sourceBuilder.identifier(name);
        return this;
      }

      @Override
      public SourceLocationBuilder lineOfCode(int line) {
        sourceBuilder.lineOfCode(line);
        return this;
      }

      @Override
      public SourceLocationBuilder packageName(String name) {
        if (name == null || "".equals(name)) {
          name = SLUtility.JAVA_DEFAULT_PACKAGE;
        }
        sourceBuilder.packageName(name);
        return this;
      }

      @Override
      public SourceLocationBuilder type(IdentifierType type) {
        sourceBuilder.type(type);
        return this;
      }

      @Override
      public SourceLocationBuilder compilation(String compilation) {
        sourceBuilder.compilation(compilation);
        return this;
      }

    }

  }

}
