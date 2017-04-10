package watch.stopwatch;

import android.animation.Animator;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Message;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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

/*
* TODO list:
* - third screen: settings
* - preset timer screen: add "add" btn (like laps screen)
* */


public class MainActivity extends AppCompatActivity {

    private enum StopwatchState {RUNNING, STOPPED, PAUSED}
    private enum TimerState {RUNNING, STOPPED, PAUSED, SET}

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


    // Listener for buttons in secondary views
    private View.OnClickListener secondaryViewBtnListener = new View.OnClickListener() {
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
    private Animator.AnimatorListener secondaryViewAnimatorListener = new Animator.AnimatorListener() {
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
    private View.OnClickListener timerPresetActionClickListener = new View.OnClickListener() {
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
    private View.OnLongClickListener longClickListener = new View.OnLongClickListener() {
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
            }
        });



        // Other elements
        secondary_view = (RelativeLayout)findViewById(R.id.secondary_view);
        separator = (ImageView)findViewById(R.id.buttonsLine);

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
                        if(stopwatch_state == StopwatchState.RUNNING){
                            // disable reset btn
                            setEnableBtnReset(false);
                        }
                        else{
                            // enable reset btn
                            setEnableBtnReset(true);
                        }
                        break;
                    case 1:
                        // timer
                        page_selector.check(R.id.page2);
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
                        break;
                    case 2:
                        // TBD
                        page_selector.check(R.id.page3);
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) { }
        };

        mViewPager.addOnPageChangeListener(pageListener);

        vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE) ;

        // message handler setup
        messageHandler = new MessageHandler(getMainLooper(), getApplicationContext());
    }

    @Override
    protected void onStop() {
        super.onStop();
        messageHandler.sendEmptyMessage(MessageHandler.MSG_STOPWATCH_SAVE_LAP);
        // save preset timer list
        savePresetTimers();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
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

    private void animateBtnCenter(View btnView){
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.center_btn_anim);
        btnView.startAnimation(animation);
        vibe.vibrate(50);
    }

    private void manage_stopwatch(View centralBtn){
        animateBtnCenter(centralBtn);
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
                    timer_timeout.h = timer_timeout.m = timer_timeout.s = 0;
                    timer_state = TimerState.STOPPED;
                }
                mSectionsPagerAdapter.getTimerButtonText().setText(R.string.central_btn_start);
                setEnableBtnReset(true);
                setEnableAddTime(true);
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
