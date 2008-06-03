package com.surelogic.sierra.client.eclipse.model;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.surelogic.adhoc.eclipse.Activator;
import com.surelogic.common.i18n.I18N;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.common.xml.Entities;
import com.surelogic.sierra.client.eclipse.Data;
import com.surelogic.sierra.jdbc.server.TransactionException;
import com.surelogic.sierra.jdbc.settings.ServerLocations;
import com.surelogic.sierra.tool.message.Services;
import com.surelogic.sierra.tool.message.SierraServerLocation;

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
	private static final String SERVER_TYPE = "server-type";
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
		} catch (final MalformedURLException e) {
			SLLogger.getLogger().log(Level.SEVERE, I18N.err(41, urlString), e);
		}
		FAKE_URL = temp;
	}

	@SuppressWarnings("unchecked")
	public static void save(final SierraServerManager manager) {
		try {
			Map<String, String> map = Platform.getAuthorizationInfo(FAKE_URL,
					"", AUTH_SCHEME);
			final Map<SierraServerLocation, Collection<String>> locMap = new HashMap<SierraServerLocation, Collection<String>>();
			for (final SierraServer server : manager.getServers()) {
				locMap.put(server.getServer(), manager
						.getProjectsConnectedTo(server));
				/* Store the password in the keyring */
				if (map == null) {
					map = new java.util.HashMap<String, String>();
				}
				if ((server.getUser() != null) && server.savePassword()) {
					// "@" symbol to ensure that one user can have multiple
					// passwords on different servers
					map.put(server.getUser() + "@" + server.getHost(), server
							.getPassword());
				}
			}
			Data.withTransaction(ServerLocations.save(locMap));
			if (map != null) {
				Platform.addAuthorizationInfo(FAKE_URL, "", AUTH_SCHEME, map);
			}
		} catch (final TransactionException e) {
			SLLogger.getLogger().log(Level.SEVERE,
					I18N.err(38, "Team Servers", "database"), e);
		} catch (final CoreException e) {
			SLLogger.getLogger().log(Level.SEVERE, I18N.err(42), e);
		}
	}

	public static void export(final SierraServerManager manager,
			List<SierraServer> serversToExport, final File file) {
		try {
			final PrintWriter pw = new PrintWriter(new FileWriter(file));
			outputXMLHeader(pw);
			for (final SierraServer server : serversToExport) {
				outputServer(pw, server,
						manager.getProjectsConnectedTo(server), false);
			}
			outputXMLFooter(pw);
			pw.close();
		} catch (final IOException e) {
			SLLogger.getLogger().log(Level.SEVERE,
					I18N.err(38, "Team Servers", file), e);
		}
	}

	private static void outputXMLHeader(PrintWriter pw) {
		pw.println("<?xml version='1.0' encoding='" + Activator.XML_ENCODING
				+ "' standalone='yes'?>");
		final StringBuilder b = new StringBuilder();
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
		if (server.gotServerInfo()) {
			final StringBuilder sb = new StringBuilder();
			if (server.isTeamServer()) {
				sb.append(Services.TEAMSERVER);
			}
			if (server.isBugLink()) {
				if (sb.length() > 0) {
					sb.append(',');
				}
				sb.append(Services.BUGLINK);
			}
			Entities.addAttribute(SERVER_TYPE, sb.toString(), b);
		}
		if (save) {
			Entities.addAttribute(SAVE_PASSWORD, Boolean.toString(server
					.savePassword()), b);
		}
		b.append(">");
		pw.println(b.toString());

		for (final String projectName : connectedProjectNames) {
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

	@SuppressWarnings("unchecked")
	public static void load(final SierraServerManager manager) throws Exception {
		final Map<String, String> passwords = Platform.getAuthorizationInfo(
				FAKE_URL, "", AUTH_SCHEME);
		try {
			for (final Entry<SierraServerLocation, Collection<String>> locEntry : Data
					.withReadOnly(ServerLocations.fetch(passwords)).entrySet()) {
				final SierraServerLocation loc = locEntry.getKey();
				final SierraServer s = manager.getOrCreate(loc.getLabel());
				s.setContextPath(loc.getContextPath());
				s.setHost(loc.getHost());
				s.setPassword(loc.getPass());
				s.setPort(loc.getPort());
				s.setSecure(loc.isSecure());
				s.setUser(loc.getUser());
				for (final String project : locEntry.getValue()) {
					manager.connect(project, s);
				}
			}
		} finally {
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
				if (contextPath == null) {
					contextPath = "/";
				}
				final int port = Integer.parseInt(portString);
				final String user = attributes.getValue(USER);
				final String serverType = attributes.getValue(SERVER_TYPE);
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
				if (serverType != null) {
					f_server.setServerType(serverType
							.contains(Services.TEAMSERVER.toString()),
							serverType.contains(Services.BUGLINK.toString()));
				}

				/* Retrieve password from keyring */
				if ((f_map != null) && savePassword) {
					final String password = f_map.get(f_server.getUser() + "@"
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
