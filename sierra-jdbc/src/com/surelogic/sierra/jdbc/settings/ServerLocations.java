package com.surelogic.sierra.jdbc.settings;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.surelogic.common.i18n.I18N;
import com.surelogic.common.jdbc.DBQuery;
import com.surelogic.common.jdbc.HasResultHandler;
import com.surelogic.common.jdbc.NullDBQuery;
import com.surelogic.common.jdbc.NullResultHandler;
import com.surelogic.common.jdbc.Nulls;
import com.surelogic.common.jdbc.Query;
import com.surelogic.common.jdbc.Queryable;
import com.surelogic.common.jdbc.Result;
import com.surelogic.common.jdbc.ResultHandler;
import com.surelogic.common.jdbc.Row;
import com.surelogic.common.jdbc.StringRowHandler;
import com.surelogic.sierra.tool.message.ServerIdentity;
import com.surelogic.sierra.tool.message.ServerLocation;

/**
 * This class implements storage and retrieval of server locations from the
 * database.
 * 
 * @author nathan
 * 
 */
public final class ServerLocations {
	/**
	 * Save a single connected server to the database. This call will fail if a
	 * server w/ the same uuid is already saved to the database.
	 * 
	 * @param cs
	 * @param savePassword
	 *            whether or not we are storing passwords in the database
	 * @throws InvalidServerException
	 *             if a connected server with the same uuid is already stored in
	 *             the database
	 * @return
	 */
	public static NullDBQuery saveServerLocation(final ConnectedServer s,
			final boolean savePassword) {
		return new NullDBQuery() {

			@Override
			public void doPerform(final Query q) {
				final ServerLocation l = s.getLocation();
				if (!q.prepared("ServerLocations.checkIdentity",
						new HasResultHandler()).call(s.getUuid())) {
					throw new IllegalArgumentException(I18N.err(160));
				}
				if (q.prepared("ServerLocations.checkLocation",
						new HasResultHandler()).call(s.getUuid())) {
					throw new InvalidServerException(I18N.err(158, l
							.createHomeURL().toString()));
				}
				q.prepared("ServerLocations.insertLocation").call(
						s.getUuid(),
						l.getProtocol(),
						Nulls.coerce(l.getHost()),
						l.getPort(),
						Nulls.coerce(l.getContextPath()),
						Nulls.coerce(l.getUser()),
						Nulls.coerce(savePassword && l.isSavePassword() ? l
								.getPass() : null), l.isAutoSync(),
						l.isSavePassword(), s.isTeamServer());
			}
		};
	}

	/**
	 * Store the provided list of server locations and their associated
	 * projects.
	 * 
	 * @param locations
	 *            a map of server locations, and the projects connected to them
	 * @param savePassword
	 *            whether or not we are storing passwords in the database
	 * @return
	 */
	public static NullDBQuery saveQuery(
			final Map<ConnectedServer, Collection<String>> locations,
			final boolean savePassword) {
		return new NullDBQuery() {
			@Override
			public void doPerform(final Query q) {
				q.statement("ServerLocations.deleteLocations").call();
				q.statement("ServerLocations.deleteProjects").call();
				final Queryable<Void> insertServerProject = q
						.prepared("ServerLocations.insertServerProject");
				final Queryable<Void> insertLocation = q
						.prepared("ServerLocations.insertLocation");
				for (final Entry<ConnectedServer, Collection<String>> locEntry : locations
						.entrySet()) {
					final ConnectedServer s = locEntry.getKey();
					final ServerLocation l = s.getLocation();
					if (s.getUuid() == null) {
						throw new IllegalArgumentException(I18N.err(44, "uuid"));
					}
					insertLocation.call(s.getUuid(), l.getProtocol(), Nulls
							.coerce(l.getHost()), l.getPort(), Nulls.coerce(l
							.getContextPath()), Nulls.coerce(l.getUser()),
							Nulls.coerce(savePassword && l.isSavePassword() ? l
									.getPass() : null), l.isAutoSync(), l
									.isSavePassword(), s.isTeamServer());
					for (final String project : locEntry.getValue()) {
						insertServerProject.call(s.getUuid(), project);
					}
				}
			}
		};
	}

	/**
	 * Update the server identities in this database to reflect the given data.
	 * If we have data about a server from a later revision, we will keep it.
	 * 
	 * @param ids
	 * @return
	 */
	public static NullDBQuery updateServerIdentities(
			final List<ServerIdentity> ids) {
		return new NullDBQuery() {
			@Override
			public void doPerform(final Query q) {
				final Queryable<Void> delete = q
						.prepared("ServerLocations.deleteIdentity");
				final Queryable<Void> insert = q
						.prepared("ServerLocations.insertIdentity");
				for (final ServerIdentity id : ids) {
					q.prepared("ServerLocations.selectIdentityRevision",
							new NullResultHandler() {
								@Override
								protected void doHandle(final Result result) {
									for (final Row r : result) {
										// We don't want to do anything if
										// we already have a higher revision
										if (r.nextLong() >= id.getRevision()) {
											return;
										}
									}
									delete.call(id.getServer());
									insert.call(id.getServer(), id.getName(),
											id.getRevision());
								}
							}).call(id.getServer());
				}
			}
		};
	}

	/**
	 * Return a list of server locations, and the projects that belong to them.
	 * Any password information not stored in the database should be provided.
	 * 
	 * @param a
	 *            map of passwords by keyed by {@code user@host}. May be {@code
	 *            null}
	 * @return
	 */
	public static DBQuery<Map<ConnectedServer, Collection<String>>> fetchQuery(
			final Map<String, String> passwords) {
		final Map<String, String> empty = Collections.emptyMap();
		final Map<String, String> passMap = passwords == null ? empty
				: passwords;
		return new DBQuery<Map<ConnectedServer, Collection<String>>>() {
			public Map<ConnectedServer, Collection<String>> perform(
					final Query q) {
				final Queryable<List<String>> projects = q.prepared(
						"ServerLocations.listServerProjects",
						new StringRowHandler());
				return q
						.statement(
								"ServerLocations.listLocations",
								new ResultHandler<Map<ConnectedServer, Collection<String>>>() {
									public Map<ConnectedServer, Collection<String>> handle(
											final Result result) {
										final Map<ConnectedServer, Collection<String>> map = new HashMap<ConnectedServer, Collection<String>>();
										// ID,LABEL,PROTOCOL,HOST,PORT,
										// CONTEXT_PATH,SERVER_USER
										for (final Row r : result) {
											final String uuid = r.nextString();
											final String name = r.nextString();
											final String protocol = r
													.nextString();
											final String host = r.nextString();
											final int port = r.nextInt();
											final String contextPath = r
													.nextString();
											final String user = r.nextString();
											final String dbPass = r
													.nextString();
											String password = passMap.get(user
													+ "@" + host);
											if (password == null) {
												password = dbPass;
											}
											final boolean autoSync = r
													.nextBoolean();
											final boolean savePass = r
													.nextBoolean();
											final boolean teamServer = r
													.nextBoolean();
											final ServerLocation loc = new ServerLocation(
													host, "https"
															.equals(protocol),
													port, contextPath, user,
													password, savePass,
													autoSync);
											final ConnectedServer s = new ConnectedServer(
													uuid, name, teamServer, loc);
											map.put(s, projects.call(uuid));
										}
										return map;
									}
								}).call();
			}
		};
	}
}
