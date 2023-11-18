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

import java.net.SocketAddress;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;

/**
 * @author <a href="mailto:jenly1314@gmail.com">Jenly</a>
 * <p>
 * <a href="https://github.com/jenly1314">Follow me</a>
 */
@SuppressWarnings("unused")
public interface Netty {

    /**
     * 建立连接
     *
     * @param host 主机
     * @param port 端口
     */
    void connect(String host, int port);

    /**
     * 建立连接
     *
     * @param socketAddress 连接地址
     */
    void connect(SocketAddress socketAddress);

    /**
     * 同步阻塞的方式建立连接
     *
     * @param host 主机
     * @param port 端口
     * @return 是否连接成功
     */
    boolean connectBlocking(String host, int port);

    /**
     * 同步阻塞的方式建立连接
     *
     * @param socketAddress 连接地址
     * @return 是否连接成功
     */
    boolean connectBlocking(SocketAddress socketAddress);

    /**
     * 重连
     * @param delayMillis 重连延迟毫秒数
     */
    void reconnect(long delayMillis);

    /**
     * 阻塞的方式同步重连
     *
     * @return 是否重连成功
     */
    boolean reconnectBlocking();

    /**
     * 发送消息
     *
     * @param msg 消息数据
     */
    void sendMessage(Object msg);

    /**
     * 阻塞的方式同步发送消息
     *
     * @param msg 消息数据
     * @return 是否发送成功
     */
    boolean sendMessageBlocking(Object msg);

    /**
     * 设置连接监听
     *
     * @param listener {@link OnConnectListener}
     */
    void setOnConnectListener(OnConnectListener listener);

    /**
     * 设置消息发送监听
     *
     * @param listener {@link OnSendMessageListener}
     */
    void setOnSendMessageListener(OnSendMessageListener listener);

    /**
     * 关闭连接
     */
    void close();

    /**
     * 同步阻塞的方式关闭连接
     * @return 是否关闭
     */
    boolean closeBlocking();

    /**
     * 断开链接
     */
    void disconnect();

    /**
     * 同步阻塞的方式断开连接
     * @return 是否断开连接
     */
    boolean disconnectBlocking();

    /**
     * 是否连接
     *
     * @return 是否已连接
     */
    boolean isConnected();

    /**
     * 是否打开
     *
     * @return 连接通道是否打开
     */
    boolean isOpen();

    /**
     * 获取{@link ChannelFuture}
     *
     * @return 获取连接的 {@link ChannelFuture}
     */
    ChannelFuture getChannelFuture();

    /**
     * 连接监听
     */
    interface OnConnectListener {
        /**
         * 连接成功
         */
        void onSuccess();

        /**
         * 连接失败
         *
         * @param e 异常信息
         */
        void onFailure(Throwable e);
    }

    /**
     * 通道消息处理（接收消息）
     */
    interface OnChannelHandler {
        /**
         * 接收消息
         *
         * @param ctx {@link  ChannelHandlerContext}
         * @param msg 接收到的消息
         */
        void onMessageReceived(ChannelHandlerContext ctx, String msg);

        /**
         * 异常
         *
         * @param ctx {@link ChannelHandlerContext}
         * @param e 异常信息
         */
        void onExceptionCaught(ChannelHandlerContext ctx, Throwable e);
    }

    /**
     * 发送消息监听
     */
    interface OnSendMessageListener {
        /**
         * 发送消息
         *
         * @param msg 消息数据
         */
        void onSendMessage(Object msg);

        /**
         * 异常
         *
         * @param e 异常信息
         */
        void onException(Throwable e);
    }
}
