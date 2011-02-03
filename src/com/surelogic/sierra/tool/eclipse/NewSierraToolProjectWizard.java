package com.surelogic.sierra.tool.eclipse;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

import javax.xml.transform.TransformerException;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.*;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.dialogs.WizardNewProjectCreationPage;
import org.xml.sax.SAXException;

import com.surelogic.common.core.EclipseUtility;
import com.surelogic.sierra.client.eclipse.Activator;
import com.surelogic.sierra.tool.ToolUtil;

public class NewSierraToolProjectWizard extends Wizard implements INewWizard {
	public static final String PLUGIN_PROVIDER = "Bundle-Vendor";
	private static final String EXPORT_PACKAGE = "Export-Package";
	
	private WizardNewProjectCreationPage namePage;
	private NewSierraToolProjectWizardPage detailsPage;

	public NewSierraToolProjectWizard() {
		// TODO Auto-generated constructor stub
	}

	public void init(IWorkbench workbench, IStructuredSelection selection) {
		setNeedsProgressMonitor(true);
	}
	
	@Override
	public void addPages()
	{
		super.addPages();
		namePage = new WizardNewProjectCreationPage("NewSierraToolProjectWizard");
		namePage.setTitle("Create a new Sierra tool");
		namePage.setDescription("???");
		//namePage.setImageDescriptor(null);
		
		detailsPage = new NewSierraToolProjectWizardPage(namePage);
		detailsPage.setTitle("Describe the new tool");
		detailsPage.setDescription("???");
		addPage(namePage);
		addPage(detailsPage);
	}
	
	@Override
	public boolean performFinish() {
		try	{
			final WorkspaceModifyOperation op = new WorkspaceModifyOperation() {	
				@Override
				protected void execute(IProgressMonitor monitor) {
					createProject(monitor != null ? monitor : new NullProgressMonitor());
				}
			};
			getContainer().run(false,true,op);
		}
		catch(InvocationTargetException x)
		{
			reportError(x);
			return false;
		} 
		catch(InterruptedException x) {
			reportError(x);
			return false;
		}
		return true; 

	}
	
	/**
	 * Displays an error that occured during the project creation.
	 * @param x details on the error
	 */
	private void reportError(Exception x) {
		ErrorDialog.openError(getShell(),
				"Error",
				"Problem while creating Sierra tool project",
				makeStatus(x));
	}

	/**
	 * Create an IStatus object from an exception.
	 * @param x exception to process
	 * @return IStatus status object for the above exception
	 */
	public static IStatus makeStatus(Exception x) {
		Throwable t = popThrowables(x);
		if(t instanceof CoreException)
			return ((CoreException)t).getStatus();
		else
			return new Status(IStatus.ERROR,
					Activator.PLUGIN_ID,
					IStatus.ERROR,
					x.getMessage(),
					t);
	}
	
	/**
	 * A helpful method (I hope). It walks through exceptions
	 * chains until it reaches the original one.
	 * Indeed it's not uncommon for TransformerException or
	 * SAXException to hold another exception that itself may 
	 * hold another exception.
	 * The result? Unreadable error messages where the original
	 * error (say ArrayOutOfBoundsException) is lost in a sea
	 * of TransformerException.
	 * @param t exception to process
	 * @return original exception, if any
	 */
	public static Throwable popThrowables(Throwable t)
	{
		if(t instanceof TransformerException)
		{
			if(((TransformerException)t).getCause() != null)
				return popThrowables(((TransformerException)t).getCause());
		}   
		else if(t instanceof SAXException)
		{
			if(((SAXException)t).getException() != null)
				return popThrowables(((SAXException)t).getException());
		}
		return t;
	}

	private static final String[] BUILDERS = {
		JavaCore.BUILDER_ID, 
		"org.eclipse.pde.ManifestBuilder",
		"org.eclipse.pde.SchemaBuilder",
		Builder.ID
	};
	
	private static final String[] SUBDIRS = {
		"META-INF", ".settings", "src", "bin"
	};
	
	private static class CopyInfo {
		final String src;
		final String dest;
		CopyInfo(String name) {
			src = dest = name;
		}
		CopyInfo(String from, String to) {
			src = from;
			dest = to;
		}
	}
	
	private static final CopyInfo[] TO_COPY = {
		new CopyInfo("README.txt"), 
		new CopyInfo("plugin.xml"), 
		new CopyInfo("build.properties"),
		new CopyInfo("classpath", ".classpath"), 
		new CopyInfo("org.eclipse.jdt.core.prefs", ".settings/org.eclipse.jdt.core.prefs"), 
	};
	
	private static void addAttr(StringBuilder sb, String attr, String... values) {
		sb.append(attr).append(": ");
		for(String val : values) {
			sb.append(val);
		}
		sb.append('\n');
	}
	
	protected void createProject(IProgressMonitor monitor) {
		monitor.beginTask("Creating Sierra tool project", 50 + (10*TO_COPY.length));
		try {
			final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			monitor.subTask("Creating directory");
			// Setup .project file
			final String name = namePage.getProjectName();
			IProject project = root.getProject(name);
			IProjectDescription description = ResourcesPlugin.getWorkspace().newProjectDescription(project.getName());
			if(!Platform.getLocation().equals(namePage.getLocationPath()))
				description.setLocation(namePage.getLocationPath());
			description.setNatureIds(new String[] { 
					Nature.ID, JavaCore.NATURE_ID, "org.eclipse.pde.PluginNature"
			});
			List<ICommand> commands = new ArrayList<ICommand>();
			for(String builderId : BUILDERS) {
				ICommand command = description.newCommand();
				command.setBuilderName(builderId);
				commands.add(command);
			}
			description.setBuildSpec(commands.toArray(new ICommand[commands.size()]));
			project.create(description,monitor);
			monitor.worked(10);
			
			project.open(monitor);
			/*
			project.setPersistentProperty(PluginConstants.SOURCE_PROPERTY_NAME,"src");
			project.setPersistentProperty(PluginConstants.RULES_PROPERTY_NAME,"rules");
			project.setPersistentProperty(PluginConstants.PUBLISH_PROPERTY_NAME,"publish");
			project.setPersistentProperty(PluginConstants.BUILD_PROPERTY_NAME,"false");
			*/
			
			monitor.subTask("Creating subdirectories");
			IPath projectPath = project.getFullPath();
			for(String subdir : SUBDIRS) {
				IPath dirPath = projectPath.append(subdir);
				IFolder folder = root.getFolder(dirPath);
				EclipseUtility.createFolderHelper(folder, monitor);
			}
			IPath pkgPath = projectPath.append("src/com/surelogic/"+name.replace('-', '/'));
			EclipseUtility.createFolderHelper(root.getFolder(pkgPath), monitor);
			monitor.worked(10);
			
			monitor.subTask("Creating files");
			for(CopyInfo info : TO_COPY) {
				IPath path = projectPath.append(info.dest);
				IFile file = root.getFile(path);
				InputStream source = getClass().getResourceAsStream("/resources/"+info.src);
				file.create(source, false, new SubProgressMonitor(monitor,10));				
			}			
			// Need to create META-INF/MANIFEST.MF from partial.manifest
			
			String dottedName = detailsPage.getPluginId();
			StringBuilder sb = new StringBuilder();
			addAttr(sb, ToolUtil.PLUGIN_NAME, detailsPage.getToolName());
			addAttr(sb, ToolUtil.PLUGIN_ID, dottedName, ";singleton:=true");
			addAttr(sb, ToolUtil.PLUGIN_VERSION, detailsPage.getToolVersion());
			addAttr(sb, PLUGIN_PROVIDER, detailsPage.getToolProvider());
			addAttr(sb, ToolUtil.TOOL_FACTORIES, dottedName, ".Factory");
			addAttr(sb, ToolUtil.TOOL_ID, detailsPage.getToolId());			
			addAttr(sb, ToolUtil.TOOL_WEBSITE, detailsPage.getToolWebsite());
			addAttr(sb, ToolUtil.TOOL_DESCRIPTION, detailsPage.getToolDescription());
			addAttr(sb, EXPORT_PACKAGE, dottedName);
			sb.append('\n');
			
			IPath manifestPath = projectPath.append("META-INF/MANIFEST.MF");
			IFile file = root.getFile(manifestPath);
			InputStream source = getClass().getResourceAsStream("/resources/partial.manifest");
			source = new SequenceInputStream(source, new StringBufferInputStream(sb.toString()));
			file.create(source, false, new SubProgressMonitor(monitor,10));	
			source.close();
			
			// Copy Factory.java to src/{package}/Factory.java
			String pkgDecl = "package "+dottedName+";\n";
			IPath factoryPath = 
				projectPath.append("src/"+dottedName.replace('.', '/')+"/Factory.java");
			file = root.getFile(factoryPath);
			source = getClass().getResourceAsStream("/resources/Factory.java");
			source = new SequenceInputStream(new StringBufferInputStream(pkgDecl), source);
			file.create(source, false, new SubProgressMonitor(monitor,10));		
			source.close();
		}
		catch(CoreException x) {
			reportError(x);
		}
		catch(IOException x) {
			reportError(x);
		}
		finally {
			monitor.done();
		}		
	}
}
