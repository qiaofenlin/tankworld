package com.xinyue.game.tank.server.framework.channel;

import com.xinyue.game.tank.command.common.IGameCommand;

import io.netty.channel.AbstractChannel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelPromise;
import io.netty.channel.DefaultChannelPipeline;
import io.netty.channel.EventLoop;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

public class GameChannel implements IGameChannel {
	private static final InternalLogger logger = InternalLoggerFactory.getInstance(AbstractChannel.class);

	private DefaultGameChannelPipeline channelPipeline;
	private Object channelId;
	private EventLoop eventLoop;
	private IGameChannelDispatcher channelDispatcher;

	public GameChannel(Object channelId, IGameChannelDispatcher channelDispatcher) {
		this.channelId = channelId;
		channelPipeline = this.newChannelPipeline();
		this.channelDispatcher = channelDispatcher;
	}

	@Override
	public Object getChannelId() {
		return channelId;
	}
	@Override
	public IGameChannelDispatcher getGameChannelDispatcher() {
		return this.channelDispatcher;
	}
	/**
	 * Returns a new {@link DefaultChannelPipeline} instance.
	 */
	protected DefaultGameChannelPipeline newChannelPipeline() {
		return new DefaultGameChannelPipeline(this);
	}

	@Override
	public IGameChannelPipeline pipeline() {
		return channelPipeline;
	}

	@Override
	public ChannelFuture close() {
		return channelPipeline.close();
	}

	@Override
	public ChannelFuture close(ChannelPromise promise) {
		return channelPipeline.close(promise);
	}

	@Override
	public ChannelFuture writeCommand(IGameCommand gameCommand) {
		return channelPipeline.writeCommand(gameCommand);
	}

	@Override
	public ChannelFuture writeCommand(IGameCommand msg, ChannelPromise promise) {
		return channelPipeline.writeCommand(msg, promise);
	}

	@Override
	public ChannelPromise newPromise() {
		return channelPipeline.newPromise();
	}

	@Override
	public EventLoop eventLoop() {
		EventLoop eventLoop = this.eventLoop;
		if (eventLoop == null) {
			throw new IllegalStateException("channel not registered to an event loop");
		}
		return eventLoop;
	}

	@Override
	public IGameChannel read() {
		channelPipeline.read();
		return this;
	}

	@Override
	public boolean isActive() {
		return false;
	}
	
	
	public class GameChannelUnsafe implements Unsafe{

		@Override
		public void close() {
			
		}

		@Override
		public void writeCommand(IGameCommand gameCommand) {
			// TODO Auto-generated method stub
			
		}
		
	}


	@Override
	public Unsafe getUnsafe() {
		return null;
	}



}
