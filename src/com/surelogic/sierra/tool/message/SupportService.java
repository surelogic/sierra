package com.surelogic.sierra.tool.message;

import java.io.File;

import com.surelogic.sierra.message.srpc.Service;

public interface SupportService extends Service {
	SupportReply request(SupportRequest t, File... f);
	
	/*
	 * Register ->
	 * 1) Environment settings (JRE version, IDE version, etc)
	 * 2) User info (name, company, email)
	 * 3) Whether they opt-in to mailing list for announcements (checkbox)
	 * 4) Whether they opt-in to feedback agent (which sends info to SureLogic
     *    automatically when Sierra exceptions occur)
	 */
	// check for updates
	// report errors
	// report usage statistics
}
