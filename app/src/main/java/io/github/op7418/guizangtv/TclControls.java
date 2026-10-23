package io.github.op7418.guizangtv;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.util.Log;

/**
 * TCL 85Q10K / tcl_mt5879_cn (Android 11) entry points.
 *
 * <p>TCL's settings and source list are system overlays shown by ShowWindowService, not
 * full-screen Activities. Starting that service puts them above this launcher exactly like
 * they appear above TCL's own home.</p>
 */
final class TclControls {
    private static final String TAG = "PlainTv.Tcl";
    private static final ComponentName SHOW_WINDOW =
            new ComponentName("com.tcl.settings", "com.tcl.settings.ShowWindowService");
    private static final String ACTION_SETTINGS = "com.tcl.settings.SHOW_WINDOW";
    private static final String ACTION_INPUTS = "com.tcl.insrc.SHOW_WINDOW";

    private TclControls() { }

    static boolean enabled() {
        return "tcl_mt5879_cn".equals(android.os.Build.DEVICE)
                || "tcl_mt5879_cn".equals(android.os.Build.PRODUCT);
    }

    /** TCL boot takeover is not wired up yet; keep the BootStart UI honest. */
    static boolean bootSupported() {
        return false;
    }

    /** Opens TCL's left settings panel (image, sound, network, Bluetooth, inputs, apps, ...). */
    static boolean showSettings(Context context) {
        return showWindow(context, ACTION_SETTINGS);
    }

    /** Opens TCL's source list: HDMI1-4, TV, AV, cast and media center. */
    static boolean showInputs(Context context) {
        return showWindow(context, ACTION_INPUTS);
    }

    /**
     * Direct HDMI selection is a system overlay, so open the source list instead of pretending
     * to tune an input. The user picks the port with the same D-pad flow as TCL's own home.
     */
    static boolean openHdmi(Context context, int port) {
        if (port < 1 || port > 4) return false;
        try {
            Intent intent = new Intent("android.intent.action.TV")
                    .setComponent(new ComponentName("com.tcl.tv", "com.tcl.tv.TVActivity"))
                    .putExtra("targetSourceId", 7 + port)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
            return true;
        } catch (RuntimeException unavailable) {
            Log.w(TAG, "HDMI" + port + " unavailable", unavailable);
            return false;
        }
    }

    private static boolean showWindow(Context context, String action) {
        try {
            // ShowWindowService is exported and uses the same system overlay as TCL's home.
            context.startService(new Intent(action).setComponent(SHOW_WINDOW));
            return true;
        } catch (RuntimeException unavailable) {
            Log.w(TAG, "TCL panel unavailable: " + action, unavailable);
            return false;
        }
    }

    static boolean openLiveTv(Context context) {
        ComponentName target = new ComponentName("com.tcl.tv", "com.tcl.tv.TVActivity");
        try {
            ActivityInfo info = context.getPackageManager().getActivityInfo(target, 0);
            if (!info.enabled || !info.exported) return false;
            context.startActivity(new Intent("tcl.sys.intent.action.TV")
                    .setComponent(target)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            return true;
        } catch (PackageManager.NameNotFoundException | RuntimeException unavailable) {
            Log.w(TAG, "Live TV unavailable", unavailable);
            return false;
        }
    }

    static String label(String pkg, String original) {
        switch (pkg) {
            case "com.tcl.cyberui": return "TCL 原桌面";
            case "com.tcl.settings": return "设置";
            case "com.tcl.tv": return "直播电视";
            case "com.tcl.ui_mediaCenter": return "媒体中心";
            case "com.tcl.MultiScreenInteraction_TV": return "多屏互动";
            case "com.xiaodianshi.tv.yst": return "哔哩哔哩";
            default: return original;
        }
    }

    static boolean startStockHome(Context context, ComponentName ignored) {
        ComponentName target = new ComponentName("com.tcl.cyberui", "com.tcl.cyberui.MainActivity");
        try {
            ActivityInfo info = context.getPackageManager().getActivityInfo(target, 0);
            if (!info.enabled || !info.exported) return false;
            context.startActivity(new Intent(Intent.ACTION_MAIN).setComponent(target)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED));
            return true;
        } catch (PackageManager.NameNotFoundException | RuntimeException unavailable) {
            Log.w(TAG, "TCL desktop unavailable", unavailable);
            return false;
        }
    }
}
