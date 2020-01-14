package io.gogo;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import io.util.Console;

public class WebSocketWithSSLServer {
	private int port = 8443;

	public WebSocketWithSSLServer(int port) {
		this.port = port;
	}

	public void start() {
		try {
			SelfSignedCertificate ssc = new SelfSignedCertificate("freetunnel.com");
			SslContext sslCtx = SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey()).build();

			ServerBootstrap b = new ServerBootstrap();

			b.group(GoGoServer.bossGroup, GoGoServer.workerGroup).channel(NioServerSocketChannel.class)
					.childHandler(new WebSocketServerInitializer(sslCtx));

			ChannelFuture f = b.bind(this.port).sync();
			System.out.println("WebSocketWithSSLServer listen on " + this.port);

			f.channel().closeFuture().sync();
		} catch (Exception e) {
			Console.error("WebSocketWithSSLServer", "Start failed: " + e.getMessage());
		}
	}
}