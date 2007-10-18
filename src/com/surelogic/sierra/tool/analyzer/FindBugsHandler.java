package com.surelogic.sierra.tool.analyzer;

import java.io.File;
import java.util.Map;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.surelogic.common.SLProgressMonitor;
import com.surelogic.sierra.tool.SierraConstants;
import com.surelogic.sierra.tool.message.IdentifierType;
import com.surelogic.sierra.tool.message.MessageArtifactFileGenerator;
import com.surelogic.sierra.tool.message.Priority;
import com.surelogic.sierra.tool.message.Severity;

/**
 * The parser for FindBugs 1.2.1 results
 * 
 * @author Tanmay.Sinha
 * 
 */
class FindBugsHandler extends DefaultHandler {

	private static final String FINDBUGS = "FindBugs";
	private static final String FINDBUGS_VERSION = "1.2.1";

	private ArtifactGenerator.SourceLocationBuilder sourceLocation;

	private ArtifactGenerator.SourceLocationBuilder primarySourceLocation;

	private ArtifactGenerator.ArtifactBuilder artifact;

	private ArtifactGenerator.ErrorBuilder error;

	private boolean inMethod;

	private boolean inField;

	private final String[] sourceDirectories;

	private final MessageArtifactFileGenerator generator;

	private String fileName;

	private boolean inClass;

	private String relativePath;

	private boolean inLongMessage = false;

	private SLProgressMonitor monitor = null;

	// private static final Logger log = SierraLogger.getLogger("Sierra");

	@SuppressWarnings("unused")
	private Map<String, Map<Integer, Long>> hashHolder;
	private boolean inErrorMessage = false;
	private final StringBuilder message;
	private final StringBuilder errorMessage;

	public FindBugsHandler(MessageArtifactFileGenerator generator,
			String[] sourceDirectories) {
		super();
		this.generator = generator;
		this.sourceDirectories = sourceDirectories;
		message = new StringBuilder();
		errorMessage = new StringBuilder();
	}

	public FindBugsHandler(MessageArtifactFileGenerator generator,
			String[] sourceDirectories, SLProgressMonitor monitor) {
		super();
		this.generator = generator;
		this.sourceDirectories = sourceDirectories;
		message = new StringBuilder();
		errorMessage = new StringBuilder();

		this.monitor = monitor;
	}

	@Deprecated
	public FindBugsHandler(MessageArtifactFileGenerator generator,
			String[] sourceDirectories,
			Map<String, Map<Integer, Long>> hashHolder) {
		super();
		this.hashHolder = hashHolder;
		this.generator = generator;
		this.sourceDirectories = sourceDirectories;
		message = new StringBuilder();
		errorMessage = new StringBuilder();
	}

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

						artifact.findingType(FINDBUGS, FINDBUGS_VERSION,
								attributes.getValue(i));
					}

					if (SierraConstants.PRIORITY.equals(aName)) {

						int priority = Integer.valueOf(attributes.getValue(i));

						Priority assignedPriority = getFindBugsPriority(priority);
						Severity assignedSeverity = getFindBugsSeverity(priority);

						artifact.priority(assignedPriority).severity(
								assignedSeverity);

					}
				}
			}

		}

		if ("Exception".equals(eName)) {
			inErrorMessage = true;
			errorMessage.setLength(0);
		}

		if ("MissingClass".equals(eName)) {
			inErrorMessage = true;
			errorMessage.setLength(0);
		}

		if ("Method".equals(eName)) {
			sourceLocation = artifact.sourceLocation();
			inMethod = true;
			if (attributes != null) {
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

				}

			}
		}

		if ("Class".equals(eName)) {
			sourceLocation = artifact.sourceLocation();
			inClass = true;
			if (attributes != null) {

				for (int i = 0; i < attributes.getLength(); i++) {
					String aName = attributes.getLocalName(i);
					if ("".equals(aName)) {
						aName = attributes.getQName(i);
					}

				}

			}

		}
		if ("Field".equals(eName)) {
			sourceLocation = artifact.sourceLocation();
			inField = true;

			if (attributes != null) {

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

				}

			}
		}

		if ("LongMessage".equals(eName)) {
			inLongMessage = true;
			message.setLength(0);
		}

		if ("SourceLine".equals(eName)) {

			if (inMethod || inField || inClass) {
				String start = "";
				String end = "";

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
							// Make the start of line number as the line of
							// code as it cannot be null
							sourceLocation.lineOfCode(Integer.parseInt(start));

						}

						if ("classname".equals(aName)) {

							className = attributes.getValue(i);
							int lastPeriod = className.lastIndexOf(".");
							if (lastPeriod == -1) {
								packageName = SierraConstants.DEFAULT_PACKAGE_PARENTHESIS;
							} else {

								packageName = className
										.substring(0, lastPeriod);
								className = className.substring(lastPeriod + 1);
							}

						}
						if ("end".equals(aName)) {
							end = attributes.getValue(i);
							sourceLocation.endLine(Integer.parseInt(end));
						}

						if ("sourcepath".equals(aName)) {
							String relativePath = attributes.getValue(i);
							int lastSlash = relativePath.lastIndexOf("/");
							fileName = relativePath.substring(lastSlash + 1);
						}

					}

					// The following code handles 4 cases of inner classes
					// identified by FindBugs, the first one is of kind
					// "Test", second is "Test$1", third "Test$TestInner"
					// and the last is class declared in the same file
					// "TestInner" it does different resolution for the
					// last kind, it uses the same name for the first three
					// kinds, for the last kind it adds the filename
					// and makes the name of format "Test$TestInner"

					String javaFileName = fileName.substring(0, fileName
							.length() - 5);

					sourceLocation.compilation(javaFileName);
					sourceLocation.className(className);
					sourceLocation.packageName(packageName);

					// if (javaFileName.equals(className)) {
					//
					// sourceLocation.className(className).packageName(
					// packageName);
					// } else {
					//
					// int dollarSign = className.indexOf("$");
					//
					// if (dollarSign != -1) {
					// // The inner class or a default of format Test$1 or
					// // Test$TestInner
					// sourceLocation.className(className).packageName(
					// packageName);
					// } else {
					//
					// // Use the name given by findbugs
					// sourceLocation.className(
					// javaFileName + "$" + className)
					// .packageName(packageName);
					// }
					//
					// }
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
							primarySourceLocation
									.endLine(Integer.parseInt(end));
						}

						// if ("sourceFile".equals(aName)) {
						// superClassName = attributes.getValue(i);
						// }

						if ("classname".equals(aName)) {

							className = attributes.getValue(i);
							int lastPeriod = className.lastIndexOf(".");
							if (lastPeriod == -1) {
								packageName = SierraConstants.DEFAULT_PACKAGE_PARENTHESIS;
							} else {

								packageName = className
										.substring(0, lastPeriod);
								className = className.substring(lastPeriod + 1);
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

					int lineNumber = Integer.parseInt(start);

					HashGenerator hashGenerator = HashGenerator.getInstance();
					boolean fileFound = false;

					for (int i = 0; i < sourceDirectories.length; i++) {

						if (!fileFound) {
							String completePath = sourceDirectories[i]
									+ File.separator + fileName;

							String relativePathHolder = relativePath.replace(
									"/", File.separator);

							if (completePath.contains(relativePathHolder)) {
								File holderFile = new File(completePath);

								if (holderFile.exists()) {

									// generator.addFile(completePath);
									if (monitor != null) {
										monitor
												.subTask("Calculating hash values (FindBugs) for "
														+ completePath);
									}
									Long hashValue = hashGenerator.getHash(
											completePath, lineNumber);

									primarySourceLocation.hash(hashValue);
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
					// kinds, for the last kind it adds the filename
					// and makes the name of format "Test$TestInner"

					String javaFileName = fileName.substring(0, fileName
							.length() - 5);

					primarySourceLocation.compilation(javaFileName);
					primarySourceLocation.className(className);
					primarySourceLocation.packageName(packageName);
					// if (superClass.equals(className)) {
					//
					// primarySourceLocation.className(className).packageName(
					// packageName);
					// } else {
					//
					// int dollarSign = className.indexOf("$");
					//
					// if (dollarSign != -1) {
					// // The inner class is of format Test$1 or
					// // Test$TestInner
					// primarySourceLocation.className(className)
					// .packageName(packageName);
					// } else {
					//
					// // Use the name given by findbugs
					// primarySourceLocation.className(
					// superClass + "$" + className).packageName(
					// packageName);
					// }
					//
					// }

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
			artifact.message(message.toString());
			inLongMessage = false;
		}

		if ("Exception".equals(eName)) {
			error = generator.error();
			error.message("Exception : " + errorMessage.toString()).tool(
					FINDBUGS);
			error.build();
		}

		if ("MissingClass".equals(eName)) {
			error = generator.error();
			error.message("Missing Class : " + errorMessage.toString()).tool(
					FINDBUGS);
			error.build();
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
			if (monitor != null) {
				monitor.worked(1);
			}
		}
	}

	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		if (inLongMessage) {
			message.append(ch, start, length);
		} else if (inErrorMessage) {
			errorMessage.append(ch, start, length);
		}
	}
}
