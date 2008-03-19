/*
 * Created on Jan 17, 2008
 */
package com.surelogic.ant.sierra;

import java.io.File;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Javac;
import org.apache.tools.ant.taskdefs.compilers.CompilerAdapter;


public class SierraScan extends Javac {
	private String document;
	private String project;
	
	public String getProjectName() {
		return project;
	}

	public void setProjectName(String p) {
		this.project = p;
	}
	
	public String getDocument() {
		return document;
	}

	public void setDocument(String doc) {
		this.document = doc;
	}
	
	@Override
	protected void scanDir(File srcDir, File destDir, String[] files) {
		/*
		 * GlobPatternMapper m = new GlobPatternMapper(); m.setFrom("*.java");
		 * m.setTo("*.class"); SourceFileScanner sfs = new
		 * SourceFileScanner(this); File[] newFiles = sfs.restrictAsFiles(files,
		 * srcDir, destDir, m);
		 */
		File[] newFiles = new File[files.length];
		int i = 0;
		for (String name : files) {
			newFiles[i] = new File(srcDir, name);
			i++;
		}

		if (newFiles.length > 0) {
			File[] newCompileList = new File[compileList.length
					+ newFiles.length];
			System.arraycopy(compileList, 0, newCompileList, 0,
					compileList.length);
			System.arraycopy(newFiles, 0, newCompileList, compileList.length,
					newFiles.length);
			compileList = newCompileList;
		}
	}

	/**
	 * Modified from Javac.compile()
	 */
	@Override
	protected void compile() {
		File destDir = this.getDestdir();
		System.out.println(destDir.getAbsolutePath());

		if (compileList.length > 0) {
			log("Scanning " + compileList.length + " source file"
					+ (compileList.length == 1 ? "" : "s")
			// + (destDir != null ? " to " + destDir : "")
			);

			if (listFiles) {
				for (int i = 0; i < compileList.length; i++) {
					String filename = compileList[i].getAbsolutePath();
					log(filename);
				}
			}

			CompilerAdapter adapter = new SierraJavacAdapter(this);

			// now we need to populate the compiler adapter
			adapter.setJavac(this);

			// finally, lets execute the compiler!!
			if (!adapter.execute()) {
				if (failOnError) {
					throw new BuildException("Failed", getLocation());
				} else {
					log("Failed", Project.MSG_ERR);
				}
			}
		}
	}
}
