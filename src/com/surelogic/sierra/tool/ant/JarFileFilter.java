/**
 * 
 */
package com.surelogic.sierra.tool.ant;

import java.io.File;
import java.io.FileFilter;

/**
 * @author ethan
 *
 */
public class JarFileFilter implements FileFilter {

	/* (non-Javadoc)
	 * @see java.io.FileFilter#accept(java.io.File)
	 */
	public boolean accept(File arg0) {
		return arg0.getName().endsWith(".jar");
	}

}
