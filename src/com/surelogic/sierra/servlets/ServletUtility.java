package com.surelogic.sierra.servlets;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.surelogic.common.i18n.I18N;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.gwt.client.data.ReportSettings;

public final class ServletUtility {

	/**
	 * Types all the servlet parameters and only takes the first value for each
	 * key. This method is picky about parameters that it launders through to
	 * its result, the key and the value must be non-null and non-empty.
	 * 
	 * @param request
	 *            the servlet parameters.
	 * @return the servlet parameters as a map.
	 * @throws IllegalArgumentException
	 *             if request is {@code null}.
	 */
	@SuppressWarnings("unchecked")
	public static Map<String, String> launderRequestParameters(
			HttpServletRequest request) {
		if (request == null) {
			throw new IllegalArgumentException(I18N.err(44, "request"));
		}

		final Map<String, String[]> parameterMap = request.getParameterMap();
		final Map<String, String> result = new HashMap<String, String>();
		for (final Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
			final String key = entry.getKey();
			if ((key != null) && !"".equals(key)) {
				final String[] values = entry.getValue();
				if (values.length >= 1) {
					final String value = values[0];
					if ((value != null) && !"".equals(value)) {
						result.put(key, value);
					}
				}
			}
		}
		return result;
	}

	/**
	 * Reads in the parameters from a servlet request and generates a report. In
	 * addition to adding parameters on the request to the report, this method
	 * also interprets {@code type} to be the type of a report, and {@code name}
	 * to be the name of the report.
	 * 
	 * @param request
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static ReportSettings launderRequestParametersAsReport(
			HttpServletRequest request) {
		if (request == null) {
			throw new IllegalArgumentException(I18N.err(44, "request"));
		}
		final ReportSettings report = new ReportSettings();
		for (final Object entry : request.getParameterMap().entrySet()) {
			final Entry<String, String[]> param = (Entry<String, String[]>) entry;
			final String key = param.getKey();
			final String[] value = param.getValue();
			if ("type".equals(key)) {
				report.setReportUuid(value[0]);
			} else if ("name".equals(key)) {
				report.setReportUuid(value[0]);
			} else {
				report.setSettingValue(key, Arrays.asList(value));
			}
		}
		return report;
	}

	/**
	 * Binary streams the passed file to the HTTP response.
	 * 
	 * @param file
	 *            the file to be streamed.
	 * @param response
	 *            the HTTP response object.
	 * @param mimeType
	 *            the mime type of the file, {@code null} is allowed.
	 * 
	 * @throws IllegalArgumentException
	 *             if either the file or response is {@code null}.
	 * @throws FileNotFoundException
	 *             if the file passed in does not exist or is not readable.
	 * @throws IOException
	 *             if there is any sort of I/O problem.
	 */
	public static void sendFileToHttpServletResponse(File file,
			HttpServletResponse response, String mimeType) throws IOException {
		if (file == null) {
			throw new IllegalArgumentException(I18N.err(44, "file"));
		}
		if (response == null) {
			throw new IllegalArgumentException(I18N.err(44, "response"));
		}
		if (!file.canRead()) {
			throw new FileNotFoundException(I18N
					.err(40, file.getAbsolutePath()));
		}
		final Logger log = SLLogger.getLogger();
		if (log.isLoggable(Level.FINE)) {
			log.log(Level.FINE, "Serving the file " + file.getAbsolutePath());
		}

		/*
		 * Set HTTP headers.
		 */
		if (mimeType != null) {
			response.setHeader("Content-Type", mimeType);
		}
		response.setHeader("Content-Length", String.valueOf(file.length()));
		final SimpleDateFormat sdf = new SimpleDateFormat(
				"EEE, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH);
		sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
		response.setHeader("Last-Modified", sdf.format(new Date(file
				.lastModified())));

		/*
		 * Stream the file.
		 */
		final BufferedOutputStream out = new BufferedOutputStream(response
				.getOutputStream());
		sendFileTo(file, out);
		out.close();
	}

	/**
	 * Writes the specified file to the prepared byte output stream. The byte
	 * output stream is <i>not</i> closed.
	 * 
	 * @param file
	 *            the file to write.
	 * @param out
	 *            the byte output stream to write the contents of the file to.
	 * @throws IOException
	 *             if there is any sort of I/O problem.
	 */
	public static void sendFileTo(final File file, final OutputStream out)
			throws IOException {
		final BufferedInputStream in = new BufferedInputStream(
				new FileInputStream(file));
		/*
		 * Stream the file.
		 */
		final byte[] buffer = new byte[1024];
		boolean eof = false;
		while (!eof) {
			final int length = in.read(buffer);
			if (length == -1) {
				eof = true;
			} else {
				out.write(buffer, 0, length);
			}
		}
		in.close();
	}

	/**
	 * Writes the specified character file to the prepared character output
	 * stream. The character output stream is <i>not</i> closed.
	 * 
	 * @param file
	 *            the file to write.
	 * @param out
	 *            the character output stream to write the contents of the file
	 *            to.
	 * @throws IOException
	 *             if there is any sort of I/O problem.
	 */
	public static void sendFileTo(final File file, final Writer out)
			throws IOException {
		final InputStreamReader in = new InputStreamReader(new FileInputStream(
				file));
		/*
		 * Stream the file.
		 */
		final char[] buffer = new char[1024];
		boolean eof = false;
		while (!eof) {
			final int length = in.read(buffer);
			if (length == -1) {
				eof = true;
			} else {
				out.write(buffer, 0, length);
			}
		}
		in.close();
	}

	private ServletUtility() {
		// no instances
	}

}
