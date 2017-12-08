package com.wangshucheng.lp_guardian.BmobSever;


import android.app.Activity;
import android.telephony.TelephonyManager;

import cn.bmob.v3.BmobObject;

/**
 * Created by wangshucheng on 7/4/17.
 */


public class Upload extends BmobObject {
    public String tvName;
    public String tvPackageName;
    public String tvLevel;
    public String tvImei;
    public String tvLocationGuardian;


    public String getTvPackageName() {
        return tvPackageName;
    }
    public void setTvPackageName(String tvPackageName) {
        this.tvPackageName = tvPackageName;
    }

    public String getTvName() {
        return tvName;
    }
    public void setTvName(String tvName) {
            this.tvName = tvName;
    }

    public String getTvLevel() {
        return tvLevel;
    }
    public void setTvLevel(String tvLevel) {
        this.tvLevel = tvLevel;
    }

    public String getTvImei() {
        return tvImei;
    }
    public void setTvImei(String tvImei) {
        this.tvImei = tvImei;
    }

    public String getTvLocationGuardian() {
        return tvLocationGuardian;
    }
    public void setTvLocationGuardian(String tvLocationGuardian) {
        this.tvLocationGuardian = tvLocationGuardian;
    }
}

