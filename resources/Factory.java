
import java.io.File;
import java.net.URI;
import java.util.*;

import com.surelogic.common.jobs.*;
import com.surelogic.sierra.tool.*;
import com.surelogic.sierra.tool.ArtifactType;
import com.surelogic.sierra.tool.AbstractToolInstance.TargetPrep;
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
			protected void execute(SLProgressMonitor monitor) throws Exception {      		
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
			}

			private List<File> init() throws Exception {
				// If processing individual source files
				final List<File> targets = new ArrayList<File>();
				roots = collectSourceRoots(new TargetPrep() {
					public void prep(File f) {
						targets.add(f);
					}					
				});

				// If processing binaries
				final List<String> paths = new ArrayList<String>();
				prepClassFiles(new TargetPrep() {
					public void prep(File f) {
						paths.add(f.getAbsolutePath());
					}
				});

				// If processing jars required for compilation/runtime
				final List<String> libs = new ArrayList<String>();
				prepAuxFiles(new TargetPrep() {
					public void prep(File f) {
						libs.add(f.getAbsolutePath());
					}
				});
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
					reportError(msg, t);
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
