package com.xinyue.game.tank.command.common;

import com.google.protobuf.InvalidProtocolBufferException;

import io.netty.buffer.ByteBuf;

public interface IGameCommand {
	
	IGameCommand newInverse();
	int getCommandId();

	CommandType getCommandType();

	long getUserId();
	void setUserId(long userId);

	/**
	 * 
	 * @Desc 在游戏中，每一个用户发送的command都有自己的一个唯一id,这个id是客户端向服务器发送的command的个数递增的。
	 * @return
	 * @Author 王广帅
	 * @Date 2018年4月23日 下午11:22:27
	 *
	 */
	long getSequenceId();
	void setSequenceId(long sequenceId);
	/**
	 * 
	 * @Desc   序列化command的包体，即数据。
	 * @return
	 * @Author 王广帅
	 * @Date 2018年4月23日 下午11:23:50
	 *
	 */
	byte[] encodeBody();
	
	void decodeBody(ByteBuf byteBuf) throws InvalidProtocolBufferException;
	
	long getReceiveTime();
	long getResponseTime();
	
	void setResponseTime(long responseTime);

}
