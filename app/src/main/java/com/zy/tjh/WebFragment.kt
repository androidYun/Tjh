package com.zy.tjh

import android.net.http.SslError
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v4.app.Fragment
import android.text.TextUtils
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import android.widget.Toast
import com.alipay.sdk.app.AuthTask
import com.tencent.mm.opensdk.modelpay.PayReq
import com.tencent.mm.opensdk.openapi.IWXAPI
import com.tencent.mm.opensdk.openapi.WXAPIFactory
import com.zy.tjh.common.Constants
import com.zy.tjh.utils.AuthResult
import com.zy.tjh.utils.OrderInfoUtil2_0
import com.zy.tjh.utils.PayResult
import com.zy.tjh.utils.PayUtils
import kotlinx.android.synthetic.main.fragment_web.*


/**
 * @ author guiyun.li
 * @ Email xyz_6776.@163.com
 * @ date 10/07/2019.
 * description:
 */
open class WebFragment : Fragment() {


    private val url by lazy { arguments?.getString(WEB_URL_KEY) ?: "" }

    private lateinit var api: IWXAPI


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        api = WXAPIFactory.createWXAPI(context, Constants.APP_ID)
        return inflater.inflate(R.layout.fragment_web, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initWebView()
        initWebViewClient()
        webView.loadUrl(url)
    }


    private fun initWebView() {
        val setting = webView.settings
        //自定义UA
        var userAgent = setting.userAgentString
        userAgent += "WebViewDemo"
        setting.userAgentString = userAgent

        /**
         * Webview在安卓5.0之前默认允许其加载混合网络协议内容
         * 在安卓5.0之后，默认不允许加载http与https混合内容，需要设置webview允许其加载混合网络协议内容
         */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setting.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        }

        //自动播放音频autoplay
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            setting.mediaPlaybackRequiresUserGesture = false
        }

        setting.javaScriptEnabled = true//设置WebView是否允许执行JavaScript脚本,默认false
        setting.setSupportZoom(true)//WebView是否支持使用屏幕上的缩放控件和手势进行缩放,默认值true
        setting.builtInZoomControls = true//是否使用内置的缩放机制
        setting.displayZoomControls = false//使用内置的缩放机制时是否展示缩放控件,默认值true

        setting.useWideViewPort = true//是否支持HTML的“viewport”标签或者使用wide viewport
        setting.loadWithOverviewMode = true//是否允许WebView度超出以概览的方式载入页面,默认false
        setting.layoutAlgorithm =
            WebSettings.LayoutAlgorithm.SINGLE_COLUMN//设置布局,会引起WebView的重新布局(relayout),默认值NARROW_COLUMNS

        setting.setRenderPriority(WebSettings.RenderPriority.HIGH)//线程优先级(在API18以上已废弃。不建议调整线程优先级，未来版本不会支持这样做)
        setting.setEnableSmoothTransition(true)//已废弃,将来会成为空操作（no-op）,设置当panning或者缩放或者持有当前WebView的window没有焦点时是否允许其光滑过渡,若为true,WebView会选择一个性能最大化的解决方案。例如过渡时WebView的内容可能不更新。若为false,WebView会保持精度（fidelity）,默认值false。
        setting.cacheMode = WebSettings.LOAD_NO_CACHE//重写使用缓存的方式，默认值LOAD_DEFAULT
        setting.pluginState = WebSettings.PluginState.ON//在API18以上已废弃。未来将不支持插件,不要使用
        setting.javaScriptCanOpenWindowsAutomatically = true//让JavaScript自动打开窗口,默认false

        //webview 中localStorage无效的解决方法
        setting.domStorageEnabled = true//DOM存储API是否可用,默认false
        setting.setAppCacheMaxSize((1024 * 1024 * 8).toLong())//设置应用缓存内容的最大值
        setting.allowFileAccess = true//是否允许访问文件,默认允许
        setting.setAppCacheEnabled(true)//应用缓存API是否可用,默认值false,结合setAppCachePath(String)使用


        setting.domStorageEnabled = true
    }

    private fun initWebViewClient() {
        val webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                if (url.contains("tjh://weixin", false)) {
                    context?.let {
                        if (!PayUtils.isWeixinAvilible(context!!)) {
                            Toast.makeText(context, "请安装微信", Toast.LENGTH_LONG).show()
                            return false
                        }


                    }

                    return false
                } else if (url.contains("tjh://alipay")) {

                    context?.let {
                        if (!PayUtils.isAliPayInstalled(context!!)) {
                            Toast.makeText(context, "请支付宝微信", Toast.LENGTH_LONG).show()
                            return false
                        }


                    }

                    return false
                }



                return super.shouldOverrideUrlLoading(view, url)
            }

            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest): Boolean {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    webView.loadUrl(request.url.toString())
                }
                return super.shouldOverrideUrlLoading(view, request)
            }


            override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: SslError?) {
                handler?.proceed()// 接受所有网站的证书
            }
        }
        webView.webViewClient = webViewClient


        webView.setOnKeyListener(
            object : View.OnKeyListener {
                override fun onKey(view: View, i: Int, keyEvent: KeyEvent): Boolean {
                    //这里处理返回键事件
                    if (webView.canGoBack()) {
                        webView.goBack()
                        return true
                    }
                    return false
                }
            })
    }

    private fun toWxchatPay(
        appId: String,
        partnerId: String,
        prepayId: String,
        nonceStr: String,
        sign: String,
        data: String
    ) {
        val req = PayReq()
        req.appId = appId
        req.partnerId = partnerId
        req.prepayId = prepayId
        req.nonceStr = nonceStr
        req.timeStamp = System.currentTimeMillis().toString()
        req.packageValue = "package"
        req.sign = sign
        req.extData = data; // optional
        // 在支付之前，如果应用没有注册到微信，应该先调用IWXMsg.registerApp将应用注册到微信
        api.sendReq(req)
    }

    val RSA2_PRIVATE = ""
    val RSA_PRIVATE = ""

    private val SDK_PAY_FLAG = 1
    private val SDK_AUTH_FLAG = 2
    private fun toAlipayPay(pId: String, appId: String, TARGET_ID: String) {
        val rsa2 = RSA2_PRIVATE.length > 0
        val authInfoMap = OrderInfoUtil2_0.buildAuthInfoMap(pId, appId, TARGET_ID, rsa2)
        val info = OrderInfoUtil2_0.buildOrderParam(authInfoMap)

        val privateKey = if (rsa2) RSA2_PRIVATE else RSA_PRIVATE
        val sign = OrderInfoUtil2_0.getSign(authInfoMap, privateKey, rsa2)
        val authInfo = "$info&$sign"
        val authRunnable = Runnable {
            // 构造AuthTask 对象
            val authTask = AuthTask(activity)
            // 调用授权接口，获取授权结果
            val result = authTask.authV2(authInfo, true)

            val msg = Message()
            msg.what = SDK_AUTH_FLAG
            msg.obj = result
            mHandler.sendMessage(msg)
        }

        // 必须异步调用
        val authThread = Thread(authRunnable)
        authThread.start()

    }

    private val mHandler = object : Handler() {

        override fun handleMessage(msg: Message) {
            when (msg.what) {
                SDK_PAY_FLAG -> {
                    val payResult = PayResult(msg.obj as Map<String, String>)
                    /**
                     * 对于支付结果，请商户依赖服务端的异步通知结果。同步通知结果，仅作为支付结束的通知。
                     */
                    val resultInfo = payResult.getResult()// 同步返回需要验证的信息
                    val resultStatus = payResult.getResultStatus()
                    // 判断resultStatus 为9000则代表支付成功
                    if (TextUtils.equals(resultStatus, "9000")) {
                        // 该笔订单是否真实支付成功，需要依赖服务端的异步通知。

                    } else {
                        // 该笔订单真实的支付结果，需要依赖服务端的异步通知。

                    }
                }
                SDK_AUTH_FLAG -> {
                    val authResult = AuthResult(msg.obj as Map<String, String>, true)
                    val resultStatus = authResult.getResultStatus()

                    // 判断resultStatus 为“9000”且result_code
                    // 为“200”则代表授权成功，具体状态码代表含义可参考授权接口文档
                    if (TextUtils.equals(resultStatus, "9000") && TextUtils.equals(authResult.getResultCode(), "200")) {
                        // 获取alipay_open_id，调支付时作为参数extern_token 的value
                        // 传入，则支付账户为该授权账户

                    } else {
                        // 其他状态值则为授权失败

                    }
                }
                else -> {
                }
            }
        }
    }

    companion object {

        private const val WEB_URL_KEY = "WEB_URL_KEY"

        fun getInstance(url: String) = WebFragment().apply {
            arguments = Bundle().apply {
                putString(WEB_URL_KEY, url)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (webView != null) {
            webView.onResume()
            //恢复pauseTimers状态
            webView.resumeTimers()
            webView.reload()
        }
    }

    override fun onPause() {
        super.onPause()
        if (webView != null) {
            webView.onPause()
            //恢复pauseTimers状态
            webView.pauseTimers()
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        if (webView != null) {
            webView.visibility = View.GONE
            webView.loadUrl("about:blank")
            webView.stopLoading()
            webView.webChromeClient = null
            webView.webViewClient = null
            webView.destroy()
        }
    }

}