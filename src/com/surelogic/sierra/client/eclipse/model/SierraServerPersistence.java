package com.surelogic.sierra.client.eclipse.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.surelogic.adhoc.Activator;
import com.surelogic.common.i18n.I18N;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.common.xml.Entities;

public final class SierraServerPersistence {

	private static final String TEAM_SERVERS = "team-servers";
	private static final String CONNECTED_PROJECT = "connected-project";
	private static final String HOST = "host";
	private static final String LABEL = "label";
	private static final String NAME = "name";
	private static final String PORT = "port";
	private static final String CONTEXT_PATH = "context-path";
	private static final String PROTOCOL = "protocol";
	private static final String SAVE_PASSWORD = "save-password";
	private static final String SERVER = "server";
	private static final String USER = "user";
	private static final String VERSION = "version";
	private static final String IS_FOCUS = "is-focus";

	// fields needed for caching the password
	private static final String AUTH_SCHEME = "";
	private static final URL FAKE_URL;

	static {
		final String urlString = "http://com.surelogic.sierra";
		URL temp = null;
		try {
			temp = new URL(urlString);
		} catch (MalformedURLException e) {
			SLLogger.getLogger().log(Level.SEVERE, I18N.err(41, urlString), e);
		}
		FAKE_URL = temp;
	}

	@SuppressWarnings("unchecked")
	public static void save(final SierraServerManager manager, final File file) {
		try {
			Map<String, String> map = Platform.getAuthorizationInfo(FAKE_URL,
					"", AUTH_SCHEME);
			PrintWriter pw = new PrintWriter(new FileWriter(file));
			outputXMLHeader(pw);
			for (SierraServer server : manager.getServers()) {
				outputServer(pw, server,
						manager.getProjectsConnectedTo(server), true);

				/* Store the password in the keyring */
				if (map == null) {
					map = new java.util.HashMap<String, String>();
				}
				if (server.getUser() != null && server.savePassword()) {
					// "@" symbol to ensure that one user can have multiple
					// passwords on different servers
					map.put(server.getUser() + "@" + server.getHost(), server
							.getPassword());
				}
			}
			outputXMLFooter(pw);
			if (map != null) {
				Platform.addAuthorizationInfo(FAKE_URL, "", AUTH_SCHEME, map);
			}

			pw.close();
		} catch (IOException e) {
			SLLogger.getLogger().log(Level.SEVERE,
					I18N.err(38, "Team Servers", file), e);
		} catch (CoreException e) {
			SLLogger.getLogger().log(Level.SEVERE, I18N.err(42), e);
		}
	}

	public static void export(final SierraServerManager manager,
			List<SierraServer> serversToExport, final File file) {
		try {
			PrintWriter pw = new PrintWriter(new FileWriter(file));
			outputXMLHeader(pw);
			for (SierraServer server : serversToExport) {
				outputServer(pw, server,
						manager.getProjectsConnectedTo(server), false);
			}
			outputXMLFooter(pw);
			pw.close();
		} catch (IOException e) {
			SLLogger.getLogger().log(Level.SEVERE,
					I18N.err(38, "Team Servers", file), e);
		}
	}

	private static void outputXMLHeader(PrintWriter pw) {
		pw.println("<?xml version='1.0' encoding='" + Activator.XML_ENCODING
				+ "' standalone='yes'?>");
		StringBuilder b = new StringBuilder();
		b.append("<").append(TEAM_SERVERS);
		Entities.addAttribute(VERSION, "1.0", b);
		b.append(">"); // don't end this element
		pw.println(b.toString());
	}

	private static void outputServer(PrintWriter pw, SierraServer server,
			List<String> connectedProjectNames, boolean save) {
		final StringBuilder b = new StringBuilder();
		b.append("  <").append(SERVER);
		Entities.addAttribute(LABEL, server.getLabel(), b);
		if (save && server.isFocus()) {
			Entities.addAttribute(IS_FOCUS, "yes", b);
		}
		Entities.addAttribute(HOST, server.getHost(), b);
		Entities.addAttribute(PROTOCOL, server.getProtocol(), b);
		Entities.addAttribute(PORT, server.getPort(), b);
		Entities.addAttribute(CONTEXT_PATH, server.getContextPath(), b);
		Entities.addAttribute(USER, server.getUser(), b);
		if (save) {
			Entities.addAttribute(SAVE_PASSWORD, Boolean.toString(server
					.savePassword()), b);
		}
		b.append(">");
		pw.println(b.toString());

		for (String projectName : connectedProjectNames) {
		  b.setLength(0);
			b.append("    <").append(CONNECTED_PROJECT);
			Entities.addAttribute(NAME, projectName, b);
			b.append("/>");
			pw.println(b.toString());
		}

		b.setLength(0);
		b.append("  </").append(SERVER).append(">");
		pw.println(b.toString());
	}

	private static void outputXMLFooter(PrintWriter pw) {
		pw.println("</" + TEAM_SERVERS + ">");
	}

	public static void load(final SierraServerManager manager, final File file)
			throws Exception {
		InputStream stream;
		try {
			stream = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			/*
			 * This means we are running the tool for the first time.
			 */
			return;
		}
		SAXParserFactory factory = SAXParserFactory.newInstance();
		SaveFileReader handler = new SaveFileReader(manager);
		try {
			// Parse the input
			SAXParser saxParser = factory.newSAXParser();
			saxParser.parse(stream, handler);
		} catch (SAXException e) {
			SLLogger.getLogger().log(Level.SEVERE, I18N.err(39, file), e);
		} finally {
			stream.close();
			/*
			 * Clear all the user and password information for sierra servers
			 */
			Platform.flushAuthorizationInfo(FAKE_URL, "", AUTH_SCHEME);
		}
	}

	/**
	 * SAX reader for the server save file.
	 */
	static class SaveFileReader extends DefaultHandler {

		private final SierraServerManager f_manager;
		private final Map<String, String> f_map;

		@SuppressWarnings("unchecked")
		SaveFileReader(SierraServerManager manager) {
			f_manager = manager;
			f_map = Platform.getAuthorizationInfo(FAKE_URL, "", AUTH_SCHEME);
		}

		private SierraServer f_server = null;

		@Override
		public void startElement(String uri, String localName, String name,
				Attributes attributes) throws SAXException {
			if (name.equals(SERVER)) {
				final String label = attributes.getValue(LABEL);
				final boolean isFocus = attributes.getValue(IS_FOCUS) != null;
				final String host = attributes.getValue(HOST);
				final String protocol = attributes.getValue(PROTOCOL);
				final String portString = attributes.getValue(PORT);
				String contextPath = attributes.getValue(CONTEXT_PATH);
				if (contextPath == null)
					contextPath = "/";
				final int port = Integer.parseInt(portString);
				final String user = attributes.getValue(USER);
				final String savePasswordString = attributes
						.getValue(SAVE_PASSWORD);
				final boolean savePassword = Boolean
						.parseBoolean(savePasswordString);
				f_server = f_manager.getOrCreate(label);
				f_server.setHost(host);
				f_server.setSecure("https".equals(protocol));
				f_server.setPort(port);
				f_server.setContextPath(contextPath);
				f_server.setUser(user);
				f_server.setSavePassword(savePassword);

				/* Retrieve password from keyring */
				if (f_map != null && savePassword) {
					String password = f_map.get(f_server.getUser() + "@"
							+ f_server.getHost());
					if (password != null) {
						// "@" symbol to ensure that one user can have multiple
						// passwords on different servers
						f_server.setPassword(password);
					} else {
						f_server.setSavePassword(false);
					}
				}

				if (isFocus) {
					f_server.setFocus();
				}

			} else if (name.equals(CONNECTED_PROJECT)) {
				final String projectName = attributes.getValue(NAME);
				if (f_server == null) {
					SLLogger.getLogger().log(Level.SEVERE,
							I18N.err(43, projectName),
							new Exception("XML Format Error"));
				} else {
					f_manager.connect(projectName, f_server);
				}
			}
		}

		@Override
		public void endElement(String uri, String localName, String name)
				throws SAXException {
			if (name.equals(SERVER)) {
				f_server = null;
			}
		}
	}
}
