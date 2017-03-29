package com.github.abe_winter.yaml_settings;

import android.content.Context;
import android.support.design.widget.TabLayout;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import java.util.ArrayList;

public class YamlPager extends PagerAdapter {
    final String TAG = "YS/YP";
    TabLayout mtabber;
    ViewPager mpager;
    ArrayList<String> mtitles;
    public ArrayList<LinearLayout> mpages;
    ArrayList<FrameLayout> mframes; // todo: can I add a ScrollView directly?

    YamlPager(Context c){
        mtabber = new TabLayout(c);
        mtabber.setLayoutParams(new TabLayout.LayoutParams(
            TabLayout.LayoutParams.MATCH_PARENT,
            TabLayout.LayoutParams.WRAP_CONTENT,
            Gravity.TOP
        ));
        mpager = new ViewPager(c);
        mtabber.setupWithViewPager(mpager);
        ViewPager.LayoutParams lparams = new ViewPager.LayoutParams();
        lparams.width = ViewGroup.LayoutParams.MATCH_PARENT;
        lparams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        mpager.setLayoutParams(lparams);
        mpages = new ArrayList<>();
        mframes = new ArrayList<>();
        mtitles = new ArrayList<>();
        mpager.setAdapter(this);
    }

    void addView(View v){
        mpages.get(mpages.size()-1).addView(v);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        container.addView(mframes.get(position));
        return mframes.get(position);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView(mframes.get(position));
    }

    @Override
    public int getCount() {return mpages.size();}

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mtitles.get(position);
    }

    void addTab(Context c, SettingsNode node){
        mtitles.add(node.description != null ? node.description : node.name);
        mframes.add(new ScrollView(c));
        LinearLayout ll = new LinearLayout(c);
        ll.setLayoutParams(YamlSettings.def_layout());
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.setFocusable(true);
        ll.setFocusableInTouchMode(true);
        int dip10 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10.f, c.getResources().getDisplayMetrics());
        ll.setPadding(dip10, dip10, dip10, dip10);
        mpages.add(ll);
        mframes.get(mframes.size()-1).addView(ll);
        mframes.get(mframes.size()-1).setLayoutParams(YamlSettings.def_layout());
    }

    void report(){
        Log.d(TAG, "YamlAdapter has "+mpages.size()+" pages");
        for (int i=0;i<mpages.size();++i) Log.d(TAG, "page "+i+" has "+mpages.get(i).getChildCount());
    }
}
