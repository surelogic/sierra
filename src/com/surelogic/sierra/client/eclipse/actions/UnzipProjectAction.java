package com.surelogic.sierra.client.eclipse.actions;

import java.io.*;
import java.net.URL;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

import com.surelogic.common.eclipse.EclipseUtility;

public class UnzipProjectAction implements IWorkbenchWindowActionDelegate {
	private final String project;
	
	public UnzipProjectAction(String project) {
		this.project = project;
	}
	
	public void init(IWorkbenchWindow window) {
		// TODO Auto-generated method stub
	}

	public void selectionChanged(IAction action, ISelection selection) {
		// TODO Auto-generated method stub
	}
	
	public void run(IAction action) {
		URL source = getClass().getResource("/resources/"+project+".zip");
		try {
			EclipseUtility.unzipToWorkspace(source);
		} catch (CoreException e) {
			e.printStackTrace(); // TODO
		} catch (IOException e) {
			e.printStackTrace(); // TODO
		}
	}

	public void dispose() {
		// TODO Auto-generated method stub
	}
}
