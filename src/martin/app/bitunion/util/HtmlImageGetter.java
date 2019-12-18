package martin.app.bitunion.util;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.bumptech.glide.Glide;

import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.graphics.drawable.DrawableWrapper;
import martin.app.bitunion.R;

public class HtmlImageGetter implements Html.ImageGetter {
    public static final int VOLLEY = 0;
    public static final int GLIDE = 1;

    private int mode;
    private Context mContext;
    private TextView mContainer;

    public HtmlImageGetter(Context c, TextView view) {
        this(c, view, VOLLEY);
    }

    public HtmlImageGetter(Context c, TextView view, int mode) {
        mContext = c;
        mContainer = view;
        this.mode = mode;
    }

    @Override
    public Drawable getDrawable(String imgUrl) {
        imgUrl = BUApi.getImageAbsoluteUrl(imgUrl);
        if (mode == GLIDE) {
            DrawableWrapper drawableWrapper = new DrawableWrapper(null);
            Glide.with(mContext).load(imgUrl).into(new GlideImageListener(mContainer, drawableWrapper));
            return drawableWrapper;
        } else {
            UrlImageDownloader urlDrawable = new UrlImageDownloader(mContext.getResources(), imgUrl);
            urlDrawable.drawable = mContext.getResources().getDrawable(R.drawable.ic_image_white_48dp);
            VolleyImageLoaderFactory.getImageLoader(mContext).get(imgUrl, new VolleyImageListener(mContainer, urlDrawable));
            return urlDrawable;
        }
    }

    private static class GlideImageListener extends SimpleTarget<Drawable> {
        private DrawableWrapper drawableWrapper;
        private TextView container;

        private GlideImageListener(TextView textView, DrawableWrapper drawable) {
            drawableWrapper = drawable;
            container = textView;
        }

        @Override
        public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
            int width = resource.getIntrinsicWidth();
            int height = resource.getIntrinsicHeight();

            int newWidth = width;
            int newHeight = height;

            if (width > container.getWidth()) {
                newWidth = container.getWidth();
                newHeight = (newWidth * height) / width;
            }

            resource.setBounds(0, 0, newWidth, newHeight);
            drawableWrapper.setBounds(0, 0, newWidth, newHeight);
            drawableWrapper.setWrappedDrawable(resource);

            container.invalidate();
        }
    }

    private static class VolleyImageListener implements ImageLoader.ImageListener {
        private UrlImageDownloader urlImageDownloader;
        private TextView container;

        private VolleyImageListener(TextView textView, UrlImageDownloader drawable) {
            urlImageDownloader = drawable;
            container = textView;
        }

        @Override
        public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
            if (response == null || response.getBitmap() == null)
                return;
            Bitmap loadedImage = response.getBitmap();
            int width = loadedImage.getWidth();
            int height = loadedImage.getHeight();

            int newWidth = width;
            int newHeight = height;

            if (width > container.getWidth()) {
                newWidth = container.getWidth();
                newHeight = (newWidth * height) / width;
            }

            Drawable result = new BitmapDrawable(container.getResources(), loadedImage);
            result.setBounds(0, 0, newWidth, newHeight);

            urlImageDownloader.setBounds(0, 0, newWidth, newHeight);
            urlImageDownloader.drawable = result;

            container.invalidate();
        }

        @Override
        public void onErrorResponse(VolleyError error) {

        }
    }

    private static class UrlImageDownloader extends BitmapDrawable {
        public Drawable drawable;

        /**
         * Create a drawable by opening a given file path and decoding the bitmap.
         *
         * @param res
         * @param filepath
         */
        public UrlImageDownloader(Resources res, String filepath) {
            super(res, filepath);
            drawable = new BitmapDrawable(res, filepath);
        }

        @Override
        public void draw(Canvas canvas) {
            // override the draw to facilitate refresh function later
            if (drawable != null) {
                drawable.draw(canvas);
            }
        }
    }
}
