package com.wangshucheng.lp_guardian.hook;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.wangshucheng.lp_guardian.data.AppInfo;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * Created by wangshucheng on 9/3/17.
 */

public class MainHook implements IXposedHookLoadPackage {

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        final Object activityThread = XposedHelpers.callStaticMethod(XposedHelpers.findClass("android.app.ActivityThread", null), "currentActivityThread");
        final Context systemContext = (Context) XposedHelpers.callMethod(activityThread, "getSystemContext");
        Uri uri = Uri.parse("content://com.wangshucheng.lp_guardian.data.AppInfoProvider/app");
        Cursor cursor = systemContext.getContentResolver().query(uri, new String[]{"level"}, "package_name=?", new String[]{loadPackageParam.packageName}, null);
        if (cursor != null && cursor.moveToNext()) {
            int level = cursor.getInt(cursor.getColumnIndex("level"));
            XposedBridge.log("Location Guardian:" + loadPackageParam.packageName + "," + level);
            HookUtils.HookAndChange(loadPackageParam.classLoader, level);
            cursor.close();
        }
    }
}


