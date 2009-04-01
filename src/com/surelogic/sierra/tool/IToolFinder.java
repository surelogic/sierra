package com.surelogic.sierra.tool;

import java.io.File;
import java.util.List;

/**
 * Interface for code to find IToolFactories
 * 
 * @author edwin
 */
public interface IToolFinder {
	List<File> findToolDirectories();
}
