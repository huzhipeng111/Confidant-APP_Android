package com.stratagile.pnrouter.utils;

import android.app.AppOpsManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.AppOpsManagerCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;

import com.stratagile.pnrouter.utils.romUtils.*;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by liyong on 2018/9/21.
 */
public class PermissionUtils {

    public static final String TAG = "PermissionUtils";

    /**
     * 检查悬浮窗权限
     *
     * @param context
     * @return
     */
    public boolean checkFloatWindowPermission(Context context) {
        //6.0 版本之后由于 google 增加了对悬浮窗权限的管理，所以方式就统一了
        if (Build.VERSION.SDK_INT < 23) {
            if (RoUtils.checkIsMiuiRom()) {
                return MiuiUtils.checkFloatWindowPermission(context);
            } else if (RoUtils.checkIsMeizuRom()) {
                return MeizuUtils.checkFloatWindowPermission(context);
            } else if (RoUtils.checkIsHuaweiRom()) {
                return HuaweiUtils.checkFloatWindowPermission(context);
            } else if (RoUtils.checkIs360Rom()) {
                return QikuUtils.checkFloatWindowPermission(context);
            } else if (RoUtils.checkIsOppoRom()) {
                return OppoUtils.checkFloatWindowPermission(context);
            }
        }
        return commonROMPermissionCheck(context);
    }

    public static boolean hasPermission(@NonNull Context context, @NonNull String permission) {
        List<String> permisstions = new ArrayList<>();
        permisstions.add(permission);
        return hasPermission(context, permisstions);
    }

    /**
     * 系统层的权限判断
     *
     * @param context     上下文
     * @param permissions 申请的权限 Manifest.permission.READ_CONTACTS
     * @return 是否有权限 ：其中有一个获取不了就是失败了
     */
    public static boolean hasPermission(@NonNull Context context, @NonNull List<String> permissions) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return true;
        for (String permission : permissions) {
            try {
                String op = AppOpsManagerCompat.permissionToOp(permission);
                if (TextUtils.isEmpty(op)) continue;
                int result = AppOpsManagerCompat.noteOp(context, op, android.os.Process.myUid(), context.getPackageName());
                if (result == AppOpsManagerCompat.MODE_IGNORED) return false;
                AppOpsManager appOpsManager = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
                String ops = AppOpsManager.permissionToOp(permission);
                int locationOp = appOpsManager.checkOp(ops, Binder.getCallingUid(), context.getPackageName());
                if (locationOp == AppOpsManager.MODE_IGNORED) return false;
                result = ContextCompat.checkSelfPermission(context, permission);
                if (result != PackageManager.PERMISSION_GRANTED) return false;
            } catch (Exception ex) {
                Log.e(TAG, "[hasPermission] error ", ex);
            }
        }
        return true;
    }

    /**
     * 跳转到权限设置界面
     *
     * @param context
     */
    public static void toPermissionSetting(Context context) throws NoSuchFieldException, IllegalAccessException {
        if (Build.VERSION.SDK_INT < 23) {
            if (RoUtils.checkIsMiuiRom()) {
                MiuiUtils.applyMiuiPermission(context);
            } else if (RoUtils.checkIsMeizuRom()) {
                MeizuUtils.applyPermission(context);
            } else if (RoUtils.checkIsHuaweiRom()) {
                HuaweiUtils.applyPermission(context);
            } else if (RoUtils.checkIs360Rom()) {
                QikuUtils.applyPermission(context);
            } else if (RoUtils.checkIsOppoRom()) {
                OppoUtils.applyOppoPermission(context);
            } else {
                RoUtils.getAppDetailSettingIntent(context);
            }
        } else {
            if (RoUtils.checkIsMeizuRom()) {
                MeizuUtils.applyPermission(context);
            } else {
                if (RoUtils.checkIsOppoRom() || RoUtils.checkIsVivoRom()
                        || RoUtils.checkIsHuaweiRom() || RoUtils.checkIsSamsunRom()) {
                    RoUtils.getAppDetailSettingIntent(context);
                } else if (RoUtils.checkIsMiuiRom()) {
                    MiuiUtils.toPermisstionSetting(context);
                } else {
                    RoUtils.commonROMPermissionApplyInternal(context);
                }
            }
        }
    }

    private static boolean commonROMPermissionCheck(Context context) {
        //最新发现魅族6.0的系统这种方式不好用，天杀的，只有你是奇葩，没办法，单独适配一下
        if (RoUtils.checkIsMeizuRom()) {
            return MeizuUtils.checkFloatWindowPermission(context);
        } else {
            Boolean result = true;
            if (Build.VERSION.SDK_INT >= 23) {
                try {
                    Class clazz = Settings.class;
                    Method canDrawOverlays = clazz.getDeclaredMethod("canDrawOverlays", Context.class);
                    result = (Boolean) canDrawOverlays.invoke(null, context);
                } catch (Exception e) {
                    Log.e(TAG, Log.getStackTraceString(e));
                }
            }
            return result;
        }
    }
}
