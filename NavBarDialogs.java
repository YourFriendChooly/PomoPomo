package com.wearelast.pomo;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by OBEY-YOSEMITE on 16-02-12.
 */
public class NavBarDialogs {

    //Context
    private static Context context;

    //Key strings
    private static String CYCLE_LABEL = "CYCLE_LABEL";
    private static String KEY_WORK = "KEY_WORK";
    private static String KEY_LONG = "KEY_LONG";
    private static String KEY_SHORT = "KEY_SHORT";
    private static String KEY_INTERVAL = "KEY_INTERVAL";
    private static String CYCLE_LIST = "CYCLE_LIST";

    //ArrayList for Headers
    private static ArrayList<String> listHeaders;

    public NavBarDialogs(Context context) {
        this.context = context;
    }

    //New Cycle Dialog
    public static class New extends DialogFragment{
        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            LayoutInflater inflater = getActivity().getLayoutInflater();
            // Inflate and set the layout for the dialog
            // Pass null as the parent view because its going in the dialog
            builder.setView(inflater.inflate(R.layout.activity_new_cycle, null))
                    // Add action buttons
                    .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {

                            //View Handlers and variable Declarations
                            final EditText workP, shortP, longP, intervalP;
                            workP = (EditText) getDialog().findViewById(R.id.txtWorkP);
                            shortP = (EditText) getDialog().findViewById(R.id.txtShortP);
                            longP = (EditText) getDialog().findViewById(R.id.txtLongP);
                            intervalP = (EditText) getDialog().findViewById(R.id.txtIntervalP);

                            //W= work period, S= Short Break, L = Long Break, pi = Intervals;
                            int w = Integer.parseInt(workP.getText().toString());
                            int s = Integer.parseInt(shortP.getText().toString());
                            int l = Integer.parseInt(longP.getText().toString());
                            int pi = Integer.parseInt(intervalP.getText().toString());

                            //Calls MainActivity.Cycle to load the cycle settings to MainActivity
                            MainActivity.Cycle cycle = new MainActivity.Cycle();
                            cycle.setCycle(w, s, l, pi);
                        }
                    })
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            New.this.getDialog().cancel();
                        }
                    }).setNeutralButton("Need Help?", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //TODO Code Instructional Dialog for Pomodoro Timings.
                }
            });
            return builder.create();
        }
    }

    //Save Cycle Dialog
    public static class Save extends DialogFragment{
        SharedPreferences p = context.getSharedPreferences(CYCLE_LABEL, 0);

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            LayoutInflater inflater = getActivity().getLayoutInflater();
            builder.setView(inflater.inflate(R.layout.activity_save_cycle, null))
                    // Add action buttons
                    .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            EditText saveName = (EditText) getDialog().findViewById(R.id.txtSaveName);

                            //Saves Cycle Details from Current Running Cycle
                            SharedPreferences.Editor editor = p.edit();
                            editor.putLong(KEY_WORK, MainActivity.Cycle.workTime);
                            editor.putLong(KEY_LONG, MainActivity.Cycle.longBreak);
                            editor.putLong(KEY_SHORT, MainActivity.Cycle.shortBreak);
                            editor.putLong(KEY_INTERVAL, MainActivity.Cycle.pomoIntervals);
                            editor.commit();

                            //Saves Cycle Headers to Prefs for ArrayAdapter Access
                            //Check if headers exists, if not, create.
                            CYCLE_LABEL = saveName.getText().toString();
                            try {
                                listHeaders.add(CYCLE_LABEL);
                            } catch (Exception e){
                                listHeaders = new ArrayList<String>();
                                listHeaders.add(CYCLE_LABEL);
                                Log.d("Cycle List", "Exception Thrown");
                            }

                            Set<String> set = new HashSet<String>(listHeaders);
                            p = context.getSharedPreferences(CYCLE_LIST, 0);
                            editor = p.edit();
                            editor.putStringSet(CYCLE_LIST, set);
                            editor.commit();
                        }
                    })
                    .setTitle(R.string.drawer_diag_save);
            return builder.create();
        }
    }

    //Load Cycle Dialog
    public static class Load extends DialogFragment{
        ArrayAdapter listAdapter;
        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            try{
                SharedPreferences p = context.getSharedPreferences(CYCLE_LIST, 0);
                listHeaders.clear();
                listHeaders.addAll(p.getStringSet(CYCLE_LIST, new HashSet<String>()));
                listAdapter.notifyDataSetChanged();
            } catch (Exception e){
                SharedPreferences p = context.getSharedPreferences(CYCLE_LIST, 0);
                Set<String> set = p.getStringSet(CYCLE_LIST, new HashSet<String>());
                Log.d("Set Size", ""+set.size());
                listHeaders = new ArrayList<>(set);
                listAdapter = new ArrayAdapter<String>
                        (getContext(), android.R.layout.simple_list_item_1, listHeaders);
                listAdapter.notifyDataSetChanged();
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setAdapter(listAdapter, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Log.d("ITEM CLICK", "NUMBER"+which);
                }
            });
            return builder.create();
        }
    }

}
