package com.surelogic.sierra.jdbc.settings;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.surelogic.common.jdbc.DBQuery;
import com.surelogic.common.jdbc.LongIdHandler;
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
import com.surelogic.sierra.tool.message.ServerInfoReply;
import com.surelogic.sierra.tool.message.Services;
import com.surelogic.sierra.tool.message.SierraServerLocation;

/**
 * This class implements storage and retrieval of server locations from the
 * database.
 * 
 * @author nathan
 * 
 */
public final class ServerLocations {
	/**
	 * Store the provided list of server locations and their associated
	 * projects.
	 * 
	 * @param locations
	 *            a map of server locations, and the projects connected to them
	 * @return
	 */
	public static NullDBQuery saveQuery(
			final Map<SierraServerLocation, Collection<String>> locations) {
		return new NullDBQuery() {
			@Override
			public void doPerform(final Query q) {
				q.statement("ServerLocations.deleteLocations").call();
				q.statement("ServerLocations.deleteProjects").call();
				final Queryable<Void> insertServerProject = q
						.prepared("ServerLocations.insertServerProject");
				final Queryable<Long> insertLocation = q.prepared(
						"ServerLocations.insertLocation", new LongIdHandler());
				for (final Entry<SierraServerLocation, Collection<String>> locEntry : locations
						.entrySet()) {
					final SierraServerLocation l = locEntry.getKey();
					if (l.getLabel() == null) {
						throw new IllegalArgumentException();
					}
					final long id = insertLocation.call(l.getLabel(), l
							.getProtocol(), Nulls.coerce(l.getHost()), l
							.getPort(), Nulls.coerce(l.getContextPath()), Nulls
							.coerce(l.getUser()), Nulls.coerce(l.getPass()), l
							.isAutoSync(), Nulls.coerce(l.getUuid()), l
							.isTeamServer());
					for (final String project : locEntry.getValue()) {
						insertServerProject.call(id, project);
					}
				}
			}
		};
	}

	/**
	 * Update the server location informations for a single server based on it's
	 * reply.
	 * 
	 * @param loc
	 * @param reply
	 * @return
	 */
	public static DBQuery<Void> updateServerLocationInfo(
			final SierraServerLocation loc, final ServerInfoReply reply) {
		return updateServerLocationInfo(Collections.singletonMap(loc, reply));
	}

	/**
	 * Update the information for the given set of locations based on their
	 * replies.
	 * 
	 * @param locations
	 * @return
	 */
	public static DBQuery<Void> updateServerLocationInfo(
			final Map<SierraServerLocation, ServerInfoReply> locations) {
		return new DBQuery<Void>() {
			public Void perform(final Query q) {
				final Queryable<Void> updateUuid = q
						.prepared("ServerLocations.updateUuid");
				for (final Entry<SierraServerLocation, ServerInfoReply> entry : locations
						.entrySet()) {
					final ServerInfoReply reply = entry.getValue();
					updateUuid.call(reply.getUid(), reply.getServices()
							.contains(Services.TEAMSERVER), entry.getKey()
							.getLabel());
					final Queryable<Void> delete = q
							.prepared("ServerLocations.deleteIdentity");
					final Queryable<Void> insert = q
							.prepared("ServerLocations.insertIdentity");
					for (final ServerIdentity id : entry.getValue()
							.getServers()) {
						q.prepared("ServerLocations.selectIdentityRevision",
								new NullResultHandler() {
									@Override
									protected void doHandle(final Result result) {
										for (final Row r : result) {
											// We don't want to do anything if
											// we already have a higher revision
											if (r.nextLong() <= id
													.getRevision()) {
												return;
											}
										}
										delete.call(id.getServer());
										insert.call(id.getServer(), id
												.getName(), id.getRevision());
									}
								}).call(id.getServer());
					}
				}
				return null;
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
	public static DBQuery<Map<SierraServerLocation, Collection<String>>> fetchQuery(
			final Map<String, String> passwords) {
		final Map<String, String> empty = Collections.emptyMap();
		final Map<String, String> passMap = passwords == null ? empty
				: passwords;
		return new DBQuery<Map<SierraServerLocation, Collection<String>>>() {
			public Map<SierraServerLocation, Collection<String>> perform(
					final Query q) {
				final Queryable<List<String>> projects = q.prepared(
						"ServerLocations.listServerProjects",
						new StringRowHandler());
				return q
						.statement(
								"ServerLocations.listLocations",
								new ResultHandler<Map<SierraServerLocation, Collection<String>>>() {
									public Map<SierraServerLocation, Collection<String>> handle(
											final Result result) {
										final Map<SierraServerLocation, Collection<String>> map = new HashMap<SierraServerLocation, Collection<String>>();
										// ID,LABEL,PROTOCOL,HOST,PORT,
										// CONTEXT_PATH,SERVER_USER
										for (final Row r : result) {
											final long id = r.nextLong();
											final String label = r.nextString();
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
											final String uuid = r.nextString();
											final boolean teamServer = r
													.nextBoolean();
											final SierraServerLocation loc = new SierraServerLocation(
													label, host, "https"
															.equals(protocol),
													port, contextPath, user,
													password, autoSync, uuid,
													teamServer);
											map.put(loc, projects.call(id));
										}
										return map;
									}
								}).call();
			}
		};
	}

}
