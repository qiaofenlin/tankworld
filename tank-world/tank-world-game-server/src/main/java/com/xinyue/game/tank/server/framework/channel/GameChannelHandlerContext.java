package com.xinyue.game.tank.server.framework.channel;

import com.xinyue.game.tank.command.common.IGameCommand;
import com.xinyue.game.tank.server.framework.DefaultGamePromise;
import com.xinyue.game.tank.server.framework.GameFuture;
import com.xinyue.game.tank.server.framework.GamePromise;

import io.netty.util.concurrent.EventExecutor;

public abstract class GameChannelHandlerContext {
	GameChannelHandlerContext next;
	GameChannelHandlerContext pre;


	private GameChannelPipeline pipeline;

	private EventExecutor executor;

	public GameChannelHandlerContext(GameChannelPipeline pipeline, EventExecutor executor) {
		this.executor = executor;
		this.pipeline = pipeline;
	}
	public abstract GameChannelHandler getHandler();

	public GameChannel channel() {
		return pipeline.channel();
	}

	public GameChannelPipeline pipeline() {
		return this.pipeline;
	}

	public EventExecutor executor() {
		if (executor == null) {
			return channel().executor();
		} else {
			return executor;
		}
	}

	public GameChannelHandlerContext fireReadCommand(IGameCommand command) {
		invokeChannelRead(next,command);
		return this;
	}

	static void invokeChannelRead(final GameChannelHandlerContext next, IGameCommand msg) {
		EventExecutor executor = next.executor();
		if (executor.inEventLoop()) {
			next.invokeChannelRead(msg);
		} else {
			executor.execute(new Runnable() {
				@Override
				public void run() {
					next.invokeChannelRead(msg);
				}
			});
		}
	}

	private void invokeChannelRead(IGameCommand msg) {
		getHandler().readCommand(msg, this);
	}

	private GamePromise<Object> newPromise() {
		return new DefaultGamePromise<>(executor());
	}

	public GameFuture<Object> fireWriteCommand(IGameCommand command) {
		return this.fireWriteCommand(command, this.newPromise());
	}

	public GameFuture<Object> fireWriteCommand(IGameCommand command, GamePromise<Object> promise) {
		write(command, promise);
		return promise;
	}

	private void write(IGameCommand msg, GamePromise<Object> promise) {
		GameChannelHandlerContext next = pre;
		EventExecutor executor = next.executor();
		if (executor.inEventLoop()) {
			next.invokeChannelWrite(msg, promise);
		} else {
			executor.execute(() -> {
				next.invokeChannelWrite(msg, promise);
			});
		}
	}

	private void invokeChannelWrite(IGameCommand msg, GamePromise<Object> promise) {
		getHandler().writeCommand(msg, this, promise);
	}

}
