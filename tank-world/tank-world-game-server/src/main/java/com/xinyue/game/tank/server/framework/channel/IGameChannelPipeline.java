package com.xinyue.game.tank.server.framework.channel;

import java.util.Map;
import java.util.Map.Entry;

import com.xinyue.game.tank.command.common.IGameCommand;

public interface IGameChannelPipeline
		extends IGameChannelInboundInvoker, IGameChannelOutboundInvoker, Iterable<Entry<String, IGameChannelHandler>> {

	IGameChannelPipeline addFirst(String name, IGameChannelHandler handler);

	IGameChannelPipeline addLast(String name, IGameChannelHandler handler);

	IGameChannelPipeline addFirst(IGameChannelHandler... handlers);

	IGameChannelPipeline addLast(IGameChannelHandler... handlers);

	IGameChannelPipeline remove(IGameChannelHandler handler);

	IGameChannelHandler remove(String name);

	IGameChannelHandler removeFirst();

	IGameChannelHandler removeLast();

	IGameChannelHandler first();

	IGameChannelHandlerContext firstContext();

	IGameChannelHandler last();

	IGameChannelHandlerContext lastContext();
	IGameChannelHandlerContext context(IGameChannelHandler handler);
	IGameChannelHandlerContext context(String name) ;
	IGameChannelHandler get(String name);

	IGameChannel channel();
	 Map<String, IGameChannelHandler> toMap();


	@Override
	IGameChannelPipeline fireChannelActive();
	
	@Override
	IGameChannelPipeline fireChannelInActive();

	@Override
	IGameChannelPipeline fireExceptionCaught(Throwable cause);

	@Override
	IGameChannelPipeline fireReadCommand(IGameCommand command);

	@Override
	IGameChannelPipeline fireReadEvent(IGameEvent msg);

}
