package com.xinyue.game.tank.command.common;

import com.google.protobuf.GeneratedMessage;
import com.google.protobuf.InvalidProtocolBufferException;

import io.netty.buffer.ByteBuf;

public abstract class AbstractGameCommand implements IGameCommand {
	private int commandId;
	private CommandType commandType;
	private long userId;
	private long sequenceId;
	private long receiveTime;
	private long responseTime;

	public AbstractGameCommand() {
		GameCommand gameCommand = this.getClass().getAnnotation(GameCommand.class);
		if (gameCommand != null) {
			commandId = gameCommand.commandId();
			commandType = gameCommand.commandType();
		}
		this.receiveTime = System.currentTimeMillis();
	}

	public IGameCommand setInverse(IGameCommand command) {
		this.responseTime = System.currentTimeMillis();
		command.setResponseTime(responseTime);
		command.setSequenceId(sequenceId);
		command.setUserId(userId);
		return command;
	}

	@Override
	public long getReceiveTime() {
		return receiveTime;
	}

	@Override
	public long getResponseTime() {
		return responseTime;
	}

	@Override
	public void setResponseTime(long responseTime) {
		this.responseTime = responseTime;
	}

	@Override
	public int getCommandId() {
		return commandId;
	}

	@Override
	public CommandType getCommandType() {
		return commandType;
	}

	@Override
	public long getUserId() {
		return userId;
	}

	@Override
	public void setUserId(long userId) {
		this.userId = userId;
	}

	@Override
	public long getSequenceId() {
		return sequenceId;
	}

	@Override
	public void setSequenceId(long sequenceId) {
		this.sequenceId = sequenceId;
	}

	@Override
	public byte[] encodeBody() {
		GeneratedMessage generatedMessage = this.getGenerateMessage();
		if (generatedMessage != null) {
			byte[] body = generatedMessage.toByteArray();
			return body;
		}

		return null;
	}

	@Override
	public void decodeBody(ByteBuf byteBuf) throws InvalidProtocolBufferException {
		byte[] body = new byte[byteBuf.readableBytes()];
		byteBuf.readBytes(body);
		this.parseFromBytes(body);

	}

	/**
	 * 
	 * @Desc 描述：从bytes数组中读取数据，这个是protobuf实现的，由子类实现。
	 * @param bytes
	 * @author wang guang shuai
	 * @throws InvalidProtocolBufferException
	 * @date 2016年9月18日 上午10:38:03
	 *
	 */
	protected abstract void parseFromBytes(byte[] bytes) throws InvalidProtocolBufferException;

	/**
	 * 
	 * @Desc 描述：把包体使用protobuf序列化，并返回序列化的管理对象。
	 * @return
	 * @author wang guang shuai
	 * @date 2016年9月18日 上午10:38:44
	 *
	 */
	protected abstract GeneratedMessage getGenerateMessage();
}
