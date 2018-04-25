package com.xinyue.game.tank.server.framework.channel;

import io.netty.util.concurrent.EventExecutor;

public abstract class AbstractGameChannel implements GameChannel {

	private ICommandSender commandSender;
	private EventExecutor executor;

	public AbstractGameChannel(EventExecutor executor, ICommandSender commandSender) {
		this.executor = executor;
		this.commandSender = commandSender;
	}

	@Override
	public EventExecutor executor() {
		return executor;
	}
	
	
}
