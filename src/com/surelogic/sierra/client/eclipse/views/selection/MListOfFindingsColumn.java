package com.surelogic.sierra.client.eclipse.views.selection;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.List;
//import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;

import org.apache.commons.lang.SystemUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.UIJob;

import com.surelogic.common.StringComparators;
import com.surelogic.common.eclipse.CascadingList;
import com.surelogic.common.eclipse.JDTUtility;
import com.surelogic.common.eclipse.SLImages;
import com.surelogic.common.eclipse.ViewUtility;
import com.surelogic.common.eclipse.CascadingList.IColumn;
import com.surelogic.common.eclipse.jobs.DatabaseJob;
import com.surelogic.common.eclipse.jobs.SLUIJob;
import com.surelogic.common.eclipse.logging.SLStatus;
import com.surelogic.common.i18n.I18N;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.client.eclipse.Data;
import com.surelogic.sierra.client.eclipse.Utility;
import com.surelogic.sierra.client.eclipse.dialogs.ExportFindingSetDialog;
import com.surelogic.sierra.client.eclipse.dialogs.MaximumFindingsShownDialog;
import com.surelogic.sierra.client.eclipse.model.FindingMutationUtility;
import com.surelogic.sierra.client.eclipse.model.selection.ISelectionObserver;
import com.surelogic.sierra.client.eclipse.model.selection.Selection;
import com.surelogic.sierra.client.eclipse.preferences.PreferenceConstants;
import com.surelogic.sierra.client.eclipse.views.FindingDetailsMediator;
import com.surelogic.sierra.client.eclipse.views.FindingDetailsView;
import com.surelogic.sierra.tool.message.Importance;

public final class MListOfFindingsColumn extends MColumn implements
		ISelectionObserver {
  /**
   * @see http://publicobject.com/glazedlists/documentation/swt_virtual_tables.html
   */
  private static final boolean USE_VIRTUAL = true;
  
	private Table f_table = null;

	MListOfFindingsColumn(CascadingList cascadingList, Selection selection,
			MColumn previousColumn) {
		super(cascadingList, selection, previousColumn);
    f_tables.add(this);
	}

	@Override
	void init() {
		getSelection().setShowingFindings(true);
		getSelection().addObserver(this);
		changed();
	}

	@Override
	void initOfNextColumnComplete() {
		final UIJob job = new SLUIJob() {
			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				MListOfFindingsColumn.super.initOfNextColumnComplete();
				notifyObserversOfLimitedFindings(f_isLimited);
				return Status.OK_STATUS;
			}
		};
		job.schedule();
	}

	@Override
	void dispose() {
		super.dispose();
		getSelection().setShowingFindings(false);
		getSelection().removeObserver(this);
		final int column = getColumnIndex();
		if (column != -1)
			getCascadingList().emptyFrom(column);
		
		notifyObserversOfDispose();
	}

  @Override
	int getColumnIndex() {
		if (f_table.isDisposed())
			return -1;
		else
			return getCascadingList().getColumnIndexOf(f_table);
	}

	@Override
	public void forceFocus() {
	  f_table.forceFocus();
	  getCascadingList().show(index);
	}
	
	public void selectionChanged(Selection selecton) {
		changed();
	}

	private void changed() {
		final UIJob job = new SLUIJob() {
			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				if (f_table != null && f_table.isDisposed()) {
					getSelection().removeObserver(MListOfFindingsColumn.this);
				} else {
					final Job job = new DatabaseJob("Refresh list of findings") {
						@Override
						protected IStatus run(IProgressMonitor monitor) {
							try {
								refreshData();
								refreshDisplay();
							} catch (Exception e) {
								final int errNo = 60;
								final String msg = I18N.err(errNo);
								return SLStatus
										.createErrorStatus(errNo, msg, e);
							} finally {
								initOfNextColumnComplete();
							}
							return Status.OK_STATUS;
						}
					};
					job.schedule();
				}
				return Status.OK_STATUS;
			}
		};
		job.schedule();
	}

	private static class FindingData {
		String f_summary;
		Importance f_importance;
		long f_findingId;

		String f_projectName;
		String f_packageName;
		int f_lineNumber;
		String f_typeName;
		String f_findingTypeName;
		String f_categoryName;
		String f_toolName;
    int index;
		
		@Override
		public String toString() {
			return "finding_id=" + f_findingId + " [" + f_importance
					+ "] of type " + f_findingTypeName + " and category "
					+ f_categoryName + " \"" + f_summary + "\" in "
					+ f_projectName + " " + f_packageName + "." + f_typeName
					+ " at line " + f_lineNumber + " from " + f_toolName;
		}
		@Override
		public boolean equals(Object o) {
		  if (o instanceof FindingData) {
		    FindingData other = (FindingData) o;
		    return f_findingId == other.f_findingId;
		  }
		  return false;
		}
		@Override
		public int hashCode() {
		  return (int) f_findingId; 
		}
	}
  static enum ColumnSort {
    UNSORTED, SORT_UP, SORT_DOWN
  }
	static abstract class ColumnData implements Comparator<FindingData> {
	  final String name;
	  int width = -1;
	  boolean visible = false;
	  ColumnSort sort = ColumnSort.UNSORTED;
	  
	  ColumnData(String name) {
	    this.name = name;
	  }	
	  ColumnData(String name, boolean visible, ColumnSort sort) {
	    this(name);
	    this.visible = visible;
	    this.width = -1;
	    this.sort = sort;
	  }
	  String getText(FindingData data)  { return ""; }
	  Image getImage(FindingData data) { return null; }

	  public int compare(FindingData o1, FindingData o2) {
      return sort == ColumnSort.SORT_DOWN ? -compareInternal(o1,o2) : compareInternal(o1,o2);
    }
	  protected int compareInternal(FindingData o1, FindingData o2) {
	    return StringComparators.SORT_ALPHABETICALLY.compare(getText(o1),getText(o2));
	  }
	}	
	private static final List<ColumnData> f_columns = new ArrayList<ColumnData>();
	static {
	  f_columns.add(new ColumnData("Summary", true, ColumnSort.SORT_UP) {
	    @Override String getText(FindingData data)  { 
	      return data.f_summary; 
	    }
	    @Override Image getImage(FindingData data)  { 
	      return Utility.getImageFor(data.f_importance);
	    }
	  });
	  f_columns.add(new ColumnData("Importance") {
	    @Override String getText(FindingData data)  { 
	      return data.f_importance.toStringSentenceCase(); 
	    }
      /*
	    @Override Image getImage(FindingData data)  { 
        return Utility.getImageFor(data.f_importance);
      }
      */
      @Override
      protected int compareInternal(FindingData o1, FindingData o2) {
        return o1.f_importance.ordinal() - o2.f_importance.ordinal();
      }
    });
	  f_columns.add(new ColumnData("Project") {
      @Override String getText(FindingData data)  { return data.f_projectName; }
    });
	  f_columns.add(new ColumnData("Package") {
      @Override String getText(FindingData data)  { return data.f_packageName; }
    });
	  f_columns.add(new ColumnData("Line#") {
      @Override String getText(FindingData data)  { return Integer.toString(data.f_lineNumber); }
      @Override
      protected int compareInternal(FindingData o1, FindingData o2) {
        return o1.f_lineNumber - o2.f_lineNumber;
      }
    });
	  f_columns.add(new ColumnData("Type") {
      @Override String getText(FindingData data)  { return data.f_typeName; }
    });
	  f_columns.add(new ColumnData("Finding Type") {
      @Override String getText(FindingData data)  { return data.f_findingTypeName; }
    });
	  f_columns.add(new ColumnData("Finding Category") {
      @Override String getText(FindingData data)  { return data.f_categoryName; }
    });
	  f_columns.add(new ColumnData("Tool") {
	    @Override String getText(FindingData data)  { return data.f_toolName; }
	  });
	}
	static Iterable<ColumnData> getColumns() {
	  return f_columns;
	}
	
	private final List<FindingData> f_rows = new /*CopyOnWrite*/ArrayList<FindingData>();
  private boolean f_isLimited = false;
	
	public void refreshData() {
		final String query = getQuery();
		try {
			final Connection c = Data.readOnlyConnection();
			try {
				final Statement st = c.createStatement();
				try {
					if (SLLogger.getLogger().isLoggable(Level.FINE)) {
						SLLogger.getLogger().fine(
								"List of findings query: " + query);
					}
					final ResultSet rs = st.executeQuery(query);
					f_rows.clear();
					f_isLimited = false;
					
					final int findingsListLimit = PreferenceConstants
							.getFindingsListLimit();
					while (rs.next()) {
					  int i = f_rows.size();
						if (i < findingsListLimit) {
							FindingData data = new FindingData();
							data.f_summary = rs.getString(1);
							data.f_importance = Importance.valueOf(rs
									.getString(2).toUpperCase());
							data.f_findingId = rs.getLong(3);
							data.f_projectName = rs.getString(4);
							data.f_packageName = rs.getString(5);
							data.f_typeName = rs.getString(6);
							data.f_lineNumber = rs.getInt(7);
							data.f_findingTypeName = rs.getString(8);
							data.f_categoryName = rs.getString(9);
							data.f_toolName = rs.getString(10);
							data.index = i;
							f_rows.add(data);
						} else {
						  f_isLimited = true;
						  
							/*
							 * We skipped a row so inform the user
							 */
							class WarningDialog extends SLUIJob {

								final private int f_findingsLimit;
								final private int f_findingsCount;

								public WarningDialog(int findingsLimit,
										int findingsCount) {
									f_findingsLimit = findingsListLimit;
									f_findingsCount = findingsCount;
								}

								@Override
								public IStatus runInUIThread(
										IProgressMonitor monitor) {
									if (PreferenceConstants
											.warnAboutMaximumFindingsShown()) {
										Dialog dialog = new MaximumFindingsShownDialog(
												f_findingsLimit,
												f_findingsCount);
										dialog.open();
									}
									return Status.OK_STATUS;
								}
							}
							final UIJob job = new WarningDialog(
									findingsListLimit, getSelection()
											.getFindingCountPorous());
							job.schedule();
							break;
						}
					}
				} finally {
					st.close();
				}
			} finally {
				c.close();
			}
		} catch (SQLException e) {
			SLLogger.getLogger().log(Level.SEVERE,
					"Query failed to read selected findings", e);
		}
	}

	/**
	 * Generates a query that is used in this class and by the export of
	 * findings. The category and tool are only used by the export.
	 * <p>
	 * The design here is suspect and probably needs some re-work.
	 * 
	 * @return Query on the overview of findings.
	 */
	public String getQuery() {
		StringBuilder b = new StringBuilder();
		b.append("select SUMMARY, IMPORTANCE, FINDING_ID,");
		b.append(" PROJECT, PACKAGE, CLASS, LINE_OF_CODE,");
		b.append(" FINDING_TYPE, CATEGORY, TOOL ");
		getSelection().addWhereClauseTo(b);
		b.append(" order by case");
		b.append(" when IMPORTANCE='Irrelevant' then 5");
		b.append(" when IMPORTANCE=       'Low' then 4");
		b.append(" when IMPORTANCE=    'Medium' then 3");
		b.append(" when IMPORTANCE=      'High' then 2");
		b.append(" when IMPORTANCE=  'Critical' then 1 end, SUMMARY");
		return b.toString();
	}

	private final KeyListener f_keyListener = new KeyListener() {
		public void keyPressed(KeyEvent e) {
			if (e.character == 0x01 && f_table != null) {
				f_table.selectAll();
			}
			else if (e.keyCode == SWT.ARROW_LEFT) {
			  getPreviousColumn().forceFocus();
			}
			else if (e.keyCode == SWT.ARROW_RIGHT /* || == ENTER */) {
			  f_doubleClick.handleEvent(null);
			}
		}

		public void keyReleased(KeyEvent e) {
			// Nothing to do
		}
	};

	private final AtomicReference<FindingData> lastSelected = new AtomicReference<FindingData>();
  private final Stack<FindingData> nearSelected = new Stack<FindingData>();
	
	private final Listener f_doubleClick = new Listener() {
		public void handleEvent(Event event) {
			final FindingData data = lastSelected.get();
			if (data != null) {
				JDTUtility.tryToOpenInEditor(data.f_projectName,
						data.f_packageName, data.f_typeName, data.f_lineNumber);
			}
		}
	};

	/**
	 * Remembers the last finding id that was selected in the list. If the list
	 * is refreshed than an attempt is made to select that finding id again. A
	 * value of <code>-1</code> indicates that no finding is selected.
	 */
	private long f_findingId = -1;

	private final Listener f_singleClick = new Listener() {
		public void handleEvent(Event event) {
			TableItem item = (TableItem) event.item;
			if (item != null) {
				final FindingData data = (FindingData) item.getData();
				lastSelected.set(data);
				addNearSelected(data.index - 1);
        addNearSelected(data.index + 1);
				
				/*
				 * Ensure the view is visible but don't change the focus.
				 */
				final FindingDetailsView view = (FindingDetailsView) ViewUtility
						.showView(FindingDetailsView.class.getName(), null,
								IWorkbenchPage.VIEW_VISIBLE);
				f_findingId = data.f_findingId;
				view.findingSelected(data.f_findingId);
			}
		}
	};
	
  protected void addNearSelected(int i) {
    if (i >= 0 && i < f_rows.size()) {
      nearSelected.add(f_rows.get(i));
    }
  }
	
	private final IColumn f_iColumn = new IColumn() {
		public Composite createContents(Composite panel) {
		  int mods = SWT.FULL_SELECTION | SWT.MULTI;
		  if (USE_VIRTUAL) {
		    mods = mods | SWT.VIRTUAL;
		  }		  
			f_table = new Table(panel, mods);
			f_table.setLinesVisible(true);
			f_table.addListener(SWT.MouseDoubleClick, f_doubleClick);
			f_table.addListener(SWT.Selection, f_singleClick);
			f_table.addKeyListener(f_keyListener);
			f_table.setItemCount(0);
			createTableColumns();
			
			if (USE_VIRTUAL) {
			  f_table.addListener(SWT.SetData, new Listener() {
			    // Only called the first time the TableItem is shown
			    // Intended to initialize the item
			    public void handleEvent(Event event) {
			      final TableItem item = (TableItem) event.item;
			      final int index = event.index;
			      FindingData data = f_rows.get(index);
			      initTableItem(event.index, data, item);
			    }
			  });
			}	  
			f_table.addListener(SWT.Traverse, new Listener() {
        public void handleEvent(Event e) {
          switch (e.detail) {
            case SWT.TRAVERSE_ESCAPE:
              setCustomTabTraversal(e);
              if (getPreviousColumn() instanceof MRadioMenuColumn) {
                MRadioMenuColumn column = (MRadioMenuColumn) getPreviousColumn();
                column.escape(null);
                /*
                column.clearSelection();
                column.emptyAfter(); // e.g. eliminate myself
                column.forceFocus();
                */
              }
              break;
            case SWT.TRAVERSE_TAB_NEXT:
              // Ignore, since we should be the last column
              setCustomTabTraversal(e);
              break;
            case SWT.TRAVERSE_TAB_PREVIOUS:
              setCustomTabTraversal(e);
              getPreviousColumn().forceFocus();
              break;
            case SWT.TRAVERSE_RETURN:
              setCustomTabTraversal(e);
              f_doubleClick.handleEvent(null);
              break;                       
          }
        }		  
			});
			
			final Menu menu = new Menu(f_table.getShell(), SWT.POP_UP);
			f_table.setMenu(menu);

			setupMenu(menu);

			updateTableContents();
			return f_table;
		}
	};

	private void updateTableContents() {
		if (f_table.isDisposed())
			return;

		f_table.setRedraw(false);
		for (TableItem i : f_table.getItems()) {
			if (i != null) {
				i.dispose();
			}
		}
		sortBasedOnColumns();
		
		if (USE_VIRTUAL) {
		  // Creates that many table items -- not necessarily initialized
		  f_table.setItemCount(f_rows.size());
		}
		  
		boolean selectionFound = false;
		int i = 0;
		for (FindingData data : f_rows) {
		  if (!USE_VIRTUAL) {
		    final TableItem item = new TableItem(f_table, SWT.NONE);
		    selectionFound = initTableItem(i, data, item);
		  } 
		  // Only needed if virtual
		  // Sets up the table to show the previous selection
		  else if (data.f_findingId == f_findingId) {
				initTableItem(i, data, f_table.getItem(i));
				selectionFound = true;
				break;
			}
			i++;
		}
		if (!selectionFound) {
		  // Look for a near-selection
		  FindingData data = null;		
		  if (!nearSelected.isEmpty()) {
		    Set<FindingData> rows = new HashSet<FindingData>(f_rows);
		    while (!nearSelected.isEmpty()) {
		      data = nearSelected.pop();
		      if (rows.contains(data)) {
		        initTableItem(data.index, data, f_table.getItem(data.index));
		        break;
		      }
		    }
		  }
		  f_findingId = (data == null) ? -1 : data.f_findingId;
		}
    nearSelected.clear();
		
    /*
		for (TableColumn c : f_table.getColumns()) {
			c.pack();
		}
		*/
    updateTableColumns();
    
    f_table.layout();
    if (USE_VIRTUAL) {
      // Computes the appropriate width for the longest item
      Point p = f_table.getSize();
      final int width = computeValueWidth();
      f_table.setSize(width, p.y);
    }
    
		f_table.setRedraw(true);
		/*
		 * Fix to bug 1115 (an XP specific problem) where the table was redrawn
		 * with lines through the row text. Aaron Silinskas found that a second
		 * call seemed to fix the problem (with a bit of flicker).
		 */
		if (SystemUtils.IS_OS_WINDOWS_XP)
			f_table.setRedraw(true);
	}

	private void sortBasedOnColumns() {
	  Comparator<FindingData> c = null;
	  // Traverse order backwards to construct proper comparator
	  int[] order = f_table.getColumnOrder();	  
	  for(int i=order.length-1; i>=0; i--) {
	    final TableColumn tc = f_table.getColumn(i);
	    final ColumnData cd = (ColumnData) tc.getData();
	    if (!cd.visible || cd.sort == ColumnSort.UNSORTED) {
	      continue; // Nothing to sort
	    }
	    if (c == null) {
	      c = cd;
	    } else {
	      final Comparator<FindingData> oldCompare = c;
	      c = new Comparator<FindingData>() {
          public int compare(FindingData o1, FindingData o2) {
            int result = cd.compare(o1, o2);
            if (result == 0) {
              return oldCompare.compare(o1, o2);
            }
            return result;
          }	        
	      };
	    }
	  }
	  if (c == null) {
	    c = f_columns.get(0); // The default sort
	  }
	  Collections.sort(f_rows, c);
  }

  private void createTableColumns() {
	  for(final ColumnData data : f_columns) {
	    final TableColumn tc = new TableColumn(f_table, SWT.NONE);
	    tc.setText(data.name);
	    tc.setData(data);
	    tc.setMoveable(true);
	    tc.addListener(SWT.Selection, new Listener() {
        public void handleEvent(Event event) {
          // Toggle sort
          switch (data.sort) {
            case SORT_DOWN: 
            default:
              data.sort = ColumnSort.UNSORTED;
              break;
            case SORT_UP:
              data.sort = ColumnSort.SORT_DOWN;
              break;
            case UNSORTED:
              data.sort = ColumnSort.SORT_UP;
              break;
          }
          updateTableContents(); 
        }	      
	    });
	    tc.addControlListener(new ControlListener() {
        public void controlMoved(ControlEvent e) {
          // Nothing to do
        }
        public void controlResized(ControlEvent e) {
          if (!updateTableColumns) {
            saveColumnAppearance(data, tc);
          }
        }	      
	    });
	  }
	}

	/**
	 * To be called after f_rows has been initialized
	 */
	private boolean loadColumnAppearance(TableColumn tc) {
	  ColumnData data = (ColumnData) tc.getData();
	  tc.setResizable(data.visible);
	  if (data.visible) {
	    if (data.width < 0) {
	      data.width = computeValueWidth(data);
	    } 
	    tc.setWidth(data.width);	    
	  } else {
	    tc.setWidth(0);
	  }
	  Image img;
	  switch (data.sort) {
	    case SORT_DOWN: 
	      img = SLImages.getImage(SLImages.IMG_DOWN);
	      break;
	    case SORT_UP:
	      img = SLImages.getImage(SLImages.IMG_UP);
	      break;
	    case UNSORTED:
	    default:
	      img = null;
	      break;
	  }
    tc.setImage(img);	  
	  return data.visible;
	}

	static void saveColumnAppearance(ColumnData data, TableColumn tc) {
	  // System.out.println("width = "+tc.getWidth());
	  data.width = tc.getWidth();
	}
  /*
  private static void setColumnVisible(TableColumn tc, boolean visible) {
    ColumnData data = (ColumnData) tc.getData();
    if (visible) {
      tc.setWidth(data.width);
      tc.setResizable(true);
    } else {
      data.width = tc.getWidth();
      tc.setWidth(0);
      tc.setResizable(false);
    }
  }
  */
	private boolean updateTableColumns = false;
	
  private void updateTableColumns() {
    int numVisible = 0;
    TableColumn lastVisible = null;
    updateTableColumns = true;
    
    for(TableColumn tc : f_table.getColumns()) {
      if (loadColumnAppearance(tc)) {
        numVisible++;
        lastVisible = tc;
      }
    }
    if (numVisible == 1) {
      ColumnData cd = (ColumnData) lastVisible.getData();
      lastVisible.setWidth(computeValueWidth(cd));
    } 
    updateTableColumns = false;
    f_table.setHeaderVisible(numVisible > 1);
  }

  /*
	 * This actually finds the longest item, and
	 * creates a real TableItem for that item 
	 * to ensure that the table gets sized properly
	 */
	int computeValueWidth() {
	  return computeValueWidth(f_columns.get(0));
	}
	
	private static final Rectangle ZERO = new Rectangle(0, 0, 0, 0);
	
	int computeValueWidth(final ColumnData cd) {
	  Image temp = new Image(null, 100, 100);
	  GC gc = new GC(temp);
	  int longest = 0;
	  FindingData longestData = null;
	  int longestIndex = -1;
	  int i = 0;
	  for(FindingData data : f_rows) {
	    Point size = gc.textExtent(cd.getText(data));
	    Image img = cd.getImage(data);
	    Rectangle rect = (img == null) ? ZERO : img.getBounds();
	    int width = size.x + rect.width;
	    if (width > longest) {
	      longest = width;
	      longestData = data;
	      longestIndex = i;
	    }	      
	    i++;
	  }
	  gc.dispose();
	  temp.dispose();
	  if (longestData != null) {
	    if (longestIndex >= f_table.getItemCount()) {
	      LOG.warning("Got index outside of table: "+longestIndex+", "+f_table.getItemCount());
	    } else {
	      initTableItem(longestIndex, longestData, f_table.getItem(longestIndex));
	    }
	  }

	  if (longest < 25) {
	    return 30;
	  }
	  return longest + 5;
	}
	
	private boolean initTableItem(final int i, FindingData data, final TableItem item) {
	  if (i != data.index) {
	    // Now set, because we're sorting
	    data.index = i;
	    //throw new IllegalArgumentException(i+" != data.index: "+data.index);
	  }
		item.setText(data.f_summary);
		item.setImage(Utility.getImageFor(data.f_importance));
		item.setData(data);
		if (data.f_findingId == f_findingId) {
			f_table.setSelection(item);
			return true;
		}
		// Init columns
		//int numVisible = 0;
		int j = 0;
    for(TableColumn tc : f_table.getColumns()) {
      ColumnData cd = (ColumnData) tc.getData();      
      item.setText(j, cd.getText(data));
      item.setImage(j, cd.getImage(data));
      /*
      if (tc.getResizable()) {
        numVisible++;
      }
      */
      j++;
    }
    /*
    // Special handling
    if (numVisible == 1) {
      item.setImage(0, Utility.getImageFor(data.f_importance));
    }
    */
		return false;
	}

	private void setupMenu(final Menu menu) {
		final MenuItem set = new MenuItem(menu, SWT.CASCADE);
		set.setText("Set Importance");
		set.setImage(SLImages.getImage(SLImages.IMG_ASTERISK_DIAMOND_ORANGE));

		/*
		 * Quick audit
		 */

		final MenuItem quickAudit = new MenuItem(menu, SWT.PUSH);
		quickAudit.setText("Mark As Examined by Me");
		quickAudit.setImage(SLImages.getImage(SLImages.IMG_SIERRA_STAMP_SMALL));

		new MenuItem(menu, SWT.SEPARATOR);

		final MenuItem filterFindingTypeFromScans = new MenuItem(menu, SWT.PUSH);
		filterFindingTypeFromScans
				.setText("Filter Finding Type From Future Scans");

		new MenuItem(menu, SWT.SEPARATOR);

		final Menu importanceMenu = new Menu(menu.getShell(), SWT.DROP_DOWN);
		set.setMenu(importanceMenu);
		final MenuItem setCritical = new MenuItem(importanceMenu, SWT.PUSH);
		setCritical.setText(Importance.CRITICAL.toStringSentenceCase());
		setCritical.setImage(SLImages
				.getImage(SLImages.IMG_ASTERISK_ORANGE_100));
		final MenuItem setHigh = new MenuItem(importanceMenu, SWT.PUSH);
		setHigh.setText(Importance.HIGH.toStringSentenceCase());
		setHigh.setImage(SLImages.getImage(SLImages.IMG_ASTERISK_ORANGE_75));
		final MenuItem setMedium = new MenuItem(importanceMenu, SWT.PUSH);
		setMedium.setText(Importance.MEDIUM.toStringSentenceCase());
		setMedium.setImage(SLImages.getImage(SLImages.IMG_ASTERISK_ORANGE_50));
		final MenuItem setLow = new MenuItem(importanceMenu, SWT.PUSH);
		setLow.setText(Importance.LOW.toStringSentenceCase());
		setLow.setImage(SLImages.getImage(SLImages.IMG_ASTERISK_ORANGE_25));
		final MenuItem setIrrelevant = new MenuItem(importanceMenu, SWT.PUSH);
		setIrrelevant.setText(Importance.IRRELEVANT.toStringSentenceCase());
		setIrrelevant.setImage(SLImages
				.getImage(SLImages.IMG_ASTERISK_ORANGE_0));

		menu.addListener(SWT.Show, new Listener() {
			public void handleEvent(Event event) {
				int[] itemIndices = f_table.getSelectionIndices();
				final boolean findingSelected = itemIndices.length > 0;
				set.setEnabled(findingSelected);
				quickAudit.setEnabled(findingSelected);
				filterFindingTypeFromScans.setEnabled(findingSelected);
				if (findingSelected) {
					String importanceSoFar = null;
					String findingTypeSoFar = null;
					for (int index : itemIndices) {
						final FindingData data = f_rows.get(index);
						String importance = data.f_importance
								.toStringSentenceCase();
						if (importanceSoFar == null) {
							importanceSoFar = importance;
						} else if (!importanceSoFar.equals(importance)) {
							importanceSoFar = ""; // More than one
						}
						// Otherwise, it's all the same so far

						String findingType = data.f_findingTypeName;
						if (findingTypeSoFar == null) {
							findingTypeSoFar = findingType;
						} else if (!findingTypeSoFar.equals(findingType)) {
							findingTypeSoFar = ""; // More than one
						}
					}
					final String currentImportance = importanceSoFar;
					final String currentFindingType = findingTypeSoFar;
					setCritical.setData(itemIndices);
					setHigh.setData(itemIndices);
					setMedium.setData(itemIndices);
					setLow.setData(itemIndices);
					setIrrelevant.setData(itemIndices);
					setCritical.setEnabled(!currentImportance
							.equals(setCritical.getText()));
					setHigh.setEnabled(!currentImportance.equals(setHigh
							.getText()));
					setMedium.setEnabled(!currentImportance.equals(setMedium
							.getText()));
					setLow.setEnabled(!currentImportance.equals(setLow
							.getText()));
					setIrrelevant.setEnabled(!currentImportance
							.equals(setIrrelevant.getText()));
					quickAudit.setData(itemIndices);
					if ("".equals(currentFindingType)) {
						filterFindingTypeFromScans.setEnabled(false);
						filterFindingTypeFromScans
								.setText("Filter Selected Findings From Future Scans");
					} else {
						filterFindingTypeFromScans.setEnabled(true);
						filterFindingTypeFromScans.setData(itemIndices);
						filterFindingTypeFromScans.setText("Filter All '"
								+ currentFindingType
								+ "' Findings From Future Scans");
					}
				}
			}
		});

		final Listener changeImportance = new SelectionListener() {
      private Importance getImportance(MenuItem item) {
        return Importance.valueOf(item.getText().toUpperCase());
      }
      @Override
      protected void handleFinding(MenuItem item, FindingData data) {
        FindingMutationUtility.asyncChangeImportance(data.f_findingId, data.f_importance, 
                                                     getImportance(item));
      }
      @Override
      protected void handleFindings(MenuItem item, FindingData data, Collection<Long> ids) {
        FindingMutationUtility.asyncChangeImportance(ids, getImportance(item));
      }
		};
		setCritical.addListener(SWT.Selection, changeImportance);
		setHigh.addListener(SWT.Selection, changeImportance);
		setMedium.addListener(SWT.Selection, changeImportance);
		setLow.addListener(SWT.Selection, changeImportance);
		setIrrelevant.addListener(SWT.Selection, changeImportance);

		quickAudit.addListener(SWT.Selection, new SelectionListener() {
      @Override
      protected void handleFinding(MenuItem item, FindingData data) {
        FindingMutationUtility.asyncComment(data.f_findingId, FindingDetailsMediator.STAMP_COMMENT);
      }
      @Override
      protected void handleFindings(MenuItem item, FindingData data, Collection<Long> ids) {
        FindingMutationUtility.asyncComment(ids, FindingDetailsMediator.STAMP_COMMENT);
      }
		});

		filterFindingTypeFromScans.addListener(SWT.Selection, new SelectionListener() {
      @Override
      protected void handleFinding(MenuItem item, FindingData data) {
        FindingMutationUtility
        .asyncFilterFindingTypeFromScans(data.f_findingId, data.f_findingTypeName);
      }
      @Override
      protected void handleFindings(MenuItem item, FindingData data, Collection<Long> ids) {
        FindingMutationUtility
        .asyncFilterFindingTypeFromScans(ids, data.f_findingTypeName);
      }
		});

		final MenuItem export = new MenuItem(menu, SWT.PUSH);
		export.setText("Export...");
		export.setImage(SLImages.getImage(SLImages.IMG_EXPORT));
		export.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				final ExportFindingSetDialog dialog = new ExportFindingSetDialog(
						PlatformUI.getWorkbench().getActiveWorkbenchWindow()
								.getShell(), getQuery());
				dialog.open();
			}
		});
	}

	private abstract class SelectionListener implements Listener {
    public final void handleEvent(Event event) {
      if (event.widget instanceof MenuItem) {
        MenuItem item = (MenuItem) event.widget;
        if (item.getData() instanceof int[]) {
          final int[] itemIndices = (int[]) item.getData();
          final FindingData data = f_rows.get(itemIndices[0]);
          if (itemIndices.length == 1) {
            handleFinding(item, data);
          } else {
            final Collection<Long> ids = extractFindingIds(itemIndices);
            handleFindings(item, data, ids);
          }
        }
      }
    }
    protected abstract void handleFinding(MenuItem item, FindingData data);
    protected abstract void handleFindings(MenuItem item, FindingData data, Collection<Long> ids);
	}
	
	private void refreshDisplay() {
		final UIJob job = new SLUIJob() {
			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				if (f_table == null) {
					int addAfterColumn = getPreviousColumn().getColumnIndex();
					// create the display table
					getCascadingList().addColumnAfter(f_iColumn,
							addAfterColumn, false);
				} else {
					// update the table's contents
					updateTableContents();
				}
				return Status.OK_STATUS;
			}
		};
		job.schedule();
	}

	private Collection<Long> extractFindingIds(final int[] itemIndices) {
		final Collection<Long> ids = new ArrayList<Long>(itemIndices.length);
		for (int ti : itemIndices) {
			FindingData fd = f_rows.get(ti);
			if (fd != null) {
				ids.add(fd.f_findingId);
			}
		}
		return ids;
	}
	
  @Override 
  void selectAll() {
    if (f_table.isFocusControl()) {
      f_table.selectAll();
    } else {
      super.selectAll();
    }
  }

  private IFindingsObserver observer;
  
  public void addObserver(IFindingsObserver o) {
    observer = o;
  }
  
  private void notifyObserversOfLimitedFindings(boolean isLimited) {
    if (observer != null) {
      observer.findingsLimited(isLimited);
    }
  }
  
  private void notifyObserversOfDispose() {
    if (observer != null) {
      observer.findingsDisposed();
    }
  }

  private static final Set<MListOfFindingsColumn> f_tables = new HashSet<MListOfFindingsColumn>();
  
  public static void columnsUpdated(final ColumnData data, final boolean nowVisible) {
    if (data.visible == nowVisible) {
      return;
    }
    data.visible = nowVisible;
    
    Iterator<MListOfFindingsColumn> it = f_tables.iterator();
    while (it.hasNext()) {
      MListOfFindingsColumn c = it.next();
      Table t = c.f_table;
      if (t != null) {
        if (t.isDisposed()) {
          it.remove();
        } 
        if (!nowVisible) {
          // Save column width        
          for(TableColumn tc : t.getColumns()) {
            if (data == tc.getData()) {
              saveColumnAppearance(data, tc);
            }
          }
        }                
        c.updateTableColumns();
      }
    }
  }
}
