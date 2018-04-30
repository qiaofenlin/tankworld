package com.xinyue.game.tank.server.framework.channel;

import com.xinyue.game.tank.command.common.IGameCommand;

import io.netty.util.concurrent.EventExecutor;

public interface IGameChannelHandlerContext extends IGameChannelInboundInvoker, IGameChannelOutboundInvoker {

	IGameChannel channel();

	String name();

	EventExecutor executor();

	IGameChannelHandler handler();

	@Override
	IGameChannelHandlerContext fireChannelActive();
	@Override
	IGameChannelHandlerContext fireChannelInActive();

	@Override
	IGameChannelHandlerContext fireExceptionCaught(Throwable cause);

	@Override
	IGameChannelHandlerContext fireReadCommand(IGameCommand msg);

	@Override
	IGameChannelHandlerContext fireReadEvent(IGameEvent msg);
	
	@Override
	IGameChannelHandlerContext read();

	IGameChannelPipeline pipeline();

}
