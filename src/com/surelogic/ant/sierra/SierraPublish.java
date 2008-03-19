/*
 * Created on Jan 17, 2008
 */
package com.surelogic.ant.sierra;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.tools.ant.*;

import com.surelogic.sierra.tool.SierraToolConstants;
import com.surelogic.sierra.tool.message.*;


public class SierraPublish extends Task {
	/***************************************************************************
	 * Ant Task Attributes
	 **************************************************************************/

	// URL used to send the scan document will be sent to this server
	private String server = null;

	private String user;

	private String password;

	private final List<String> timeseries = new ArrayList<String>();
	
	private String document;

	private static boolean notEmpty(String s) {
		return s != null && !"".equals(s);
	}
	
	@Override
    public void execute() throws BuildException
    {
		if (notEmpty(getServer()) && notEmpty(getUser()) && getPassword() != null) {
			File doc = new File(getDocument() + SierraToolConstants.PARSED_FILE_SUFFIX);
			if (doc.exists()) {
				uploadRunDocument(doc);
			} 
		}
		StringBuilder sb = new StringBuilder("Bad argument to SierraPublish\n");
		sb.append("server = '").append(getServer()).append("'\n");
		sb.append("user = '").append(getUser()).append("'\n");
		sb.append("timeSeries = '");
		boolean first = true;
		for (String series : getTimeseries()) {
			if (first) {
				first = false;
			} else {
				sb.append(", ");
			}
			sb.append(series);
		}
		sb.append("'\n");
		sb.append("document = '").append(getDocument()).append("'\n");
		throw new BuildException(sb.toString());
    }
	
	/**
	 * Modified from SierraAnalysis.uploadRunDocument()
	 * 
	 * Optional action. Uploads the generated scan document to the desired
	 * server.
	 * 
	 * @param config
	 */
	private void uploadRunDocument(final File scanDoc) {
		log("Uploading the Run document to " + getServer()
				+ "...", org.apache.tools.ant.Project.MSG_INFO);
		MessageWarehouse warehouse = MessageWarehouse.getInstance();
		Scan run;
		try {
			run = warehouse.fetchScan(scanDoc, true);

			SierraServerLocation location = 
				new SierraServerLocation(getServer(), getUser(), getPassword());

			SierraService ts = SierraServiceClient.create(location);

			if (getTimeseries().isEmpty()) {
				// Use server default
				run.getConfig().setTimeseries(null); 
			} else {
				// Verify the timeseries
				List<String> list = ts.getTimeseries(new TimeseriesRequest())
				.getTimeseries();
				if (list == null || list.isEmpty()) {
					throw new BuildException(
					"The target build server does not have any valid timeseries to publish to.");
				}
				if (!list.containsAll(getTimeseries())) {
					StringBuilder sb = new StringBuilder();
					sb.append("Invalid timeseries. Valid timeseries are:\n");
					for (String string : list) {
						sb.append(string);
						sb.append("\n");
					}
					throw new BuildException(sb.toString());
				}
				run.getConfig().setTimeseries(getTimeseries());
			}
			// FIXME utilize the return value once Bug 867 is resolved
			ts.publishRun(run);
		} catch (ScanVersionException e) {
			throw new IllegalStateException(scanDoc
					+ " is not the same version as the server.", e);
		}
	}
	
	/***************************************************************************
	 * Getters and Setters for attributes
	 **************************************************************************/

	public void setServer(String server) {
		this.server = server;
	}

	public String getServer() {
		return server;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * @return the server timeseries.
	 */
	public final List<String> getTimeseries() {
		return timeseries;
	}

	/**
	 * @param timeseries
	 *            the server timeseries to set.
	 */
	public final void setTimeseries(String timeseries) {
		this.timeseries.clear();
		
		String[] strings = timeseries.split(",");
		for (String timeseriesName : strings) {
			String trimmed = timeseriesName.trim();
			if (!"".equals(trimmed)) {
				this.timeseries.add(trimmed);
			}
		}
	}
	
	public String getDocument() {
		return document;
	}

	public void setDocument(String doc) {
		this.document = doc;
	}
}
