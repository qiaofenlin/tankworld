package com.xinyue.game.tank.server.framework;

import io.netty.util.concurrent.Promise;

public interface GamePromise<V> extends Promise<V>,GameFuture<V> {
	void setError(GameError error);
}
