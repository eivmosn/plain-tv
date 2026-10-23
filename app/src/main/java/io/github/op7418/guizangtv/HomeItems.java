package io.github.op7418.guizangtv;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import org.json.JSONArray;
import java.util.*;

/** Stable app/source identities; removing an entry never uninstalls an app. */
final class HomeItems {
    static final String[] DEFAULTS = {"hdmi:1", "tv:live", "com.xiaodianshi.tv.yst",
            "com.tcl.ui_mediaCenter", "com.tcl.cyberui"};
    final Context context;
    final LinkedHashMap<String,String> names = new LinkedHashMap<>();
    final List<String> order = new ArrayList<>();
    HomeItems(Context c) {
        context = c;
        String[] labels = {"PlayStation 5", "直播电视", "哔哩哔哩", "媒体中心", "TCL 原桌面"};
        for (int i=0;i<DEFAULTS.length;i++) names.put(DEFAULTS[i], labels[i]);
        discover();
        try {
            JSONArray data = new JSONArray(c.getSharedPreferences("home-items",0).getString("order","[]"));
            for (int i=0;i<data.length() && order.size()<9;i++) {
                String id = data.getString(i);
                if (validId(id) && !order.contains(id)) order.add(id);
            }
        } catch (Exception ignored) { order.clear(); }
        order.remove("com.tcl.settings");
        if (order.isEmpty()) order.addAll(Arrays.asList(DEFAULTS));
    }
    void discover() {
        for (String category : new String[]{Intent.CATEGORY_LEANBACK_LAUNCHER, Intent.CATEGORY_LAUNCHER}) {
            for (ResolveInfo r : context.getPackageManager().queryIntentActivities(new Intent(Intent.ACTION_MAIN).addCategory(category), 0)) {
                String id = r.activityInfo.packageName;
                if (!id.equals(context.getPackageName()) && r.activityInfo.enabled && r.activityInfo.exported)
                    names.putIfAbsent(id, TclControls.label(id, r.loadLabel(context.getPackageManager()).toString()));
            }
        }
    }
    String name(String id) { return names.getOrDefault(id, context.getSharedPreferences("home-items",0).getString("name:"+id,id)); }
    boolean rename(String id,String value) {
        value=value==null?"":value.trim();
        if(value.isEmpty())return false;
        names.put(id,value);
        return context.getSharedPreferences("home-items",0).edit().putString("name:"+id,value).commit();
    }
    static boolean validId(String id) { return id.equals("hdmi:1") || id.equals("hdmi:2")
            || id.equals("tv:live") || id.matches("[a-zA-Z][a-zA-Z0-9_]*(\\.[a-zA-Z0-9_]+)+"); }
    boolean save(List<String> next) {
        if (next.isEmpty() || next.size()>9 || new HashSet<>(next).size()!=next.size()) return false;
        for(String id : next) if(!validId(id)) return false;
        android.content.SharedPreferences.Editor e = context.getSharedPreferences("home-items",0).edit()
                .putString("order",new JSONArray(next).toString());
        for(String id:next) e.putString("name:"+id,name(id));
        if(!e.commit()) return false;
        order.clear(); order.addAll(next); return true;
    }
}
