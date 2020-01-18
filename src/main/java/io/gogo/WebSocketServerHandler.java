package io.gogo;

import java.util.concurrent.TimeUnit;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.timeout.IdleStateHandler;
import io.util.ChannelUtils;
import io.util.Console;
import io.util.VersionComparator;

public class WebSocketServerHandler extends SimpleChannelInboundHandler<WebSocketFrame> {
	private Channel remoteChannel = null;

	public void channelRead0(final ChannelHandlerContext ctx, WebSocketFrame frame) throws Exception {
		if (frame instanceof TextWebSocketFrame) {

			String[] arr = ((TextWebSocketFrame) frame).text().split(":");
			if (arr.length != 3 || VersionComparator.compareVersion(arr[2].trim(), GoGoServer.version) < 0) {
				ChannelUtils.closeOnFlush(ctx.channel());
				ChannelUtils.closeOnFlush(this.remoteChannel);
				return;
			}
			final String remoteHost = arr[0];
			final Integer remotePort = Integer.valueOf(arr[1]);

			Bootstrap b = new Bootstrap();
			b.group(ctx.channel().eventLoop()).channel(io.netty.channel.socket.nio.NioSocketChannel.class)
					.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, Integer.valueOf(60000))
					.option(ChannelOption.SO_KEEPALIVE, Boolean.valueOf(true))
					.handler(new ChannelInitializer<SocketChannel>() {
						public void initChannel(SocketChannel ch) {
							ch.pipeline().addLast(
									new ChannelHandler[] { new IdleStateHandler(0L, 0L, 60L, TimeUnit.SECONDS) });
							ch.pipeline().addLast(new ChannelHandler[] { new RelayWebSocketHandler(ctx.channel()) });
						}
					});

			b.connect(remoteHost, remotePort.intValue()).addListener(new ChannelFutureListener() {
				public void operationComplete(ChannelFuture future) throws Exception {
					if (future.isSuccess()) {

						WebSocketServerHandler.this.remoteChannel = future.channel();

						if (ctx.channel().isActive()) {

							ctx.channel().writeAndFlush(new TextWebSocketFrame("success"));
						} else {
							ChannelUtils.closeOnFlush(WebSocketServerHandler.this.remoteChannel);
						}
					} else {
						Console.error("WebSocketServerHandler",
								"Connect to " + remoteHost + ":" + remotePort + " failed ....");

						if (ctx.channel().isActive()) {
							ctx.channel().writeAndFlush(new TextWebSocketFrame("error"))
									.addListener(ChannelFutureListener.CLOSE);
						}
					}
				}
			});
		} else if (frame instanceof BinaryWebSocketFrame) {
			BinaryWebSocketFrame binFrame = (BinaryWebSocketFrame) frame;
			if (this.remoteChannel.isActive()) {
				this.remoteChannel.write(binFrame.content().retain());
			} else {
				ChannelUtils.closeOnFlush(ctx.channel());
			}
		} else {
			String message = "unsupported frame type: " + frame.getClass().getName();
			throw new UnsupportedOperationException(message);
		}
	}

	public void channelReadComplete(ChannelHandlerContext ctx) {
		if (this.remoteChannel != null && this.remoteChannel.isActive()) {
			this.remoteChannel.flush();
		}
	}

	public void channelInactive(ChannelHandlerContext ctx) {
		if (this.remoteChannel != null) {
			ChannelUtils.closeOnFlush(this.remoteChannel);
		}
	}

	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
		if (evt instanceof io.netty.handler.timeout.IdleStateEvent) {
			ChannelUtils.closeOnFlush(ctx.channel());
			ChannelUtils.closeOnFlush(this.remoteChannel);
		} else {

			super.userEventTriggered(ctx, evt);
		}
	}

	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		Console.error("WebSocketServerHandler", "exceptionCaught: " + cause.getMessage());
		ChannelUtils.closeOnFlush(ctx.channel());
		if (this.remoteChannel != null)
			ChannelUtils.closeOnFlush(this.remoteChannel);
	}
}
