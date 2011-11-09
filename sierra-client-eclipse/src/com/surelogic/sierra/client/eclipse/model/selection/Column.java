package com.surelogic.sierra.client.eclipse.model.selection;

public abstract class Column {
  private final String name;
  protected boolean visible = false;
  protected int width = -1;
  protected ColumnSort sort = ColumnSort.UNSORTED;
  protected int index = -1;
  
  protected Column(String name) {
    this.name = name;
  }
  
  public final String getName() {
    return name;
  }

  public final boolean isVisible() {
    return visible;
  }
  
  public final int getWidth() {
    return width;
  }

  public final ColumnSort getSort() {
    return sort;
  }
  
  public final int getIndex() {
    return index;
  }

  public void setVisible(boolean viz) {
    visible = viz;
  }
  
  public final void configure(boolean viz, int width, ColumnSort sort, int i) {
    if (width < -1) {
      throw new IllegalArgumentException("illegal width: "+width);
    }
    if (sort == null) {
      throw new IllegalArgumentException("null sort");
    }
    if (i < 0) {
      throw new IllegalArgumentException("illegal index: "+i);
    }
    visible = viz;
    this.width = width;
    this.sort = sort;
    this.index = i;
  }
  
  public final void configure(Column other) {
    visible = other.visible;
    width = other.width;
    sort = other.sort;
    index = other.index;
  }
}
