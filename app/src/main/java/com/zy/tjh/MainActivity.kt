package com.zy.tjh

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.zy.tjh.R

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val beginTransaction = supportFragmentManager.beginTransaction()
        beginTransaction.add(R.id.frameLayout, WebFragment.getInstance("ttps://wx.tenpay.com/cgi-bin/mmpayweb-bin/checkmweb?prepay_id=wx20161110163838f231619da20804912345&package=1037687096&redirect_url=https://www.wechatpay.com.cn"))
        beginTransaction.commit()
        // tjhpay//weixin?appid=xxxxx&partnerid=xxxxxprepayid=xxxxxxsign=xxxxxxx
    }
}
