package com.surelogic.sierra.client.eclipse.views;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.logging.Level;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.surelogic.common.i18n.I18N;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.common.xml.Entities;
import com.surelogic.sierra.client.eclipse.Activator;

public final class FindingDetailsPersistence {

	private static final String FINDING_DETAILS_VIEW = "finding-details-view";
	private static final String SHOWING = "showing-finding-id";
	private static final String NONE = "none";

	static void save(long findingId) {
		save(Long.toString(findingId));
	}

	static void saveNull() {
		save(NONE);
	}

	static private void save(String findingId) {
		final File file = Activator.getDefault()
				.getFindingDetailsViewSaveFile();
		if (SLLogger.getLogger().isLoggable(Level.FINE)) {
			SLLogger.getLogger().fine(
					"Saving that the Findings Details View was showing the finding "
							+ findingId + " to " + file);
		}
		try {
			PrintWriter pw = new PrintWriter(new FileWriter(file));
			outputXML(pw, findingId);
			pw.close();
		} catch (IOException e) {
			final String msg = I18N.err(38, "Finding Details View state", file);
			SLLogger.getLogger().log(Level.SEVERE, msg, e);
		}
	}

	private static void outputXML(PrintWriter pw, String findingId) {
		pw.println("<?xml version='1.0' encoding='" + Activator.XML_ENCODING
				+ "' standalone='yes'?>");
		StringBuilder b = new StringBuilder();
		b.append("<").append(FINDING_DETAILS_VIEW);
		Entities.addAttribute(SHOWING, findingId, b);
		b.append("/>");
		pw.println(b.toString());
	}

	static void load(final FindingDetailsMediator view) {
		final File file = Activator.getDefault()
				.getFindingDetailsViewSaveFile();
		InputStream stream;
		try {
			try {
				stream = new FileInputStream(file);
			} catch (FileNotFoundException e) {
				/*
				 * This means we are running the tool for the first time.
				 */
				return;
			}
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SaveFileReader handler = new SaveFileReader();
			try {
				// Parse the input
				SAXParser saxParser = factory.newSAXParser();
				saxParser.parse(stream, handler);
			} catch (Exception e) {
				SLLogger.getLogger().log(Level.SEVERE, I18N.err(39, file), e);
			} finally {
				stream.close();
			}
			if (handler.isFindingIdFound()) {
				final long findingId = handler.getFindingId();
				if (SLLogger.getLogger().isLoggable(Level.FINE)) {
					SLLogger.getLogger().fine(
							"Findings Details View was showing the finding "
									+ findingId + " according to " + file);
				}
				final Job job = new Job("Restore Finding Details View") {
					@Override
					protected IStatus run(IProgressMonitor monitor) {
						view.asyncQueryAndShow(findingId);
						return Status.OK_STATUS;
					}
				};
				job.schedule();
			}
		} catch (IOException e) {
			SLLogger.getLogger().log(Level.SEVERE, I18N.err(40, file), e);
		}
	}

	/**
	 * SAX reader for the server save file.
	 */
	static class SaveFileReader extends DefaultHandler {

		private boolean f_found = false;

		public boolean isFindingIdFound() {
			return f_found;
		}

		private long f_findingId;

		public long getFindingId() {
			return f_findingId;
		}

		@Override
		public void startElement(String uri, String localName, String name,
				Attributes attributes) throws SAXException {
			if (name.equals(FINDING_DETAILS_VIEW)) {
				final String showing = attributes.getValue(SHOWING);
				if (!NONE.equals(showing)) {
					f_findingId = Long.parseLong(showing);
					f_found = true;
				}
			}
		}
	}

	private FindingDetailsPersistence() {
		// no instances
	}
}
