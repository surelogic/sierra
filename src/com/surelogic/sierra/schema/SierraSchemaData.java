package com.surelogic.sierra.schema;

import java.io.InputStream;
import java.net.URL;

import com.surelogic.common.license.SLLicenseProduct;
import com.surelogic.flashlight.schema.AbstractSchemaData;

public class SierraSchemaData extends AbstractSchemaData {
	public SierraSchemaData() {
		super("com.surelogic.sierra.schema",
			  Thread.currentThread().getContextClassLoader(),
			  SLLicenseProduct.SIERRA);
	}

	protected Object newInstance(String qname) 
	throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		return loader.loadClass(qname).newInstance();
	}

	public URL getSchemaResource(final String name) {
		return loader.getResource(getSchemaResourcePath(name));
	}
	
	protected InputStream getResourceAsStream(String path) {
		return loader.getResourceAsStream(path);
	}
}
