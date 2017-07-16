package watch.stopwatch;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Resources;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Message;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.annotation.ColorInt;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private enum StopwatchState {RUNNING, STOPPED, PAUSED}
    private enum TimerState {RUNNING, STOPPED, PAUSED, SET}
    private enum CenterBtnFeedback {SOUND, VIBRATE, BOTH, NONE}
    private enum Mode {BTN, VOL_UP, VOL_DN, CLAP, SWING, PROX}

    private static final String TAG = MainActivity.class.getSimpleName();

    // Key for shared preferences to retrieve the preset timers
    private static String KEY_PRESET_TIMERS = "key_preset_timers";

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private CustomViewPager mViewPager;
    private ViewPager.OnPageChangeListener pageListener;
    private RadioGroup page_selector;
    private Button btn1;
    private Button btn2;
    private Button btn3;
    private ImageView bigBtn;
    private Vibrator vibe;
    private MessageHandler messageHandler;
    private StopwatchState stopwatch_state = StopwatchState.STOPPED;
    private TimerState timer_state = TimerState.STOPPED;
    private View lapRecordView = null;
    private Time timer_timeout = new Time(0, 0, 0, 0);
    private View timerPresetView = null;
    private RelativeLayout secondary_view;
    private ImageView separator;
    private ArrayList<Time> preset_timers;
    private TimerListAdapter presetTimerAdapter;
    private SharedPreferences sp;

    // Preference variables
    private CenterBtnFeedback centerBtnFeedback = CenterBtnFeedback.VIBRATE;
    private boolean nightModeOn = false;
    private Mode start_mode = Mode.BTN;
    private Mode stop_mode = Mode.BTN;
    private Mode lap_mode = Mode.BTN;
    private SensorManager sensorManager;

    /***********************************************************************************************
     *
     * LISTENERS
     */

    // Listener for certain preference
    private OnSharedPreferenceChangeListener preferenceChangeListener = new OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sp, String key) {
            Context context = getApplicationContext();
            if(key.equals(context.getString(R.string.KEY_TOUCHBTN))){
                setCenterBtnFeedback(context, key);
            }
            else if(key.equals(context.getString(R.string.KEY_START)) ||
                    key.equals(context.getString(R.string.KEY_STOP)) ||
                    key.equals(context.getString(R.string.KEY_LAP)))
                setStartStopLapMode(context, key);
        }
    };

    // Listener for buttons in secondary views
    private OnClickListener secondaryViewBtnListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            int id = v.getId();
            switch(id){
                case R.id.lapTotalTimeBtn: {
                    v.setBackgroundColor(getResources().getColor(R.color.colorPrimaryLight));
                    Button btn = (Button) findViewById(R.id.lapTimeBtn);
                    btn.setBackgroundColor(getResources().getColor(R.color.greyLight));
                    Message message = new Message();
                    message.arg1 = LapsListAdapter.LapsFormat.ABSOLUTE.ordinal(); // set the correct format
                    message.what = MessageHandler.MSG_STOPWATCH_LAP_FORMAT;
                    messageHandler.sendMessage(message);
                    }
                    break;
                case R.id.lapTimeBtn: {
                    v.setBackgroundColor(getResources().getColor(R.color.colorPrimaryLight));
                    Button btn = (Button) findViewById(R.id.lapTotalTimeBtn);
                    btn.setBackgroundColor(getResources().getColor(R.color.greyLight));
                    Message message = new Message();
                    message.arg1 = LapsListAdapter.LapsFormat.RELATIVE.ordinal(); // set the correct format
                    message.what = MessageHandler.MSG_STOPWATCH_LAP_FORMAT;
                    messageHandler.sendMessage(message);
                    }
                    break;
            }
        }
    };

    // Listener for the end of closing secondary view animation
    private AnimatorListener secondaryViewAnimatorListener = new AnimatorListener() {
        @Override
        public void onAnimationStart(Animator animation) {}

        @Override
        public void onAnimationEnd(Animator animation) {
            secondary_view.removeAllViews();
            if(lapRecordView != null) {
                lapRecordView.animate().setListener(null);
                lapRecordView = null;
            }
            if(timerPresetView != null) {
                timerPresetView.animate().setListener(null);
                timerPresetView = null;
            }
        }

        @Override
        public void onAnimationCancel(Animator animation) {}

        @Override
        public void onAnimationRepeat(Animator animation) {}
    };


    // Listener for a click on a preset timer list item
    private OnClickListener timerPresetActionClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            // Get the parent view and the text with the timer
            ViewGroup row = (ViewGroup) v.getParent();
            TextView preset = (TextView) row.findViewById(R.id.timerItem_value);
            if (preset != null) {
                final String[] values = preset.getText().toString().split(":");
                int id = v.getId();
                switch (id) {

                    case R.id.setBtn: {
                        timer_timeout.h = Integer.valueOf(values[0]);
                        timer_timeout.m = Integer.valueOf(values[1]);
                        timer_timeout.s = Integer.valueOf(values[2]);
                        timer_timeout.ms = 0;

                        setTimer();

                        // mark the timer as set
                        timer_state = TimerState.SET;

                        // click on the second button to close the list
                        btn2.performClick();
                    }
                    break;

                    case R.id.editBtn: {
                        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(MainActivity.this);
                        LayoutInflater inflater = getLayoutInflater();
                        final View dialogView = inflater.inflate(R.layout.set_timer_layout, null);
                        dialogBuilder.setView(dialogView);

                        // set the max and the min value for number picker or won't show the wheel
                        final NumberPicker hours = (NumberPicker) dialogView.findViewById(R.id.numberPicker_hours);
                        final NumberPicker min = (NumberPicker) dialogView.findViewById(R.id.numberPicker_min);
                        final NumberPicker sec = (NumberPicker) dialogView.findViewById(R.id.numberPicker_sec);
                        hours.setMaxValue(99);
                        hours.setMinValue(0);
                        hours.setWrapSelectorWheel(true);
                        min.setMaxValue(59);
                        min.setMinValue(0);
                        min.setWrapSelectorWheel(true);
                        sec.setMaxValue(59);
                        sec.setMinValue(0);
                        sec.setWrapSelectorWheel(true);

                        // Set the values to the selected timer
                        hours.setValue(Integer.valueOf(values[0]));
                        min.setValue(Integer.valueOf(values[1]));
                        sec.setValue(Integer.valueOf(values[2]));

                        dialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                Time old_timer = new Time(Integer.valueOf(values[0]), Integer.valueOf(values[1]), Integer.valueOf(values[2]), 0);
                                Time new_timer = new Time(hours.getValue(), min.getValue(), sec.getValue(), 0);

                                // search for this item in the list and update it
                                preset_timers.set(preset_timers.indexOf(old_timer), new_timer);

                                presetTimerAdapter.notifyDataSetChanged();
                                dialog.dismiss();
                            }
                        });
                        dialogBuilder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        AlertDialog b = dialogBuilder.create();
                        b.show();
                    }
                    break;

                    case R.id.deleteBtn: {
                        // search for the item in the list and delete it
                        Time old_timer = new Time(Integer.valueOf(values[0]), Integer.valueOf(values[1]), Integer.valueOf(values[2]), 0);
                        preset_timers.remove(preset_timers.indexOf(old_timer));
                        presetTimerAdapter.notifyDataSetChanged();
                    }
                    break;
                }
            }
        }
    };

    // Listener for long click on addTime btn in timer view
    private OnLongClickListener longClickListener = new OnLongClickListener() {
        @Override
        public boolean onLongClick(final View v) {
            final Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if(v.isPressed()) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if(v.getId() == R.id.addSecBtn)
                                    addSec(v);
                                else
                                    addMin(v);
                            }
                        });
                    }
                    else
                        timer.cancel();
                }
            },100,200);

            return true;
        }
    };

    // Listener for accelerometer and proximity sensors
    private SensorEventListener sensorEventListener = new SensorEventListener() {
        private final int PROXIMITY_THRESHOLD = 4;
        private final int MAGNITUDE_THRESHOLD = 35; // sensitivity to the movement
        private final int ACC_FILTER_DATA_MIN_TIME = 50; // ms
        long lastSaved = System.currentTimeMillis();

        @Override
        public void onSensorChanged(SensorEvent event) {
            if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                // select the correct button
                View btn = null;
                if(start_mode == Mode.SWING || stop_mode == Mode.SWING)
                    btn = bigBtn;
                else if(lap_mode == Mode.SWING)
                    btn = btn1;

                if(btn != null) {
                    // filter some samples in order to avoid multiple matches at the same movement
                    if ((System.currentTimeMillis() - lastSaved) > ACC_FILTER_DATA_MIN_TIME) {
                        lastSaved = System.currentTimeMillis();

                        // Get the values from the sensor
                        float[] acceleration = new float[3];
                        System.arraycopy(event.values, 0, acceleration, 0, event.values.length);

                        double magnitude = Math.sqrt((acceleration[0] * acceleration[0]) +
                                (acceleration[1] * acceleration[1]) +
                                (acceleration[2] * acceleration[2]));

                        if (magnitude > MAGNITUDE_THRESHOLD) {
                            btn.performClick();
                        }
                    }
                }
            }
            else if (event.sensor.getType() == Sensor.TYPE_PROXIMITY) {
                if(event.values[0] <= PROXIMITY_THRESHOLD){
                    // select the correct button
                    View btn = null;
                    if(start_mode == Mode.PROX || stop_mode == Mode.PROX)
                        btn = bigBtn;
                    else if(lap_mode == Mode.PROX)
                        btn = btn1;
                    if (btn != null) {
                        btn.performClick();

                    }
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {}
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // get the sensor manager before reading prefs
        sensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);

        readPreferences();

        if(nightModeOn)
            setTheme(R.style.NightAppTheme);

        setContentView(R.layout.activity_main);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (CustomViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        // Set the limit of pages kept in memory
        mViewPager.setOffscreenPageLimit(mSectionsPagerAdapter.getCount());
        //TODO: if we want to keep the state after the app is closed...
        // http://stackoverflow.com/questions/13273285/how-do-i-tell-my-custom-fragmentpageradapter-to-stop-destroying-my-fragments/

        // Buttons
        btn1 = (Button)findViewById(R.id.button1);
        btn2 = (Button)findViewById(R.id.button2);
        btn3 = (Button)findViewById(R.id.button3);

        // Attach the long listener when the view has been drawn
        mViewPager.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

            public void onGlobalLayout() {
                // remove the listener or it will be called every time the view is drawn
                mViewPager.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                // addMin and addSec LONG listener
                ImageView addTime = mSectionsPagerAdapter.getAddSecBtn();
                addTime.setOnLongClickListener(longClickListener);
                addTime = mSectionsPagerAdapter.getAddMinBtn();
                addTime.setOnLongClickListener(longClickListener);
                // I believe there are 2 big buttons, but it doesn't matter which one is given here
                // because this reference will be used just for perform the click
                // and the callback will take care of everything
                bigBtn = (ImageView)findViewById(R.id.bigBtn);
            }
        });



        // Other elements
        secondary_view = (RelativeLayout)findViewById(R.id.secondary_view);
        separator = (ImageView)findViewById(R.id.buttonsLine);
        final RelativeLayout main_content = (RelativeLayout)findViewById(R.id.main_content);

        // Read the preset timers and set the adapter
        preset_timers = new ArrayList<>();
        readPresetTimers();
        presetTimerAdapter = new TimerListAdapter(preset_timers, getApplicationContext(), timerPresetActionClickListener);

        // Page indicator RadioGroup
        page_selector = (RadioGroup)findViewById(R.id.page_selector);
        page_selector.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId){
                    case R.id.page1: mViewPager.setCurrentItem(0);break;
                    case R.id.page2: mViewPager.setCurrentItem(1);break;
                    case R.id.page3: mViewPager.setCurrentItem(2);break;
                }
            }
        });

        pageListener = new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

            @Override
            public void onPageSelected(int position) {
                // when swipe to the next page, update the radiobutton,
                // the buttons text and the state of reset button
                switch(position){
                    case 0:
                        // stopwatch
                        page_selector.check(R.id.page1);
                        btn1.setText(R.string.btn1_page1_text);
                        btn2.setText(R.string.btn2_page1_text);
                        btn1.setVisibility(View.VISIBLE);
                        btn3.setVisibility(View.VISIBLE);
                        if(stopwatch_state == StopwatchState.RUNNING){
                            // disable reset btn
                            setEnableBtnReset(false);
                        }
                        else{
                            // enable reset btn
                            setEnableBtnReset(true);
                        }
                        // set the background color only for day theme
                        if(!nightModeOn)
                            main_content.setBackgroundColor(getResources().getColor(R.color.background_color));
                        break;
                    case 1:
                        // timer
                        page_selector.check(R.id.page2);
                        btn1.setVisibility(View.VISIBLE);
                        btn3.setVisibility(View.VISIBLE);
                        btn1.setText(R.string.btn1_page2_text);
                        btn2.setText(R.string.btn2_page2_text);
                        if(timer_state == TimerState.RUNNING){
                            // disable reset btn
                            setEnableBtnReset(false);
                        }
                        else{
                            // enable reset btn
                            setEnableBtnReset(true);
                        }
                        // set the background color only for day theme
                        if(!nightModeOn)
                            main_content.setBackgroundColor(getResources().getColor(R.color.background_color));
                        break;
                    case 2:
                        // settings
                        page_selector.check(R.id.page3);
                        btn1.setVisibility(View.GONE);
                        btn3.setVisibility(View.GONE);
                        btn2.setText("Designed by ???");
                        // set the background color only for day theme
                        if(!nightModeOn)
                            main_content.setBackgroundColor(getResources().getColor(R.color.colorPrimaryLight));
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) { }
        };

        mViewPager.addOnPageChangeListener(pageListener);

        vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        // message handler setup
        messageHandler = new MessageHandler(getMainLooper(), getApplicationContext());
    }

    private void setCenterBtnFeedback(Context context, String key){
        String pref = sp.getString(key, context.getString(R.string.vibrate_only));
        if(pref.equals(context.getString(R.string.vibrate_only)))
            centerBtnFeedback = CenterBtnFeedback.VIBRATE;
        else if(pref.equals(context.getString(R.string.sound_only)))
            centerBtnFeedback = CenterBtnFeedback.SOUND;
        else if(pref.equals(context.getString(R.string.soundAndVibrate)))
            centerBtnFeedback = CenterBtnFeedback.BOTH;
        else //none
            centerBtnFeedback = CenterBtnFeedback.NONE;
    }

    private void readPreferences() {
        //TODO: read preferences
        Context context = getApplicationContext();
        sp = PreferenceManager.getDefaultSharedPreferences(context);

        // touch button feedback
        setCenterBtnFeedback(context, context.getString(R.string.KEY_TOUCHBTN));

        // night mode
        String no = context.getString(R.string.KEY_NIGHT_DEFAULT);
        String pref = sp.getString(context.getString(R.string.KEY_NIGHT), no);
        if(pref.equals(no))
            nightModeOn = false;
        else
            nightModeOn = true;

        // start / stop / lap
        setStartStopLapMode(context, context.getString(R.string.KEY_START));

        // screen always on

    }

    private void setStartStopLapMode(Context context, String key) {
        // TODO: finish
        String pref = sp.getString(key, context.getString(R.string.KEY_START_STOP_LAP_DEFAULT));

        if(key.equals(context.getString(R.string.KEY_START))){
            if(pref.equals(context.getString(R.string.dedicated_button))){
                start_mode = Mode.BTN;
            }
            else if(pref.equals(context.getString(R.string.volume_up))){
                start_mode = Mode.VOL_UP;
            }
            else if(pref.equals(context.getString(R.string.volume_down))){
                start_mode = Mode.VOL_DN;
            }
            else if(pref.equals(context.getString(R.string.clap_sound))){
                start_mode = Mode.CLAP;
            }
            else if(pref.equals(context.getString(R.string.swing_motion))){
                start_mode = Mode.SWING;
            }
            else if(pref.equals(context.getString(R.string.proximity_sensor))){
                start_mode = Mode.PROX;
            }
        }
        // register listeners in case this method is been called by the preferenceChange Listener
        registerSensorsListener();
    }

    private void registerSensorsListener(){
        // register sensors listeners only if there is some feature that needs it selected

        // motion sensor
        if(start_mode == Mode.SWING ||
                stop_mode == Mode.SWING ||
                lap_mode == Mode.SWING)
            sensorManager.registerListener(sensorEventListener,
                    sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                    SensorManager.SENSOR_DELAY_NORMAL);
        // proximity sensor
        if(start_mode == Mode.PROX ||
                stop_mode == Mode.PROX ||
                lap_mode == Mode.PROX)
            sensorManager.registerListener(sensorEventListener,
                    sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY),
                    SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // register listener
        sp.registerOnSharedPreferenceChangeListener(preferenceChangeListener);
        registerSensorsListener();
    }

    @Override
    protected void onStop() {
        super.onStop();
        messageHandler.cleanUp();
        // save preset timer list
        savePresetTimers();
        // unregister sensor listener
        sensorManager.unregisterListener(sensorEventListener);
        // unregister preference change listener
        PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                .unregisterOnSharedPreferenceChangeListener(preferenceChangeListener);
    }

    /**
     * Callback for button1 click
     * @param view
     */
    public void btn1Click(View view) {
        vibe.vibrate(30);
        int page = page_selector.getCheckedRadioButtonId();
        switch(page){
            case R.id.page1:
                // stopwatch - lap
                if(stopwatch_state == StopwatchState.RUNNING){
                    messageHandler.sendEmptyMessage(MessageHandler.MSG_STOPWATCH_LAP);
                }
                break;
            case R.id.timerListAddBtn:
                // fallthrough
            case R.id.page2:
                // timer - set/add
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
                LayoutInflater inflater = this.getLayoutInflater();
                final View dialogView = inflater.inflate(R.layout.set_timer_layout, null);
                dialogBuilder.setView(dialogView);

                // set the max and the min value for number picker or won't show the wheel
                final NumberPicker hours = (NumberPicker) dialogView.findViewById(R.id.numberPicker_hours);
                final NumberPicker min = (NumberPicker) dialogView.findViewById(R.id.numberPicker_min);
                final NumberPicker sec = (NumberPicker) dialogView.findViewById(R.id.numberPicker_sec);
                hours.setMaxValue(99);
                hours.setMinValue(0);
                hours.setWrapSelectorWheel(true);
                min.setMaxValue(59);
                min.setMinValue(0);
                min.setWrapSelectorWheel(true);
                sec.setMaxValue(59);
                sec.setMinValue(0);
                sec.setWrapSelectorWheel(true);

                // Set the values to the current timer
                String current[] = mSectionsPagerAdapter.getTimerTV().getText().toString().split(":");
                hours.setValue(Integer.valueOf(current[0]));
                min.setValue(Integer.valueOf(current[1]));
                sec.setValue(Integer.valueOf(current[2]));

                dialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        if(timerPresetView == null) { //// SET
                            // get the values from the number pickers
                            timer_timeout.h = hours.getValue();
                            timer_timeout.m = min.getValue();
                            timer_timeout.s = sec.getValue();

                            setTimer();

                            // mark the timer as set
                            timer_state = TimerState.SET;
                        }
                        else{ //// ADD
                            Time t  = new Time(hours.getValue(), min.getValue(), sec.getValue(), 0);
                            preset_timers.add(t);
                            presetTimerAdapter.notifyDataSetChanged();
                        }
                        dialog.dismiss();
                    }
                });
                dialogBuilder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                AlertDialog b = dialogBuilder.create();
                b.show();
                break;
            case R.id.page3:
                // TBD
                break;
        }

    }

    /**
     * Callback for button2 click
     * @param view
     */
    public void btn2Click(View view) {
        vibe.vibrate(30);
        int page = page_selector.getCheckedRadioButtonId();

        Button btn = (Button)view;

        switch(page){
            case R.id.page1:
                // stopwatch - lap record
                if(lapRecordView == null){
                    lapRecordView = createSecondaryView(btn, R.layout.lap_list, mSectionsPagerAdapter.getStopwatchTV());

                    // give to the handler the list
                    Message msg = new Message();
                    msg.obj = lapRecordView.findViewById(R.id.lapsList);
                    msg.what = MessageHandler.MSG_STOPWATCH_SHOW_LAP;
                    messageHandler.sendMessage(msg);

                    // set the clickListener for view's buttons
                    Button viewBtn = (Button)lapRecordView.findViewById(R.id.lapTimeBtn);
                    viewBtn.setOnClickListener(secondaryViewBtnListener);
                    viewBtn = (Button)lapRecordView.findViewById(R.id.lapTotalTimeBtn);
                    viewBtn.setOnClickListener(secondaryViewBtnListener);
                }
                else{
                    destroySecondaryView(btn, R.string.btn2_page1_text, lapRecordView);
                }
                break;
            case R.id.page2:
                // timer - presets
                if(timerPresetView == null){
                    timerPresetView = createSecondaryView(btn, R.layout.timer_list, mSectionsPagerAdapter.getTimerTV());

                    // Associate the adapter to the list view
                    ListView list = (ListView) timerPresetView.findViewById(R.id.timerList);
                    list.setAdapter(presetTimerAdapter);

                    // set addPresetTimer button listener
                    Button addPreset = (Button)timerPresetView.findViewById(R.id.timerListAddBtn);
                    addPreset.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            btn1Click(v);
                        }
                    });

                    // Change SET to ADD
                    btn1.setText(R.string.timer_add);
                }
                else{
                    destroySecondaryView(btn, R.string.btn2_page2_text, timerPresetView);
                    // Change ADD to SET
                    btn1.setText(R.string.btn1_page2_text);
                }
                break;
            case R.id.page3:
                // TBD
                break;
        }

    }

    /**
     * Callback for button3 click
     * @param view
     */
    public void btn3Click(View view) {
        vibe.vibrate(30);
        int page = page_selector.getCheckedRadioButtonId();
        switch(page){
            case R.id.page1:
                // stopwatch - reset
                messageHandler.sendEmptyMessage(MessageHandler.MSG_STOPWATCH_RESET);
                stopwatch_state = StopwatchState.STOPPED;
                break;
            case R.id.page2:
                // timer - reset
                messageHandler.sendEmptyMessage(MessageHandler.MSG_TIMER_RESET);
                timer_timeout.h = timer_timeout.m = timer_timeout.s = 0;
                setTimer();
                TextView btn = mSectionsPagerAdapter.getTimerButtonText();
                btn.setText(R.string.central_btn_start);
                setEnableAddTime(true);
                timer_state = TimerState.STOPPED;
                break;
            case R.id.page3:
                // TBD
                break;
        }

    }

    /**
     * Callback for center button
     * @param view
     */
    public void btnCenter(View view) {
        int page = page_selector.getCheckedRadioButtonId();
        switch(page){
            case R.id.page1:
                // stopwatch
                manage_stopwatch(view);
                break;
            case R.id.page2:
                // timer
                manage_timer(view);
                break;
            case R.id.page3:
                // TBD
                break;
        }
    }

    /**
     * Callback for listening volume up/down pressed event
     * @param keyCode
     * @param event
     * @return
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        View btn = null;
        if ((keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)) {
            // select the correct button
            if(start_mode == Mode.VOL_DN || stop_mode == Mode.VOL_DN)
                btn = bigBtn;
            else if(lap_mode == Mode.VOL_DN)
                btn = btn1;
        }
        else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP){
            // select the correct button
            if(start_mode == Mode.VOL_UP || stop_mode == Mode.VOL_UP)
                btn = bigBtn;
            else if(lap_mode == Mode.VOL_UP)
                btn = btn1;
        }
        if(btn != null)
            btn.performClick();
        return true;
    }

        // Provide the correct feedback (sound, vibrate, both or none)
    private void btnCenterFeedback(){
        switch (centerBtnFeedback){
            case VIBRATE:
                vibe.vibrate(50);
                break;
            case SOUND:
                MediaPlayer.create(getApplicationContext(), R.raw.tiny_btn_push).start();
                break;
            case BOTH:
                vibe.vibrate(50);
                MediaPlayer.create(getApplicationContext(), R.raw.tiny_btn_push).start();
                break;
            case NONE: break;
        }
    }

    private void manage_stopwatch(View centralBtn){
        // button animation
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.center_btn_anim);
        centralBtn.startAnimation(animation);
        btnCenterFeedback();

        switch(stopwatch_state){
            case STOPPED:
                messageHandler.initStopwatch(mSectionsPagerAdapter.getStopwatchTV(),
                                                mSectionsPagerAdapter.getStopwatchNeedle(),
                                                mSectionsPagerAdapter.getStopwatchButtonText());
                messageHandler.sendEmptyMessage(MessageHandler.MSG_STOPWATCH_START);
                // disable reset btn
                setEnableBtnReset(false);
                stopwatch_state = StopwatchState.RUNNING;
                break;
            case RUNNING:
                messageHandler.sendEmptyMessage(MessageHandler.MSG_STOPWATCH_PAUSE);
                // enable reset btn
                setEnableBtnReset(true);
                stopwatch_state = StopwatchState.PAUSED;
                break;
            case PAUSED:
                messageHandler.sendEmptyMessage(MessageHandler.MSG_STOPWATCH_RESUME);
                // disable reset btn
                setEnableBtnReset(false);
                stopwatch_state = StopwatchState.RUNNING;
                break;
        }
    }

    private void manage_timer(View centralBtn){
        // start the timer only if it's set
        switch (timer_state){
            case SET:
                messageHandler.initTimer(mSectionsPagerAdapter.getTimerTV(),
                                            mSectionsPagerAdapter.getTimerNeedle(),
                                            mSectionsPagerAdapter.getTimerButtonText(),
                                            centralBtn,
                                            mSectionsPagerAdapter.getCircleFillView());
                // fallthrough
            case PAUSED:
                // send a message with the number of seconds
                Message mex = new Message();
                mex.obj = timer_timeout;
                mex.what = MessageHandler.MSG_TIMER_START;
                messageHandler.sendMessage(mex);

                // disable reset btn
                setEnableBtnReset(false);
                setEnableAddTime(false);
                timer_state = TimerState.RUNNING;
                btnCenterFeedback();
                break;
            case RUNNING:
                // check if the timer is expired
                Animation anim = centralBtn.getAnimation();
                if(anim == null){
                    // no animation running = timer not expired
                    messageHandler.sendEmptyMessage(MessageHandler.MSG_TIMER_PAUSE);

                    // update the internal timeout to the current position, so the user can add time
                    String current[] = mSectionsPagerAdapter.getTimerTV().getText().toString().split(":");
                    timer_timeout.h = Integer.valueOf(current[0]);
                    timer_timeout.m = Integer.valueOf(current[1]);
                    timer_timeout.s = Integer.valueOf(current[2]);

                    timer_state = TimerState.PAUSED;
                }
                else{
                    // stop animation
                    centralBtn.clearAnimation();
                    // reset color
                    centralBtn.setBackgroundTintList(null);
                    // stop sound and vibration, if any
                    messageHandler.sendEmptyMessage(MessageHandler.MSG_TIMER_CLEAR);
                    timer_timeout.h = timer_timeout.m = timer_timeout.s = 0;
                    timer_state = TimerState.STOPPED;
                }
                mSectionsPagerAdapter.getTimerButtonText().setText(R.string.central_btn_start);
                setEnableBtnReset(true);
                setEnableAddTime(true);
                btnCenterFeedback();
                break;
        }
    }

    private View createSecondaryView(Button btn, int mainLayoutResource, TextView clockTV){
        // create and show the view
        separator.setVisibility(View.VISIBLE);
        btn.setText("");
        btn.setBackgroundResource(R.drawable.btn_pressed);

        View list_view = View.inflate(getApplicationContext(), mainLayoutResource, secondary_view);

        // set the y position to the height of the father, so the view'll be out of screen (bottom)
        list_view.setY(secondary_view.getHeight());
        int[] locationOnScreen = {0, 0};
        clockTV.getLocationOnScreen(locationOnScreen);
        int toY = locationOnScreen[1] + (clockTV.getHeight() / 2);

        // set the height of the internal layout = parent's height - y offset - line height - button height
        // I tried to apply these value to list_view, but that view IS secondary_view + inflated layout
        RelativeLayout main_list_layout = (RelativeLayout) list_view.findViewById(R.id.mainListLayout);
        ViewGroup.LayoutParams params = main_list_layout.getLayoutParams();
        params.height = (secondary_view.getHeight() - toY - separator.getHeight() - btn.getHeight());
        main_list_layout.setLayoutParams(params);
        // set background color based on theme
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = getTheme();
        theme.resolveAttribute(R.attr.background_color_blurr, typedValue, true);
        @ColorInt int color = typedValue.data;
        main_list_layout.setBackgroundColor(color);

        // use animate() to make the changes permanent
        list_view.animate().translationY(toY);
        list_view.animate().setDuration(500);
        list_view.animate().start();

        // disable the pager
        mViewPager.setSwipeable(false);

        return list_view;
    }

    private void destroySecondaryView(Button btn, int textResource, View activeView) {
        // hide and destroy the view
        separator.setVisibility(View.INVISIBLE);
        btn.setText(textResource);
        btn.setBackgroundResource(0);
        btn.setBackgroundColor(getResources().getColor(R.color.colorPrimaryLight));

        // Set a listener for the closing animation
        activeView.animate().setListener(secondaryViewAnimatorListener);

        // Set the closing animation
        activeView.animate().translationY(secondary_view.getHeight());
        activeView.animate().setDuration(500);
        activeView.animate().start();

        // Enable the pager
        mViewPager.setSwipeable(true);
    }

        // Method to enable/disable the reset btn easily
    private void setEnableBtnReset(boolean enable){
        btn3.setEnabled(enable);
        if(enable){
            btn3.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        }
        else{
            btn3.setBackgroundColor(getResources().getColor(R.color.greyDark));
        }
    }

    // Enable the add time buttons
    private void setEnableAddTime(boolean enable){
        ImageView minBtn = mSectionsPagerAdapter.getAddMinBtn();
        ImageView secBtn = mSectionsPagerAdapter.getAddSecBtn();
        minBtn.setEnabled(enable);
        secBtn.setEnabled(enable);
    }

    // Method to set the graphics asset (text and needle)
    private void setTimer(){
        // set the text
        TextView timeTV = mSectionsPagerAdapter.getTimerTV();
        timeTV.setText(timer_timeout.getFormattedShortTime());

        // set the needle in the correct position
        ImageView needle = mSectionsPagerAdapter.getTimerNeedle();
        needle.setRotation((float)timer_timeout.s * 6f);

        // hide the filling circle
        CircleFillView circle = mSectionsPagerAdapter.getCircleFillView();
        if(circle.getVisibility() == View.VISIBLE){
            circle.setVisibility(View.INVISIBLE);
            circle.setValue(CircleFillView.MIN_VALUE);
        }
    }

    public void addMin(View view) {
        timer_timeout.m++;
        setTimer();
        // mark the timer as set
        timer_state = TimerState.SET;
    }

    public void addSec(View view) {
        timer_timeout.s++;
        setTimer();
        // mark the timer as set
        timer_state = TimerState.SET;
    }

    /* Save the timers in the shared preferences */
    private void savePresetTimers(){

        /* Preset timers list format:
         * hh:mm:ss:mls, hh:mm:ss:mls, ..
         */

        String list = "";
        // create the string
        if(!preset_timers.isEmpty()){
            list += preset_timers.get(0).getFormattedTime();
            for(int i = 1; i < preset_timers.size(); i++){
                list += ", ";
                list += preset_timers.get(i).getFormattedTime();
            }
        }
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(KEY_PRESET_TIMERS, list);
        editor.apply();
    }

    private void readPresetTimers(){
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String str = sharedPref.getString(KEY_PRESET_TIMERS, "");
        if(!str.isEmpty()){
            String[] timers = str.split(", ");
            for(String t : timers){
                String[] values = t.split(":");
                preset_timers.add(new Time(Integer.valueOf(values[0]),Integer.valueOf(values[1]),
                                           Integer.valueOf(values[2]), Integer.valueOf(values[3])));
            }
        }
    }
}
