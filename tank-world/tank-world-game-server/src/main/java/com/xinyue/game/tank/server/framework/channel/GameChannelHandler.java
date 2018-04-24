package com.xinyue.game.tank.server.framework.channel;

import com.xinyue.game.tank.command.common.IGameCommand;
import com.xinyue.game.tank.server.framework.GamePromise;

public interface GameChannelHandler {

	void writeCommand(IGameCommand command, GameChannelHandlerContext ctx, GamePromise<Object> promise);

	void readCommand(IGameCommand comamnd, GameChannelHandlerContext ctx);

}
