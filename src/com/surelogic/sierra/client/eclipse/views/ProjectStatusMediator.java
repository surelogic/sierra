package com.surelogic.sierra.client.eclipse.views;

import java.io.File;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.ide.IDE;

import com.surelogic.common.eclipse.SLImages;
import com.surelogic.common.eclipse.jobs.DatabaseJob;
import com.surelogic.common.eclipse.logging.SLStatus;
import com.surelogic.common.i18n.I18N;
import com.surelogic.sierra.client.eclipse.Data;
import com.surelogic.sierra.client.eclipse.actions.NewScan;
import com.surelogic.sierra.client.eclipse.actions.NewScanDialogAction;
import com.surelogic.sierra.client.eclipse.model.*;
import com.surelogic.sierra.jdbc.finding.ClientFindingManager;
import com.surelogic.sierra.jdbc.finding.FindingAudits;

public final class ProjectStatusMediator extends AbstractSierraViewMediator {
	private final Tree f_statusTree;
    private List<ProjectStatus> projects = Collections.emptyList();
	
	public ProjectStatusMediator(IViewCallback cb, Tree statusTree) {
		super(cb);
		f_statusTree = statusTree;
	}

	public String getHelpId() {
		return "com.surelogic.sierra.client.eclipse.view-project-status";
	}

	public String getNoDataId() {
		return "sierra.eclipse.noDataProjectStatus";
	}
	
	@Override
	public Listener getNoDataListener() {
		return new Listener() {
			public void handleEvent(Event event) {
				new NewScanDialogAction().run(null);
			}
		};
	}
	
	@Override 
	public void init() {
		super.init();
		/*
		f_statusTree.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				updateEventTable();
			}
		});
		*/
		asyncUpdateContents();
	}

	public void setFocus() {
		f_statusTree.setFocus();
	}

	@Override
	public void changed() {
		asyncUpdateContents();
	}

	private void asyncUpdateContents() {
		final Job job = new DatabaseJob(
				"Updating project status") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				monitor.beginTask("Updating list", IProgressMonitor.UNKNOWN);
				try {
					updateContents();
				} catch (Exception e) {
					final int errNo = 58; // FIX
					final String msg = I18N.err(errNo);
					return SLStatus.createErrorStatus(errNo, msg, e);
				}
				monitor.done();
				return Status.OK_STATUS;
			}
		};
		job.schedule();
		
	}

	private void updateContents() throws Exception {
		Connection c = Data.transactionConnection();
		Exception exc = null;
		try {			
			ClientFindingManager cfm = ClientFindingManager.getInstance(c);
			final List<ProjectStatus> projects = new ArrayList<ProjectStatus>();
			for(String name : Projects.getInstance().getProjectNames()) {				
				// Check for new local audits
				List<FindingAudits> findings = cfm.getNewLocalAudits(name); 
				// FIX Check for new remote audits
				
				// FIX Check for a full scan (later than what's on the server?)
				final File scan = NewScan.getScanDocumentFile(name);
				ProjectStatus s = new ProjectStatus(name, scan, findings);
				projects.add(s);
			}
			asyncUpdateContentsForUI(new IViewUpdater() {
				public void updateContentsForUI() {
					updateContentsInUI(projects);
				}
			});
			c.commit();
		} catch (Exception e) {
			c.rollback();
			exc = e;
		} finally {
			try {
				c.close();
			} finally {
				if (exc != null) {
					throw exc;
				}
			}
		}
	}

	public void updateContentsInUI(List<ProjectStatus> projects) {
		// No need to synchronize since only updated/viewed in UI thread?
		this.projects = projects;
		
		final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd 'at' HH:mm:ss");
		f_statusTree.removeAll();

		
		boolean noProjects = projects.isEmpty();
		f_statusTree.setVisible(!noProjects);
		f_view.hasData(!noProjects);
		
		for (ProjectStatus ps : projects) {
			final TreeItem root = new TreeItem(f_statusTree, SWT.NONE);
			final SierraServer server = 
				SierraServerManager.getInstance().getServer(ps.name);
			if (server != null) {
				root.setText(ps.name+" ["+server.getLabel()+']');
			} else {
				root.setText(ps.name);
			}			
			root.setImage(SLImages
					.getWorkbenchImage(IDE.SharedImages.IMG_OBJ_PROJECT));
			root.setData(ps);
			root.setExpanded(true);
			
			if (ps.scanDoc.exists()) {
				TreeItem scan = new TreeItem(root, SWT.NONE);
				Date modified = new Date(ps.scanDoc.lastModified());
				scan.setText("Last full scan on "+dateFormat.format(modified));
				scan.setImage(SLImages.getImage(SLImages.IMG_SIERRA_SCAN));
				scan.setData(ps.scanDoc);
			}
			if (!ps.findings.isEmpty()) {
				TreeItem audits = new TreeItem(root, SWT.NONE);
				audits.setText(ps.numAudits+" audit(s) on "+ps.findings.size()+" finding(s)");
				audits.setImage(SLImages.getImage(SLImages.IMG_SIERRA_STAMP));
				audits.setData(ps.findings);
				
				if (ps.earliestAudit != null) {
					TreeItem earliest = new TreeItem(audits, SWT.NONE);
					earliest.setText("Earliest on "+dateFormat.format(ps.latestAudit));
				}
				if (ps.latestAudit != null && ps.earliestAudit != ps.latestAudit) {
					TreeItem latest = new TreeItem(audits, SWT.NONE);
					latest.setText("Latest on "+dateFormat.format(ps.latestAudit));
				}
			}
		}
		f_statusTree.getParent().layout();
		for(TreeItem item : f_statusTree.getItems()) {
			item.setExpanded(true);
		}
	}
}
