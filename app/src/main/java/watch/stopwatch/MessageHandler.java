package watch.stopwatch;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

/**
 * Handler callback for update the UI
 */

public class MessageHandler extends Handler {

    // Messages available
    public static final int MSG_STOPWATCH_START = 0;
    public static final int MSG_STOPWATCH_STOP = 1;
    public static final int MSG_STOPWATCH_UPDATE = 2;

    private Stopwatch timer = new Stopwatch();
    private final long REFRESH_RATE = 100;

    private SectionsPagerAdapter pagerAdapter;

    public MessageHandler(SectionsPagerAdapter adapter, Looper looper){
        super(looper);
        pagerAdapter = adapter;
    }

    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case MSG_STOPWATCH_START:
                timer.start(); //start timer
                pagerAdapter.updateStopwatchButtonText(R.string.central_btn_stop);
                sendEmptyMessage(MSG_STOPWATCH_UPDATE);
                break;

            case MSG_STOPWATCH_UPDATE:
                pagerAdapter.updateStopwatch(timer.getFormattedElapsedTime(), 5);
                sendEmptyMessageDelayed(MSG_STOPWATCH_UPDATE,REFRESH_RATE);
                break;
            case MSG_STOPWATCH_STOP:
                removeMessages(MSG_STOPWATCH_UPDATE); // no more updates.
                timer.stop();//stop timer
                pagerAdapter.updateStopwatch(timer.getFormattedElapsedTime(), 0);
                pagerAdapter.updateStopwatchButtonText(R.string.central_btn_start);
                break;

            default:
                break;
        }
    }
}
