package com.xinyue.game.tank.server.framework.channel;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.WeakHashMap;
import java.util.concurrent.RejectedExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinyue.game.tank.command.common.IGameCommand;
import com.xinyue.game.tank.server.framework.channel.adapter.GameChannelHandlerAdapter;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPipelineException;
import io.netty.channel.ChannelPromise;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.FastThreadLocal;
import io.netty.util.internal.StringUtil;

public class DefaultGameChannelPipeline implements IGameChannelPipeline {
	private Logger logger = LoggerFactory.getLogger(DefaultGameChannelPipeline.class);
	private static final String HEAD_NAME = generateName0(HeadContext.class);
	private static final String TAIL_NAME = generateName0(TailContext.class);
	final AbstractGameChannelHandlerContext head;
	final AbstractGameChannelHandlerContext tail;
	private boolean registered;

	private PendingHandlerCallback pendingHandlerCallbackHead;

	private static final FastThreadLocal<Map<Class<?>, String>> nameCaches = new FastThreadLocal<Map<Class<?>, String>>() {
		@Override
		protected Map<Class<?>, String> initialValue() throws Exception {
			return new WeakHashMap<Class<?>, String>();
		}
	};
	private IGameChannel channel;

	protected DefaultGameChannelPipeline(IGameChannel channel) {
		this.channel = channel;
		tail = new TailContext(this);
		head = new HeadContext(this);

		head.next = tail;
		tail.prev = head;
	}

	private AbstractGameChannelHandlerContext newContext(EventExecutor executor, String name,
			IGameChannelHandler handler) {
		return new DefaultGameChannelHandlerContext(this, executor, name, handler);
	}

	@Override
	public IGameChannelHandler remove(String name) {
		return remove(getContextOrDie(name)).handler();
	}

	@Override
	public IGameChannelHandler removeFirst() {
		if (head.next == tail) {
			throw new NoSuchElementException();
		}
		return remove(head.next).handler();
	}

	@Override
	public IGameChannelHandler removeLast() {
		if (head.next == tail) {
			throw new NoSuchElementException();
		}
		return remove(tail.prev).handler();
	}

	@Override
	public IGameChannelHandler first() {
		IGameChannelHandlerContext first = firstContext();
		if (first == null) {
			return null;
		}
		return first.handler();
	}

	@Override
	public final IGameChannelHandlerContext firstContext() {
		AbstractGameChannelHandlerContext first = head.next;
		if (first == tail) {
			return null;
		}
		return head.next;
	}

	@Override
	public IGameChannelHandler last() {
		AbstractGameChannelHandlerContext last = tail.prev;
		if (last == head) {
			return null;
		}
		return last.handler();
	}

	@Override
	public IGameChannelHandlerContext lastContext() {
		AbstractGameChannelHandlerContext last = tail.prev;
		if (last == head) {
			return null;
		}
		return last;
	}

	@Override
	public IGameChannelHandler get(String name) {
		IGameChannelHandlerContext ctx = context(name);
		if (ctx == null) {
			return null;
		} else {
			return ctx.handler();
		}
	}

	@Override
	public IGameChannel channel() {
		return channel;
	}

	@Override
	public IGameChannelPipeline fireChannelActive() {
		AbstractGameChannelHandlerContext.invokeChannelActive(head);
		return this;
	}

	@Override
	public IGameChannelPipeline fireChannelInActive() {
		AbstractGameChannelHandlerContext.invokeChannelInActive(head);
		return this;
	}

	@Override
	public IGameChannelPipeline fireExceptionCaught(Throwable cause) {
		AbstractGameChannelHandlerContext.invokeExceptionCaught(head, cause);
		return this;
	}

	@Override
	public IGameChannelPipeline fireReadCommand(IGameCommand command) {
		AbstractGameChannelHandlerContext.invokeChannelRead(head, command);
		return this;
	}

	@Override
	public IGameChannelPipeline fireReadEvent(IGameEvent msg) {
		AbstractGameChannelHandlerContext.invokeReadEvent(head, msg);
		return this;
	}

	private static String generateName0(Class<?> handlerType) {
		return StringUtil.simpleClassName(handlerType) + "#0";
	}

	/**
	 * Called once a {@link Throwable} hit the end of the
	 * {@link ChannelPipeline} without been handled by the user in
	 * {@link ChannelHandler#exceptionCaught(ChannelHandlerContext, Throwable)}.
	 */
	protected void onUnhandledInboundException(Throwable cause) {
		logger.warn("An exceptionCaught() event was fired, and it reached at the tail of the pipeline. "
				+ "It usually means the last handler in the pipeline did not handle the exception.", cause);
	}

	/**
	 * Called once a message hit the end of the {@link ChannelPipeline} without
	 * been handled by the user in
	 * {@link ChannelInboundHandler#channelRead(ChannelHandlerContext, Object)}.
	 * This method is responsible to call
	 * {@link ReferenceCountUtil#release(Object)} on the given msg at some
	 * point.
	 */
	protected void onUnhandledInboundMessage(Object msg) {
		logger.debug("Discarded inbound message {} that reached at the tail of the pipeline. "
				+ "Please check your pipeline configuration.", msg);
	}

	final class TailContext extends AbstractGameChannelHandlerContext implements IGameChannelInboundHandler {

		TailContext(IGameChannelPipeline pipeline) {
			super(pipeline, null, TAIL_NAME, true, false);
			setAddComplete();
		}

		@Override
		public void handlerAdded(IGameChannelHandlerContext ctx) throws Exception {

		}

		@Override
		public void handlerRemoved(IGameChannelHandlerContext ctx) throws Exception {

		}

		@Override
		public IGameChannelHandler handler() {
			return this;
		}

		@Override
		public void channelActive(IGameChannelHandlerContext ctx) throws Exception {

		}

		@Override
		public void channelInActive(IGameChannelHandlerContext ctx) {

		}

		@Override
		public void readCommand(IGameChannelHandlerContext ctx, IGameCommand gameCommand) {
			onUnhandledInboundMessage(gameCommand);
		}

		@Override
		public void readEvent(IGameChannelHandlerContext ctx, IGameEvent gameEvent) {
			logger.warn("收到的event未处理：{}", gameEvent.getClass().getName());
		}

		@Override
		public void exceptionCaught(IGameChannelHandlerContext ctx, Throwable exp) {
			onUnhandledInboundException(exp);
		}

		@Override
		public ChannelPromise newPromise() {
			return null;
		}

	}

	final class HeadContext extends AbstractGameChannelHandlerContext
			implements IGameChannelOutboundHandler, IGameChannelInboundHandler {

		HeadContext(DefaultGameChannelPipeline pipeline) {
			super(pipeline, null, HEAD_NAME, false, true);
			setAddComplete();
		}

		@Override
		public IGameChannelHandler handler() {
			return this;
		}

		@Override
		public void handlerAdded(IGameChannelHandlerContext ctx) throws Exception {

		}

		@Override
		public void handlerRemoved(IGameChannelHandlerContext ctx) throws Exception {

		}

		@Override
		public void channelActive(IGameChannelHandlerContext ctx) throws Exception {
			ctx.fireChannelActive();
		}

		@Override
		public void channelInActive(IGameChannelHandlerContext ctx) {
			ctx.fireChannelInActive();
		}

		@Override
		public void readCommand(IGameChannelHandlerContext ctx, IGameCommand gameCommand) {
			ctx.fireReadCommand(gameCommand);
		}

		@Override
		public void readEvent(IGameChannelHandlerContext ctx, IGameEvent gameEvent) {
			ctx.fireReadEvent(gameEvent);
		}

		@Override
		public void exceptionCaught(IGameChannelHandlerContext ctx, Throwable exp) {
			ctx.fireExceptionCaught(exp);
		}

		@Override
		public void close(IGameChannelHandlerContext ctx, ChannelPromise promise) throws Exception {

		}

		@Override
		public void read(IGameChannelHandlerContext ctx) throws Exception {

		}

		@Override
		public void write(IGameChannelHandlerContext ctx, IGameCommand msg, ChannelPromise promise) throws Exception {
		}
	}

	@Override
	public ChannelFuture close() {
		return tail.close();
	}

	@Override
	public ChannelFuture close(ChannelPromise promise) {
		return tail.close(promise);
	}

	@Override
	public ChannelFuture writeCommand(IGameCommand gameCommand) {
		return tail.writeCommand(gameCommand);
	}

	@Override
	public ChannelFuture writeCommand(IGameCommand msg, ChannelPromise promise) {
		return tail.writeCommand(msg, promise);
	}

	// TODO 这里会以后替换成自定义的promise
	@Override
	public ChannelPromise newPromise() {
		return null;
	}

	@Override
	public IGameChannelPipeline read() {
		tail.read();
		return this;
	}

	@Override
	public final Map<String, IGameChannelHandler> toMap() {
		Map<String, IGameChannelHandler> map = new LinkedHashMap<String, IGameChannelHandler>();
		AbstractGameChannelHandlerContext ctx = head.next;
		for (;;) {
			if (ctx == tail) {
				return map;
			}
			map.put(ctx.name(), ctx.handler());
			ctx = ctx.next;
		}
	}

	@Override
	public Iterator<Entry<String, IGameChannelHandler>> iterator() {
		return toMap().entrySet().iterator();
	}

	private static void checkMultiplicity(IGameChannelHandler handler) {
		if (handler instanceof GameChannelHandlerAdapter) {
			GameChannelHandlerAdapter h = (GameChannelHandlerAdapter) handler;
			if (h.added) {
				throw new ChannelPipelineException(h.getClass().getName()
						+ " is not a @Sharable handler, so can't be added or removed multiple times.");
			}
			h.added = true;
		}
	}

	private String filterName(String name, IGameChannelHandler handler) {
		if (name == null) {
			return generateName(handler);
		}
		checkDuplicateName(name);
		return name;
	}

	private void checkDuplicateName(String name) {
		if (context0(name) != null) {
			throw new IllegalArgumentException("Duplicate handler name: " + name);
		}
	}

	private String generateName(IGameChannelHandler handler) {
		Map<Class<?>, String> cache = nameCaches.get();
		Class<?> handlerType = handler.getClass();
		String name = cache.get(handlerType);
		if (name == null) {
			name = generateName0(handlerType);
			cache.put(handlerType, name);
		}

		// It's not very likely for a user to put more than one handler of the
		// same type, but make sure to avoid
		// any name conflicts. Note that we don't cache the names generated
		// here.
		if (context0(name) != null) {
			String baseName = name.substring(0, name.length() - 1); // Strip the
																	// trailing
																	// '0'.
			for (int i = 1;; i++) {
				String newName = baseName + i;
				if (context0(newName) == null) {
					name = newName;
					break;
				}
			}
		}
		return name;
	}

	private void callHandlerAdded0(final AbstractGameChannelHandlerContext ctx) {
		try {
			ctx.handler().handlerAdded(ctx);
			ctx.setAddComplete();
		} catch (Throwable t) {
			boolean removed = false;
			try {
				remove0(ctx);
				try {
					ctx.handler().handlerRemoved(ctx);
				} finally {
					ctx.setRemoved();
				}
				removed = true;
			} catch (Throwable t2) {
				if (logger.isWarnEnabled()) {
					logger.warn("Failed to remove a handler: " + ctx.name(), t2);
				}
			}

			if (removed) {
				fireExceptionCaught(new ChannelPipelineException(
						ctx.handler().getClass().getName() + ".handlerAdded() has thrown an exception; removed.", t));
			} else {
				fireExceptionCaught(new ChannelPipelineException(ctx.handler().getClass().getName()
						+ ".handlerAdded() has thrown an exception; also failed to remove.", t));
			}
		}
	}

	private static void remove0(AbstractGameChannelHandlerContext ctx) {
		AbstractGameChannelHandlerContext prev = ctx.prev;
		AbstractGameChannelHandlerContext next = ctx.next;
		prev.next = next;
		next.prev = prev;
	}

	private AbstractGameChannelHandlerContext context0(String name) {
		AbstractGameChannelHandlerContext context = head.next;
		while (context != tail) {
			if (context.name().equals(name)) {
				return context;
			}
			context = context.next;
		}
		return null;
	}

	private void callHandlerCallbackLater(AbstractGameChannelHandlerContext ctx, boolean added) {
		assert !registered;

		PendingHandlerCallback task = added ? new PendingHandlerAddedTask(ctx) : new PendingHandlerRemovedTask(ctx);
		PendingHandlerCallback pending = pendingHandlerCallbackHead;
		if (pending == null) {
			pendingHandlerCallbackHead = task;
		} else {
			// Find the tail of the linked-list.
			while (pending.next != null) {
				pending = pending.next;
			}
			pending.next = task;
		}
	}

	private void callHandlerRemoved0(final AbstractGameChannelHandlerContext ctx) {
		// Notify the complete removal.
		try {
			try {
				ctx.handler().handlerRemoved(ctx);
			} finally {
				ctx.setRemoved();
			}
		} catch (Throwable t) {
			fireExceptionCaught(new ChannelPipelineException(
					ctx.handler().getClass().getName() + ".handlerRemoved() has thrown an exception.", t));
		}
	}

	private final class PendingHandlerRemovedTask extends PendingHandlerCallback {

		PendingHandlerRemovedTask(AbstractGameChannelHandlerContext ctx) {
			super(ctx);
		}

		@Override
		public void run() {
			callHandlerRemoved0(ctx);
		}

		@Override
		void execute() {
			EventExecutor executor = ctx.executor();
			if (executor.inEventLoop()) {
				callHandlerRemoved0(ctx);
			} else {
				try {
					executor.execute(this);
				} catch (RejectedExecutionException e) {
					if (logger.isWarnEnabled()) {
						logger.warn("Can't invoke handlerRemoved() as the EventExecutor {} rejected it,"
								+ " removing handler {}.", executor, ctx.name(), e);
					}
					// remove0(...) was call before so just call
					// AbstractChannelHandlerContext.setRemoved().
					ctx.setRemoved();
				}
			}
		}
	}

	private final class PendingHandlerAddedTask extends PendingHandlerCallback {

		PendingHandlerAddedTask(AbstractGameChannelHandlerContext ctx) {
			super(ctx);
		}

		@Override
		public void run() {
			callHandlerAdded0(ctx);
		}

		@Override
		void execute() {
			EventExecutor executor = ctx.executor();
			if (executor.inEventLoop()) {
				callHandlerAdded0(ctx);
			} else {
				try {
					executor.execute(this);
				} catch (RejectedExecutionException e) {
					if (logger.isWarnEnabled()) {
						logger.warn(
								"Can't invoke handlerAdded() as the EventExecutor {} rejected it, removing handler {}.",
								executor, ctx.name(), e);
					}
					remove0(ctx);
					ctx.setRemoved();
				}
			}
		}
	}

	private abstract static class PendingHandlerCallback implements Runnable {
		final AbstractGameChannelHandlerContext ctx;
		PendingHandlerCallback next;

		PendingHandlerCallback(AbstractGameChannelHandlerContext ctx) {
			this.ctx = ctx;
		}

		abstract void execute();
	}

	@Override
	public IGameChannelPipeline addFirst(String name, IGameChannelHandler handler) {
		final AbstractGameChannelHandlerContext newCtx;
		synchronized (this) {
			checkMultiplicity(handler);
			name = filterName(name, handler);

			newCtx = newContext(null, name, handler);

			addFirst0(newCtx);

			// If the registered is false it means that the channel was not
			// registered on an eventloop yet.
			// In this case we add the context to the pipeline and add a task
			// that will call
			// ChannelHandler.handlerAdded(...) once the channel is registered.
			if (!registered) {
				newCtx.setAddPending();
				callHandlerCallbackLater(newCtx, true);
				return this;
			}

			EventExecutor executor = newCtx.executor();
			if (!executor.inEventLoop()) {
				newCtx.setAddPending();
				executor.execute(new Runnable() {
					@Override
					public void run() {
						callHandlerAdded0(newCtx);
					}
				});
				return this;
			}
		}
		callHandlerAdded0(newCtx);
		return this;
	}

	private void addFirst0(AbstractGameChannelHandlerContext newCtx) {
		AbstractGameChannelHandlerContext nextCtx = head.next;
		newCtx.prev = head;
		newCtx.next = nextCtx;
		head.next = newCtx;
		nextCtx.prev = newCtx;
	}

	@Override
	public IGameChannelPipeline addLast(String name, IGameChannelHandler handler) {
		final AbstractGameChannelHandlerContext newCtx;
		synchronized (this) {
			checkMultiplicity(handler);

			newCtx = newContext(null, filterName(name, handler), handler);

			addLast0(newCtx);

			// If the registered is false it means that the channel was not
			// registered on an eventloop yet.
			// In this case we add the context to the pipeline and add a task
			// that will call
			// ChannelHandler.handlerAdded(...) once the channel is registered.
			if (!registered) {
				newCtx.setAddPending();
				callHandlerCallbackLater(newCtx, true);
				return this;
			}

			EventExecutor executor = newCtx.executor();
			if (!executor.inEventLoop()) {
				newCtx.setAddPending();
				executor.execute(new Runnable() {
					@Override
					public void run() {
						callHandlerAdded0(newCtx);
					}
				});
				return this;
			}
		}
		callHandlerAdded0(newCtx);
		return this;
	}

	private void addLast0(AbstractGameChannelHandlerContext newCtx) {
		AbstractGameChannelHandlerContext prev = tail.prev;
		newCtx.prev = prev;
		newCtx.next = tail;
		prev.next = newCtx;
		tail.prev = newCtx;
	}

	@Override
	public IGameChannelPipeline addFirst(IGameChannelHandler... handlers) {
		if (handlers == null) {
			throw new NullPointerException("handlers");
		}

		for (IGameChannelHandler h : handlers) {
			if (h == null) {
				break;
			}
			addFirst(null, h);
		}

		return this;
	}

	@Override
	public IGameChannelPipeline addLast(IGameChannelHandler... handlers) {
		if (handlers == null) {
			throw new NullPointerException("handlers");
		}

		for (IGameChannelHandler h : handlers) {
			if (h == null) {
				break;
			}
			addLast(null, h);
		}

		return this;
	}

	@Override
	public IGameChannelPipeline remove(IGameChannelHandler handler) {
		remove(getContextOrDie(handler));
		return this;
	}

	private AbstractGameChannelHandlerContext remove(final AbstractGameChannelHandlerContext ctx) {
		assert ctx != head && ctx != tail;

		synchronized (this) {
			remove0(ctx);

			// If the registered is false it means that the channel was not
			// registered on an eventloop yet.
			// In this case we remove the context from the pipeline and add a
			// task that will call
			// ChannelHandler.handlerRemoved(...) once the channel is
			// registered.
			if (!registered) {
				callHandlerCallbackLater(ctx, false);
				return ctx;
			}

			EventExecutor executor = ctx.executor();
			if (!executor.inEventLoop()) {
				executor.execute(new Runnable() {
					@Override
					public void run() {
						callHandlerRemoved0(ctx);
					}
				});
				return ctx;
			}
		}
		callHandlerRemoved0(ctx);
		return ctx;
	}

	private AbstractGameChannelHandlerContext getContextOrDie(IGameChannelHandler handler) {
		AbstractGameChannelHandlerContext ctx = (AbstractGameChannelHandlerContext) context(handler);
		if (ctx == null) {
			throw new NoSuchElementException(handler.getClass().getName());
		} else {
			return ctx;
		}
	}

	private AbstractGameChannelHandlerContext getContextOrDie(String name) {
		AbstractGameChannelHandlerContext ctx = (AbstractGameChannelHandlerContext) context(name);
		if (ctx == null) {
			throw new NoSuchElementException(name);
		} else {
			return ctx;
		}
	}

	@Override
	public final IGameChannelHandlerContext context(String name) {
		if (name == null) {
			throw new NullPointerException("name");
		}

		return context0(name);
	}

	@Override
	public final IGameChannelHandlerContext context(IGameChannelHandler handler) {
		if (handler == null) {
			throw new NullPointerException("handler");
		}

		AbstractGameChannelHandlerContext ctx = head.next;
		for (;;) {

			if (ctx == null) {
				return null;
			}

			if (ctx.handler() == handler) {
				return ctx;
			}

			ctx = ctx.next;
		}
	}
}
