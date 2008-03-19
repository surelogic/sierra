package com.surelogic.sierra.servlets;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.surelogic.common.i18n.I18N;

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
		if (request == null)
			throw new IllegalArgumentException(I18N.err(44, "request"));

		final Map<String, String[]> parameterMap = request.getParameterMap();
		final Map<String, String> result = new HashMap<String, String>();
		for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
			final String key = entry.getKey();
			if (key != null && !"".equals(key)) {
				final String[] values = entry.getValue();
				if (values.length >= 1) {
					final String value = values[0];
					if (value != null && !"".equals(value)) {
						result.put(key, value);
					}
				}
			}
		}
		return result;
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
	public static void sendCacheFile(File file, HttpServletResponse response,
			String mimeType) throws IOException {
		if (file == null)
			throw new IllegalArgumentException(I18N.err(44, "file"));
		if (response == null)
			throw new IllegalArgumentException(I18N.err(44, "response"));
		if (!file.canRead()) {
			throw new FileNotFoundException(I18N
					.err(40, file.getAbsolutePath()));
		}

		BufferedInputStream in = new BufferedInputStream(new FileInputStream(
				file));

		/*
		 * Set HTTP headers.
		 */
		if (mimeType != null) {
			response.setHeader("Content-Type", mimeType);
		}
		response.setHeader("Content-Length", String.valueOf(file.length()));
		SimpleDateFormat sdf = new SimpleDateFormat(
				"EEE, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH);
		sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
		response.setHeader("Last-Modified", sdf.format(new Date(file
				.lastModified())));

		/*
		 * Stream the file.
		 */
		BufferedOutputStream out = new BufferedOutputStream(response
				.getOutputStream());
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
		out.close();
		in.close();
	}

	private ServletUtility() {
		// no instances
	}
}
