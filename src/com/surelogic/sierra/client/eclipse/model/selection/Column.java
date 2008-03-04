package com.surelogic.sierra.client.eclipse.model.selection;

public class Column {
  private final String name;
  private boolean isVisible = false;
  private int width = -1;
  private ColumnSort sort = ColumnSort.UNSORTED;
  private int index = -1;
  
  public Column(String name) {
    this.name = name;
  }
  
  public String getName() {
    return name;
  }

  public boolean isVisible() {
    return isVisible;
  }
  
  public int getWidth() {
    return width;
  }

  public ColumnSort getSort() {
    return sort;
  }
  
  public int getIndex() {
    return index;
  }

  public void configure(boolean viz, int width, ColumnSort sort, int i) {
    if (width < -1) {
      throw new IllegalArgumentException("illegal width: "+width);
    }
    if (sort == null) {
      throw new IllegalArgumentException("null sort");
    }
    if (i < 0) {
      throw new IllegalArgumentException("illegal index: "+i);
    }
    isVisible = viz;
    this.width = width;
    this.sort = sort;
    this.index = i;
  }
}
