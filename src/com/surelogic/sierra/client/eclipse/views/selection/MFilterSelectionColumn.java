package com.surelogic.sierra.client.eclipse.views.selection;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;

import org.apache.commons.lang.SystemUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.progress.UIJob;

import com.surelogic.common.eclipse.*;
import com.surelogic.common.eclipse.jobs.SLUIJob;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.client.eclipse.model.selection.Filter;
import com.surelogic.sierra.client.eclipse.model.selection.IFilterObserver;
import com.surelogic.sierra.client.eclipse.model.selection.Selection;

public final class MFilterSelectionColumn extends MColumn implements
		IFilterObserver
{

	private final Filter f_filter;

	Filter getFilter() {
		return f_filter;
	}

	private Composite f_panel = null;
	private Table f_reportContents = null;
	private Label f_totalCount = null;
	private Label f_porousCount = null;
	private Group f_reportGroup = null;
	private TableColumn f_valueColumn = null;
	private TableColumn f_graphColumn = null;
  private Color f_barColorDark = null;
  private Color f_barColorLight = null;

	private Menu f_menu = null;
	private MenuItem f_selectAllMenuItem = null;
	private MenuItem f_deselectAllMenuItem = null;
	private MenuItem f_sortByCountMenuItem = null;
  private List<String> valueList;
	private String f_mouseOverLine = "";
  
	private boolean f_sortByCount = false;

	private static final int GRAPH_WIDTH = 75;
	
	MFilterSelectionColumn(CascadingList cascadingList, Selection selection,
			MColumn previousColumn, Filter filter) {
		super(cascadingList, selection, previousColumn);
		assert filter != null;
		f_filter = filter;
	}

	@Override
	void init() {
		CascadingList.IColumn c = new CascadingList.IColumn() {
			public Composite createContents(Composite panel) {
				f_panel = new Composite(panel, SWT.NONE);
				f_panel.setLayout(new FillLayout());			
				
				f_reportGroup = new Group(f_panel, SWT.NONE);
				f_reportGroup.setText(f_filter.getFactory().getFilterLabel());
				GridLayout gridLayout = new GridLayout();
				f_reportGroup.setLayout(gridLayout);

				f_totalCount = new Label(f_reportGroup, SWT.RIGHT);
				f_totalCount.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT,
						true, false));

				f_reportContents = new Table(f_reportGroup, SWT.VIRTUAL | SWT.CHECK | SWT.FULL_SELECTION | SWT.V_SCROLL);
				GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
				f_reportContents.setLayoutData(data);

				//f_reportContents.setHeaderVisible(true);
				f_valueColumn = new TableColumn(f_reportContents, SWT.BORDER);
				f_valueColumn.setText("Value");
				f_valueColumn.setToolTipText("");				
				f_graphColumn = new TableColumn(f_reportContents, SWT.BORDER);
				f_graphColumn.setText("#");
				f_graphColumn.setWidth(75);
				f_graphColumn.setToolTipText("# of applicable findings with the given value");
				f_reportContents.setBackground(f_reportGroup.getBackground());
				f_reportContents.addFocusListener(new FocusListener() {
          public void focusGained(FocusEvent e) {
            if (valueList == null || valueList.isEmpty()) {
              Color focused = 
                f_reportContents.getDisplay().getSystemColor(SWT.COLOR_LIST_SELECTION);
              Color focusedText = 
                f_reportContents.getDisplay().getSystemColor(SWT.COLOR_LIST_SELECTION_TEXT);
              f_totalCount.setBackground(focused); 
              f_totalCount.setForeground(focusedText);
            }
          }
          public void focusLost(FocusEvent e) {
            f_totalCount.setBackground(null);           
            f_totalCount.setForeground(null);  
          }				  
				});
				
				
				final AtomicBoolean ignoreNextSelection = new AtomicBoolean();
				f_reportContents.addKeyListener(new KeyListener() {
          public void keyPressed(KeyEvent e) {                   
            MColumn column = null;
            if (e.keyCode == SWT.ARROW_LEFT) {
              column = getPreviousColumn();
            }
            else if (e.keyCode == SWT.ARROW_RIGHT) {
              column = getNextColumn();
            }
            else if (e.keyCode == SWT.ARROW_DOWN || e.keyCode == SWT.ARROW_UP) {
            	// Works because it's always called before the selection handler            	
            	ignoreNextSelection.set(true);
            	return;
            }
            focusOnColumn(column);
          }
          public void keyReleased(KeyEvent e) {
            if (e.character == ' ' ||
                SystemUtils.IS_OS_MAC_OSX && e.character == SWT.CR) {              
              // Called after the table toggles the item
              int selected = f_reportContents.getSelectionIndex();
              if (selected >= 0) {
                TableItem item = f_reportContents.getItem(selected);
                if (SystemUtils.IS_OS_MAC_OSX) {
                  item.setChecked(!item.getChecked());
                }
                selectionChanged(item);
              }
              return;              
            }     
          }         
        }); 
				f_reportContents.addListener(SWT.Traverse, new Listener() {
	        public void handleEvent(Event e) {
	          switch (e.detail) {
	            case SWT.TRAVERSE_TAB_NEXT:
	              setCustomTabTraversal(e);
	              focusOnColumn(getNextColumn());
	              break;
	            case SWT.TRAVERSE_TAB_PREVIOUS:	   
	              setCustomTabTraversal(e);
	              focusOnColumn(getPreviousColumn());
	              break;
	            case SWT.TRAVERSE_ESCAPE:
	              setCustomTabTraversal(e);
	              MColumn column = getPreviousColumn();
	              if (column instanceof MRadioMenuColumn) {
	                MRadioMenuColumn radio = (MRadioMenuColumn) column;
	                radio.escape(null);
	              }
	              break;
	          }
	        }
				});
				
				f_reportContents.addListener(SWT.MouseDown, new Listener() {
					public void handleEvent(Event e) {
						int mods = e.stateMask & SWT.MODIFIER_MASK;
						if (e.button != 1 || mods != 0) {
							ignoreNextSelection.set(true);						
						}
					}				
				});
				f_reportContents.addSelectionListener(new SelectionListener() {
					public void widgetDefaultSelected(SelectionEvent e) {
						// e.g. return
						TableItem item = (TableItem) e.item;
						if ((e.detail & SWT.CHECK) == 0) {
							item.setChecked(!item.getChecked());
						}
						selectionChanged(item);
					}

					public void widgetSelected(SelectionEvent e) {       
						if (ignoreNextSelection.getAndSet(false)) {
							return;
						}
						widgetDefaultSelected(e);
					}				  
				});		
				
				f_reportContents.addListener(SWT.MouseMove, new Listener() {
		      public void handleEvent(Event e) {
		        Point p = new Point(e.x, e.y);
		        TableItem item = f_reportContents.getItem(p);
		        if (item != null) {
		          f_mouseOverLine = (String) item.getData();
		          f_reportContents.redraw();
		        }
		      }
		    });
				f_reportContents.addListener(SWT.MouseExit, new Listener() {
		      public void handleEvent(Event e) {
		        f_mouseOverLine = "";
		        f_reportContents.redraw();
		      }
		    });
				
	      f_barColorDark = new Color(f_reportContents.getDisplay(), 255, 113, 18);
	      f_barColorLight = new Color(f_reportContents.getDisplay(), 238, 216, 198);
	      /**
	       * See http://publicobject.com/glazedlists/documentation/swt_virtual_tables.html
	       */
	      f_reportContents.addListener(SWT.SetData, new Listener() {
	        // Only called the first time the TableItem is shown
          // Intended to initialize the item
	        public void handleEvent(Event event) {
	          final TableItem item = (TableItem) event.item;
	          final int index = event.index;
	          updateData(item, index);
	        }
	      });
	      
	      /**
	       * Note that the next three listeners implement the bar graph
	       * See http://www.eclipse.org/articles/article.php?file=Article-CustomDrawingTableAndTreeItems/index.html
	       */ 	     
	      f_reportContents.addListener(SWT.MeasureItem, new Listener() {
	        /**
	         * The first custom draw event that is sent. This event 
	         * gives a client the opportunity to specify the width 
	         * and/or height of a cell's content
	         */
	        public void handleEvent(Event event) {
	          if (event.index == 1) {
	            event.width = GRAPH_WIDTH;
	          }
	        }
	      });	      
	      f_reportContents.addListener(SWT.EraseItem, new Listener() {
	        /**
	         * Sent just before the background of a cell is about to be drawn. 
	         * The background consists of the cell's background color or the 
	         * selection background if the item is selected. This event allows
	         * a client to custom draw one or both of these. Also, this event 
	         * allows the client to indicate whether the cell's default foreground
	         * should be drawn following the drawing of the background.
	         */
	        public void handleEvent(Event event) {
	          if (event.index == 1) {
	            /*
	             * Specifies that we will handle all but the focus rectangle
	             */
	            event.detail &= ~(1<<5); // SWT.HOT;
	            event.detail &= ~SWT.SELECTED;
	            event.detail &= ~SWT.BACKGROUND;
	            event.detail &= ~SWT.FOREGROUND;
	          }
	        }
	      });	      
	      f_reportContents.addListener(SWT.PaintItem, new Listener() {
	        /**
	         * Sent for a cell just after its default foreground contents have
	         * been drawn. This event allows a client to augment the cell, or 
	         * to completely draw the cell's content.
	         */
	        public void handleEvent(Event event) {
	          if (event.index == 1) {
	            /*
	             * We're drawing everything
	             */
	            TableItem item = (TableItem) event.item;
	            String value = (String) item.getData();
	            int count = f_filter.getSummaryCountFor(value);
	            int percent = computeBarGraphPercent(count);
	            Display display = f_reportContents.getDisplay();
	            GC gc = event.gc;
	            boolean checked = item.getChecked();
	            final int width = computeBarGraphWidth(item, GRAPH_WIDTH);
	            
	            // Save old colors
	            Color oldForeground = gc.getForeground();
	            Color oldBackground = gc.getBackground();
	            // Draw background fill
	            final int height = event.height-2;
	            if (f_mouseOverLine.equals(value)) {
	              //gc.setForeground(f_barColorDark);
	              gc.setBackground(display.getSystemColor(SWT.COLOR_WHITE));
	              gc.fillRectangle(event.x, event.y, GRAPH_WIDTH, height);
	              gc.setBackground(f_barColorLight);
	              gc.fillRectangle(event.x, event.y, width, height);
	            } else {
	              //gc.setForeground(f_barColorLight);
	              gc.setBackground(f_reportGroup.getBackground());
	              gc.fillRectangle(event.x, event.y, GRAPH_WIDTH, height);
	              gc.setBackground(f_barColorDark);
	              gc.fillRectangle(event.x, event.y, width, height);
	            }
	            
	            // Draw bounding rectangle and quartile bars
	            Rectangle rect2 = new Rectangle(event.x, event.y, GRAPH_WIDTH - 1, height);
	            gc.setForeground(display.getSystemColor(SWT.COLOR_GRAY));
	            gc.drawRectangle(rect2);
	            if (percent > 25) {
	              int p = (GRAPH_WIDTH - 1) * 25 / 100;
	              gc.drawLine(event.x + p, event.y, event.x + p, event.y + height);
	            }
	            if (percent > 50) {
	              int p = (GRAPH_WIDTH - 1) * 50 / 100;
	              gc.drawLine(event.x + p, event.y, event.x + p, event.y + height);
	            }
	            if (percent > 75) {
	              int p = (GRAPH_WIDTH - 1) * 75 / 100;
	              gc.drawLine(event.x + p, event.y, event.x + p, event.y + height);
	            }
	            String text = StringUtility.toCommaSepString(count);
	            Point size = gc.textExtent(text);
	            int offset = Math.max(0, (height - size.y) / 2);
	            int rightJ = GRAPH_WIDTH - 2 - size.x;	            
	            boolean mouseOverGraph = f_mouseOverLine.equals(value);
	            if (mouseOverGraph || checked) {
	              gc.setForeground(display.getSystemColor(SWT.COLOR_BLACK));
	            }
	            gc.drawText(text, event.x + rightJ, event.y + offset, true);
	            
	            // Restore old colors
	            gc.setForeground(oldForeground);
	            gc.setBackground(oldBackground);
	          }
	        }
	      });	      
	      			
				f_porousCount = new Label(f_reportGroup, SWT.RIGHT);
				f_porousCount.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT,
						true, false));

				f_menu = new Menu(f_reportGroup.getShell(), SWT.POP_UP);
				f_menu.addListener(SWT.Show, new Listener() {
					public void handleEvent(Event event) {
						final boolean valuesExist = f_filter.hasValues();
						f_selectAllMenuItem.setEnabled(valuesExist);
						f_deselectAllMenuItem.setEnabled(valuesExist);
						f_sortByCountMenuItem.setSelection(f_sortByCount);
					}
				});

				f_selectAllMenuItem = new MenuItem(f_menu, SWT.PUSH);
				f_selectAllMenuItem.setText("Select All");
				f_selectAllMenuItem.addListener(SWT.Selection, new Listener() {
					public void handleEvent(Event event) {
						selectAllItems();
					}
				});
				f_deselectAllMenuItem = new MenuItem(f_menu, SWT.PUSH);
				f_deselectAllMenuItem.setText("Deselect All");
				f_deselectAllMenuItem.addListener(SWT.Selection,
						new Listener() {
							public void handleEvent(Event event) {
								f_filter.setPorousNone();
				        for(TableItem item : f_reportContents.getItems()) {
		              item.setChecked(false);
		            }
							}
						});
				new MenuItem(f_menu, SWT.SEPARATOR);
				f_sortByCountMenuItem = new MenuItem(f_menu, SWT.CHECK);
				f_sortByCountMenuItem.setText("Sort By Finding Count");
				f_sortByCountMenuItem.addListener(SWT.Selection,
						new Listener() {
							public void handleEvent(Event event) {
								f_sortByCount = !f_sortByCount;
								updateReport();
							}
						});

				//f_reportViewport.setMenu(f_menu);
				f_reportContents.setMenu(f_menu);
				f_reportGroup.setMenu(f_menu);
				f_totalCount.setMenu(f_menu);
				f_porousCount.setMenu(f_menu);

				updateReport();
				return f_panel;
			}
		};
		getCascadingList().addColumnAfter(c,
				getPreviousColumn().getColumnIndex(), false);
		f_filter.addObserver(this);
		initOfNextColumnComplete();
	}

	int computeBarGraphPercent(int count) {
	  int total = f_filter.getFindingCountTotal();
    int percent = (int) (((double) count / (double) total) * 100);
    return percent;
	}
	
	int computeBarGraphWidth(TableItem item, int totalWidth) {
	  String value = (String) item.getData();
    int count = f_filter.getSummaryCountFor(value);
    int percent = computeBarGraphPercent(count);
    int width = (totalWidth - 1) * percent / 100;
    if (width < 2 && count > 0)
      width = 2;
    return width;
	}
	
	@Override
	void dispose() {
		super.dispose();
		f_filter.removeObserver(this);
		final int column = getColumnIndex();
		if (column != -1)
			getCascadingList().emptyFrom(column);
		
		/*
		if (lineFactory != null) {
		  lineFactory.dispose();
		}
		*/
	}

	@Override
	int getColumnIndex() {
		if (f_panel.isDisposed())
			return -1;
		else
			return getCascadingList().getColumnIndexOf(f_panel);
	}

	/**
	 * Must be called from the UI thread.
	 */
	private void updateReport() {
		if (f_panel.isDisposed())
			
		  return;
		/*
		 * Fix total count at the top.
		 */
		final int total = f_filter.getFindingCountTotal();
		f_totalCount.setText(StringUtility.toCommaSepString(total)
				+ (total == 1 ? " Finding" : " Findings"));

		/*
		 * Fix the value lines.
		 */
		final List<String> valueList = f_sortByCount ? f_filter
				.getValuesOrderedBySummaryCount() : f_filter.getAllValues();
    this.valueList = valueList;
				
		/*
		 * filterContentsChanged tracks if the rows in this filter selection
		 * column have changed. We want to avoid a call to pack because the
		 * scroll bar gets moved back up to the top each time this method is
		 * called.
		 */
		boolean filterContentsChanged = false;
		final int currentRows = f_reportContents.getItemCount();
		if (currentRows != valueList.size()) {
		  filterContentsChanged = true;
		  f_reportContents.setItemCount(valueList.size());
		  // Update all the items
		  TableItem[] items = f_reportContents.getItems();
		  int i=0;
		  for(String row : valueList) {
			updateData(items[i], row);			  
	        i++;
		  }	    
		} else {
		  TableItem[] items = f_reportContents.getItems();
		  int i=0;
		  for(String row : valueList) {
			TableItem item = items[i];
			if (!row.equals(item.getData())) {
			  filterContentsChanged = true;
			  updateData(item, row);			  
			}
	        i++;
		  }
		}

		final int porousCount = f_filter.getFindingCountPorous();
		if (f_porousCount != null && !f_porousCount.isDisposed())
			f_porousCount.setText("");
		if (f_reportContents.getItemCount() > 0) {
			final String porousCountString = 
			  StringUtility.toCommaSepString(porousCount);
			f_porousCount.setText(porousCountString);
			String msg = (porousCount == 0 ? "No" : porousCountString) + 
			             (porousCount != 1 ? " findings" : " finding")
                                     + " selected";
			f_totalCount.setToolTipText(msg);
			f_porousCount.setToolTipText(msg);
		}		
		f_valueColumn.setWidth(computeValueWidth());
		f_graphColumn.setWidth(GRAPH_WIDTH + 5);
		f_reportContents.layout();
		f_reportGroup.layout();
		if (filterContentsChanged)
			f_panel.pack();
		f_panel.layout();
	}
	
	int computeValueWidth() {
    Image temp = new Image(null, 100, 100);
    GC gc = new GC(temp);
	  int longest = 0;
	  int longestIndex = -1;
	  int i = 0;
    for(String value : valueList) {
      Point size = gc.textExtent(value);
      if (size.x > longest) {
        longest = size.x;
        longestIndex = i;
      }
      i++;
    }
    gc.dispose();
    temp.dispose();
    
    if (longestIndex >= 0) {
      updateData(f_reportContents.getItem(longestIndex), longestIndex);
    }
    if (longest < 25) {
      return 50;
    }
    return longest + 25;
	}
	
	void updateData(final TableItem item, int i) {
	  final String value = valueList.get(i);
	  //System.out.println("Initialized "+i+": "+value);
	  updateData(item, value);
	}
	  
	void updateData(final TableItem item, String value) {
	  item.setText(value);
	  item.setText(0, value);
    item.setData(value);
    item.setChecked(f_filter.isPorous(value));
	}
	
	public void filterChanged(Filter filter) {
		if (f_panel.isDisposed())
			return;
		final UIJob job = new SLUIJob() {
			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				updateReport();
				return Status.OK_STATUS;
			}
		};
		job.schedule();
	}

	public void filterQueryFailure(Filter filter, Exception e) {
		SLLogger.getLogger().log(
				Level.SEVERE,
				"query for " + this.getClass().getName() + " failed on "
						+ filter, e);
	}

	public void filterDisposed(Filter filter) {
		dispose();
	}

	public void selectionChanged(TableItem item) {
		/*
		 * The selection changed on a line.
		 */
		f_filter.setPorous(item.getText(), item.getChecked());
		updateReport();
	}
	
	@Override
	public void forceFocus() {
	  f_reportContents.forceFocus();
	  getCascadingList().show(index);
	}

  private void focusOnColumn(MColumn column) {
    if (column != null) {
      column.forceFocus();
    }
  }
  
  @Override 
  void selectAll() {
    if (f_reportContents.isFocusControl()) {
      selectAllItems();
    } else {
      super.selectAll();
    }
  }

  private void selectAllItems() {
    f_filter.setPorousAll();
    for(TableItem item : f_reportContents.getItems()) {
      item.setChecked(true);
    }
  }
}
