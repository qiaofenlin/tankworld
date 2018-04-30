package com.xinyue.game.tank.server.framework.channel;

import com.xinyue.game.tank.command.common.IGameCommand;

public interface IGameChannelDispatcher	 {
	/**
	 * 
	 * @Desc  接收来自客户端的请求命令，命令会到送到用户所属的channel中。
	 * @param message
	 * @Author 王广帅
	 * @Date 2018年4月30日 下午12:40:16
	 *
	 */
	void receive(IGameCommand message);
	/**
	 * 
	 * @Desc 接收来自游戏内部的事件，事件会发送到用户所属的channel中。
	 * @param gameEvent
	 * @Author 王广帅
	 * @Date 2018年4月30日 下午12:40:41
	 *
	 */
	void receive(IGameEvent gameEvent);
}
