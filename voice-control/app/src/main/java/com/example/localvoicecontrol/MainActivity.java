package com.example.localvoicecontrol;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.view.View;
import android.widget.*;
import android.graphics.drawable.Drawable;
import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends Activity {
    private TextView status, lastCommand, history;
    private EditText commandInput;
    private LinearLayout content;
    private static final int REQUEST_MIC = 22;
    private static final int REQUEST_SPEECH = 23;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        ScrollView scroll = new ScrollView(this);
        content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        int pad = (int)(20 * getResources().getDisplayMetrics().density);
        content.setPadding(pad, pad, pad, pad);
        scroll.addView(content);

        TextView title = new TextView(this);
        title.setText("🎙  Local Voice Control"); title.setTextSize(28); title.setTextColor(0xFF183C9A); title.setPadding(0,0,0,pad/2);
        content.addView(title);
        ImageView banner = new ImageView(this);
        banner.setImageResource(com.example.localvoicecontrol.R.drawable.voice_control_banner);
        banner.setContentDescription("Local voice control illustration");
        banner.setScaleType(ImageView.ScaleType.FIT_CENTER);
        content.addView(banner, new LinearLayout.LayoutParams(-1, (int)(120 * getResources().getDisplayMetrics().density)));
        TextView subtitle = new TextView(this);
        subtitle.setText("Voice shortcuts • App launcher • Accessibility tools");
        subtitle.setTextSize(14); subtitle.setPadding(0,0,0,pad/2); content.addView(subtitle);
        TextView intro = new TextView(this);
        intro.setText("On-device assistive controls • No account, backend, or website.\nVoice recognition is provided by Android and may need internet depending on your speech provider.");
        content.addView(intro);
        status = new TextView(this); status.setTextSize(16); status.setPadding(0,pad/2,0,pad/2); content.addView(status);

        addButton("1. Enable Accessibility Service", v -> startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)));
        addButton("2. Grant Microphone Permission", v -> {
            if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED)
                requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_MIC);
            else Toast.makeText(this,"Microphone permission already granted",Toast.LENGTH_SHORT).show();
        });
        addButton("🎙 Speak a command", v -> listen());
        commandInput = new EditText(this); commandInput.setSingleLine(true); commandInput.setHint("Or type: open Settings / tap Search / type hello"); content.addView(commandInput);
        addButton("Run typed command", v -> runCommand(commandInput.getText().toString()));
        lastCommand = new TextView(this); lastCommand.setText("Last command: none"); lastCommand.setTextSize(16); lastCommand.setPadding(0,pad/2,0,pad/2); content.addView(lastCommand);
        addButton("Open installed apps list", v -> showApps());
        addButton("Open Android Settings", v -> runCommand("open Settings"));
        addButton("Clear command field", v -> commandInput.setText(""));
        TextView quick = new TextView(this); quick.setText("Quick controls"); quick.setTextSize(20); quick.setPadding(0,pad,0,0); content.addView(quick);
        addButton("Go Home", v -> runCommand("go home"));
        addButton("Go Back", v -> runCommand("go back"));
        addButton("Show Recent Apps", v -> runCommand("recent apps"));
        addButton("Open Notifications", v -> runCommand("open notifications"));
        addButton("Open Quick Settings", v -> runCommand("quick settings"));
        addButton("Scroll Down", v -> runCommand("scroll down"));
        addButton("Scroll Up", v -> runCommand("scroll up"));
        addButton("Swipe Left", v -> runCommand("swipe left"));
        addButton("Swipe Right", v -> runCommand("swipe right"));
        addButton("Long press Search", v -> runCommand("long press Search"));
        addButton("Tap Allow", v -> runCommand("tap Allow"));
        TextView historyTitle = new TextView(this); historyTitle.setText("Command activity"); historyTitle.setTextSize(20); historyTitle.setPadding(0,pad,0,0); content.addView(historyTitle);
        history = new TextView(this); history.setText("No commands run yet."); history.setPadding(0,pad/2,0,pad/2); content.addView(history);
        addButton("Clear command history", v -> { if(history != null) history.setText("No commands run yet."); if(lastCommand != null) lastCommand.setText("Last command: none"); });
        TextView help = new TextView(this);
        help.setPadding(0,pad,pad,pad);
        help.setText("COMMAND EXAMPLES\n• open WhatsApp / open Settings / open Camera\n• tap Search / click Allow / long press a word\n• type hello world (types into the focused field)\n• scroll down / scroll up / swipe left / swipe right\n• go home / go back / recent apps / notifications / quick settings\n\nCommands act only after you press Speak or Run. Text tapping and typing depend on the target app exposing accessible controls. Unlock the phone and enable this service manually in Settings. Android security screens and some protected apps cannot be controlled."); content.addView(help);
        setContentView(scroll); refreshStatus();
    }

    private void addButton(String label, View.OnClickListener click) {
        Button b = new Button(this); b.setText(label); b.setOnClickListener(click); content.addView(b);
    }
    @Override protected void onResume() { super.onResume(); refreshStatus(); }
    private void refreshStatus() {
        if (status != null) status.setText("Microphone: " + (checkSelfPermission(Manifest.permission.RECORD_AUDIO)==PackageManager.PERMISSION_GRANTED ? "Granted" : "Not granted") +
            "\nAccessibility service: " + (VoiceAccessibilityService.enabled(this) ? "Enabled" : "OFF — enable it above") +
            "\nService connected: " + (VoiceAccessibilityService.isConnected() ? "Yes" : "No"));
    }
    private void listen() {
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO)!=PackageManager.PERMISSION_GRANTED) { requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO},REQUEST_MIC); return; }
        if (!SpeechRecognizer.isRecognitionAvailable(this)) { Toast.makeText(this,"No Android speech recognition provider is available. Type a command instead.",Toast.LENGTH_LONG).show(); return; }
        Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE,Locale.getDefault());
        i.putExtra(RecognizerIntent.EXTRA_PROMPT,"Say a command");
        try { startActivityForResult(i,REQUEST_SPEECH); } catch (Exception e) { Toast.makeText(this,"Speech recognition could not start",Toast.LENGTH_LONG).show(); }
    }
    @Override protected void onActivityResult(int request, int result, Intent data) {
        super.onActivityResult(request,result,data);
        if(request==REQUEST_SPEECH && result==RESULT_OK && data!=null) {
            ArrayList<String> matches=data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if(matches!=null && !matches.isEmpty()) runCommand(matches.get(0));
        }
    }
    private void runCommand(String command) {
        String value=command==null?"":command.trim();
        if(value.isEmpty()) { Toast.makeText(this,"Say or type a command first",Toast.LENGTH_SHORT).show(); return; }
        lastCommand.setText("Last command: " + value);
        if (history != null) {
            String previous = history.getText().toString();
            if ("No commands run yet.".equals(previous)) previous = "";
            String updated = "• " + value + (previous.isEmpty() ? "" : "\n" + previous);
            String[] lines = updated.split("\n");
            StringBuilder limited = new StringBuilder();
            for (int i=0; i<Math.min(lines.length, 8); i++) { if(i>0) limited.append("\n"); limited.append(lines[i]); }
            history.setText(limited.toString());
        }
        VoiceAccessibilityService.command(this,value);
        refreshStatus();
    }
    private void showApps() {
        Intent query=new Intent(Intent.ACTION_MAIN); query.addCategory(Intent.CATEGORY_LAUNCHER);
        final java.util.List<android.content.pm.ResolveInfo> apps=getPackageManager().queryIntentActivities(query,0);
        java.util.Collections.sort(apps,(a,b)->String.valueOf(a.loadLabel(getPackageManager())).compareToIgnoreCase(String.valueOf(b.loadLabel(getPackageManager()))));
        LinearLayout list=new LinearLayout(this); list.setOrientation(LinearLayout.VERTICAL);
        EditText filter=new EditText(this); filter.setHint("Filter app names"); list.addView(filter);
        ScrollView sc=new ScrollView(this); LinearLayout rows=new LinearLayout(this); rows.setOrientation(LinearLayout.VERTICAL); sc.addView(rows); list.addView(sc,new LinearLayout.LayoutParams(-1,0,1));
        Runnable populate=()->{ rows.removeAllViews(); String q=filter.getText().toString().toLowerCase(Locale.ROOT); for(android.content.pm.ResolveInfo info:apps){String label=String.valueOf(info.loadLabel(getPackageManager())); if(!label.toLowerCase(Locale.ROOT).contains(q)) continue; Button b=new Button(this); b.setText(label); b.setOnClickListener(v->{Intent launch=getPackageManager().getLaunchIntentForPackage(info.activityInfo.packageName); if(launch!=null){launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);startActivity(launch);} }); rows.addView(b); }};
        filter.addTextChangedListener(new android.text.TextWatcher(){public void beforeTextChanged(CharSequence s,int st,int count,int after){} public void onTextChanged(CharSequence s,int st,int before,int count){populate.run();} public void afterTextChanged(android.text.Editable e){}});
        populate.run(); new android.app.AlertDialog.Builder(this).setTitle("Installed launchable apps").setView(list).setPositiveButton("Close",null).show();
    }
    @Override public void onRequestPermissionsResult(int request,String[] permissions,int[] results){super.onRequestPermissionsResult(request,permissions,results);refreshStatus();}
}
