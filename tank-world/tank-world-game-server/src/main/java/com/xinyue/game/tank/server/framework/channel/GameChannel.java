package com.xinyue.game.tank.server.framework.channel;

import com.xinyue.game.tank.command.common.IGameCommand;
import com.xinyue.game.tank.server.framework.GameFuture;
import com.xinyue.game.tank.server.framework.GamePromise;

import io.netty.channel.ChannelHandlerContext;
import io.netty.util.concurrent.EventExecutor;

public interface GameChannel {
	EventExecutor executor();

	ChannelHandlerContext getCtx();

	void setCtx(ChannelHandlerContext ctx);

	void readCommand(IGameCommand command);

	void writeCommand(IGameCommand command);

	GameFuture<Object> writeCommand(IGameCommand command, GamePromise<Object> promise);

	void writeEvent(Object event);

	GameFuture<Object> writeEvent(Object event, GamePromise<Object> obj);
}
