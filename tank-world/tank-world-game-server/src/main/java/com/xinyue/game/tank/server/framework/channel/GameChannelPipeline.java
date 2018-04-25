package com.xinyue.game.tank.server.framework.channel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinyue.game.tank.command.common.IGameCommand;
import com.xinyue.game.tank.server.framework.GameFuture;
import com.xinyue.game.tank.server.framework.GamePromise;

public class GameChannelPipeline {
	private Logger logger = LoggerFactory.getLogger(GameChannelPipeline.class);
	private GameChannel gameChannel;
	private GameChannelHandlerContext head;
	private GameChannelHandlerContext tail;

	public GameChannelPipeline(GameChannel gameChannel) {
		this.gameChannel = gameChannel;
		head = new HeadContext(this);
		tail = new TailContext(this);
		head.next = tail;
		tail.pre = head;
	}

	public GameChannel channel() {
		return gameChannel;
	}

	public GameChannelPipeline addLast(GameChannelHandler handler) {
		GameChannelHandlerContext newCtx = new DefaultGameChannelContext(this, null, handler);
		addLast0(newCtx);
		return this;
	}

	private void addLast0(GameChannelHandlerContext newCtx) {
		GameChannelHandlerContext prev = tail.pre;
		newCtx.pre = prev;
		newCtx.next = tail;
		prev.next = newCtx;
		tail.pre = newCtx;
	}

	public void fireReadCommand(IGameCommand command) {
		GameChannelHandlerContext.invokeChannelRead(head, command);
	}

	public GameFuture<Object> writeCommand(IGameCommand gameCommand) {
		return tail.fireWriteCommand(gameCommand);
	}

	public GameFuture<Object> writeCommand(IGameCommand command, GamePromise<Object> promise) {
		return tail.fireWriteCommand(command, promise);
	}

	final class HeadContext extends GameChannelHandlerContext implements GameChannelHandler {
		public HeadContext(GameChannelPipeline pipeline) {
			super(pipeline, null);
		}
		@Override
		public GameChannelHandler getHandler(){
			return this;
		}

		@Override
		public void writeCommand(IGameCommand command, GameChannelHandlerContext ctx, GamePromise<Object> promise) {
			ctx.channel().getCommandSender().sendCommand(command);
			
		}

		@Override
		public void readCommand(IGameCommand command, GameChannelHandlerContext ctx) {
			

		}
	}

	final class TailContext extends GameChannelHandlerContext implements GameChannelHandler{

		public TailContext(GameChannelPipeline pipeline) {
			super(pipeline, null);
		}

		@Override
		public void writeCommand(IGameCommand command, GameChannelHandlerContext ctx, GamePromise<Object> promise) {
			
		}

		@Override
		public void readCommand(IGameCommand command, GameChannelHandlerContext ctx) {
			logger.warn("command : {} 未处理", command.getClass().getName());
		}

		@Override
		public GameChannelHandler getHandler() {
			return this;
		}

	}

}
