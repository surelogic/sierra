package com.surelogic.sierra.client.eclipse.model;

import static com.surelogic.sierra.tool.SierraToolConstants.PARSED_FILE_SUFFIX;
import static com.surelogic.sierra.tool.SierraToolConstants.PARSED_ZIP_FILE_SUFFIX;
import static com.surelogic.sierra.tool.SierraToolConstants.USE_ZIP;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

import com.surelogic.common.SLUtility;
import com.surelogic.common.eclipse.Activator;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.client.eclipse.Tools;
import com.surelogic.sierra.client.eclipse.preferences.PreferenceConstants;
import com.surelogic.sierra.tool.*;
import com.surelogic.sierra.tool.message.Config;
import com.surelogic.sierra.tool.message.ToolExtension;
import com.surelogic.sierra.tool.targets.FileTarget;
import com.surelogic.sierra.tool.targets.FilteredDirectoryTarget;
import com.surelogic.sierra.tool.targets.FullDirectoryTarget;
import com.surelogic.sierra.tool.targets.IToolTarget;
import com.surelogic.sierra.tool.targets.JarTarget;
import com.surelogic.sierra.tool.targets.ToolTarget;

/**
 * Utility class for getting configuration objects that are required to run
 * scans, it handles both project and compilation unit configs
 */
public final class ConfigGenerator {
	private static final String[] PLUGINS = {
			SierraToolConstants.MESSAGE_PLUGIN_ID,
			SierraToolConstants.COMMON_PLUGIN_ID,
			SierraToolConstants.TOOL_PLUGIN_ID,
			/*
			 * SierraToolConstants.PMD_PLUGIN_ID,
			 * SierraToolConstants.FB_PLUGIN_ID,
			 */
			SierraToolConstants.JUNIT4_PLUGIN_ID,
			SierraToolConstants.JUNIT_PLUGIN_ID,
			SierraToolConstants.JAVA5_PLUGIN_ID, };

	private static final ConfigGenerator INSTANCE = new ConfigGenerator();
	/** The location to store tool results */
	private final File f_resultRoot = new File(
			SierraToolConstants.SIERRA_RESULTS_PATH);

	/** The default folder from the preference page */
	private final String f_sierraPath = PreferenceConstants
			.getSierraDataDirectory().getAbsolutePath();

	/** The plug-in directory that has tools folder */
	private final String tools;

	private final Map<String, String> pluginDirs = new HashMap<String, String>();

	/** The number of excluded tools : Default 0 */
	private int f_numberofExcludedTools = 0;

	private ConfigGenerator() {
		// singleton
		tools = Activator.getDefault().getDirectoryOf(
				SierraToolConstants.TOOL_PLUGIN_ID)
				+ SierraToolConstants.TOOLS_FOLDER;
		/*
		 * String jdt =
		 * Activator.getDefault().getDirectoryOf("org.eclipse.jdt.core");
		 * System.out.println(jdt);
		 */
		for (String id : PLUGINS) {
			getDirectoryOfPlugin(id);
		}
		for (String id : Tools.getToolPluginIds()) {
			getDirectoryOfPlugin(id);
		}
		getDirectoryOfAllPlugins(SierraToolConstants.CORE_RUNTIME_PLUGIN_ID);
		getDirectoryOfAllPlugins(SierraToolConstants.JDT_CORE_PLUGIN_ID);
	}

	private void getDirectoryOfPlugin(String id) {
		try {
			pluginDirs.put(id, Activator.getDefault().getDirectoryOf(id));
		} catch (IllegalStateException e) {
			System.out.println("Couldn't find plugin: " + id);
		}
	}

	private void getDirectoryOfAllPlugins(String rootId) {
		for (String id : Activator.getDefault().getDependencies(rootId)) {
			getDirectoryOfPlugin(id);
		}
	}

	public static ConfigGenerator getInstance() {
		return INSTANCE;
	}

	/**
	 * Get a list of configs for provided compilation units coupled with a
	 * package - compilation unit map
	 * 
	 * @param compilationUnits
	 * @return list of {@link ConfigCompilationUnit}
	 */
	public List<ConfigCompilationUnit> getCompilationUnitConfigs(
			Collection<ICompilationUnit> compilationUnits) {
		final List<ConfigCompilationUnit> configCompilationUnits = new ArrayList<ConfigCompilationUnit>();

		Map<String, List<ICompilationUnit>> projectCompilationUnitMap = new HashMap<String, List<ICompilationUnit>>();
		for (ICompilationUnit c : compilationUnits) {

			String projectNameHolder = c.getJavaProject().getElementName();

			Set<String> projectsInMap = projectCompilationUnitMap.keySet();
			List<ICompilationUnit> compilationUnitsHolder;
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

		for (Map.Entry<String, List<ICompilationUnit>> entry : projectCompilationUnitMap
				.entrySet()) {
			List<ICompilationUnit> cus = entry.getValue();
			if (!cus.isEmpty()) {
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
				String packageName = SLUtility.JAVA_DEFAULT_PACKAGE;
				if (types.length > 0 || "package-info.java".equals(c.getElementName())) {
					for(org.eclipse.jdt.core.IPackageDeclaration decl : c.getPackageDeclarations()) {
						packageName = decl.getElementName();
						break;
					}
					/*
					String qualifiedName = types[0].getFullyQualifiedName();

					int lastPeriod = qualifiedName.lastIndexOf('.');
					if (lastPeriod != -1) {
						packageName = qualifiedName.substring(0, lastPeriod);
					}
					*/
				} else {
					SLLogger.getLogger().warning("No package for "+c.getElementName());
					continue;
				}
					
				Set<String> packageInMap = packageCompilationUnitMap
				.keySet();
				List<String> compilationUnitsHolder;
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
			} catch (JavaModelException e) {
				SLLogger.getLogger().log(Level.SEVERE,
						"Error when getting compilation unit types", e);
			}

		}
		return packageCompilationUnitMap;
	}

	private Config getCompilationUnitConfig(
			List<ICompilationUnit> compilationUnits) {
		Config config = null;
		if (!compilationUnits.isEmpty()) {
			final ICompilationUnit firstCU = compilationUnits.get(0);
			String projectPath = firstCU.getJavaProject().getResource()
					.getLocation().toString();
			File baseDir = new File(projectPath);
			IJavaProject javaProject = firstCU.getJavaProject();
			File scanDocument = new File(computeScanDocumentName(javaProject,
					true));

			config = new Config();

			config.setBaseDirectory(baseDir);
			config.setProject(firstCU.getResource().getProject().getName());
			config.setDestDirectory(f_resultRoot);
			config.setScanDocument(scanDocument);
			setupTools(config, javaProject);

			try {
				String defaultOutputLocation = javaProject.getOutputLocation()
						.makeRelative().toOSString();

				for (ICompilationUnit c : compilationUnits) {
					String outputLocation = computeOutputLocation(javaProject,
							c, defaultOutputLocation);
					IType[] types = c.getAllTypes();
					Set<IFile> classFiles = new HashSet<IFile>();
					for (IType t : types) {

						String qualifiedName = t.getFullyQualifiedName();

						int lastPeriod = qualifiedName.lastIndexOf('.');

						String packageName;
						String javaType;
						String folder;
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
						File osFile = new File(osLoc);
						config.addTarget(new FileTarget(
								IToolTarget.Type.BINARY, osFile.toURI(), null));
					}

					String srcLoc = c.getResource().getLocation().toOSString();
					File srcFile = new File(srcLoc);
					IPackageFragmentRoot p = (IPackageFragmentRoot) c
							.getAncestor(IJavaElement.PACKAGE_FRAGMENT_ROOT);
					File rootFile = new File(p.getResource().getLocation()
							.toOSString());
					config.addTarget(new FileTarget(srcFile.toURI(), rootFile
							.toURI()));
				}

				setupToolForProject(config, javaProject, false);
			} catch (JavaModelException e) {
				SLLogger.getLogger().log(Level.SEVERE,
						"Error when getting compilation unit types", e);
			} catch (CoreException e) {
				SLLogger.getLogger().log(Level.SEVERE,
						"Error when getting compilation unit types", e);
			} catch (IllegalStateException ise) {
				SLLogger.getLogger().log(Level.SEVERE, ise.getMessage(), ise);
			}

			// Get clean option
			// Get included dirs -project specific
			// Get excluded dirs - project specific

		}
		return config;
	}

	private String computeOutputLocation(IJavaProject p, ICompilationUnit c,
			String defaultOut) throws JavaModelException {
		final IPath cuPath = c.getResource().getFullPath();
		for (IClasspathEntry e : p.getRawClasspath()) {
			final IPath out = e.getOutputLocation();
			if (out != null && e.getEntryKind() == IClasspathEntry.CPE_SOURCE
					&& e.getPath().isPrefixOf(cuPath)) {
				// FIX Need to check include/excludes
				return out.makeRelative().toOSString();
			}
		}
		return defaultOut;
	}

	public Config getProjectConfig(IJavaProject project) {
		String projectPath = project.getResource().getLocation().toString();
		File baseDir = new File(projectPath);
		File scanDocument = new File(computeScanDocumentName(project, false));

		Config config = new Config();

		try {
			setupToolForProject(config, project, true);
		} catch (JavaModelException e) {
			SLLogger.getLogger().log(Level.SEVERE,
					"Error when getting output location", e);
		}

		config.setBaseDirectory(baseDir);
		config.setProject(project.getProject().getName());
		config.setDestDirectory(f_resultRoot);
		config.setScanDocument(scanDocument);
		setupTools(config, project);

		// Get clean option
		// Get included dirs -project specific
		// Get excluded dirs - project specific
		return config;
	}

	private String computeScanDocumentName(IJavaProject project, boolean partial) {
		return f_sierraPath + File.separator + project.getProject().getName()
				+ (partial ? ".partial." : ".") + ToolUtil.getTimeStamp()
				+ (USE_ZIP ? PARSED_ZIP_FILE_SUFFIX : PARSED_FILE_SUFFIX);
	}

	private void setupTools(Config config, IJavaProject javaProject) {
		config.setJavaVendor(System.getProperty("java.vendor"));
		config.setJavaVersion(System.getProperty("java.version"));
		config.setMemorySize(PreferenceConstants.getToolMemoryMB());
		config.setToolsDirectory(new File(tools));
		config.setPluginDirs(pluginDirs);
		config.setComplianceLevel(javaProject.getOption(
				JavaCore.COMPILER_COMPLIANCE, true));
		config.setSourceLevel(javaProject.getOption(JavaCore.COMPILER_SOURCE,
				true));
		config.setTargetLevel(javaProject.getOption(
				JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, true));
	
		// Compute set of excluded tools
		StringBuilder excludedTools = new StringBuilder();
		f_numberofExcludedTools = 0;

		for (IToolFactory f : Tools.findToolFactories()) {			
			if (!PreferenceConstants.runTool(f)) {
				// Only need to add a comma if this isn't the first one
				if (f_numberofExcludedTools != 0) {
					excludedTools.append(", ");
				}
				excludedTools.append(f.getId());
				f_numberofExcludedTools++;
			} else {
				for(final IToolExtension t : f.getExtensions()) {
					final ToolExtension ext = new ToolExtension();
					ext.setTool(f.getId());
					ext.setId(t.getId());
					ext.setVersion(t.getVersion());
					config.addExtension(ext);
				}
			}
		}
		if (f_numberofExcludedTools == 0) {
			config.setExcludedToolsList(null);
		} else {
			config.setExcludedToolsList(excludedTools.toString());
		}
		
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
		} else {
			return files;
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
	 * @param toBeAnalyzed
	 *            Whether the project will be analyzed, or is simply referred to
	 */
	private static void setupToolForProject(final Config cfg, IJavaProject p,
			final boolean toBeAnalyzed) throws JavaModelException {
		setupToolForProject(cfg, new HashSet<IJavaProject>(), p, toBeAnalyzed);
	}

	private static void setupToolForProject(final Config cfg,
			Set<IJavaProject> handled, IJavaProject p,
			final boolean toBeAnalyzed) throws JavaModelException {
		if (handled.contains(p)) {
			return;
		}
		handled.add(p);

		final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		for (IClasspathEntry cpe : p.getResolvedClasspath(true)) {
			handleClasspathEntry(cfg, handled, toBeAnalyzed, root, cpe);
		}
		handleOutputLocation(cfg, p.getOutputLocation(), toBeAnalyzed);
	}

	private static void handleOutputLocation(final Config cfg, IPath outLoc,
			final boolean toBeAnalyzed) {
		if (outLoc == null) {
			return;
		}
		final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IResource res = root.findMember(outLoc);
		if (res == null) {
			return;
		}
		URI out = res.getLocationURI();
		cfg.addTarget(new FullDirectoryTarget(
				toBeAnalyzed ? IToolTarget.Type.BINARY : IToolTarget.Type.AUX,
				out));
	}

	private static void handleClasspathEntry(final Config cfg,
			Set<IJavaProject> handled, final boolean toBeAnalyzed,
			final IWorkspaceRoot root, IClasspathEntry cpe)
			throws JavaModelException {
		switch (cpe.getEntryKind()) {
		case IClasspathEntry.CPE_SOURCE:
			if (toBeAnalyzed) {
				IResource res = root.findMember(cpe.getPath());
				URI loc = res.getLocationURI();

				IPath[] includePatterns = cpe.getInclusionPatterns();
				IPath[] excludePatterns = cpe.getExclusionPatterns();
				if ((excludePatterns != null && excludePatterns.length > 0)
						|| (includePatterns != null && includePatterns.length > 0)) {
					final String[] inclusions = convertPaths(includePatterns);
					final String[] exclusions = convertPaths(excludePatterns);
					cfg.addTarget(new FilteredDirectoryTarget(
							IToolTarget.Type.SOURCE, loc, inclusions,
							exclusions));
				} else {
					cfg.addTarget(new FullDirectoryTarget(
							IToolTarget.Type.SOURCE, loc));
				}
			}
			handleOutputLocation(cfg, cpe.getOutputLocation(), toBeAnalyzed);
			break;
		case IClasspathEntry.CPE_LIBRARY:
			IPath srcPath = cpe.getSourceAttachmentPath();
			// FIX cpe.getSourceAttachmentRootPath();
			if (srcPath != null) {
				IToolTarget srcTarget = createTarget(root, cpe
						.getSourceAttachmentPath(), null);
				cfg.addTarget(createTarget(root, cpe.getPath(), srcTarget));
			} else {
				cfg.addTarget(createTarget(root, cpe.getPath(), null));
			}
			break;
		case IClasspathEntry.CPE_PROJECT:
			String projName = cpe.getPath().lastSegment();
			IProject proj = root.getProject(projName);
			setupToolForProject(cfg, handled, JavaCore.create(proj), false);
			break;
		default:
		}
	}

	private static String[] convertPaths(IPath[] patterns) {
		if (patterns == null || patterns.length == 0) {
			return null;
		}
		final String[] exclusions = new String[patterns.length];
		int i = 0;
		for (IPath exclusion : patterns) {
			exclusions[i] = exclusion.toString();
			i++;
		}
		return exclusions;
	}

	private static ToolTarget createTarget(final IWorkspaceRoot root,
			IPath libPath, IToolTarget src) {
		URI lib;
		File libFile = new File(libPath.toOSString());
		if (libFile.exists()) {
			lib = libFile.toURI();
		} else {
			IResource res = root.findMember(libPath);
			if (res == null) {
				return null;
			}
			lib = res.getLocationURI();
		}
		if (new File(lib).isDirectory()) {
			return new FullDirectoryTarget(IToolTarget.Type.AUX, lib);
		} else {
			return new JarTarget(lib);
		}
	}
}
