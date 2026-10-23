package io.github.op7418.guizangtv;

import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.graphics.drawable.GradientDrawable;
import android.view.*;
import android.widget.*;
import java.util.*;

/** Remote-first grouped settings. Focus browses; OK commits; Back returns one level. */
final class HomeSettings {
    private final FrameHomeActivity a;
    private Dialog dialog;
    private String page="overview";
    private final Map<String,Integer> remembered=new HashMap<>();
    private final List<View> choices=new ArrayList<>();
    private final Deque<List<String>> undo=new ArrayDeque<>();
    private LinearLayout options;
    private boolean rendering;
    private final Map<String,ImageView[]> homeActions=new LinkedHashMap<>();
    private String homeFocusId;
    private int homeFocusAction=-1;
    private FrameLayout stage;
    private ImageView backdrop;
    private View content;
    private int backdropRequest;
    private int cityRequest;
    private boolean searching;
    private EditText cityInput;
    private String cityQuery="",cityError="";
    private List<HomeWeather.City> cityResults=new ArrayList<>();
    HomeSettings(FrameHomeActivity activity){a=activity;}
    boolean showing(){return dialog!=null&&dialog.isShowing();}
    String page(){return showing()?page:"closed";}
    void time(String time){}
    void weatherChanged(){}
    void show(){
        if(showing())return;
        page="overview";undo.clear();
        dialog=new Dialog(a){@Override public void onBackPressed(){back();}};
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setOnDismissListener(d->{a.wallpapers.cancelPreview();a.wallpapers.cancelSelection();undo.clear();a.updateRotation();a.surface.controls.get(2).requestFocus();});
        dialog.show();dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.getWindow().setLayout(-1,-1);
        dialog.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN|View.SYSTEM_UI_FLAG_HIDE_NAVIGATION|View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        stage=new FrameLayout(a);stage.setBackgroundColor(0xFF121619);
        backdrop=new ImageView(a);backdrop.setScaleType(ImageView.ScaleType.CENTER_CROP);stage.addView(backdrop,new FrameLayout.LayoutParams(-1,-1));
        View shade=new View(a);shade.setBackground(new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,new int[]{0xC2080C0F,0x7D080C0F}));stage.addView(shade,new FrameLayout.LayoutParams(-1,-1));
        dialog.setContentView(stage);render();a.updateRotation();
    }
    void close(){cityRequest++;searching=false;if(dialog!=null)dialog.dismiss();}
    private void back(){
        a.wallpapers.cancelPreview();a.wallpapers.cancelSelection();
        if(page.equals("overview")){close();return;}
        go(page.equals("city")?"weather":page.equals("wallpaper")||page.equals("rotation")?"background":page.startsWith("item:")||page.equals("add")?"home":"overview");
    }
    private void go(String next){cityRequest++;searching=false;a.wallpapers.cancelPreview();a.wallpapers.cancelSelection();page=next;render();}
    private String title(){
        switch(page){case "overview":return "设置";case "background":return "背景";case "wallpaper":return "壁纸";case "rotation":return "轮播";
            case "weather":return "时间与天气";case "city":return "选择城市";case "layout":return "布局";case "type":return "字体";case "focus":return "选中样式";case "home":return "首页内容";case "add":return "首页内容 / 添加";case "tv":return "电视与启动";
            default:return "首页内容 / "+a.items.name(page.substring(5));}
    }
    private void render(){
        rendering=true;choices.clear();homeActions.clear();
        boolean overview=page.equals("overview");
        LinearLayout panel=new LinearLayout(a);panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(a.dp(70),a.dp(overview?42:24),a.dp(70),a.dp(30));
        if(!overview){

        }
        TextView title=label(title(),28);title.setPadding(a.dp(14),0,0,0);title.setGravity(Gravity.CENTER_VERTICAL);
        panel.addView(title,new LinearLayout.LayoutParams(-1,a.dp(overview?64:68)));
        LinearLayout body=new LinearLayout(a);body.setOrientation(LinearLayout.HORIZONTAL);
        panel.addView(body,new LinearLayout.LayoutParams(-1,0,1));
        ScrollView scroll=new ScrollView(a);scroll.setVerticalScrollBarEnabled(false);scroll.setOverScrollMode(View.OVER_SCROLL_NEVER);
        options=new LinearLayout(a);options.setOrientation(LinearLayout.VERTICAL);options.setPadding(0,0,0,a.dp(12));
        scroll.addView(options,new ScrollView.LayoutParams(-1,-2));
        body.addView(scroll,new LinearLayout.LayoutParams(a.dp(720),-1));
        switch(page){
            case "overview":
                entry("背景", "壁纸、轮播与纯色",()->go("background"));entry("布局","列表、Dock、图标与文字",()->go("layout"));
                entry("字体","字形与字号",()->go("type"));entry("选中样式","文字高亮与毛玻璃",()->go("focus"));
                entry("时间与天气","城市、温度与天气显示",()->go("weather"));entry("首页内容","添加、排序、隐藏",()->go("home"));entry("电视与启动","自动进入与 TCL 系统",()->go("tv"));break;
            case "background":
                choice("照片","background",0);choice("浅色","background",1);choice("深色","background",2);
                if(a.appearance.get("background")==0){entry("壁纸",a.wallpapers.name(a.wallpapers.selected()),()->go("wallpaper"));entry("轮播",a.wallpapers.rotating()?"每 "+a.wallpapers.minutes()+" 分钟":"固定壁纸",()->go("rotation"));}
                else note("切换回照片背景后，可选择壁纸和轮播。原来的照片与轮播偏好会保留。");break;
            case "layout":
                choice("纵向列表","layout",0);choice("横向 Dock","layout",1);
                choice("应用：文字","apps",0);choice("应用：图标","apps",1);choice("应用：图标＋文字","apps",2);
                choice("右侧：文字","controls",0);choice("右侧：图标","controls",1);choice("右侧：图标＋文字","controls",2);
                note("仅显示图标时，聚焦后会显示名称。");break;
            case "type":
                choice("字体：轻盈","font",0);choice("字体：标准","font",1);choice("字体：衬线","font",2);
                choice("字号：小","size",0);choice("字号：标准","size",1);choice("字号：大","size",2);break;
            case "focus":
                choice("文字高亮","focus",0);choice("毛玻璃","focus",1);
                choice("底板：柔和","material",0);choice("底板：浓雾","material",1);choice("底板：清透","material",2);
                choice("淡化：轻","dim",0);choice("淡化：标准","dim",1);choice("淡化：明显","dim",2);break;
            case "rotation":
                option("固定壁纸",!a.wallpapers.rotating(),()->{if(a.wallpapers.rotation(false,a.wallpapers.minutes()))render();else failed();});
                for(int m:WallpaperCycle.MINUTES){final int value=m;option("每 "+m+" 分钟",a.wallpapers.rotating()&&a.wallpapers.minutes()==m,()->{
                    if(a.wallpapers.rotation(true,value))render();else failed();});}
                note("只在照片首页显示时计时；设置页、其他应用和纯色背景会暂停轮播。选择壁纸不会改变轮播模式。");break;
            case "wallpaper":gallery();break;
            case "home": {
                a.items.discover();
                int n=a.items.order.size();
                View add=addAppButton(()->go("add"));
                List<ImageView[]> rows=new ArrayList<>();
                for(int i=0;i<n;i++){String id=a.items.order.get(i);homeRow(i,id,rows);}
                View first=null;
                for(int i=0;i<n;i++){
                    ImageView[] acts=rows.get(i);
                    if(first==null){for(ImageView a:acts)if(a.isEnabled()){first=a;break;}}
                    for(int j=0;j<acts.length;j++){
                        if(!acts[j].isEnabled())continue;
                        if(j<acts.length-1)acts[j].setNextFocusRightId(acts[j+1].getId());
                        if(j>0)acts[j].setNextFocusLeftId(acts[j-1].getId());
                        if(i==0)acts[j].setNextFocusUpId(add.getId());else acts[j].setNextFocusUpId(rows.get(i-1)[j].getId());
                        if(i<n-1)acts[j].setNextFocusDownId(rows.get(i+1)[j].getId());
                    }
                }
                if(first!=null)add.setNextFocusDownId(first.getId());
                break;
            }
            case "add":
                a.items.discover();for(String id:a.items.names.keySet())if(!a.items.order.contains(id))compactButton(a.items.name(id),()->{
                    if(a.items.order.size()>=9){a.message("首页最多保留 9 个入口");return;}List<String> next=new ArrayList<>(a.items.order);next.add(id);if(save(next))go("home");});break;
            case "weather":
                option("关闭天气",!a.weather.enabled(),()->{if(a.weather.enabled(false)){a.refreshHome();render();}else failed();});
                option("显示天气",a.weather.enabled(),()->{if(a.weather.enabled(true)){a.refreshHome();render();}else failed();});
                option("自动识别城市",a.weather.automatic(),()->{if(a.weather.autoCity()){a.refreshHome();render();}else failed();});
                entry("手动选择城市",a.weather.city(),()->go("city"));
                note("天气显示在时间右侧，每 30 分钟更新。自动城市根据网络出口识别；使用代理时可能不准确，可手动纠正。");
                note("数据：Open-Meteo · CC BY 4.0\n城市：IPWhois · 不申请精确定位");break;
            case "city":
                cityInput=new EditText(a);cityInput.setText(cityQuery);cityInput.setTextColor(0xF2FFFFFF);cityInput.setHintTextColor(0x99FFFFFF);cityInput.setTextSize(18);
                cityInput.setHint("城市名称或拼音");cityInput.setSingleLine(true);cityInput.setPadding(a.dp(14),0,a.dp(14),0);cityInput.setBackground(WallpaperMaterial.rounded(0x26FFFFFF,a.dp(10)));
                cityInput.setOnFocusChangeListener((v,focus)->v.setBackground(WallpaperMaterial.rounded(focus?0x45FFFFFF:0x26FFFFFF,a.dp(10))));
                options.addView(cityInput,new LinearLayout.LayoutParams(-1,a.dp(44)));
                entry("搜索城市",searching?"正在搜索…":"确认后查看匹配城市",this::searchCities);
                for(HomeWeather.City city:cityResults)entry(city.name,city.region,()->{if(a.weather.select(city)){a.refreshHome();go("weather");}else failed();});
                if(!cityError.isEmpty())a.message(cityError);break;
            case "tv":
                entry("开机自动进入",BootStart.enabled(a)?"已开启":"已关闭",()->BootStart.show(a));entry("电视系统设置","网络、声音、画面等",a::systemSettings);entry("TCL 原桌面","打开原来的电视首页",a::stockHome);break;
            default:
                String id=page.substring(5);int pos=a.items.order.indexOf(id);
                if(pos>0)entry("上移","与上一项交换位置",()->move(id,-1));if(pos>=0&&pos<a.items.order.size()-1)entry("下移","与下一项交换位置",()->move(id,1));
                if(a.items.order.size()>1)entry("从首页隐藏","应用仍保留在电视上",()->{List<String> next=new ArrayList<>(a.items.order);next.remove(id);if(save(next))go("home");});
                else note("至少保留一个首页入口。");break;
        }
        if(content!=null)stage.removeView(content);content=panel;stage.addView(panel,new FrameLayout.LayoutParams(-1,-1));
        background(a.wallpaperDrawable());rendering=false;weatherChanged();
        if(!choices.isEmpty())choices.get(Math.min(remembered.getOrDefault(page,0),choices.size()-1)).requestFocus();
        if(homeFocusId!=null){
            ImageView[] acts=homeActions.get(homeFocusId);
            if(acts!=null){
                int want=homeFocusAction>=0?homeFocusAction:0;
                for(int k=0;k<acts.length;k++){ImageView b=acts[(want+k)%acts.length];if(b.isEnabled()){b.requestFocus();break;}}
            }
            homeFocusId=null;homeFocusAction=-1;
        }
    }
    private void searchCities(){
        if(searching)return;cityQuery=cityInput.getText().toString().trim();
        ((android.view.inputmethod.InputMethodManager)a.getSystemService(android.content.Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(cityInput.getWindowToken(),0);
        searching=true;cityError="";cityResults.clear();int request=++cityRequest;render();
        a.weather.search(cityQuery,(cities,error)->{if(request!=cityRequest||!showing()||!page.equals("city"))return;searching=false;cityResults=cities;cityError=error;render();});
    }
    private void gallery(){
        for(int i=0;i<a.wallpapers.count();i++){
            final int index=i;
            TextView v=button(a.wallpapers.name(i),()->{
                if(index==a.wallpapers.selected())return;
                String origin=page;
                a.wallpapers.select(index,()->{if(showing()&&page.equals(origin))render();});
            });
            android.graphics.drawable.Drawable thumb=a.wallpapers.thumbnail(i);
            if(thumb!=null){thumb=roundedThumb(thumb);thumb.setBounds(0,0,a.dp(48),a.dp(27));v.setCompoundDrawables(thumb,null,checkmark(i==a.wallpapers.selected()),null);v.setCompoundDrawablePadding(a.dp(14));}
            v.setOnFocusChangeListener((view,focus)->{decorate(view,focus);if(focus)remembered.put(page,choices.indexOf(view));});
            options.addView(v,new LinearLayout.LayoutParams(-1,a.dp(46)));
        }
        note("上下浏览预览，按确定设为壁纸。");
    }
    private void choice(String label,String key,int value){
        TextView v=button(label,()->{if(a.appearance.get(key)==value)return;if(a.appearance.set(key,value)){a.refreshHome();render();}else failed();});
        v.setCompoundDrawables(null,null,checkmark(a.appearance.get(key)==value),null);
        options.addView(v,new LinearLayout.LayoutParams(-1,a.dp(44)));
    }
    private void row(String label,String key,String[] labels){
        if(!label.equals(title()))heading(label);
        for(int i=0;i<labels.length;i++){
            final int value=i;
            TextView v=button(labels[i],()->{
                if(a.appearance.get(key)==value)return;
                if(a.appearance.set(key,value)){a.refreshHome();render();}else failed();
            });
            v.setCompoundDrawables(null,null,checkmark(a.appearance.get(key)==i),null);
            options.addView(v,new LinearLayout.LayoutParams(-1,a.dp(42)));
        }
    }
    private void option(String name,boolean selected,Runnable run){
        TextView v=button(name,run);v.setCompoundDrawables(null,null,checkmark(selected),null);
        options.addView(v,new LinearLayout.LayoutParams(-1,a.dp(44)));
    }
    private Drawable checkmark(boolean selected){
        if(!selected)return null;
        Drawable mark=a.getDrawable(R.drawable.mark_check).mutate();mark.setTint(0xDFFFFFFF);mark.setBounds(0,0,a.dp(16),a.dp(16));return mark;
    }
    private void heading(String s){TextView v=label(s,14);v.setTextColor(0xADFFFFFF);v.setPadding(a.dp(14),a.dp(12),0,a.dp(7));options.addView(v);}
    private void note(String s){}
    private TextView compactButton(String text,Runnable run){
        TextView v=label(text,18);v.setGravity(Gravity.CENTER_VERTICAL);v.setPadding(a.dp(16),0,a.dp(16),0);
        v.setSingleLine(true);v.setFocusable(true);v.setFocusableInTouchMode(true);v.setClickable(true);v.setId(View.generateViewId());choices.add(v);
        v.setOnClickListener(view->run.run());v.setOnFocusChangeListener((view,focus)->decorate(view,focus));
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,a.dp(42));lp.bottomMargin=a.dp(6);options.addView(v,lp);
        return v;
    }
    private View addAppButton(Runnable run){
        TextView v=label("添加应用",18);v.setGravity(Gravity.CENTER_VERTICAL);v.setPadding(a.dp(20),0,a.dp(20),0);
        Drawable plus=a.getDrawable(R.drawable.mark_plus).mutate();plus.setTint(0xEBFFFFFF);plus.setBounds(0,0,a.dp(16),a.dp(16));v.setCompoundDrawables(plus,null,null,null);v.setCompoundDrawablePadding(a.dp(8));
        v.setId(View.generateViewId());v.setFocusable(true);v.setFocusableInTouchMode(true);v.setClickable(true);choices.add(v);
        v.setOnClickListener(view->run.run());v.setOnFocusChangeListener((view,focus)->decorate(view,focus));
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,a.dp(42));lp.bottomMargin=a.dp(16);options.addView(v,lp);
        return v;
    }
    private void homeRow(int index,String id,List<ImageView[]> rows){
        LinearLayout row=new LinearLayout(a);row.setOrientation(LinearLayout.HORIZONTAL);row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(a.dp(14),0,a.dp(10),0);
        TextView name=label((index+1)+"  "+a.items.name(id),19);name.setSingleLine(true);name.setEllipsize(android.text.TextUtils.TruncateAt.END);
        row.addView(name,new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.MATCH_PARENT,1));
        ImageView up=action(R.drawable.mark_arrow_up,0xEBFFFFFF,()->move(id,-1),index>0);
        ImageView down=action(R.drawable.mark_arrow_down,0xEBFFFFFF,()->move(id,1),index<a.items.order.size()-1);
        ImageView edit=action(R.drawable.mark_pencil_simple,0xEBFFFFFF,()->editName(id),true);
        ImageView del=action(R.drawable.mark_trash,0xFFFF6B6B,()->removeItem(id),a.items.order.size()>1);
        int[] margins={8,10,10,10};ImageView[] acts={up,down,edit,del};
        for(int j=0;j<acts.length;j++){LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(a.dp(40),a.dp(40));lp.leftMargin=a.dp(margins[j]);row.addView(acts[j],lp);}
        homeActions.put(id,acts);rows.add(acts);
        options.addView(row,new LinearLayout.LayoutParams(-1,a.dp(52)));
    }
    private void editName(String id){
        Dialog dialog=new Dialog(a){@Override public void onBackPressed(){dismiss();}};
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        WindowManager.LayoutParams win=dialog.getWindow().getAttributes();win.dimAmount=0.65f;dialog.getWindow().setAttributes(win);
        FrameLayout root=new FrameLayout(a);
        ImageView bg=new ImageView(a);bg.setScaleType(ImageView.ScaleType.CENTER_CROP);bg.setAlpha(0.55f);
        root.addView(bg,new FrameLayout.LayoutParams(-1,-1));
        WallpaperMaterial.request(a.wallpaperDrawable(),42,image->{if(image!=null)bg.setImageBitmap(image);});
        View veil=new View(a);veil.setBackgroundColor(0x99000000);root.addView(veil,new FrameLayout.LayoutParams(-1,-1));
        LinearLayout panel=new LinearLayout(a);panel.setOrientation(LinearLayout.VERTICAL);panel.setPadding(a.dp(34),a.dp(28),a.dp(34),a.dp(24));
        GradientDrawable glass=new GradientDrawable();glass.setColor(0xE61A1E23);glass.setCornerRadius(a.dp(24));panel.setBackground(glass);
        FrameLayout.LayoutParams panelLp=new FrameLayout.LayoutParams(a.dp(680),FrameLayout.LayoutParams.WRAP_CONTENT,Gravity.CENTER);root.addView(panel,panelLp);
        TextView title=label("编辑名称",28);title.setTextColor(0xF2FFFFFF);panel.addView(title,new LinearLayout.LayoutParams(-1,a.dp(52)));
        EditText input=new EditText(a);input.setText(a.items.name(id));input.setSingleLine(true);input.setSelectAllOnFocus(true);input.setTextSize(20);
        input.setTextColor(0xF2FFFFFF);input.setHintTextColor(0x80FFFFFF);input.setPadding(a.dp(18),0,a.dp(18),0);
        GradientDrawable field=new GradientDrawable();field.setColor(0x33F5F4EF);field.setCornerRadius(a.dp(14));input.setBackground(field);
        input.setOnFocusChangeListener((v,focus)->{GradientDrawable d=new GradientDrawable();d.setColor(focus?0x55F5F4EF:0x33F5F4EF);d.setCornerRadius(a.dp(14));v.setBackground(d);});
        panel.addView(input,new LinearLayout.LayoutParams(-1,a.dp(58)));
        LinearLayout buttons=new LinearLayout(a);buttons.setOrientation(LinearLayout.HORIZONTAL);buttons.setGravity(Gravity.END);
        TextView cancel=label("取消",17),save=label("保存",17);
        for(TextView b:new TextView[]{cancel,save}){b.setGravity(Gravity.CENTER);b.setPadding(a.dp(24),0,a.dp(24),0);b.setFocusable(true);b.setFocusableInTouchMode(true);b.setClickable(true);
            GradientDrawable base=new GradientDrawable();base.setColor(b==save?0x55F5F4EF:0x26F5F4EF);base.setCornerRadius(a.dp(14));b.setBackground(base);
            b.setOnFocusChangeListener((v,focus)->{GradientDrawable d=new GradientDrawable();d.setColor(focus?0x66F5F4EF:0x26F5F4EF);d.setCornerRadius(a.dp(14));v.setBackground(d);});}
        LinearLayout.LayoutParams bl=new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,a.dp(46));bl.leftMargin=a.dp(12);
        buttons.addView(cancel,bl);buttons.addView(save,new LinearLayout.LayoutParams(bl));
        LinearLayout.LayoutParams buttonsLp=new LinearLayout.LayoutParams(-1,LinearLayout.LayoutParams.WRAP_CONTENT);buttonsLp.topMargin=a.dp(22);panel.addView(buttons,buttonsLp);
        cancel.setOnClickListener(v->dialog.dismiss());
        save.setOnClickListener(v->{String value=input.getText().toString().trim();if(value.isEmpty()){a.message("名称不能为空");return;}
            if(a.items.rename(id,value)){homeFocusId=id;homeFocusAction=2;a.refreshHome();dialog.dismiss();render();}else a.message("保存失败");});
        dialog.setOnShowListener(d->{input.requestFocus();input.setSelection(input.getText().length());dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);});
        dialog.setContentView(root);
        dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,WindowManager.LayoutParams.MATCH_PARENT);
        dialog.show();
    }
    private ImageView action(int res,int tint,Runnable run,boolean enabled){
        ImageView v=new ImageView(a);v.setImageResource(res);v.setColorFilter(tint);v.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        v.setPadding(a.dp(9),a.dp(9),a.dp(9),a.dp(9));v.setId(View.generateViewId());v.setTag(tint);
        GradientDrawable base=new GradientDrawable();base.setColor(0x26F5F4EF);base.setCornerRadius(a.dp(12));v.setBackground(base);
        v.setFocusable(enabled);v.setFocusableInTouchMode(enabled);v.setClickable(enabled);v.setEnabled(enabled);v.setAlpha(enabled?1f:0.28f);
        v.setOnClickListener(view->run.run());v.setOnFocusChangeListener((view,focus)->decorate(view,focus));
        if(enabled)choices.add(v);return v;
    }
    private void removeItem(String id){
        if(a.items.order.size()<=1){a.message("至少保留一个入口");return;}
        int index=a.items.order.indexOf(id);
        String target=index<a.items.order.size()-1?a.items.order.get(index+1):a.items.order.get(index-1);
        List<String> next=new ArrayList<>(a.items.order);next.remove(id);
        if(save(next)){homeFocusId=target;homeFocusAction=3;render();}
    }
    private void entry(String name,String subtitle,Runnable run){
        LinearLayout v=new LinearLayout(a);v.setOrientation(LinearLayout.HORIZONTAL);v.setGravity(Gravity.CENTER_VERTICAL);
        v.setPadding(a.dp(14),0,a.dp(14),0);v.setId(View.generateViewId());
        v.setFocusable(true);v.setFocusableInTouchMode(true);v.setClickable(true);choices.add(v);
        LinearLayout texts=new LinearLayout(a);texts.setOrientation(LinearLayout.VERTICAL);texts.setGravity(Gravity.CENTER_VERTICAL);
        TextView title=label(name,19);title.setIncludeFontPadding(false);texts.addView(title);
        v.addView(texts,new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.WRAP_CONTENT,1));
        ImageView arrow=new ImageView(a);arrow.setImageResource(R.drawable.mark_caret_right);arrow.setColorFilter(0xAAFFFFFF);
        LinearLayout.LayoutParams ap=new LinearLayout.LayoutParams(a.dp(14),a.dp(14));ap.gravity=Gravity.CENTER_VERTICAL;v.addView(arrow,ap);
        v.setOnFocusChangeListener((view,focus)->{decorate(view,focus);if(focus&&!rendering)remembered.put(page,choices.indexOf(view));});
        v.setOnClickListener(view->run.run());
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,a.dp(44));lp.bottomMargin=a.dp(4);options.addView(v,lp);decorate(v,false);
    }
    private TextView button(String text,Runnable run){
        TextView v=label(text,19);v.setPadding(a.dp(14),0,a.dp(16),0);v.setGravity(Gravity.CENTER_VERTICAL);v.setId(View.generateViewId());v.setFocusable(true);v.setFocusableInTouchMode(true);v.setClickable(true);choices.add(v);
        v.setOnFocusChangeListener((view,focus)->{decorate(view,focus);if(focus&&!rendering)remembered.put(page,choices.indexOf(view));});
        v.setOnClickListener(view->run.run());v.setOnKeyListener((view,key,e)->(key==KeyEvent.KEYCODE_DPAD_CENTER||key==KeyEvent.KEYCODE_ENTER)&&e.getAction()==KeyEvent.ACTION_DOWN&&e.getRepeatCount()>0);return v;
    }
    private void decorate(View view,boolean focus){
        GradientDrawable bg=new GradientDrawable();
        bg.setColor(focus?0x30F5F4EF:0x00000000);
        bg.setCornerRadius(a.dp(10));
        view.setBackground(bg);
        if(view instanceof ImageView){ImageView iv=(ImageView)view;int tint=iv.getTag() instanceof Integer?(Integer)iv.getTag():0xAAFFFFFF;GradientDrawable glass=new GradientDrawable();glass.setColor(focus?0x55F5F4EF:0x26F5F4EF);glass.setCornerRadius(a.dp(12));iv.setBackground(glass);iv.setColorFilter(focus?(tint==0xFFFF6B6B?0xFFFF9A9A:0xFFFFFFF7):tint);return;}
        if(view instanceof TextView){TextView v=(TextView)view;v.setTextColor(0xEBFFFFFF);Drawable arrow=v.getCompoundDrawables()[2];if(arrow!=null)arrow.setTint(focus?0xE6FFFFFF:0xAAFFFFFF);return;}
        ViewGroup group=(ViewGroup)view;
        for(int i=0;i<group.getChildCount();i++){
            View child=group.getChildAt(i);
            if(child instanceof TextView){TextView tv=(TextView)child;tv.setTextColor("sub".equals(tv.getTag())?0xA6FFFFFF:0xEBFFFFFF);}
            else if(child instanceof ImageView)((ImageView)child).setColorFilter(focus?0xE6FFFFFF:0xAAFFFFFF);
            else if(child instanceof ViewGroup){
                ViewGroup inner=(ViewGroup)child;
                for(int j=0;j<inner.getChildCount();j++){View item=inner.getChildAt(j);
                    if(item instanceof TextView){TextView tv=(TextView)item;tv.setTextColor("sub".equals(tv.getTag())?0xA6FFFFFF:0xEBFFFFFF);}
                    else if(item instanceof ImageView)((ImageView)item).setColorFilter(focus?0xE6FFFFFF:0xAAFFFFFF);}
            }
        }
    }
    private TextView label(String s,int size){TextView v=new TextView(a);v.setText(s);v.setTextSize(size);v.setTextColor(0xF2FFFFFF);v.setTypeface(android.os.Build.VERSION.SDK_INT>=28?Typeface.create(Typeface.create("sans-serif",Typeface.NORMAL),300,false):Typeface.create("sans-serif-light",Typeface.NORMAL));v.setIncludeFontPadding(false);return v;}
    private void background(Drawable source){
        int request=++backdropRequest;
        WallpaperMaterial.request(source,42,image->{if(request==backdropRequest&&showing()&&image!=null)backdrop.setImageBitmap(image);});
    }
    private Drawable roundedThumb(Drawable source){
        android.graphics.Bitmap image=android.graphics.Bitmap.createBitmap(a.dp(48),a.dp(27),android.graphics.Bitmap.Config.ARGB_8888);
        android.graphics.Canvas c=new android.graphics.Canvas(image);android.graphics.Path path=new android.graphics.Path();
        path.addRoundRect(0,0,image.getWidth(),image.getHeight(),a.dp(4),a.dp(4),android.graphics.Path.Direction.CW);c.clipPath(path);
        source.setBounds(0,0,image.getWidth(),image.getHeight());source.draw(c);return new android.graphics.drawable.BitmapDrawable(a.getResources(),image);
    }
    private boolean save(List<String> next){List<String> old=new ArrayList<>(a.items.order);if(!a.items.save(next)){failed();return false;}undo.push(old);a.refreshHome();return true;}
    private void move(String id,int direction){
        List<String> next=new ArrayList<>(a.items.order);int index=next.indexOf(id);Collections.swap(next,index,index+direction);
        if(save(next)){homeFocusId=id;homeFocusAction=direction<0?0:1;render();}
    }
    private void undo(){if(undo.isEmpty())return;if(a.items.save(undo.peek())){undo.pop();a.refreshHome();render();}else failed();}
    private void failed(){a.message("保存失败，保留原设置，请重试");}
}

