package com.surelogic.sierra.tool.analyzer;

import java.io.File;
import java.util.Map;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.surelogic.sierra.tool.message.IdentifierType;
import com.surelogic.sierra.tool.message.Priority;
import com.surelogic.sierra.tool.message.Severity;

class PMDHandler extends DefaultHandler {

	private ArtifactGenerator generator;

	private static final String PMD = "PMD";

	private ArtifactGenerator.SourceLocationBuilder sourceLocation;

	boolean hasClassName;

	private ArtifactGenerator.ArtifactBuilder artifact;

	private String fileName;

	private boolean hasPackage = false;

	private boolean inViolation;

	private StringBuilder message;

	private String messageHolder;

	@SuppressWarnings("unused")
	private Map<String, Map<Integer, Long>> hashHolder;

	public PMDHandler(ArtifactGenerator generator) {
		super();
		this.generator = generator;
		message = new StringBuilder();
	}

	public PMDHandler(ArtifactGenerator generator,
			Map<String, Map<Integer, Long>> hashHolder) {
		super();
		this.hashHolder = hashHolder;
		this.generator = generator;
		message = new StringBuilder();
	}

	@Override
	public void startElement(String namespaceURI, String lName, String qName,
			Attributes attrs) throws SAXException {

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
			inViolation = true;
			message.setLength(0);

			if (attrs != null) {
				for (int i = 0; i < attrs.getLength(); i++) {
					String aName = attrs.getLocalName(i);
					if ("".equals(aName)) {
						aName = attrs.getQName(i);
					}

					if ("line".equals(aName)) {

						int lineNumber = Integer.parseInt(attrs.getValue(i));

						// The hash code generation
						HashGenerator hashGenerator = HashGenerator
								.getInstance();
						Long hashValue = hashGenerator.getHash(fileName,
								lineNumber);
						// Long hashValue = hashHolder.get(fileName).get(
						// (Integer) lineNumber);

						// System.out.println(hashValue);

						sourceLocation = sourceLocation.hash(hashValue)
								.lineOfCode(lineNumber);

					}

					if ("package".equals(aName)) {

						hasPackage = true;
						String packageName = attrs.getValue(i);

						int lastSlash = fileName.lastIndexOf(File.separator);
						String sourceDirectory = fileName.substring(0,
								lastSlash);
						sourceLocation.packageName(packageName).path(
								sourceDirectory);
					}

					if ("class".equals(aName)) {
						className = attrs.getValue(i);

						int lastSlash = fileName.lastIndexOf(File.separator);
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

					if (Parser.PRIORITY.equals(aName)) {

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

			messageHolder = message.toString();
			messageHolder = messageHolder.replace('\n', ' ');
			messageHolder = messageHolder.trim();

			artifact.message(messageHolder);

			if (!hasClassName) {
				int lastSlash = fileName.lastIndexOf(File.separator);
				String fileNameHolder = fileName.substring(lastSlash + 1,
						fileName.length());
				String className = fileNameHolder.substring(0, fileNameHolder
						.length() - 5);
				sourceLocation.className(className);
			}
			hasClassName = false;

			// Populate package name as default package if there is no
			// package associated with it
			if (!hasPackage) {
				String packageName = Parser.DEFAULT_PACKAGE;
				int lastSlash = fileName.lastIndexOf(File.separator);
				String sourceDirectory = fileName.substring(0, lastSlash);
				sourceLocation.packageName(packageName).path(sourceDirectory);
			}
			hasPackage = false;
			sourceLocation.build();
			artifact.build();
		}

	}

	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {

		// start and length are added and reduced by 1 because PMD messages
		// contain "\n" before and after each message
		if (inViolation) {
			// char s = ch[start];
			// char e = ch[start + length - 1];
			//
			// if ((s == '\n') && (e == '\n')) {
			// message.append(ch, start + 1, length - 2);
			// } else if ((s == '\n') && (e != '\n')) {
			message.append(ch, start, length);

			// } else if ((s != '\n') && (e == '\n')) {
			// message.append(ch, start, length - 2);
			// } else {
			// message.append(ch, start, length);
			// }
		}
	}
}