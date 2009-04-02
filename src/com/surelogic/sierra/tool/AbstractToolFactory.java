package com.surelogic.sierra.tool;

import java.io.File;

public abstract class AbstractToolFactory implements IToolFactory {
	public boolean isProduction() {
		return true;
	}
	
	public void init(File toolHome) {
		// Nothing to do
	}
	
	@Override
	public final int hashCode() {
		return this.getClass().hashCode();
	}
	
	@Override
	public final boolean equals(Object o) {
		if (o instanceof AbstractToolFactory) {
			return this.getClass().equals(o.getClass());
		}
		return false;
	}
}
