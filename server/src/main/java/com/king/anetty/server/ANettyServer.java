/*
 * Copyright 2019 Jenly Yu
 * <a href="mailto:jenly1314@gmail.com">Jenly</a>
 * <a href="https://github.com/jenly1314">jenly1314</a>
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package com.king.anetty.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

/**
 * Netty服务端
 * @author <a href="mailto:jenly1314@gmail.com">Jenly</a>
 */
public class ANettyServer implements NettyServer {

    private ChannelFuture mChannelFuture;
    private ServerBootstrap mBootstrap;
    private EventLoopGroup mWorkerGroup;
    private EventLoopGroup mGroup;

    public ANettyServer(){
        init();
    }

    private void init(){
        mBootstrap = new ServerBootstrap();
        mWorkerGroup= new NioEventLoopGroup();
        mGroup = new NioEventLoopGroup();
        mBootstrap.group(mGroup,mWorkerGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)//消息立即发出去
                .option(ChannelOption.SO_REUSEADDR, true)
                .childOption(ChannelOption.SO_KEEPALIVE, true)//保持长链接
                .handler(new LoggingHandler(LogLevel.INFO))
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        System.out.println("connected:" + ch.remoteAddress());
                        //建立管道
                        ChannelPipeline channelPipeline = ch.pipeline();
                        //消息结束分隔符
//                        DelimiterBasedFrameDecoder delimiterBasedFrameDecoder = new DelimiterBasedFrameDecoder(131072,Unpooled.wrappedBuffer(new byte[]{0x04}));
                        //添加相关编码器，解码器，处理器等
                        channelPipeline
                                .addLast(new StringEncoder())
                                .addLast(new StringDecoder())
                                .addLast(new StringChannelHandler(){

                                    @Override
                                    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
                                        super.channelRead0(ctx,msg);
                                        System.out.println("Received message:" + msg);
                                        //接收到客户端消息后，直接回复
                                        ctx.writeAndFlush(ch.remoteAddress() + ":" + msg);
                                    }

                                    @Override
                                    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                                        super.exceptionCaught(ctx, cause);

                                    }
                                });
                    }
                });
    }

    @Override
    public void start(String host,int port) {
        try {
            mChannelFuture = mBootstrap.localAddress(host,port).bind().addListener(future -> {
                if(future.isSuccess()){
                    System.out.println("Server start success.");
                }else{
                    System.out.println("Server start failed.");
                }
            }).sync();
            mChannelFuture.channel().closeFuture().sync();
            System.out.println("Server connect closed.");
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            mGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws Exception{
        //启动Netty服务  你的本地IP,端口
        new ANettyServer().start("192.168.100.49",6000);
    }

}
