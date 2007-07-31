package com.surelogic.sierra.tool.analyzer;

import java.io.File;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.surelogic.sierra.tool.message.IdentifierType;
import com.surelogic.sierra.tool.message.Priority;
import com.surelogic.sierra.tool.message.Severity;

class FindBugsHandler extends DefaultHandler {

	private static final String FINDBUGS = "FindBugs";

	private ArtifactGenerator.SourceLocationBuilder sourceLocation;

	private ArtifactGenerator.SourceLocationBuilder primarySourceLocation;

	private ArtifactGenerator.ArtifactBuilder artifact;

	private boolean inMethod;

	private boolean inField;

	private String[] sourceDirectories;

	private ArtifactGenerator generator;

	private String fileName;

	private String completeFileName;

	private boolean inClass;

	private String relativePath;

	private boolean inLongMessage = false;

	private StringBuilder message;

	public FindBugsHandler(ArtifactGenerator generator,
			String[] sourceDirectories) {
		super();
		this.generator = generator;
		this.sourceDirectories = sourceDirectories;
		message = new StringBuilder();
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

						artifact.findingType(FINDBUGS, attributes.getValue(i));
					}

					if (Parser.PRIORITY.equals(aName)) {

						int priority = Integer.valueOf(attributes.getValue(i));

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
							packageName = Parser.DEFAULT_PACKAGE;
						} else {

							packageName = className.substring(0, lastPeriod);
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
							packageName = Parser.DEFAULT_PACKAGE;
						} else {

							packageName = className.substring(0, lastPeriod);
							className = className.substring(lastPeriod + 1);
						}

						sourceLocation.className(className).packageName(
								packageName).path("");

					}

				}

			}
		}

		if ("LongMessage".equals(eName)) {
			inLongMessage = true;
			message.setLength(0);
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
							sourceLocation.lineOfCode(Integer.parseInt(start));

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
								packageName = Parser.DEFAULT_PACKAGE;
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

					int s = Integer.parseInt(start);

					HashGenerator hashGenerator = new HashGenerator();
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
									primarySourceLocation.hash(hashGenerator
											.getHash(completePath, s));

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

					String superClass = fileName.substring(0,
							fileName.length() - 5);

					if (superClass.equals(className)) {

						primarySourceLocation.className(className).packageName(
								packageName).path(completeFileName);
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
									superClass + "$" + className).packageName(
									packageName).path(completeFileName);
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
			artifact.message(message.toString());
			inLongMessage = false;
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
		if (inLongMessage) {
			message.append(ch, start, length);
		}
	}
}
