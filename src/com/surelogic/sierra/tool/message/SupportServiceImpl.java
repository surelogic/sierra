package com.surelogic.sierra.tool.message;

import java.io.File;

import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.message.srpc.SRPCServlet;

public class SupportServiceImpl extends SRPCServlet implements SupportService {
	private static final long serialVersionUID = 4144761373500031238L;
	static {
		SLLogger.getLogger().warning("Starting SupportService");
	}
	
	public SupportReply request(SupportRequest r, File... files) {
		SLLogger.getLogger().warning("SupportService.request()");
		/*
		for (File f : files) {
			t.getPairs().put(f.getName(), Long.toString(f.length()));
		}
		return t;
		*/
		switch (r.getType()) {
		case REGISTER:
		case UPDATE:
		case USAGE:
		case ERROR:
			break;
		default:
			return new SupportReply("Unknown request type");
		}
		return new SupportReply("OK");
	}

}
