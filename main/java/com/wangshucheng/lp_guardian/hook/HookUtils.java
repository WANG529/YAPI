package com.wangshucheng.lp_guardian.hook;

import android.content.Context;
import android.location.Location;
import android.os.Build;
import android.telephony.CellIdentityCdma;
import android.telephony.CellIdentityGsm;
import android.telephony.CellIdentityLte;
import android.telephony.CellIdentityWcdma;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.CellLocation;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.widget.Toast;

import com.wangshucheng.lp_guardian.BmobSever.Upload;
import com.wangshucheng.lp_guardian.data.AppInfo;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * Created by wangshucheng on 9/3/17.
 */

public class HookUtils {

    public static DecimalFormat df = new DecimalFormat("#.00");



    static void HookAndChange(ClassLoader classLoader, int level) {
        //handleLoadPackage(level, classLoader, loadPackageParam);
        //onlyGoogleMap(classLoader);
        switch (level) {
            case 1:
                return;
            case 2:
                onlyTrack(classLoader);
                break;
            case 3:
                onlyCity(classLoader);
                break;
            case 4:
                offLocation(classLoader);
                break;
        }
    }

    private static ArrayList getCell(int mcc, int mnc, int lac, int cid, int sid, int networkType) {
        ArrayList arrayList = new ArrayList();

        CellInfoGsm cellInfoGsm = (CellInfoGsm) XposedHelpers.newInstance(CellInfoGsm.class);
        XposedHelpers.callMethod(cellInfoGsm, "setCellIdentity", XposedHelpers.newInstance(CellIdentityGsm.class, new Object[]{
                Integer.valueOf(mcc), Integer.valueOf(mnc), Integer.valueOf(lac), Integer.valueOf(cid)
        }));

        CellInfoCdma cellInfoCdma = (CellInfoCdma) XposedHelpers.newInstance(CellInfoCdma.class);
        XposedHelpers.callMethod(cellInfoCdma, "setCellIdentity", XposedHelpers.newInstance(CellIdentityCdma.class, new Object[]{
                Integer.valueOf(lac), Integer.valueOf(sid), Integer.valueOf(cid), Integer.valueOf(0), Integer.valueOf(0)
        }));

        CellInfoWcdma cellInfoWcdma = (CellInfoWcdma) XposedHelpers.newInstance(CellInfoWcdma.class);
        XposedHelpers.callMethod(cellInfoWcdma, "setCellIdentity", XposedHelpers.newInstance(CellIdentityWcdma.class, new Object[]{
                Integer.valueOf(mcc), Integer.valueOf(mnc), Integer.valueOf(lac), Integer.valueOf(cid), Integer.valueOf(300)
        }));

        CellInfoLte cellInfoLte = (CellInfoLte) XposedHelpers.newInstance(CellInfoLte.class);
        XposedHelpers.callMethod(cellInfoLte, "setCellIdentity", XposedHelpers.newInstance(CellIdentityLte.class, new Object[]{
                Integer.valueOf(mcc), Integer.valueOf(mnc), Integer.valueOf(cid), Integer.valueOf(300), Integer.valueOf(lac)
        }));


        if (networkType == 1 || networkType == 2) {
            arrayList.add(cellInfoGsm);
        } else if (networkType == 13) {
            arrayList.add(cellInfoLte);
        } else if (networkType == 4 || networkType == 5 || networkType == 6 || networkType == 7 || networkType == 12 || networkType == 14) {
            arrayList.add(cellInfoCdma);
        } else if (networkType == 3 || networkType == 8 || networkType == 9 || networkType == 10 || networkType == 15) {
            arrayList.add(cellInfoWcdma);
        }
        return arrayList;
    }

    private static void onlyTrack(ClassLoader classLoader) {
        offCell(classLoader);
        XposedHelpers.findAndHookMethod(Location.class, "getLatitude", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                double latitude = (double) param.getResult();
                int fInt = (int) latitude;
                BigDecimal b1 = new BigDecimal(Double.toString(latitude));
                BigDecimal b2 = new BigDecimal(Integer.toString(fInt));
                double fPoint = 40 + b1.subtract(b2).doubleValue();
                XposedBridge.log("latitude:" + latitude + ", doublevalue:" + fPoint);
                if (latitude > 0.0)
                    param.setResult(fPoint);
            }
        });

        XposedHelpers.findAndHookMethod(Location.class, "getLongitude", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                double longitude = (double) param.getResult();
                XposedBridge.log("longitude:" + longitude);
                int fInt = (int) longitude;
                BigDecimal b1 = new BigDecimal(Double.toString(longitude));
                BigDecimal b2 = new BigDecimal(Integer.toString(fInt));
                double fPoint = 116 + b1.subtract(b2).doubleValue();
                XposedBridge.log("longitude:" + longitude + ", doublevalue:" + fPoint);
                if (longitude > 0.0)
                    param.setResult(fPoint);
            }
        });
    }

    private static void onlyCity(ClassLoader classLoader) {
        offCell(classLoader); //knock cell info & net info
        XposedHelpers.findAndHookMethod(Location.class, "getLatitude", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                double latitude = (double) param.getResult();
                //double fPoint = Math.floor(latitude);
                double fPoint = Double.parseDouble(df.format(latitude));
                XposedBridge.log("latitude:" + latitude + ", intValue:" + fPoint);
                if (latitude > 0.0)
                    param.setResult(fPoint);
            }
        });

        XposedHelpers.findAndHookMethod(Location.class, "getLongitude", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                double longitude = (double) param.getResult();
                XposedBridge.log("longitude:" + longitude);
                //double fPoint = Math.floor(longitude);
                double fPoint = Double.parseDouble(df.format(longitude));
                XposedBridge.log("longitude:" + longitude + ", intValue:" + fPoint);
                if (longitude > 0.0)
                    param.setResult(fPoint);
            }
        });
    }


    private static void offLocation(ClassLoader classLoader) {
        offCell(classLoader);
        XposedHelpers.findAndHookMethod(Location.class, "getLatitude", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                param.setResult(0.0);
            }
        });

        XposedHelpers.findAndHookMethod(Location.class, "getLongitude", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                param.setResult(0.0);
            }
        });
    }

    private static void offCell(ClassLoader classLoader) {
        XposedHelpers.findAndHookMethod("android.telephony.TelephonyManager", classLoader,
                "getNetworkOperatorName", new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        param.setResult("");
                    }
                });

        XposedHelpers.findAndHookMethod("android.telephony.TelephonyManager", classLoader,
                "getNetworkOperator", new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        param.setResult("");
                    }
                });

        XposedHelpers.findAndHookMethod("android.telephony.TelephonyManager", classLoader,
                "getSimOperatorName", new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        param.setResult("");
                    }
                });

        XposedHelpers.findAndHookMethod("android.telephony.TelephonyManager", classLoader,
                "getSimOperator", new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        param.setResult(null);
                    }
                });

        XposedHelpers.findAndHookMethod("android.telephony.TelephonyManager", classLoader,
                "getSimCountryIso", new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        param.setResult("");
                    }
                });

        XposedHelpers.findAndHookMethod("android.telephony.TelephonyManager", classLoader,
                "getNetworkCountryIso", new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        param.setResult("");
                    }
                });
        XposedHelpers.findAndHookMethod("android.telephony.TelephonyManager", classLoader,
                "getNetworkType", new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        param.setResult(TelephonyManager.NETWORK_TYPE_UNKNOWN);
                    }
                });

        XposedHelpers.findAndHookMethod("android.telephony.TelephonyManager", classLoader,
                "getPhoneType", new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        param.setResult(TelephonyManager.PHONE_TYPE_NONE);
                    }
                });

        XposedHelpers.findAndHookMethod("android.telephony.TelephonyManager", classLoader,
                "getCurrentPhoneType", new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        param.setResult(0);
                    }
                });

        XposedHelpers.findAndHookMethod("android.telephony.TelephonyManager", classLoader,
                "getDataState", new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        param.setResult(TelephonyManager.DATA_DISCONNECTED);
                    }
                });

        XposedHelpers.findAndHookMethod("android.telephony.TelephonyManager", classLoader,
                "getSimState", new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        param.setResult(TelephonyManager.SIM_STATE_UNKNOWN);
                    }
                });

        XposedHelpers.findAndHookMethod("android.telephony.TelephonyManager", classLoader,
                "getCellLocation", new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        GsmCellLocation gsmCellLocation = new GsmCellLocation();
                        gsmCellLocation.setLacAndCid(0, 0);
                        param.setResult(gsmCellLocation);
                    }
                });

        XposedHelpers.findAndHookMethod("android.telephony.PhoneStateListener", classLoader,
                "onCellLocationChanged", CellLocation.class, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        GsmCellLocation gsmCellLocation = new GsmCellLocation();
                        gsmCellLocation.setLacAndCid(0, 0);
                        param.setResult(gsmCellLocation);
                    }
                });

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
            XposedHelpers.findAndHookMethod("android.telephony.TelephonyManager", classLoader,
                    "getPhoneCount", new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            param.setResult(1);
                        }
                    });
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            XposedHelpers.findAndHookMethod("android.telephony.TelephonyManager", classLoader,
                    "getNeighboringCellInfo", new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            param.setResult(new ArrayList<>());
                        }
                    });
        }

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {
            XposedHelpers.findAndHookMethod("android.telephony.TelephonyManager", classLoader,
                    "getAllCellInfo", new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            param.setResult(getCell(460, 0, 0, 0, 0, 0));
                        }
                    });
            XposedHelpers.findAndHookMethod("android.telephony.PhoneStateListener", classLoader,
                    "onCellInfoChanged", List.class, new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            param.setResult(getCell(460, 0, 0, 0, 0, 0));
                        }
                    });
        }

        XposedHelpers.findAndHookMethod("android.net.wifi.WifiManager", classLoader, "getScanResults", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                param.setResult(new ArrayList<>());
            }
        });

        XposedHelpers.findAndHookMethod("android.net.wifi.WifiManager", classLoader, "getWifiState", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                param.setResult(1);
            }
        });

        XposedHelpers.findAndHookMethod("android.net.wifi.WifiManager", classLoader, "isWifiEnabled", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                param.setResult(true);
            }
        });

        XposedHelpers.findAndHookMethod("android.net.wifi.WifiInfo", classLoader, "getMacAddress", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                param.setResult("00-00-00-00-00-00-00-00");
            }
        });

        XposedHelpers.findAndHookMethod("android.net.wifi.WifiInfo", classLoader, "getSSID", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                param.setResult("null");
            }
        });

        XposedHelpers.findAndHookMethod("android.net.wifi.WifiInfo", classLoader, "getBSSID", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                param.setResult("00-00-00-00-00-00-00-00");
            }
        });


        XposedHelpers.findAndHookMethod("android.net.NetworkInfo", classLoader,
                "getTypeName", new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        param.setResult("WIFI");
                    }
                });
        XposedHelpers.findAndHookMethod("android.net.NetworkInfo", classLoader,
                "isConnectedOrConnecting", new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        param.setResult(true);
                    }
                });

        XposedHelpers.findAndHookMethod("android.net.NetworkInfo", classLoader,
                "isConnected", new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        param.setResult(true);
                    }
                });

        XposedHelpers.findAndHookMethod("android.net.NetworkInfo", classLoader,
                "isAvailable", new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        param.setResult(true);
                    }
                });

        XposedHelpers.findAndHookMethod("android.telephony.CellInfo", classLoader,
                "isRegistered", new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        param.setResult(true);
                    }
                });
    }



}
