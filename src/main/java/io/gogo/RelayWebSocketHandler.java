package io.gogo;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import io.util.ChannelUtils;
import io.util.Console;
import java.util.ArrayList;
import java.util.List;

public final class RelayWebSocketHandler extends ChannelInboundHandlerAdapter {
	private Channel clientChannel = null;
	private List<ByteBuf> byteBufList = new ArrayList<>();

	public RelayWebSocketHandler(Channel channel) {
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
		if (evt instanceof IdleStateEvent) {
			releaseCollectedByteBuf();
			ChannelUtils.closeOnFlush(ctx.channel());
			ChannelUtils.closeOnFlush(this.clientChannel);
			Console.info("RelayWebSocketHandler", "Close idle Browser connection.");
		} else {
			super.userEventTriggered(ctx, evt);
		}
	}

	public void channelInactive(ChannelHandlerContext ctx) {
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