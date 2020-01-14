package io.gogo;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.internal.StringUtil;

public class GoGoServer {
	public static boolean isDevMode = false;
	public static String version = "1.8.0";
	public static boolean enableSSL = true;
	public static String sslPort = "8443";

	public static EventLoopGroup bossGroup = new NioEventLoopGroup(1);
	public static EventLoopGroup workerGroup = new NioEventLoopGroup();

	public static void main(String[] args) {
		String ip = System.getenv("OPENSHIFT_DIY_IP");
		String port = System.getenv("PORT");
		if ((!(StringUtil.isNullOrEmpty(ip))) || (!(StringUtil.isNullOrEmpty(port)))) {
			enableSSL = false;
		}
		if (StringUtil.isNullOrEmpty(port)) {
			port = "8080";
		} else if (args.length > 0) {
			try {
				Integer.valueOf(args[0]);
				port = args[0];
			} catch (Exception e) {
				port = "8080";
			}
		}

		final String fPort = port;

		new Thread(new Runnable() {
			public void run() {
				new WebSocketServer(Integer.valueOf(fPort.trim()).intValue()).start();
			}
		}).start();

		if (enableSSL) {
			new Thread(new Runnable() {
				public void run() {
					new WebSocketWithSSLServer(Integer.valueOf(GoGoServer.sslPort.trim()).intValue()).start();
				}
			}).start();
		}

		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				GoGoServer.bossGroup.shutdownGracefully();
				GoGoServer.workerGroup.shutdownGracefully();
			}
		});
	}
}