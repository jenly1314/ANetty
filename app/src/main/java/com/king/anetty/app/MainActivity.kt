package com.king.anetty.app

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.king.anetty.ANetty
import com.king.anetty.Netty.OnChannelHandler
import com.king.anetty.Netty.OnConnectListener
import com.king.anetty.Netty.OnSendMessageListener
import com.king.anetty.app.databinding.ActivityMainBinding
import io.netty.channel.ChannelHandlerContext

/**
 * ANetty 使用示例
 * @author <a href="mailto:jenly1314@gmail.com">Jenly</a>
 * <p>
 * <a href="https://github.com/jenly1314">Follow me</a>
 */
class MainActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    var mHost = "192.168.100.100"

    var mPort = 6000

    lateinit var aNetty: ANetty

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.etHost.setText(mHost)
        binding.etPort.setText(mPort.toString())

        initANetty()
    }

    override fun onDestroy() {
        aNetty.close()
        super.onDestroy()
    }

    private fun getContext() = this

    /**
     * 初始化ANetty
     */
    private fun initANetty() {
        aNetty = ANetty(object : OnChannelHandler {
            override fun onMessageReceived(ctx: ChannelHandlerContext?, msg: String?) {
                binding.tvContent.append("接收：$msg\n")
            }

            override fun onExceptionCaught(ctx: ChannelHandlerContext?, e: Throwable?) {
                binding.tvContent.append("断开连接：$e\n")
                if (!aNetty.isConnected) {
                    binding.btnStart.text = "连接"
                    binding.btnStart.isEnabled = true
                }
            }

        }, true)

        aNetty.setOnConnectListener(object : OnConnectListener {
            override fun onSuccess() {
                binding.tvContent.append("连接成功\n")
                binding.btnStart.text = "断开"
                binding.btnStart.isEnabled = true
                binding.progressBar.isVisible = false
                Toast.makeText(getContext(), "连接成功", Toast.LENGTH_SHORT).show()
            }

            override fun onFailure(e: Throwable?) {
                e?.message?.also {
                    binding.tvContent.append("连接失败：$it\n")
                }
                binding.btnStart.text = "连接"
                binding.btnStart.isEnabled = true
                binding.progressBar.isVisible = false
                Toast.makeText(getContext(), "连接失败", Toast.LENGTH_SHORT).show()
            }
        })

        aNetty.setOnSendMessageListener(object : OnSendMessageListener {
            override fun onSendMessage(msg: Any?) {
                binding.tvContent.append("发送：$msg\n")
            }

            override fun onException(e: Throwable?) {
                if (!aNetty.isConnected) {
                    binding.tvContent.append("断开连接：$e\n")
                    binding.btnStart.text = "连接"
                    binding.btnStart.isEnabled = true
                }
            }
        })
    }

    /**
     * 连接或断开连接
     */
    private fun clickStart() {
        if (!aNetty.isConnected) {
            mHost = binding.etHost.text.toString()
            if (TextUtils.isEmpty(binding.etHost.text)) {
                Toast.makeText(getContext(), "请输入IP地址", Toast.LENGTH_SHORT).show()
                return
            }
            if (TextUtils.isEmpty(binding.etPort.text)) {
                Toast.makeText(getContext(), "请输入端口", Toast.LENGTH_SHORT).show()
                return
            }
            mPort = binding.etPort.text.toString().toInt()
            binding.btnStart.text = "连接中…"
            binding.btnStart.isEnabled = false
            binding.progressBar.isVisible = true
            aNetty.connect(mHost, mPort)
        } else {
            aNetty.disconnectBlocking()
            binding.tvContent.append("断开连接\n")
            binding.btnStart.text = "连接"
            binding.btnStart.isEnabled = true
        }
    }

    /**
     * 发送
     */
    private fun clickSend() {
        if (!TextUtils.isEmpty(binding.etContent.text)) {
            if (aNetty.isConnected) {
                val data = binding.etContent.text.toString()
                aNetty.sendMessage(data)
                binding.etContent.setText("")
            } else {
                Toast.makeText(getContext(), "未连接", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun clickClear() {
        binding.tvContent.text = ""
    }

    fun onClick(v: View) {
        when (v.id) {
            R.id.btnStart -> clickStart()
            R.id.btnSend -> clickSend()
            R.id.btnClear -> clickClear()
        }
    }
}