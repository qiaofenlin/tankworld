package com.xinyue.game.tank.server.framework;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import com.xinyue.game.tank.command.common.IGameCommand;
import com.xinyue.game.tank.server.framework.channel.ChannelEvent;

@Service
public class RequestDispatcher {
	private Map<String, MethodInvokerInfo> methodMap = new HashMap<>();
	private Map<String, MethodInvokerInfo> eventMap = new HashMap<>();
	@Autowired
	private ApplicationContext applicationContext;

	@PostConstruct
	public void init() {
		String[] commandHandlerBeanNames = applicationContext.getBeanNamesForAnnotation(CommandHandler.class);
		if (commandHandlerBeanNames != null) {
			for (String beanName : commandHandlerBeanNames) {
				Object obj = applicationContext.getBean(beanName);
				Method[] methods = obj.getClass().getMethods();
				for (Method method : methods) {
					ReceiveCommand receiveCommand = method.getAnnotation(ReceiveCommand.class);
					if (receiveCommand != null) {
						Class<?>[] parameterClazzes = method.getParameterTypes();
						Class<?> commandClass = parameterClazzes[0];
						String key = commandClass.getName();
						MethodInvokerInfo methodInvokerInfo = new MethodInvokerInfo(obj, method);
						methodMap.put(key, methodInvokerInfo);
					}
					ReceiveEvent receiveEvent = method.getAnnotation(ReceiveEvent.class);
					if (receiveEvent != null) {
						Class<?>[] parameterClazzes = method.getParameterTypes();
						Class<?> commandClass = parameterClazzes[0];
						String key = commandClass.getName();
						MethodInvokerInfo methodInvokerInfo = new MethodInvokerInfo(obj, method);
						eventMap.put(key, methodInvokerInfo);
					}
				}
			}
		}
	}

	public void Invoker(IGameCommand gameCommand, GameContext ctx) {
		String key = gameCommand.getClass().getName();
		MethodInvokerInfo methodInvokerInfo = this.methodMap.get(key);
		if (methodInvokerInfo != null) {
			Object obj = methodInvokerInfo.getObject();
			try {

				methodInvokerInfo.getMethod().invoke(obj, gameCommand, ctx);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				e.printStackTrace();
			}
		} else {
			throw new IllegalArgumentException("404，找不到command:" + key + "的处理方法");
		}
	}

	public void Invoker(ChannelEvent gameEvent, GameContext ctx) {
		String key = gameEvent.getClass().getName();
		MethodInvokerInfo methodInvokerInfo = this.eventMap.get(key);
		if (methodInvokerInfo != null) {
			Object obj = methodInvokerInfo.getObject();
			try {

				methodInvokerInfo.getMethod().invoke(obj, gameEvent, ctx);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				e.printStackTrace();
			}
		} else {
			throw new IllegalArgumentException("404，找不到event:" + key + "的处理方法");
		}
	}
}
