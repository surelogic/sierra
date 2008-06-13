package com.surelogic.sierra.client.eclipse.jsure;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;

import com.surelogic.common.eclipse.*;
import com.surelogic.common.eclipse.jobs.DatabaseJob;
import com.surelogic.common.eclipse.logging.SLStatus;
import com.surelogic.common.i18n.I18N;
import com.surelogic.common.jdbc.*;
import com.surelogic.sierra.client.eclipse.*;
import com.surelogic.sierra.client.eclipse.views.AbstractSierraViewMediator;
import com.surelogic.sierra.client.eclipse.views.FindingDetailsView;
import com.surelogic.sierra.client.eclipse.views.IViewUpdater;
import com.surelogic.sierra.client.eclipse.views.selection.FindingsSelectionView;
import com.surelogic.sierra.jdbc.finding.*;

public class JSureFindingDetailsMediator extends AbstractSierraViewMediator
implements IViewUpdater {
	private static final int VIEW_OWN_DEPENDENCIES = 0;
	private static final int VIEW_DEPENDENT_ON_THIS = 1;
  
	private final RGB f_BackgroundColorRGB;

	private final Composite[] f_parents;
	private final Label[] f_labels;
	private final TreeViewer[] f_viewers;
	
	private volatile FindingDetail f_finding;
	private FindingRelationOverview f_relatedChildren;
	private FindingRelationOverview f_relatedAncestors;
	private final Map<Long,FindingDetail> details = new HashMap<Long,FindingDetail>();
	
	private volatile FindingRelation lastSelected;

	public JSureFindingDetailsMediator(final JSureFindingDetailsView view, 
			                           Composite[] parents, Label[] labels, TreeViewer[] viewers) {
		super(view);
		f_parents = parents;
		f_labels  = labels;
		f_viewers = viewers;
		
		int i=0;
		for(TreeViewer v : viewers) {
			final boolean lookAtChildren = (i == 0);
			Provider p = new Provider(lookAtChildren);
			v.setLabelProvider(p);
			v.setContentProvider(p);
			v.addSelectionChangedListener(new ISelectionChangedListener() {
				public void selectionChanged(SelectionChangedEvent event) {
					IStructuredSelection selection;
					if (event.getSelection() instanceof IStructuredSelection) {
						selection = (IStructuredSelection) event.getSelection();
					} else {
						return;
					}
					if (!selection.isEmpty()) {
						lastSelected = (FindingRelation) selection.getFirstElement();
					}
				}
			});
			v.getTree().addListener(SWT.MouseDoubleClick, new Listener() {
				public void handleEvent(Event event) {
					final FindingRelation fr = lastSelected;
					if (fr != null) {
						Long findingID = lookAtChildren ? fr.getChildId() : fr.getParentId();
						FindingDetail data = details.get(findingID);					
						JDTUtility.tryToOpenInEditor(data.getProjectName(), 
								data.getPackageName(), data.getClassName(), data.getLineOfCode());
						
						FindingDetailsView.findingSelected(findingID, false);
					}
				}
			});
			i++;
		}
		
		final Action children = new Action("Dependencies", SWT.PUSH) {
			@Override
			public void run() {
				view.setDataPage(VIEW_OWN_DEPENDENCIES);
			}
		};
		view.addToActionBar(children);
		view.addToActionBar(new Action("Dependents", SWT.PUSH) {
			@Override
			public void run() {
				view.setDataPage(VIEW_DEPENDENT_ON_THIS);
			}
		});		
		
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
						synchronized (details) {
							details.clear();
							details.put(findingIdObj, detail);
							f_relatedChildren  = FindingRelationOverview.getOverviewOrNull(q, findingIdObj, true);
							f_relatedAncestors = FindingRelationOverview.getOverviewOrNull(q, findingIdObj, false);
							for(FindingRelation r : f_relatedChildren.getRelations()) {
								Long id = r.getChildId();
								FindingDetail finding = FindingDetail.getDetailOrNull(c, id);
								details.put(id, finding);
							}
							for(FindingRelation r : f_relatedAncestors.getRelations()) {
								Long id = r.getParentId();
								FindingDetail finding = FindingDetail.getDetailOrNull(c, id);
								details.put(id, finding);
							}
							f_relatedChildren.sort(new Comparator<FindingRelation>() {
								public int compare(FindingRelation o1, FindingRelation o2) {
									return getLabel(o1, true).compareTo(getLabel(o2, true));
								}								
							});
							f_relatedAncestors.sort(new Comparator<FindingRelation>() {
								public int compare(FindingRelation o1, FindingRelation o2) {
									return getLabel(o1, false).compareTo(getLabel(o2, false));
								}								
							});
						}												
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
		
		f_labels[VIEW_OWN_DEPENDENCIES].setText("Dependencies for "+f_finding.getSummary());
		f_viewers[VIEW_OWN_DEPENDENCIES].setInput(f_relatedChildren);
		
		f_labels[VIEW_DEPENDENT_ON_THIS].setText("Dependents on "+f_finding.getSummary());
		f_viewers[VIEW_DEPENDENT_ON_THIS].setInput(f_relatedAncestors);
		
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
		for(Composite parent : f_parents) {
			parent.layout(true, true);
		}
	}
	
	static final Object[] emptyArray = new Object[0];
	
	class Provider implements ILabelProvider, ITreeContentProvider {
		final boolean lookAtChildren;
		
		public Provider(boolean lookAtChildren) {
			this.lookAtChildren = lookAtChildren;
		}

		public void addListener(ILabelProviderListener listener) {
			// TODO Auto-generated method stub			
		}

		public void removeListener(ILabelProviderListener listener) {
			// TODO Auto-generated method stub
		}
		
		public void dispose() {
			// TODO Auto-generated method stub			
		}

		public boolean isLabelProperty(Object element, String property) {
			// TODO Auto-generated method stub
			return false;
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			// TODO Auto-generated method stub
		}

		public Object[] getElements(Object inputElement) {
			FindingRelationOverview fro = (FindingRelationOverview) inputElement;
			return fro.toArray();
		}

		public Object[] getChildren(Object parentElement) {
			// FIX if we show more of the graph
			return emptyArray;
		}

		public Object getParent(Object element) {
			// FIX if we show more of the graph
			return null;
		}

		public boolean hasChildren(Object element) {
			// FIX if we show more of the graph
			return false;
		}		
		
		public Image getImage(Object element) {
			return null;
		}

		public String getText(Object element) {
			FindingRelation fr = (FindingRelation) element;
			return getLabel(fr, lookAtChildren);
		}
	}
	
	public String getLabel(FindingRelation fr, boolean lookAtChildren) {
		FindingDetail fd  = details.get(lookAtChildren ? fr.getChildId() : fr.getParentId());
		//String pre = lookAtChildren ? "child " : "parent "; 
		//return pre + fr.getRelationType() + ' ' + fd.getSummary();
		return fr.getRelationType() + ' ' + fd.getSummary();
	}
}
