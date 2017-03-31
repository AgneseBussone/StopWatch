package watch.stopwatch;

import android.animation.Animator;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Message;
import android.os.Vibrator;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;


public class MainActivity extends AppCompatActivity {

    private enum StopwatchState {RUNNING, STOPPED, PAUSED}
    private enum TimerState {RUNNING, STOPPED, PAUSED, SET}

    private static final String TAG = MainActivity.class.getSimpleName();

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
    ViewPager.OnPageChangeListener pageListener;
    private RadioGroup page_selector;
    private Button btn1;
    private Button btn2;
    private Button btn3;
    private ImageView settingsBtn;
    private Vibrator vibe;
    private MessageHandler messageHandler;
    private StopwatchState stopwatch_state = StopwatchState.STOPPED;
    private TimerState timer_state = TimerState.STOPPED;
    private View lapRecordView = null;
    private Time timer_timeout = new Time(0, 0, 0, 0);
    private View timerPresetView = null;
    private RelativeLayout secondary_view;
    private ImageView separator;


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
                case R.id.timerAddBtn:
                    //TODO
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
        settingsBtn = (ImageView)findViewById(R.id.button_settings);

        secondary_view = (RelativeLayout)findViewById(R.id.secondary_view);
        separator = (ImageView)findViewById(R.id.buttonsLine);


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
                // timer - set
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

                dialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // get the values from the number pickers
                        timer_timeout.h = hours.getValue();
                        timer_timeout.m = min.getValue();
                        timer_timeout.s = sec.getValue();

                        setTimer();

                        // mark the timer as set
                        timer_state = TimerState.SET;

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
                    lapRecordView = createSecondaryView(btn, R.layout.lap_list);

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
                    timerPresetView = createSecondaryView(btn, R.layout.timer_list);

                    // set the clickListener for view's buttons
                    Button viewBtn = (Button)timerPresetView.findViewById(R.id.timerAddBtn);
                    viewBtn.setOnClickListener(secondaryViewBtnListener);
                }
                else{
                    destroySecondaryView(btn, R.string.btn2_page2_text, timerPresetView);
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
                animateBtnCenter(centralBtn);
                messageHandler.initTimer(mSectionsPagerAdapter.getTimerTV(),
                                            mSectionsPagerAdapter.getTimerNeedle(),
                                            mSectionsPagerAdapter.getTimerButtonText(),
                                            centralBtn);
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
                    timer_state = TimerState.PAUSED;
                }
                else{
                    // stop animation
                    centralBtn.clearAnimation();
                    timer_timeout.h = timer_timeout.m = timer_timeout.s = 0;
                    setEnableAddTime(true); //TODO: correct?
                    timer_state = TimerState.STOPPED;
                }
                mSectionsPagerAdapter.getTimerButtonText().setText(R.string.central_btn_start);
                setEnableBtnReset(true);
                break;
            case PAUSED:
                animateBtnCenter(centralBtn);
                messageHandler.sendEmptyMessage(MessageHandler.MSG_TIMER_RESUME);
                // disable reset btn
                setEnableBtnReset(false);
                timer_state = TimerState.RUNNING;
                break;
        }
    }

    private View createSecondaryView(Button btn, int mainLayoutResource){
        // create and show the view
        separator.setVisibility(View.VISIBLE);
        btn.setText("");
        btn.setBackgroundResource(R.drawable.btn_pressed);

        View list_view = View.inflate(getApplicationContext(), mainLayoutResource, secondary_view);

        // set the y position to the height of the father, so the view'll be out of screen (bottom)
        list_view.setY(secondary_view.getHeight());
        float toY = settingsBtn.getY() + settingsBtn.getHeight() + 3;

        // set the height of the internal layout = parent's height - y offset - line height - button height
        // I tried to apply these value to list_view, but that view IS secondary_view + inflated layout
        RelativeLayout main_list_layout = (RelativeLayout) list_view.findViewById(R.id.mainListLayout);
        ViewGroup.LayoutParams params = main_list_layout.getLayoutParams();
        params.height = (secondary_view.getHeight() - (int)toY - separator.getHeight() - btn.getHeight());
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

    public void editPresetTimer(View view) {
    }

    public void deletePresetTimer(View view) {
    }

}
