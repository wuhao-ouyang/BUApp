package martin.app.bitunion.widget;

import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class SwipeDetector implements View.OnTouchListener {
    public static final int SWIPE_LEFT = 1;
    public static final int SWIPE_RIGHT = 2;

    private final int trigger;
    private final SwipeListener swipeListener;

    private float startX;
    private boolean isTriggered = false;

    public interface SwipeListener {
        void onSwiped(int swipeAction);
    }

    public SwipeDetector(int triggerLimit, SwipeListener l) {
        trigger = triggerLimit;
        swipeListener = l;

        startX = -1;
    }

    @Override
    public boolean onTouch(View v, MotionEvent motion) {
        switch (motion.getAction()) {
            case MotionEvent.ACTION_MOVE:
                float pxMoved = 0;
                if (startX != -1)
                    pxMoved = motion.getX() - startX;
                startX = motion.getX();
                if (pxMoved > trigger)
                    isTriggered = true;
                break;
            case MotionEvent.ACTION_UP:
                startX = -1;
                if (isTriggered) {
                    if (swipeListener != null)
                        swipeListener.onSwiped(SWIPE_RIGHT);
                    isTriggered = false;
                    Log.i("SwipeDetector", "swipe >> " + SWIPE_RIGHT);
                }
                break;
            default:
        }
        return false;
    }
}
