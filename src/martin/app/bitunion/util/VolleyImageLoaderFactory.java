package martin.app.bitunion.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.LruCache;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.ImageLoader.ImageCache;
import com.android.volley.toolbox.Volley;

public class VolleyImageLoaderFactory {

    private static int DEFAULT_IMAGE_CACHE_SIZE = 10 * 1024 * 1024;

    private static RequestQueue sImageQueue;
    private static ImageCache sImageCache;
    private static ImageLoader sImageLoader;

    public static ImageLoader getImageLoader(Context context) {
        if (sImageLoader == null) {
            sImageQueue = Volley.newRequestQueue(context);
            sImageCache = new DefaultImageCache(DEFAULT_IMAGE_CACHE_SIZE);
            sImageLoader = new ImageLoader(sImageQueue, sImageCache);
        }
        return sImageLoader;
    }

    public static class DefaultImageCache extends LruCache<String, Bitmap> implements ImageCache {

        /**
         * @param maxSize for caches that do not override {@link #sizeOf}, this is
         *                the maximum number of entries in the cache. For all other caches,
         *                this is the maximum sum of the sizes of the entries in this cache.
         */
        public DefaultImageCache(int maxSize) {
            super(maxSize);
        }

        @Override
        protected int sizeOf(String key, Bitmap value) {
            return value.getByteCount();
        }

        @Override
        public Bitmap getBitmap(String url) {
            return get(url);
        }

        @Override
        public void putBitmap(String url, Bitmap bitmap) {
            put(url, bitmap);
        }
    }
}
