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

import com.surelogic.sierra.tool.config.Config;

/**
 * Class representing the definition of a single project
 * 
 * @author ethan
 * 
 */
public class Project {
	private FileUtils fileUtils = FileUtils.getFileUtils();
	private String name = null;
	private File dir = null;
	private List<Source> sources = new ArrayList<Source>();
	private List<Binary> binaries = new ArrayList<Binary>();
	private Path src = null;
	private Path bin = null;
	private org.apache.tools.ant.Project proj = null;

	public Project(org.apache.tools.ant.Project proj) {
		this.proj = proj;
		src = new Path(proj);
		bin = new Path(proj);
	}
	
	public Project(org.apache.tools.ant.Project proj, Config config) {
		this.proj = proj;
		src = new Path(proj);
		bin = new Path(proj);
		
		name = config.getProject();
		dir = new File(config.getBaseDirectory());
		
		Source srcs = new Source();
		Binary bins = new Binary();
	}

	public void validate() {

		if (name == null) {
			throw new BuildException(
					"Parameter 'name' is required for 'project'.");
		}
		if (dir == null) {
			throw new BuildException(
					"Parameter 'baseDir' is required for 'project'.");
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
				System.out.println("Sources in Project element: "
						+ source.toString());// ,
				// org.apache.tools.ant.Project.MSG_DEBUG);

				String[] list = source.getDirectoryScanner()
						.getIncludedDirectories();

				File basedir = source.getDir();

				for (String string : list) {
					File srcDir = fileUtils.resolveFile(basedir, string);
					if (!srcDir.exists()) {
						throw new BuildException("srcDir \"" + srcDir.getPath()
								+ "\" does not exist.");
					}
					src.append(new Path(proj, srcDir.getAbsolutePath()));
				}
			}
		}
		if (!binaries.isEmpty()) {
			for (Binary binary : binaries) {
				System.out.println("Binaries in Project element: "
						+ binary.toString());// ,
				// org.apache.tools.ant.Project.MSG_DEBUG);

				String[] list = binary.getDirectoryScanner()
						.getIncludedDirectories();
				File basedir = binary.getDir();

				for (String string : list) {
					File binDir = fileUtils.resolveFile(basedir, string);
					if (!binDir.exists()) {
						throw new BuildException("binDir \"" + binDir.getPath()
								+ "\" does not exist.");
					}
					bin.append(new Path(proj, binDir.getAbsolutePath()));
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
