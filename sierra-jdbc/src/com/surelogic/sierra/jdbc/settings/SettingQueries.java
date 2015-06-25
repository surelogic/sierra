package com.surelogic.sierra.jdbc.settings;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;

import com.surelogic.common.jdbc.DBQuery;
import com.surelogic.common.jdbc.NullDBQuery;
import com.surelogic.common.jdbc.NullRowHandler;
import com.surelogic.common.jdbc.Query;
import com.surelogic.common.jdbc.Queryable;
import com.surelogic.common.jdbc.Row;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.jdbc.tool.FindingTypeDO;
import com.surelogic.sierra.jdbc.tool.FindingTypes;
import com.surelogic.sierra.tool.message.BugLinkService;
import com.surelogic.sierra.tool.message.BugLinkServiceClient;
import com.surelogic.sierra.tool.message.CreateScanFilterRequest;
import com.surelogic.sierra.tool.message.Extension;
import com.surelogic.sierra.tool.message.ExtensionName;
import com.surelogic.sierra.tool.message.FilterSet;
import com.surelogic.sierra.tool.message.GetExtensionsRequest;
import com.surelogic.sierra.tool.message.GetExtensionsResponse;
import com.surelogic.sierra.tool.message.Importance;
import com.surelogic.sierra.tool.message.ListCategoryRequest;
import com.surelogic.sierra.tool.message.ListCategoryResponse;
import com.surelogic.sierra.tool.message.ListScanFilterRequest;
import com.surelogic.sierra.tool.message.ListScanFilterResponse;
import com.surelogic.sierra.tool.message.ScanFilter;
import com.surelogic.sierra.tool.message.ServerInfoReply;
import com.surelogic.sierra.tool.message.ServerInfoRequest;
import com.surelogic.sierra.tool.message.ServerInfoServiceClient;
import com.surelogic.sierra.tool.message.ServerLocation;
import com.surelogic.sierra.tool.message.Services;
import com.surelogic.sierra.tool.message.SierraServiceClientException;

/**
 * This class represents implementations of the client-side behavior of the bug
 * link service.
 * 
 * @author nathan
 * 
 */
public class SettingQueries {

  public static final String LOCAL_UUID = "de3034ec-65d5-4d4a-b059-1adf8fc7b12d";
  public static final String LOCAL_NAME = "Default";

  /**
   * Return a view of the provided scan filter
   * 
   * @param scanFilter
   * @return
   */
  public static DBQuery<ScanFilterView> scanFilterFor(final ScanFilterDO scanFilter) {
    return new DBQuery<ScanFilterView>() {
      @Override
      public ScanFilterView perform(final Query q) {
        return scanFilterView(q, scanFilter);
      }
    };
  }

  public static final DBQuery<ListCategoryRequest> categoryRequest() {
    return new DBQuery<ListCategoryRequest>() {
      @Override
      public ListCategoryRequest perform(final Query q) {
        final ListCategoryRequest r = new ListCategoryRequest();
        r.getServerRevisions().addAll(q.statement("FilterSets.latestServerRevisions", new ServerRevisionRowHandler()).call());
        return r;
      }
    };
  }

  public static final DBQuery<List<ExtensionName>> localExtensions() {
    return new DBQuery<List<ExtensionName>>() {
      @Override
      public List<ExtensionName> perform(final Query q) {
        return new FindingTypes(q).getExtensionNames();
      }
    };
  }

  /**
   * Queries the specified server for a list of categories, and returns a
   * {@link DBQuery} that, when run, will write the given categories into the
   * database. Categories that are of a lower revision than the local copy will
   * not be overwritten.
   * 
   * @param loc
   * @return
   */
  public static final DBQuery<ListCategoryResponse> retrieveCategories(final ServerLocation loc, final ListCategoryRequest request,
      final List<ExtensionName> localExtensions) {
    final BugLinkService service = BugLinkServiceClient.create(loc);
    final ListCategoryResponse response = service.listCategories(request);
    final List<ExtensionName> list = new ArrayList<>();
    list.addAll(response.getDependencies());
    list.removeAll(localExtensions);
    GetExtensionsResponse resp = new GetExtensionsResponse();
    final GetExtensionsRequest req = new GetExtensionsRequest();
    req.getExtensions().addAll(response.getDependencies());
    req.getExtensions().removeAll(localExtensions);
    if (!req.getExtensions().isEmpty()) {
      resp = service.getExtensions(req);
    }
    return updateCategories(response, resp, true);
  }

  /**
   * Queries the specified server for a list of categories, and returns a
   * {@link DBQuery} that, when run, will return a list of categories that would
   * have been updated. Categories that are of a lower revision than the local
   * copy will not be returned.
   */
  public static final DBQuery<ListCategoryResponse> getNewCategories(final ServerLocation loc, final ListCategoryRequest request,
      final List<ExtensionName> localExtensions) {
    final BugLinkService service = BugLinkServiceClient.create(loc);
    final ListCategoryResponse response = service.listCategories(request);
    final List<ExtensionName> list = new ArrayList<>();
    list.addAll(response.getDependencies());
    list.removeAll(localExtensions);
    GetExtensionsResponse resp = new GetExtensionsResponse();
    final GetExtensionsRequest req = new GetExtensionsRequest();
    req.getExtensions().addAll(response.getDependencies());
    req.getExtensions().removeAll(localExtensions);
    if (!req.getExtensions().isEmpty()) {
      resp = service.getExtensions(req);
    }
    return updateCategories(response, resp, false);
  }

  /**
   * Produces a query that, when run, will return and optionally update the set
   * of categories that are changed when applying the given response to the
   * local database.
   * 
   * @param categories
   * @param newExtensions
   * @param update
   * @return
   */
  public static final DBQuery<ListCategoryResponse> updateCategories(final ListCategoryResponse categories,
      final GetExtensionsResponse newExtensions, final boolean update) {
    return new DBQuery<ListCategoryResponse>() {
      @Override
      public ListCategoryResponse perform(final Query q) {
        final Categories sets = new Categories(q);
        final FindingTypes types = new FindingTypes(q);
        final Queryable<Void> delete = update ? q.prepared("Definitions.deleteDefinition") : null;
        final Queryable<Void> insert = update ? q.prepared("Definitions.insertDefinition") : null;
        if (update) {
          for (final Extension e : newExtensions.getExtension()) {
            types.registerExtension(FindingTypes.convertDO(e));
          }
        }
        final Iterator<FilterSet> it = categories.getFilterSets().iterator();
        while (it.hasNext()) {
          final FilterSet set = it.next();
          final String uid = set.getUid();
          final CategoryDO c = sets.getCategory(uid);
          if (c == null) {
            if (update) {
              insert.call(uid, set.getOwner());
              sets.writeCategory(Categories.convertDO(set));
            }
          } else if (c.getRevision() < set.getRevision()) {
            if (update) {
              delete.call(uid);
              insert.call(uid, set.getOwner());
              sets.writeCategory(Categories.convertDO(set));
            }
          } else if (!update) {
            it.remove(); // Remove since it's older
          }
        }
        if (update) {
          sets.deleteCategories(categories.getDeletions());
        }
        return categories;
      }
    };
  }

  public static final DBQuery<List<CategoryDO>> getLocalCategories() {
    return new DBQuery<List<CategoryDO>>() {
      @Override
      public List<CategoryDO> perform(final Query q) {
        final Categories sets = new Categories(q);
        return sets.listCategories();
      }
    };
  }

  public static final DBQuery<ListScanFilterRequest> scanFilterRequest() {
    return new DBQuery<ListScanFilterRequest>() {
      @Override
      public ListScanFilterRequest perform(final Query q) {
        final ListScanFilterRequest r = new ListScanFilterRequest();
        r.getServerRevisions().addAll(q.statement("ScanFilters.latestServerRevisions", new ServerRevisionRowHandler()).call());
        return r;
      }
    };
  }

  public static final DBQuery<ServerScanFilterInfo> retrieveScanFilters(final ServerLocation loc,
      final ListScanFilterRequest request, final List<ExtensionName> localExtensions) {
    return getScanFilters(loc, request, localExtensions, true);
  }

  public static final DBQuery<ServerScanFilterInfo> getNewScanFilters(final ServerLocation loc,
      final ListScanFilterRequest request, final List<ExtensionName> localExtensions) {
    return getScanFilters(loc, request, localExtensions, false);
  }

  private static final DBQuery<ServerScanFilterInfo> getScanFilters(final ServerLocation loc, final ListScanFilterRequest request,
      final List<ExtensionName> localExtensions, final boolean update) {
    final BugLinkService service = BugLinkServiceClient.create(loc);
    final ListScanFilterResponse response = service.listScanFilters(request);
    final GetExtensionsRequest req = new GetExtensionsRequest();
    req.getExtensions().addAll(response.getDependencies());
    req.getExtensions().removeAll(localExtensions);
    final GetExtensionsResponse resp = req.getExtensions().isEmpty() ? new GetExtensionsResponse() : service.getExtensions(req);
    final Set<ScanFilter> changed = new HashSet<>();
    return new DBQuery<ServerScanFilterInfo>() {
      @Override
      public ServerScanFilterInfo perform(final Query q) {
        final FindingTypes types = new FindingTypes(q);
        final ScanFilters filters = new ScanFilters(q);
        if (update) {
          for (final Extension e : resp.getExtension()) {
            types.registerExtension(FindingTypes.convertDO(e));
          }
        }
        final Queryable<Void> delete = update ? q.prepared("Definitions.deleteDefinition") : null;
        final Queryable<Void> insert = update ? q.prepared("Definitions.insertDefinition") : null;
        for (final ScanFilter filter : response.getScanFilter()) {
          final String uid = filter.getUid();
          final ScanFilterDO f = filters.getScanFilter(uid);
          if (f == null) {
            if (update) {
              insert.call(uid, filter.getOwner());
              filters.writeScanFilter(ScanFilters.convertDO(filter));
            }
            changed.add(filter);
          } else if (f.getRevision() < filter.getRevision()) {
            if (update) {
              delete.call(uid);
              insert.call(uid, filter.getOwner());
              filters.writeScanFilter(ScanFilters.convertDO(filter));
            }
            changed.add(filter);
          }
        }
        if (update) {
          for (final String uid : response.getDeletions()) {
            filters.deleteScanFilter(uid);
          }
        }
        return new ServerScanFilterInfo(response.getScanFilter(), changed, response.getDeletions());
      }
    };
  }

  /**
   * Returns the scan filters that are available locally, keyed by server label.
   * 
   * @return
   */
  public static final DBQuery<Map<NamedServer, List<ScanFilter>>> getLocalScanFilters() {
    return new DBQuery<Map<NamedServer, List<ScanFilter>>>() {
      @Override
      public Map<NamedServer, List<ScanFilter>> perform(final Query q) {
        final Map<NamedServer, List<ScanFilter>> m = new HashMap<>();
        final ScanFilters filters = new ScanFilters(q);
        final List<ScanFilterDO> sfs = filters.listScanFilters();
        final Map<String, ScanFilterDO> dos = new HashMap<>(sfs.size());
        for (final ScanFilterDO sf : sfs) {
          dos.put(sf.getUid(), sf);
        }
        q.prepared("ServerLocations.serverScanFilters", new NullRowHandler() {
          NamedServer label;
          List<ScanFilter> sfs;

          @Override
          protected void doHandle(final Row r) {
            final NamedServer l = new NamedServer(r.nextString(), r.nextString());
            final String uuid = r.nextString();
            if (!l.equals(label)) {
              label = l;
              sfs = new ArrayList<>();
              m.put(l, sfs);
            }
            sfs.add(ScanFilters.convert(dos.get(uuid), l.getUuid()));
          }
        }).call();
        return m;
      }
    };
  }

  /**
   * Generates a filter to be used when when filtering artifacts from a scan and
   * assigning importance to findings during a scan publish action.
   * 
   * @param uid
   * @return
   */
  public static final DBQuery<ScanFilterView> scanFilterForUid(final String uid) {
    return new DBQuery<ScanFilterView>() {
      @Override
      public ScanFilterView perform(final Query q) {
        final ScanFilters filters = new ScanFilters(q);
        final ScanFilterDO sf = filters.getScanFilter(uid);
        if (sf == null) {
          throw new IllegalArgumentException("No scan filter with uid " + uid + " exists");
        }
        return scanFilterView(q, sf);
      }

    };
  }

  /**
   * Generates a filter to be used when when filtering artifacts from a scan and
   * assigning importance to findings during a scan publish action. Attach it to
   * the given scan.
   * 
   * @param projectName
   * @return
   */
  public static final DBQuery<ScanFilterView> scanFilterForProject(final String projectName) {
    return new DBQuery<ScanFilterView>() {
      @Override
      public ScanFilterView perform(final Query q) {
        final ScanFilters filters = new ScanFilters(q);
        final ScanFilterDO sf = filters.getScanFilterByProject(projectName);
        return scanFilterView(q, sf);
      }
    };
  }

  public static final DBQuery<String> scanFilterNameForProject(final String projectName) {
    return new DBQuery<String>() {
      @Override
      public String perform(final Query q) {
        final ScanFilters filters = new ScanFilters(q);
        final ScanFilterDO sf = filters.getScanFilterByProject(projectName);
        // Not using ScanFilterView due to overhead of getting all the
        // details
        return sf.getName();
      }
    };
  }

  /**
   * Record the scan filter used to generate a particular scan.
   * 
   * @param projectName
   * @return
   */
  public static DBQuery<ScanFilterView> recordScanFilter(final ScanFilterView filter, final String scanUuid) {
    if (filter == null || scanUuid == null) {
      throw new IllegalArgumentException("Arguments may not be null.");
    }
    return new DBQuery<ScanFilterView>() {
      @Override
      public ScanFilterView perform(final Query q) {
        final ScanFilterDO normFilter = new ScanFilterDO();
        normFilter.setName(filter.getName());
        normFilter.setUid(filter.getUuid());
        normFilter.setRevision(filter.getRevision());
        final Set<TypeFilterDO> filters = normFilter.getFilterTypes();
        for (final Entry<String, Importance> e : filter.getIncludedFindingTypesAndImportances().entrySet()) {
          filters.add(new TypeFilterDO(e.getKey(), e.getValue(), false));
        }
        final ScanFilters sf = new ScanFilters(q);
        sf.writeScanFilterToScan(normFilter, scanUuid);
        return filter;
      }
    };
  }

  /**
   * Copy the local default settings to the given server.
   * 
   * @param loc
   * @param name
   *          the name for the settings.
   * @return
   */
  public static NullDBQuery copyDefaultScanFilterToServer(final ServerLocation loc, final String name) {
    return new NullDBQuery() {
      @Override
      public void doPerform(final Query q) {
        final ScanFilters filters = new ScanFilters(q);
        final ScanFilter f = ScanFilters.convert(filters.getDefaultScanFilter(), null);
        final CreateScanFilterRequest r = new CreateScanFilterRequest();
        r.setName(name);
        r.getCategoryFilter().addAll(f.getCategoryFilter());
        r.getTypeFilter().addAll(f.getTypeFilter());
        BugLinkServiceClient.create(loc).createScanFilter(r);
      }
    };
  }

  /**
   * Updates the default scan filter to be equivalent to the given scan filter.
   * This is a copy operation, so later changes to the provided scan filter will
   * have no effect on the local defaults.
   * 
   * @param scanFilterUuid
   * @return
   */
  public static DBQuery<ScanFilterDO> updateDefaultScanFilter(final String scanFilterUuid) {
    return new DBQuery<ScanFilterDO>() {
      @Override
      public ScanFilterDO perform(final Query q) {
        final ScanFilters filters = new ScanFilters(q);
        final ScanFilterDO sf = filters.getScanFilter(scanFilterUuid);
        if (sf == null) {
          throw new IllegalArgumentException(String.format("%s is not a locally available scan filter.", scanFilterUuid));
        }
        sf.setName(LOCAL_NAME);
        sf.setUid(LOCAL_UUID);
        sf.setRevision(0L);
        filters.writeScanFilter(sf);
        return sf;
      }
    };
  }

  /**
   * Update the default filter set locally. Do not increment the revision.
   * 
   * @param uids
   *          a list of finding type identifiers to include in scans
   * @return
   */
  public static DBQuery<ScanFilterDO> updateDefaultScanFilter(final Collection<String> uids) {
    return new DBQuery<ScanFilterDO>() {
      @Override
      public ScanFilterDO perform(final Query q) {
        final ScanFilters s = new ScanFilters(q);
        final ScanFilterDO scanFilter = new ScanFilterDO();
        scanFilter.setName(LOCAL_NAME);
        scanFilter.setUid(LOCAL_UUID);
        scanFilter.setRevision(0L);
        final Set<TypeFilterDO> types = scanFilter.getFilterTypes();
        for (final String uid : uids) {
          types.add(new TypeFilterDO(uid, null, false));
        }
        s.writeScanFilter(scanFilter);
        return scanFilter;
      }
    };
  }

  static ScanFilterView scanFilterView(final Query q, final ScanFilterDO sf) {
    final Categories cats = new Categories(q);
    final FindingTypes types = new FindingTypes(q);
    final Set<CategoryFilterDO> catFilters = sf.getCategories();
    final List<String> catFilterUids = new ArrayList<>(catFilters.size());
    for (final CategoryFilterDO cat : catFilters) {
      catFilterUids.add(cat.getUid());
    }
    final Map<String, CategoryGraph> catGraphs = new HashMap<>(catFilters.size());
    final Set<String> typeSet = new HashSet<>(catFilters.size() * 10);
    for (final CategoryGraph graph : cats.getCategoryGraphs(catFilterUids)) {
      catGraphs.put(graph.getUid(), graph);
      typeSet.addAll(graph.getFindingTypes());
    }
    for (final TypeFilterDO typeFilter : sf.getFilterTypes()) {
      typeSet.add(typeFilter.getFindingType());
    }
    final Map<String, FindingTypeDO> typeMap = new HashMap<>(typeSet.size());
    for (final String type : typeSet) {
      typeMap.put(type, types.getFindingType(type));
    }
    return new ScanFilterView(sf, typeMap, catGraphs);
  }

  /**
   * The full location, on the Java classpath, of the default world file.
   */
  public static final String DEFAULT_FILTER_SET_FILE = "com/surelogic/sierra/tool/message/data/buglink-default-scan-filter.txt";

  /**
   * Gets the default set of finding type UUIDs that have been selected by
   * SureLogic to be included in scans.
   * 
   * @return the SureLogic default filter set.
   */
  public static Set<String> getSureLogicDefaultScanFilters() {

    final Set<String> types = new HashSet<>();
    final URL defaultURL = Thread.currentThread().getContextClassLoader().getResource(DEFAULT_FILTER_SET_FILE);
    if (defaultURL == null) {
      SLLogger.getLogger().log(Level.WARNING,
          "Unable to find the SureLogic default filter set file " + DEFAULT_FILTER_SET_FILE + " on the classpath.");
      return types;
    }
    try {
      final BufferedReader in = new BufferedReader(new InputStreamReader(defaultURL.openStream()));
      try {
        String s = in.readLine();
        while (s != null) {
          s = s.trim();
          if (!s.startsWith("--") && !"".equals(s)) {
            types.add(s);
          }
          s = in.readLine();
        }
      } finally {
        in.close();
      }
    } catch (final IOException e) {
      SLLogger.getLogger().log(Level.WARNING,
          "IO failure reading the SureLogic default filter set file " + DEFAULT_FILTER_SET_FILE, e);
    }
    return types;
  }

  /**
   * Update the connection information on an existing server location. The name
   * may not be changed, and the uuid must match an existing connected server
   * stored in the database.
   * 
   * @param cs
   * @param savePassword
   * @return
   */
  public static NullDBQuery updateServerLocation(final ConnectedServer cs, final boolean savePassword) {
    return new NullDBQuery() {

      @Override
      public void doPerform(final Query q) {
        final ServerLocation l = cs.getLocation();
        final Map<ConnectedServer, Collection<String>> servers = ServerLocations.fetchQuery(null).perform(q);
        final Map<ConnectedServer, Collection<String>> newServers = new HashMap<>(servers.size());
        for (final Entry<ConnectedServer, Collection<String>> entry : servers.entrySet()) {
          boolean found = false;
          final ConnectedServer cs1 = entry.getKey();
          if (cs1.getUuid().equals(cs.getUuid())) {
            found = true;
            new ConnectedServer(cs1.getUuid(), cs1.getName(), cs1.isTeamServer(), l);
            newServers.put(cs, entry.getValue());
          } else {
            newServers.put(cs1, entry.getValue());
          }
          if (!found) {
            throw new IllegalArgumentException("No server matching this uuid exists in the database.");
          }
        }
        ServerLocations.saveQuery(newServers, savePassword).doPerform(q);
      }
    };
  }

  /**
   * Makes a call to the specified server, and saves the location and
   * corresponding server information into the database. If the transaction
   * fails, the cause will be an {@link InvalidServerException} if we already
   * have a connection that points to this server in the database.
   * 
   * @param server
   * @throws SierraServiceClientException
   *           if the server cannot be validated
   * @return the reply from the server
   */
  public static DBQuery<ConnectedServer> checkAndSaveServerLocation(final ServerLocation server, final boolean savePassword) {
    final ServerInfoReply reply = ServerInfoServiceClient.create(server).getServerInfo(new ServerInfoRequest());
    return new DBQuery<ConnectedServer>() {
      @Override
      public ConnectedServer perform(final Query q) {
        ServerLocations.updateServerIdentities(reply.getServers()).perform(q);
        final String name = reply.getName() == null ? reply.getUid() : reply.getName();
        final ConnectedServer s = new ConnectedServer(reply.getUid(), name, reply.getServices().contains(Services.TEAMSERVER),
            server);
        ServerLocations.saveServerLocation(s, savePassword).perform(q);
        return s;
      }
    };

  }

  public static NullDBQuery updateServerInfo(final ServerLocation location) {
    final ServerInfoReply reply = ServerInfoServiceClient.create(location).getServerInfo(new ServerInfoRequest());
    return ServerLocations.updateServerIdentities(reply.getServers());
  }
}
