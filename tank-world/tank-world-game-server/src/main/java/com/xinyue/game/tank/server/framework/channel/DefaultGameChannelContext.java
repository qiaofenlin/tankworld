package com.xinyue.game.tank.server.framework.channel;

import io.netty.util.concurrent.EventExecutor;

public class DefaultGameChannelContext extends GameChannelHandlerContext {

	private GameChannelHandler handler;

	public DefaultGameChannelContext(GameChannelPipeline pipeline, EventExecutor executor, GameChannelHandler handler) {
		super(pipeline, executor);
		this.handler = handler;
	}

	@Override
	public GameChannelHandler getHandler() {
		return handler;
	}

}
