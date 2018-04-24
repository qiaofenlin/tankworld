package com.xinyue.game.tank.server.framework;

public interface GamePromise<V> extends GameFuture<V> {
	void setError(GameError error);
}
