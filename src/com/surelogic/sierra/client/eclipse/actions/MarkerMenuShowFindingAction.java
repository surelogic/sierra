package com.surelogic.sierra.client.eclipse.actions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IVerticalRulerInfo;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.AbstractMarkerAnnotationModel;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.ITextEditorExtension;
import org.eclipse.ui.texteditor.IUpdate;

import com.surelogic.common.eclipse.ViewUtility;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.client.eclipse.dialogs.FindingListDialog;
import com.surelogic.sierra.client.eclipse.views.FindingDetailsView;

/**
 * Refer to MarkerRulerAction from FindBugs eclipse plugin
 * 
 */
public class MarkerMenuShowFindingAction implements IUpdate,
		IEditorActionDelegate, MouseListener, IMenuListener {

	private final List<IMarker> f_markers;
	private ITextEditor f_editor;
	private IVerticalRulerInfo f_ruler;
	private IAction f_action;

	public MarkerMenuShowFindingAction() {
		f_markers = new ArrayList<IMarker>();
	}

	public void update() {

		if (f_markers.size() > 0) {
			try {
				if (f_markers.size() == 1) {

					String s = (String) f_markers.get(0).getAttribute(
							"findingid");
					FindingDetailsView view = (FindingDetailsView) ViewUtility
							.showView(FindingDetailsView.ID);
					view.findingSelected(Long.parseLong(s));

					// System.out.println(o);

				} else {

					Map<Long, String> findingsMap = new HashMap<Long, String>();
					for (IMarker m : f_markers) {
						String text = (String) m.getAttribute(IMarker.MESSAGE);
						String s = (String) m.getAttribute("findingid");
						Long id = Long.parseLong(s);
						findingsMap.put(id, text);
					}

					FindingListDialog fld = new FindingListDialog(PlatformUI
							.getWorkbench().getActiveWorkbenchWindow()
							.getShell(), findingsMap);

					fld.setBlockOnOpen(true);
					fld.open();
				}

			} catch (CoreException e) {
				SLLogger.getLogger().log(Level.SEVERE,
						"Error getting the findingid", e);
			}
		}

	}

	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
		Control control;
		if (f_editor != null) {
			if (f_ruler != null) {
				control = f_ruler.getControl();
				if (control != null && !control.isDisposed()) {
					control.removeMouseListener(this);
				}
			}
			if (f_editor instanceof ITextEditorExtension) {
				((ITextEditorExtension) f_editor)
						.removeRulerContextMenuListener(this);
			}
		}
		if (targetEditor instanceof ITextEditor) {
			f_editor = (ITextEditor) targetEditor;
			if (f_editor instanceof ITextEditorExtension) {
				((ITextEditorExtension) f_editor)
						.addRulerContextMenuListener(this);
			}
			f_ruler = (IVerticalRulerInfo) f_editor
					.getAdapter(IVerticalRulerInfo.class);
			if (f_ruler != null) {
				control = f_ruler.getControl();
				if (control != null && !control.isDisposed()) {
					control.addMouseListener(this);
				}
			}
		} else {
			f_ruler = null;
			f_editor = null;
		}

	}

	public void run(IAction action) {
		f_action = action;
		obtainSierraMarkers();
		if (f_markers.size() > 0) {
			update();
		}

	}

	private void obtainSierraMarkers() {
		f_markers.clear();
		if (f_editor == null || f_ruler == null) {
			return;
		}
		IMarker[] allMarkers = null;
		IResource resource = extractResource(f_editor);
		try {
			allMarkers = resource.findMarkers(MarkersHandler.SIERRA_MARKER,
					true, IResource.DEPTH_ZERO);
		} catch (CoreException e) {
			SLLogger.getLogger().log(
					Level.SEVERE,
					"Error when trying to retrieve markers for sierra "
							+ resource.getName(), e);
		}

		if (allMarkers != null) {
			AbstractMarkerAnnotationModel model = getModel();
			IDocument document = getDocument();
			for (int i = 0; i < allMarkers.length; i++) {
				if (includesRulerLine(model.getMarkerPosition(allMarkers[i]),
						document)) {
					f_markers.add(allMarkers[i]);
				}
			}
		}

	}

	private boolean includesRulerLine(Position markerPosition,
			IDocument document) {
		if (markerPosition != null) {
			try {
				int markerLine = document.getLineOfOffset(markerPosition
						.getOffset());
				int line = f_ruler.getLineOfLastMouseButtonActivity();
				if (line == markerLine) {
					return true;
				}
			} catch (BadLocationException x) {
				SLLogger
						.getLogger()
						.log(
								Level.SEVERE,
								"Error inspecting markers to find FindBugs warnings",
								x);
			}
		}
		return false;
	}

	private IDocument getDocument() {
		IDocumentProvider provider = f_editor.getDocumentProvider();
		return provider.getDocument(f_editor.getEditorInput());
	}

	private AbstractMarkerAnnotationModel getModel() {
		IDocumentProvider provider = f_editor.getDocumentProvider();
		IAnnotationModel model = provider.getAnnotationModel(f_editor
				.getEditorInput());
		if (model instanceof AbstractMarkerAnnotationModel) {
			return (AbstractMarkerAnnotationModel) model;
		}
		return null;
	}

	public void selectionChanged(IAction action, ISelection selection) {
		f_action = action;

	}

	public void mouseDoubleClick(MouseEvent e) {
		// Nothing to do

	}

	public void mouseDown(MouseEvent e) {
		// if (e.button == 1) {
		// obtainSierraMarkers();
		// if (f_markers.size() > 0) {
		// update();
		// }
		// }

	}

	public void mouseUp(MouseEvent e) {
		if (e.button == 1) {
			obtainSierraMarkers();
			if (f_markers.size() > 0) {
				update();
			}
		}

	}

	public void menuAboutToShow(IMenuManager manager) {
		if (f_action != null) {
			obtainSierraMarkers();
			f_action.setEnabled((f_markers.size() > 0));
		}

	}

	/**
	 * Refer to
	 * 
	 * http://wiki.eclipse.org/FAQ_How_do_I_create_problem_markers_for_my_compiler%3F
	 * 
	 * @param editor
	 * @return
	 */
	private IResource extractResource(IEditorPart editor) {
		IEditorInput input = editor.getEditorInput();
		if (!(input instanceof IFileEditorInput))
			return null;
		return ((IFileEditorInput) input).getFile();
	}

}
