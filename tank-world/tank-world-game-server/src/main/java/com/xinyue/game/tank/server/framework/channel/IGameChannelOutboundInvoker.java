package com.xinyue.game.tank.server.framework.channel;

import com.xinyue.game.tank.command.common.IGameCommand;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelPromise;

public interface IGameChannelOutboundInvoker {

	ChannelFuture close();

	ChannelFuture close(ChannelPromise promise);

	ChannelFuture writeCommand(IGameCommand gameCommand);

	ChannelFuture writeCommand(IGameCommand msg, ChannelPromise promise);

	ChannelPromise newPromise();
	
	IGameChannelOutboundInvoker read();
}
