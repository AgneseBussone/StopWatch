package watch.stopwatch;

import android.animation.Animator;
import android.content.Context;
import android.os.Bundle;
import android.os.Message;
import android.os.Vibrator;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;


public class MainActivity extends AppCompatActivity {

    private enum StopwatchState {RUNNING, STOPPED, PAUSED}

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
    StopwatchState stopwatch_state = StopwatchState.STOPPED;
    private View lapRecordView = null;

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
                    }
                    break;
                case R.id.lapTimeBtn: {
                    v.setBackgroundColor(getResources().getColor(R.color.colorPrimaryLight));
                    Button btn = (Button) findViewById(R.id.lapTotalTimeBtn);
                    btn.setBackgroundColor(getResources().getColor(R.color.greyLight));
                    }
                    break;
            }
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
        settingsBtn = (ImageView)findViewById(R.id.button_settings);


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
                // when swipe to the next page, update the radiobutton
                // and button text
                switch(position){
                    case 0:
                        page_selector.check(R.id.page1);
                        btn1.setText(R.string.btn1_page1_text);
                        btn2.setText(R.string.btn2_page1_text);
                        break;
                    case 1:
                        page_selector.check(R.id.page2);
                        btn1.setText(R.string.btn1_page2_text);
                        btn2.setText(R.string.btn2_page2_text);
                        break;
                    case 2:
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
                // timer
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
        switch(page){
            case R.id.page1:
                // stopwatch - lap record
                final RelativeLayout secondary_view = (RelativeLayout)findViewById(R.id.secondary_view);
                ImageView buttonsLine = (ImageView)findViewById(R.id.buttonsLine);
                Button btn = (Button)view;

                if(lapRecordView == null){
                    // create and show the view
                    buttonsLine.setVisibility(View.VISIBLE);
                    btn.setText("");
                    btn.setBackgroundResource(R.drawable.btn_pressed);
                    View laps_view = View.inflate(getApplicationContext(), R.layout.lap_list, secondary_view);

                    // set the y position to the height of the father, so the view'll be out of screen (bottom)
                    laps_view.setY(secondary_view.getHeight());
                    float toY = settingsBtn.getY() + settingsBtn.getHeight() + 3;

                    // set the height of the internal layout = parent's height - y offset - line height - button height
                    // I tried to apply these value to laps_view, but that view IS secondary_view + inflated layout
                    RelativeLayout lap_list_layout = (RelativeLayout) laps_view.findViewById(R.id.lapListLayout);
                    ViewGroup.LayoutParams params = lap_list_layout.getLayoutParams();
                    params.height = (secondary_view.getHeight() - (int)toY - buttonsLine.getHeight() - btn.getHeight());
                    lap_list_layout.setLayoutParams(params);

                    // use animate() to make the changes permanent
                    laps_view.animate().translationY(toY);
                    laps_view.animate().setDuration(500);
                    laps_view.animate().start();
                    lapRecordView = laps_view;

                    // give to the handler the list
                    Message msg = new Message();
                    msg.obj = findViewById(R.id.lapsList);
                    msg.what = MessageHandler.MSG_STOPWATCH_SHOW_LAP;
                    messageHandler.sendMessage(msg);

                    // set the clickListener for view's buttons
                    Button viewBtn = (Button)laps_view.findViewById(R.id.lapTimeBtn);
                    viewBtn.setOnClickListener(secondaryViewBtnListener);
                    viewBtn = (Button)laps_view.findViewById(R.id.lapTotalTimeBtn);
                    viewBtn.setOnClickListener(secondaryViewBtnListener);

                    // disable the pager
                    mViewPager.setSwipeable(false);
                }
                else{
                    // hide and destroy the view
                    buttonsLine.setVisibility(View.INVISIBLE);
                    btn.setText(R.string.btn2_page1_text);
                    btn.setBackgroundResource(0);
                    btn.setBackgroundColor(getResources().getColor(R.color.colorPrimaryLight));
                    lapRecordView.animate().setListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animation) {}

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            secondary_view.removeAllViews();
                            lapRecordView.animate().setListener(null);
                            lapRecordView = null;
                        }

                        @Override
                        public void onAnimationCancel(Animator animation) {}

                        @Override
                        public void onAnimationRepeat(Animator animation) {}
                    });
                    lapRecordView.animate().translationY(secondary_view.getHeight());
                    lapRecordView.animate().setDuration(500);
                    lapRecordView.animate().start();
                    mViewPager.setSwipeable(true);
                }
                break;
            case R.id.page2:
                // timer
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
                // timer
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
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.center_btn_anim);
        view.startAnimation(animation);
        startStopwatch();
    }

    private void startStopwatch(){
        vibe.vibrate(50);
        switch(stopwatch_state){
            case STOPPED:
                messageHandler.initStopwatch(mSectionsPagerAdapter.getStopwatchTV(),
                    mSectionsPagerAdapter.getStopwatchNeedle(),
                    mSectionsPagerAdapter.getStopwatchButtonText());
                messageHandler.sendEmptyMessage(MessageHandler.MSG_STOPWATCH_START);
                // disable reset btn
                btn3.setEnabled(false);
                btn3.setBackgroundColor(getResources().getColor(R.color.greyDark));
                stopwatch_state = StopwatchState.RUNNING;
                break;
            case RUNNING:
                messageHandler.sendEmptyMessage(MessageHandler.MSG_STOPWATCH_PAUSE);
                // enable reset btn
                btn3.setEnabled(true);
                btn3.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                stopwatch_state = StopwatchState.PAUSED;
                break;
            case PAUSED:
                messageHandler.sendEmptyMessage(MessageHandler.MSG_STOPWATCH_RESUME);
                // enable reset btn
                btn3.setEnabled(true);
                btn3.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                stopwatch_state = StopwatchState.RUNNING;
                break;
        }
    }
}
