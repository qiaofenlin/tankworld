package com.xinyue.game.tank.server.framework.channel;

import com.xinyue.game.tank.command.common.IGameCommand;

public interface IGameChannelInboundInvoker {

	IGameChannelInboundInvoker fireChannelActive();
	IGameChannelInboundInvoker fireChannelInActive();

	IGameChannelInboundInvoker fireExceptionCaught(Throwable cause);

	IGameChannelInboundInvoker fireReadCommand(IGameCommand msg);

	IGameChannelInboundInvoker fireReadEvent(IGameEvent msg);

}
