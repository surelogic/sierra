package com.surelogic.sierra.tool.registration;

import com.surelogic.sierra.message.srpc.Service;

public interface Registration extends Service {
	/**
	 * @return A message to display
	 */
	RegistrationResponse register(ProductRegistrationInfo info);
	ProductInfo checkForUpdates(ProductInfo info);
}
