package io.gogo;

import java.util.ArrayList;
import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.util.ReferenceCountUtil;
import io.util.ChannelUtils;
import io.util.Console;

public final class RelayWebSocketHandler extends ChannelInboundHandlerAdapter {
	private Channel clientChannel;
	private List<ByteBuf> byteBufList;

	public RelayWebSocketHandler(Channel channel) {
		this.byteBufList = new ArrayList<ByteBuf>();
		this.clientChannel = channel;
	}

	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
		if (this.clientChannel.isActive()) {
			ByteBuf fullByteBuf = Unpooled.wrappedBuffer((ByteBuf[]) this.byteBufList.toArray(new ByteBuf[0]));
			this.clientChannel.writeAndFlush(new BinaryWebSocketFrame(fullByteBuf));
			this.byteBufList.clear();
		} else {
			releaseCollectedByteBuf();
			ChannelUtils.closeOnFlush(ctx.channel());
		}
	}

	public void channelRead(ChannelHandlerContext ctx, Object msg) {
		this.byteBufList.add((ByteBuf) msg);
	}

	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
		if (evt instanceof io.netty.handler.timeout.IdleStateEvent) {
			releaseCollectedByteBuf();
			ChannelUtils.closeOnFlush(ctx.channel());
			ChannelUtils.closeOnFlush(this.clientChannel);
			Console.info("RelayWebSocketHandler", "Close idle Browser connection.");
		} else {
			super.userEventTriggered(ctx, evt);
		}
	}

	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		releaseCollectedByteBuf();
		ChannelUtils.closeOnFlush(this.clientChannel);
	}

	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		Console.error("RelayWebSocketHandler", "exceptionCaught: " + cause.getMessage());
		releaseCollectedByteBuf();
		ChannelUtils.closeOnFlush(ctx.channel());
		ChannelUtils.closeOnFlush(this.clientChannel);
	}

	public void releaseCollectedByteBuf() {
		for (ByteBuf b : this.byteBufList) {
			ReferenceCountUtil.release(b);
		}
		this.byteBufList.clear();
	}
}
