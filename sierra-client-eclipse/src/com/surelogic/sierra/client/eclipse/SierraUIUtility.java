package com.surelogic.sierra.client.eclipse;

import java.sql.Connection;
import java.sql.SQLException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IType;
import org.eclipse.swt.graphics.Image;

import com.surelogic.NonNull;
import com.surelogic.Nullable;
import com.surelogic.Utility;
import com.surelogic.common.CommonImages;
import com.surelogic.common.core.JDTUtility;
import com.surelogic.common.core.logging.SLEclipseStatusUtility;
import com.surelogic.common.i18n.I18N;
import com.surelogic.common.ui.JDTUIUtility;
import com.surelogic.common.ui.SLImages;
import com.surelogic.common.ui.jobs.SLUIJob;
import com.surelogic.sierra.client.eclipse.jobs.AbstractSierraDatabaseJob;
import com.surelogic.sierra.tool.message.Importance;

@Utility
public final class SierraUIUtility {

  static public final String MULTIPLE_TOOLS = "(From Multiple Tools)";

  private SierraUIUtility() {
    // no instances
  }

  @NonNull
  public static Image getImageFor(Importance importance) {
    final String imageName;
    if (importance == Importance.IRRELEVANT)
      imageName = CommonImages.IMG_ASTERISK_ORANGE_0;
    else if (importance == Importance.LOW)
      imageName = CommonImages.IMG_ASTERISK_ORANGE_25;
    else if (importance == Importance.MEDIUM)
      imageName = CommonImages.IMG_ASTERISK_ORANGE_50;
    else if (importance == Importance.HIGH)
      imageName = CommonImages.IMG_ASTERISK_ORANGE_75;
    else
      imageName = CommonImages.IMG_ASTERISK_ORANGE_100;
    return SLImages.getImage(imageName);
  }

  @NonNull
  public static Image getImageForTool(String toolName) {
    String imageName = CommonImages.IMG_UNKNOWN;
    if ("FindBugs".equals(toolName)) {
      imageName = CommonImages.IMG_FINDBUGS_FINDING;
    } else if ("PMD".equals(toolName)) {
      imageName = CommonImages.IMG_PMD_FINDING;
    } else if ("CPD".equals(toolName)) {
      imageName = CommonImages.IMG_EDIT_CUT;
    } else if (MULTIPLE_TOOLS.equals(toolName)) {
      return SLImages.getGrayscaleImage(CommonImages.IMG_SIERRA_LOGO);
    }
    return SLImages.getImage(imageName);
  }

  /**
   * Tries to get the Java image for the passed information in the Eclipse
   * workspace, but just returns a public class image if it fails.
   * 
   * @param optionalProject
   *          a project name or {@code null} to search all projects.
   * @param packageName
   *          a package name.
   * @param typeName
   *          a type name.
   * @return an appropriate image.
   */
  @NonNull
  public static Image getImageForType(@Nullable String optionalProject, String packageName, String typeName) {
    IType jdtType = JDTUtility.findIType(optionalProject, packageName, typeName);
    if (jdtType == null)
      return SLImages.getImage(CommonImages.IMG_CLASS);
    else
      return SLImages.getImageFor(jdtType);
  }

  /**
   * 
   * @param projectName
   * @param packageName
   * @param typeName
   * @param findingId
   */
  public static void tryToOpenInEditor(final String projectName, final String packageName, final String typeName,
      final long findingId) {
    final Job job = new AbstractSierraDatabaseJob("Querying details of finding " + findingId) {
      @Override
      protected IStatus run(final IProgressMonitor monitor) {
        monitor.beginTask("Looking up declaration information on " + findingId, IProgressMonitor.UNKNOWN);
        try {
          final Connection c = Data.getInstance().readOnlyConnection();
          try {
            boolean isField = false;
            boolean isMethod = false;
            String declName = "";

            // TODO QUERY AND FILL IN DATA

            final boolean uiIsField = isField;
            final boolean uiIsMethod = isMethod;
            final String uiDeclName = declName;
            final SLUIJob uiJob = new SLUIJob() {

              @Override
              public IStatus runInUIThread(IProgressMonitor monitor) {
                if (uiIsField)
                  if (JDTUIUtility.tryToOpenInEditorUsingFieldName(projectName, packageName, typeName, uiDeclName))
                    return Status.OK_STATUS;

                if (uiIsMethod)
                  if (JDTUIUtility.tryToOpenInEditorUsingMethodName(projectName, packageName, typeName, uiDeclName))
                    return Status.OK_STATUS;

                // just open the type
                JDTUIUtility.tryToOpenInEditor(projectName, packageName, typeName);

                return Status.OK_STATUS;
              }
            };
          } finally {
            c.close();
          }
          monitor.done();
          return Status.OK_STATUS;
        } catch (final SQLException e) {
          final int errNo = 57;
          final String msg = I18N.err(errNo, findingId);
          return SLEclipseStatusUtility.createErrorStatus(errNo, msg, e);
        }
      }
    };
    job.schedule();

  }
}
