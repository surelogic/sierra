package com.surelogic.sierra.schema;

import java.io.InputStream;
import java.net.URL;

import com.surelogic.common.jdbc.AbstractSchemaData;
import com.surelogic.common.license.SLLicenseProduct;

public class SierraSchemaData extends AbstractSchemaData {
	public SierraSchemaData() {
		super("com.surelogic.sierra.schema",
			  Thread.currentThread().getContextClassLoader(),
			  SLLicenseProduct.SIERRA);
	}

	@Override
  protected Object newInstance(String qname) 
	throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		return loader.loadClass(qname).newInstance();
	}

	@Override
  public URL getSchemaResource(final String name) {
		return loader.getResource(getSchemaResourcePath(name));
	}
	
	@Override
  protected InputStream getResourceAsStream(String path) {
		return loader.getResourceAsStream(path);
	}
}
