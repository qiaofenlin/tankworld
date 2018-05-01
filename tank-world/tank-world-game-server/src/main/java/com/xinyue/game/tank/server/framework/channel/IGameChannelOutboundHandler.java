package com.xinyue.game.tank.server.framework.channel;

import com.xinyue.game.tank.command.common.IGameCommand;

import io.netty.channel.ChannelPromise;

public interface IGameChannelOutboundHandler extends IGameChannelHandler {

	void close(IGameChannelHandlerContext ctx, ChannelPromise promise) throws Exception;

	void read(IGameChannelHandlerContext ctx) throws Exception;

	void write(IGameChannelHandlerContext ctx, IGameCommand msg, ChannelPromise promise) throws Exception;

}
