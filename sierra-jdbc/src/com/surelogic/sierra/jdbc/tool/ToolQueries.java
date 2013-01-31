package com.surelogic.sierra.jdbc.tool;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.surelogic.common.jdbc.DBQuery;
import com.surelogic.common.jdbc.NullDBQuery;
import com.surelogic.common.jdbc.Query;
import com.surelogic.sierra.tool.message.BugLinkService;
import com.surelogic.sierra.tool.message.BugLinkServiceClient;
import com.surelogic.sierra.tool.message.EnsureExtensionRequest;
import com.surelogic.sierra.tool.message.EnsureExtensionResponse;
import com.surelogic.sierra.tool.message.Extension;
import com.surelogic.sierra.tool.message.ExtensionName;
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
	 * Ensures that the list of extensions are available in the server database,
	 * registering them if necessary.
	 * 
	 * @param loc
	 * @param extensions
	 * @return
	 */
	public static DBQuery<List<Extension>> ensureExtensions(
			final ServerLocation loc, final List<ExtensionName> extensions) {
		final BugLinkService service = BugLinkServiceClient.create(loc);
		final EnsureExtensionRequest eerReq = new EnsureExtensionRequest();
		eerReq.getExtensions().addAll(extensions);
		final EnsureExtensionResponse response = service
				.ensureExtensions(eerReq);
		return new DBQuery<List<Extension>>() {
			@Override
      public List<Extension> perform(final Query q) {
				final List<Extension> extensions = new ArrayList<Extension>();
				final FindingTypes t = new FindingTypes(q);
				for (final ExtensionName unknown : response
						.getUnknownExtensions()) {
					final RegisterExtensionRequest req = new RegisterExtensionRequest();
					req.setExtension(FindingTypes.convert(t.getExtension(
							unknown.getName(), unknown.getVersion())));
					service.registerExtension(req);
				}
				return extensions;
			}
		};
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
