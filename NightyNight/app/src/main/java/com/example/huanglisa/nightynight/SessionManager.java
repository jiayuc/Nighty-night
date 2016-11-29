package com.example.huanglisa.nightynight;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by huanglisa on 11/4/16.
 */

public class SessionManager {
    //shared preferences
    SharedPreferences pref;

    //editor for shared preference
    SharedPreferences.Editor editor;

    //context
    Context _context;

    //shared pref mode
    int PRIVATE_MODE = 0;

    private static final String PREF_NAME = "AndroidHivePref";
    private static final String IS_LOGIN ="isLoggedIn";

    public static final String KEY_NAME = "name";
    public static final String KEY_EMAIL ="email";
    public static final String KEY_TOKEN ="token";
    public static final String KEY_BUILDINGS = "buildings";

    public SessionManager(Context context){
        this._context = context;
        pref = context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    public String getToken(){
        return pref.getString(KEY_TOKEN, "");
    }
    //create login session
    public void createLoginSession(String name, String email, String token){
        editor.putBoolean(IS_LOGIN, true);
        editor.putString(KEY_NAME, name);
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_TOKEN, token);
        //commit change
        editor.commit();
    }



    //create new login page if not log in
    public void checkLogin(){
        if(!this.isLoggedIn()){
            Intent intent = new Intent(_context, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            _context.startActivity(intent);
        }
    }

    /**
     * Clear session details
     * */
    public void logoutUser(){
        // Clearing all data from Shared Preferences
        System.out.format("log out!!%n");
        editor.clear();
        editor.commit();

        // After logout redirect user to Loing Activity
        Intent i = new Intent(_context, MainActivity.class);

        // Closing all the Activities
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        // Add new Flag to start new Activity
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        // Staring Login Activity
        _context.startActivity(i);
    }

    /**
     * Quick check for login
     * **/
    // Get Login State
    public boolean isLoggedIn(){
        System.out.format("check log in:%b%n", pref.getBoolean(IS_LOGIN, false));
        return pref.getBoolean(IS_LOGIN, false);
    }

    public void addBuildingList(List<ReceivedBuilding> list){
        //Set<String> buildingList = new HashSet<>();
        String buildingList = "";

        for(ReceivedBuilding building : list){
            //buildingList.add(building.id);
            if(buildingList == ""){
                buildingList = buildingList + building.id+","+building.name+","+building.index;
            } else {
                buildingList = buildingList + "|" + building.id + "," + building.name+","+building.index;
            }
        }

        editor.putString(KEY_BUILDINGS, buildingList);
        System.out.format("list length: %d%n", list.size());
        System.out.format("just created buildingList %s%n", buildingList);
        editor.commit();

    }

    public void updateBuilding(ReceivedBuilding rb){
        String buildingString = pref.getString(KEY_BUILDINGS, null);
        buildingString = buildingString.concat("|" + rb.id + "," + rb.name + "," + rb.index);
        System.out.format("updated buildingString %s%n", buildingString);
        editor.putString(KEY_BUILDINGS, buildingString);
        editor.commit();
    }

    public List<ReceivedBuilding> getBuildingList(){
        List<ReceivedBuilding> idList = new ArrayList<>();
        String buildingString = pref.getString(KEY_BUILDINGS, null);
        System.out.format("%nbuildingString %s%n", buildingString);
        if(buildingString == null){
            return idList;
        }
        String[] list = buildingString.split("\\|");
        System.out.format("first item: %s%n", list[1]);
        for(String pair : list){
            String[] pairString = pair.split(",");
            System.out.format("pair: %s, pariString size %d%n", pair, pairString.length);
            if(pairString.length != 3){
                continue;
            }
            String id = pairString[0];
            String name =pairString[1];
            int index = Integer.parseInt(pairString[2]);
            ReceivedBuilding rb = new ReceivedBuilding(id, name, index, pref.getString(KEY_TOKEN, null));
            idList.add(rb);
        }
        return idList;

    }

}
