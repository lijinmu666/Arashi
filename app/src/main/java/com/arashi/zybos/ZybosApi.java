package com.arashi.zybos;

import android.content.Context;

import java.util.List;








public final class ZybosApi {

    private ZybosApi() {}

    

    
    public static boolean setInstallPackageDisallow(boolean disallow) {
        return ZybosService.invokeSafe("setInstallPackageDisallow",
                new Class[]{boolean.class}, disallow) != null;
    }

    
    public static boolean silentInstallApk(String path, boolean flag, String pkg) {
        return ZybosService.invokeSafe("silentInstallApk",
                new Class[]{String.class, boolean.class, String.class}, path, flag, pkg) != null;
    }

    
    public static boolean silentUninstallApk(String pkg, boolean flag) {
        return ZybosService.invokeSafe("silentUninstallApk",
                new Class[]{String.class, boolean.class}, pkg, flag) != null;
    }

    
    public static boolean cancelUpdate() {
        return ZybosService.invokeSafe("cancelUpdate", new Class[0]) != null;
    }

    

    public static boolean setPackageListDisallow(List<String> list) {
        return ZybosService.invokeSafe("setPackageListDisallow",
                new Class[]{List.class}, list) != null;
    }

    public static boolean setClassListDisallow(List<String> list) {
        return ZybosService.invokeSafe("setClassListDisallow",
                new Class[]{List.class}, list) != null;
    }

    public static boolean setClassListAllow(List<String> list) {
        return ZybosService.invokeSafe("setClassListAllow",
                new Class[]{List.class}, list) != null;
    }

    
    public static boolean setUSBDisallow(boolean disallow) {
        return ZybosService.invokeSafe("setUSBDisallow",
                new Class[]{boolean.class}, disallow) != null;
    }

    

    public static Object getSuperviseConfig() {
        return ZybosService.invokeSafe("getSuperviseConfig", new Class[0]);
    }

    public static Object getOtaConfig() {
        return ZybosService.invokeSafe("getOtaConfig", new Class[0]);
    }

    public static boolean setOtaAutoUpdate(boolean enabled) {
        return ZybosService.invokeSafe("setOtaAutoUpdate",
                new Class[]{boolean.class}, enabled) != null;
    }

    

    public static boolean lockScreen() {
        return ZybosService.invokeSafe("lockScreen", new Class[0]) != null;
    }

    public static boolean disableNami(Context ctx) {
        return ZybosService.invokeSafe("disableNami",
                new Class[]{Context.class}, ctx) != null;
    }

    public static boolean setDefaultBrowser(String pkg) {
        return ZybosService.invokeSafe("setDefaultBrowser",
                new Class[]{String.class}, pkg) != null;
    }

    public static boolean sendLowChargingNotification() {
        return ZybosService.invokeSafe("sendLowChargingNotification", new Class[0]) != null;
    }

    public static boolean cancelLowChargingNotification() {
        return ZybosService.invokeSafe("cancelLowChargingNotification", new Class[0]) != null;
    }

    

    public static boolean setNightDisplayActivated(boolean on) {
        return ZybosService.invokeSafe("setNightDisplayActivated",
                new Class[]{boolean.class}, on) != null;
    }

    public static boolean setNightDisplayColorTemperature(int temperature) {
        return ZybosService.invokeSafe("setNightDisplayColorTemperature",
                new Class[]{int.class}, temperature) != null;
    }

    public static boolean setDisplayDaltonizerEnabled(boolean enabled) {
        return ZybosService.invokeSafe("setDisplayDaltonizerEnabled",
                new Class[]{boolean.class}, enabled) != null;
    }

    public static Object isInDisplayDaltonizerMode() {
        return ZybosService.invokeSafe("isInDisplayDaltonizerMode", new Class[0]);
    }

    public static boolean setStylusEnabled(boolean enabled) {
        return ZybosService.invokeSafe("setStylusEnabled",
                new Class[]{boolean.class}, enabled) != null;
    }

    public static boolean setMtkPQColorTemperatureEnable(boolean enabled) {
        return ZybosService.invokeSafe("setMtkPQColorTemperatureEnable",
                new Class[]{boolean.class}, enabled) != null;
    }

    public static boolean setMtkPQGammaIndex(int index) {
        return ZybosService.invokeSafe("setMtkPQGammaIndex",
                new Class[]{int.class}, index) != null;
    }

    public static Object getMtkPQGammaDefaultValue() {
        return ZybosService.invokeSafe("getMtkPQGammaDefaultValue", new Class[0]);
    }

    public static Object getMtkPQGammaMinValue() {
        return ZybosService.invokeSafe("getMtkPQGammaMinValue", new Class[0]);
    }

    public static Object getMtkPQGammaMaxValue() {
        return ZybosService.invokeSafe("getMtkPQGammaMaxValue", new Class[0]);
    }

    public static Object getMtkPQGammaCurrentValue() {
        return ZybosService.invokeSafe("getMtkPQGammaCurrentValue", new Class[0]);
    }

    

    public static Object getChargerAgingCV() {
        return ZybosService.invokeSafe("getChargerAgingCV", new Class[0]);
    }

    public static boolean setChargerAgingCV(int value) {
        return ZybosService.invokeSafe("setChargerAgingCV",
                new Class[]{int.class}, value) != null;
    }

    public static boolean setChargerIeoc(int value) {
        return ZybosService.invokeSafe("setChargerIeoc",
                new Class[]{int.class}, value) != null;
    }

    

    public static Object getSysfsControl(String path) {
        return ZybosService.invokeSafe("getSysfsControl",
                new Class[]{String.class}, path);
    }

    public static boolean addInDndList(String pkg) {
        return ZybosService.invokeSafe("addInDndList",
                new Class[]{String.class}, pkg) != null;
    }

    public static boolean removeFromDndList(String pkg) {
        return ZybosService.invokeSafe("removeFromDndList",
                new Class[]{String.class}, pkg) != null;
    }

    public static boolean setZybOsDnsDotMode(int mode, String value) {
        return ZybosService.invokeSafe("setZybOsDnsDotMode",
                new Class[]{int.class, String.class}, mode, value) != null;
    }

    

    
    public static boolean updatePowerOnTime(int hour, int minute) {
        return ZybosService.invokeSafe("updatePowerOnTime",
                new Class[]{int.class, int.class}, hour, minute) != null;
    }
}
