package com.surelogic.sierra.client.eclipse.jsure;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.ui.ISharedImages;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.progress.UIJob;

import com.surelogic.common.eclipse.*;
import com.surelogic.common.eclipse.jobs.DatabaseJob;
import com.surelogic.common.eclipse.jobs.SLUIJob;
import com.surelogic.common.eclipse.logging.SLStatus;
import com.surelogic.common.i18n.I18N;
import com.surelogic.common.images.CommonImages;
import com.surelogic.common.jdbc.*;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.client.eclipse.*;
import com.surelogic.sierra.client.eclipse.model.*;
import com.surelogic.sierra.client.eclipse.views.AbstractSierraViewMediator;
import com.surelogic.sierra.client.eclipse.views.IViewUpdater;
import com.surelogic.sierra.client.eclipse.views.selection.FindingsSelectionView;
import com.surelogic.sierra.jdbc.finding.*;
import com.surelogic.sierra.tool.message.Importance;

public class JSureFindingDetailsMediator extends AbstractSierraViewMediator
implements IViewUpdater {
  private static final int VIEW_DEPENDENT_ON_THIS = 0;
  private static final int VIEW_OWN_DEPENDENCIES = 1;
  
	private final RGB f_BackgroundColorRGB;

	private final Composite[] f_parents;
	private final TreeViewer[] f_viewers;
	
	private volatile FindingDetail f_finding;
	private volatile FindingRelationOverview f_relatedChildren;
	private volatile FindingRelationOverview f_relatedAncestors;

	public JSureFindingDetailsMediator(JSureFindingDetailsView view, Composite[] parents, TreeViewer[] viewers) {
		super(view);
		f_parents = parents;
		f_viewers = viewers;

		f_BackgroundColorRGB = parents[0].getDisplay().getSystemColor(
				SWT.COLOR_LIST_BACKGROUND).getRGB();
	}

	public String getHelpId() {
	  // TODO
		return "com.surelogic.sierra.client.eclipse.view-finding-details";
	}

	public String getNoDataI18N() {
	  // TODO
		return "sierra.eclipse.noDataFindingDetails";
	}
	@Override
	public Listener getNoDataListener() {
		return new Listener() {
			public void handleEvent(Event event) {
				ViewUtility.showView(FindingsSelectionView.ID);
			}
		};
	}
	
	void asyncQueryAndShow(final FindingDetail detail) {
		final Long findingIdObj = detail.getFindingId();
		f_finding = detail;
	  
		final Job job = new DatabaseJob("Querying JSure details of finding " + findingIdObj) {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				monitor.beginTask("Querying finding data",
						IProgressMonitor.UNKNOWN);
				try {
					Connection c = Data.readOnlyConnection();
					try {
						Query q = new ConnectionQuery(c);
						f_relatedChildren  = FindingRelationOverview.getOverviewOrNull(q, findingIdObj, true);
						f_relatedAncestors = FindingRelationOverview.getOverviewOrNull(q, findingIdObj, false);
						
						// got details, update the view in the UI thread
						asyncUpdateContentsForUI(JSureFindingDetailsMediator.this);							  
					} finally {
						if (c != null) {
							c.close();
						}
					}
					monitor.done();
					return Status.OK_STATUS;
				} catch (SQLException e) {
					final int errNo = 57;
					final String msg = I18N.err(errNo, findingIdObj);
					return SLStatus.createErrorStatus(errNo, msg, e);
				}
			}
		};
		job.schedule();
	}

	/*
	private final Listener f_radioListener = new Listener() {
		public void handleEvent(Event event) {
			final Importance current = f_finding.getImportance();
			if (event.widget != null) {
				if (event.widget.getData() instanceof Importance) {
					final Importance desired = (Importance) event.widget
							.getData();
					if (desired != current) {
						FindingMutationUtility.asyncChangeImportance(f_finding
								.getFindingId(), current, desired);
					}
				}
			}
		}
	};

	private final Listener f_locationListener = new Listener() {
		public void handleEvent(Event event) {
			final TreeItem item = (TreeItem) event.item;
			if (item == null) {
				return;
			}
			final SourceDetail src = (SourceDetail) item.getData();
			if (src == null)
				return;

			JDTUtility.tryToOpenInEditor(f_finding.getProjectName(), src
					.getPackageName(), src.getClassName(), src.getLineOfCode());
		}
	};
	 */

	@Override
	public void init() {
		super.init();

		updateContentsForUI();
	}
	@Override
	public void dispose() {
		super.dispose();
	}

	public void setFocus() {
	  // TODO
	}

	/**
	 * Must be invoked from the SWT thread.
	 */
	public void updateContentsForUI() {
		final boolean showFinding = f_finding != null;

		// Page doesn't match our state
		if (!f_view.matchesStatus(showFinding)) {
			f_view.hasData(showFinding);
		}
		if (!showFinding)
			return;

		/*
		 * We have a finding so show the details about it.
		 */
		/*
		f_summaryIcon.setImage(Utility.getImageFor(f_finding.getImportance()));
		f_summaryIcon.setToolTipText("The importance of this finding is "
				+ f_finding.getImportance().toStringSentenceCase());
		f_summaryText.setText(f_finding.getSummary());

		for (MenuItem i : f_importanceRadioPopupMenu.getItems()) {
			i.setSelection(f_finding.getImportance() == i.getData());
		}

		f_findingSynopsis.setText(getFindingSynopsis());

		initLocationTree(f_locationTree, f_finding);

		StringBuffer b = new StringBuffer();
		HTMLPrinter.insertPageProlog(b, 0, f_BackgroundColorRGB,
				StyleSheetHelper.getInstance().getStyleSheet());
		String details = f_finding.getFindingTypeDetail();
		b.append(details);
*/		
		//f_parent.layout(true, true);
	}
}
