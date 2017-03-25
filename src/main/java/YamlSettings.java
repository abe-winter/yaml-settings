package com.github.abe_winter.yaml_settings;

import android.content.Context;
import android.content.DialogInterface;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Space;
import android.widget.Switch;
import android.widget.TextView;

import com.esotericsoftware.yamlbeans.YamlReader;

import org.apache.commons.io.IOUtils;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Locale;

class SettingsNode {
    String type; // one of ()
    String name; // sometimes shown, used for sync
    String description; // shown below field for text, next to for switch
    SettingsNode[] children;
    String[] options;

    static SettingsNode[] fromlist(ArrayList<Map> items){
        ArrayList<SettingsNode> ret = new ArrayList<SettingsNode>(items.size());
        for (int i=0;i<items.size();++i) ret.add(new SettingsNode(items.get(i)));
        return ret.toArray(new SettingsNode[0]);
    }

    SettingsNode(){}

    // ctor for loading from json
    SettingsNode(Map map){
        if (map.containsKey("type")) type = (String)map.get("type");
        if (map.containsKey("name")) name = (String)map.get("name");
        if (map.containsKey("description")) description = (String)map.get("description");
        if (map.containsKey("children")) children = fromlist((ArrayList<Map>) map.get("children"));
        if (map.containsKey("options")) options = ((ArrayList<String>)map.get("options")).toArray(new String[0]);
    }
}

interface SettingsCallback {
    void btnclick(View sview, View v, SettingsNode node);
    void textchange(View sview, Editable e, String group, String name, String value);
    void radiochange(View sview, CompoundButton buttonView, boolean isChecked, String group, String name, String option);
    void switchchange(View sview, CompoundButton buttonView, boolean isChecked, String group, String name);
    void listadd(StringList sl, String name, String val);
    void listrm(StringList sl, View elt, String name, String val);
    void pickimage(ImageSel sel, String name);
}

interface SettingsCallbackProxy {
    SettingsCallback getListener();
}

class ImageSel extends LinearLayout implements View.OnClickListener {
    Button m_btn;
    ImageView m_img;
    SettingsNode m_node;
    SettingsCallbackProxy m_proxy;

    ImageSel(Context c, SettingsNode node){
        super(c);
        setOrientation(VERTICAL);
        m_img = new ImageView(c);
        m_btn = new Button(c);
        m_node = node;
        m_btn.setText("Load image from library");
        m_btn.setOnClickListener(this);
        addView(m_img);
        addView(m_btn);
    }

    @Override
    public void onClick(View v) {
        if (m_proxy!=null && m_proxy.getListener()!=null)
            m_proxy.getListener().pickimage(this, m_node.name);
    }
}

class MissingAttr extends Exception {}

class StringList extends LinearLayout implements View.OnClickListener {
    LinearLayout m_elts;
    Button m_btn;
    SettingsCallbackProxy mproxy;
    String TAG = "YS_StringList";
    SettingsCallbackProxy m_proxy;
    SettingsNode m_node;
    int strListDelIcon;

    StringList(Context c, SettingsNode node, int strListDelIcon_) throws MissingAttr {
        super(c);
        strListDelIcon = strListDelIcon_;
        if (strListDelIcon_ == 0) throw new MissingAttr();
        setOrientation(VERTICAL);
        m_node = node;
        m_elts = new LinearLayout(c);
        m_elts.setOrientation(VERTICAL);
        m_btn = new Button(c);
        m_btn.setOnClickListener(this);
        m_btn.setText(m_node.description);
        addView(m_elts);
        addView(m_btn);
    }

    public void onClick(View v){
        final TextInputEditText editor = new TextInputEditText(v.getContext());
        final StringList outer = this;
        AlertDialog dialog = new AlertDialog.Builder(v.getContext())
            .setTitle(m_node.description)
            .setView(editor)
            .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                @Override public void onClick(DialogInterface dialog, int which) {
                    if (m_proxy != null && m_proxy.getListener() != null)
                        m_proxy.getListener().listadd(outer, m_node.name, editor.getText().toString());
                }
            })
            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override public void onClick(DialogInterface dialog, int which) {}
            }).show();
    }

    // note: this is used when building the view from cloud data, not when 'add' is clicked.
    void addElt(Context c, final String val){
        final LinearLayout elt = new LinearLayout(c);
        elt.setOrientation(HORIZONTAL);
        TextView tv = new TextView(c);
        tv.setText(val);
        ImageButton btn = new ImageButton(c);
        btn.setImageResource(strListDelIcon);
        elt.addView(btn);
        elt.addView(tv);
        final StringList outer = this;
        btn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // todo: ideally we want to send the server a key, not the preview text
                if (m_proxy != null && m_proxy.getListener() != null)
                    m_proxy.getListener().listrm(outer, elt, m_node.name, val);
            }
        });
        m_elts.addView(elt);
    }
}

public class YamlSettings extends LinearLayout implements SettingsCallbackProxy {
    final String AttrNs = "http://schemas.android.com/apk/res-auto";
    final String TAG = "NM_YamlSettings";
    SettingsNode[] prefs;
    SettingsCallback listener;
    Map<String, View> view_lookup;
    int strListDelIcon;

    public YamlSettings(Context c, AttributeSet attrs) throws IOException, MissingAttr {
        super(c, attrs);
        strListDelIcon = attrs.getAttributeResourceValue(AttrNs, "strListDelIcon", 0);
        InputStream stream = getResources().openRawResource(attrs.getAttributeResourceValue("http://schemas.android.com/apk/res-auto", "yamlRawId", 0));
        YamlReader r = new YamlReader(IOUtils.toString(stream, "UTF-8"));
        prefs = SettingsNode.fromlist((ArrayList<Map>) r.read());
        setOrientation(VERTICAL);
        view_lookup = new HashMap<String, View>();
        addNodes(c, prefs, null);
    }

    void addNodes(Context c, SettingsNode[] nodes, String group) throws MissingAttr {
        for (int i=0;i<nodes.length;++i) addNode(c, nodes[i], group);
    }

    static LayoutParams def_layout(){return new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);}

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

    void addNode(Context c, final SettingsNode node, final String group) throws MissingAttr {
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
        if (node.children != null) addNodes(c, node.children, node.name);
    }

    String fullname(String group, String name){return group == null ? name : group+"."+name;}

    public SettingsCallback getListener(){
        return listener;
    }
}
