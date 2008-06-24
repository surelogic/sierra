package com.surelogic.sierra.jdbc.settings;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.surelogic.common.jdbc.DBQuery;
import com.surelogic.common.jdbc.DBQueryNoResult;
import com.surelogic.common.jdbc.LongIdHandler;
import com.surelogic.common.jdbc.Query;
import com.surelogic.common.jdbc.Queryable;
import com.surelogic.common.jdbc.Result;
import com.surelogic.common.jdbc.ResultHandler;
import com.surelogic.common.jdbc.Row;
import com.surelogic.common.jdbc.StringRowHandler;
import com.surelogic.sierra.tool.message.SierraServerLocation;

public final class ServerLocations {
	/**
	 * Store the provided list of server locations. Passwords will not be saved.
	 * 
	 * @param locations
	 *            a map of server locations, and the projects connected to them
	 * @return
	 */
	public static DBQueryNoResult save(
			final Map<SierraServerLocation, Collection<String>> locations) {
		return new DBQueryNoResult() {
			@Override
			public void doPerform(Query q) {
				q.statement("ServerLocations.deleteLocations").call();
				q.statement("ServerLocations.deleteProjects").call();
				final Queryable<Void> insertServerProject = q
						.prepared("ServerLocations.insertServerProject");
				final Queryable<Long> insertLocation = q.prepared(
						"ServerLocations.insertLocation", new LongIdHandler());
				for (final Entry<SierraServerLocation, Collection<String>> locEntry : locations
						.entrySet()) {
					final SierraServerLocation l = locEntry.getKey();
					final long id = insertLocation.call(l.getLabel(), l
							.getProtocol(), l.getHost(), l.getPort(), l
							.getContextPath(), l.getUser());
					for (final String project : locEntry.getValue()) {
						insertServerProject.call(id, project);
					}
				}
			}
		};
	}

	/**
	 * Return a list of server locations, and the projects that belong to them.
	 * Any password information not stored in the database should be provided.
	 * 
	 * @param a
	 *            map of passwords by keyed by {@code user@host}. May be
	 *            {@code null}
	 * @return
	 */
	public static DBQuery<Map<SierraServerLocation, Collection<String>>> fetch(
			Map<String, String> passwords) {
		final Map<String, String> empty = Collections.emptyMap();
		final Map<String, String> passMap = passwords == null ? empty
				: passwords;
		return new DBQuery<Map<SierraServerLocation, Collection<String>>>() {
			public Map<SierraServerLocation, Collection<String>> perform(Query q) {
				final Queryable<List<String>> projects = q.prepared(
						"ServerLocations.listServerProjects",
						new StringRowHandler());
				return q
						.statement(
								"ServerLocations.listLocations",
								new ResultHandler<Map<SierraServerLocation, Collection<String>>>() {
									public Map<SierraServerLocation, Collection<String>> handle(
											Result result) {
										final Map<SierraServerLocation, Collection<String>> map = new HashMap<SierraServerLocation, Collection<String>>();
										// ID,LABEL,PROTOCOL,HOST,PORT,CONTEXT_PATH,SERVER_USER
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
											String password = passMap.get(user
													+ "@" + host);
											if (password == null) {
												password = r.nextString();
											}
											final SierraServerLocation loc = new SierraServerLocation(
													label, host, "https"
															.equals(protocol),
													port, contextPath, user,
													password);
											map.put(loc, projects.call(id));
										}
										return map;
									}
								}).call();
			}
		};
	}
}
