package com.surelogic.sierra.client.eclipse.model;

import static com.surelogic.common.tool.SureLogicToolsPropertiesUtility.combine;
import static com.surelogic.common.tool.SureLogicToolsPropertiesUtility.combineLists;
import static com.surelogic.common.tool.SureLogicToolsPropertiesUtility.getFilterFor;
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
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;

import org.eclipse.core.resources.IContainer;
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
import org.eclipse.jdt.core.IPackageDeclaration;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

import com.surelogic.common.FileUtility;
import com.surelogic.common.Pair;
import com.surelogic.common.SLUtility;
import com.surelogic.common.core.EclipseUtility;
import com.surelogic.common.core.JDTUtility;
import com.surelogic.common.core.JavaProjectResources;
import com.surelogic.common.jobs.remote.AbstractLocalSLJob;
import com.surelogic.common.jobs.remote.AbstractRemoteSLJob;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.common.tool.SureLogicToolsFilter;
import com.surelogic.common.tool.SureLogicToolsPropertiesUtility;
import com.surelogic.sierra.client.eclipse.Tools;
import com.surelogic.sierra.client.eclipse.preferences.SierraPreferencesUtility;
import com.surelogic.sierra.tool.IToolExtension;
import com.surelogic.sierra.tool.IToolFactory;
import com.surelogic.sierra.tool.SierraToolConstants;
import com.surelogic.sierra.tool.ToolUtil;
import com.surelogic.sierra.tool.message.Config;
import com.surelogic.sierra.tool.message.ToolExtension;
import com.surelogic.sierra.tool.targets.FileTarget;
import com.surelogic.sierra.tool.targets.FilteredDirectoryTarget;
import com.surelogic.sierra.tool.targets.FullDirectoryTarget;
import com.surelogic.sierra.tool.targets.IToolTarget;
import com.surelogic.sierra.tool.targets.IToolTarget.Type;
import com.surelogic.sierra.tool.targets.JarTarget;
import com.surelogic.sierra.tool.targets.ToolTarget;

/**
 * Utility class for getting configuration objects that are required to run
 * scans, it handles both project and compilation unit configs
 */
public final class ConfigGenerator {
  private static final boolean useOldFiltering = false;

  private static final String[] PLUGINS = { SierraToolConstants.MESSAGE_PLUGIN_ID, AbstractLocalSLJob.COMMON_PLUGIN_ID,
      SierraToolConstants.TOOL_PLUGIN_ID };

  private static final ConfigGenerator INSTANCE = new ConfigGenerator();
  /** The location to store tool results */
  private final File f_resultRoot = new File(SierraToolConstants.SIERRA_RESULTS_PATH);

  private final Map<String, File> pluginDirs = new HashMap<>();

  /** The number of excluded tools : Default 0 */
  private int f_numberofExcludedTools = 0;

  /** The default folder from the preference page */
  private static String getSierraPath() {
    return SierraPreferencesUtility.getSierraScanDirectory().getAbsolutePath();
  }

  private ConfigGenerator() {
    for (String id : PLUGINS) {
      pluginDirs.put(id, EclipseUtility.getInstallationDirectoryOf(id));
    }

    for (String id : Tools.getToolPluginIds()) {
      pluginDirs.put(id, EclipseUtility.getInstallationDirectoryOf(id));
    }
  }

  public static ConfigGenerator getInstance() {
    return INSTANCE;
  }

  /**
   * Get a list of configs for provided compilation units coupled with a package
   * - compilation unit map
   *
   * @param compilationUnits
   * @return list of {@link ConfigCompilationUnit}
   */
  public List<ConfigCompilationUnit> getCompilationUnitConfigs(Collection<ICompilationUnit> compilationUnits) {
    final List<ConfigCompilationUnit> configCompilationUnits = new ArrayList<>();

    Map<String, List<ICompilationUnit>> projectCompilationUnitMap = new HashMap<>();
    for (ICompilationUnit c : compilationUnits) {

      String projectNameHolder = c.getJavaProject().getElementName();

      Set<String> projectsInMap = projectCompilationUnitMap.keySet();
      List<ICompilationUnit> compilationUnitsHolder;
      if (projectsInMap.contains(projectNameHolder)) {
        compilationUnitsHolder = projectCompilationUnitMap.get(projectNameHolder);
      } else {
        compilationUnitsHolder = new ArrayList<>();

      }
      compilationUnitsHolder.add(c);
      projectCompilationUnitMap.put(projectNameHolder, compilationUnitsHolder);
    }

    for (Map.Entry<String, List<ICompilationUnit>> entry : projectCompilationUnitMap.entrySet()) {
      List<ICompilationUnit> cus = entry.getValue();
      if (!cus.isEmpty()) {
        final ConfigCompilationUnit ccu = new ConfigCompilationUnit(getCompilationUnitConfig(cus),
            getPackageCompilationUnitMap(cus));
        configCompilationUnits.add(ccu);
      }
    }

    return configCompilationUnits;

  }

  public int getNumberofExcludedTools() {
    return f_numberofExcludedTools;
  }

  private Map<String, List<String>> getPackageCompilationUnitMap(List<ICompilationUnit> compilationUnits) {
    Map<String, List<String>> packageCompilationUnitMap = new HashMap<>();

    for (ICompilationUnit c : compilationUnits) {
      try {
        IType[] types = c.getAllTypes();
        String packageName = SLUtility.JAVA_DEFAULT_PACKAGE;
        if (types.length > 0 || SLUtility.PACKAGE_INFO_JAVA.equals(c.getElementName())) {
          for (org.eclipse.jdt.core.IPackageDeclaration decl : c.getPackageDeclarations()) {
            packageName = decl.getElementName();
            break;
          }
          /*
           * String qualifiedName = types[0].getFullyQualifiedName();
           * 
           * int lastPeriod = qualifiedName.lastIndexOf('.'); if (lastPeriod !=
           * -1) { packageName = qualifiedName.substring(0, lastPeriod); }
           */
        } else {
          SLLogger.getLogger().warning("No package for " + c.getElementName());
          continue;
        }

        Set<String> packageInMap = packageCompilationUnitMap.keySet();
        List<String> compilationUnitsHolder;
        if (packageInMap.contains(packageName)) {
          compilationUnitsHolder = packageCompilationUnitMap.get(packageName);
        } else {
          compilationUnitsHolder = new ArrayList<>();

        }

        String holder = c.getElementName();

        if (holder.endsWith(".java")) {
          holder = holder.substring(0, holder.length() - 5);
        }

        compilationUnitsHolder.add(holder);
        packageCompilationUnitMap.put(packageName, compilationUnitsHolder);
      } catch (JavaModelException e) {
        SLLogger.getLogger().log(Level.SEVERE, "Error when getting compilation unit types", e);
      }

    }
    return packageCompilationUnitMap;
  }

  private Config getCompilationUnitConfig(List<ICompilationUnit> compilationUnits) {
    Config config = null;
    if (!compilationUnits.isEmpty()) {
      final ICompilationUnit firstCU = compilationUnits.get(0);
      IPath projectLoc = firstCU.getJavaProject().getResource().getLocation();
      String projectPath = projectLoc.toString();
      File baseDir = new File(projectPath);
      IJavaProject javaProject = firstCU.getJavaProject();
      final String docPrefix = computeDocumentPrefix(javaProject, true);
      final File scanDocument = new File(completeScanDocumentName(docPrefix));

      final Copier copier = new Copier(javaProject.getElementName(), projectLoc.toFile(), new File(docPrefix));
      config = copier.config;

      config.setBaseDirectory(baseDir);
      config.setProject(firstCU.getResource().getProject().getName());
      config.setDestDirectory(f_resultRoot);
      config.setScanDocument(scanDocument);
      config.setLogPath(completeLogPath(docPrefix));
      setupTools(config, javaProject);

      final List<String> excludedClasses = new ArrayList<>();
      final SureLogicToolsFilter filter = copier.getToolsFilter();
      try {
        String defaultOutputLocation = javaProject.getOutputLocation().makeRelative().toOSString();

        for (ICompilationUnit c : compilationUnits) {
          String cuPackageName = "";
          for (IPackageDeclaration pd : c.getPackageDeclarations()) {
            cuPackageName = pd.getElementName();
          }
          final IPath path = c.getResource().getFullPath();
          boolean excludeFilterMatchesCU = filter.matches(path.toFile().getAbsolutePath(), cuPackageName);
          if (excludeFilterMatchesCU) {
            for (IType t : c.getAllTypes()) {
              excludedClasses.add(t.getFullyQualifiedName());
            }
            continue; // Excluded
          }
          String outputLocation = computeOutputLocation(javaProject, c, defaultOutputLocation);
          IType[] types = c.getAllTypes();
          Set<ClassFile> classFiles = new HashSet<>();
          for (IType t : types) {

            String qualifiedName = t.getFullyQualifiedName();

            int lastPeriod = qualifiedName.lastIndexOf('.');

            String packageName;
            String javaType;
            String folder;
            if (lastPeriod != -1) {
              packageName = qualifiedName.substring(0, lastPeriod);
              packageName = packageName.replace(".", File.separator);
              javaType = qualifiedName.substring(lastPeriod + 1);
              folder = outputLocation + File.separator + packageName;
            } else {
              packageName = "";
              javaType = qualifiedName;
              folder = outputLocation;
            }

            IResource classFolder = ResourcesPlugin.getWorkspace().getRoot().findMember(folder);
            if (classFolder != null) {
              Set<IFile> files = new HashSet<>();
              getClassFiles(classFolder, javaType, files);
              for (IFile f : files) {
                classFiles.add(new ClassFile(packageName, f));
              }
            } else {
              throw new IllegalStateException("Unable to find binaries for project " + t.getJavaProject().getElementName());
            }

          }

          for (ClassFile f : classFiles) {
            String osLoc = f.second().getLocation().toOSString();
            File osFile = new File(osLoc);
            if (copyBeforeScan) {
              copier.addFileTarget(f);
            } else {
              config.addTarget(new FileTarget(IToolTarget.Type.BINARY, osFile.toURI(), null));
            }
          }

          String srcLoc = c.getResource().getLocation().toOSString();
          File srcFile = new File(srcLoc);
          IPackageFragmentRoot p = (IPackageFragmentRoot) c.getAncestor(IJavaElement.PACKAGE_FRAGMENT_ROOT);
          File rootFile = new File(p.getResource().getLocation().toOSString());
          if (copyBeforeScan) {
            copier.addFileTarget(Type.SOURCE, (IFile) c.getResource(), p.getResource());
          } else {
            config.addTarget(new FileTarget(srcFile.toURI(), rootFile.toURI()));
          }
        }

        setupToolForProject(copier, javaProject, false);
      } catch (JavaModelException e) {
        SLLogger.getLogger().log(Level.SEVERE, "Error when getting compilation unit types", e);
      } catch (CoreException e) {
        SLLogger.getLogger().log(Level.SEVERE, "Error when getting compilation unit types", e);
      } catch (IllegalStateException ise) {
        SLLogger.getLogger().log(Level.SEVERE, ise.getMessage(), ise);
      }

      // Get clean option
      // Get included dirs -project specific
      // Get excluded dirs - project specific
      config.setExcludedClasses(excludedClasses);
    }
    return config;
  }

  private String computeOutputLocation(IJavaProject p, ICompilationUnit c, String defaultOut) throws JavaModelException {
    final IPath cuPath = c.getResource().getFullPath();
    for (IClasspathEntry e : p.getRawClasspath()) {
      final IPath out = e.getOutputLocation();
      if (out != null && e.getEntryKind() == IClasspathEntry.CPE_SOURCE && e.getPath().isPrefixOf(cuPath)) {
        // FIX Need to check include/excludes
        return out.makeRelative().toOSString();
      }
    }
    return defaultOut;
  }

  public Config getProjectConfig(IJavaProject project) {
    String projectPath = project.getResource().getLocation().toString();
    File baseDir = new File(projectPath);
    final String docPrefix = computeDocumentPrefix(project, false);
    final File scanDocument = new File(completeScanDocumentName(docPrefix));

    Copier copier = new Copier(project.getElementName(), project.getProject().getLocation().toFile(), new File(docPrefix));
    Config config = copier.config;
    config.setProject(project.getProject().getName());

    try {
      setupToolForProject(copier, project, true);
    } catch (JavaModelException e) {
      SLLogger.getLogger().log(Level.SEVERE, "Error when getting output location", e);
    }

    config.setBaseDirectory(baseDir);
    config.setDestDirectory(f_resultRoot);
    config.setScanDocument(scanDocument);
    config.setLogPath(completeLogPath(docPrefix));
    setupTools(config, project);

    // Get clean option
    // Get included dirs -project specific
    // Get excluded dirs - project specific
    return config;
  }

  private String computeDocumentPrefix(IJavaProject project, boolean partial) {
    return getSierraPath() + File.separator + project.getProject().getName() + (partial ? ".partial." : ".")
        + ToolUtil.getTimeStamp();
  }

  private String completeScanDocumentName(String prefix) {
    return prefix + (USE_ZIP ? PARSED_ZIP_FILE_SUFFIX : PARSED_FILE_SUFFIX);
  }

  private String completeLogPath(String prefix) {
    return prefix + AbstractRemoteSLJob.LOG_SUFFIX;
  }

  private void setupTools(Config config, IJavaProject javaProject) {
    config.setJavaVendor(System.getProperty("java.vendor"));
    config.setJavaVersion(System.getProperty("java.version"));
    config.setMemorySize(EclipseUtility.getIntPreference(SierraPreferencesUtility.TOOL_MEMORY_MB));
    for (Map.Entry<String, File> e : pluginDirs.entrySet()) {
      //System.out.println(" FILE PATH : " + e.getValue().getAbsolutePath());
      config.putPluginDir(e.getKey(), e.getValue().getAbsolutePath());
    }
    config.setComplianceLevel(javaProject.getOption(JavaCore.COMPILER_COMPLIANCE, true));
    config.setSourceLevel(javaProject.getOption(JavaCore.COMPILER_SOURCE, true));
    config.setTargetLevel(javaProject.getOption(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, true));

    // Compute set of excluded tools
    StringBuilder excludedTools = new StringBuilder();
    f_numberofExcludedTools = 0;

    for (IToolFactory f : Tools.findToolFactories()) {
      for (final IToolExtension t : f.getExtensions()) {
        /*
         * if (t.isCore()) { // Implied by the above continue; }
         */
        final ToolExtension ext = new ToolExtension();
        ext.setTool(f.getId());
        ext.setId(t.getId());
        ext.setVersion(t.getVersion());
        config.addExtension(ext);
      }
    }
    if (f_numberofExcludedTools == 0) {
      config.setExcludedToolsList(null);
    } else {
      config.setExcludedToolsList(excludedTools.toString());
    }

  }

  static class ClassFile extends Pair<String, IFile> {
    ClassFile(String path, IFile f) {
      super(path, f);
    }
  }

  /**
   * Returns a list of all the java files in a given project that match
   * javaFileName
   *
   * @param javaFileName
   *          The name to match
   * @throws CoreException
   */
  private Set<IFile> getClassFiles(IResource resource, String javaFileName, Set<IFile> files) throws CoreException {
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
        if (f.getFileExtension() != null && f.getFileExtension().equals("class")) {

          String fileName = f.getName();
          String mainClassName = javaFileName + ".class";
          String javaTypes = javaFileName + "$";
          if (fileName.equals(mainClassName) || fileName.contains(javaTypes)) {
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
  private void getClassFilesInFolder(IFolder folder, Set<IFile> files, String javaFileName) throws CoreException {

    IResource[] resources = folder.members();

    for (IResource r : resources) {
      if (r.getType() == IResource.FILE) {
        IFile f = (IFile) r;
        if (f.getFileExtension() != null && f.getFileExtension().equalsIgnoreCase("class")) {
          String fileName = f.getName();
          String mainClassName = javaFileName + ".class";
          String javaTypes = javaFileName + "$";
          if (fileName.equals(mainClassName) || fileName.contains(javaTypes)) {
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
   *          Whether the project will be analyzed, or is simply referred to
   */
  private static void setupToolForProject(final Copier cfg, IJavaProject p, final boolean toBeAnalyzed) throws JavaModelException {
    setupToolForProject(cfg, new HashSet<IJavaProject>(), p, toBeAnalyzed);
  }

  private static void setupToolForProject(final Copier copier, Set<IJavaProject> handled, IJavaProject p,
      final boolean toBeAnalyzed) throws JavaModelException {
    if (handled.contains(p)) {
      return;
    }
    handled.add(p);
    final Config cfg = copier.config;
    if (toBeAnalyzed && copier.getToolsFilter() != null) {
      configureExcludedClasses(cfg, p, copier.getToolsFilter());
    }
    final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
    final String[] packagesAsPaths = convertPkgsToSierraStyle(copier.combinedPackages);
    for (IClasspathEntry cpe : p.getResolvedClasspath(true)) {
      handleClasspathEntry(copier, handled, toBeAnalyzed, root, copier.combinedFolders, packagesAsPaths, cpe);
    }
    handleOutputLocation(copier, p.getOutputLocation(), packagesAsPaths, toBeAnalyzed);
  }

  private static void configureExcludedClasses(Config cfg, IJavaProject p, SureLogicToolsFilter filter) throws JavaModelException {
    final List<String> excluded = new ArrayList<>();
    JavaProjectResources jpr = JDTUtility.collectAllResources(p, null);
    for (ICompilationUnit cu : JDTUtility.applyToolsFilter(jpr.cus, filter, false)) {
      excluded.add(JDTUtility.computeQualifiedName(cu));
    }
    cfg.setExcludedClasses(excluded);
  }

  static String[] convertPkgsToSierraStyle(String[] pkgs) {
    if (pkgs == null || pkgs.length == 0) {
      return SLUtility.EMPTY_STRING_ARRAY;
    }
    final String[] paths = new String[pkgs.length];
    int i = 0;
    for (String p : pkgs) {
      paths[i] = p.replace('.', '/').replaceAll("\\*", "**");
      i++;
    }
    return paths;
  }

  private static void handleOutputLocation(final Copier cfg, IPath outLoc, String[] excludedPkgs, final boolean toBeAnalyzed) {
    if (outLoc == null) {
      return;
    }
    final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
    IResource res = root.findMember(outLoc);
    if (res == null || !res.exists()) {
      return;
    }
    final URI out = res.getLocationURI();
    final IToolTarget.Type type = toBeAnalyzed ? IToolTarget.Type.BINARY : IToolTarget.Type.AUX;
    if (useOldFiltering && excludedPkgs != null && excludedPkgs.length > 0) {
      if (copyBeforeScan) {
        cfg.addFilteredDirTarget(type, outLoc, res, null, excludedPkgs);
        cfg.addDirTarget(IToolTarget.Type.AUX, outLoc, res);
      } else {
        cfg.config.addTarget(new FilteredDirectoryTarget(type, out, null, excludedPkgs));
        cfg.config.addTarget(new FullDirectoryTarget(IToolTarget.Type.AUX, out));
      }
    } else {
      if (copyBeforeScan) {
        cfg.addDirTarget(type, outLoc, res);
      } else {
        cfg.config.addTarget(new FullDirectoryTarget(type, out));
      }
    }
  }

  private static void handleClasspathEntry(final Copier copier, Set<IJavaProject> handled, final boolean toBeAnalyzed,
      final IWorkspaceRoot root, String[] excludedPaths, String[] excludedPkgs, IClasspathEntry cpe) throws JavaModelException {
    switch (cpe.getEntryKind()) {
    case IClasspathEntry.CPE_SOURCE:
      if (toBeAnalyzed) {
        if (useOldFiltering) {
          // Check if excluded
          final String path = cpe.getPath().toPortableString();
          for (String excluded : excludedPaths) {
            if (path.equals(excluded)) {
              return;
            }
          }
        }
        IResource res = root.findMember(cpe.getPath());
        URI loc = res.getLocationURI();

        IPath[] includePatterns = cpe.getInclusionPatterns();
        IPath[] excludePatterns = cpe.getExclusionPatterns();
        if (excludePatterns != null && excludePatterns.length > 0 || includePatterns != null && includePatterns.length > 0) {
          final String[] inclusions = convertPaths(includePatterns);
          final String[] exclusions;
          if (useOldFiltering && excludedPkgs.length > 0) {
            // Fold the exclude pkgs in with the exclude patterns
            Set<String> temp = new HashSet<>();
            for (String p : convertPaths(excludePatterns)) {
              temp.add(p);
            }
            for (String p : excludedPkgs) {
              temp.add(p);
            }
            exclusions = temp.toArray(SLUtility.EMPTY_STRING_ARRAY);
          } else {
            exclusions = convertPaths(excludePatterns);
          }
          if (copyBeforeScan) {
            copier.addFilteredDirTarget(IToolTarget.Type.SOURCE, cpe.getPath(), res, inclusions, exclusions);
          } else {
            copier.config.addTarget(new FilteredDirectoryTarget(IToolTarget.Type.SOURCE, loc, inclusions, exclusions));
          }
        } else if (useOldFiltering && excludedPkgs.length > 0) {
          if (copyBeforeScan) {
            copier.addFilteredDirTarget(IToolTarget.Type.SOURCE, cpe.getPath(), res, null, excludedPkgs);
          } else {
            copier.config.addTarget(new FilteredDirectoryTarget(IToolTarget.Type.SOURCE, loc, null, excludedPkgs));
          }
        } else {
          if (copyBeforeScan) {
            copier.addDirTarget(IToolTarget.Type.SOURCE, cpe.getPath(), res);
          } else {
            copier.config.addTarget(new FullDirectoryTarget(IToolTarget.Type.SOURCE, loc));
          }
        }
      }
      handleOutputLocation(copier, cpe.getOutputLocation(), excludedPkgs, toBeAnalyzed);
      break;
    case IClasspathEntry.CPE_LIBRARY:
      IPath srcPath = cpe.getSourceAttachmentPath();
      // FIX cpe.getSourceAttachmentRootPath();
      if (srcPath != null) {
        IToolTarget srcTarget = createTarget(copier, root, cpe.getSourceAttachmentPath(), null);
        copier.config.addTarget(createTarget(copier, root, cpe.getPath(), srcTarget));
      } else {
        copier.config.addTarget(createTarget(copier, root, cpe.getPath(), null));
      }
      break;
    case IClasspathEntry.CPE_PROJECT:
      String projName = cpe.getPath().lastSegment();
      IProject proj = root.getProject(projName);
      setupToolForProject(copier, handled, JavaCore.create(proj), false);
      break;
    default:
    }
  }

  static String[] makeAbsolute(String project, String[] excludedPaths) {
    String[] rv = new String[excludedPaths.length];
    for (int i = 0; i < rv.length; i++) {
      rv[i] = '/' + project + '/' + excludedPaths[i];
    }
    return rv;
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

  private static ToolTarget createTarget(Copier copier, final IWorkspaceRoot root, IPath libPath, IToolTarget src) {
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

    if (copyBeforeScan && (!lib.getScheme().equals("file") || !new File(lib).exists())) {
      lib = copier.copyRecursive(root, libPath);
    }
    if (new File(lib).isDirectory()) {
      return new FullDirectoryTarget(IToolTarget.Type.AUX, lib);
    } else {
      return new JarTarget(lib);
    }
  }

  static final boolean copyBeforeScan = true;

  /**
   * Helper class to copy resources into a temp dir and to map targets to those
   * copies
   *
   * @author edwin
   */
  class Copier {
    final File tmpDir;
    final Config config = new Config();
    final SureLogicToolsFilter filter;
    final String[] combinedFolders;
    final String[] combinedPackages;

    Copier(String projectName, File projectLocation, File tmp) {
      tmpDir = tmp;

      final Properties props = SureLogicToolsPropertiesUtility
          .readFileOrNull(new File(projectLocation, SLUtility.SL_TOOLS_PROPS_FILE));
      if (props != null) {
        final String[] excludedSourceFolders = makeAbsolute(projectName,
            SureLogicToolsPropertiesUtility.getExcludedSourceFolders(props));
        final String[] excludedPackagePatterns = SureLogicToolsPropertiesUtility.getExcludedPackagePatterns(props);
        final String[] bytecodeSourceFolders = makeAbsolute(projectName,
            SureLogicToolsPropertiesUtility.getBytecodeSourceFolders(props));
        final String[] bytecodePackagePatterns = SureLogicToolsPropertiesUtility.getBytecodePackagePatterns(props);
        final SureLogicToolsFilter excludeFilter = getFilterFor(excludedSourceFolders, excludedPackagePatterns);
        final SureLogicToolsFilter bytecodeFilter = getFilterFor(bytecodeSourceFolders, bytecodePackagePatterns);
        config.initFromToolsProps(props, excludedSourceFolders, excludedPackagePatterns);
        filter = combine(excludeFilter, bytecodeFilter);
        combinedFolders = combineLists(excludedSourceFolders, bytecodeSourceFolders);
        combinedPackages = combineLists(excludedPackagePatterns, bytecodePackagePatterns);
      } else {
        filter = null;
        combinedFolders = SLUtility.EMPTY_STRING_ARRAY;
        combinedPackages = SLUtility.EMPTY_STRING_ARRAY;
      }
    }

    SureLogicToolsFilter getToolsFilter() {
      return filter;
    }

    void addFileTarget(ClassFile cf) {
      String pkg = cf.first();
      String dest = pkg == null || pkg.length() == 0 ? cf.second().getName() : pkg + '/' + cf.second().getName();
      URI mappedTarget = copy(dest, cf.second());
      if (mappedTarget != null) {
        config.addTarget(new FileTarget(Type.BINARY, mappedTarget, null));
      }
    }

    void addFileTarget(Type type, IFile target, IResource root) {
      String path = computeRelativePath(target, root);
      URI mappedTarget = copy(path, target);
      config.addTarget(new FileTarget(type, mappedTarget, tmpDir.toURI())); // TODO
      // is
      // the
      // root
      // correct?
    }

    String computeRelativePath(IResource target, IResource root) {
      return relPathHelper(target, root).toString();
    }

    private StringBuilder relPathHelper(IResource here, IResource root) {
      if (here == root || here == null) {
        return new StringBuilder();
      }
      StringBuilder sb = relPathHelper(here.getParent(), root);
      if (sb.length() > 0) {
        sb.append('/');
      }
      sb.append(here.getName());
      return sb;
    }

    void addDirTarget(Type type, IPath outLoc, IResource res) {
      URI mapped = copyResources(outLoc, res, nullFilter);
      if (mapped != null) {
        config.addTarget(new FullDirectoryTarget(type, mapped));
      }
    }

    void addFilteredDirTarget(final Type type, IPath outLoc, IResource res, final String[] inclusions, final String[] exclusions) {
      final CopyFilter f = new CopyFilter() {
        final FilteredDirectoryTarget filter = new FilteredDirectoryTarget(type, null, inclusions, exclusions);

        @Override
        public boolean include(String relativePath) {
          return !filter.exclude(relativePath);
        }
      };
      URI mapped = copyResources(outLoc, res, f);
      if (mapped != null) {
        // Filtering handled above
        config.addTarget(new FilteredDirectoryTarget(type, mapped, inclusions, exclusions));
      }
    }

    private URI copy(String relativePath, IFile f) {
      final File dest = new File(tmpDir, relativePath);
      dest.getParentFile().mkdirs();
      try {
        FileUtility.copy(f.getFullPath().toString(), f.getContents(), dest);
      } catch (CoreException e) {
        e.printStackTrace();
        return null;
      }
      return dest.toURI();
    }

    private URI copyResources(IPath relative, IResource res, CopyFilter filter) {
      final File destRoot = new File(tmpDir, relative.toString());
      try {
        boolean copied = copyResources(res, destRoot, filter, null);
        if (!copied) {
          return null;
        }
      } catch (CoreException e) {
        e.printStackTrace();
        return null;
      }
      return destRoot.toURI();
    }

    private boolean copyResources(IResource res, File dest, CopyFilter filter, String relativePath) throws CoreException {
      if (relativePath != null && !filter.include(relativePath)) {
        return false; // TODO is this right?
      }
      if (res instanceof IFile) {
        final IFile f = (IFile) res;
        dest.mkdirs();
        return FileUtility.copy(f.getFullPath().toString(), f.getContents(), new File(dest, f.getName()));
      } else { // Assumed to be container
        final String updatedPath;
        final File updatedDest;
        if (relativePath == null) {
          updatedPath = "";
          updatedDest = dest;
        } else {
          updatedPath = relativePath.length() > 0 ? relativePath + '/' + res.getName() : res.getName();
          updatedDest = new File(dest, res.getName());
        }
        final IContainer c = (IContainer) res;
        boolean copied = false;
        for (IResource child : c.members()) {
          copied |= copyResources(child, updatedDest, filter, updatedPath);
        }
        return copied;
      }
    }

    // Primarily for jars / directories of .class files
    URI copyRecursive(IWorkspaceRoot root, IPath libPath) {
      File libFile = new File(libPath.toOSString());
      if (libFile.exists()) {
        File dest = new File(tmpDir, libPath.toString());
        if (libFile.isFile()) {
          dest.getParentFile().mkdirs();
        } else {
          dest.mkdirs();
        }
        FileUtility.recursiveCopy(libFile, dest);
        return dest.toURI();
      } else {
        final IResource res = root.findMember(libPath);
        if (res == null) {
          return null;
        }
        final String relativePath = computeRelativePath(res, root);
        if (res instanceof IFile) {
          return copy(relativePath, (IFile) res);
        } else {
          return copyResources(res.getFullPath(), res, nullFilter);
        }
      }
    }
  }

  interface CopyFilter {
    boolean include(String relativePath);
  }

  static final CopyFilter nullFilter = new CopyFilter() {
    @Override
    public boolean include(String relativePath) {
      return true;
    }
  };

  public boolean copyBeforeScan() {
    return copyBeforeScan;
  }
}
