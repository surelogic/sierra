package com.surelogic.sierra.message.srpc;

import java.lang.reflect.Method;

class MethodInvocation {

	private final Method m;

	private final Object[] args;

	public MethodInvocation(Method m, Object[] args) {
		this.m = m;
		this.args = args;
	}

	public Method getMethod() {
		return m;
	}

	public Object[] getArgs() {
		return args;
	}

	public Object invoke(Object target) throws SRPCException {
		try {
			return m.invoke(target, args);
		} catch (Exception e) {
			throw new SRPCException(e);
		}
	}

}
