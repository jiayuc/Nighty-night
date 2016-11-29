package com.example.huanglisa.nightynight;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by huanglisa on 11/20/16.
 */

public class FriendViewAdapter extends FragmentPagerAdapter {
    Context ctxt=null;





    public FriendViewAdapter(Context ctxt, FragmentManager mgr) {
        super(mgr);
        this.ctxt=ctxt;
    }

    @Override
    public int getCount() {
        return(10);
    }

    @Override
    public Fragment getItem(int position) {
        return(FriendFragment.newInstance(position));
    }




}