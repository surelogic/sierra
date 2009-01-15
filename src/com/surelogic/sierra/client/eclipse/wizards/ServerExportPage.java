package com.surelogic.sierra.client.eclipse.wizards;

import java.io.File;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.*;

import com.surelogic.common.eclipse.SLImages;
import com.surelogic.common.images.CommonImages;
import com.surelogic.sierra.client.eclipse.model.SierraServer;
import com.surelogic.sierra.client.eclipse.model.ConnectedServerManager;
import com.surelogic.sierra.client.eclipse.model.SierraServerPersistence;

public class ServerExportPage extends AbstractExportWizardPage<SierraServer> {
	private final List<SierraServer> f_SelectedSierraServers = f_selections;

	public ServerExportPage() {
		super("SierraServerExportWizardPage", "Sierra Team Server location"); //$NON-NLS-1$
		setPageComplete(false);
		setTitle("Export Sierra Team Server Locations");
		setDescription("Export the selected Sierra Team Server locations");
	}

	/*
	 * @see IDialogPage#createControl(Composite)
	 */
	public void createControl(Composite parent) {
    setupControl(parent, "Select the Sierra Team Server locations to export:",
        new SierraServerContentProvider(), new SierraServerLabelProvider(),
        SierraServer.class, "sierra-team-server-locations.xml");
	}

  @Override
	protected void initializeTable() {
		Set<SierraServer> servers = ConnectedServerManager.getInstance()
				.getServers();

		f_TableViewer.setInput(servers);
		// Check any necessary projects
		if (f_SelectedSierraServers != null) {
			f_TableViewer.setCheckedElements(f_SelectedSierraServers
					.toArray(new IJavaProject[f_SelectedSierraServers.size()]));
		}
	}

	public boolean exportServers() {
		SierraServerPersistence.export(ConnectedServerManager.getInstance(),
				f_SelectedSierraServers, new File(f_exportFilenameText
						.getText()));
		return true;
	}

	private static class SierraServerContentProvider implements
			IStructuredContentProvider {

		public Object[] getElements(Object inputElement) {
			if (inputElement instanceof Set) {
				Set<?> servers = (Set<?>) inputElement;
				return servers.toArray();
			}
			return null;
		}

		public void dispose() {
			// Nothing to do
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			// Nothing to do
		}
	}

	private static class SierraServerLabelProvider implements ILabelProvider {

		public Image getImage(Object element) {
			if (element instanceof SierraServer) {
				return SLImages.getImage(CommonImages.IMG_SIERRA_SERVER);
			}
			return null;
		}

		public String getText(Object element) {
			if (element instanceof SierraServer) {
				SierraServer holder = (SierraServer) element;
				return holder.getLabel();
			}
			return null;
		}

		public void addListener(ILabelProviderListener listener) {
			// Nothing to do
		}

		public void dispose() {
			// Nothing to do
		}

		public boolean isLabelProperty(Object element, String property) {
			// Nothing to do
			return false;
		}

		public void removeListener(ILabelProviderListener listener) {
			// Nothing to do
		}
	}
}
