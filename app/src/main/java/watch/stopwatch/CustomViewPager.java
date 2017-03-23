package watch.stopwatch;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Class that extends the ViewPager class in order to disable swiping action when a secondary
 * view (es. laps list) is displayed.
 */

public class CustomViewPager extends ViewPager {

    private boolean swipeable = true;

    public CustomViewPager(Context context) {
        super(context);
    }

    public CustomViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (swipeable) {
            return super.onTouchEvent(event);
        }
        return false; // swipe disabled
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (swipeable) {
            return super.onInterceptTouchEvent(event);
        }
        return false; // swipe disabled
    }

    public void setSwipeable(boolean swipeable) {
        this.swipeable = swipeable;
    }
}
