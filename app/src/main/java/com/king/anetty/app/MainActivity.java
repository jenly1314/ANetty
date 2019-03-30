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
package com.king.anetty.app;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.king.anetty.ANetty;
import com.king.anetty.Netty;

public class MainActivity extends AppCompatActivity {

    private EditText etHost;
    private EditText etPort;
    private EditText etContent;
    private TextView tvContent;

    private Toast mToast;

    //TODO 配置您的TCP连接主机
    private String mHost = "192.168.100.48";
    //TODO 配置您的TCP连接端口
    private int mPort = 6000;

    private Netty mNetty;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etHost = findViewById(R.id.etHost);
        etPort = findViewById(R.id.etPort);
        etContent = findViewById(R.id.etContent);
        tvContent = findViewById(R.id.tvContent);
        tvContent.setMovementMethod(ScrollingMovementMethod.getInstance());
        etHost.setText(mHost);
        etPort.setText(String.valueOf(mPort));
        initNetty();
    }

    /**
     * 初始化Netty
     */
    private void initNetty(){
        mNetty = new ANetty(new Netty.OnChannelHandler() {
            @Override
            public void onMessageReceived(String msg) {
                //接收到消息
                tvContent.append(msg + "\n");
            }

            @Override
            public void onExceptionCaught(Throwable e) {

            }
        }, true);

        mNetty.setOnConnectListener(new Netty.OnConnectListener() {
            @Override
            public void onSuccess() {
                //TODO 连接成功
                showToast("连接成功");
            }

            @Override
            public void onFailed() {
                //TODO 连接失败
                showToast("连接失败");
            }

            @Override
            public void onError(Exception e) {
                //TODO 连接异常
                showToast("连接异常");
                Log.e(ANetty.TAG,e.getMessage());
            }
        });

    }

    /**
     * 连接Netty
     */
    private void connectNetty(){

        mHost = etHost.getText().toString();
        if(!TextUtils.isEmpty(etPort.getText())){
            mPort = Integer.parseInt(etPort.getText().toString());
        }

        if(TextUtils.isEmpty(mHost)){
            showToast("请配置TCP连接的主机");
            return;
        }
        //连接Netty
        mNetty.connect(mHost,mPort);
    }

    /**
     * 发送消息
     * @param msg
     */
    private void sendNettyMsg(String msg){
        if(mNetty.isConnected()){
            mNetty.sendMessage(msg);
        }else{
            showToast("Netty未连接");
        }

    }

    private void showToast(String text){
        if(mToast == null){
            mToast = Toast.makeText(MainActivity.this,text,Toast.LENGTH_SHORT);
        }
        mToast.setText(text);
        mToast.show();

    }

    @Override
    protected void onDestroy() {
        mNetty.disconnect();
        super.onDestroy();
    }

    private void clickSend(){
        if(TextUtils.isEmpty(etContent.getText())){
            showToast("请输入消息内容");
            return;
        }

        sendNettyMsg(etContent.getText().toString());

//        String json = "{\"body\":{\"cNo\":\"粤BD82700\",\"dPermit\":\"132627\",\"lat\":22.53421,\"lon\":114.09242,\"mac\":\"15e1f216f0053c6999968d00480a27b17d2a1a33f0e907fcfd54b360eed1e474\",\"time\":1553562614264},\"head\":{\"sn\":1,\"time\":1553562614265,\"type\":\"LOGIN_REQ\",\"ver\":\"1.0.1\"}}";
//        sendNettyMsg(json);
    }

    public void onClick(View v){
        switch (v.getId()){
            case R.id.btnConnect:
                connectNetty();
                break;
            case R.id.btnSend:
                clickSend();
                break;
        }
    }
}
