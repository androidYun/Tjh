package com.zy.tjh.utils

import android.content.Context
import android.content.Intent
import android.net.Uri

/**
 * @ author guiyun.li
 * @ Email xyz_6776.@163.com
 * @ date 30/09/2019.
 * description:
 */
object PayUtils {
    /**
     * 检测是否安装支付宝
     * @param context
     * @return
     */
    fun isAliPayInstalled(context: Context): Boolean {
        val uri = Uri.parse("alipays://platformapi/startApp")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        val componentName = intent.resolveActivity(context.packageManager)
        return componentName != null
    }

    /**
     * 检测是否安装微信
     * @param context
     * @return
     */
    fun isWeixinAvilible(context: Context): Boolean {
        val packageManager = context.packageManager// 获取packagemanager
        val pinfo = packageManager.getInstalledPackages(0)// 获取所有已安装程序的包信息
        if (pinfo != null) {
            for (i in pinfo.indices) {
                val pn = pinfo[i].packageName
                if (pn == "com.tencent.mm") {
                    return true
                }
            }
        }
        return false
    }
}
