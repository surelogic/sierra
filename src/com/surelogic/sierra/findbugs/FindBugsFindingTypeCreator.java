package com.surelogic.sierra.findbugs;

import java.io.*;
import java.util.*;
import java.util.logging.Logger;

import javax.xml.parsers.SAXParser;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.surelogic.common.logging.SLLogger;
import com.surelogic.common.xml.XMLUtil;
import com.surelogic.sierra.setup.AbstractFindingTypeCreator;

public class FindBugsFindingTypeCreator extends AbstractFindingTypeCreator {
	// Tool: FindBugs Version: 1.3.6 Mnemonic: RpC_REPEATED_CONDITIONAL_TEST
	static final File missing = new File("./src/com/surelogic/sierra/findbugs/missingFindingTypes.txt");
	
	// category in findbugs.xml
	// descriptions in message.xml
	
	public static void main(String[] args) throws IOException {
		new FindBugsFindingTypeCreator().process(missing).finish();		
	}

	@Override
	public void finish() throws IOException {
		final Logger log = SLLogger.getLogger();
		final SAXParser sp = XMLUtil.createSAXParser();
		FindBugsMessageParser msgParser = new FindBugsMessageParser();
		XMLUtil.parseResource(log, sp, new FileInputStream(path+File.separator+"messages.xml"), 
				              msgParser, "Could not parse a FindBugs pattern");	
		FindBugsXMLParser fbParser = new FindBugsXMLParser(tool, mnemonics, msgParser.getInfoMap());
		XMLUtil.parseResource(log, sp, new FileInputStream(path+File.separator+"findbugs.xml"), 
				              fbParser, "Could not parse a FindBugs pattern");
		fbParser.categorizeMnemonics();	
	}
	
	static class FindBugsXMLParser extends DefaultHandler {
		private final Map<String, FindBugsMessageParser.Info> infoMap;
		private final List<String> mnemonics;
		private final String tool;
		private final Map<String,List<String>> category2types = new HashMap<String,List<String>>();

		FindBugsXMLParser(String tool, List<String> mnemonics, Map<String, FindBugsMessageParser.Info> infoMap) {
			this.infoMap = infoMap;
			this.mnemonics = mnemonics;
			this.tool = tool;
		}

		@Override
		public void startElement(String uri, String localName, String qName,
				Attributes attributes) throws SAXException {
			if (FindBugsParser.BUG_PATTERN.equals(localName)) {
				final String mnemonic = attributes.getValue(FindBugsParser.TYPE);
				if (!mnemonics.contains(mnemonic)) {
					return;
				}
				final String category = attributes.getValue(FindBugsParser.CATEGORY);
				List<String> types = category2types.get(category);
				if (types == null) {
					types = new ArrayList<String>();
					category2types.put(category, types);
				}
				types.add(mnemonic);
				
				System.out.println("  <findingType>");
				System.out.println("    <id>"+mnemonic+"</id>");
				System.out.println("    <artifact tool=\""+tool+"\"	mnemonic=\""+mnemonic+"\"/>");
				final FindBugsMessageParser.Info info = infoMap.get(mnemonic);
				System.out.println("    <shortMessage>"+info.shortDesc+"</shortMessage>");
				//System.out.println("LongDesc:  "+info.longDesc);
				System.out.println("    <info>");
				System.out.println("      <![CDATA["+info.details+"]]>");
				System.out.println("    </info>");
				System.out.println("    <name>"+info.shortDesc+"</name>");
				System.out.println("  </findingType>");
			}
		}
		
		void categorizeMnemonics() {
			System.out.println("\n");
			for(Map.Entry<String,List<String>> e : category2types.entrySet()) {
				System.out.println("Category: "+e.getKey());
				for(String mnem : e.getValue()) {
					System.out.println("    <findingType>"+mnem+"</findingType>");
				}
			}	
		}
	}
}
