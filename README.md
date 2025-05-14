# ANetty

![Image](app/src/main/ic_launcher-web.png)

[![MavenCentral](https://img.shields.io/maven-central/v/com.github.jenly1314/anetty?logo=sonatype)](https://repo1.maven.org/maven2/com/github/jenly1314/ANetty)
[![JitPack](https://img.shields.io/jitpack/v/github/jenly1314/ANetty?logo=jitpack)](https://jitpack.io/#jenly1314/ANetty)
[![CI](https://img.shields.io/github/actions/workflow/status/jenly1314/ANetty/gradle.yml?logo=github)](https://github.com/jenly1314/ANetty/actions/workflows/gradle.yml)
[![Download](https://img.shields.io/badge/download-APK-brightgreen?logo=github)](https://raw.githubusercontent.com/jenly1314/ANetty/master/app/release/app-release.apk)
[![API](https://img.shields.io/badge/API-21%2B-brightgreen?logo=android)](https://developer.android.com/guide/topics/manifest/uses-sdk-element#ApiLevels)
[![License](https://img.shields.io/github/license/jenly1314/ANetty?logo=open-source-initiative)](https://opensource.org/licenses/LICENSE-2-0)


ANetty for Android 是基于Netty二次封装的Android链路通讯库，用以快速开发高性能，高可靠性的网络交互。在保证易于开发的同时还保证其应用的性能，稳定性和伸缩性。


## 效果展示
![Image](GIF.gif)

> 你也可以直接下载 [演示App](https://raw.githubusercontent.com/jenly1314/ANetty/master/app/release/app-release.apk) 体验效果

## 引入

### Gradle:

1. 在Project的 **build.gradle** 或 **setting.gradle** 中添加远程仓库

    ```gradle
    repositories {
        //...
        mavenCentral()
    }
    ```

2. 在Module的 **build.gradle** 中添加依赖项
   ```gradle
   // ANetty
   implementation 'com.github.jenly1314:anetty:1.1.0'

   ```

## 使用

### 代码示例

> ANetty暂并不提供Netty服务端封装库，因为在真实的场景中，服务端一般都依赖项目对应的业务。为了方便演示ANetty客户端，这里提供了一个简易的[服务端demo](server)；直接以Java的形式直接在main方法中直接运行ANettyServer即可启动Netty服务。（此Demo主要是为了方便测试与ANetty的客户端进行通信）

Netty服务端：
```Java
   // 初始化并启动服务
   new ANettyServer().start(port);
```

Netty客户端：
```Java
    // 初始化Netty
   Netty mNetty = new ANetty(new Netty.OnChannelHandler() {
      @Override
      public void onMessageReceived(ChannelHandlerContext ctx,String msg) {
         // TODO 接收到的消息
      }
   
     @Override
     public void onExceptionCaught(ChannelHandlerContext ctx,Throwable e) {
         // TODO 异常
     }
   }, true);
   // 设置连接监听
   mNetty.setOnConnectListener(new Netty.OnConnectListener() {
      @Override
      public void onSuccess() {
         // TODO 连接成功
      }
      
      @Override
      public void onFailure(Throwable e) {
         // TODO 连接失败
      }
   
   });
   // 设置发送消息监听
   mNetty.setOnSendMessageListener(new Netty.OnSendMessageListener() {
      @Override
      public void onSendMessage(Object msg) {
         // TODO 发送的消息
      }
      
      @Override
      public void onException(Throwable e) {
         //TODO 异常
      }
   });

   // 初始化后，建立连接
   mNetty.connect(host, port);
   
   //---------------------
   // ...
   // 发送消息
   mNetty.sendMessage(msg);
   
   //---------------------
   // ...
   // 断开连接
   mNetty.disconnect();
   
   //---------------------
   // ...
   // 重连
   mNetty.reconnect(delayMillis);
   
   //---------------------
   // ...
   // 关闭连接
   mNetty.close();    

```

> 如需测试；可以在Netty服务端启动后；然后使用Netty客户端输入对应的连接地址和端口，即可与服务端进行通信。（也可直接使用[演示App](https://raw.githubusercontent.com/jenly1314/ANetty/master/app/release/app-release.apk)进行测试。）

更多使用详情，请查看[app](app)中的源码使用示例或直接查看 [API帮助文档](https://jitpack.io/com/github/jenly1314/ANetty/latest/javadoc/)

## 相关推荐

- [EasyChat](https://github.com/yetel/EasyChatAndroidClient) 一款即时通讯APP。
- [ASocket](https://github.com/jenly1314/ASocket) 一个TCP/UDP协议的封装库，方便快速实现TCP的长连接与UDP的单播、组播、广播等相关通信。
- [AWebSocket](https://github.com/jenly1314/AWebSocket) 基于okhttp封装的 WebSocket，简洁易用。

## 版本日志

#### v1.1.0：2023-11-18
* 迁移发布至 MavenCentral
* minSdk要求从 16+ 提升至 21+
* compileSdk更新至33
* 更新Gradle至8.0
* 更新netty依赖至v4.1.101.Final
* 修改Netty相关定义，对外提供更多可用的方法

#### v1.0.3：2019-11-13 （之前发布的版本是在JCenter）
* OnChannelHandler接口内方法新增ChannelHandlerContext参数

#### v1.0.2：2019-10-30
* 新增setOnSendMessageListener方法
* 更新Netty依赖至4.1.43.Final

#### v1.0.1：2019-9-26
* 移除support依赖
* 更新Netty依赖至4.1.42.Final

#### v1.0.0：2019-3-30
* ANetty初始版本

---

![footer](https://jenly1314.github.io/page/footer.svg)
