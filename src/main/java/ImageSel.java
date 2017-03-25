package com.github.abe_winter.yaml_settings;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class ImageSel extends LinearLayout implements View.OnClickListener {
    Button m_btn;
    public ImageView m_img;
    public SettingsNode m_node;
    SettingsCallbackProxy m_proxy;

    public ImageSel(Context c, SettingsNode node){
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
