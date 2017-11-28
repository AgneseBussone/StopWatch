package watch.stopwatch;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Handler callback to update the UI
 */


public class MessageHandler extends Handler {
    private static final String TAG = MessageHandler.class.getSimpleName();
    private enum TimerExpiredFeedback {SOUND, VIBRATE, BOTH, NONE}

    // Messages available
    public static final int MSG_STOPWATCH_START      = 0;
    public static final int MSG_STOPWATCH_RESET      = 1;
    public static final int MSG_STOPWATCH_UPDATE     = 2;
    public static final int MSG_STOPWATCH_PAUSE      = 3;
    public static final int MSG_STOPWATCH_RESUME     = 4;
    public static final int MSG_STOPWATCH_LAP        = 5;
    public static final int MSG_STOPWATCH_SHOW_LAP   = 7;
    public static final int MSG_STOPWATCH_LAP_FORMAT = 8;
    public static final int MSG_TIMER_START          = 9;
    public static final int MSG_TIMER_UPDATE         = 10;
    public static final int MSG_TIMER_STOP           = 11;
    public static final int MSG_TIMER_PAUSE          = 12;
    public static final int MSG_TIMER_RESET          = 13;
    public static final int MSG_TIMER_CLEAR          = 14;

    private Chronometer stopwatch_chronometer = new Chronometer();
    private final long REFRESH_RATE = 100;
    private Context context;
    private Vibrator vibe;


    // stopwatch graphical asset
    private TextView stopwatch_tv = null;
    private ImageView stopwatch_needle = null;
    private TextView stopwatchBtn_tv = null;

    // time info for stopwatch laps
    private long last_lap;
    private ArrayList<String[]> laps = new ArrayList<>();
    private LapsListAdapter lapsListAdapter;

    // timer graphical asset
    private TextView timer_tv = null;
    private ImageView timer_needle = null;
    private TextView timerBtn_tv = null;
    private View timerBtn = null;
    private Countdown timer = null;
    private CircleFillView circleFillView = null;
    private long total_ms = 0;
    private TimerExpiredFeedback timerExpiredFeedback = TimerExpiredFeedback.NONE;
    private Ringtone alarm_sound;
    private long[] vibe_path = {10, 100, 200, 100, 500}; // off - on - off....


    // Listener for certain preference
    private SharedPreferences.OnSharedPreferenceChangeListener preferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sp, String key) {
            if(key.equals(context.getString(R.string.KEY_SOUND))){
                setTimerExpiredFeedback(sp, key);
            }else if(key.equals(context.getString(R.string.KEY_RINGTONE_URI))){
                String ringtone = sp.getString(key, context.getString(R.string.none));
                alarm_sound = RingtoneManager.getRingtone(context, Uri.parse(ringtone));

            }
        }
    };

    public MessageHandler(Looper looper, Context context){
        super(looper);
        this.context = context;
        readPreferences();
        vibe = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE) ;

    }

    public void cleanUp(){
        // unregister listener
        PreferenceManager.getDefaultSharedPreferences(context)
                .unregisterOnSharedPreferenceChangeListener(preferenceChangeListener);
    }

    private void readPreferences() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);

        // sound timer expired
        setTimerExpiredFeedback(sp, context.getString(R.string.KEY_SOUND));

        // ringtone
        String no_sound = context.getString(R.string.KEY_RINGTONE_URI_DEFAULT);
        String ringtone = sp.getString(context.getString(R.string.KEY_RINGTONE_URI), no_sound);
        // if the user didn't select a ringtone, use the default system alarm sound
        if(ringtone.equals(no_sound)){
            Uri alarmTone = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            alarm_sound = RingtoneManager.getRingtone(context, alarmTone);
        }
        else // user defined sound
            alarm_sound = RingtoneManager.getRingtone(context, Uri.parse(ringtone));

        // register listener
        sp.registerOnSharedPreferenceChangeListener(preferenceChangeListener);
    }

    private void setTimerExpiredFeedback(SharedPreferences sp, String key) {
        String pref = sp.getString(key, context.getString(R.string.none));
        if(pref.equals(context.getString(R.string.vibrate_only)))
            timerExpiredFeedback = TimerExpiredFeedback.VIBRATE;
        else if(pref.equals(context.getString(R.string.sound_only)))
            timerExpiredFeedback = TimerExpiredFeedback.SOUND;
        else if(pref.equals(context.getString(R.string.soundAndVibrate)))
            timerExpiredFeedback = TimerExpiredFeedback.BOTH;
        else //none
            timerExpiredFeedback = TimerExpiredFeedback.NONE;
    }

    void initStopwatch(TextView time_tv, ImageView img, TextView btn_tv){
        if(stopwatch_tv == null)
            stopwatch_tv = time_tv;
        if(stopwatch_needle == null)
            stopwatch_needle = img;
        if(stopwatchBtn_tv ==  null)
            stopwatchBtn_tv = btn_tv;
    }

    void initTimer(TextView time_tv, ImageView img, TextView btn_tv, View btn, CircleFillView circleView){
        if(timer_tv == null)
            timer_tv = time_tv;
        if(timer_needle == null)
            timer_needle = img;
        if(timerBtn_tv ==  null)
            timerBtn_tv = btn_tv;
        if(timerBtn == null)
            timerBtn = btn;
        if(circleFillView == null)
            circleFillView = circleView;
    }

    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case MSG_STOPWATCH_START:
                stopwatch_chronometer.start(); //start chronometer
                if(stopwatchBtn_tv != null) {
                    stopwatchBtn_tv.setText(R.string.central_btn_stop);
                }
                sendEmptyMessage(MSG_STOPWATCH_UPDATE);
                break;

            case MSG_STOPWATCH_UPDATE:
                updateStopwatch();
                sendEmptyMessageDelayed(MSG_STOPWATCH_UPDATE,REFRESH_RATE);
                break;

            case MSG_STOPWATCH_RESET:
                removeMessages(MSG_STOPWATCH_UPDATE); // no more updates.
                stopwatch_chronometer.stop();//stop chronometer
                if(stopwatch_needle != null & stopwatchBtn_tv != null) {
                    stopwatch_tv.setText(R.string.time_default_stopwatch);
                    stopwatch_needle.animate().rotation(0f);
                    stopwatchBtn_tv.setText(R.string.central_btn_start);
                }
                laps.clear();
                if(lapsListAdapter != null) {
                    lapsListAdapter.notifyDataSetChanged();
                    lapsListAdapter = null;
                }
                break;

            case MSG_STOPWATCH_PAUSE:
                removeMessages(MSG_STOPWATCH_UPDATE);
                stopwatch_chronometer.pause();
                updateStopwatch();
                if(stopwatchBtn_tv != null) {
                    stopwatchBtn_tv.setText(R.string.central_btn_start);
                }
                break;

            case MSG_STOPWATCH_RESUME:
                stopwatch_chronometer.resume(); //start chronometer
                if(stopwatchBtn_tv != null) {
                    stopwatchBtn_tv.setText(R.string.central_btn_stop);
                }
                sendEmptyMessage(MSG_STOPWATCH_UPDATE);
                break;

            case MSG_STOPWATCH_LAP:
                // add the time to the list
                long time = stopwatch_chronometer.getElapsedTimeMs();
                Time absolute = new Time(time);
                if(laps.isEmpty()){
                    // first entry: the two format are the same
                    laps.add(new String[]{absolute.getFormattedTime(), absolute.getFormattedTime()});
                }
                else{
                    Time relative = new Time(time - last_lap);
                    laps.add(new String[]{absolute.getFormattedTime(), relative.getFormattedTime()});
                }
                last_lap = time;
                if(lapsListAdapter!= null)
                    lapsListAdapter.notifyDataSetChanged();
                break;

            case MSG_STOPWATCH_SHOW_LAP:
                if(msg.obj != null) {
                    ListView list = (ListView)msg.obj;
                    lapsListAdapter = new LapsListAdapter(context, laps);
                    list.setAdapter(lapsListAdapter);
                }
                break;

            case MSG_STOPWATCH_LAP_FORMAT:
                // get the format information out of the message
                if(lapsListAdapter!= null) {
                    lapsListAdapter.setLapsFormat(LapsListAdapter.LapsFormat.values()[msg.arg1]);
                    // notify the adapter so that it can rebuild the list with the updated format
                    lapsListAdapter.notifyDataSetChanged();
                }
                break;

            case MSG_TIMER_START:
                if(msg.obj != null) {
                    Time timer_timeout = (Time) msg.obj;
                    total_ms = timer_timeout.getMilliseconds();
                    timer = new Countdown(total_ms, REFRESH_RATE, this);
                    if (timerBtn_tv != null && circleFillView != null) {
                        timerBtn_tv.setText(R.string.central_btn_stop);
                        circleFillView.setValue(CircleFillView.MIN_VALUE);
                        circleFillView.setVisibility(View.VISIBLE);
                    }
                    timer.start();
                }
                break;

            case MSG_TIMER_STOP:
                // Message received from the Countdown obj
                if(timerBtn != null && circleFillView != null) {
                    // set the color of the central btn
                    timerBtn.setBackgroundTintList(context.getResources().getColorStateList(R.color.timer_btn_statelist));
                    // hide the filling circle
                    circleFillView.setVisibility(View.INVISIBLE);

                    // play animation on the central btn
                    Animation animation = AnimationUtils.loadAnimation(context, R.anim.center_btn_anim_timer_out);
                    timerBtn.startAnimation(animation);

                    switch (timerExpiredFeedback){
                        case SOUND:
                            if(alarm_sound != null)
                                alarm_sound.play();
                            break;
                        case VIBRATE:
                            vibe.vibrate(vibe_path, 0);
                            break;
                        case BOTH:
                            if(alarm_sound != null)
                                alarm_sound.play();
                            vibe.vibrate(vibe_path, 0);
                            break;
                        case NONE: break;
                    }
                }
                // fallthrough
            case MSG_TIMER_UPDATE:
                if(msg.obj != null) {
                    Time timer_timeout = (Time) msg.obj;
                    updateTimer(timer_timeout);
                }
                break;

            case MSG_TIMER_PAUSE:
                if(timer != null) {
                    timer.cancel();
                    timer = null;
                }
                break;

            case MSG_TIMER_RESET:
                if (timer != null) {
                    timer.cancel();
                    timer = null;
                }
                removeMessages(MSG_TIMER_UPDATE);
                if(circleFillView != null) {
                    circleFillView.setValue(CircleFillView.MIN_VALUE);
                    circleFillView.setVisibility(View.INVISIBLE);
                }
                break;

            case MSG_TIMER_CLEAR:
                switch (timerExpiredFeedback) {
                    case SOUND:
                        if (alarm_sound != null && alarm_sound.isPlaying())
                            alarm_sound.stop();
                        break;
                    case VIBRATE:
                        vibe.vibrate(vibe_path, -1);
                        break;
                    case BOTH:
                        if (alarm_sound != null && alarm_sound.isPlaying())
                            alarm_sound.stop();
                        vibe.vibrate(vibe_path, -1);
                        break;
                    case NONE:
                        break;
                }
                break;

            default:
                break;
        }
    }

    /* Update graphics elements */
    private void updateStopwatch(){
        if(stopwatch_tv != null && stopwatch_needle != null) {
            Time time = stopwatch_chronometer.getElapsedTime();
            stopwatch_tv.setText(time.getFormattedTime());
            stopwatch_needle.setRotation(((float) time.s + (time.ms / 1000f)) * 6f);
        }
    }

    private void updateTimer(Time timer_timeout){
        if(timer_tv != null && timer_needle != null && circleFillView != null){
            timer_tv.setText(timer_timeout.getFormattedShortTime());
            timer_needle.setRotation(((float) timer_timeout.s + (timer_timeout.ms / 1000f)) * 6f);
            if(total_ms > 0) {
                int fill_value = CircleFillView.MAX_VALUE - (int) ((CircleFillView.MAX_VALUE * timer_timeout.getMilliseconds()) / total_ms);
                circleFillView.setValue(fill_value);
            }
            else{
                circleFillView.setValue(0);
            }
        }
    }

    public String getLaps(){
        if(lapsListAdapter != null){
            return lapsListAdapter.getLapString();
        }
        return "";
    }
}
