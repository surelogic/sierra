package com.surelogic.sierra.tool.analyzer;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * This class was built to test the hash calculation using double parsing
 * method. It is no longer used
 * 
 * @author Tanmay.Sinha
 * 
 */
@Deprecated
public class HashHandler extends DefaultHandler {

	private Map<String, Map<Integer, Long>> hashHolder;

	private boolean inMethod;

	private boolean inField;

	private String fileNameFB;

	private String fileNamePMD;

	private boolean inClass;

	private String[] sourceDirectories;

	private String relativePath;

	// private String relativePath;

	public HashHandler(Map<String, Map<Integer, Long>> hashHolder,
			String[] sourceDirectories) {
		super();

		this.sourceDirectories = sourceDirectories;
		this.hashHolder = hashHolder;
	}

	public Map<String, Map<Integer, Long>> getHashHolder() {
		return hashHolder;
	}

	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {

		String eName = localName;

		if ("".equals(eName)) {
			eName = qName;
		}

		if ("Method".equals(eName)) {
			inMethod = true;
		}
		if ("Class".equals(eName)) {
			inClass = true;
		}
		if ("Field".equals(eName)) {
			inField = true;

		}

		if ("SourceLine".equals(eName)) {

			if (!inMethod && !inField && !inClass) {

				String start = "0";
				// String end = "0";

				if (attributes != null) {

					for (int i = 0; i < attributes.getLength(); i++) {
						String aName = attributes.getLocalName(i);
						if ("".equals(aName)) {
							aName = attributes.getQName(i);
						}

						if ("start".equals(aName)) {
							start = attributes.getValue(i);
						}

						// if ("end".equals(aName)) {
						// end = attributes.getValue(i);
						// }

						if ("sourcepath".equals(aName)) {
							String sourcePath = attributes.getValue(i);
							relativePath = sourcePath;
							int lastSlash = sourcePath.lastIndexOf("/");
							fileNameFB = sourcePath.substring(lastSlash + 1);
						}

					}

					// ASSUMPTION: Start and end for SourceLine element
					// inside the BugInstance represent the line number of
					// the bug else it is not a bug that can assigned a line
					// number and assume the lineStart as the line number

					int s = Integer.parseInt(start);

					boolean fileFound = false;
					for (int i = 0; i < sourceDirectories.length; i++) {

						if (!fileFound) {
							String completePath = sourceDirectories[i]
									+ File.separator + fileNameFB;

							String relativePathHolder = relativePath.replace(
									"/", File.separator);

							if (completePath.contains(relativePathHolder)) {
								File holderFile = new File(completePath);

								if (holderFile.exists()) {
									Map<Integer, Long> lineHashMap = hashHolder
											.get(completePath);
									Long hashValue = 0L;
									if (lineHashMap == null) {
										lineHashMap = new HashMap<Integer, Long>();
										lineHashMap.put(s, hashValue);

									} else if (!lineHashMap.containsKey(s)) {
										lineHashMap.put(s, hashValue);
									}

									hashHolder.put(completePath, lineHashMap);
									fileFound = true;
								}
							}

						}
					}
				}
			}

		}

		if ("file".equals(eName)) {

			if (attributes != null) {
				for (int i = 0; i < attributes.getLength(); i++) {
					String aName = attributes.getLocalName(i);
					if ("".equals(aName)) {
						aName = attributes.getQName(i);
					}

					if ("name".equals(aName)) {
						fileNamePMD = attributes.getValue(i);
					}
				}
			}

		}

		if ("violation".equals(eName)) {

			if (attributes != null) {
				for (int i = 0; i < attributes.getLength(); i++) {
					String aName = attributes.getLocalName(i);
					if ("".equals(aName)) {
						aName = attributes.getQName(i);
					}

					if ("line".equals(aName)) {

						int lineNumber = Integer.parseInt(attributes
								.getValue(i));

						Map<Integer, Long> lineHashMap = hashHolder
								.get(fileNamePMD);
						Long hashValue = 0L;
						if (lineHashMap == null) {
							lineHashMap = new HashMap<Integer, Long>();
							lineHashMap.put(lineNumber, hashValue);

						} else if (!lineHashMap.containsKey(lineNumber)) {
							lineHashMap.put(lineNumber, hashValue);
						}

						hashHolder.put(fileNamePMD, lineHashMap);

					}

				}
			}
		}
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
		}

		if ("Field".equals(eName)) {
			inField = false;
		}

		if ("Class".equals(eName)) {
			inClass = false;
		}

	}

	// @Override
	// public void characters(char[] ch, int start, int length)
	// throws SAXException {
	// if (inLongMessage) {
	// message.append(ch, start, length);
	// }
	// }

}
