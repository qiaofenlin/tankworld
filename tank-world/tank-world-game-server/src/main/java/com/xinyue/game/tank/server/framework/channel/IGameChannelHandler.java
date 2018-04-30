package com.xinyue.game.tank.server.framework.channel;

import io.netty.channel.ChannelHandler;

public interface IGameChannelHandler {
	  /**
     * Gets called after the {@link ChannelHandler} was added to the actual context and it's ready to handle events.
     */
    void handlerAdded(IGameChannelHandlerContext ctx) throws Exception;

    /**
     * Gets called after the {@link ChannelHandler} was removed from the actual context and it doesn't handle events
     * anymore.
     */
    void handlerRemoved(IGameChannelHandlerContext ctx) throws Exception;
}
