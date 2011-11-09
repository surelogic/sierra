package com.surelogic.sierra.tool.eclipse;

import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import com.surelogic.common.core.builder.AbstractJavaBuilder;

public class Builder extends AbstractJavaBuilder {
	public static final String ID = "com.surelogic.sierra.client.eclipse.sierraToolBuilder";
	
	public Builder() {
		// TODO Auto-generated constructor stub
	}

	@Override
	protected IProject[] build(int kind, Map args, IProgressMonitor monitor)
	throws CoreException {
		// TODO Auto-generated method stub
		return null; // No more calls
	}
}
