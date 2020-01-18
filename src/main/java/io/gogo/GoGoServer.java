package io.gogo;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.internal.StringUtil;

public class GoGoServer {
	public static boolean isDevMode = false;
	public static String version = "1.8.0";
	public static boolean enableSSL = true;
	public static String port = "8080";
	public static String sslPort = "8443";

	public static EventLoopGroup bossGroup = new NioEventLoopGroup(1);
	public static EventLoopGroup workerGroup = new NioEventLoopGroup();

	public static void main(String[] args) {
		String env_ip = System.getenv("OPENSHIFT_DIY_IP");
		String env_port = System.getenv("PORT");
		if (!StringUtil.isNullOrEmpty(env_ip) || !StringUtil.isNullOrEmpty(env_port)) {
			enableSSL = false;
		}
		if (!StringUtil.isNullOrEmpty(env_port)) {
			port = env_port;

		} else if (args.length > 0) {
			try {
				Integer.valueOf(args[0]);
				port = args[0];
			} catch (Exception exception) {
			}

			try {
				Integer.valueOf(args[1]);
				sslPort = args[1];
			} catch (Exception exception) {
			}
		}

		(new WebSocketServer(Integer.valueOf(port.trim()).intValue())).start();

		if (enableSSL) {
			(new WebSocketWithSSLServer(Integer.valueOf(sslPort.trim()).intValue())).start();
		}

		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				GoGoServer.bossGroup.shutdownGracefully();
				GoGoServer.workerGroup.shutdownGracefully();
			}
		});
	}
}
