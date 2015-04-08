package martin.app.bitunion.util;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.text.Html;
import android.util.Log;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

import martin.app.bitunion.BUApplication;
import martin.app.bitunion.R;

public class HtmlImageGetter implements Html.ImageGetter {

    private static HashMap<String, Drawable> imgCache = new HashMap<String, Drawable>();
    private TextView htmlTextView;
    private Drawable defaultDrawable;
    private Context mContext;

    public HtmlImageGetter(Context c, TextView view) {
        mContext = c;
        htmlTextView = view;
        defaultDrawable = mContext.getResources().getDrawable(
                R.drawable.ic_action_picture);
    }

    @Override
    public Drawable getDrawable(String imgUrl) {
        // Get MD5 of imgUrl
        String imgKey = null;
        try {
            imgKey = BUAppUtils.hashImgUrl(imgUrl);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            Log.e("HtmlImageGetter", "Hash error>>" + imgUrl);
        }

        // Check if image is in cache
        if (imgCache.get(imgKey) != null)
            return imgCache.get(imgKey);

        imgUrl = BUApiHelper.getImageAbsoluteUrl(imgUrl);
        Log.v("ImageGetter", "img not cached");

        Log.v("ImageGetter", "img Url>>" + imgUrl);
        URLDrawable urlDrawable = new URLDrawable(defaultDrawable);
        new AsyncThread(urlDrawable).execute(imgKey, imgUrl);
        return urlDrawable;
    }

    private class AsyncThread extends AsyncTask<String, Integer, Drawable> {
        private String imgKey;
        private URLDrawable drawable;

        public AsyncThread(URLDrawable drawable) {
            this.drawable = drawable;
        }

        @Override
        protected Drawable doInBackground(String... strings) {
            imgKey = strings[0];
            InputStream inps = null;
            try {
                inps = BUAppUtils.getImageVewInputStream(strings[1]);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
            if (inps == null)
                return null;
            Drawable drawable = Drawable.createFromStream(inps, imgKey);
            return drawable;
        }

        @Override
        protected void onPostExecute(Drawable result) {
            if (result == null)
                return;
            imgCache.put(imgKey, drawable);
            drawable.setDrawable(result);
            htmlTextView.setText(htmlTextView.getText());
        }
    }

    public class URLDrawable extends BitmapDrawable {

        private Drawable drawable;

        @SuppressWarnings("deprecation")
        public URLDrawable(Drawable defaultDraw) {
            setDrawable(defaultDraw);
        }

        private void setDrawable(Drawable ndrawable) {
            drawable = ndrawable;
//				float dpi = MainActivity.PIXDENSITY;
            float scalingFactor = (float) htmlTextView.getMeasuredWidth()
                    / drawable.getIntrinsicWidth();
            if (drawable.getIntrinsicWidth() < 100) {
                drawable.setBounds(0, 0, drawable.getIntrinsicWidth(),
                        drawable.getIntrinsicHeight());
                setBounds(0, 0, drawable.getIntrinsicWidth(),
                        drawable.getIntrinsicHeight());
                return;
            }
            drawable.setBounds(0, 0, htmlTextView.getMeasuredWidth(),
                    (int) (drawable.getIntrinsicHeight() * scalingFactor));
            setBounds(0, 0, htmlTextView.getMeasuredWidth(),
                    (int) (drawable.getIntrinsicHeight() * scalingFactor));
        }

        @Override
        public void draw(Canvas canvas) {
            drawable.draw(canvas);
        }
    }
}
