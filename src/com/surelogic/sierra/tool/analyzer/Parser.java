package com.surelogic.sierra.tool.analyzer;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;

import com.surelogic.sierra.tool.SierraLogger;

/**
 * XML Parser for results from the tools. Uses SAX parser.
 * 
 * @author Tanmay.Sinha
 * 
 */
public class Parser {

	public static final String PRIORITY = "priority";

	public static final String DEFAULT_PACKAGE = "Default Package";

	private ArtifactGenerator generator;

	private static final Logger log = SierraLogger.getLogger("Sierra");

	public Parser(ArtifactGenerator generator) {
		this.generator = generator;
	}

	public void parsePMD39(String fileName) {
		PMD39Handler handler = new PMD39Handler(generator);
		SAXParserFactory factory = SAXParserFactory.newInstance();
		try {
			// Parse the input
			SAXParser saxParser = factory.newSAXParser();
			saxParser.parse(new File(fileName), handler);
		} catch (SAXException se) {
			log.log(Level.SEVERE,
					"Could not parse the PMD file. Possible errors in the generated file"
							+ se);
		} catch (ParserConfigurationException e) {
			log.log(Level.SEVERE,
					"Could not parse the PMD file. Parser configuration error."
							+ e);
		} catch (IOException e) {
			log.log(Level.SEVERE, "Could not parse the PMD file. I/O Error."
					+ e);
		}
	}

	public void parsePMD40(String fileName) {
		PMD40Handler handler = new PMD40Handler(generator);
		SAXParserFactory factory = SAXParserFactory.newInstance();
		try {
			// Parse the input
			SAXParser saxParser = factory.newSAXParser();
			saxParser.parse(new File(fileName), handler);
		} catch (SAXException se) {
			log.log(Level.SEVERE,
					"Could not parse the PMD file. Possible errors in the generated file"
							+ se);
		} catch (ParserConfigurationException e) {
			log.log(Level.SEVERE,
					"Could not parse the PMD file. Parser configuration error."
							+ e);
		} catch (IOException e) {
			log.log(Level.SEVERE, "Could not parse the PMD file. I/O Error."
					+ e);
		}
	}

	public void parseFB(String fileName, String[] sourceDirectories) {
		FindBugsHandler handler = new FindBugsHandler(generator,
				sourceDirectories);
		SAXParserFactory factory = SAXParserFactory.newInstance();

		try {
			// Parse the input
			SAXParser saxParser = factory.newSAXParser();
			saxParser.parse(new File(fileName), handler);
		} catch (SAXException se) {
			log.log(Level.SEVERE,
					"Could not parse the FindBugs file. Possible errors in the generated file"
							+ se);
		} catch (ParserConfigurationException e) {
			log.log(Level.SEVERE, "Could not parse the FindBugs file." + e);
		} catch (IOException e) {
			log.log(Level.SEVERE,
					"Could not parse the FindBugs file. I/O Error." + e);
		}
	}

	@SuppressWarnings("deprecation")
	public void parseForHash(String fileName,
			Map<String, Map<Integer, Long>> hashHolder,
			String[] sourceDirectories) {
		HashHandler handler = new HashHandler(hashHolder, sourceDirectories);
		SAXParserFactory factory = SAXParserFactory.newInstance();
		try {
			// Parse the input
			SAXParser saxParser = factory.newSAXParser();
			saxParser.parse(new File(fileName), handler);
		} catch (SAXException se) {
			log.log(Level.SEVERE,
					"Could not parse the PMD file. Possible errors in the generated file"
							+ se);
		} catch (ParserConfigurationException e) {
			log.log(Level.SEVERE,
					"Could not parse the PMD file. Parser configuration error."
							+ e);
		} catch (IOException e) {
			log.log(Level.SEVERE, "Could not parse the PMD file. I/O Error."
					+ e);
		}
	}

	public void parseFB(String toolFileFB, String[] sourceDirectories,
			Map<String, Map<Integer, Long>> hashHolder) {
		FindBugsHandler handler = new FindBugsHandler(generator,
				sourceDirectories, hashHolder);
		SAXParserFactory factory = SAXParserFactory.newInstance();

		try {
			// Parse the input
			SAXParser saxParser = factory.newSAXParser();
			saxParser.parse(new File(toolFileFB), handler);
		} catch (SAXException se) {
			log.log(Level.SEVERE,
					"Could not parse the FindBugs file. Possible errors in the generated file"
							+ se);
		} catch (ParserConfigurationException e) {
			log.log(Level.SEVERE, "Could not parse the FindBugs file." + e);
		} catch (IOException e) {
			log.log(Level.SEVERE,
					"Could not parse the FindBugs file. I/O Error." + e);
		}

	}

	public void parsePMD(String toolFilePMD,
			Map<String, Map<Integer, Long>> hashHolder) {
		PMD39Handler handler = new PMD39Handler(generator, hashHolder);
		SAXParserFactory factory = SAXParserFactory.newInstance();
		try {
			// Parse the input
			SAXParser saxParser = factory.newSAXParser();
			saxParser.parse(new File(toolFilePMD), handler);
		} catch (SAXException se) {
			log.log(Level.SEVERE,
					"Could not parse the PMD file. Possible errors in the generated file"
							+ se);
		} catch (ParserConfigurationException e) {
			log.log(Level.SEVERE,
					"Could not parse the PMD file. Parser configuration error."
							+ e);
		} catch (IOException e) {
			log.log(Level.SEVERE, "Could not parse the PMD file. I/O Error."
					+ e);
		}
	}

}
