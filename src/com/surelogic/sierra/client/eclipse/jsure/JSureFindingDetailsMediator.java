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
	private final Map<Long,FindingRelationOverview> f_childOverviews = 
		new HashMap<Long,FindingRelationOverview>();
	private final Map<Long,FindingRelationOverview> f_ancestorOverviews = 
		new HashMap<Long,FindingRelationOverview>();
	
	private volatile Object lastSelected;

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
						lastSelected = selection.getFirstElement();
					}
				}
			});
			v.getTree().addListener(SWT.MouseDoubleClick, new Listener() {
				public void handleEvent(Event event) {
					final Object last = lastSelected;
					final FindingDetail data;
					if (last instanceof FindingRelationOverview) {						
						final FindingRelationOverview fro = (FindingRelationOverview) last;
						data = details.get(fro.getId());
					} else {
						final FindingRelation fr = (FindingRelation) lastSelected;				
						data = getDetail(fr, lookAtChildren);	
					}
					if (data != null) {				
						JDTUtility.tryToOpenInEditor(data.getProjectName(), 
								data.getPackageName(), data.getClassName(), data.getLineOfCode());
						
						FindingDetailsView.findingSelected(data.getFindingId(), false);
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
		children.setChecked(true);
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
	
	enum Direction {
		BOTH(true, true), AT_CHILDREN(true, false), AT_ANCESTORS(false, true);
		
		private final boolean atChildren, atAncestors;
		private Direction(boolean children, boolean ancestors) {
			
			atChildren = children;
			atAncestors = ancestors;
		}
		boolean lookAtChildren() {
			return atChildren;
		}
		boolean lookAtAncestors() {
			return atAncestors;
		}
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
							// FIX really only need to clear if database changes
							details.clear();
							f_childOverviews.clear();
							f_ancestorOverviews.clear();
							
							getOverviews(c, q, findingIdObj, detail, Direction.BOTH);
							
							f_relatedChildren  = f_childOverviews.get(findingIdObj);
							f_relatedAncestors = f_ancestorOverviews.get(findingIdObj);						
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

			private void getOverviews(final Connection c, final Query q, 
					                  final Long rootId, FindingDetail detail, Direction d) {
				//System.out.println(d+" : "+rootId);
				details.put(rootId, detail);
				if (d.lookAtChildren()) {
					FindingRelationOverview children  = FindingRelationOverview.getOverviewOrNull(q, rootId, true);					
					f_childOverviews.put(rootId, children);
					for(FindingRelation r : children.getRelations()) {
						Long id = r.getChildId();
						//System.out.println("Details for child "+id);
						getDetails(c, q, id, Direction.AT_CHILDREN);
					}
					children.sort(new Comparator<FindingRelation>() {
						public int compare(FindingRelation o1, FindingRelation o2) {
							return getLabel(o1, true).compareTo(getLabel(o2, true));
						}								
					});
				}
				if (d.lookAtAncestors()) {
					FindingRelationOverview ancestors = FindingRelationOverview.getOverviewOrNull(q, rootId, false);
					f_ancestorOverviews.put(rootId, ancestors);
					for(FindingRelation r : ancestors.getRelations()) {
						Long id = r.getParentId();
						getDetails(c, q, id, Direction.AT_ANCESTORS);
					}
					ancestors.sort(new Comparator<FindingRelation>() {
						public int compare(FindingRelation o1, FindingRelation o2) {
							return getLabel(o1, false).compareTo(getLabel(o2, false));
						}								
					});		
				}		
			}

			private void getDetails(final Connection c, final Query q, Long id, Direction d) {
				if (!details.containsKey(id)) {
					FindingDetail finding = FindingDetail.getDetailOrNull(c, id);
					getOverviews(c, q, id, finding, d);
				}
			}
		};
		job.schedule();
	}

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
		
		//f_labels[VIEW_OWN_DEPENDENCIES].setText("Dependencies for "+f_finding.getSummary());
		f_viewers[VIEW_OWN_DEPENDENCIES].setInput(new Root(f_relatedChildren));
		
		//f_labels[VIEW_DEPENDENT_ON_THIS].setText("Dependents on "+f_finding.getSummary());
		f_viewers[VIEW_DEPENDENT_ON_THIS].setInput(new Root(f_relatedAncestors));
		
		for(Composite parent : f_parents) {
			parent.layout(true, true);
		}
	}
	
	static class Root {
		final FindingRelationOverview overview;
		
		Root(FindingRelationOverview o) {
			overview = o;
		}
	}
	
	static final Object[] emptyArray = new Object[0];
	
	class Provider implements ILabelProvider, ITreeContentProvider {
		final boolean includeFocus = true;
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
			Root root = (Root) inputElement;
			if (includeFocus) {
				return new Object[] { root.overview };
			}
			return root.overview.toArray();
		}

		public boolean hasChildren(Object parent) {
			final FindingRelationOverview fro = getOverview(parent, lookAtChildren);						
			return fro != null && !fro.isEmpty();			
		}	
		
		public Object[] getChildren(Object parent) {
			final FindingRelationOverview fro = getOverview(parent, lookAtChildren);
			return fro != null && !fro.isEmpty() ? fro.toArray() : emptyArray;
		}

		public Object getParent(Object elt) {
			if (elt instanceof FindingRelationOverview) {
				return null;
			} 
			FindingRelation fr = (FindingRelation) elt;
			Long parentId = lookAtChildren ? fr.getParentId() : fr.getChildId();
			if (parentId == f_finding.getFindingId()) {
				return lookAtChildren ? f_relatedChildren : f_relatedAncestors;			
			}
			return null;
		}
		
		public Image getImage(Object elt) {
			FindingDetail fd;
			if (elt instanceof FindingRelationOverview) {
				FindingRelationOverview fro = (FindingRelationOverview) elt;
				fd = details.get(fro.getId());
			} else {
				FindingRelation fr = (FindingRelation) elt;
				fd = getDetail(fr, lookAtChildren);
			}
			return Utility.getImageFor(fd.getAssuranceType());
		}

		public String getText(Object elt) {
			if (elt instanceof FindingRelationOverview) {
				FindingRelationOverview fro = (FindingRelationOverview) elt;
				FindingDetail fd            = details.get(fro.getId());
				return fd.getSummary();
			}
			FindingRelation fr = (FindingRelation) elt;
			return getLabel(fr, lookAtChildren);
		}
	}
	
	private FindingDetail getDetail(FindingRelation fr, boolean lookAtChildren) {
		return details.get(lookAtChildren ? fr.getChildId() : fr.getParentId());
	}
	
	private String getLabel(FindingRelation fr, boolean lookAtChildren) {
		FindingDetail fd = getDetail(fr, lookAtChildren);
		//String pre = lookAtChildren ? "child " : "parent "; 
		//return pre + fr.getRelationType() + ' ' + fd.getSummary();
		return fr.getRelationType() + ' ' + fd.getSummary();
	}

	private FindingRelationOverview getOverview(Object parent, boolean lookAtChildren) {
		final FindingRelationOverview fro;
		if (parent instanceof FindingRelationOverview) {
			fro = (FindingRelationOverview) parent;
		} else {
			FindingRelation fr = (FindingRelation) parent;
			Long parentId = lookAtChildren ? fr.getChildId() : fr.getParentId();
			Map<Long,FindingRelationOverview> overviews = 
				lookAtChildren ? f_childOverviews : f_ancestorOverviews;
			fro = overviews.get(parentId);
		}
		return fro;
	}
}
