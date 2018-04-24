package com.xinyue.game.tank.server.framework;

import java.lang.reflect.Method;

public class MethodInvokerInfo {

	private Object object;
	private Method method;

	public MethodInvokerInfo(Object object, Method method) {
		super();
		this.object = object;
		this.method = method;
	}

	public Object getObject() {
		return object;
	}

	public Method getMethod() {
		return method;
	}

}
