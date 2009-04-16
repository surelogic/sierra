package com.surelogic.sierra.checkstyle;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.util.*;

import com.puppycrawl.tools.checkstyle.Checker;
import com.puppycrawl.tools.checkstyle.ConfigurationLoader;
import com.puppycrawl.tools.checkstyle.PropertiesExpander;
import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import com.puppycrawl.tools.checkstyle.api.AuditListener;
import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
import com.puppycrawl.tools.checkstyle.api.Configuration;
import com.puppycrawl.tools.checkstyle.api.SeverityLevel;
import com.surelogic.common.jobs.*;
import com.surelogic.sierra.tool.*;
import com.surelogic.sierra.tool.ArtifactType;
import com.surelogic.sierra.tool.analyzer.ILazyArtifactGenerator;
import com.surelogic.sierra.tool.message.*;
import com.surelogic.sierra.tool.message.ArtifactGenerator.ArtifactBuilder;
import com.surelogic.sierra.tool.targets.IToolTarget;

public class Factory extends AbstractToolFactory {
	public Set<ArtifactType> getArtifactTypes() {
		// TODO report available artifact types
		// Check the tool's source code to see how to get this info
		return Collections.emptySet(); 
	}

	@Override
	public List<File> getRequiredJars(Config config) {
		// TODO add any libraries not listed in the manifest
		// This should only be needed for tool extensions
		return super.getRequiredJars(config);
	}
	
	protected IToolInstance create(Config config, ILazyArtifactGenerator generator, boolean close) {
		return new AbstractToolInstance(this, config, generator, close) {
			SourceRoots roots; // Used to compute package names for files
			
			@Override
			protected SLStatus execute(SLProgressMonitor monitor) throws Exception {      		
				final List<File> targets = init();					
				int num = targets.size(); 
				monitor.begin(num > 0 ? num : 10);

				// Replaced original template with code adapted from Checkstyle
				final Properties props = System.getProperties();
				
				// Note that the file must be included under the src subdirectory to be found
				InputStream stream = Factory.class.getResourceAsStream("/sun_checks.xml");				
		        final Configuration config =
		        	ConfigurationLoader.loadConfiguration(stream, new PropertiesExpander(props), true);				        
		        final AuditListener listener = new Listener(monitor);
		        final Checker c = createChecker(config, listener);	
		        c.process(targets);
		        
				return status.build();
			}

			private Checker createChecker(Configuration config, AuditListener listener) throws CheckstyleException {
				// Adapted from Checkstyle
	            final Checker c = new Checker();
	            final ClassLoader moduleClassLoader = Checker.class.getClassLoader();
	            c.setModuleClassLoader(moduleClassLoader);
	            c.configure(config);
	            c.addListener(listener);
	            return c;
			}

			private List<File> init() throws Exception {
				// If processing individual source files
				final List<File> targets = new ArrayList<File>();
				roots = collectSourceRoots(new SourcePrep() {
					public void prep(File f) {
						targets.add(f);
					}					
				});

				// If processing binaries
				final List<String> paths = new ArrayList<String>();
				for (IToolTarget t : getBinTargets()) {
					final String path = new File(t.getLocation()).getAbsolutePath();
					switch (t.getKind()) {
					case FILE:
					case JAR:
						paths.add(path);
						break;
					case DIRECTORY:
						for (URI loc : t.getFiles()) {
							File f = new File(loc);
							if (f.exists()) {
								paths.add(f.getAbsolutePath());
							}
						}
						break;
					default:
						System.out.println("Ignoring target " + t.getLocation());
					}
				}

				// If processing jars required for compilation/runtime
				final List<String> libs = new ArrayList<String>();
				for (IToolTarget t : getAuxTargets()) {
					final String path = new File(t.getLocation()).getAbsolutePath();
					switch (t.getKind()) {
					case DIRECTORY:
					case JAR:
						libs.add(path);
						break;
					case FILE:							
					default:
						System.out.println("Ignoring target " + t.getLocation());
					}
				}
				// TODO use targets, paths, libs
				return targets;
			}
			
			Priority translateSeverityLevel(SeverityLevel level) {
				if (SeverityLevel.ERROR == level) {
					return Priority.HIGH;
				}
				if (SeverityLevel.WARNING == level) {
					return Priority.MEDIUM;
				}
				if (SeverityLevel.INFO == level) {
					return Priority.LOW;
				}
				return Priority.IGNORE;
			}
			
			/**
			 * Creates the listener that updates the monitor and status builder
			 */
			class Listener implements AuditListener {
				private final SLProgressMonitor monitor;
				private final ArtifactGenerator gen = getGenerator();
				
				Listener(SLProgressMonitor mon) {
					monitor = mon;
				}
				
				public void auditStarted(AuditEvent arg0) {		
					System.out.println("Starting Checkstyle scan");
				}

				public void fileStarted(AuditEvent e) {
					System.out.println("Scanning "+e.getFileName());
				}
				
				public void addError(AuditEvent e) {
					// Adapted from original createArtifact()
					final ArtifactBuilder artifact = gen.artifact();			
					final SourceInfo info = SourceInfo.get(roots, e.getFileName(), e.getLine());					
					setSourceLocation(artifact.primarySourceLocation(), info); // TODO fill in SourceInfo
					//e.getColumn();
					
					artifact.findingType(getId(), getVersion(), e.getSourceName()); // TODO
					artifact.message(e.getMessage()); // TODO
					
					Priority priority = translateSeverityLevel(e.getSeverityLevel());
					Severity severity = e.getSeverityLevel() == SeverityLevel.ERROR ? Severity.ERROR : Severity.WARNING;
					artifact.priority(priority).severity(severity); // TODO
					artifact.build();
				}				
				
				public void addException(AuditEvent e, Throwable t) {
					// Adapted from original processTarget()
					final String msg = "Problem with "+e.getFileName()+": "+t.getMessage(); // TODO
					reportError(msg, t);
				}
				
				public void fileFinished(AuditEvent arg0) {
					monitor.worked(1);	
				}

				public void auditFinished(AuditEvent arg0) {
					System.out.println("Finishing Checkstyle scan");
				}
			}
		};
	}
}
