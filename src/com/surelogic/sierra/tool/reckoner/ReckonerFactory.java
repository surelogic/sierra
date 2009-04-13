package com.surelogic.sierra.tool.reckoner;

import java.io.File;
import java.util.*;
import java.util.logging.Level;

import com.surelogic.common.jobs.*;
import com.surelogic.sierra.tool.*;
import com.surelogic.sierra.tool.analyzer.ILazyArtifactGenerator;
import com.surelogic.sierra.tool.message.Config;

public class ReckonerFactory extends AbstractToolFactory {	
	public Set<ArtifactType> getArtifactTypes() {
		// TODO Auto-generated method stub
		return Collections.emptySet();
	}

	@Override
	public List<File> getRequiredJars(Config config) {
		final List<File> jars = super.getRequiredJars(config);
		final boolean debug = LOG.isLoggable(Level.FINE);
		/*
		  findJars(jars, new File(config.getToolsDirectory(), "reckoner/lib"));
		  jars.add(new File(config.getToolsDirectory(), "reckoner/reckoner.jar"));
		 */

		// Created temporarily to reuse code accessing config
		final AbstractTool util = new AbstractTool(this, config) {
			public Set<ArtifactType> getArtifactTypes() {
				throw new UnsupportedOperationException();
			}
			public SLStatus run(SLProgressMonitor monitor) {
				throw new UnsupportedOperationException();
			}
		};
		
		// TODO remove special case to save spaces
		// 
		// Add all the plugins needed by Reckoner (e.g. JDT Core and
		// company)
		for (String id : config.getPluginDirs().keySet()) {
			if (id.startsWith("org.eclipse")) {
				util.addPluginToPath(debug, jars, id);
			} else {
				//System.out.println("Unused: "+id);
			}
		}
		/*
		  for (String id : required) {
			  // FIX what about transitive dependencies?
			  addPluginToPath(debug, jars, id);
		  }
		 */
		return jars;
	}

	@Override
	protected IToolInstance create(Config config,
			ILazyArtifactGenerator generator, boolean close) {
		return new Reckoner1_0Tool(this, config, generator, close);
	}
}
