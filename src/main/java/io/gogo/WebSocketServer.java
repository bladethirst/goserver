package io.gogo;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.util.internal.StringUtil;
import io.util.Console;

public class WebSocketServer {
	private int port;

	public WebSocketServer(int port) {
		this.port = port;
	}

	public void start() {
		ServerBootstrap b = new ServerBootstrap();
		b.group(GoGoServer.bossGroup, GoGoServer.workerGroup)
				.channel(io.netty.channel.socket.nio.NioServerSocketChannel.class)
				.childHandler(new WebSocketServerInitializer());
		String ip = System.getenv("OPENSHIFT_DIY_IP");
		ChannelFuture f = null;
		if (StringUtil.isNullOrEmpty(ip)) {
			f = b.bind(this.port);
		} else {
			f = b.bind(ip.trim(), this.port);
		}
		f.addListener(bindFuture -> {
			if (bindFuture.isSuccess()) {
				Console.info("SocksProxyServer", "WebSocketServer listen on " + this.port);
			} else {
				Console.error("SocksProxyServer", "Start Failed: " + bindFuture.cause().getMessage());
				System.exit(0);
			}
		});
	}
}
