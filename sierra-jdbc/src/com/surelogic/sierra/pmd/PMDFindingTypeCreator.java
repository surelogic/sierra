package com.surelogic.sierra.pmd;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.xml.parsers.SAXParser;

import com.surelogic.common.logging.SLLogger;
import com.surelogic.common.xml.XMLUtil;
import com.surelogic.sierra.setup.AbstractFindingTypeCreator;
import com.surelogic.sierra.setup.FindingTypeInfo;

public class PMDFindingTypeCreator extends AbstractFindingTypeCreator {
    static final File missing = new File(
            "./src/com/surelogic/sierra/pmd/missingFindingTypes.txt");

    public static void main(String[] args) throws IOException {
        new PMDFindingTypeCreator().process(missing).finish();
    }

    @Override
    public void finish() throws IOException {
		final Logger log = SLLogger.getLogger();
		final SAXParser sp = XMLUtil.createSAXParser();				
		final PMDRulesXMLReader parser = new PMDRulesXMLReader() {
			@Override
			protected String getPMDURI() {
				return PMD2_0_0ToolInfoGenerator.PMD_URI;
			}			
		};
		// Load in rulesets
		final File rulesets = new File(path+"/rulesets/java");
		for(File ruleset : rulesets.listFiles()) {
			if (ruleset.getName().endsWith(".xml")) {
				XMLUtil.parseResource(log, sp, new FileInputStream(ruleset), parser, "Could not parse a PMD pattern");	
			}
		}		
		categorizeMnemonics(parser.getInfoMap());	
    }
    
	void categorizeMnemonics(Map<String, FindingTypeInfo> map) {
		final Map<String,List<String>> category2types = new HashMap<String,List<String>>();
		for(String t : mnemonics) {
			FindingTypeInfo info = map.get(t);
			output(System.out, tool, t, info);		
		
			List<String> types = category2types.get(info.category);
			if (types == null) {
				types = new ArrayList<String>();
				category2types.put(info.category, types);
			}
			types.add(t);
		}
		System.out.println("\n");
		for(Map.Entry<String,List<String>> e : category2types.entrySet()) {
			System.out.println("Category: "+e.getKey());
			for(String mnem : e.getValue()) {
				System.out.println("        <filter>");
				System.out.println("            <type>"+mnem+"</type>");
				System.out.println("            <filtered>false</filtered>");
				System.out.println("        </filter>");
			}
		}	
	}
}
