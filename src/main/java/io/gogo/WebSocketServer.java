package io.gogo;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.internal.StringUtil;
import io.util.Console;

public class WebSocketServer {
	private int port = 8080;

	public WebSocketServer(int port) {
		this.port = port;
	}

	public void start() {
		try {
			ServerBootstrap b = new ServerBootstrap();
			b.group(GoGoServer.bossGroup, GoGoServer.workerGroup).channel(NioServerSocketChannel.class)
					.childHandler(new WebSocketServerInitializer());

			String ip = System.getenv("OPENSHIFT_DIY_IP");
			ChannelFuture f = null;
			if (StringUtil.isNullOrEmpty(ip))
				f = b.bind(this.port).sync();
			else {
				f = b.bind(ip.trim(), this.port).sync();
			}
			System.out.println("WebSocketServer listen on " + this.port);

			f.channel().closeFuture().sync();
		} catch (Exception e) {
			Console.error("WebSocketServer", "Start failed: " + e.getMessage());
		}
	}
}