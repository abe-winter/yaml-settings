package com.github.abe_winter.yaml_settings;

import android.view.View;
import android.text.Editable;
import android.widget.CompoundButton;

public interface SettingsCallback {
    void btnclick(View sview, View v, SettingsNode node);
    void textchange(View sview, Editable e, String group, String name, String value);
    void radiochange(View sview, CompoundButton buttonView, boolean isChecked, String group, String name, String option);
    void switchchange(View sview, CompoundButton buttonView, boolean isChecked, String group, String name);
    void listadd(StringList sl, String name, String val);
    void listrm(StringList sl, View elt, String name, String val);
    void pickimage(ImageSel sel, String name);
}
