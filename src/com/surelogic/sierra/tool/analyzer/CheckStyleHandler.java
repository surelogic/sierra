package com.surelogic.sierra.tool.analyzer;

import java.io.File;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.surelogic.common.SLProgressMonitor;
import com.surelogic.sierra.tool.SierraConstants;
import com.surelogic.sierra.tool.message.MessageArtifactFileGenerator;

public class CheckStyleHandler extends DefaultHandler {

	private static final String CHECK_STYLE = "Checkstyle";
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

	private ArtifactGenerator.SourceLocationBuilder f_primarySourceLocation;

	private ArtifactGenerator.ArtifactBuilder f_artifact;

	// private ArtifactGenerator.ErrorBuilder error;

	private final MessageArtifactFileGenerator f_generator;

	private String f_fileName;

	private SLProgressMonitor f_monitor = null;
	private String f_className;
	private String f_packageName = "NA";
	private boolean f_ignore = false;

	public CheckStyleHandler(MessageArtifactFileGenerator generator,
			SLProgressMonitor monitor) {
		super();
		this.f_generator = generator;
		this.f_monitor = monitor;
	}

	@Override
	public void startElement(String uri, String localName, String name,
			Attributes attributes) throws SAXException {

		if (name.equals(CHECK_STYLE)) {
			f_isValid = true;
		}

		if (name.equals(FILE)) {
			f_fileName = attributes.getValue(NAME);
			File file = new File(f_fileName);

			/*
			 * Currently the code handles only .class and .java files all other
			 * cases are stored with their complete name
			 */
			if (f_fileName.endsWith(".class")) {
				f_className = file.getName().substring(0,
						file.getName().length() - 6);
			} else if (f_fileName.endsWith(".java")) {
				f_className = file.getName().substring(0,
						file.getName().length() - 5);
				String pakkage = PackageFinder.getInstance().getPackage(file);

				if (pakkage.equals(SierraConstants.DEFAULT_PACKAGE)) {
					f_packageName = SierraConstants.DEFAULT_PACKAGE_PARENTHESIS;
				} else {
					f_packageName = pakkage;
				}
			} else {
				/*
				 * Not a java file
				 */
				f_className = file.getName();
			}

		}

		if (name.equals(ERROR)) {

			final String source = attributes.getValue(SOURCE);
			final String message = attributes.getValue(MESSAGE);
			final int line = Integer.valueOf(attributes.getValue(LINE));

			// Mark TreeWalker as error in scan document
			if (source.equals(TREEWALKER)) {
				f_ignore = true;
				// IGNORE FOR NOW
				// error = generator.error();
				// error.message(source + " : " + message);
			} else {

				f_artifact = f_generator.artifact();
				f_primarySourceLocation = f_artifact.primarySourceLocation();
				f_primarySourceLocation.className(f_className).packageName(
						f_packageName);

				// generator.addFile(fileName);

				if (f_monitor != null) {
					f_monitor
							.subTask("Calculating hash value (Checkstyle) for "
									+ f_fileName);
				}
				long hash = HashGenerator.getInstance().getHash(f_fileName,
						line);

				f_primarySourceLocation.lineOfCode(line).hash(hash);
				f_artifact.message(message).findingType(CHECK_STYLE, VERSION,
						source);
			}
		}
	}

	@Override
	public void endElement(String uri, String localName, String name)
			throws SAXException {

		if (name.endsWith(ERROR)) {
			if (!f_ignore) {
				f_primarySourceLocation.build();
				f_artifact.build();
			} else {
				f_ignore = false;
			}
		}

	}

	public boolean isValid() {
		return f_isValid;
	}

}
