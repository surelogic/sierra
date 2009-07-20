package com.surelogic.sierra.client.eclipse.wizards;

import java.io.*;
import java.util.*;
import java.util.zip.*;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IWorkbench;

import com.surelogic.sierra.jdbc.settings.CategoryDO;
import com.surelogic.sierra.jdbc.tool.FindingTypeDO;
import com.surelogic.sierra.tool.ArtifactType;
import com.surelogic.sierra.tool.ToolUtil;

public class ArtifactTypeSetupWizard extends Wizard {
	private final Collection<ArtifactType> types;
	private final List<FindingTypeDO> findingTypes;
	private final List<CategoryDO> categories;
	private ArtifactTypeMappingPage f_mappingPage;
	private FindingTypeSetupPage f_findingTypePage;
	private ManifestLocationPage f_manifestLocationPage;
	private Map<String,List<ArtifactType>> f_plugins;
	
	
	public ArtifactTypeSetupWizard(List<ArtifactType> t, List<FindingTypeDO> ft, 
			                       List<CategoryDO> cats) {
		types = t;
		findingTypes = ft;
		categories = cats;
	}

	public void init(IWorkbench workbench, IStructuredSelection selection) {
		setWindowTitle("Setup Artifact Types");
	}
	
	@Override
	public void addPages() {
		f_mappingPage = new ArtifactTypeMappingPage(types, findingTypes);
		f_findingTypePage = new FindingTypeSetupPage(types, categories);
		
		f_plugins = organizeTypes();
		f_manifestLocationPage = new ManifestLocationPage(f_plugins);
		addPage(f_mappingPage);
		addPage(f_findingTypePage);
		addPage(f_manifestLocationPage);
	}
	
	private Map<String,List<ArtifactType>> organizeTypes() {
		Map<String,List<ArtifactType>> plugins = new HashMap<String,List<ArtifactType>>();
		for(ArtifactType t : types) {
			List<ArtifactType> at = plugins.get(t.plugin);
			if (at == null) {
				at = new ArrayList<ArtifactType>();
				plugins.put(t.plugin, at);
			}
			at.add(t);
		}
		return plugins;
	}
	
	@Override
	public boolean performFinish() {
		// Clear internal state
		for(ArtifactType t : types) {
			if (AbstractArtifactTypePage.DEFAULT.equals(t.getFindingType())) {
				t.setFindingType(null);
			}
		}
		
		Map<String,String> locations = f_manifestLocationPage.getLocationMap();
		for(Map.Entry<String,List<ArtifactType>> plugin : f_plugins.entrySet()) {
			try {
				final File f = new File(locations.get(plugin.getKey()));
				if (f.exists() && f.getName().endsWith(".jar")) {
					// FIX check if it has the expected files??
					final File temp = File.createTempFile("sierra", ".jar");
					final File old = File.createTempFile("oldSierra", ".jar");
					ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(temp));
					ZipEntry ze = new ZipEntry(ToolUtil.SIERRA_MANIFEST);
					zos.putNextEntry(ze);
					writeManifest(zos, plugin.getValue());
					zos.closeEntry();
					
					// Copy the other files
					final boolean moved = f.renameTo(old);
					ZipFile zf = moved ? new ZipFile(old) : new ZipFile(f);
					Enumeration<? extends ZipEntry> e = zf.entries();
					while (e.hasMoreElements()) {
						ze = e.nextElement();
						if (ToolUtil.SIERRA_MANIFEST.equals(ze.getName())) {
							// Already written
							continue;
						}
						copyStream(zos, zf, ze);
					}
					zf.close();
					zos.close();
					
					if (moved || f.renameTo(old)) {
						if (temp.renameTo(f)) {
							old.delete();
						}
					} else {
						boolean renamed = temp.renameTo(new File(f.getParentFile(), f.getName()+".new"));
						//System.out.println(renamed);
					}
				} else {
					OutputStream os = new FileOutputStream(f);
					writeManifest(os, plugin.getValue());
					os.close();
				}
			} catch(IOException e) {
				e.printStackTrace();
				return false;
			}	
		}
		return true;
	}
	
	private void copyStream(ZipOutputStream zos, ZipFile zf, ZipEntry ze) throws IOException {
		InputStream in = zf.getInputStream(ze);
		ZipEntry newE = new ZipEntry(ze.getName());
		zos.putNextEntry(newE);

		// Transfer bytes from in to out
		final byte[] buf = new byte[4096];
		int len;
		while ((len = in.read(buf)) > 0) {
			zos.write(buf, 0, len);
		}
		in.close();
		zos.closeEntry();
	}

	private void writeManifest(final OutputStream os, List<ArtifactType> types) throws IOException {
		Writer w = new OutputStreamWriter(os);
		PrintWriter out = new PrintWriter(w);
		out.println("Manifest-Version: 1.0\n");
		boolean first = true;

		List<ArtifactType> unmapped = new ArrayList<ArtifactType>();
		for(final ArtifactType t : types) {
			final String fType = t.getFindingType();
			if (mappedToFindingType(fType)) {
				if (first) {
					out.println("Name: "+ToolUtil.FINDING_TYPE_MAPPING_KEY);
					first = false;
				}
				out.println(t.type+": "+fType);
			} else {
				unmapped.add(t);
			}
		}
		if (!first) {
			out.println();
		}	

		if (unmapped.size() > 0) {
			out.println("Name: "+ToolUtil.CATEGORY_MAPPING_KEY);
			for(final ArtifactType t : unmapped) {
				out.println(t.type+": "+t.getCategory());			
			}				
			out.println();
		}

		first = true;
		for(final ArtifactType t : unmapped) {
			if (!t.includeInScan()) {
				if (first) {
					out.println("Name: "+ToolUtil.SCAN_FILTER_BLACKLIST_KEY);
					first = false;
				}
				out.println(t.type+": ignore");			
			}
		}
		out.println();
		out.flush();
	}

	private boolean mappedToFindingType(final String fType) {
		return fType != null && !AbstractArtifactTypePage.DEFAULT.equals(fType);
	}
}
