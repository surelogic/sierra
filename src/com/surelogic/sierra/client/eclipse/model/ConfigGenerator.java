package com.surelogic.sierra.client.eclipse.model;

import java.io.File;
import java.net.URI;
import java.util.*;
import java.util.logging.Level;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.*;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.PlatformUI;

import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.client.eclipse.Activator;
import com.surelogic.sierra.client.eclipse.preferences.PreferenceConstants;
import com.surelogic.sierra.tool.SierraToolConstants;
import com.surelogic.sierra.tool.message.Config;
import com.surelogic.sierra.tool.targets.*;

/**
 * Utility class for getting configuration objects that are required to run
 * scans, it handles both project and compilation unit configs
 * 
 * @author Tanmay.Sinha
 * 
 */
public final class ConfigGenerator {

	private static final ConfigGenerator INSTANCE = new ConfigGenerator();
	/** The location to store tool results */
	private final File f_resultRoot = new File(
			SierraToolConstants.SIERRA_RESULTS_PATH);

	/** The default folder from the preference page */
	private final String f_sierraPath = PreferenceConstants.getSierraPath();

	/** The plug-in directory that has tools folder */
	private final String tools;
	
	/** The plug-in directory for common */
  private final String common;
  
  /** The plug-in directory for sierra-message */
  private final String message;

	/** The number of excluded tools : Default 0 */
	private int f_numberofExcludedTools = 0;

	private ConfigGenerator() {
		// singleton
		tools = Activator.getDefault().getDirectoryOf(
				SierraToolConstants.TOOL_PLUGIN_ID)
				+ SierraToolConstants.TOOLS_FOLDER;
		
		message = Activator.getDefault().getDirectoryOf(
        SierraToolConstants.MESSAGE_PLUGIN_ID);
		
		common = Activator.getDefault().getDirectoryOf(
        SierraToolConstants.COMMON_PLUGIN_ID);
	}

	public static ConfigGenerator getInstance() {
		return INSTANCE;
	}

	/**
	 * Get config objects for the list of {@link IJavaProject} provided
	 * 
	 * @param projects
	 * @return list of {@link Config}
	 */
	public List<Config> getProjectConfigs(List<IJavaProject> projects) {

		List<Config> configs = new ArrayList<Config>();

		for (final IJavaProject p : projects) {
			if (containsAtLeastOneCompilationUnit(p)) {
				configs.add(getProjectConfig(p));
			} else {
				/*
				 * Put up a dialog informing the user that no scan will be done
				 * on a project that contains no compilation units.
				 */
				PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
					public void run() {
						MessageDialog md = new MessageDialog(
								PlatformUI.getWorkbench()
										.getActiveWorkbenchWindow().getShell(),
								"Scan Skipped",
								null,
								"Sierra cannot scan the project '"
										+ p.getElementName()
										+ "' because it contains no Java compilation units",
								MessageDialog.INFORMATION,
								new String[] { "OK" }, 0);
						md.open();
					}
				});
			}
		}
		return configs;
	}

	private boolean containsAtLeastOneCompilationUnit(IJavaProject p) {
		try {
			for (IPackageFragment pf : p.getPackageFragments()) {
				if (pf.getCompilationUnits().length > 0)
					return true;
			}
		} catch (JavaModelException e) {
			SLLogger.getLogger().log(
					Level.SEVERE,
					"Failure trying to determine if " + p.getElementName()
							+ " contains any Java compilation units.", e);
		}
		return false;
	}

	/**
	 * Get a list of configs for provided compilation units coupled with a
	 * package - compilation unit map
	 * 
	 * @param compilationUnits
	 * @return list of {@link ConfigCompilationUnit}
	 */
	public List<ConfigCompilationUnit> getCompilationUnitConfigs(
			List<ICompilationUnit> compilationUnits) {
		final List<ConfigCompilationUnit> configCompilationUnits = new ArrayList<ConfigCompilationUnit>();

		Map<String, List<ICompilationUnit>> projectCompilationUnitMap = new HashMap<String, List<ICompilationUnit>>();
		for (ICompilationUnit c : compilationUnits) {

			String projectNameHolder = c.getJavaProject().getElementName();

			Set<String> projectsInMap = projectCompilationUnitMap.keySet();
			List<ICompilationUnit> compilationUnitsHolder = null;
			if (projectsInMap.contains(projectNameHolder)) {
				compilationUnitsHolder = projectCompilationUnitMap
						.get(projectNameHolder);
			} else {
				compilationUnitsHolder = new ArrayList<ICompilationUnit>();

			}
			compilationUnitsHolder.add(c);
			projectCompilationUnitMap.put(projectNameHolder,
					compilationUnitsHolder);
		}

		Set<String> projects = projectCompilationUnitMap.keySet();

		for (String s : projects) {
			List<ICompilationUnit> cus = projectCompilationUnitMap.get(s);
			if (cus.size() > 0) {
				final ConfigCompilationUnit ccu = new ConfigCompilationUnit(
						getCompilationUnitConfig(cus),
						getPackageCompilationUnitMap(cus));
				configCompilationUnits.add(ccu);
			}
		}

		return configCompilationUnits;

	}

	public int getNumberofExcludedTools() {
		return f_numberofExcludedTools;
	}

	private Map<String, List<String>> getPackageCompilationUnitMap(
			List<ICompilationUnit> compilationUnits) {
		Map<String, List<String>> packageCompilationUnitMap = new HashMap<String, List<String>>();

		for (ICompilationUnit c : compilationUnits) {
			try {
				IType[] types = c.getAllTypes();
				if (types.length > 0) {
					String qualifiedName = types[0].getFullyQualifiedName();

					int lastPeriod = qualifiedName.lastIndexOf(".");

					String packageName = SierraToolConstants.DEFAULT_PACKAGE_PARENTHESIS;
					if (lastPeriod != -1) {
						packageName = qualifiedName.substring(0, lastPeriod);
					}

					Set<String> packageInMap = packageCompilationUnitMap
							.keySet();
					List<String> compilationUnitsHolder = null;
					if (packageInMap.contains(packageName)) {
						compilationUnitsHolder = packageCompilationUnitMap
								.get(packageName);
					} else {
						compilationUnitsHolder = new ArrayList<String>();

					}

					String holder = c.getElementName();

					if (holder.endsWith(".java")) {
						holder = holder.substring(0, holder.length() - 5);
					}

					compilationUnitsHolder.add(holder);
					packageCompilationUnitMap.put(packageName,
							compilationUnitsHolder);
				}
			} catch (JavaModelException e) {
				SLLogger.getLogger("sierra").log(Level.SEVERE,
						"Error when getting compilation unit types", e);
			}

		}
		return packageCompilationUnitMap;
	}

	private Config getCompilationUnitConfig(
			List<ICompilationUnit> compilationUnits) {
		Config config = null;
		if (compilationUnits.size() > 0) {
			String projectPath = compilationUnits.get(0).getJavaProject()
					.getResource().getLocation().toString();
			File baseDir = new File(projectPath);
			File scanDocument = new File(f_sierraPath
					+ File.separator
					+ compilationUnits.get(0).getResource().getProject()
							.getName() + " - partial - " + getTimeStamp()
					+ SierraToolConstants.PARSED_FILE_SUFFIX);

			config = new Config();

			config.setBaseDirectory(baseDir);
			config.setProject(compilationUnits.get(0).getResource()
					.getProject().getName());
			config.setDestDirectory(f_resultRoot);
			config.setScanDocument(scanDocument);
			config.setJavaVendor(System.getProperty("java.vendor"));
			config.setScanDocument(scanDocument);
			setupTools(config);
			config.setExcludedToolsList(getExcludedTools());
			String binDirs = null;
			String srcDirs = null;
			IJavaProject javaProject = compilationUnits.get(0).getJavaProject();

			try {
				String outputLocation = javaProject.getOutputLocation()
						.makeRelative().toOSString();

				for (ICompilationUnit c : compilationUnits) {
					IType[] types = c.getAllTypes();
					Set<IFile> classFiles = new HashSet<IFile>();
					for (IType t : types) {

						String qualifiedName = t.getFullyQualifiedName();

						int lastPeriod = qualifiedName.lastIndexOf(".");

						String packageName = null;
						String javaType = null;
						String folder = null;
						if (lastPeriod != -1) {
							packageName = qualifiedName
									.substring(0, lastPeriod);
							packageName = packageName.replace(".",
									File.separator);
							javaType = qualifiedName.substring(lastPeriod + 1);
							folder = outputLocation + File.separator
									+ packageName;
						} else {
							packageName = "";
							javaType = qualifiedName;
							folder = outputLocation;
						}

						IResource classFolder = ResourcesPlugin.getWorkspace()
								.getRoot().findMember(folder);
						if (classFolder != null) {
							getClassFiles(classFolder, javaType, classFiles);
						} else {
							throw new IllegalStateException(
									"Unable to find binaries for project "
											+ t.getJavaProject()
													.getElementName());
						}

					}

					for (IFile f : classFiles) {
					  String osLoc = f.getLocation().toOSString();
						if (binDirs == null) {
							binDirs = osLoc;
						} else {
							binDirs = binDirs + ";" + osLoc;
						}
						File osFile = new File(osLoc);
						config.addTarget(new FileTarget(IToolTarget.Type.BINARY, osFile.toURI(), null));
					}

					String srcLoc = c.getResource().getLocation().toOSString();
					if (srcDirs == null) {
						srcDirs = srcLoc;
					} else {
						srcDirs = srcDirs + ";"
								+ srcLoc;
					}
					File srcFile = new File(srcLoc);
					IPackageFragmentRoot p = (IPackageFragmentRoot) c.getAncestor(IJavaElement.PACKAGE_FRAGMENT_ROOT);
					File rootFile = new File(p.getResource().getLocation().toOSString());
					config.addTarget(new FileTarget(srcFile.toURI(), rootFile.toURI())); 
				}
				config.setBinDirs(binDirs);
				config.setSourceDirs(srcDirs);

			} catch (JavaModelException e) {
				SLLogger.getLogger("sierra").log(Level.SEVERE,
						"Error when getting compilation unit types", e);
			} catch (CoreException e) {
				SLLogger.getLogger("sierra").log(Level.SEVERE,
						"Error when getting compilation unit types", e);
			} catch (IllegalStateException ise) {
				SLLogger.getLogger("sierra").log(Level.SEVERE,
						ise.getMessage(), ise);
			}

			// Get clean option
			// Get included dirs -project specific
			// Get excluded dirs - project specific

		}
		return config;
	}

	public Config getProjectConfig(IJavaProject project) {
		String projectPath = project.getResource().getLocation().toString();
		File baseDir = new File(projectPath);
		File scanDocument = new File(f_sierraPath + File.separator
				+ project.getProject().getName() + " - " + getTimeStamp()
				+ SierraToolConstants.PARSED_FILE_SUFFIX);

		Config config = new Config();

		try {
			IResource binDirHolder = ResourcesPlugin.getWorkspace().getRoot()
					.findMember(project.getOutputLocation());

			// If we cannot find the binary directory make the project root as
			// the binary location
			if (binDirHolder != null) {
				config.setBinDirs(binDirHolder.getLocation().toOSString());
			} else {
				config.setBinDirs(baseDir.getAbsolutePath());
			}
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
					
	    setupToolForProject(config, project, true);
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
		setupTools(config);
		config.setExcludedToolsList(getExcludedTools());

		// Get clean option
		// Get included dirs -project specific
		// Get excluded dirs - project specific
		return config;
	}

  private void setupTools(Config config) {
    config.setToolsDirectory(new File(tools));
    config.setCommonDirectory(common);
    config.setMessageDirectory(message);
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
	private Set<IFile> getClassFiles(IResource resource, String javaFileName,
			Set<IFile> files) throws CoreException {
		IResource[] resources = null;

		if (resource instanceof IProject) {
			IProject project = (IProject) resource;
			resources = project.members();
		} else if (resource instanceof IFolder) {
			IFolder folder = (IFolder) resource;
			resources = folder.members();
		}

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
	
  /**
   * @param toBeAnalyzed Whether the project will be analyzed, or is simply referred to
   */
  private static void setupToolForProject(final Config cfg, IJavaProject p, final boolean toBeAnalyzed) 
  throws JavaModelException 
  {
    final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
    for(IClasspathEntry cpe : p.getResolvedClasspath(true)) {
      handleClasspathEntry(cfg, toBeAnalyzed, root, cpe);
    }
    URI out = root.findMember(p.getOutputLocation()).getLocationURI();
    cfg.addTarget(new FullDirectoryTarget(toBeAnalyzed ? IToolTarget.Type.BINARY : IToolTarget.Type.AUX, out));
  }
  
  private static void handleClasspathEntry(final Config cfg, final boolean toBeAnalyzed, 
      final IWorkspaceRoot root, IClasspathEntry cpe) 
  throws JavaModelException 
  {
    switch (cpe.getEntryKind()) {
      case IClasspathEntry.CPE_SOURCE:
        if (toBeAnalyzed) {
          IResource res = root.findMember(cpe.getPath());
          URI loc = res.getLocationURI();

          IPath[] includePatterns = cpe.getInclusionPatterns();                
          IPath[] excludePatterns = cpe.getExclusionPatterns();
          if ((excludePatterns != null && excludePatterns.length > 0) || 
              (includePatterns != null && includePatterns.length > 0)) {
            final String[] inclusions = convertPaths(includePatterns);
            final String[] exclusions = convertPaths(excludePatterns);                
            cfg.addTarget(new FilteredDirectoryTarget(IToolTarget.Type.SOURCE, loc,
                inclusions, exclusions));
          } else {
            cfg.addTarget(new FullDirectoryTarget(IToolTarget.Type.SOURCE, loc));
          }
        }
        break;
      case IClasspathEntry.CPE_LIBRARY:
        IPath srcPath = cpe.getSourceAttachmentPath();
        // FIX cpe.getSourceAttachmentRootPath();
        if (srcPath != null) {
          IToolTarget srcTarget = createTarget(root, cpe.getSourceAttachmentPath(), null);
          cfg.addTarget(createTarget(root, cpe.getPath(), srcTarget));
        } else {
          cfg.addTarget(createTarget(root, cpe.getPath(), null));
        }
        break;
      case IClasspathEntry.CPE_PROJECT:
        String projName = cpe.getPath().lastSegment();
        IProject proj = root.getProject(projName);
        setupToolForProject(cfg, JavaCore.create(proj), false);
        break;
    }
  }
  
  private static String[] convertPaths(IPath[] patterns) {
    if (patterns == null || patterns.length == 0) {
      return null;
    }
    final String[] exclusions = new String[patterns.length];
    int i = 0;
    for(IPath exclusion : patterns) {
      exclusions[i] = exclusion.toString();
      i++;
    }
    return exclusions;
  }

  private static ToolTarget createTarget(final IWorkspaceRoot root, IPath libPath, IToolTarget src) {
    URI lib;
    File libFile = new File(libPath.toOSString());
    if (libFile.exists()) {
      lib = libFile.toURI();
    } else {
      lib = root.findMember(libPath).getLocationURI();
    }
    if (new File(lib).isDirectory()) {
      return new FullDirectoryTarget(IToolTarget.Type.AUX, lib);
    } else {
      return new JarTarget(lib);
    }
  }
}
