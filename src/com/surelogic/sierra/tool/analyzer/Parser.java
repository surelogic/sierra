package com.surelogic.sierra.tool.analyzer;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.surelogic.sierra.tool.SierraLogger;
import com.surelogic.sierra.tool.message.IdentifierType;
import com.surelogic.sierra.tool.message.Priority;
import com.surelogic.sierra.tool.message.Severity;

/**
 * XML Parser for results from the tools. Uses SAX parser.
 * 
 * @author Tanmay.Sinha
 * 
 */
public class Parser {

	private static final String PRIORITY = "priority";

	private static final String DEFAULT_PACKAGE = "Default Package";

	private String[] sourceDirectories;

	private ArtifactGenerator generator;

	private static final Logger log = SierraLogger.getLogger("Sierra");

	Parser(ArtifactGenerator generator) {
		this.generator = generator;
	}

	public void parsePMD(String fileName) {
		PMDHandler handler = new PMDHandler();
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
		FindBugsHandler handler = new FindBugsHandler();
		SAXParserFactory factory = SAXParserFactory.newInstance();

		this.sourceDirectories = sourceDirectories;

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

	class FindBugsHandler extends DefaultHandler {

		private static final String FINDBUGS = "FindBugs";

		private ArtifactGenerator.SourceLocationBuilder sourceLocation;

		private ArtifactGenerator.SourceLocationBuilder primarySourceLocation;

		private ArtifactGenerator.ArtifactBuilder artifact;

		private boolean inMethod;

		private boolean inField;

		private String elementValueHolder;

		private String fileName;

		private String completeFileName;

		private boolean inClass;

		private String relativePath;

		@Override
		public void startElement(String uri, String localName, String qName,
				Attributes attributes) throws SAXException {

			String eName = localName;

			if ("".equals(eName)) {
				eName = qName;
			}

			if ("BugInstance".equals(eName)) {
				artifact = generator.artifact();
				primarySourceLocation = artifact.primarySourceLocation();
				if (attributes != null) {
					for (int i = 0; i < attributes.getLength(); i++) {
						String aName = attributes.getLocalName(i);
						if ("".equals(aName)) {
							aName = attributes.getQName(i);
						}

						if ("type".equals(aName)) {

							artifact.findingType(FINDBUGS, attributes
									.getValue(i));
						}

						if (PRIORITY.equals(aName)) {

							int priority = Integer.valueOf(attributes
									.getValue(i));

							Priority assignedPriority = getFindBugsPriority(priority);
							Severity assignedSeverity = getFindBugsSeverity(priority);

							artifact.priority(assignedPriority).severity(
									assignedSeverity);

						}
					}
				}

			}

			if ("Method".equals(eName)) {
				sourceLocation = artifact.sourceLocation();
				inMethod = true;
				if (attributes != null) {
					String className = "";
					String packageName = "";
					for (int i = 0; i < attributes.getLength(); i++) {
						String aName = attributes.getLocalName(i);
						if ("".equals(aName)) {
							aName = attributes.getQName(i);
						}

						if ("name".equals(aName)) {
							String method = attributes.getValue(i);

							sourceLocation.type(IdentifierType.METHOD);
							sourceLocation.identifier(method);
						}

						if ("classname".equals(aName)) {

							className = attributes.getValue(i);
							int lastPeriod = className.lastIndexOf(".");
							if (lastPeriod == -1) {
								packageName = DEFAULT_PACKAGE;
							} else {

								packageName = className
										.substring(0, lastPeriod);
								className = className.substring(lastPeriod + 1);
							}
							// Why is this path an empty space?
							// -> Path is only populated for primary source
							// locations as it is used for identifying the
							// location of the file for the double click in the
							// eclipse view
							sourceLocation.className(className).packageName(
									packageName).path("");
						}

					}

				}
			}

			if ("Class".equals(eName)) {
				inClass = true;
			}
			if ("Field".equals(eName)) {
				sourceLocation = artifact.sourceLocation();
				inField = true;

				if (attributes != null) {
					String className = "";
					String packageName = "";

					for (int i = 0; i < attributes.getLength(); i++) {
						String aName = attributes.getLocalName(i);
						if ("".equals(aName)) {
							aName = attributes.getQName(i);
						}

						if ("name".equals(aName)) {
							String field = attributes.getValue(i);

							sourceLocation.type(IdentifierType.FIELD);
							sourceLocation.identifier(field);
						}

						if ("classname".equals(aName)) {

							className = attributes.getValue(i);
							int lastPeriod = className.lastIndexOf(".");
							if (lastPeriod == -1) {
								packageName = DEFAULT_PACKAGE;
							} else {

								packageName = className
										.substring(0, lastPeriod);
								className = className.substring(lastPeriod + 1);
							}

							sourceLocation.className(className).packageName(
									packageName).path("");

						}

					}

				}
			}

			if ("SourceLine".equals(eName)) {

				if (inMethod || inField) {
					String start = "";
					String end = "";

					if (attributes != null) {
						for (int i = 0; i < attributes.getLength(); i++) {
							String aName = attributes.getLocalName(i);
							if ("".equals(aName)) {
								aName = attributes.getQName(i);
							}

							if ("start".equals(aName)) {
								start = attributes.getValue(i);
								// Make the start of line number as the line of
								// code as it cannot be null
								sourceLocation.lineOfCode(Integer
										.parseInt(start));

							}

							if ("end".equals(aName)) {
								end = attributes.getValue(i);
								sourceLocation.endLine(Integer.parseInt(end));
							}

							// if ("sourceFile".equals(aName)) {
							// superClassName = attributes.getValue(i);
							// }

						}
					}
				} else if (inClass) {
					if (attributes != null) {
						for (int i = 0; i < attributes.getLength(); i++) {
							String aName = attributes.getLocalName(i);
							if ("".equals(aName)) {
								aName = attributes.getQName(i);
							}

							// if ("sourceFile".equals(aName)) {
							// superClassName = attributes.getValue(i);
							// }

						}
					}
				} else {

					String start = "0";
					String end = "0";

					if (attributes != null) {

						String className = "";
						String packageName = "";

						for (int i = 0; i < attributes.getLength(); i++) {
							String aName = attributes.getLocalName(i);
							if ("".equals(aName)) {
								aName = attributes.getQName(i);
							}

							if ("start".equals(aName)) {
								start = attributes.getValue(i);
								primarySourceLocation.lineOfCode(Integer
										.parseInt(start));
							}

							if ("end".equals(aName)) {
								end = attributes.getValue(i);
								primarySourceLocation.endLine(Integer
										.parseInt(end));
							}

							// if ("sourceFile".equals(aName)) {
							// superClassName = attributes.getValue(i);
							// }

							if ("classname".equals(aName)) {

								className = attributes.getValue(i);
								int lastPeriod = className.lastIndexOf(".");
								if (lastPeriod == -1) {
									packageName = DEFAULT_PACKAGE;
								} else {

									packageName = className.substring(0,
											lastPeriod);
									className = className
											.substring(lastPeriod + 1);
								}

							}

							if ("sourcepath".equals(aName)) {
								String sourcePath = attributes.getValue(i);
								relativePath = sourcePath;
								int lastSlash = sourcePath.lastIndexOf("/");
								fileName = sourcePath.substring(lastSlash + 1);
							}

						}

						// ASSUMPTION: Start and end for SourceLine element
						// inside the BugInstance represent the line number of
						// the bug else it is not a bug that can assigned a line
						// number and assume the lineStart as the line number

						int s = Integer.parseInt(start);

						HashGenerator hashGenerator = new HashGenerator();
						boolean fileFound = false;

						for (int i = 0; i < sourceDirectories.length; i++) {

							if (!fileFound) {
								String completePath = sourceDirectories[i]
										+ File.separator + fileName;

								String relativePathHolder = relativePath
										.replace("/", File.separator);

								if (completePath.contains(relativePathHolder)) {
									File holderFile = new File(completePath);

									if (holderFile.exists()) {
										String hash = hashGenerator.getHash(
												completePath, s);
										primarySourceLocation.hash(hash);

										completeFileName = sourceDirectories[i];
										fileFound = true;
									}
								}

							}
						}

						// The following code handles 4 cases of inner classes
						// identified by FindBugs, the first one is of kind
						// "Test", second is "Test$1", third "Test$TestInner"
						// and the last is class declared in the same file
						// "TestInner" it does different resolution for the
						// last kind, it uses the same name for the first three
						// kinds, for the last kind it adds the superclass name
						// and makes the name of format "Test$TestInner"

						String superClass = fileName.substring(0, fileName
								.length() - 5);

						log.info("________" + completeFileName);

						if (superClass.equals(className)) {

							primarySourceLocation.className(className)
									.packageName(packageName).path(
											completeFileName);
						} else {

							int dollarSign = className.indexOf("$");

							if (dollarSign != -1) {
								// The inner class is of format Test$1 or
								// Test$TestInner
								primarySourceLocation.className(className)
										.packageName(packageName).path(
												completeFileName);
							} else {

								// Use the name given by findbugs
								primarySourceLocation.className(
										superClass + "$" + className)
										.packageName(packageName).path(
												completeFileName);
							}

						}

					}

				}

			}
		}

		private Severity getFindBugsSeverity(int priority) {
			switch (priority) {
			case 1:
				return Severity.ERROR;
			case 2:
				return Severity.WARNING;
			case 3:
				return Severity.WARNING;
			case 4:
				return Severity.ERROR;
			case 5:
				return Severity.INFO;
			}
			return null;

		}

		private Priority getFindBugsPriority(int priority) {
			switch (priority) {
			case 1:
				return Priority.HIGH;
			case 2:
				return Priority.MEDIUM;
			case 3:
				return Priority.LOW;
			case 4:
				return Priority.EXPERIMENTAL;
			case 5:
				return Priority.IGNORE;
			}
			return null;
		}

		@Override
		public void endElement(String uri, String localName, String qName)
				throws SAXException {
			String eName = localName;

			if ("".equals(eName)) {
				eName = qName;
			}

			if ("Method".equals(eName)) {
				inMethod = false;
				sourceLocation.build();
			}

			if ("LongMessage".equals(eName)) {
				String message = elementValueHolder;
				artifact.message(message);
			}

			if ("Field".equals(eName)) {
				inField = false;
				sourceLocation.build();
			}

			if ("Class".equals(eName)) {
				inClass = false;
			}

			if ("BugInstance".equals(eName)) {

				primarySourceLocation.build();
				artifact.build();
			}
		}

		@Override
		public void characters(char[] ch, int start, int length)
				throws SAXException {
			String data = new String(ch, start, length);

			if (data.length() != 1) {
				elementValueHolder = data;
			}
		}
	}

	class PMDHandler extends DefaultHandler {

		private static final String PMD = "PMD";

		private ArtifactGenerator.SourceLocationBuilder sourceLocation;

		boolean hasClassName;

		private ArtifactGenerator.ArtifactBuilder artifact;

		private String fileName;

		private boolean hasPackage = false;

		@Override
		public void startElement(String namespaceURI, String lName,
				String qName, Attributes attrs) throws SAXException {

			String eName = lName; // element name

			if ("".equals(eName)) {
				eName = qName;
			}

			if ("file".equals(eName)) {

				if (attrs != null) {
					for (int i = 0; i < attrs.getLength(); i++) {
						String aName = attrs.getLocalName(i);
						if ("".equals(aName)) {
							aName = attrs.getQName(i);
						}

						if ("name".equals(aName)) {
							fileName = attrs.getValue(i);
						}
					}
				}

			}

			if ("violation".equals(eName)) {
				artifact = generator.artifact();
				sourceLocation = artifact.primarySourceLocation();
				String method = "";
				String className = "";

				if (attrs != null) {
					for (int i = 0; i < attrs.getLength(); i++) {
						String aName = attrs.getLocalName(i);
						if ("".equals(aName)) {
							aName = attrs.getQName(i);
						}

						if ("line".equals(aName)) {

							int lineNumber = Integer
									.parseInt(attrs.getValue(i));

							// The hash code generation
							HashGenerator hashGenerator = new HashGenerator();
							String hash = hashGenerator.getHash(fileName,
									lineNumber);

							sourceLocation = sourceLocation.hash(hash)
									.lineOfCode(lineNumber);

						}

						if ("package".equals(aName)) {

							hasPackage = true;
							String packageName = attrs.getValue(i);

							int lastSlash = fileName
									.lastIndexOf(File.separator);
							String sourceDirectory = fileName.substring(0,
									lastSlash);
							sourceLocation.packageName(packageName).path(
									sourceDirectory);
						}

						if ("class".equals(aName)) {
							className = attrs.getValue(i);

							int lastSlash = fileName
									.lastIndexOf(File.separator);
							String fileNameHolder = fileName.substring(
									lastSlash + 1, fileName.length());
							String superClassName = fileNameHolder.substring(0,
									fileNameHolder.length() - 5);

							if (superClassName.equals(className)) {
								sourceLocation.className(className);
							} else {
								sourceLocation.className(superClassName + "$"
										+ className);
							}
							hasClassName = true;
						}

						if ("externalInfoURL".equals(aName)) {
							// String info = attrs.getValue(i);

							// Match it with existing finding type info
						}

						if ("method".equals(aName)) {
							method = attrs.getValue(i);
							sourceLocation.type(IdentifierType.METHOD);
							sourceLocation.identifier(method);
						}

						if ("rule".equals(aName)) {
							artifact.findingType(PMD, attrs.getValue(i));
						}

						if (PRIORITY.equals(aName)) {

							int priority = Integer.valueOf(attrs.getValue(i));

							Priority assignedPriority = getPMDPriority(priority);
							Severity assignedSeverity = getPMDSeveriry(priority);

							artifact.priority(assignedPriority).severity(
									assignedSeverity);

						}
					}
				}

				if ("".equals(method)) {
					sourceLocation.type(IdentifierType.CLASS);
					sourceLocation.identifier(className);
				}
			}
		}

		private Severity getPMDSeveriry(int priority) {
			switch (priority) {
			case 1:
				return Severity.ERROR;
			case 2:
				return Severity.ERROR;
			case 3:
				return Severity.WARNING;
			case 4:
				return Severity.WARNING;
			case 5:
				return Severity.INFO;
			}
			return null;
		}

		private Priority getPMDPriority(int priority) {
			switch (priority) {
			case 1:
				return Priority.HIGH;
			case 2:
				return Priority.MEDIUM;
			case 3:
				return Priority.HIGH;
			case 4:
				return Priority.MEDIUM;
			case 5:
				return Priority.LOW;
			}
			return null;
		}

		@Override
		public void endElement(String namespaceURI, String sName, String qName)
				throws SAXException {

			String eName = sName; // element name

			if ("".equals(eName)) {
				eName = qName;
			}

			if ("violation".equals(eName)) {

				if (!hasClassName) {
					int lastSlash = fileName.lastIndexOf(File.separator);
					String fileNameHolder = fileName.substring(lastSlash + 1,
							fileName.length());
					String className = fileNameHolder.substring(0,
							fileNameHolder.length() - 5);
					sourceLocation.className(className);
				}
				hasClassName = false;

				// Populate package name as default package if there is no
				// package associated with it
				if (!hasPackage) {
					String packageName = DEFAULT_PACKAGE;
					int lastSlash = fileName.lastIndexOf(File.separator);
					String sourceDirectory = fileName.substring(0, lastSlash);
					sourceLocation.packageName(packageName).path(
							sourceDirectory);
				}
				hasPackage = false;
				sourceLocation.build();
				artifact.build();
			}

		}

		@Override
		public void characters(char[] ch, int start, int length)
				throws SAXException {
			String data = new String(ch, start, length);

			if (data.length() != 1) {
				data = data.substring(1, data.length() - 1);
				artifact.message(data);

			}
		}
	}
}
