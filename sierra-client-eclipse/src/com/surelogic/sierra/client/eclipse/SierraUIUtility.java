package com.surelogic.sierra.client.eclipse;

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
import com.surelogic.common.jdbc.NullDBQuery;
import com.surelogic.common.jdbc.NullResultHandler;
import com.surelogic.common.jdbc.Query;
import com.surelogic.common.jdbc.Result;
import com.surelogic.common.jdbc.Row;
import com.surelogic.common.jdbc.StatementException;
import com.surelogic.common.ui.JDTUIUtility;
import com.surelogic.common.ui.SLImages;
import com.surelogic.common.ui.jobs.SLUIJob;
import com.surelogic.sierra.client.eclipse.jobs.AbstractSierraDatabaseJob;
import com.surelogic.sierra.jdbc.finding.ArtifactDetail;
import com.surelogic.sierra.tool.message.IdentifierType;
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
    if (importance == Importance.IRRELEVANT) {
      imageName = CommonImages.IMG_ASTERISK_ORANGE_0;
    } else if (importance == Importance.LOW) {
      imageName = CommonImages.IMG_ASTERISK_ORANGE_25;
    } else if (importance == Importance.MEDIUM) {
      imageName = CommonImages.IMG_ASTERISK_ORANGE_50;
    } else if (importance == Importance.HIGH) {
      imageName = CommonImages.IMG_ASTERISK_ORANGE_75;
    } else {
      imageName = CommonImages.IMG_ASTERISK_ORANGE_100;
    }
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
    if (jdtType == null) {
      return SLImages.getImage(CommonImages.IMG_CLASS);
    } else {
      return SLImages.getImageFor(jdtType);
    }
  }

  /**
   * Makes a best effort to lookup a more precise location in the class for a
   * particular finding identifier. Sierra uses this method to open the Java
   * editor when a finding id has 0 for the source line of code. If possible a
   * method or field declaration is opened. If nothing can be found the class is
   * opened.
   *
   * @param projectName
   *          a project name.
   * @param packageName
   *          a package name.
   * @param typeName
   *          a type name
   * @param lineNumber
   *          a line number (use findingId if 0).
   * @param findingId
   *          a finding identifier (to use for querying)
   */
  public static void tryToOpenInEditor(final String projectName, final String packageName, final String typeName,
      final int lineNumber, final long findingId) {
    if (lineNumber > 0)
      if (JDTUIUtility.tryToOpenInEditor(projectName, packageName, typeName, lineNumber))
        return;
    final Job job = new AbstractSierraDatabaseJob("Querying details of finding " + findingId) {
      @Override
      protected IStatus run(final IProgressMonitor monitor) {
        monitor.beginTask("Looking up declaration information on " + findingId, IProgressMonitor.UNKNOWN);
        try {
          Data.getInstance().withReadOnly(new NullDBQuery() {
            boolean isField = false;
            boolean isMethod = false;
            String declName = "";

            @Override
            public void doPerform(Query q) {
              q.prepared("ViewSource.findIdentifiers", new NullResultHandler() {
                @Override
                protected void doHandle(Result result) {
                  for (Row r : result) {
                    String id = r.nextString();
                    if (id != null) {
                      IdentifierType type = IdentifierType.valueOf(r.nextString());
                      if (type == IdentifierType.FIELD) {
                        isField = true;
                        isMethod = false;
                        declName = id;
                        return;
                      } else if (type == IdentifierType.METHOD) {
                        isMethod = true;
                        declName = id;
                        return;
                      }
                    }
                  }
                }
              }).call(findingId);
              if (!isField && !isMethod) {
                q.prepared("ViewSource.findIdentifiersBySecondarySources", new NullResultHandler() {
                  @Override
                  protected void doHandle(Result result) {
                    for (Row r : result) {
                      String id = r.nextString();
                      if (id != null) {
                        IdentifierType type = IdentifierType.valueOf(r.nextString());
                        if (type == IdentifierType.FIELD) {
                          isField = true;
                          isMethod = false;
                          declName = id;
                          return;
                        } else if (type == IdentifierType.METHOD) {
                          isMethod = true;
                          declName = id;
                          return;
                        }
                      }
                    }
                  }
                }).call(findingId);
              }
              final SLUIJob uiJob = new SLUIJob() {

                @Override
                public IStatus runInUIThread(IProgressMonitor monitor) {
                  if (isField) {
                    if (JDTUIUtility.tryToOpenInEditorUsingFieldName(projectName, packageName, typeName, declName)) {
                      return Status.OK_STATUS;
                    }
                  }

                  if (isMethod) {
                    if (JDTUIUtility.tryToOpenInEditorUsingMethodName(projectName, packageName, typeName, declName)) {
                      return Status.OK_STATUS;
                    }
                  }

                  // just open the type
                  JDTUIUtility.tryToOpenInEditor(projectName, packageName, typeName);

                  return Status.OK_STATUS;
                }
              };
              uiJob.schedule();

            }
          });
        } catch (StatementException e) {
          final int errNo = 57;
          final String msg = I18N.err(errNo, findingId);
          return SLEclipseStatusUtility.createErrorStatus(errNo, msg, e);
        }
        monitor.done();
        return Status.OK_STATUS;

      }
    };
    job.schedule();
  }

  /**
   * Makes a best effort to lookup a more precise location in the class for a
   * particular artifact. Sierra uses this method to open the Java editor when a
   * finding id has 0 for the source line of code. If possible a method or field
   * declaration is opened. If nothing can be found the class is opened.
   *
   * @param projectName
   *          a project name.
   * @param packageName
   *          a package name.
   * @param typeName
   *          a type name
   * @param lineNumber
   *          a line number (use artifact detail if 0).
   * @param artifactDetail
   *          the details about an artifact.
   */
  public static void tryToOpenInEditor(final String projectName, final String packageName, final String typeName,
      final int lineNumber, final ArtifactDetail artifactDetail) {
    if (lineNumber > 0)
      if (JDTUIUtility.tryToOpenInEditor(projectName, packageName, typeName, lineNumber))
        return;

    // TODO try use the passed artifact detail

    // worst case -- just open the type
    JDTUIUtility.tryToOpenInEditor(projectName, packageName, typeName);
  }
}
