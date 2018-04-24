package com.xinyue.game.tank.server.framework;

import io.netty.util.concurrent.Future;

public interface GameFuture<V> extends Future<V> {

	GameError getGameError();
}
