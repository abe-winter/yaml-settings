package com.github.abe_winter.yaml_settings;

import android.content.Context;
import android.content.DialogInterface;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.Space;
import android.widget.Switch;
import android.widget.TableLayout;
import android.widget.TextView;
import android.support.design.widget.TabLayout;

import com.esotericsoftware.yamlbeans.YamlReader;

import org.apache.commons.io.IOUtils;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Locale;

interface SettingsCallbackProxy {
    SettingsCallback getListener();
}

public class YamlSettings extends LinearLayout implements SettingsCallbackProxy {
    final String AttrNs = "http://schemas.android.com/apk/res-auto";
    final String TAG = "NM_YamlSettings";
    SettingsNode[] prefs;
    public SettingsCallback listener;
    public Map<String, View> view_lookup;
    int strListDelIcon;
    YamlPager madapter;

    public class TabMisuse extends Exception {}

    public YamlSettings(Context c, AttributeSet attrs) throws IOException, StringList.MissingAttr, TabMisuse {
        super(c, attrs);
        strListDelIcon = attrs.getAttributeResourceValue(AttrNs, "strListDelIcon", 0);
        InputStream stream = getResources().openRawResource(attrs.getAttributeResourceValue("http://schemas.android.com/apk/res-auto", "yamlRawId", 0));
        YamlReader r = new YamlReader(IOUtils.toString(stream, "UTF-8"));
        prefs = SettingsNode.fromlist((ArrayList<Map>) r.read());
        view_lookup = new HashMap<String, View>();
        setOrientation(LinearLayout.VERTICAL);
        if (prefs.length > 0 && prefs[0].type.equals("tab")){
            for (int i=0; i<prefs.length; ++i){
                if (!prefs[i].type.equals("tab")){
                    Log.e(TAG, "tab misuse at "+i+" "+prefs[i].type);
                    throw new TabMisuse();
                }
            }
            madapter = new YamlPager(c);
            super.addView(madapter.mtabber);
            super.addView(madapter.mpager);
        }
        addNodes(c, prefs, null, true);
        if (madapter != null) madapter.notifyDataSetChanged();
    }

    public void setTab(int i){
        madapter.mpager.setCurrentItem(i);
    }

    void addNodes(Context c, SettingsNode[] nodes, String group, boolean top) throws StringList.MissingAttr {
        for (int i=0;i<nodes.length;++i) addNode(c, nodes[i], group);
    }

    static LayoutParams def_layout(){return new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);}

    @Override
    public void addView(View v){
        if (madapter != null) madapter.addView(v);
        else super.addView(v);
    }

    // convert "xy_za_bc" > "Xy za bc"
    static String mktitle(String name){
        String[] toks = name.split("_");
        StringBuilder sb = new StringBuilder();
        for (int i=0;i<toks.length;++i)
            sb.append(
                i == 0
                ? toks[i].substring(0,1).toUpperCase(Locale.getDefault()) + toks[i].substring(1)
                : " "+toks[i]
            );
        return sb.toString();
    }

    void drawGroup(Context c, String name){
        TextView v = new TextView(c);
        v.setLayoutParams(def_layout());
        v.setText(mktitle(name));
        v.setTextSize(18);
        addView(v);
    }

    void addNode(Context c, final SettingsNode node, final String group) throws StringList.MissingAttr {
        final View outer = this;
        switch (node.type){
            case "group":{
                drawGroup(c, node.name);
                // note: node.children get added later
                break;
            }
            case "radio":{
                if (node.name != null){
                    if (node.options != null){
                        RadioGroup rg = new RadioGroup(c);
                        for (int i=0; i<node.options.length; ++i){
                            RadioButton r = new RadioButton(c);
                            r.setLayoutParams(def_layout());
                            r.setText(node.options[i]);
                            final String opt = node.options[i];
                            r.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                @Override
                                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                    if (listener != null) listener.radiochange(outer, buttonView, isChecked, group, node.name, opt);
                                }
                            });
                            rg.addView(r);
                        }
                        addView(rg);
                        view_lookup.put(fullname(group, node.name), rg);
                    }
                }
                break;
            }
            case "number":
            case "email":
            case "password":
            case "text":{
                TextInputEditText text = new TextInputEditText(c);
                TextInputLayout layout = new TextInputLayout(c);
                layout.setLayoutParams(def_layout());
                layout.setHint(mktitle(node.name));
                layout.addView(text);
                addView(layout);
                text.addTextChangedListener(new TextWatcher() {
                    @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                    @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
                    @Override public void afterTextChanged(Editable s) {
                        if (listener != null) listener.textchange(outer, s, group, node.name, s.toString());
                    }
                });
                switch (node.type){
                    case "email": text.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS); break;
                    case "number": text.setInputType(InputType.TYPE_CLASS_NUMBER); break;
                    case "password": text.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD); break;
                }
                if (node.description != null){
                    TextView tv = new TextView(c);
                    tv.setText(node.description);
                    addView(tv);
                }
                view_lookup.put(fullname(group, node.name), text);
                break;
            }
            case "button":{
                Button btn = new Button(c);
                btn.setText(mktitle(node.name));
                addView(btn);
                btn.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (listener!=null) listener.btnclick(outer, v, node);
                    }
                });
                break;
            }
            case "switch":{
                Switch v = new Switch(c);
                v.setLayoutParams(def_layout());
                v.setText(node.description);
                addView(v);
                v.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (listener!=null) listener.switchchange(outer, buttonView, isChecked, group, node.name);
                    }
                });
                view_lookup.put(fullname(group, node.name), v);
                break;
            }
            case "stringlist":{
                drawGroup(c, node.name);
                StringList sl = new StringList(c, node, strListDelIcon);
                sl.m_proxy = this;
                addView(sl);
                view_lookup.put(fullname(group, node.name), sl);
                break;
            }
            case "image":{
                drawGroup(c, node.name);
                ImageSel isel = new ImageSel(c, node);
                isel.m_proxy = this;
                addView(isel);
                view_lookup.put(fullname(group, node.name), isel);
                break;
            }
            case "tab":{
                madapter.addTab(c, node);
                break;
            }
            default:{
                Log.e(TAG, "unk widget type "+node.type);
                break;
            }
        }
        Space sp = new Space(c);
        sp.setLayoutParams(new LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10.f, getResources().getDisplayMetrics())
        ));
        addView(sp);
        if (node.children != null) addNodes(c, node.children, node.name, false);
    }

    String fullname(String group, String name){return group == null ? name : group+"."+name;}

    public SettingsCallback getListener(){
        return listener;
    }
}
