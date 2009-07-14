package com.surelogic.sierra.client.eclipse.wizards;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.*;

public class ManifestLocationPage extends WizardPage {
	private String location;
	
	protected ManifestLocationPage() {
		super("ManifestLocationPage");
		setPageComplete(false);
		setTitle("Select Manifest Location");
		setDescription("Select an output location for the generated manifest");
	}

	public void createControl(Composite parent) {
		final Composite panel = new Composite(parent, SWT.NONE);
		panel.setLayout(new RowLayout(SWT.HORIZONTAL));
		
		final Label l = new Label(panel, SWT.WRAP);
		
		final Button b = new Button(panel, SWT.NONE);
		b.setText("Browse");
		b.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}

			public void widgetSelected(SelectionEvent e) {
		        FileDialog fd = new FileDialog(getShell(), SWT.SAVE);
		        fd.setText("Save As");		        
		        //fd.setFilterPath("C:/");
		        //String[] filterExt = { "*.txt", "*.doc", ".rtf", "*.*" };
		        //fd.setFilterExtensions(filterExt);
		        String path = fd.open();
		        if (path != null) {
		        	location = path;
		        	l.setText(path);
		        	l.pack();
		        	setPageComplete(true);
		        }
			}			
		});		
		panel.pack();
		setControl(panel);
	}
	
	public String getLocation() {
		return location;
	}
	/* Causes stack overflow
	@Override
	public boolean isPageComplete() {
		setPageComplete(location != null && new File(location).isDirectory()); 
		return super.isPageComplete();
	}
	*/
}
