package com.example.localvoicecontrol;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.content.Context;
import android.content.Intent;
import android.graphics.Path;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;
import java.util.List;
import java.util.Locale;

public class VoiceAccessibilityService extends AccessibilityService {
    private static volatile VoiceAccessibilityService live;
    @Override protected void onServiceConnected(){super.onServiceConnected();live=this;}
    @Override public void onAccessibilityEvent(android.view.accessibility.AccessibilityEvent event){}
    @Override public void onInterrupt(){}
    @Override public void onDestroy(){if(live==this)live=null;super.onDestroy();}
    static boolean isConnected(){return live!=null;}
    static boolean enabled(Context c){String enabled=Settings.Secure.getString(c.getContentResolver(),Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);return enabled!=null&&enabled.toLowerCase(Locale.ROOT).contains(c.getPackageName().toLowerCase(Locale.ROOT)+"/"+VoiceAccessibilityService.class.getName().toLowerCase(Locale.ROOT));}

    static void command(Context context,String raw){
        String original=raw==null?"":raw.trim(); String x=original.toLowerCase(Locale.ROOT); VoiceAccessibilityService s=live;
        if(x.isEmpty())return;
        if(x.equals("home")||x.contains("go home")||x.equals("press home")){global(context,s,GLOBAL_ACTION_HOME);return;}
        if(x.equals("back")||x.contains("go back")||x.equals("press back")){global(context,s,GLOBAL_ACTION_BACK);return;}
        if(x.contains("recent apps")||x.contains("open recents")){global(context,s,GLOBAL_ACTION_RECENTS);return;}
        if(x.contains("notifications")){global(context,s,GLOBAL_ACTION_NOTIFICATIONS);return;}
        if(x.contains("quick settings")){global(context,s,GLOBAL_ACTION_QUICK_SETTINGS);return;}
        if(x.contains("power dialog")){global(context,s,GLOBAL_ACTION_POWER_DIALOG);return;}
        if(x.startsWith("type ")||x.startsWith("enter text ")){String text=original.substring(original.indexOf(' ')+1).trim();typeText(context,s,text);return;}
        if(x.startsWith("tap ")||x.startsWith("click ")||x.startsWith("press ")){String target=original.substring(original.indexOf(' ')+1).trim();clickText(context,s,target,false);return;}
        if(x.startsWith("long press ")){clickText(context,s,original.substring(11).trim(),true);return;}
        if(x.contains("scroll")||x.startsWith("swipe ")){gestureScroll(context,s,x);return;}
        if(x.startsWith("open ")||x.startsWith("launch ")||x.startsWith("start ")){String name=original.substring(original.indexOf(' ')+1).trim();launchApp(context,name);return;}
        Toast.makeText(context,"Unknown command. Try open [app], tap [text], type [text], scroll down, or go home.",Toast.LENGTH_LONG).show();
    }
    private static void global(Context c,VoiceAccessibilityService s,int action){if(s==null){warn(c);return;}boolean ok=s.performGlobalAction(action);if(!ok)Toast.makeText(c,"Android did not allow that action here",Toast.LENGTH_SHORT).show();}
    private static void warn(Context c){Toast.makeText(c,"Enable Local Voice Control under Android Settings → Accessibility, then reopen this app",Toast.LENGTH_LONG).show();}
    private static void launchApp(Context c,String requested){
        String q=requested.toLowerCase(Locale.ROOT).trim();
        if(q.equals("settings")){try{c.startActivity(new Intent(Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));return;}catch(Exception ignored){}}
        Intent query=new Intent(Intent.ACTION_MAIN);query.addCategory(Intent.CATEGORY_LAUNCHER);
        List<android.content.pm.ResolveInfo> list=c.getPackageManager().queryIntentActivities(query,0);
        android.content.pm.ResolveInfo best=null;
        for(android.content.pm.ResolveInfo ri:list){String label=String.valueOf(ri.loadLabel(c.getPackageManager())).toLowerCase(Locale.ROOT);if(label.equals(q)){best=ri;break;}if(best==null&&(label.contains(q)||q.contains(label)))best=ri;}
        if(best!=null){Intent i=c.getPackageManager().getLaunchIntentForPackage(best.activityInfo.packageName);if(i!=null){i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);try{c.startActivity(i);return;}catch(Exception ignored){}}}
        Toast.makeText(c,"Couldn't find a launchable app named: "+requested,Toast.LENGTH_LONG).show();
    }
    private static void typeText(Context c,VoiceAccessibilityService s,String text){
        if(s==null){warn(c);return;} if(text.isEmpty()){Toast.makeText(c,"Say: type followed by the text",Toast.LENGTH_SHORT).show();return;}
        AccessibilityNodeInfo root=s.getRootInActiveWindow();if(root==null){Toast.makeText(c,"No active app field is available",Toast.LENGTH_SHORT).show();return;}
        AccessibilityNodeInfo focus=root.findFocus(AccessibilityNodeInfo.FOCUS_INPUT);
        if(focus==null){Toast.makeText(c,"Tap a text field first, then run type [text]",Toast.LENGTH_LONG).show();return;}
        Bundle args=new Bundle();args.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,text);
        boolean ok=focus.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT,args);if(!ok)Toast.makeText(c,"This field does not allow accessibility text entry",Toast.LENGTH_LONG).show();
        focus.recycle();root.recycle();
    }
    private static void clickText(Context c,VoiceAccessibilityService s,String target,boolean longPress){
        if(s==null){warn(c);return;}if(Build.VERSION.SDK_INT<24&&longPress){Toast.makeText(c,"Long press gestures require Android 7 or later",Toast.LENGTH_SHORT).show();return;}
        AccessibilityNodeInfo root=s.getRootInActiveWindow();if(root==null){Toast.makeText(c,"No accessible screen is active",Toast.LENGTH_SHORT).show();return;}
        List<AccessibilityNodeInfo> matches=root.findAccessibilityNodeInfosByText(target);
        if(matches!=null){for(AccessibilityNodeInfo n:matches){if(!longPress){AccessibilityNodeInfo candidate=n;while(candidate!=null){if(candidate.isClickable()&&candidate.performAction(AccessibilityNodeInfo.ACTION_CLICK)){root.recycle();return;}AccessibilityNodeInfo parent=candidate.getParent();if(candidate!=n)candidate.recycle();candidate=parent;} }else{Rect r=new Rect();n.getBoundsInScreen(r);if(!r.isEmpty()){Path p=new Path();p.moveTo(r.centerX(),r.centerY());GestureDescription g=new GestureDescription.Builder().addStroke(new GestureDescription.StrokeDescription(p,0,850)).build();boolean sent=s.dispatchGesture(g,null,null);if(sent){root.recycle();return;}}}}}
        root.recycle();Toast.makeText(c,"Couldn't find accessible text: "+target,Toast.LENGTH_LONG).show();
    }
    private static void gestureScroll(Context c,VoiceAccessibilityService s,String x){
        if(s==null){warn(c);return;}if(Build.VERSION.SDK_INT<24){Toast.makeText(c,"Gestures require Android 7 or later",Toast.LENGTH_SHORT).show();return;}
        android.util.DisplayMetrics d=c.getResources().getDisplayMetrics();float cx=d.widthPixels/2f,cy=d.heightPixels/2f;Path p=new Path();
        if(x.contains("left")){p.moveTo(d.widthPixels*.8f,cy);p.lineTo(d.widthPixels*.2f,cy);}else if(x.contains("right")){p.moveTo(d.widthPixels*.2f,cy);p.lineTo(d.widthPixels*.8f,cy);}else if(x.contains("up")){p.moveTo(cx,d.heightPixels*.78f);p.lineTo(cx,d.heightPixels*.28f);}else{p.moveTo(cx,d.heightPixels*.28f);p.lineTo(cx,d.heightPixels*.78f);}
        s.dispatchGesture(new GestureDescription.Builder().addStroke(new GestureDescription.StrokeDescription(p,0,400)).build(),null,null);
    }
}
