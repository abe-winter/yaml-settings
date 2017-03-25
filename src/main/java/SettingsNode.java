package com.github.abe_winter.yaml_settings;

import java.util.Map;
import java.util.ArrayList;

public class SettingsNode {
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
