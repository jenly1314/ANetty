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
package com.king.anetty;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.king.anetty.handler.StringChannelHandler;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

/**
 * @author <a href="mailto:jenly1314@gmail.com">Jenly</a>
 */
public class ANetty implements Netty {

    public static final String TAG = ANetty.class.getSimpleName();

    private static final int NETTY_INIT = 0x01;
    private static final int NETTY_CONNECT = 0x02;
    private static final int NETTY_SEND_MESSAGE = 0x03;


    public static final String DEFAULT_CHARSET = "UTF-8";
    /**
     * 默认消息结束分隔符
     */
    public static final byte[] DEFAULT_DELIMITER = new byte[]{0x04};
    /**
     * 默认传输内容最大长度 128K
     */
    public static final int DEFAULT_MAX_FRAME_LENGTH = 131072;
    /**
     * 是否debug，true则会显示打印日志
     */
    private boolean isDebug;

    private HandlerThread mHandlerThread;

    private Handler mHandler;

    private Handler mMainHandler;

    private String mHost;

    private int mPort;

    private ChannelFuture mChannelFuture;

    private Bootstrap mBootstrap;

    private EventLoopGroup mGroup;

    private ChannelInitializer<SocketChannel> mChannelInitializer;

    private OnConnectListener mOnConnectListener;

    private OnSendMessageListener mOnSendMessageListener;

    /**
     * 构造
     * @param onChannelHandler {@link OnChannelHandler}
     */
    public ANetty(final OnChannelHandler onChannelHandler){
        this(onChannelHandler,false);
    }

    /**
     * 构造
     * @param onChannelHandler {@link OnChannelHandler}
     * @param isDebug
     */
    public ANetty(final OnChannelHandler onChannelHandler, boolean isDebug){
        this.isDebug = isDebug;
        this.mChannelInitializer = new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch){
                //建立管道
                ChannelPipeline channelPipeline = ch.pipeline();
                //添加相关编码器，解码器，处理器等
                channelPipeline
                        .addLast(new StringEncoder())
                        .addLast(new StringDecoder())
                        .addLast(new StringChannelHandler(){
                            @Override
                            protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
                                super.channelRead0(ctx, msg);
                                if(isDebug){
                                    Log.d(TAG,"Received message:" + msg);
                                }
                                if(onChannelHandler!=null){
                                    mMainHandler.post(() -> onChannelHandler.onMessageReceived(ctx,msg));
                                }
                            }

                            @Override
                            public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                                super.exceptionCaught(ctx, cause);
                                if(isDebug){
                                    Log.w(TAG,cause.getMessage());
                                }
                                if(onChannelHandler!=null){
                                    mMainHandler.post(() -> onChannelHandler.onExceptionCaught(ctx,cause));
                                }
                            }
                        });
            }
        };
        initHandlerThread();
        mHandler.sendEmptyMessage(NETTY_INIT);
    }

    /**
     * 构造
     * @param channelInitializer {@link ChannelInitializer}
     * @param isDebug
     */
    public ANetty(ChannelInitializer<SocketChannel> channelInitializer, boolean isDebug){
        this.isDebug = isDebug;
        this.mChannelInitializer = channelInitializer;
        initHandlerThread();
        mHandler.sendEmptyMessage(NETTY_INIT);
    }

    /**
     * 构造
     * @param bootstrap {@link Bootstrap}
     */
    public ANetty(Bootstrap bootstrap){
        this(bootstrap,false);
    }

    /**
     * 构造
     * @param bootstrap {@link Bootstrap}
     * @param isDebug
     */
    public ANetty(Bootstrap bootstrap,boolean isDebug){
        this.mBootstrap = bootstrap;
        this.isDebug = isDebug;
        this.mGroup = bootstrap.config().group();
        initHandlerThread();
    }

    private void initHandlerThread(){
        mMainHandler = new Handler(Looper.getMainLooper());
        mHandlerThread = new HandlerThread(ANetty.class.getSimpleName());
        mHandlerThread.start();

        mHandler = new Handler(mHandlerThread.getLooper()){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what){
                    case NETTY_INIT:
                        handleNettyInit();
                        break;
                    case NETTY_CONNECT:
                        handleConnect();
                        break;
                    case NETTY_SEND_MESSAGE:
                        handleSendMessage(msg.obj);
                        break;
                }
            }
        };
    }

    private void handleNettyInit(){
        mBootstrap = new Bootstrap();
        mBootstrap.channel(NioSocketChannel.class);
        mGroup = new NioEventLoopGroup();
        mBootstrap.group(mGroup)
                .option(ChannelOption.TCP_NODELAY, true)// 消息立即发出去
                .option(ChannelOption.SO_REUSEADDR, true)
                .handler(new LoggingHandler(isDebug ? LogLevel.DEBUG : LogLevel.INFO));
        mBootstrap.handler(mChannelInitializer);
    }


    private void handleConnect(){
        try {
            mChannelFuture = mBootstrap.connect(mHost,mPort)
                    .addListener(future -> {
                        boolean isSuccess = future.isSuccess();
                        if(isDebug){
                            if(isSuccess){
                                Log.d(TAG,"Netty connect success.");
                            }else{
                                Log.d(TAG,"Netty connect failed.");
                            }
                        }
                        if(mOnConnectListener!=null){
                            mMainHandler.post(() -> {
                                if(isSuccess){
                                    mOnConnectListener.onSuccess();
                                }else{
                                    mOnConnectListener.onFailed();
                                }
                            });

                        }
                    })
                    .sync();
        } catch (Exception e) {
            e.printStackTrace();
            if(mOnConnectListener!=null){
                mMainHandler.post(()-> mOnConnectListener.onError(e));
            }
        }
    }

    private void handleSendMessage(Object msg){
        try{
            if(isOpen()) {
                mChannelFuture.channel().writeAndFlush(msg).addListener(future -> {
                    boolean isSuccess = future.isSuccess();
                    if (isDebug) {
                        if (isSuccess) {
                            Log.d(TAG, "Send message:" + msg);
                        }else{
                            Log.d(TAG, "Send failed.");
                        }
                    }
                    if(mOnSendMessageListener!=null){
                        mMainHandler.post(() -> mOnSendMessageListener.onSendMessage(msg,isSuccess));
                    }

                }).sync();
            }else{
                if(mOnSendMessageListener!=null){
                    mMainHandler.post(() -> mOnSendMessageListener.onSendMessage(msg,false));
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            if(mOnSendMessageListener!=null){
                mMainHandler.post(() -> mOnSendMessageListener.onException(e));
            }
        }
    }

    @Override
    public void connect(String host, int port) {
        if(isConnected()){
            return;
        }
        this.mHost = host;
        this.mPort = port;
        mHandler.sendEmptyMessage(NETTY_CONNECT);
    }

    @Override
    public void reconnect(long delayMillis) {
        close();
        mHandler.sendEmptyMessageDelayed(NETTY_CONNECT,delayMillis);
    }

    @Override
    public void sendMessage(Object msg) {
        mHandler.obtainMessage(NETTY_SEND_MESSAGE,msg).sendToTarget();
    }

    @Override
    public void setOnConnectListener(OnConnectListener listener) {
        this.mOnConnectListener = listener;
    }

    @Override
    public void setOnSendMessageListener(OnSendMessageListener listener) {
        this.mOnSendMessageListener = listener;
    }

    @Override
    public void close() {
        if(isOpen()){
            mChannelFuture.channel().close();
            if(isDebug){
                Log.d(TAG,"Netty channel connect closed.");
            }

        }
    }

    @Override
    public void disconnect() {
        if(isConnected()){
            mChannelFuture.channel().disconnect();
            if(isDebug) {
                Log.d(TAG, "Netty channel disconnected.");
            }
        }
    }


    @Override
    public boolean isConnected() {
        boolean isConnected = mChannelFuture != null && mChannelFuture.channel().isActive();
        if(isDebug && !isConnected){
            Log.w(TAG,"Netty channel is not connected.");
        }
        return isConnected;
    }

    @Override
    public boolean isOpen() {
        boolean isOpen = mChannelFuture != null && mChannelFuture.channel().isOpen();
        if(isDebug && !isOpen){
            Log.w(TAG,"Netty channel is not opened.");
        }
        return isOpen;
    }

    @Override
    public ChannelFuture getChannelFuture() {
        return mChannelFuture;
    }
}
