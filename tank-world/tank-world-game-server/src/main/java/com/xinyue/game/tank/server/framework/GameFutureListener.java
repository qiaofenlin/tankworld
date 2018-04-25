package com.xinyue.game.tank.server.framework;

import io.netty.util.concurrent.GenericFutureListener;

public interface GameFutureListener<F extends GameFuture<?>> extends GenericFutureListener<F> {

}
