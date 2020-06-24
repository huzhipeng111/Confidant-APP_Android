package com.stratagile.pnrouter.utils

import android.content.Context
import android.os.Build
import android.util.Log
import com.socks.library.KLog

/**
 * Created by Anroid on 2017/3/1.
 */
object VersionUtil {
    /**
     * 返回当前程序版本名 int类型
     */
    fun getAppVersionCode(context: Context): Int {
        var versionCode = -1
        try {
            // ---get the package info---
            val pm = context.packageManager
            val pi = pm.getPackageInfo(context.packageName, 0)
            versionCode = pi.versionCode
        } catch (e: Exception) {
            Log.e("VersionInfo", "Exception", e)
        }
        return versionCode
    }

    /**
     * 返回当前程序版本名 int类型
     */
    fun getAppVersionName(context: Context): String {
        var versionName = ""
        try {
            // ---get the package info---
            val pm = context.packageManager
            val pi = pm.getPackageInfo(context.packageName, 0)
            versionName = pi.versionName
        } catch (e: Exception) {
            Log.e("VersionInfo", "Exception", e)
        }
        return versionName
    }

    /**
     * 品牌索引
     */
    val deviceBrand: Int
        get() {
            var type = 0
            val brand = Build.BRAND.toLowerCase()
            KLog.i(brand)
            type = when (brand) {
                "xiaomi" -> 2
                "redmi" -> 2
                "Redmi" -> 2
                "huawei" -> 3
                "honor" -> 3
                "zhongxing" -> 4
                "oppo" -> 5
                "vivo" -> 6
                "meizu" -> 7
                "onejia" -> 8
                else -> 0
            }
            KLog.i(brand)
            val brand1 = Build.BRAND.toLowerCase()
            return type
        }
}