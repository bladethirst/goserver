package io.gogo;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import io.util.Console;

public class WebSocketWithSSLServer {
	private int port;

	public WebSocketWithSSLServer(int port) {
		this.port = port;
	}

	public void start() {
		try {
			SelfSignedCertificate ssc = new SelfSignedCertificate("freetunnel.com");
			SslContext sslCtx = SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey()).build();

			ServerBootstrap b = new ServerBootstrap();
			b.group(GoGoServer.bossGroup, GoGoServer.workerGroup)
					.channel(io.netty.channel.socket.nio.NioServerSocketChannel.class)
					.childHandler(new WebSocketServerInitializer(sslCtx));
			b.bind(this.port).addListener(bindFuture -> {
				if (bindFuture.isSuccess()) {
					Console.info("WebSocketWithSSLServer", "Listening on " + this.port);
				} else {
					Console.error("WebSocketWithSSLServer", "Start Failed: " + bindFuture.cause().getMessage());
					System.exit(0);
				}
			});
		} catch (Exception e) {
			Console.error("WebSocketWithSSLServer", "Start failed: " + e.getMessage());
		}
	}
}
