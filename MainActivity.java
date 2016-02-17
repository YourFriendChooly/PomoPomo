package com.wearelast.pomo;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Shader;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback{

    public static class Cycle {
        /**
         * Public class Cycle contains interval settings, as well as conversion method.
         */
        private static long min2milli (int min){
            //TODO change SECONDS to MINUTES for release.
            long m = TimeUnit.SECONDS.toMillis(min);
            return m;
        }
        static long workTime = min2milli(10);
        static long shortBreak = min2milli(5);
        static long longBreak = min2milli(7);
        static long pomoIntervals = 4;

        public void setCycle (int w, int s, int l, long pi){
            workTime = min2milli(w);
            shortBreak = min2milli(s);
            longBreak = min2milli(l);
            pomoIntervals = pi;
        }
    }

    public TextView txtTimeRemaining;

    //Create our ProgressBar
    ProgressBar progressBar;

    int currentCount = 0;

    //Our imageview for our little Pomo
    ImageView pomoHolder, distractionHolder;
    AnimationDrawable pomoRun, distractionRun;

    //Surfaceview for background
    SurfaceView uxSurface;
    SurfaceHolder uxHolder;

    public SurfaceHolder sh;
    Bitmap rxBackground;
    int bgY = 0;
    public Context ctx;
    BitmapDrawable bitmapDrawable;
    BgMovementThread thread;
    private int mBGFarMoveX = 0;
    private int mBGNearMoveX = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Toolbar for Pomo
        //TODO Populate some additional icons.. Possibly change view styles for countdown. Or perhaps a start / stop button.
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //UI Assignments
        progressBar = (ProgressBar) findViewById(R.id.uxProgressBar);
        pomoHolder = (ImageView) findViewById(R.id.uxPomoRun);
        distractionHolder = (ImageView) findViewById(R.id.uxDistractionCloud);
        pomoRun = (AnimationDrawable) pomoHolder.getBackground();
        distractionRun = (AnimationDrawable) distractionHolder.getBackground();
        txtTimeRemaining = (TextView) findViewById(R.id.txtTimeRemaining);

        //Calls drawerMake to populate our navigation drawer.
        drawerMake(this, toolbar);

        //Let's get our surfaceview going.
        uxSurface = (SurfaceView) findViewById(R.id.uxSurfaceView);
        uxHolder = uxSurface.getHolder();


        Button bQuickStart = (Button) findViewById(R.id.bQuickStart);
        //Quickstart runs a default pomodoro cycle and starts our animations.
        bQuickStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startTimer(Cycle.workTime);
                pomoRun.start();
                distractionRun.start();
                thread.start();
            }
        });

        Button bLoadLast = (Button) findViewById(R.id.bLoadLast);
        bLoadLast.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO Load last pomo configuration from memory and begin timer.
            }
        });

        //Resources for the Background
        rxBackground = BitmapFactory.decodeResource(getResources(),R.drawable.background1);
        bitmapDrawable = new BitmapDrawable(getResources(), rxBackground);
        bitmapDrawable.setTileModeX(Shader.TileMode.REPEAT);
        sh = uxSurface.getHolder();
        sh.addCallback(this);
    }

    //Thread for handling background
    class BgMovementThread extends Thread {

        boolean run = true;

        public BgMovementThread(SurfaceHolder surfaceHolder, Context context) {
            sh = surfaceHolder;
            ctx = context;
        }

        public void run() {
            while (run) {
                Canvas c = null;
                try {
                    c = sh.lockCanvas(null);
                    synchronized (sh) {
                        doDraw(c);
                    }
                } finally {
                    if (c != null) {
                        sh.unlockCanvasAndPost(c);
                    }
                }
            }
        }

        private void doDraw(Canvas canvas) {
            // decrement the far background
            mBGFarMoveX = mBGFarMoveX - 6;
            // decrement the near background
            mBGNearMoveX = mBGNearMoveX - 6;
            // calculate the wrap factor for matching image draw
            int newFarX = rxBackground.getWidth() - (-mBGFarMoveX);
            // if we have scrolled all the way, reset to start
            if (newFarX <= 0) {
                mBGFarMoveX = 0;
                // only need one draw
                canvas.drawBitmap(rxBackground, mBGFarMoveX, 5, null);
            } else {
                // need to draw original and wrap
                canvas.drawBitmap(rxBackground, mBGFarMoveX, 5, null);
                canvas.drawBitmap(rxBackground, newFarX, 5, null);
            }
        }
    }

    //Starts the PomoTimer
    public void startTimer(long startTime){
        final PomoTimer pomoTimer = new PomoTimer(startTime, 1000);
        pomoTimer.start();
    }

    //Create and populate the drawer.
    public void drawerMake(Activity a, Toolbar t){
        //Drawer Items
        PrimaryDrawerItem item0 = new PrimaryDrawerItem().withName(R.string.drawer_new);
        PrimaryDrawerItem item1 = new PrimaryDrawerItem().withName(R.string.drawer_load);
        PrimaryDrawerItem item2 = new PrimaryDrawerItem().withName(R.string.drawer_save);
        PrimaryDrawerItem item3 = new PrimaryDrawerItem().withName(R.string.drawer_stats);
        SecondaryDrawerItem item4 = new SecondaryDrawerItem().withName(R.string.drawer_cheevos);
        SecondaryDrawerItem item5 = new SecondaryDrawerItem().withName(R.string.drawer_unlocks);

        //Create the drawer
        Drawer drawer = new DrawerBuilder()
                .withActivity(a)
                .withToolbar(t)
                .addDrawerItems(
                        item0, item1, item2, item3,
                        new DividerDrawerItem(),
                        item4, item5
                )
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        Log.d("DRAWERCLICK", "ITEM "+position);
                        NavBarDialogs diag = new NavBarDialogs(getApplicationContext());
                        switch (position){
                            case 0:
                                NavBarDialogs.New n = new NavBarDialogs.New();
                                n.show(getSupportFragmentManager(), "newCycle");
                                break;
                            case 1:
                                NavBarDialogs.Load l = new NavBarDialogs.Load();
                                l.show(getSupportFragmentManager(), "loadCycle");
                                //LoadCycle loadCycle = new LoadCycle();
                                //loadCycle.show(getSupportFragmentManager(), "loadCycle");
                                break;
                            case 2:
                                NavBarDialogs.Save s = new NavBarDialogs.Save();
                                s.show(getSupportFragmentManager(), "saveCycle");
//                                SaveCycle saveCycle = new SaveCycle();
//                                saveCycle.getContext(getApplicationContext());
//                                saveCycle.show(getSupportFragmentManager(), "saveCycle");
                            case 3:
                                //TODO Drawer Stats
                                break;
                            case 4:
                                //TODO Drawer Acheivements
                                break;
                            case 5:
                                //TODO Drawer Unlockables
                        }
                        return false;
                    }
                }).build();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Canvas c = holder.lockCanvas();
        c.drawBitmap(rxBackground, 0, 5, null);
        holder.unlockCanvasAndPost(c);
        thread = new BgMovementThread(sh, ctx);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    //The Pomodoro Countdown Timer.
    public class PomoTimer extends CountDownTimer {

        /**
         * @param millisInFuture    The number of millis in the future from the call
         *                          to {@link #start()} until the countdown is done and {@link #onFinish()}
         *                          is called.
         * @param countDownInterval The interval along the way to receive
         *                          {@link #onTick(long)} callbacks.
         */
        public PomoTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
            //Reset progressbar to most recent value of timer in cycle.
            progressBar.setMax((int) millisInFuture);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            String minutesUntilFinished = String.valueOf(TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished));
            txtTimeRemaining.setText(minutesUntilFinished);

            //Increment the progressbar every interval with the remaining time for the timer cycle.
            int progressValue = progressBar.getMax() - (int) millisUntilFinished;
            progressBar.setProgress(progressValue);
        }


        @Override
        public void onFinish() {
            //TODO Confirm that current Configuration is correct.
            //For loop's job is to switch between the Work Cycles and the Short Break cycles until a
            //set # of cycles has been reached, then reset.
            if (currentCount == Cycle.pomoIntervals*2) {
                AnimStateSwitch(2);
                startTimer(Cycle.longBreak);
                currentCount = 0;
            } else {
                switch (currentCount % 2) {
                    case 0:
                        AnimStateSwitch(0);
                        startTimer(Cycle.shortBreak);
                        break;
                    case 1:
                        AnimStateSwitch(1);
                        startTimer(Cycle.workTime);
                        break;
                } currentCount++;
            }

            //progressBar.setProgress(0);
            //progressBar.invalidate();
        }
    }

    //The Animation State Switcher
    public void AnimStateSwitch(int state) {
        switch (state){
            case 0:
                //pomoHolder.setBackgroundResource(R.drawable.work_period);
                pomoRun.start();
                Log.d("State Switch","Work Period");
                return;
            case 1:
                //TODO Code pomoHolder.setBackgroundResource with SHORT_BREAK animation.
                Log.d("State Switch","Short Break");
                return;

            case 2:
                //TODO Code pomoHolder.setBackgroundResource with LONG_BREAK animation.
                Log.d("State Switch","Long Break");
                return;
        }
    }
}


