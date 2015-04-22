package martin.app.bitunion.widget;

import android.graphics.Canvas;
import android.graphics.ColorFilter;

import com.bumptech.glide.load.resource.drawable.GlideDrawable;

public class UrlDrawable extends GlideDrawable {

    public GlideDrawable drawable;

    public UrlDrawable() {
        super();
    }

    @Override
    public boolean isAnimated() {
        if (drawable != null) {
            return drawable.isAnimated();
        }
        return false;
    }

    @Override
    public void setLoopCount(int i) {
        if (drawable != null) {
            drawable.setLoopCount(i);
        }
    }

    @Override
    public void start() {
        if (drawable != null) {
            drawable.start();
        }
    }

    @Override
    public void stop() {
        if (drawable != null) {
            drawable.stop();
        }
    }

    @Override
    public boolean isRunning() {
        if (drawable != null) {
            return drawable.isRunning();
        }
        return false;
    }

    @Override
    public void setAlpha(int alpha) {
        if (drawable != null) {
            drawable.setAlpha(alpha);
        }
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        if (drawable != null) {
            drawable.setColorFilter(cf);
        }
    }

    @Override
    public int getOpacity() {
        if (drawable != null) {
            return drawable.getOpacity();
        }
        return 0;
    }

    @Override
    public void draw(Canvas canvas) {
        if (drawable != null) {
            drawable.draw(canvas);
            drawable.start();
        }
    }
}
