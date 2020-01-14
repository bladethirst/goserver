package io.gogo;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.CharsetUtil;

public class WebSocketIndexPageHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
	protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest req) throws Exception {
		if (!(req.decoderResult().isSuccess())) {
			sendHttpResponse(ctx, req,
					new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST));
			return;
		}

		if (req.method() != HttpMethod.GET) {
			sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.FORBIDDEN));
			return;
		}

		if ("/".equals(req.uri())) {
			ByteBuf content = Unpooled.copiedBuffer("Welcome GoGo Server " + GoGoServer.version, CharsetUtil.US_ASCII);
			FullHttpResponse res = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, content);

			res.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/html; charset=UTF-8");
			HttpUtil.setContentLength(res, content.readableBytes());

			sendHttpResponse(ctx, req, res);
		} else if ("/getGoGoServerVersion".equals(req.uri())) {
			ByteBuf content = Unpooled.copiedBuffer(GoGoServer.version, CharsetUtil.US_ASCII);
			FullHttpResponse res = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, content);

			res.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/html; charset=UTF-8");
			HttpUtil.setContentLength(res, content.readableBytes());

			sendHttpResponse(ctx, req, res);
		} else {
			sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_FOUND));
		}
	}

	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		ctx.close();
	}

	private static void sendHttpResponse(ChannelHandlerContext ctx, FullHttpRequest req, FullHttpResponse res) {
		if (res.status().code() != 200) {
			ByteBuf buf = Unpooled.copiedBuffer(res.status().toString(), CharsetUtil.UTF_8);
			res.content().writeBytes(buf);
			buf.release();
			HttpUtil.setContentLength(res, res.content().readableBytes());
		}

		ChannelFuture f = ctx.channel().writeAndFlush(res);
		if ((!(HttpUtil.isKeepAlive(req))) || (res.status().code() != 200))
			f.addListener(ChannelFutureListener.CLOSE);
	}
}