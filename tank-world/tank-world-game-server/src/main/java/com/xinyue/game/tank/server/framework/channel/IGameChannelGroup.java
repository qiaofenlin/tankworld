package com.xinyue.game.tank.server.framework.channel;

/**
 * 管理IGameChannel的类，负责所有IGameChannel的添加，获取，删除,这个类不是线程安全的
 * 
 * @author 王广帅
 * @company 心悦网络
 * @Date 2018年5月1日 下午8:35:00
 */
public interface IGameChannelGroup {
	/**
	 * 
	 * @Desc  创建一个IGameChannel
	 * @return
	 * @Author 王广帅
	 * @Date 2018年5月1日 下午8:37:16
	 *
	 */
	IGameChannel createGameChannel();
	
	void addGameChannel(Object channelId, IGameChannel gameChannel);

	IGameChannel remove(Object channelId);

	IGameChannel getGameChannel(Object channelId);
}
