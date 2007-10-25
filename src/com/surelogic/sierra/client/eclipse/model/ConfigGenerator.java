package com.surelogic.sierra.client.eclipse.model;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;

import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.client.eclipse.preferences.PreferenceConstants;
import com.surelogic.sierra.tool.SierraConstants;
import com.surelogic.sierra.tool.analyzer.BuildFileGenerator;
import com.surelogic.sierra.tool.config.Config;

public final class ConfigGenerator {

	private static final ConfigGenerator INSTANCE = new ConfigGenerator();
	/** The location to store tool results */
	private final File f_resultRoot = new File(
			SierraConstants.SIERRA_RESULTS_PATH);

	/** The default folder from the preference page */
	private final String f_sierraPath = PreferenceConstants.getSierraPath();

	/** The plug-in directory that has tools folder */
	private final String tools = BuildFileGenerator.getToolsDirectory()
			+ SierraConstants.TOOLS_FOLDER;

	/** The number of excluded tools : Default 0 */
	private int f_numberofExcludedTools = 0;

	private ConfigGenerator() {
		// singleton
	}

	public static ConfigGenerator getInstance() {
		return INSTANCE;
	}

	public List<Config> getProjectConfigs(List<IJavaProject> projects) {

		List<Config> configs = new ArrayList<Config>();

		for (IJavaProject p : projects) {
			configs.add(getProjectConfig(p));
		}

		return configs;

	}

	public List<Config> getCompilationUnitConfigs(
			List<ICompilationUnit> compilationUnits) {

		List<Config> configs = new ArrayList<Config>();

		for (ICompilationUnit c : compilationUnits) {
			configs.add(getCompilationUnitConfig(c));
		}

		return configs;

	}

	private Config getCompilationUnitConfig(ICompilationUnit compilationUnit) {
		String projectPath = compilationUnit.getJavaProject().getResource()
				.getLocation().toString();
		File baseDir = new File(projectPath);
		File scanDocument = new File(f_sierraPath + File.separator
				+ compilationUnit.getResource().getProject().getName() + " - "
				+ getTimeStamp() + SierraConstants.PARSED_FILE_SUFFIX);

		Config config = new Config();

		config.setBaseDirectory(baseDir);
		config.setProject(compilationUnit.getResource().getProject().getName());
		config.setDestDirectory(f_resultRoot);
		config.setScanDocument(scanDocument);
		config.setJavaVendor(System.getProperty("java.vendor"));
		config.setScanDocument(scanDocument);
		config.setToolsDirectory(new File(tools));
		config.setExcludedToolsList(getExcludedTools());
		String binDirs = null;
		try {
			IJavaProject javaProject = compilationUnit.getJavaProject();
			String outputLocation = javaProject.getOutputLocation()
					.makeRelative().toOSString();
			IType[] types = compilationUnit.getAllTypes();
			Set<IFile> classFiles = new HashSet<IFile>();
			for (IType t : types) {

				String qualifiedName = t.getFullyQualifiedName();

				int lastPeriod = qualifiedName.lastIndexOf(".");

				String packageName = qualifiedName.substring(0, lastPeriod);
				packageName = packageName.replace(".", File.separator);

				String javaType = qualifiedName.substring(lastPeriod + 1);
				String folder = outputLocation + File.separator + packageName;
				IFolder classFolder = (IFolder) ResourcesPlugin.getWorkspace()
						.getRoot().findMember(folder);
				getClassFiles(classFolder, javaType, classFiles);

			}

			for (IFile f : classFiles) {
				if (binDirs == null) {
					binDirs = f.getLocation().toOSString();
				} else {
					binDirs = binDirs + ";" + f.getLocation().toOSString();
				}
			}

			System.out.println("Class file :" + binDirs);
			config.setBinDirs(binDirs);
			config.setSourceDirs(compilationUnit.getResource().getLocation()
					.toOSString());

		} catch (JavaModelException e) {
			SLLogger.getLogger("sierra").log(Level.SEVERE,
					"Error when getting compilation unit types", e);
		} catch (CoreException e) {
			SLLogger.getLogger("sierra").log(Level.SEVERE,
					"Error when getting compilation unit types", e);
		}

		// Get clean option
		// Get included dirs -project specific
		// Get excluded dirs - project specific
		return config;
	}

	private Config getProjectConfig(IJavaProject project) {
		String projectPath = project.getResource().getLocation().toString();
		File baseDir = new File(projectPath);
		File scanDocument = new File(f_sierraPath + File.separator
				+ project.getProject().getName() + " - " + getTimeStamp()
				+ SierraConstants.PARSED_FILE_SUFFIX);

		Config config = new Config();

		try {
			IFolder binDirHolder = (IFolder) ResourcesPlugin.getWorkspace()
					.getRoot().findMember(project.getOutputLocation());
			config.setBinDirs(binDirHolder.getLocation().toOSString());
			IClasspathEntry[] entries = project.getRawClasspath();
			String srcDir = null;
			for (IClasspathEntry e : entries) {
				if (e.getEntryKind() == IClasspathEntry.CPE_SOURCE) {
					IResource resourceHolder = ResourcesPlugin.getWorkspace()
							.getRoot().findMember(e.getPath());

					if (srcDir == null) {
						srcDir = resourceHolder.getLocation().toOSString();
					} else {
						srcDir = srcDir + ";"
								+ resourceHolder.getLocation().toOSString();
					}
				}
			}
			config.setSourceDirs(srcDir);
		} catch (JavaModelException e) {
			SLLogger.getLogger("sierra").log(Level.SEVERE,
					"Error when getting outputlocation", e);
		}

		config.setBaseDirectory(baseDir);
		config.setProject(project.getProject().getName());
		config.setDestDirectory(f_resultRoot);
		config.setScanDocument(scanDocument);
		config.setJavaVendor(System.getProperty("java.vendor"));
		config.setScanDocument(scanDocument);
		config.setToolsDirectory(new File(tools));
		config.setExcludedToolsList(getExcludedTools());

		// Get clean option
		// Get included dirs -project specific
		// Get excluded dirs - project specific

		return config;
	}

	private String getTimeStamp() {
		Date date = Calendar.getInstance().getTime();
		long time = Calendar.getInstance().getTimeInMillis();

		/*
		 * Change the colon on date to semi-colon as file name with a colon is
		 * invalid
		 */
		String timeStamp = date.toString().replace(":", ";") + " - "
				+ String.valueOf(time);
		return timeStamp;
	}

	/**
	 * Generates a comma separated string of the exluded tools
	 * 
	 * @return
	 */
	private String getExcludedTools() {

		StringBuffer excludedTools = new StringBuffer();
		excludedTools.append("");
		f_numberofExcludedTools = 0;

		if (!PreferenceConstants.runFindBugs()) {
			excludedTools.append("findbugs, ");
			f_numberofExcludedTools++;
		}

		if (!PreferenceConstants.runCheckStyle()) {
			excludedTools.append("checkstyle, ");
			f_numberofExcludedTools++;
		}

		if (!PreferenceConstants.runPMD()) {
			excludedTools.append("pmd, ");
			f_numberofExcludedTools++;
		}

		if (!PreferenceConstants.runReckoner()) {
			excludedTools.append("reckoner");
			f_numberofExcludedTools++;
		}

		String listOfTools = excludedTools.toString();
		if (listOfTools != null && listOfTools.endsWith(", ")) {
			listOfTools = listOfTools.substring(0, listOfTools.length() - 2);
		}

		if (listOfTools.equals("")) {
			return null;
		}

		return listOfTools;
	}

	public int getNumberofExcludedTools() {
		return f_numberofExcludedTools;
	}

	/**
	 * Returns a list of all the java files in a given project
	 * 
	 * @param javaFileName
	 * @param classFiles
	 * 
	 * @param project
	 * @return
	 * @throws CoreException
	 */
	private Set<IFile> getClassFiles(IFolder folder, String javaFileName,
			Set<IFile> files) throws CoreException {

		IResource[] resources = folder.members();

		for (IResource r : resources) {
			if (r.getType() == IResource.FILE) {
				IFile f = (IFile) r;
				if (f.getFileExtension() != null
						&& f.getFileExtension().equals("class")) {

					String fileName = f.getName();
					String mainClassName = javaFileName + ".class";
					String javaTypes = javaFileName + "$";
					if ((fileName.equals(mainClassName))
							|| (fileName.contains(javaTypes))) {
						files.add(f);
					}

				}
			}

			if (r.getType() == IResource.FOLDER) {

				getClassFilesInFolder((IFolder) r, files, javaFileName);
			}
		}
		return files;
	}

	/**
	 * Recursively add java files in the provided list
	 * 
	 * @param folder
	 * @param files
	 * @param javaFileName
	 * @throws CoreException
	 */
	private void getClassFilesInFolder(IFolder folder, Set<IFile> files,
			String javaFileName) throws CoreException {

		IResource[] resources = folder.members();

		for (IResource r : resources) {
			if (r.getType() == IResource.FILE) {
				IFile f = (IFile) r;
				if (f.getFileExtension() != null
						&& f.getFileExtension().equalsIgnoreCase("class")) {
					String fileName = f.getName();
					String mainClassName = javaFileName + ".class";
					String javaTypes = javaFileName + "$";
					if ((fileName.equals(mainClassName))
							|| (fileName.contains(javaTypes))) {
						files.add(f);
					}
				}
			}

			if (r.getType() == IResource.FOLDER) {
				getClassFilesInFolder((IFolder) r, files, javaFileName);
			}
		}

	}
}
