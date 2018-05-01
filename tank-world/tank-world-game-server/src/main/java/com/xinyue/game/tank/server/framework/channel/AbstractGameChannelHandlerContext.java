package com.xinyue.game.tank.server.framework.channel;

import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

import com.xinyue.game.tank.command.common.IGameCommand;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelPromise;
import io.netty.channel.DefaultChannelPromise;
import io.netty.channel.VoidChannelPromise;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.internal.ObjectUtil;
import io.netty.util.internal.PromiseNotificationUtil;
import io.netty.util.internal.StringUtil;
import io.netty.util.internal.ThrowableUtil;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

public abstract class AbstractGameChannelHandlerContext implements IGameChannelHandlerContext {
	private static final InternalLogger logger = InternalLoggerFactory
			.getInstance(AbstractGameChannelHandlerContext.class);
	private final boolean inbound;
	private final boolean outbound;
	private final IGameChannelPipeline pipeline;
	final EventExecutor executor;
	private final String name;
	private Runnable invokeReadTask;
	volatile AbstractGameChannelHandlerContext next;
	volatile AbstractGameChannelHandlerContext prev;

	private static final AtomicIntegerFieldUpdater<AbstractGameChannelHandlerContext> HANDLER_STATE_UPDATER = AtomicIntegerFieldUpdater
			.newUpdater(AbstractGameChannelHandlerContext.class, "handlerState");

	private volatile int handlerState = INIT;
	private static final int INIT = 0;

	private static final int ADD_PENDING = 1;

	private static final int ADD_COMPLETE = 2;

	private static final int REMOVE_COMPLETE = 3;

	AbstractGameChannelHandlerContext(IGameChannelPipeline pipeline, EventExecutor executor, String name,
			boolean inbound, boolean outbound) {
		this.name = ObjectUtil.checkNotNull(name, "name");
		this.pipeline = pipeline;
		this.executor = executor;
		this.inbound = inbound;
		this.outbound = outbound;
	}

	final void setAddComplete() {
		for (;;) {
			int oldState = handlerState;
			// Ensure we never update when the handlerState is REMOVE_COMPLETE
			// already.
			// oldState is usually ADD_PENDING but can also be REMOVE_COMPLETE
			// when an EventExecutor is used that is not
			// exposing ordering guarantees.
			if (oldState == REMOVE_COMPLETE || HANDLER_STATE_UPDATER.compareAndSet(this, oldState, ADD_COMPLETE)) {
				return;
			}
		}
	}

	final void setRemoved() {
		handlerState = REMOVE_COMPLETE;
	}

	final void setAddPending() {
		boolean updated = HANDLER_STATE_UPDATER.compareAndSet(this, INIT, ADD_PENDING);
		assert updated; // This should always be true as it MUST be called
						// before setAddComplete() or setRemoved().
	}

	@Override
	public IGameChannel channel() {
		return pipeline.channel();
	}

	@Override
	public IGameChannelPipeline pipeline() {
		return pipeline;
	}

	@Override
	public EventExecutor executor() {
		if (executor == null) {
			return channel().eventLoop();
		} else {
			return executor;
		}
	}

	@Override
	public String name() {
		return name;
	}

	// TODO 这里以后会替换自定义的GameChannelPromise
	@Override
	public ChannelPromise newPromise() {
		// return new DefaultChannelPromise(channel(), executor);
		return null;
	}

	private boolean invokeHandler() {
		// Store in local variable to reduce volatile reads.
		int handlerState = this.handlerState;
		return handlerState == ADD_COMPLETE || (handlerState == ADD_PENDING);
	}

	private AbstractGameChannelHandlerContext findContextInbound() {
		AbstractGameChannelHandlerContext ctx = this;
		do {
			ctx = ctx.next;
		} while (!ctx.inbound);
		return ctx;
	}

	@Override
	public IGameChannelHandlerContext fireChannelActive() {
		invokeChannelActive(findContextInbound());
		return this;
	}

	static void invokeChannelActive(final AbstractGameChannelHandlerContext next) {
		EventExecutor executor = next.executor();
		if (executor.inEventLoop()) {
			next.invokeChannelActive();
		} else {
			executor.execute(new Runnable() {
				@Override
				public void run() {
					next.invokeChannelActive();
				}
			});
		}
	}

	private void invokeChannelActive() {
		if (invokeHandler()) {
			try {
				((IGameChannelInboundHandler) handler()).channelActive(this);
			} catch (Throwable t) {
				notifyHandlerException(t);
			}
		} else {
			fireChannelActive();
		}
	}

	@Override
	public IGameChannelHandlerContext fireChannelInActive() {
		invokeChannelInActive(findContextInbound());
		return this;
	}

	static void invokeChannelInActive(final AbstractGameChannelHandlerContext next) {
		EventExecutor executor = next.executor();
		if (executor.inEventLoop()) {
			next.invokeChannelInActive();
		} else {
			executor.execute(new Runnable() {
				@Override
				public void run() {
					next.invokeChannelInActive();
				}
			});
		}
	}

	private void invokeChannelInActive() {
		if (invokeHandler()) {
			try {
				((IGameChannelInboundHandler) handler()).channelActive(this);
			} catch (Throwable t) {
				notifyHandlerException(t);
			}
		} else {
			fireChannelActive();
		}
	}

	@Override
	public IGameChannelHandlerContext fireReadEvent(final IGameEvent event) {
		invokeReadEvent(findContextInbound(), event);
		return this;
	}

	static void invokeReadEvent(final AbstractGameChannelHandlerContext next, final IGameEvent event) {
		ObjectUtil.checkNotNull(event, "event");
		EventExecutor executor = next.executor();
		if (executor.inEventLoop()) {
			next.invokeReadEvent(event);
		} else {
			executor.execute(new Runnable() {
				@Override
				public void run() {
					next.invokeReadEvent(event);
				}
			});
		}
	}

	private void invokeReadEvent(IGameEvent event) {
		if (invokeHandler()) {
			try {
				((IGameChannelInboundHandler) handler()).readEvent(this, event);
			} catch (Throwable t) {
				notifyHandlerException(t);
			}
		} else {
			fireReadEvent(event);
		}
	}

	@Override
	public IGameChannelHandlerContext fireReadCommand(final IGameCommand msg) {
		invokeChannelRead(findContextInbound(), msg);
		return this;
	}

	static void invokeChannelRead(final AbstractGameChannelHandlerContext next, IGameCommand m) {
		EventExecutor executor = next.executor();
		if (executor.inEventLoop()) {
			next.invokeChannelRead(m);
		} else {
			executor.execute(new Runnable() {
				@Override
				public void run() {
					next.invokeChannelRead(m);
				}
			});
		}
	}

	@Override
	public IGameChannelHandlerContext fireExceptionCaught(Throwable cause) {
		invokeExceptionCaught(next, cause);
		return this;
	}

	static void invokeExceptionCaught(final AbstractGameChannelHandlerContext next, final Throwable cause) {
		ObjectUtil.checkNotNull(cause, "cause");
		EventExecutor executor = next.executor();
		if (executor.inEventLoop()) {
			next.invokeExceptionCaught(cause);
		} else {
			try {
				executor.execute(new Runnable() {
					@Override
					public void run() {
						next.invokeExceptionCaught(cause);
					}
				});
			} catch (Throwable t) {
				if (logger.isWarnEnabled()) {
					logger.warn("Failed to submit an exceptionCaught() event.", t);
					logger.warn("The exceptionCaught() event that was failed to submit was:", cause);
				}
			}
		}
	}

	private void invokeChannelRead(IGameCommand msg) {
		if (invokeHandler()) {
			try {
				((IGameChannelInboundHandler) handler()).readCommand(this, msg);
			} catch (Throwable t) {
				notifyHandlerException(t);
			}
		} else {
			fireReadCommand(msg);
		}
	}

	private static boolean inExceptionCaught(Throwable cause) {
		do {
			StackTraceElement[] trace = cause.getStackTrace();
			if (trace != null) {
				for (StackTraceElement t : trace) {
					if (t == null) {
						break;
					}
					if ("exceptionCaught".equals(t.getMethodName())) {
						return true;
					}
				}
			}

			cause = cause.getCause();
		} while (cause != null);

		return false;
	}

	private void notifyHandlerException(Throwable cause) {
		if (inExceptionCaught(cause)) {
			if (logger.isWarnEnabled()) {
				logger.warn("An exception was thrown by a user handler " + "while handling an exceptionCaught event",
						cause);
			}
			return;
		}

		invokeExceptionCaught(cause);
	}

	private void invokeExceptionCaught(final Throwable cause) {
		if (invokeHandler()) {
			try {
				((IGameChannelInboundHandler) handler()).exceptionCaught(this, cause);
			} catch (Throwable error) {
				if (logger.isDebugEnabled()) {
					logger.debug(
							"An exception {}" + "was thrown by a user handler's exceptionCaught() "
									+ "method while handling the following exception:",
							ThrowableUtil.stackTraceToString(error), cause);
				} else if (logger.isWarnEnabled()) {
					logger.warn("An exception '{}' [enable DEBUG level for full stacktrace] "
							+ "was thrown by a user handler's exceptionCaught() "
							+ "method while handling the following exception:", error, cause);
				}
			}
		} else {
			fireExceptionCaught(cause);
		}
	}

	private boolean isNotValidPromise(ChannelPromise promise, boolean allowVoidPromise) {
		if (promise == null) {
			throw new NullPointerException("promise");
		}

		if (promise.isDone()) {
			// Check if the promise was cancelled and if so signal that the
			// processing of the operation
			// should not be performed.
			//
			// See https://github.com/netty/netty/issues/2349
			if (promise.isCancelled()) {
				return true;
			}
			throw new IllegalArgumentException("promise already done: " + promise);
		}

		if (promise.channel() != channel()) {
			throw new IllegalArgumentException(
					String.format("promise.channel does not match: %s (expected: %s)", promise.channel(), channel()));
		}

		if (promise.getClass() == DefaultChannelPromise.class) {
			return false;
		}

		if (!allowVoidPromise && promise instanceof VoidChannelPromise) {
			throw new IllegalArgumentException(
					StringUtil.simpleClassName(VoidChannelPromise.class) + " not allowed for this operation");
		}

		// if (promise instanceof AbstractChannel.CloseFuture) {
		// throw new IllegalArgumentException(
		// StringUtil.simpleClassName(AbstractChannel.CloseFuture.class) + " not
		// allowed in a pipeline");
		// }
		return false;
	}

	@Override
	public ChannelFuture close() {
		return close(newPromise());
	}

	@Override
	public ChannelFuture close(final ChannelPromise promise) {
		if (isNotValidPromise(promise, false)) {
			// cancelled
			return promise;
		}

		final AbstractGameChannelHandlerContext next = findContextOutbound();
		EventExecutor executor = next.executor();
		if (executor.inEventLoop()) {
			next.invokeClose(promise);
		} else {
			safeExecute(executor, new Runnable() {
				@Override
				public void run() {
					next.invokeClose(promise);
				}
			}, promise, null);
		}

		return promise;
	}

	private void invokeClose(ChannelPromise promise) {
		if (invokeHandler()) {
			try {
				((IGameChannelOutboundHandler) handler()).close(this, promise);
			} catch (Throwable t) {
				notifyOutboundHandlerException(t, promise);
			}
		} else {
			close(promise);
		}
	}

	private static void notifyOutboundHandlerException(Throwable cause, ChannelPromise promise) {
		// Only log if the given promise is not of type VoidChannelPromise as
		// tryFailure(...) is expected to return
		// false.
		PromiseNotificationUtil.tryFailure(promise, cause, promise instanceof VoidChannelPromise ? null : logger);
	}

	private static void safeExecute(EventExecutor executor, Runnable runnable, ChannelPromise promise,
			IGameCommand msg) {
		try {
			executor.execute(runnable);
		} catch (Throwable cause) {

			promise.setFailure(cause);

		}
	}

	private AbstractGameChannelHandlerContext findContextOutbound() {
		AbstractGameChannelHandlerContext ctx = this;
		do {
			ctx = ctx.prev;
		} while (!ctx.outbound);
		return ctx;
	}

	@Override
	public IGameChannelHandlerContext read() {
		final AbstractGameChannelHandlerContext next = findContextOutbound();
		EventExecutor executor = next.executor();
		if (executor.inEventLoop()) {
			next.invokeRead();
		} else {
			Runnable task = next.invokeReadTask;
			if (task == null) {
				next.invokeReadTask = task = new Runnable() {
					@Override
					public void run() {
						next.invokeRead();
					}
				};
			}
			executor.execute(task);
		}

		return this;
	}

	private void invokeRead() {
		if (invokeHandler()) {
			try {
				((IGameChannelOutboundHandler) handler()).read(this);
			} catch (Throwable t) {
				notifyHandlerException(t);
			}
		} else {
			read();
		}
	}

	@Override
	public ChannelFuture writeCommand(IGameCommand msg) {
		return writeCommand(msg, newPromise());
	}

	@Override
	public ChannelFuture writeCommand(IGameCommand msg, ChannelPromise promise) {
		if (msg == null) {
			throw new NullPointerException("msg");
		}

		write(msg, promise);

		return promise;
	}

	private void invokeWriteAndFlush(IGameCommand msg, ChannelPromise promise) {
		if (invokeHandler()) {
			invokeWrite0(msg, promise);
		} else {
			writeCommand(msg, promise);
		}
	}

	private void invokeWrite0(IGameCommand msg, ChannelPromise promise) {
		try {
			((IGameChannelOutboundHandler) handler()).write(this, msg, promise);
		} catch (Throwable t) {
			notifyOutboundHandlerException(t, promise);
		}
	}

	private void write(IGameCommand m, ChannelPromise promise) {
		AbstractGameChannelHandlerContext next = findContextOutbound();
		EventExecutor executor = next.executor();
		if (executor.inEventLoop()) {

			next.invokeWriteAndFlush(m, promise);

		} else {

			safeExecute(executor, () -> {
				next.invokeWriteAndFlush(m, promise);
			}, promise, m);
		}
	}

}
