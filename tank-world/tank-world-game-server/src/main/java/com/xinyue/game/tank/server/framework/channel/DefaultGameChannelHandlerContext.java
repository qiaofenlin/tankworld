package com.xinyue.game.tank.server.framework.channel;

import io.netty.util.concurrent.EventExecutor;

public class DefaultGameChannelHandlerContext extends AbstractGameChannelHandlerContext {
	private IGameChannelHandler handler;

	DefaultGameChannelHandlerContext(IGameChannelPipeline pipeline, EventExecutor executor, String name,
			IGameChannelHandler handler) {
		super(pipeline, executor, name, isInbound(handler), isOutbound(handler));
		this.handler = handler;
	}

	private static boolean isInbound(IGameChannelHandler handler) {
		return handler instanceof IGameChannelInboundHandler;
	}

	private static boolean isOutbound(IGameChannelHandler handler) {
		return handler instanceof IGameChannelOutboundHandler;
	}

	@Override
	public IGameChannelHandler handler() {
		return handler;
	}

}
