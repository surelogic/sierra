package com.surelogic.sierra.setup;

import java.io.*;
import java.util.*;

/**
 * A common superclass of tools to help create the finding type
 * information, based on the tool's artifact types 
 * 
 * @author Edwin.Chan
 */
public abstract class AbstractFindingTypeCreator {
	protected static final String TOOL = "Tool:";
	protected static final String VERSION = "Version:";
	protected static final String MNEMONIC = "Mnemonic:";
	
	protected String tool    = null;
	protected String version = null;
	protected String path    = null;
	protected final List<String> mnemonics = new ArrayList<String>();
	
	protected final AbstractFindingTypeCreator process(File missingTypes) throws IOException {
		Reader r = new FileReader(missingTypes);
		BufferedReader br = new BufferedReader(r);
		String line;
		
		while ((line = br.readLine()) != null) {
			final StringTokenizer st = new StringTokenizer(line);
			for(int i=0; i<3; i++) {
				final String tag   = st.nextToken();
				final String value = st.nextToken();
				if (TOOL.equals(tag)) {
					if (tool == null) {
						tool = value;
					} 
					else if (!tool.equals(value)){
						throw new IllegalStateException(tool+" != "+value);
					}
				}
				else if (VERSION.equals(tag)) {
					if (version == null) {
						version = value;
						if (tool == null) {
							throw new IllegalStateException("No tool defined: "+line);
						} else {
							path = "./src/com/surelogic/sierra/"+tool.toLowerCase()+value.replace('.', '_');
						}
					} 
					else if (!version.equals(value)){
						throw new IllegalStateException(version+" != "+value);
					}
				}
				else if (MNEMONIC.equals(tag)) {
					mnemonics.add(value);
				}
			}
		}
		return this;
	}
	
	public abstract void finish() throws IOException;
}
