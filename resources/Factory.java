
import java.io.File;
import java.net.URI;
import java.util.*;

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

				final ArtifactGenerator gen = getGenerator();
				for(File target : targets) {
					final boolean success = processTarget(gen, target);
					if (success) {
						monitor.worked(1);
					}
				}
				return status.build();
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

			private boolean processTarget(final ArtifactGenerator gen, File target) {
				try {
					// TODO scan target and create artifacts	
					System.out.println("Doing something with "+target);

					if (false) {
						createArtifact(gen);
					}
					return true;
				} catch (Exception e) {
					final String msg = "Problem with "+target+": "+e.getMessage(); // TODO
					gen.error().tool(getId()).message(msg).build();					   
					status.addChild(SLStatus.createWarningStatus(-1, msg, e));
				}
				return false;
			}

			private void createArtifact(final ArtifactGenerator gen) {
				final ArtifactBuilder artifact = gen.artifact();						
				setSourceLocation(artifact.primarySourceLocation(),
						new SourceInfo()); // TODO fill in SourceInfo

				artifact.findingType(getId(), getVersion(), "Something"); // TODO
				artifact.message("Found something"); // TODO
				artifact.priority(Priority.MEDIUM).severity(Severity.WARNING); // TODO
				artifact.build();
			}
		};
	}
}
