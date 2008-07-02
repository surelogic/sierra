package com.surelogic.sierra.jdbc.scan;

import java.util.List;

import com.surelogic.common.jdbc.Query;
import com.surelogic.common.jdbc.Row;
import com.surelogic.common.jdbc.RowHandler;
import com.surelogic.common.jdbc.SingleRowHandler;

/**
 * This class implements data access logic for scans in the database.
 * 
 * @author nathan
 * 
 */
public final class Scans {

	private final Query q;

	public Scans(final Query q) {
		this.q = q;
	}

	/**
	 * Return information about all scans residing in the database for a
	 * particular project.
	 * 
	 * @param project
	 * @return a {@link List} of {@link ScanInfo} objects. May be empty.
	 */
	public List<ScanInfo> getScanInfo(final String project) {
		if (project == null) {
			throw new IllegalArgumentException("Project may not be null.");
		}
		return q.prepared("Scans.projectScans", new ScanInfoHandler()).call(
				project);
	}

	/**
	 * Return information about the latest scan on a project currently residing
	 * in the database.
	 * 
	 * @param project
	 * @return a {@link ScanInfo} object, or <code>null</code> if none exists
	 */
	public ScanInfo getLatestScanInfo(final String project) {
		return q.prepared("Scans.latestProjectScan",
				SingleRowHandler.from(new ScanInfoHandler())).call(project);
	}

	private static class ScanInfoHandler implements RowHandler<ScanInfo> {

		public ScanInfo handle(final Row r) {
			return new ScanInfo(r.nextString(), r.nextString(), r.nextString(),
					r.nextString(), r.nextString(), r.nextDate(), ScanStatus
							.valueOf(r.nextString()), r.nextBoolean());
		}

	}
}
