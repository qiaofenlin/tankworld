package com.xinyue.game.tank.server.framework.channel;

import com.xinyue.game.tank.command.common.IGameCommand;

public interface IGameChannelInboundHandler extends IGameChannelHandler{

	void channelActive(IGameChannelHandlerContext ctx) throws Exception;

	void channelInActive(IGameChannelHandlerContext ctx);

	void readCommand(IGameChannelHandlerContext ctx, IGameCommand gameCommand);

	void readEvent(IGameChannelHandlerContext ctx, IGameEvent gameEvent);

	void exceptionCaught(IGameChannelHandlerContext ctx, Throwable exp);
}
