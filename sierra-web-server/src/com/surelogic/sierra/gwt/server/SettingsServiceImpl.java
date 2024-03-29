package com.surelogic.sierra.gwt.server;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.google.common.base.CharMatcher;
import com.surelogic.common.jdbc.DBQuery;
import com.surelogic.common.jdbc.Query;
import com.surelogic.common.jdbc.Queryable;
import com.surelogic.common.jdbc.ResultHandler;
import com.surelogic.common.jdbc.Row;
import com.surelogic.common.jdbc.RowHandler;
import com.surelogic.common.jdbc.SingleRowHandler;
import com.surelogic.common.jdbc.StringResultHandler;
import com.surelogic.common.jdbc.TransactionException;
import com.surelogic.sierra.gwt.SierraServiceServlet;
import com.surelogic.sierra.gwt.client.data.Category;
import com.surelogic.sierra.gwt.client.data.Extension;
import com.surelogic.sierra.gwt.client.data.FindingType;
import com.surelogic.sierra.gwt.client.data.FindingType.ArtifactTypeInfo;
import com.surelogic.sierra.gwt.client.data.FindingTypeFilter;
import com.surelogic.sierra.gwt.client.data.PortalServerLocation;
import com.surelogic.sierra.gwt.client.data.PortalServerLocation.Protocol;
import com.surelogic.sierra.gwt.client.data.Project;
import com.surelogic.sierra.gwt.client.data.Report.OutputType;
import com.surelogic.sierra.gwt.client.data.ReportSettings;
import com.surelogic.sierra.gwt.client.data.Result;
import com.surelogic.sierra.gwt.client.data.ScanFilter;
import com.surelogic.sierra.gwt.client.data.ScanFilterEntry;
import com.surelogic.sierra.gwt.client.data.Status;
import com.surelogic.sierra.gwt.client.data.cache.ReportCache;
import com.surelogic.sierra.gwt.client.data.dashboard.DashboardSettings;
import com.surelogic.sierra.gwt.client.data.dashboard.DashboardSettings.DashboardRow;
import com.surelogic.sierra.gwt.client.data.dashboard.ReportWidget;
import com.surelogic.sierra.gwt.client.service.SettingsService;
import com.surelogic.sierra.jdbc.RevisionException;
import com.surelogic.sierra.jdbc.dashboard.DashboardQueries;
import com.surelogic.sierra.jdbc.project.ProjectDO;
import com.surelogic.sierra.jdbc.project.Projects;
import com.surelogic.sierra.jdbc.reports.ReportSettingQueries;
import com.surelogic.sierra.jdbc.server.ConnectionFactory;
import com.surelogic.sierra.jdbc.server.Server;
import com.surelogic.sierra.jdbc.server.ServerFiles;
import com.surelogic.sierra.jdbc.server.ServerQuery;
import com.surelogic.sierra.jdbc.server.UserQuery;
import com.surelogic.sierra.jdbc.settings.Categories;
import com.surelogic.sierra.jdbc.settings.CategoryDO;
import com.surelogic.sierra.jdbc.settings.CategoryEntryDO;
import com.surelogic.sierra.jdbc.settings.CategoryFilterDO;
import com.surelogic.sierra.jdbc.settings.ConnectedServer;
import com.surelogic.sierra.jdbc.settings.ScanFilterDO;
import com.surelogic.sierra.jdbc.settings.ScanFilters;
import com.surelogic.sierra.jdbc.settings.ServerLocations;
import com.surelogic.sierra.jdbc.settings.SettingQueries;
import com.surelogic.sierra.jdbc.settings.TypeFilterDO;
import com.surelogic.sierra.jdbc.tool.ArtifactTypeDO;
import com.surelogic.sierra.jdbc.tool.ExtensionDO;
import com.surelogic.sierra.jdbc.tool.FindingTypeDO;
import com.surelogic.sierra.jdbc.tool.FindingTypes;
import com.surelogic.sierra.jdbc.user.User;
import com.surelogic.sierra.tool.message.ServerLocation;

public final class SettingsServiceImpl extends SierraServiceServlet implements SettingsService {
  private static final long serialVersionUID = 6781260512153199775L;

  public List<String> searchProjects(final String query, final int limit) {
    return ConnectionFactory.INSTANCE.withUserReadOnly(new UserQuery<List<String>>() {
      public List<String> perform(final Query q, final Server s, final User u) {
        return q.prepared("Projects.query", new ResultHandler<List<String>>() {
          public List<String> handle(final com.surelogic.common.jdbc.Result r) {
            final List<String> result = new ArrayList<>();
            final int count = 0;
            for (final Row row : r) {
              if (limit == -1 || count < limit) {
                result.add(row.nextString());
              } else {
                return result;
              }
            }
            return result;
          }

        }).call(query.replace('*', '%').concat("%"));
      }
    });
  }

  public List<Project> getProjects() {
    return ConnectionFactory.INSTANCE.withUserTransaction(new UserQuery<List<Project>>() {
      public List<Project> perform(final Query q, final Server s, final User u) {
        final List<Project> result = new ArrayList<>();
        final Projects projects = new Projects(q);
        final ScanFilters filters = new ScanFilters(q);
        final FindingTypes types = new FindingTypes(q);
        final Categories cats = new Categories(q);
        for (final ProjectDO projectDO : projects.listProjects()) {
          final Project prj = new Project();
          prj.setUuid(projectDO.getUuid());
          prj.setName(projectDO.getName());
          final String sfUuid = projectDO.getScanFilter();
          prj.setScanFilter(ServiceUtil.getFilter(filters.getScanFilter(sfUuid), types, cats));
          result.add(prj);
        }
        return result;
      }
    });
  }

  public List<Category> getCategories() {
    return ConnectionFactory.INSTANCE.withUserReadOnly(new UserQuery<List<Category>>() {

      public List<Category> perform(final Query q, final Server server, final User user) {
        final String serverUID = server.getUid();
        final Map<String, Category> sets = new HashMap<>();
        final FindingTypes types = new FindingTypes(q);
        final Categories fs = new Categories(q);
        /*
         * Get the server locations that we know about, keyed by label
         */
        final Map<String, ConnectedServer> servers = new HashMap<>();
        for (final ConnectedServer s : ServerLocations.fetchQuery(null).perform(q).keySet()) {
          servers.put(s.getUuid(), s);
        }
        /*
         * Retrieves the server uuid and label for the category
         */
        final Queryable<String[]> categoryServer = q.prepared("Definitions.getDefinitionServer",
            SingleRowHandler.from(new RowHandler<String[]>() {
          public String[] handle(final com.surelogic.common.jdbc.Row row) {
            return new String[] { row.nextString(), row.nextString() };
          }
        }));
        for (final CategoryDO detail : fs.listCategories()) {
          final Category set = getOrCreateSet(detail.getUid(), sets);
          set.setName(detail.getName());
          final String[] owningServer = categoryServer.call(detail.getUid());
          set.setLocal(owningServer != null && serverUID.equals(owningServer[0]));
          if (owningServer != null) {
            set.setOwnerLabel(owningServer[1]);
            final ConnectedServer owner = servers.get(owningServer[1]);
            if (owner != null) {
              final StringBuilder urlBuf = new StringBuilder(owner.getLocation().createHomeURL().toString());
              urlBuf.append("#categories/uuid=");
              urlBuf.append(detail.getUid());
              set.setOwnerURL(urlBuf.toString());
            }
          }
          String info = CharMatcher.is('\t').removeFrom(detail.getInfo());
          info.replace('\n', ' ');
          set.setInfo(info);
          final Set<Category> parents = new HashSet<>();
          for (final String parent : detail.getParents()) {
            parents.add(getOrCreateSet(parent, sets));
          }
          set.setParents(parents);
          final Set<FindingTypeFilter> filters = new HashSet<>();
          for (final CategoryEntryDO fDetail : detail.getFilters()) {
            final FindingTypeFilter filter = new FindingTypeFilter();
            filter.setFiltered(fDetail.isFiltered());
            final FindingTypeDO type = types.getFindingType(fDetail.getFindingType());
            filter.setName(type.getName());
            filter.setUuid(type.getUid());
            filter.setShortMessage(type.getShortMessage());
            filters.add(filter);
          }
          set.setEntries(filters);
          set.setRevision(detail.getRevision());
          q.prepared("FilterSets.scanFiltersUsing", new RowHandler<Void>() {
            public Void handle(final Row r) {
              set.getScanFiltersUsing().add(new Category.ScanFilterInfo(r.nextString(), r.nextString()));
              return null;
            }
          }).call(detail.getId());
        }
        final List<Category> values = new ArrayList<>(sets.values());
        Collections.sort(values);
        return values;
      }
    });
  }

  static Category getOrCreateSet(final String uid, final Map<String, Category> sets) {
    Category set = sets.get(uid);
    if (set == null) {
      set = new Category();
      set.setUuid(uid);
      sets.put(uid, set);
    }
    return set;
  }

  public Result<String> createCategory(final String name, final List<String> entries, final List<String> parents) {
    return ConnectionFactory.INSTANCE.withUserTransaction(new UserQuery<Result<String>>() {
      public Result<String> perform(final Query q, final Server s, final User u) {
        final Categories sets = new Categories(q);
        final long revision = s.nextRevision();
        final CategoryDO set = sets.createCategory(name, null, revision);
        set.getParents().addAll(parents);
        final Set<CategoryEntryDO> doEntries = set.getFilters();
        final List<String> moEntries = entries;
        for (final String entry : moEntries) {
          doEntries.add(new CategoryEntryDO(entry, true));
        }
        sets.updateCategory(set, revision);
        q.prepared("Definitions.insertDefinition").call(set.getUid(), s.getUid());
        return Result.success("Filter set " + name + " created.", set.getUid());
      }
    });

  }

  // TODO do this w/o passing back and forth the whole graph
  public Status updateCategory(final Category c) {
    return ConnectionFactory.INSTANCE.withUserTransaction(new UserQuery<Status>() {
      public Status perform(final Query q, final Server s, final User u) {
        if (!s.getUid().equals(q.prepared("Definitions.getDefinitionServer", new StringResultHandler()).call(c.getUuid()))) {
          return Status.failure("This category is not owned by this server, and cannot be updated.");
        }
        final Categories sets = new Categories(q);
        final long revision = s.nextRevision();
        final CategoryDO set = new CategoryDO();
        set.setUid(c.getUuid());
        set.setInfo(c.getInfo());
        set.setName(c.getName());
        set.setRevision(c.getRevision());
        final Set<String> parentSet = set.getParents();
        final Set<Category> parents = c.getParents();
        for (final Category parent : parents) {
          parentSet.add(parent.getUuid());
        }
        final Set<CategoryEntryDO> entrySet = set.getFilters();
        final Set<FindingTypeFilter> entries = c.getEntries();
        for (final FindingTypeFilter entry : entries) {
          entrySet.add(new CategoryEntryDO(entry.getUuid(), entry.isFiltered()));
        }
        try {
          sets.updateCategory(set, revision);
        } catch (final RevisionException e) {
          return Status.failure(e.getMessage());
        } catch (final IllegalArgumentException e) {
          return Status.failure(e.getMessage());
        }
        return Status.success("Category " + c.getName() + " updated.");
      }
    });
  }

  public Result<String> duplicateCategory(final String newName, final Category source) {
    return ConnectionFactory.INSTANCE.withUserTransaction(new UserQuery<Result<String>>() {
      public Result<String> perform(final Query q, final Server s, final User u) {
        final Categories sets = new Categories(q);
        final long revision = s.nextRevision();
        final CategoryDO set = sets.createCategory(newName, source.getInfo(), revision);
        final Set<String> catParentUuids = set.getParents();
        for (final Category parent : source.getParents()) {
          catParentUuids.add(parent.getUuid());
        }
        final Set<CategoryEntryDO> doEntries = set.getFilters();
        for (final FindingTypeFilter entry : source.getEntries()) {
          doEntries.add(new CategoryEntryDO(entry.getUuid(), entry.isFiltered()));
        }
        sets.updateCategory(set, revision);
        q.prepared("Definitions.insertDefinition").call(set.getUid(), s.getUid());
        return Result.success("Category duplicate " + newName + " created.", set.getUid());
      }
    });
  }

  public Status deleteCategory(final String uuid) {
    return ConnectionFactory.INSTANCE.withUserTransaction(new UserQuery<Status>() {
      public Status perform(final Query q, final Server s, final User u) {
        final Categories cats = new Categories(q);
        final String server = s.getUid();
        if (server.equals(q.prepared("Definitions.getDefinitionServer", new StringResultHandler()).call(uuid))) {
          try {
            q.prepared("FilterSets.insertDeletedFilterSet").call(uuid, server, s.nextRevision());
            cats.deleteCategory(uuid);
          } catch (final IllegalArgumentException e) {
            return Status.failure(e.getMessage());
          }
        } else {
          return Status.failure("The category does not belong to this server and cannot be deleted.");
        }
        return Status.success("Category deleted");
      }
    });
  }

  public List<FindingType> getFindingTypes() {
    return ConnectionFactory.INSTANCE.withUserTransaction(new UserQuery<List<FindingType>>() {
      public List<FindingType> perform(final Query q, final Server s, final User u) {
        final List<FindingType> result = new ArrayList<>();
        final FindingTypes types = new FindingTypes(q);
        for (final FindingTypeDO type : types.listFindingTypes()) {
          result.add(getType(type, q));
        }
        Collections.sort(result);
        return result;
      }
    });
  }

  public Result<FindingType> getFindingType(final String uuid) {
    return ConnectionFactory.INSTANCE.withUserTransaction(new UserQuery<Result<FindingType>>() {
      public Result<FindingType> perform(final Query q, final Server s, final User u) {
        final FindingTypes types = new FindingTypes(q);
        final FindingTypeDO type = types.getFindingType(uuid);
        if (type != null) {
          return Result.success("Finding Type Found", getType(type, q));
        } else {
          return Result.failure("No finding type found", null);
        }
      }
    });
  }

  FindingType getType(final FindingTypeDO type, final Query q) {
    final FindingType info = new FindingType();
    info.setInfo(type.getInfo());
    info.setName(type.getName());
    info.setShortMessage(type.getShortMessage());
    info.setUuid(type.getUid());
    final Map<ArtifactTypeInfo, ArtifactTypeInfo> set = new HashMap<>();
    for (final ArtifactTypeDO artDO : type.getArtifactTypes()) {
      final ArtifactTypeInfo newInfo = new ArtifactTypeInfo(artDO.getTool(), artDO.getMnemonic());
      final ArtifactTypeInfo oldInfo = set.get(newInfo);
      if (oldInfo == null) {
        set.put(newInfo, newInfo);
        newInfo.getVersions().add(artDO.getVersion());
      } else {
        oldInfo.getVersions().add(artDO.getVersion());
      }
    }
    info.getArtifactTypes().addAll(set.keySet());
    q.prepared("FindingTypes.categoriesReferencing", new RowHandler<Void>() {
      public Void handle(final Row r) {
        final FindingType.CategoryInfo ci = new FindingType.CategoryInfo(r.nextString(), r.nextString(), r.nextString());
        if (r.nextBoolean()) {
          info.getCategoriesExcluding().add(ci);
        } else {
          info.getCategoriesIncluding().add(ci);
        }
        return null;
      }
    }).call(type.getId());
    q.prepared("FindingTypes.scanFiltersIncluding", new RowHandler<Void>() {
      public Void handle(final Row r) {
        info.getScanFiltersIncluding().add(new FindingType.ScanFilterInfo(r.nextString(), r.nextString()));
        return null;
      }
    }).call(type.getId());
    return info;
  }

  public List<ScanFilter> getScanFilters() {
    return ConnectionFactory.INSTANCE.withReadOnly(new ServerQuery<List<ScanFilter>>() {

      public List<ScanFilter> perform(final Query q, final Server s) {
        final String serverUID = s.getUid();
        final Queryable<String> definitionServer = q.prepared("Definitions.getDefinitionServer", new StringResultHandler());

        final FindingTypes ft = new FindingTypes(q);
        final Categories fs = new Categories(q);
        final ScanFilters filters = new ScanFilters(q);
        final ScanFilterDO def = filters.getDefaultScanFilter();
        final List<ScanFilter> list = new ArrayList<>();
        for (final ScanFilterDO fDO : filters.listScanFilters()) {
          final ScanFilter filter = ServiceUtil.getFilter(fDO, ft, fs);
          filter.setLocal(serverUID.equals(definitionServer.call(filter.getUuid())));
          filter.setDefault(fDO.getUid().equals(def.getUid()));
          list.add(filter);
        }
        Collections.sort(list);
        return list;
      }
    });
  }

  public ScanFilter createScanFilter(final String name) {
    return ConnectionFactory.INSTANCE.withUserTransaction(new UserQuery<ScanFilter>() {
      public ScanFilter perform(final Query q, final Server s, final User u) {
        final ScanFilters filters = new ScanFilters(q);
        final ScanFilter f = ServiceUtil.getFilter(filters.createScanFilter(name, s.nextRevision()), new FindingTypes(q),
            new Categories(q));
        f.setLocal(true);
        q.prepared("Definitions.insertDefinition").call(f.getUuid(), s.getUid());
        return f;
      }
    });
  }

  public Status updateScanFilter(final ScanFilter f) {
    final ScanFilterDO fDO = new ScanFilterDO();
    fDO.setName(f.getName());
    fDO.setRevision(f.getRevision());
    fDO.setUid(f.getUuid());
    final Set<CategoryFilterDO> cats = fDO.getCategories();
    final Set<TypeFilterDO> types = fDO.getFilterTypes();
    for (final ScanFilterEntry e : f.getCategories()) {
      final CategoryFilterDO c = new CategoryFilterDO();
      c.setImportance(ServiceUtil.importance(e.getImportance()));
      c.setUid(e.getUuid());
      cats.add(c);
    }
    for (final ScanFilterEntry e : f.getTypes()) {
      final TypeFilterDO t = new TypeFilterDO();
      t.setImportance(ServiceUtil.importance(e.getImportance()));
      t.setFiltered(false);
      t.setFindingType(e.getUuid());
      types.add(t);
    }
    return ConnectionFactory.INSTANCE.withUserTransaction(new UserQuery<Status>() {
      public Status perform(final Query q, final Server s, final User u) {
        if (!s.getUid().equals(q.prepared("Definitions.getDefinitionServer", new StringResultHandler()).call(fDO.getUid()))) {
          return Status.failure("This scan filter is not owned by this server, and cannot be updated.");
        }
        final ScanFilters sf = new ScanFilters(q);
        try {
          sf.updateScanFilter(fDO, s.nextRevision());
        } catch (final RevisionException e) {
          return Status.failure("Someone else has already updated this scan filter.");
        }
        return Status.success("Scan filter updated");
      }
    });

  }

  public Status deleteScanFilter(final String uuid) {
    return ConnectionFactory.INSTANCE.withUserTransaction(new UserQuery<Status>() {
      public Status perform(final Query q, final Server s, final User u) {
        return ConnectionFactory.INSTANCE.withUserTransaction(new UserQuery<Status>() {
          public Status perform(final Query q, final Server s, final User u) {
            final ScanFilters filters = new ScanFilters(q);
            final String server = s.getUid();
            if (server.equals(q.prepared("Definitions.getDefinitionServer", new StringResultHandler()).call(uuid))) {
              try {
                q.prepared("ScanFilters.insertDeletedScanFilter").call(uuid, server, s.nextRevision());
                filters.deleteScanFilter(uuid);
              } catch (final IllegalArgumentException e) {
                return Status.failure(e.getMessage());
              }
            } else {
              return Status.failure("The scan filter does not belong to this server and cannot be deleted.");
            }
            return Status.success("Category deleted");
          }
        });
      }
    });
  }

  public Status setDefaultScanFilter(final ScanFilter f) {
    return ConnectionFactory.INSTANCE.withUserTransaction(new UserQuery<Status>() {
      public Status perform(final Query q, final Server s, final User u) {
        s.nextRevision();
        final ScanFilters filters = new ScanFilters(q);
        filters.setDefaultScanFilter(f.getUuid());
        return Status.success("Default scan filter saved.");
      }
    });
  }

  public List<PortalServerLocation> listServerLocations() {
    final List<PortalServerLocation> servers = new ArrayList<>();
    for (final ConnectedServer cs : ConnectionFactory.INSTANCE.withReadOnly(ServerLocations.fetchQuery(Collections.EMPTY_MAP))
        .keySet()) {
      final ServerLocation l = cs.getLocation();
      final PortalServerLocation s = new PortalServerLocation(cs.getName(), cs.getUuid(),
          PortalServerLocation.Protocol.fromValue(l.getProtocol()), l.getHost(), l.getPort(), l.getContextPath(), l.getUser(),
          l.getPass(), cs.isTeamServer());
      servers.add(s);
    }
    return servers;
  }

  public Status deleteServerLocation(final String uuid) {
    return ConnectionFactory.INSTANCE.withTransaction(new DBQuery<Status>() {

      public Status perform(final Query q) {
        final Map<ConnectedServer, Collection<String>> servers = ServerLocations.fetchQuery(null).perform(q);
        ConnectedServer server = null;
        for (final ConnectedServer s : servers.keySet()) {
          if (s.getUuid().equals(uuid)) {
            server = s;
            break;
          }
        }
        if (server != null) {
          servers.remove(server);
        }
        ServerLocations.saveQuery(servers, true).doPerform(q);
        return Status.success((server == null ? "Server" : server.getName()) + " deleted.");
      }
    });
  }

  public Status saveServerLocation(final PortalServerLocation loc) {
    final ServerLocation l = new ServerLocation(loc.getHost(), loc.getProtocol() == Protocol.HTTPS, loc.getPort(), loc.getContext(),
        loc.getUser(), loc.getPass(), true, true);
    ConnectedServer cs;
    if (loc.getUuid() == null) {
      // This is a new server location
      try {
        cs = ConnectionFactory.INSTANCE.withTransaction(SettingQueries.checkAndSaveServerLocation(l, true));
      } catch (final TransactionException e) {
        return Status.failure(e.getCause().getMessage());
      }
    } else {
      cs = new ConnectedServer(loc.getUuid(), loc.getName(), loc.isTeamServer(), l);
      ConnectionFactory.INSTANCE.withTransaction(SettingQueries.updateServerLocation(cs, true));
    }
    return Status.success(cs.getName() + " saved.");
  }

  public Status saveProjectFilter(final String project, final String scanFilter) {
    return ConnectionFactory.INSTANCE.withUserTransaction(new UserQuery<Status>() {
      public Status perform(final Query query, final Server server, final User user) {
        server.nextRevision();
        new Projects(query).updateProjectFilter(project, scanFilter);
        return Status.success();
      }
    });

  }

  public List<ReportSettings> listReportSettings() {
    return ConnectionFactory.INSTANCE.withUserReadOnly(ReportSettingQueries.listUserQueries());
  }

  public Status saveReportSettings(final ReportSettings settings) {
    if (settings.getUuid() == null) {
      settings.setUuid(UUID.randomUUID().toString());
    }
    ConnectionFactory.INSTANCE.withUserTransaction(ReportSettingQueries.save(settings));
    return Status.success("Settings saved.");
  }

  public Status deleteReportSettings(final String uuid) {
    ConnectionFactory.INSTANCE.withUserTransaction(ReportSettingQueries.delete(uuid));
    return Status.success("Settings deleted");
  }

  public DashboardSettings getDashboardSettings() {

    DashboardSettings settings = ConnectionFactory.INSTANCE.withUserReadOnly(DashboardQueries.getDashboard());
    if (settings == null) {
      settings = new DashboardSettings();

      final DashboardRow row0 = settings.getRow(0, true);

      final ReportSettings latestScans = new ReportSettings(ReportCache.latestScans());
      latestScans.setWidth("450");
      row0.setLeftColumn(new ReportWidget(latestScans, OutputType.CHART));

      final ReportSettings auditContribs = new ReportSettings(ReportCache.userAudits());
      latestScans.setWidth("320");
      row0.setRightColumn(new ReportWidget(auditContribs, OutputType.CHART));

      final DashboardRow row1 = settings.getRow(1, true);
      final ReportSettings userAudits = new ReportSettings(ReportCache.userAudits());
      row1.setRightColumn(new ReportWidget(userAudits, OutputType.TABLE));

      final DashboardRow row2 = settings.getRow(2, true);
      final ReportSettings publishedProjects = new ReportSettings(ReportCache.publishedProjects());
      row2.setSingleColumn(new ReportWidget(publishedProjects, OutputType.TABLE));
    }
    return settings;
  }

  public Status saveDashboardSettings(final DashboardSettings settings) {
    ConnectionFactory.INSTANCE.withUserTransaction(DashboardQueries.updateDashboard(settings));
    return Status.success();
  }

  public List<Extension> listExtensions() {
    return ConnectionFactory.INSTANCE.withReadOnly(new DBQuery<List<Extension>>() {
      public List<Extension> perform(final Query q) {
        final List<Extension> exts = new ArrayList<>();
        for (final ExtensionDO e : new FindingTypes(q).getExtensions()) {
          final Extension ext = new Extension();
          ext.setName(e.getName());
          ext.setVersion(e.getVersion());
          for (final List<ArtifactTypeDO> artList : e.getArtifactMap().values()) {
            for (final ArtifactTypeDO art : artList) {
              ext.getArtifactTypes().add(new Extension.ArtifactType(art.getTool(), art.getDisplay()));
            }
          }
          for (final FindingTypeDO ftDO : e.getNewFindingTypes()) {
            ext.getFindingTypes().add(new Extension.FindingType(ftDO.getUid(), ftDO.getName()));
          }
          ext.setInstalled(new File(ServerFiles.getSierraLocalTeamServerDirectory(), e.getPath()).exists());
          exts.add(ext);
        }
        return exts;
      }
    });
  }
}