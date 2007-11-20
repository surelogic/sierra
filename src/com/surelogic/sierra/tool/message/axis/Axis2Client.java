package com.surelogic.sierra.tool.message.axis;

import java.io.File;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.transport.http.HttpTransportProperties;

import com.surelogic.sierra.tool.message.SierraServerLocation;
import com.surelogic.sierra.tool.message.SierraService;
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
import com.surelogic.sierra.tool.message.axis.SierraServiceBeanServiceStub.FindingTypeFilter;
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
import com.surelogic.sierra.tool.message.axis.SierraServiceBeanServiceStub.Scan;
import com.surelogic.sierra.tool.message.axis.SierraServiceBeanServiceStub.Scan8;
import com.surelogic.sierra.tool.message.axis.SierraServiceBeanServiceStub.Settings;
import com.surelogic.sierra.tool.message.axis.SierraServiceBeanServiceStub.SettingsReply;
import com.surelogic.sierra.tool.message.axis.SierraServiceBeanServiceStub.SettingsRequest;
import com.surelogic.sierra.tool.message.axis.SierraServiceBeanServiceStub.Severity;
import com.surelogic.sierra.tool.message.axis.SierraServiceBeanServiceStub.SourceLocation;
import com.surelogic.sierra.tool.message.axis.SierraServiceBeanServiceStub.ToolOutput;
import com.surelogic.sierra.tool.message.axis.SierraServiceBeanServiceStub.TrailObsoletion;

public class Axis2Client implements SierraService {

	// TODO evaluate a good number for this.
	private static final int TIMEOUT = 300000;

	private final SierraServiceBeanServiceStub stub;

	public Axis2Client() {
		this(null);
	}

	public Axis2Client(SierraServerLocation server) {
		// I need this for BASIC HTTP authenticator for connecting to the
		// WebService
		HttpTransportProperties.Authenticator auth = new HttpTransportProperties.Authenticator();
		auth.setUsername(server.getUser());
		auth.setPassword(server.getPass());
		// set if realm or domain is known
		if (server == null) {
			server = SierraServerLocation.DEFAULT;
		}
		try {
			stub = new SierraServiceBeanServiceStub(server.createWSDLUrl()
					.toString());
			final Options options = stub._getServiceClient().getOptions();
			options.setProperty(
					org.apache.axis2.transport.http.HTTPConstants.AUTHENTICATE,
					auth);
			options.setProperty(HTTPConstants.SO_TIMEOUT, new Integer(TIMEOUT));
			options.setProperty(HTTPConstants.CONNECTION_TIMEOUT, new Integer(
					TIMEOUT));
		} catch (AxisFault e) {
			throw new ClientException(e);
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
			throw new ClientException(e);
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
			throw new ClientException(e);
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
			throw new ClientException(e);
		}
	}

	public com.surelogic.sierra.tool.message.SettingsReply getSettings(
			com.surelogic.sierra.tool.message.SettingsRequest request)
			throws com.surelogic.sierra.tool.message.ServerMismatchException {
		try {
			return new SettingsReplyConverter().convert(stub
					.getSettings(new GetSettingsConverter().convert(request)));
		} catch (RemoteException e) {
			throw new ClientException(e);
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
			throw new ClientException(e);
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
			throw new ClientException(e);
		} catch (com.surelogic.sierra.tool.message.axis.ServerMismatchException e) {
			throw new com.surelogic.sierra.tool.message.ServerMismatchException(
					e);
		}

	}

	public void publishRun(com.surelogic.sierra.tool.message.Scan scan) {
		try {
			stub.publishRun(new ScanConverter().convert(scan));
		} catch (RemoteException e) {
			throw new ClientException(e);
		}
	}

	private static class SettingsReplyConverter
			implements
			Converter<GetSettingsResponse, com.surelogic.sierra.tool.message.SettingsReply> {

		public com.surelogic.sierra.tool.message.SettingsReply convert(
				GetSettingsResponse in) {
			final com.surelogic.sierra.tool.message.SettingsReply out = new com.surelogic.sierra.tool.message.SettingsReply();
			final SettingsReply reply = in.getGetSettingsResponse();
			out.setRevision(reply.getRevision());
			out.setSettings(new SettingsConverter()
					.convert(reply.getSettings()));
			return out;
		}
	}

	private static class SettingsConverter implements
			Converter<Settings, com.surelogic.sierra.tool.message.Settings> {

		public com.surelogic.sierra.tool.message.Settings convert(Settings in) {
			final com.surelogic.sierra.tool.message.Settings out = new com.surelogic.sierra.tool.message.Settings();
			final List<com.surelogic.sierra.tool.message.FindingTypeFilter> filters = out
					.getFilter();
			final FindingTypeFilter[] inFilters = in.getFilter();
			if (inFilters != null) {
				filters.addAll(arrayToList(inFilters,
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
			final com.surelogic.sierra.tool.message.FindingTypeFilter out = new com.surelogic.sierra.tool.message.FindingTypeFilter();
			out.setDelta(in.getDelta());
			out.setFiltered(in.getFiltered());
			Importance imp = in.getImportance();
			if (imp != null) {
				out.setImportance(com.surelogic.sierra.tool.message.Importance
						.valueOf(imp.getValue()));
			}
			out.setName(in.getName());
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
			request.setProject(in.getProject());
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
			out.setTrail(Arrays.asList(response.getTrail()));
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
			Converter<com.surelogic.sierra.tool.message.Scan, Scan8> {

		public Scan8 convert(com.surelogic.sierra.tool.message.Scan in) {
			final Scan8 out = new Scan8();
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
			final Collection<com.surelogic.sierra.tool.message.Artifact> inA = in
					.getArtifacts().getArtifact();
			if (inA != null) {
				a.setArtifact(collToArray(inA, new Artifact[inA.size()],
						new ArtifactConverter()));
			}
			final Collection<com.surelogic.sierra.tool.message.Error> inE = in
					.getErrors().getErrors();
			if (inE != null) {
				e.setErrors(collToArray(inE, new Error[inE.size()],
						new ErrorConverter()));
			}
			final Collection<com.surelogic.sierra.tool.message.ClassMetric> inM = in
					.getMetrics().getClassMetric();
			if (inM != null) {
				m.set_class(collToArray(inM, new ClassMetric[inM.size()],
						new ClassMetricConverter()));
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

	interface Converter<A, B> {
		B convert(A a);
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

	public static class ClientException extends RuntimeException {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1505452896619669024L;

		public ClientException() {
			super();
		}

		public ClientException(String message, Throwable cause) {
			super(message, cause);
		}

		public ClientException(String message) {
			super(message);
		}

		public ClientException(Throwable cause) {
			super(cause);
		}

	}

}
