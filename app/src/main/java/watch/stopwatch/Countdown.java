package watch.stopwatch;

import android.os.CountDownTimer;
import android.os.Message;

/**
 * Class used for the timer.
 */

public class Countdown extends CountDownTimer {
    MessageHandler mHandler;

    public Countdown(long millisInFuture, long countDownInterval, MessageHandler messenger) {
        super(millisInFuture, countDownInterval);
        mHandler = messenger;
    }

    @Override
    public void onTick(long millisUntilFinished) {
        Time remaining_time = new Time(millisUntilFinished);
        Message mex = new Message();
        mex.obj = remaining_time;
        mex.what = MessageHandler.MSG_TIMER_UPDATE;
        mHandler.sendMessage(mex);
    }

    @Override
    public void onFinish() {
        Time remaining_time = new Time(0);
        Message mex = new Message();
        mex.obj = remaining_time;
        mex.what = MessageHandler.MSG_TIMER_STOP;
        mHandler.sendMessage(mex);
    }
}
