package com.surelogic.sierra.tool.registration;

import com.surelogic.sierra.message.srpc.Service;

@Service(version = "1.0")
public interface Registration {
	/**
	 * @return A message to display
	 */
	RegistrationResponse register(ProductRegistrationInfo info);

	ProductInfo checkForUpdates(ProductInfo info);
}
