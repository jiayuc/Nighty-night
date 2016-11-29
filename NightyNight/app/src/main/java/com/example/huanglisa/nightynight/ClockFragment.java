package com.example.huanglisa.nightynight;

import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.ALARM_SERVICE;

import com.example.huanglisa.nightynight.rest.ApiClient;
import com.example.huanglisa.nightynight.rest.UserApiInterface;
import com.example.huanglisa.nightynight.rest.ClockApiInterface;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import android.app.AlarmManager;

/**
 * Created by huanglisa on 11/1/16.
 */

public class ClockFragment extends Fragment implements RecyclerViewSwitchListener{
    private static final String TAG = "ClockFragment";
    private List<ClockItem> clockList = new ArrayList<>();
    private RecyclerView recyclerView;
    private ClockListAdapter clockAdapter;
    private Button addBtn;
    private static final int REQUEST_ADD = 1;
    private UserApiInterface userApiInterface;
    private ClockApiInterface clockApiInterface;

    private ProgressDialog progress;

    public Calendar calender;
    AlarmManager alarmManager;
    private PendingIntent pendingIntent;
    private Intent alarmReceiverIntent;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


        //API
        userApiInterface = ApiClient.getClient().create(UserApiInterface.class);
        clockApiInterface = ApiClient.getClient().create(ClockApiInterface.class);

        View view = inflater.inflate(R.layout.fragment_clock, container, false);
        recyclerView = (RecyclerView) view.findViewById(R.id.clockList_view);
        clockAdapter = new ClockListAdapter(clockList, this);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(clockAdapter);

        //instance of progressBar
        generateProgressBar();
        prepareClockData();




        //alarm
        // Get the alarm manager service
        alarmManager = (AlarmManager) getContext().getSystemService(ALARM_SERVICE);
        // set the alarm to the time that you picked
        calender = Calendar.getInstance(TimeZone.getDefault(), Locale.getDefault());
        //pending intent, will be responsible to create notification
        alarmReceiverIntent = new Intent(getContext(), AlarmReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(getContext(), 0, alarmReceiverIntent, PendingIntent.FLAG_UPDATE_CURRENT);



        addBtn = (Button)view.findViewById(R.id.addButton);
        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), ClockSetterActivity.class);
                startActivityForResult(intent, REQUEST_ADD);
            }
        });
        return view;


    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(data != null) {
            if (requestCode == REQUEST_ADD) {
                System.out.format("add clock item%n");
                addClockData(data.getExtras().getInt("sleepHour"), data.getExtras().getInt("sleepMin"), data.getExtras().getInt("wakeupHour"), data.getExtras().getInt("wakeupMin"));
            }
        }
    }

    @Override
    public void onViewSwitched(int pos){
        int activatedItemCount = 0;
        for(int i=0; i<clockList.size(); i++){
            if(i == pos){
                String id = null;
                System.out.format("clock status: %b%n", clockList.get(i).getStatus());
                if(clockList.get(i).getStatus() == false) { //need to set a clock
                    activatedItemCount = activatedItemCount + 1;
                    clockList.get(i).setStatus(true);
                    id = clockList.get(i).getId();
                } else { //cancel a clock
                    cancelAlarm();
                    clockList.get(i).setStatus(false);
                }
                Call<ReceivedClock> call = userApiInterface.userUpdateActiveClock(((MainActivity) getActivity()).session.getToken(), id);
                call.enqueue(new Callback<ReceivedClock>() {
                    @Override
                    public void onResponse(Call<ReceivedClock> call, Response<ReceivedClock> response) {
                        if (!response.isSuccessful()) {
                            Log.e(TAG, "failed to update clock item for user");
                            return;
                        }
                        Log.d(TAG, "succeed to update clock item for user");
                        if(response.body() == null) {
                            System.out.format("clock is null");
                        }
                        if(response.body() != null) {
                            setAlarm(response.body().wakeHour, response.body().wakeMin);
                        }
                    }

                    @Override
                    public void onFailure(Call<ReceivedClock> call, Throwable t) {
                        Log.e(TAG, "failed to update clock item for user");
                    }
                });
            }else if(clockList.get(i).getStatus() != false){
                clockList.get(i).setStatus(false);
            }
        }
        if(activatedItemCount > 1){
            Log.e(TAG, "more than one items are activated");
            return;
        }
        clockAdapter.notifyDataSetChanged();
    }

    public void setAlarm(int hour, int min){
        cancelAlarm();


        calender.set(Calendar.HOUR_OF_DAY, hour);
        calender.set(Calendar.MINUTE, min);
        calender.set(Calendar.SECOND, 0);
        calender.set(Calendar.AM_PM, hour < 12 ? Calendar.AM : Calendar.PM);
        System.out.format("set alarm: %d:%d %d%n", hour, min, calender.getTimeInMillis());
        alarmManager.set(AlarmManager.RTC_WAKEUP, calender.getTimeInMillis(), pendingIntent);

    }

    public void cancelAlarm(){
        System.out.format("cancel alarm!%n");
        alarmManager.cancel(pendingIntent);
    }

    public void generateProgressBar(){
        progress = new ProgressDialog(getContext());
        progress.setTitle("Loading");
        progress.setMessage("Wait while loading...");
        progress.setCancelable(false);
    }

    public void addClockData(int sleepHour, int sleepMin, int wakeHour, int wakeMin){

        Call <ReceivedClock> call = clockApiInterface.addClock(((MainActivity)getActivity()).session.getToken(), sleepHour, sleepMin, wakeHour, wakeMin);
        call.enqueue(new Callback<ReceivedClock>() {
            @Override
            public void onResponse(Call<ReceivedClock> call, Response<ReceivedClock> response) {
                if(!response.isSuccessful()){
                    Log.e(TAG, "failed to add clock item "+response.message());
                    return;
                }


                ClockItem cItem = new ClockItem(response.body().sleepHour,response.body().sleepMin,response.body().wakeHour,response.body().wakeMin, response.body().id, false);
                clockList.add(cItem);
                System.out.format("notify clock list, new list size: %d%n", clockList.size());
                clockAdapter.notifyItemInserted(clockList.size()-1);
                //clockAdapter.notifyItemRangeChanged(0, clockList.size());
            }

            @Override
            public void onFailure(Call<ReceivedClock> call, Throwable t) {
                Log.e(TAG, "failed to add clock item (onFailure) "+t.getMessage());
            }
        });

    }



    private void prepareClockData(){
        progress.show();
        Call<ReceivedClock> getActiveClockCall = userApiInterface.userGetActiveClock(((MainActivity)getActivity()).session.getToken());
        getActiveClockCall.enqueue(new Callback<ReceivedClock>() {
            @Override
            public void onResponse(Call<ReceivedClock> call1, Response<ReceivedClock> response) {
                if(!response.isSuccessful()) {
                    Log.e(TAG, "failed to get activated clock " + response.message());
                    progress.dismiss();
                    return;
                }
                final String id = response.body() == null? null : response.body().id;
                System.out.format("activated id: %s%n", id);
                Call<List<ReceivedClock>> getListCall = clockApiInterface.getUserClocks(((MainActivity)getActivity()).session.getToken());
                getListCall.enqueue(new Callback<List<ReceivedClock>>() {
                    @Override
                    public void onResponse(Call<List<ReceivedClock>> call2, Response<List<ReceivedClock>> response) {

                        if(!response.isSuccessful()){
                            Log.e(TAG, "failed to get clock items "+response.message());
                            progress.dismiss();
                            return;
                        }
                        for(ReceivedClock clock : response.body()){
                            boolean ifTurnedOn = false;
                            if( id != null && clock.id.equals(id)){
                                System.out.format("get activated clock!!!!");
                                ifTurnedOn = true;
                            }
                            ClockItem cItem = new ClockItem(clock.sleepHour, clock.sleepMin, clock.wakeHour, clock.wakeMin, clock.id, ifTurnedOn);

                            clockList.add(cItem);

                        }
                        Log.e(TAG, "get clock items"+response.body().size());
                        clockAdapter.notifyDataSetChanged();
                        progress.dismiss();
                    }

                    @Override
                    public void onFailure(Call<List<ReceivedClock>> call2, Throwable t) {
                        Log.e(TAG, "failed to get clock items " + t.getMessage());
                        progress.dismiss();
                    }
                });
            }

            @Override
            public void onFailure(Call<ReceivedClock> call1, Throwable t) {
                Log.e(TAG, "failed to get activated clock(on Failure) "+t.getMessage());
                progress.dismiss();
            }
        });


    }

}
