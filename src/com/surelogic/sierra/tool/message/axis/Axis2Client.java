package com.surelogic.sierra.tool.message.axis;

import com.surelogic.sierra.tool.message.InvalidLoginException;
import com.surelogic.sierra.tool.message.SierraServerLocation;
import com.surelogic.sierra.tool.message.SierraService;
import com.surelogic.sierra.tool.message.SierraServiceClientException;
import com.surelogic.sierra.tool.message.axis.SierraServiceBeanServiceStub.FindingTypeFilter;
import com.surelogic.sierra.tool.message.axis.SierraServiceBeanServiceStub.GlobalSettings;
import com.surelogic.sierra.tool.message.axis.SierraServiceBeanServiceStub.WriteGlobalSettings;
import com.surelogic.sierra.tool.message.axis.SierraServiceBeanServiceStub.GlobalSettingsRequest;
import com.surelogic.sierra.tool.message.axis.SierraServiceBeanServiceStub.GetGlobalSettings;
import com.surelogic.sierra.tool.message.axis.SierraServiceBeanServiceStub.Artifact;
import com.surelogic.sierra.tool.message.axis.SierraServiceBeanServiceStub.ArtifactType;
import com.surelogic.sierra.tool.message.axis.SierraServiceBeanServiceStub.Artifacts;
import com.surelogic.sierra.tool.message.axis.SierraServiceBeanServiceStub.Audit;
import com.surelogic.sierra.tool.message.axis.SierraServiceBeanServiceStub.AuditEvent;
import com.surelogic.sierra.tool.message.axis.SierraServiceBeanServiceStub.AuditTrail;
import com.surelogic.sierra.tool.message.axis.SierraServiceBeanServiceStub.AuditTrailResponse;
import com.surelogic.sierra.tool.message.axis.SierraServiceBeanServiceStub.AuditTrailUpdate;
import com.surelogic.sierra.tool.message.axis.SierraServiceBeanServiceStub.ClassMetric;
import com.surelogic.sierra.tool.message.axis.SierraServiceBeanServiceStub.CommitAuditTrailResponse;
import com.surelogic.sierra.tool.message.axis.SierraServiceBeanServiceStub.CommitAuditTrails;
import com.surelogic.sierra.tool.message.axis.SierraServiceBeanServiceStub.CommitAuditTrailsResponse;
import com.surelogic.sierra.tool.message.axis.SierraServiceBeanServiceStub.Config;
import com.surelogic.sierra.tool.message.axis.SierraServiceBeanServiceStub.Error;
import com.surelogic.sierra.tool.message.axis.SierraServiceBeanServiceStub.Errors;
import com.surelogic.sierra.tool.message.axis.SierraServiceBeanServiceStub.FilterEntry;
import com.surelogic.sierra.tool.message.axis.SierraServiceBeanServiceStub.FilterSet;
import com.surelogic.sierra.tool.message.axis.SierraServiceBeanServiceStub.GetAuditTrailRequest;
import com.surelogic.sierra.tool.message.axis.SierraServiceBeanServiceStub.GetAuditTrails;
import com.surelogic.sierra.tool.message.axis.SierraServiceBeanServiceStub.GetAuditTrailsResponse;
import com.surelogic.sierra.tool.message.axis.SierraServiceBeanServiceStub.GetQualifiers;
import com.surelogic.sierra.tool.message.axis.SierraServiceBeanServiceStub.GetQualifiersResponse;
import com.surelogic.sierra.tool.message.axis.SierraServiceBeanServiceStub.GetSettings;
import com.surelogic.sierra.tool.message.axis.SierraServiceBeanServiceStub.GetSettingsResponse;
import com.surelogic.sierra.tool.message.axis.SierraServiceBeanServiceStub.GetUid;
import com.surelogic.sierra.tool.message.axis.SierraServiceBeanServiceStub.GetUidResponse;
import com.surelogic.sierra.tool.message.axis.SierraServiceBeanServiceStub.IdentifierType;
import com.surelogic.sierra.tool.message.axis.SierraServiceBeanServiceStub.Importance;
import com.surelogic.sierra.tool.message.axis.SierraServiceBeanServiceStub.Match;
import com.surelogic.sierra.tool.message.axis.SierraServiceBeanServiceStub.Merge;
import com.surelogic.sierra.tool.message.axis.SierraServiceBeanServiceStub.MergeAuditTrailRequest;
import com.surelogic.sierra.tool.message.axis.SierraServiceBeanServiceStub.MergeAuditTrailResponse;
import com.surelogic.sierra.tool.message.axis.SierraServiceBeanServiceStub.MergeAuditTrails;
import com.surelogic.sierra.tool.message.axis.SierraServiceBeanServiceStub.MergeAuditTrailsResponse;
import com.surelogic.sierra.tool.message.axis.SierraServiceBeanServiceStub.Metrics;
import com.surelogic.sierra.tool.message.axis.SierraServiceBeanServiceStub.Priority;
import com.surelogic.sierra.tool.message.axis.SierraServiceBeanServiceStub.ProjectSettings;
import com.surelogic.sierra.tool.message.axis.SierraServiceBeanServiceStub.Scan;
import com.surelogic.sierra.tool.message.axis.SierraServiceBeanServiceStub.Scan9;
import com.surelogic.sierra.tool.message.axis.SierraServiceBeanServiceStub.Settings;
import com.surelogic.sierra.tool.message.axis.SierraServiceBeanServiceStub.SettingsReply;
import com.surelogic.sierra.tool.message.axis.SierraServiceBeanServiceStub.SettingsRequest;
import com.surelogic.sierra.tool.message.axis.SierraServiceBeanServiceStub.Severity;
import com.surelogic.sierra.tool.message.axis.SierraServiceBeanServiceStub.SourceLocation;
import com.surelogic.sierra.tool.message.axis.SierraServiceBeanServiceStub.ToolOutput;
import com.surelogic.sierra.tool.message.axis.SierraServiceBeanServiceStub.TrailObsoletion;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.transport.http.HttpTransportProperties;

import java.io.File;

import java.rmi.RemoteException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;

public class Axis2Client implements SierraService {
	// TODO evaluate a good number for this.
	private static final int TIMEOUT = 10 * 60 * 1000;
	private final SierraServiceBeanServiceStub stub;

	public Axis2Client() {
		this(null);
	}

	public Axis2Client(SierraServerLocation server) {
		// set if realm or domain is known
		if (server == null) {
			server = SierraServerLocation.DEFAULT;
		}

		// I need this for BASIC HTTP authenticator for connecting to the
		// WebService
		HttpTransportProperties.Authenticator auth = new HttpTransportProperties.Authenticator();
		auth.setUsername(server.getUser());
		auth.setPassword(server.getPass());

		try {
			stub = new SierraServiceBeanServiceStub(server.createWSDLUrl()
					.toString());

			final Options options = stub._getServiceClient().getOptions();
			options.setProperty(
					org.apache.axis2.transport.http.HTTPConstants.AUTHENTICATE,
					auth);
			options.setProperty(HTTPConstants.SO_TIMEOUT, Integer
					.valueOf(TIMEOUT));
			options.setProperty(HTTPConstants.CONNECTION_TIMEOUT, Integer
					.valueOf(TIMEOUT));
		} catch (AxisFault e) {
			throw wrapException(e);
		}
	}

	public com.surelogic.sierra.tool.message.CommitAuditTrailResponse commitAuditTrails(
			com.surelogic.sierra.tool.message.CommitAuditTrailRequest audits)
			throws com.surelogic.sierra.tool.message.ServerMismatchException {
		try {
			return new CommitAuditTrailResponseConverter().convert(stub
					.commitAuditTrails(new CommitAuditTrailRequestConverter()
							.convert(audits)));
		} catch (RemoteException e) {
			throw wrapException(e);
		} catch (com.surelogic.sierra.tool.message.axis.ServerMismatchException e) {
			throw new com.surelogic.sierra.tool.message.ServerMismatchException(
					e);
		}
	}

	public com.surelogic.sierra.tool.message.AuditTrailResponse getAuditTrails(
			com.surelogic.sierra.tool.message.GetAuditTrailRequest request)
			throws com.surelogic.sierra.tool.message.ServerMismatchException {
		try {
			return new AuditTrailResponseConverter().convert(stub
					.getAuditTrails(new GetAuditTrailsConverter()
							.convert(request)));
		} catch (RemoteException e) {
			throw wrapException(e);
		} catch (com.surelogic.sierra.tool.message.axis.ServerMismatchException e) {
			throw new com.surelogic.sierra.tool.message.ServerMismatchException(
					e);
		}
	}

	public com.surelogic.sierra.tool.message.Qualifiers getQualifiers(
			com.surelogic.sierra.tool.message.QualifierRequest request) {
		try {
			final GetQualifiersResponse response = stub
					.getQualifiers(new GetQualifiers());
			final String[] qualifiers = response.getGetQualifiersResponse()
					.getQualifier();
			final com.surelogic.sierra.tool.message.Qualifiers q = new com.surelogic.sierra.tool.message.Qualifiers();

			if (qualifiers != null) {
				q.setQualifier(Arrays.asList(qualifiers));
			}

			return q;
		} catch (RemoteException e) {
			throw wrapException(e);
		}
	}

	public com.surelogic.sierra.tool.message.SettingsReply getSettings(
			com.surelogic.sierra.tool.message.SettingsRequest request)
			throws com.surelogic.sierra.tool.message.ServerMismatchException {
		try {
			return new SettingsReplyConverter().convert(stub
					.getSettings(new GetSettingsConverter().convert(request)));
		} catch (RemoteException e) {
			throw wrapException(e);
		} catch (com.surelogic.sierra.tool.message.axis.ServerMismatchException e) {
			throw new com.surelogic.sierra.tool.message.ServerMismatchException(
					e);
		}
	}

	public com.surelogic.sierra.tool.message.ServerUIDReply getUid(
			com.surelogic.sierra.tool.message.ServerUIDRequest request) {
		try {
			GetUidResponse uid = stub.getUid(new GetUid());
			com.surelogic.sierra.tool.message.ServerUIDReply reply = new com.surelogic.sierra.tool.message.ServerUIDReply();
			reply.setUid(uid.getGetUidResponse().getUid());

			return reply;
		} catch (RemoteException e) {
			throw wrapException(e);
		}
	}

	public com.surelogic.sierra.tool.message.MergeAuditTrailResponse mergeAuditTrails(
			com.surelogic.sierra.tool.message.MergeAuditTrailRequest seed)
			throws com.surelogic.sierra.tool.message.ServerMismatchException {
		try {
			return new MergeAuditTrailResponseConverter().convert(stub
					.mergeAuditTrails(new MergeAuditTrailsConverter()
							.convert(seed)));
		} catch (RemoteException e) {
			throw wrapException(e);
		} catch (com.surelogic.sierra.tool.message.axis.ServerMismatchException e) {
			throw new com.surelogic.sierra.tool.message.ServerMismatchException(
					e);
		}
	}

	public void publishRun(com.surelogic.sierra.tool.message.Scan scan) {
		try {
			stub.publishRun(new ScanConverter().convert(scan));
		} catch (RemoteException e) {
			throw wrapException(e);
		}
	}

	public com.surelogic.sierra.tool.message.GlobalSettings getGlobalSettings(
			com.surelogic.sierra.tool.message.GlobalSettingsRequest request) {
		try {
			final GetGlobalSettings in = new GetGlobalSettings();
			in.setGetGlobalSettings(new GlobalSettingsRequest());
			return new GlobalSettingsConverter().convert(stub
					.getGlobalSettings(in).getGetGlobalSettingsResponse());
		} catch (RemoteException e) {
			throw wrapException(e);
		}
	}

	public void writeGlobalSettings(
			com.surelogic.sierra.tool.message.GlobalSettings settings) {
		try {
			WriteGlobalSettings out = new WriteGlobalSettings();
			out.setWriteGlobalSettings(new MessageGlobalSettingsConverter()
					.convert(settings));
			stub.writeGlobalSettings(out);
		} catch (RemoteException e) {
			throw wrapException(e);
		}
	}

	/**
	 * Convert the client exception into an exception that can be interpreted by
	 * the client.
	 * 
	 * @param t
	 * @return
	 */
	private static SierraServiceClientException wrapException(Throwable t) {
		if (!(t instanceof AxisFault)) {
			return new SierraServiceClientException(t);
		}

		final AxisFault fault = (AxisFault) t;

		if (fault.getMessage().equals(
				"Transport error: 401 Error: Unauthorized")) {
			return new InvalidLoginException(fault);
		} else {
			return new SierraServiceClientException(fault);
		}
	}

	private static <A, B> B[] collToArray(Collection<A> list, B[] arr,
			Converter<A, B> converter) {
		if (list != null) {
			int i = 0;

			for (A a : list) {
				arr[i++] = converter.convert(a);
			}

			return arr;
		} else {
			return null;
		}
	}

	private static <A, B> List<B> arrayToList(A[] arr, Converter<A, B> converter) {
		if (arr != null) {
			List<B> list = new ArrayList<B>();

			for (A a : arr) {
				list.add(converter.convert(a));
			}

			return list;
		}

		return null;
	}

	private static Calendar fromDate(Date date) {
		if (date != null) {
			final Calendar c = Calendar.getInstance();
			c.setTime(date);

			return c;
		}

		return null;
	}

	private static String fromFile(File file) {
		if (file != null) {
			return file.getPath();
		}

		return null;
	}

	interface Converter<A, B> {
		B convert(A a);
	}

	private static class MessageGlobalSettingsConverter
			implements
			Converter<com.surelogic.sierra.tool.message.GlobalSettings, GlobalSettings> {

		public GlobalSettings convert(
				com.surelogic.sierra.tool.message.GlobalSettings in) {
			GlobalSettings out = new GlobalSettings();
			List<com.surelogic.sierra.tool.message.FindingTypeFilter> inFilters = in
					.getFilter();
			FindingTypeFilter[] outFilters = new FindingTypeFilter[inFilters
					.size()];
			out.setFilter(collToArray(in.getFilter(), outFilters,
					new MessageFindingTypeFilterConverter()));
			return out;
		}

	}

	private static class MessageFindingTypeFilterConverter
			implements
			Converter<com.surelogic.sierra.tool.message.FindingTypeFilter, FindingTypeFilter> {

		public FindingTypeFilter convert(
				com.surelogic.sierra.tool.message.FindingTypeFilter in) {
			FindingTypeFilter out = new FindingTypeFilter();
			com.surelogic.sierra.tool.message.Importance i = in.getImportance();
			if (i != null) {
				out.setImportance(Importance.Factory.fromValue(i.name()));
			}
			Integer delta = in.getDelta();
			if (delta != null) {
				in.setDelta(delta);
			}
			out.setFiltered(in.isFiltered());
			out.setName(in.getName());
			return out;
		}

	}

	private static class GlobalSettingsConverter
			implements
			Converter<GlobalSettings, com.surelogic.sierra.tool.message.GlobalSettings> {

		public com.surelogic.sierra.tool.message.GlobalSettings convert(
				GlobalSettings in) {
			com.surelogic.sierra.tool.message.GlobalSettings out = new com.surelogic.sierra.tool.message.GlobalSettings();
			FindingTypeFilter[] filters = in.getFilter();
			if (filters != null) {
				out.setFilter(arrayToList(filters,
						new FindingTypeFilterConverter()));
			}
			return out;
		}

	}

	private static class FindingTypeFilterConverter
			implements
			Converter<FindingTypeFilter, com.surelogic.sierra.tool.message.FindingTypeFilter> {

		public com.surelogic.sierra.tool.message.FindingTypeFilter convert(
				FindingTypeFilter in) {
			com.surelogic.sierra.tool.message.FindingTypeFilter out = new com.surelogic.sierra.tool.message.FindingTypeFilter();
			Importance i = in.getImportance();
			if (i != null) {
				out.setImportance(com.surelogic.sierra.tool.message.Importance
						.fromValue(in.getImportance().getValue()));
			}
			out.setDelta(in.getDelta());
			out.setFiltered(in.getFiltered());
			out.setName(in.getName());
			return out;
		}
	}

	private static class SettingsReplyConverter
			implements
			Converter<GetSettingsResponse, com.surelogic.sierra.tool.message.SettingsReply> {
		public com.surelogic.sierra.tool.message.SettingsReply convert(
				GetSettingsResponse in) {
			final com.surelogic.sierra.tool.message.SettingsReply out = new com.surelogic.sierra.tool.message.SettingsReply();
			final SettingsReply reply = in.getGetSettingsResponse();
			final Settings[] settings = reply.getSettings();
			final FilterSet[] filterSets = reply.getFilterSets();
			final ProjectSettings[] projectSettings = reply
					.getProjectSettings();

			if (settings != null) {
				out.getSettings().addAll(
						arrayToList(settings, new SettingsConverter()));
			}

			if (projectSettings != null) {
				out.getProjectSettings().addAll(
						arrayToList(projectSettings,
								new ProjectSettingsConverter()));
			}

			if (filterSets != null) {
				out.getFilterSets().addAll(
						arrayToList(filterSets, new FilterSetConverter()));
			}

			return out;
		}
	}

	private static class SettingsConverter implements
			Converter<Settings, com.surelogic.sierra.tool.message.Settings> {
		public com.surelogic.sierra.tool.message.Settings convert(Settings in) {
			final com.surelogic.sierra.tool.message.Settings out = new com.surelogic.sierra.tool.message.Settings();
			final String[] sets = in.getFilterSets();

			if (sets != null) {
				out.getFilterSets().addAll(Arrays.asList(sets));
			}

			out.setName(in.getName());
			out.setRevision(in.getRevision());

			return out;
		}
	}

	private static class ProjectSettingsConverter
			implements
			Converter<ProjectSettings, com.surelogic.sierra.tool.message.ProjectSettings> {
		public com.surelogic.sierra.tool.message.ProjectSettings convert(
				ProjectSettings in) {
			final com.surelogic.sierra.tool.message.ProjectSettings out = new com.surelogic.sierra.tool.message.ProjectSettings();
			out.setName(in.getName());
			out.setProject(in.getProject());

			return out;
		}
	}

	private static class FilterSetConverter implements
			Converter<FilterSet, com.surelogic.sierra.tool.message.FilterSet> {
		public com.surelogic.sierra.tool.message.FilterSet convert(FilterSet in) {
			final com.surelogic.sierra.tool.message.FilterSet out = new com.surelogic.sierra.tool.message.FilterSet();
			out.setName(in.getName());
			out.setUid(in.getUid());

			final FilterEntry[] entries = in.getFilter();

			if (entries != null) {
				out.getFilter().addAll(
						arrayToList(entries, new FilterEntryConverter()));
			}

			return out;
		}
	}

	private static class FilterEntryConverter
			implements
			Converter<FilterEntry, com.surelogic.sierra.tool.message.FilterEntry> {
		public com.surelogic.sierra.tool.message.FilterEntry convert(
				FilterEntry in) {
			final com.surelogic.sierra.tool.message.FilterEntry out = new com.surelogic.sierra.tool.message.FilterEntry();
			out.setFiltered(in.getFiltered());
			out.setType(in.getType());

			return out;
		}
	}

	private static class GetSettingsConverter
			implements
			Converter<com.surelogic.sierra.tool.message.SettingsRequest, GetSettings> {
		public GetSettings convert(
				com.surelogic.sierra.tool.message.SettingsRequest in) {
			final GetSettings out = new GetSettings();
			final SettingsRequest request = new SettingsRequest();
			request.setServer(in.getServer());

			Long revision = in.getRevision();

			if (revision != null) {
				request.setRevision(revision);
			}

			out.setGetSettings(request);

			return out;
		}
	}

	private static class AuditTrailResponseConverter
			implements
			Converter<GetAuditTrailsResponse, com.surelogic.sierra.tool.message.AuditTrailResponse> {
		public com.surelogic.sierra.tool.message.AuditTrailResponse convert(
				GetAuditTrailsResponse in) {
			final com.surelogic.sierra.tool.message.AuditTrailResponse out = new com.surelogic.sierra.tool.message.AuditTrailResponse();
			AuditTrailResponse response = in.getGetAuditTrailsResponse();
			out.setObsolete(arrayToList(response.getObsolete(),
					new ObsoleteConverter()));
			out.setUpdate(arrayToList(response.getUpdate(),
					new AuditTrailUpdateConverter()));

			return out;
		}
	}

	private static class ObsoleteConverter
			implements
			Converter<TrailObsoletion, com.surelogic.sierra.tool.message.TrailObsoletion> {
		public com.surelogic.sierra.tool.message.TrailObsoletion convert(
				TrailObsoletion in) {
			final com.surelogic.sierra.tool.message.TrailObsoletion out = new com.surelogic.sierra.tool.message.TrailObsoletion();
			out.setRevision(in.getRevision());
			out.setObsoletedTrail(in.getObsoletedTrail());
			out.setTrail(in.getTrail());

			return out;
		}
	}

	private static class AuditTrailUpdateConverter
			implements
			Converter<AuditTrailUpdate, com.surelogic.sierra.tool.message.AuditTrailUpdate> {
		public com.surelogic.sierra.tool.message.AuditTrailUpdate convert(
				AuditTrailUpdate in) {
			final com.surelogic.sierra.tool.message.AuditTrailUpdate out = new com.surelogic.sierra.tool.message.AuditTrailUpdate();
			out
					.setAudit(arrayToList(in.getAudit(),
							new MessageAuditConverter()));

			final Importance imp = in.getImportance();

			if (imp != null) {
				out.setImportance(com.surelogic.sierra.tool.message.Importance
						.valueOf(imp.getValue()));
			}

			out
					.setMatch(arrayToList(in.getMatch(),
							new MessageMatchConverter()));
			out.setSummary(in.getSummary());
			out.setTrail(in.getTrail());

			return out;
		}
	}

	private static class MessageAuditConverter implements
			Converter<Audit, com.surelogic.sierra.tool.message.Audit> {
		public com.surelogic.sierra.tool.message.Audit convert(Audit in) {
			final com.surelogic.sierra.tool.message.Audit out = new com.surelogic.sierra.tool.message.Audit();
			out.setEvent(com.surelogic.sierra.tool.message.AuditEvent
					.valueOf(in.getEvent().getValue()));
			out.setRevision(in.getRevision());
			out.setTimestamp(in.getTimestamp().getTime());
			out.setUser(in.getUser());
			out.setValue(in.getValue());

			return out;
		}
	}

	private static class MessageMatchConverter implements
			Converter<Match, com.surelogic.sierra.tool.message.Match> {
		public com.surelogic.sierra.tool.message.Match convert(Match in) {
			final com.surelogic.sierra.tool.message.Match out = new com.surelogic.sierra.tool.message.Match();
			out.setClassName(in.getClassName());
			out.setFindingType(in.getFindingType());
			out.setHash(in.getHash());
			out.setPackageName(in.getPackageName());

			return out;
		}
	}

	private static class GetAuditTrailsConverter
			implements
			Converter<com.surelogic.sierra.tool.message.GetAuditTrailRequest, GetAuditTrails> {
		public GetAuditTrails convert(
				com.surelogic.sierra.tool.message.GetAuditTrailRequest in) {
			final GetAuditTrails out = new GetAuditTrails();
			final GetAuditTrailRequest request = new GetAuditTrailRequest();
			request.setProject(in.getProject());

			final Long revision = in.getRevision();

			if (revision != null) {
				request.setRevision(revision);
			}

			request.setServer(in.getServer());
			out.setGetAuditTrails(request);

			return out;
		}
	}

	private static class MergeAuditTrailResponseConverter
			implements
			Converter<MergeAuditTrailsResponse, com.surelogic.sierra.tool.message.MergeAuditTrailResponse> {
		public com.surelogic.sierra.tool.message.MergeAuditTrailResponse convert(
				MergeAuditTrailsResponse in) {
			final com.surelogic.sierra.tool.message.MergeAuditTrailResponse out = new com.surelogic.sierra.tool.message.MergeAuditTrailResponse();
			final MergeAuditTrailResponse response = in
					.getMergeAuditTrailsResponse();
			out.setRevision(response.getRevision());

			String[] trails = response.getTrail();

			if (trails != null) {
				out.setTrail(Arrays.asList(trails));
			}

			return out;
		}
	}

	private static class MergeAuditTrailsConverter
			implements
			Converter<com.surelogic.sierra.tool.message.MergeAuditTrailRequest, MergeAuditTrails> {
		public MergeAuditTrails convert(
				com.surelogic.sierra.tool.message.MergeAuditTrailRequest in) {
			final MergeAuditTrails out = new MergeAuditTrails();
			final MergeAuditTrailRequest request = new MergeAuditTrailRequest();
			out.setMergeAuditTrails(request);
			request.setProject(in.getProject());
			request.setServer(in.getServer());

			final List<com.surelogic.sierra.tool.message.Merge> merges = in
					.getMerge();

			if (merges != null) {
				request.setMerge(collToArray(merges, new Merge[merges.size()],
						new MergeConverter()));
			}

			return out;
		}
	}

	private static class MergeConverter implements
			Converter<com.surelogic.sierra.tool.message.Merge, Merge> {
		public Merge convert(com.surelogic.sierra.tool.message.Merge in) {
			final Merge out = new Merge();
			out.setImportance(Importance.Factory.fromValue(in.getImportance()
					.name()));
			out.setSummary(in.getSummary());

			final List<com.surelogic.sierra.tool.message.Match> matches = in
					.getMatch();

			if (matches != null) {
				out.setMatch(collToArray(in.getMatch(), new Match[matches
						.size()], new MatchConverter()));
			}

			return out;
		}
	}

	private static class MatchConverter implements
			Converter<com.surelogic.sierra.tool.message.Match, Match> {
		public Match convert(com.surelogic.sierra.tool.message.Match in) {
			final Match out = new Match();
			out.setClassName(in.getClassName());
			out.setFindingType(in.getFindingType());

			Long hash = in.getHash();

			if (hash != null) {
				out.setHash(hash);
			}

			out.setPackageName(in.getPackageName());

			return out;
		}
	}

	private static class ScanConverter implements
			Converter<com.surelogic.sierra.tool.message.Scan, Scan9> {
		public Scan9 convert(com.surelogic.sierra.tool.message.Scan in) {
			final Scan9 out = new Scan9();
			final Scan scan = new Scan();
			scan.setConfig(new ConfigConverter().convert(in.getConfig()));
			scan.setToolOutput(new ToolOutputConverter().convert(in
					.getToolOutput()));
			scan.setUid(in.getUid());
			out.setScan(scan);

			return out;
		}
	}

	private static class ConfigConverter implements
			Converter<com.surelogic.sierra.tool.message.Config, Config> {
		public Config convert(com.surelogic.sierra.tool.message.Config in) {
			final Config out = new Config();
			out.setBaseDirectory(fromFile(in.getBaseDirectory()));
			out.setBinDirs(in.getBinDirs());
			out.setClasspath(in.getClasspath());
			out.setCleanTempFiles(in.isCleanTempFiles());
			out.setDestDirectory(fromFile(in.getDestDirectory()));
			out.setExcludedToolsList(in.getExcludedToolsList());
			out.setJavaVendor(in.getJavaVendor());
			out.setJavaVersion(in.getJavaVersion());
			out.setMultithreaded(in.isMultithreaded());
			out.setPmdRulesFile(fromFile(in.getPmdRulesFile()));
			out.setProject(in.getProject());

			List<String> qualifiers = in.getQualifiers();

			if (qualifiers != null) {
				out.setQualifiers(qualifiers.toArray(new String[qualifiers
						.size()]));
			}

			out.setRunDateTime(fromDate(in.getRunDateTime()));
			out.setScanDocument(fromFile(in.getScanDocument()));
			out.setSourceDirs(in.getSourceDirs());
			out.setToolsDirectory(fromFile(in.getToolsDirectory()));

			return out;
		}
	}

	private static class ToolOutputConverter implements
			Converter<com.surelogic.sierra.tool.message.ToolOutput, ToolOutput> {
		public ToolOutput convert(
				com.surelogic.sierra.tool.message.ToolOutput in) {
			final ToolOutput out = new ToolOutput();
			final Artifacts a = new Artifacts();
			final Errors e = new Errors();
			final Metrics m = new Metrics();

			if (in.getArtifacts() != null) {
				final Collection<com.surelogic.sierra.tool.message.Artifact> inA = in
						.getArtifacts().getArtifact();

				if (inA != null) {
					a.setArtifact(collToArray(inA, new Artifact[inA.size()],
							new ArtifactConverter()));
				}
			}

			if (in.getErrors() != null) {
				final Collection<com.surelogic.sierra.tool.message.Error> inE = in
						.getErrors().getErrors();

				if (inE != null) {
					e.setErrors(collToArray(inE, new Error[inE.size()],
							new ErrorConverter()));
				}
			}

			if (in.getMetrics() != null) {
				final Collection<com.surelogic.sierra.tool.message.ClassMetric> inM = in
						.getMetrics().getClassMetric();

				if (inM != null) {
					m.set_class(collToArray(inM, new ClassMetric[inM.size()],
							new ClassMetricConverter()));
				}
			}

			out.setArtifacts(a);
			out.setErrors(e);
			out.setMetrics(m);

			return out;
		}
	}

	private static class ErrorConverter implements
			Converter<com.surelogic.sierra.tool.message.Error, Error> {
		public Error convert(com.surelogic.sierra.tool.message.Error in) {
			final Error out = new Error();
			out.setMessage(in.getMessage());
			out.setTool(in.getTool());

			return out;
		}
	}

	private static class ClassMetricConverter
			implements
			Converter<com.surelogic.sierra.tool.message.ClassMetric, ClassMetric> {
		public ClassMetric convert(
				com.surelogic.sierra.tool.message.ClassMetric in) {
			final ClassMetric out = new ClassMetric();
			out.set_package(in.getPackage());
			out.setLoc(in.getLoc());
			out.setName(in.getName());

			return out;
		}
	}

	private static class ArtifactConverter implements
			Converter<com.surelogic.sierra.tool.message.Artifact, Artifact> {
		public Artifact convert(com.surelogic.sierra.tool.message.Artifact in) {
			final Artifact out = new Artifact();
			final List<com.surelogic.sierra.tool.message.SourceLocation> additional = in
					.getAdditionalSources();

			if (additional != null) {
				out.setAdditionalSources(collToArray(additional,
						new SourceLocation[additional.size()],
						new SourceLocationConverter()));
			}

			out.setArtifactType(new ArtifactTypeConverter().convert(in
					.getArtifactType()));
			out.setMessage(in.getMessage());
			out.setPrimarySourceLocation(new SourceLocationConverter()
					.convert(in.getPrimarySourceLocation()));
			out
					.setPriority(Priority.Factory.fromValue(in.getPriority()
							.name()));
			out
					.setSeverity(Severity.Factory.fromValue(in.getSeverity()
							.name()));

			return out;
		}
	}

	private static class ArtifactTypeConverter
			implements
			Converter<com.surelogic.sierra.tool.message.ArtifactType, ArtifactType> {
		public ArtifactType convert(
				com.surelogic.sierra.tool.message.ArtifactType in) {
			final ArtifactType out = new ArtifactType();
			out.setMnemonic(in.getMnemonic());
			out.setTool(in.getTool());
			out.setVersion(in.getVersion());

			return out;
		}
	}

	private static class SourceLocationConverter
			implements
			Converter<com.surelogic.sierra.tool.message.SourceLocation, SourceLocation> {
		public SourceLocation convert(
				com.surelogic.sierra.tool.message.SourceLocation in) {
			final SourceLocation out = new SourceLocation();
			out.setClassName(in.getClassName());
			out.setCompilation(in.getCompilation());
			out.setEndLineOfCode(in.getEndLineOfCode());

			final Long hash = in.getHash();

			if (hash != null) {
				out.setHash(in.getHash());
			}

			out.setIdentifier(in.getIdentifier());

			final com.surelogic.sierra.tool.message.IdentifierType type = in
					.getIdentifierType();

			if (type != null) {
				out.setIdentifierType(IdentifierType.Factory.fromValue(type
						.name()));
			}

			out.setLineOfCode(in.getLineOfCode());
			out.setPackageName(in.getPackageName());
			out.setPathName(in.getPathName());

			return out;
		}
	}

	private static class CommitAuditTrailResponseConverter
			implements
			Converter<CommitAuditTrailsResponse, com.surelogic.sierra.tool.message.CommitAuditTrailResponse> {
		public com.surelogic.sierra.tool.message.CommitAuditTrailResponse convert(
				CommitAuditTrailsResponse commit) {
			final CommitAuditTrailResponse old = commit
					.getCommitAuditTrailsResponse();
			final com.surelogic.sierra.tool.message.CommitAuditTrailResponse response = new com.surelogic.sierra.tool.message.CommitAuditTrailResponse();
			final long revision = old.getRevision();
			final String[] uids = old.getUid();
			response.setRevision(revision);

			if (uids != null) {
				response.setUid(Arrays.asList(uids));
			}

			return response;
		}
	}

	private static class CommitAuditTrailRequestConverter
			implements
			Converter<com.surelogic.sierra.tool.message.CommitAuditTrailRequest, CommitAuditTrails> {
		public CommitAuditTrails convert(
				com.surelogic.sierra.tool.message.CommitAuditTrailRequest in) {
			final SierraServiceBeanServiceStub.CommitAuditTrails out = new SierraServiceBeanServiceStub.CommitAuditTrails();
			final SierraServiceBeanServiceStub.CommitAuditTrailRequest request = new SierraServiceBeanServiceStub.CommitAuditTrailRequest();
			out.setCommitAuditTrails(request);
			request.setServer(in.getServer());

			final List<com.surelogic.sierra.tool.message.AuditTrail> auditTrail = in
					.getAuditTrail();

			if (auditTrail != null) {
				request.setAuditTrail(collToArray(auditTrail,
						new SierraServiceBeanServiceStub.AuditTrail[auditTrail
								.size()], new AuditTrailConverter()));
			}

			return out;
		}
	}

	private static class AuditTrailConverter implements
			Converter<com.surelogic.sierra.tool.message.AuditTrail, AuditTrail> {
		public AuditTrail convert(
				com.surelogic.sierra.tool.message.AuditTrail in) {
			final SierraServiceBeanServiceStub.AuditTrail trail = new SierraServiceBeanServiceStub.AuditTrail();
			trail.setFinding(in.getFinding());

			final List<com.surelogic.sierra.tool.message.Audit> oldAudits = in
					.getAudits();

			if (oldAudits != null) {
				trail.setAudits(collToArray(oldAudits, new Audit[oldAudits
						.size()], new AuditConverter()));
			}

			return trail;
		}
	}

	private static class AuditConverter
			implements
			Converter<com.surelogic.sierra.tool.message.Audit, SierraServiceBeanServiceStub.Audit> {
		public Audit convert(com.surelogic.sierra.tool.message.Audit a) {
			final Audit out = new Audit();
			out.setEvent(AuditEvent.Factory.fromValue(a.getEvent().name()));

			final Long revision = a.getRevision();

			if (revision != null) {
				out.setRevision(revision);
			}

			out.setTimestamp(fromDate(a.getTimestamp()));
			out.setUser(a.getUser());
			out.setValue(a.getValue());

			return out;
		}
	}
}
