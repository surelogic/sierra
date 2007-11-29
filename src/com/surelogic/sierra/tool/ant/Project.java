/**
 * 
 */
package com.surelogic.sierra.tool.ant;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.DirSet;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.util.FileUtils;

import com.surelogic.sierra.tool.message.Config;

/**
 * Class representing the definition of a single project
 * 
 * @author ethan
 * 
 */
public class Project {
	private FileUtils fileUtils = FileUtils.getFileUtils();
	//The project name - REQUIRED
	private String name = null;
	// The project's directory - REQUIRED
	private File dir = null;
	// Nested element containing source locations - OPTIONAL
	private List<Source> sources = new ArrayList<Source>();
	// Nested element containing binary locations - OPTIONAL
	private List<Binary> binaries = new ArrayList<Binary>();
	private Path src = null;
	private Path bin = null;
	private org.apache.tools.ant.Project antProject = null;

	public Project(org.apache.tools.ant.Project antProject) {
		this.antProject = antProject;
		src = new Path(antProject);
		bin = new Path(antProject);
	}
	
	public Project(org.apache.tools.ant.Project antProject, Config config) {
		this.antProject = antProject;
		src = new Path(antProject);
		bin = new Path(antProject);
		
		if(config.getBinDirs() != null){
			bin.append(new Path(antProject, config.getBinDirs()));
		}
		if(config.getSourceDirs() != null){
			src.append(new Path(antProject, config.getSourceDirs()));
		}
		
		name = config.getProject();
		dir = config.getBaseDirectory();
	}

	public void validate() {

		if (name == null) {
			throw new BuildException(
					"Parameter 'name' is required for 'project'.");
		}
		if (dir == null) {
			throw new BuildException(
					"Parameter 'dir' is required for 'project'.");
		} else if (dir != null) {
			if (!dir.isDirectory()) {
				throw new BuildException(
						"Parameter 'dir' must be a valid directory. "
								+ dir.getAbsolutePath()
								+ " is not a valid directory.");
			}
		}
		if (!sources.isEmpty()) {
			for (Source source : sources) {
				antProject.log("Sources in Project element: "
						+ source.toString(),
    				org.apache.tools.ant.Project.MSG_DEBUG);

				String[] list = source.getDirectoryScanner(antProject)
						.getIncludedDirectories();

				File basedir = source.getDir(antProject);

				for (String string : list) {
					File srcDir = fileUtils.resolveFile(basedir, string);
					if (!srcDir.exists()) {
						throw new BuildException("srcDir \"" + srcDir.getPath()
								+ "\" does not exist.");
					}
					src.append(new Path(antProject, srcDir.getAbsolutePath()));
				}
			}
		}
		if (!binaries.isEmpty()) {
			for (Binary binary : binaries) {
				antProject.log("Binaries in Project element: "
						+ binary.toString(),
    				org.apache.tools.ant.Project.MSG_DEBUG);

				String[] list = binary.getDirectoryScanner(antProject)
						.getIncludedDirectories();
				File basedir = binary.getDir(antProject);

				for (String string : list) {
					File binDir = fileUtils.resolveFile(basedir, string);
					if (!binDir.exists()) {
						throw new BuildException("binDir \"" + binDir.getPath()
								+ "\" does not exist.");
					}
					bin.append(new Path(antProject, binDir.getAbsolutePath()));
				}
			}
		}
	}

	public final String getName() {
		return name;
	}

	public final void setName(String name) {
		this.name = name;
	}

	public final File getDir() {
		return dir;
	}

	public final void setDir(File dir) {
		this.dir = dir;
	}

	public void addConfiguredSource(Source src) {
		sources.add(src);
	}

	public void addConfiguredBinary(Binary bin) {
		binaries.add(bin);
	}

	public Path getSources() {
		return src;
	}

	public Path getBinaries() {
		return bin;
	}

	/* *******************************************************
	 *  Inner classes
	 *  ******************************************************/
	public static class Source extends DirSet {
		public Source() {
			super();
		}
	}

	public static class Binary extends DirSet {
		public Binary() {
			super();
		}
	}
}
