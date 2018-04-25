package com.xinyue.game.tank.server.framework.channel;

import com.xinyue.game.tank.command.common.IGameCommand;

public interface ICommandSender {

	void sendCommand(IGameCommand command);
	
}
