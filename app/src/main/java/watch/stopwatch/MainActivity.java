package watch.stopwatch;

import android.content.Context;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.RadioGroup;


public class MainActivity extends AppCompatActivity {
    private enum StopwatchState {RUNNING, STOPPED, PAUSED}

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
    private ViewPager mViewPager;
    ViewPager.OnPageChangeListener pageListener;
    private RadioGroup page_selector;
    private Button btn1;
    private Button btn2;
    private Vibrator vibe;
    private MessageHandler messageHandler;
    StopwatchState stopwatch_state = StopwatchState.STOPPED;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        btn1 = (Button)findViewById(R.id.button1);
        btn2 = (Button)findViewById(R.id.button2);




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
        messageHandler = new MessageHandler(getMainLooper());
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
                // stopwatch
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
                // stopwatch
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
                messageHandler.sendEmptyMessage(MessageHandler.MSG_STOPWATCH_STOP);
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
                stopwatch_state = StopwatchState.RUNNING;
                break;
            case RUNNING:
                messageHandler.sendEmptyMessage(MessageHandler.MSG_STOPWATCH_PAUSE);
                stopwatch_state = StopwatchState.PAUSED;
                break;
            case PAUSED:
                messageHandler.sendEmptyMessage(MessageHandler.MSG_STOPWATCH_RESUME);
                stopwatch_state = StopwatchState.RUNNING;
                break;
        }
    }
}
