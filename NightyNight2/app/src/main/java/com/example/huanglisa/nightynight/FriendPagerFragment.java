package com.example.huanglisa.nightynight;

/**
 * Created by huanglisa on 11/20/16.
 */

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

public class FriendPagerFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        System.out.format("get here%n");
        View view=inflater.inflate(R.layout.pager_friend, container, false);
        ViewPager pager=(ViewPager)view.findViewById(R.id.friend_pager);
        pager.setAdapter(buildAdapter());



        return(view);
    }

    private PagerAdapter buildAdapter() {
        return(new FriendViewAdapter(getActivity(), getChildFragmentManager()));
    }
}