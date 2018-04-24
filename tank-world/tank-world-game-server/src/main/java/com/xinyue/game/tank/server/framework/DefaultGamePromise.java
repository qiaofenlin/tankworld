package com.xinyue.game.tank.server.framework;

import io.netty.util.concurrent.DefaultPromise;
import io.netty.util.concurrent.EventExecutor;

public class DefaultGamePromise<V> extends DefaultPromise<V> implements GamePromise<V> {
	private GameError error;

	public DefaultGamePromise(EventExecutor executor) {
		super(executor);
	}

	@Override
	public void setError(GameError error) {
		this.error = error;
	}

	@Override
	public GameError getGameError() {
		return error;
	}

}
