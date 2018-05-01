package com.xinyue.game.tank.server.framework.channel;

import com.xinyue.game.tank.command.common.IGameCommand;

import io.netty.channel.EventLoop;

public interface IGameChannel extends IGameChannelOutboundInvoker {

	Object getChannelId();

	EventLoop eventLoop();

	IGameChannelPipeline pipeline();

	@Override
	IGameChannel read();

	boolean isActive();

	Unsafe getUnsafe();

	IGameChannelDispatcher getGameChannelDispatcher();

	public interface Unsafe {
		void close();
		void writeCommand(IGameCommand gameCommand);
	}

}
