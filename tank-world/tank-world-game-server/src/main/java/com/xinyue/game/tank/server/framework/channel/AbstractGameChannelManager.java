package com.xinyue.game.tank.server.framework.channel;

import java.util.HashMap;
import java.util.Map;

import com.xinyue.game.tank.command.common.IGameCommand;

import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.EventExecutorGroup;

public abstract class AbstractGameChannelManager {
	private EventExecutorGroup executorGroup;
	private EventExecutor executor = null;
	private Map<Long, GameChannel> gameChannelMap = new HashMap<>();

	public AbstractGameChannelManager(int threads) {
		executorGroup = new DefaultEventExecutorGroup(threads);
		executor = executorGroup.next();
	}

	private void execute(Runnable task) {
		executor.execute(task);
	}

	public void receiveCommand(IGameCommand command, ICommandSender commandSender) {
		Runnable task = () -> {
			long userId = command.getUserId();
			GameChannel channel = gameChannelMap.get(userId);
			if (channel == null) {
				channel = newGameChannle(executorGroup.next(), commandSender);
				channel.init(userId);
				gameChannelMap.put(userId, channel);
			}
			channel.readCommand(command);
		};
		this.execute(task);
	}

	public abstract GameChannel newGameChannle(EventExecutor executor, ICommandSender commandSender);
}
