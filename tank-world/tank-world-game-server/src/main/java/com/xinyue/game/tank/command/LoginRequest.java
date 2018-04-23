package com.xinyue.game.tank.command;

import com.google.protobuf.GeneratedMessage;
import com.google.protobuf.InvalidProtocolBufferException;
import com.xinyue.game.tank.command.common.AbstractGameCommand;
import com.xinyue.game.tank.command.common.CommandType;
import com.xinyue.game.tank.command.common.GameCommand;

@GameCommand(commandId = 1001, commandType = CommandType.REQUEST)
public class LoginRequest extends AbstractGameCommand {

	@Override
	public LoginResponse newInverse() {
		LoginResponse inverse = new LoginResponse();
		this.setInverse(inverse);
		return inverse;
	}

	@Override
	protected void parseFromBytes(byte[] bytes) throws InvalidProtocolBufferException {

	}

	@Override
	protected GeneratedMessage getGenerateMessage() {
		return null;
	}

}
