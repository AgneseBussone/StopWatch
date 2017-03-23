package watch.stopwatch;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Handler callback for update the UI
 */

public class MessageHandler extends Handler {

    // Messages available
    public static final int MSG_STOPWATCH_START      = 0;
    public static final int MSG_STOPWATCH_RESET      = 1;
    public static final int MSG_STOPWATCH_UPDATE     = 2;
    public static final int MSG_STOPWATCH_PAUSE      = 3;
    public static final int MSG_STOPWATCH_RESUME     = 4;
    public static final int MSG_STOPWATCH_LAP        = 5;
    public static final int MSG_STOPWATCH_SAVE_LAP   = 6;
    public static final int MSG_STOPWATCH_SHOW_LAP   = 7;
    public static final int MSG_STOPWATCH_LAP_FORMAT = 8;

    private Chronometer timer = new Chronometer();
    private final long REFRESH_RATE = 100;
    private Context context;

    // stopwatch graphical asset
    private TextView stopwatch_tv = null;
    private ImageView stopwatch_needle = null;
    private TextView stopwatchBtn_tv = null;

    // time info for stopwatch laps
    private long last_lap;
    private ArrayList<String[]> laps = new ArrayList<>();
    private LapsListAdapter lapsListAdapter;

    public MessageHandler(Looper looper, Context context){
        super(looper);
        this.context = context;
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
                timer.start(); //start timer
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
                timer.stop();//stop timer
                if(stopwatch_needle != null & stopwatchBtn_tv != null) {
                    stopwatch_tv.setText(R.string.time_default);
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
                long time = timer.getElapsedTime();
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
