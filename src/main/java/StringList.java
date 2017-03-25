package com.github.abe_winter.yaml_settings;

import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.support.v7.app.AlertDialog;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;

public class StringList extends LinearLayout implements View.OnClickListener {
    LinearLayout m_elts;
    Button m_btn;
    SettingsCallbackProxy mproxy;
    String TAG = "YS_StringList";
    SettingsCallbackProxy m_proxy;
    SettingsNode m_node;
    int strListDelIcon;

    public StringList(Context c, SettingsNode node, int strListDelIcon_) throws MissingAttr {
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
    public void addElt(Context c, final String val){
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
