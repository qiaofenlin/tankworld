package com.xinyue.game.tank.command;

import com.google.protobuf.GeneratedMessage;
import com.google.protobuf.InvalidProtocolBufferException;
import com.xinyue.game.tank.command.common.AbstractGameCommand;
import com.xinyue.game.tank.command.common.CommandType;
import com.xinyue.game.tank.command.common.GameCommand;

@GameCommand(commandId = 1001, commandType = CommandType.RESPONSE)
public class LoginResponse extends AbstractGameCommand {

	@Override
	public LoginRequest newInverse() {
		
		return null;
	}

	@Override
	protected void parseFromBytes(byte[] bytes) throws InvalidProtocolBufferException {
		// TODO Auto-generated method stub

	}

	@Override
	protected GeneratedMessage getGenerateMessage() {
		// TODO Auto-generated method stub
		return null;
	}

}
