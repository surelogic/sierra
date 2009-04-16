package com.surelogic.sierra.tool;

import java.io.File;
import java.util.List;

/**
 * Interface for code to find IToolFactory objects
 * Used in conjunction with ToolUtil.addToolFinder()
 * 
 * @author edwin
 */
public interface IToolFinder {
	/**
	 * @return A list of directories where Sierra can find tools
	 */
	List<File> findToolDirectories();
}
