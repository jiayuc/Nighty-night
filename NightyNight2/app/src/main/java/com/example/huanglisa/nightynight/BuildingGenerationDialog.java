package com.example.huanglisa.nightynight;

import android.app.AlertDialog;
import android.app.Dialog;
import android.support.v4.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.example.huanglisa.nightynight.rest.ApiClient;
import com.example.huanglisa.nightynight.rest.BuildingApiInterface;


/**
 * Created by huanglisa on 11/26/16.
 */

public class BuildingGenerationDialog extends DialogFragment {
    private static final String TAG = "BuildingDialog";

    // Use this instance of the interface to deliver action events
    BuildingFragment mListener;
    private BuildingApiInterface buildingApiInterface;
    SessionManager session;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        mListener = (BuildingFragment) getTargetFragment();


        //session
        session = new SessionManager(getContext().getApplicationContext());

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        View view = inflater.inflate(R.layout.fragment_building_generation, null);
        final TextInputEditText nameInput = (TextInputEditText) view.findViewById(R.id.building_name);
        builder.setView(view)
                // Add action buttons
                .setPositiveButton("create", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int int_id) {
                        String name = nameInput.getText().toString();
                        if(name.length() == 0){
                            Toast.makeText(getContext(), "please type in valid name", Toast.LENGTH_LONG).show();
                        }
                        mListener.onBuildingGenerationDialogPositiveClick(name);
                        //String name = "Grayce";
                        //String id = "temp";
                        //mListener.onFriendSearchDialogPositiveClick(true, id, name, email);
                    }
                })
                .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //mListener.onDialogNegativeClick(FriendEmailSearchFragment.this);
                        BuildingGenerationDialog.this.getDialog().cancel();
                    }
                });
        return builder.create();
    }
}
