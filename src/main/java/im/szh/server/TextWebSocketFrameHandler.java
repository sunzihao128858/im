package im.szh.server;


import com.alibaba.fastjson.JSONObject;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.local.LocalChannel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.time.LocalDateTime;

/**
 * 处理TextWebSocketFrame
 *
 * @author waylau.com
 * 2015年3月26日
 */
public class TextWebSocketFrameHandler extends
        SimpleChannelInboundHandler<TextWebSocketFrame> {

	// 存放连接
	public static ChannelGroup channelsSet = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) throws Exception { // (1)
		Channel incoming = ctx.channel();
		int num = 0;



		for (Channel channel : channelsSet) {
			System.out.println("channelRead0-ctx.channel()--"+ctx.channel().id());
			String tmp_channel_id = channel.id().toString();
			String msg_text = msg.text();
            if (channel != incoming){
                channel.writeAndFlush(new TextWebSocketFrame("[" + incoming.remoteAddress() + "]" + msg.text()+" id是："+ incoming.id()+" ---服务器时间："+LocalDateTime.now()));
            }else {
            	JSONObject jsonObject = JSONObject.parseObject(msg.text());
				String name =	jsonObject.getString("name");
				String site =	jsonObject.getString("site");
				String time =	jsonObject.getString("time");
				System.out.printf(name);

            	channel.writeAndFlush(new TextWebSocketFrame("[you]" + name +" id是："+ incoming.id()+" ---服务器时间："+LocalDateTime.now()));
            }
            if(tmp_channel_id.equals(msg_text)){
            	channel.writeAndFlush(new TextWebSocketFrame("相等了"));
				System.out.println("相等了");
			}
			System.out.println("打印结果："+channel.id()+"---"+msg.text());
			System.out.println("集合中存在的连接："+channel.id());
			num++;
        }
		System.out.println("集合有多少个连接啊："+num);
	}



	@Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {  // (2)
        Channel incoming = ctx.channel();

        // Broadcast a message to multiple Channels
		channelsSet.writeAndFlush(new TextWebSocketFrame("[SERVER] - " + incoming.remoteAddress() + " 加入"));
		channelsSet.add(incoming);
		String first = incoming.id().toString();  //
		//String json = "{\"first\":\"" +first + "\",\"code\":\"00001\"}";
		//System.out.printf(json);
		//String json2 = "{\"name\":\"张三\",\"code\":\"123\"}";
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("first",first);
		jsonObject.put("code",00001);
		incoming.writeAndFlush(jsonObject);
		System.out.printf("Sunzihao:"+jsonObject.toString());
		System.out.println("Client:"+incoming.remoteAddress() +"加入");
		System.out.println("handlerAdded--"+ incoming.id());
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {  // (3)
        Channel incoming = ctx.channel();

        // Broadcast a message to multiple Channels
		channelsSet.writeAndFlush(new TextWebSocketFrame("[SERVER] - " + incoming.remoteAddress() + " 离开"));

		System.out.println("Client:"+incoming.remoteAddress() +"离开");
		System.out.println("handlerRemoved--"+ incoming.id());
        // A closed Channel is automatically removed from ChannelGroup,
        // so there is no need to do "channels.remove(ctx.channel());"
    }

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception { // (5)
        Channel incoming = ctx.channel();
		System.out.println("Client:"+incoming.remoteAddress()+"在线");
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception { // (6)
        Channel incoming = ctx.channel();
		System.out.println("Client:"+incoming.remoteAddress()+"掉线");
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)	// (7)
			throws Exception {
    	Channel incoming = ctx.channel();
		System.out.println("Client:"+incoming.remoteAddress()+"异常");
        // 当出现异常就关闭连接
        cause.printStackTrace();
        ctx.close();
	}

}
