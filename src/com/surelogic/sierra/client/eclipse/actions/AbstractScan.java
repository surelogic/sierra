package com.surelogic.sierra.client.eclipse.actions;

import java.util.*;
import java.util.logging.Logger;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.ui.PlatformUI;

import com.surelogic.common.eclipse.BalloonUtility;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.client.eclipse.preferences.PreferenceConstants;

public abstract class AbstractScan<T extends IJavaElement>  {
  /** The logger */
  protected static final Logger LOG = SLLogger.getLogger("sierra");

  private final boolean isRescan;
  
  protected AbstractScan(boolean isRescan) {
    this.isRescan = isRescan;
  }
  
  /**   
   * @return if editors were saved   
   */
  protected boolean trySavingEditors() {
    boolean saved;
    // Bug 1075 Fix - Ask for saving editors
    if (!PreferenceConstants.alwaysSaveResources()) {
      saved = PlatformUI.getWorkbench().saveAllEditors(true);
    } else {
      saved = PlatformUI.getWorkbench().saveAllEditors(false);
    }
    return saved;
  }

  protected StringBuilder computeLabel(final List<String> names) {
    final StringBuilder sb = new StringBuilder(isRescan ? "Re-scanning" : "Scanning ");
    /*
     * Fix for bug 1157. At JPL we encountered 87 projects and
     * the balloon pop-up went off the screen.
     */
    if (names.size() <= 5) {
      boolean first = true;
      for(String name : names) {
        if (first == true) {
          first = false;
        } else {
          sb.append(", ");
        }
        sb.append(name);          
      }
      sb.append(". ");
    } else {
      sb.append(names.size());
      sb.append(isRescan ? " comp units" : " projects");
    }
    return sb;
  }

  protected void scan(List<T> elements, List<String> names) {
    if (elements.size() <= 0) {
      return;
    }
    boolean saved = trySavingEditors();
    
    if (saved) {
      final StringBuilder sb = computeLabel(names); // FIX merge w/ showStartBalloon?
      startScanJob(elements);      
      showStartBalloon(sb);
    } else {
      // Scan not run, because of modified editors
    }
  } 
  
  protected void showStartBalloon(final StringBuilder label) {
    if (PreferenceConstants.showBalloonNotifications()) {
      label.append(". ");
      label.append("You may continue your work. ");
      label.append("You will be notified when the");
      if (isRescan) {
        label.append(" scan has completed.");
        BalloonUtility.showMessage("Sierra scan started", label
            .toString());
      } else {
        label.append(" re-scan has completed.");
        BalloonUtility.showMessage("Sierra re-scan started", label
            .toString());
      }
    }
  }
  
  abstract void startScanJob(List<T> elements);
}
