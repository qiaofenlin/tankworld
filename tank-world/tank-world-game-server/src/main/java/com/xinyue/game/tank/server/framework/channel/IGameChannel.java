package com.xinyue.game.tank.server.framework.channel;

import com.xinyue.game.tank.command.common.IGameCommand;

import io.netty.channel.ChannelPromise;
import io.netty.util.concurrent.EventExecutor;

public interface IGameChannel {

	EventExecutor executor();

	void readCommand(IGameCommand command);

	void readEvent(IGameEvent gameEvent);

	void writeCommand(IGameCommand command);
	Unsafe unsafe();

	interface Unsafe {
		/**
		 * Close the {@link Channel} of the {@link ChannelPromise} and notify
		 * the {@link ChannelPromise} once the operation was complete.
		 */
		void close(ChannelPromise promise);

		/**
		 * Schedules a write operation.
		 */
		void write(IGameCommand msg, ChannelPromise promise);

		/**
		 * Flush out all write operations scheduled via
		 * {@link #write(Object, ChannelPromise)}.
		 */
		void flush();
	}

}
