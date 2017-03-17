package watch.stopwatch;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Handler callback for update the UI
 */

public class MessageHandler extends Handler {

    // Messages available
    public static final int MSG_STOPWATCH_START     = 0;
    public static final int MSG_STOPWATCH_STOP      = 1;
    public static final int MSG_STOPWATCH_UPDATE    = 2;
    public static final int MSG_STOPWATCH_PAUSE     = 3;
    public static final int MSG_STOPWATCH_RESUME    = 4;
    public static final int MSG_STOPWATCH_LAP       = 5;
    public static final int MSG_STOPWATCH_SAVE_LAP  = 6;

    private Chronometer timer = new Chronometer();
    private final long REFRESH_RATE = 100;

    // stopwatch graphical asset
    private TextView stopwatch_tv = null;
    private ImageView stopwatch_needle = null;
    private TextView stopwatchBtn_tv = null;

    // time info for stopwatch laps
    private long stopwatch_start;
    private ArrayList<Long> laps;

    public MessageHandler(Looper looper){
        super(looper);
    }

    void initStopwatch(TextView time_tv, ImageView img, TextView btn_tv){
        if(stopwatch_tv == null)
            stopwatch_tv = time_tv;
        if(stopwatch_needle == null)
            stopwatch_needle = img;
        if(stopwatchBtn_tv ==  null)
            stopwatchBtn_tv = btn_tv;
    }

    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case MSG_STOPWATCH_START:
                stopwatch_start = timer.start(); //start timer
                if(stopwatchBtn_tv != null) {
                    stopwatchBtn_tv.setText(R.string.central_btn_stop);
                }
                sendEmptyMessage(MSG_STOPWATCH_UPDATE);
                break;

            case MSG_STOPWATCH_UPDATE:
                updateStopwatch();
                sendEmptyMessageDelayed(MSG_STOPWATCH_UPDATE,REFRESH_RATE);
                break;

            case MSG_STOPWATCH_STOP:
                removeMessages(MSG_STOPWATCH_UPDATE); // no more updates.
                timer.stop();//stop timer
                if(stopwatch_needle != null & stopwatchBtn_tv != null) {
                    stopwatch_tv.setText(R.string.time_default);
                    stopwatch_needle.setRotation(0f);
                    stopwatchBtn_tv.setText(R.string.central_btn_start);
                }
                break;

            case MSG_STOPWATCH_PAUSE:
                removeMessages(MSG_STOPWATCH_UPDATE);
                timer.pause();
                updateStopwatch();
                if(stopwatchBtn_tv != null) {
                    stopwatchBtn_tv.setText(R.string.central_btn_start);
                }
                break;

            case MSG_STOPWATCH_RESUME:
                timer.resume(); //start timer
                if(stopwatchBtn_tv != null) {
                    stopwatchBtn_tv.setText(R.string.central_btn_stop);
                }
                sendEmptyMessage(MSG_STOPWATCH_UPDATE);
                break;

            case MSG_STOPWATCH_LAP:
                // add the time to the list
                laps.add(timer.getElapsedTime());
                break;

            case MSG_STOPWATCH_SAVE_LAP:
                // save the laps into shared preferences
                //TODO: call proper method of settings activity
                break;

            default:
                break;
        }
    }

    /* Update graphics elements */
    private void updateStopwatch(){
        if(stopwatch_tv != null && stopwatch_needle != null) {
            Time time = timer.getTime();
            stopwatch_tv.setText(time.getFormattedTime());
            stopwatch_needle.setRotation(((float) time.s + (time.ms / 1000f)) * 6f);
        }
    }
}
