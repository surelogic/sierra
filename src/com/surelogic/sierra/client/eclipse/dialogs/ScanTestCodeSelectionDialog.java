package com.surelogic.sierra.client.eclipse.dialogs;

import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

import com.surelogic.common.jobs.*;
import com.surelogic.common.eclipse.SLImages;
import com.surelogic.common.images.CommonImages;
import com.surelogic.sierra.tool.message.Config;

public class ScanTestCodeSelectionDialog extends Dialog {
  private final List<Config> configs;
  
  public ScanTestCodeSelectionDialog(Shell parentShell, List<Config> configs) {
    super(parentShell);
    this.configs = configs;
  }
  
  @Override
  protected void configureShell(Shell newShell) {
    super.configureShell(newShell);
    newShell.setImage(SLImages.getImage(CommonImages.IMG_SIERRA_LOGO));
    newShell.setText("Select Test Codes for Scan Jobs");
  }
  
  @Override
  protected Control createDialogArea(Composite parent) {
    final Composite panel = (Composite) super.createDialogArea(parent);
    GridLayout gridLayout = new GridLayout();
    gridLayout.numColumns = 2;
    panel.setLayout(gridLayout);
    
    for(final Config config : configs) {
      Label l = new Label(panel, SWT.NONE);
      l.setText(config.getProject());
      l.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false));
      
      final Combo c = new Combo(panel, /*SWT.READ_ONLY |*/ SWT.DROP_DOWN);
      c.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false));
      for(TestCode code : TestCode.values()) {
        c.add(code.toString());
      }
      c.select(0);
      c.addSelectionListener(new SelectionListener() {
        public void widgetDefaultSelected(SelectionEvent e) {
          widgetSelected(e);
        }
        public void widgetSelected(SelectionEvent e) {
          int selected = c.getSelectionIndex();
          config.setTestCode(c.getItem(selected));
        }        
      });
    }
    return panel;
  }
}
