package com.surelogic.sierra.jdbc.tool;

import java.util.HashSet;
import java.util.Set;

import com.surelogic.common.jdbc.DBQuery;
import com.surelogic.common.jdbc.NullDBQuery;
import com.surelogic.common.jdbc.Query;
import com.surelogic.sierra.tool.message.BugLinkService;
import com.surelogic.sierra.tool.message.BugLinkServiceClient;
import com.surelogic.sierra.tool.message.Extension;
import com.surelogic.sierra.tool.message.ListExtensionRequest;
import com.surelogic.sierra.tool.message.RegisterExtensionRequest;
import com.surelogic.sierra.tool.message.ServerLocation;

/**
 * Miscellaneous queries for use with the tool api.
 * 
 * @author nathan
 * 
 */
public class ToolQueries {
	private ToolQueries() {
		// Do nothing
	}

	/**
	 * Ensure that all of the server extensions are in the local database, and
	 * optionally register any local extensions with the server.
	 * 
	 * @param loc
	 * @param registerLocals
	 *            whether or not to register local extensions on the server
	 * @return
	 */
	public static DBQuery<Void> synchronizeExtensions(final ServerLocation loc,
			final boolean registerLocals) {
		return new NullDBQuery() {
			@Override
			public void doPerform(final Query q) {
				final BugLinkService s = BugLinkServiceClient.create(loc);
				final FindingTypes ft = new FindingTypes(q);
				final Set<Extension> serverExts = new HashSet<Extension>(s
						.listExtensions(new ListExtensionRequest())
						.getExtensions());
				final Set<Extension> localExts = new HashSet<Extension>();
				for (final ExtensionDO eDO : ft.getExtensions()) {
					localExts.add(FindingTypes.convert(eDO));
				}
				for (final Extension ext : serverExts) {
					if (!localExts.contains(ext)) {
						ft.registerExtension(FindingTypes.convertDO(ext));
					}
				}
				if (registerLocals) {
					for (final Extension ext : localExts) {
						if (!serverExts.contains(ext)) {
							final RegisterExtensionRequest req = new RegisterExtensionRequest();
							req.setExtension(ext);
							s.registerExtension(req);
						}
					}
				}
			}
		};
	}
}
