package com.surelogic.sierra.tool.analyzer;

import java.io.File;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.surelogic.common.SLProgressMonitor;
import com.surelogic.sierra.tool.message.MessageArtifactFileGenerator;

public class CheckStyleHandler extends DefaultHandler {

	private static final String CHECK_STYLE = "checkstyle";
	private static final String FILE = "file";
	private static final String NAME = "name";
	private static final String ERROR = "error";
	private static final String LINE = "line";
	private static final String MESSAGE = "message";
	private static final String SOURCE = "source";
	private static final String VERSION = "4.3";
	private static final String TREEWALKER = "com.puppycrawl.tools.checkstyle.TreeWalker";
	// private static final String COLUMN = "column";
	private boolean f_isValid = false;

	private ArtifactGenerator.SourceLocationBuilder primarySourceLocation;

	private ArtifactGenerator.ArtifactBuilder artifact;

	// private ArtifactGenerator.ErrorBuilder error;

	private final MessageArtifactFileGenerator generator;

	private String fileName;

	private SLProgressMonitor monitor = null;
	private String className;
	private String packageName = "NA";

	public CheckStyleHandler(MessageArtifactFileGenerator generator,
			SLProgressMonitor monitor) {
		super();
		this.generator = generator;
		this.monitor = monitor;
	}

	@Override
	public void startElement(String uri, String localName, String name,
			Attributes attributes) throws SAXException {

		if (name.equals(CHECK_STYLE)) {
			f_isValid = true;
		}

		if (name.equals(FILE)) {
			fileName = attributes.getValue(NAME);
			File file = new File(fileName);

			/*
			 * Currently the code handles only .class and .java files all other
			 * cases are stored with their complete name
			 */
			if (fileName.endsWith(".class")) {
				className = file.getName().substring(0,
						file.getName().length() - 6);
			} else if (fileName.endsWith(".java")) {
				className = file.getName().substring(0,
						file.getName().length() - 5);
				packageName = PackageFinder.getInstance().getPackage(file);
			} else {
				/*
				 * Not a java file
				 */
				className = file.getName();
			}

		}

		if (name.equals(ERROR)) {

			final String source = attributes.getValue(SOURCE);
			final String message = attributes.getValue(MESSAGE);
			final int line = Integer.valueOf(attributes.getValue(LINE));

			// Mark TreeWalker as error in scan document
			if (source.equals(TREEWALKER)) {
				// IGNORE FOR NOW
				// error = generator.error();
				// error.message(source + " : " + message);
			} else {

				artifact = generator.artifact();
				primarySourceLocation = artifact.primarySourceLocation();
				primarySourceLocation.className(className).packageName(
						packageName);

				// generator.addFile(fileName);

				if (monitor != null) {
					monitor.subTask("Calculating hash value (Checkstyle)"
							+ fileName);
				}
				long hash = HashGenerator.getInstance().getHash(fileName, line);

				primarySourceLocation.lineOfCode(line).hash(hash);
				artifact.message(message).findingType(CHECK_STYLE, VERSION,
						source);
			}
		}
	}

	@Override
	public void endElement(String uri, String localName, String name)
			throws SAXException {

		if (name.endsWith(ERROR)) {
			if (artifact != null) {
				primarySourceLocation.build();
				artifact.build();
			}
		}

	}

	public boolean isValid() {
		return f_isValid;
	}

}
