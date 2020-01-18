package io.gogo;

import java.util.concurrent.TimeUnit;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.timeout.IdleStateHandler;

public class WebSocketServerInitializer extends ChannelInitializer<SocketChannel> {
	private SslContext sslCtx = null;

	private static final String WEBSOCKET_PATH = "/websocket";

	public WebSocketServerInitializer(SslContext sslCtx) {
		this.sslCtx = sslCtx;
	}

	public void initChannel(SocketChannel ch) throws Exception {
		ChannelPipeline pipeline = ch.pipeline();

		if (this.sslCtx != null) {
			pipeline.addLast(new ChannelHandler[] { this.sslCtx.newHandler(ch.alloc()) });
		}
		pipeline.addLast(new ChannelHandler[] { new HttpServerCodec() });
		pipeline.addLast(new ChannelHandler[] { new HttpObjectAggregator(10485760) });
		pipeline.addLast(new ChannelHandler[] { new WebSocketServerCompressionHandler() });
		pipeline.addLast(new ChannelHandler[] { new WebSocketServerProtocolHandler(WEBSOCKET_PATH, null, true) });
		pipeline.addLast(new ChannelHandler[] { new WebSocketIndexPageHandler() });
		pipeline.addLast(new ChannelHandler[] { new IdleStateHandler(0L, 0L, 60L, TimeUnit.SECONDS) });
		pipeline.addLast(new ChannelHandler[] { new WebSocketServerHandler() });
	}

	public WebSocketServerInitializer() {
	}
}
