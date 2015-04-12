package martin.app.bitunion.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.LruCache;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.ImageLoader.ImageCache;
import com.android.volley.toolbox.Volley;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class VolleyImageLoaderFactory {

    private static int DEFAULT_IMAGE_CACHE_SIZE = 10 * 1024 * 1024;

    private static Context sContext;
    private static RequestQueue sImageQueue;
    private static ImageCache sImageCache;
    private static ImageLoader sImageLoader;

    public static ImageLoader getImageLoader(Context context) {
        sContext = context;
        if (sImageLoader == null) {
            sImageQueue = Volley.newRequestQueue(context);
            sImageCache = new DefaultImageCache(DEFAULT_IMAGE_CACHE_SIZE);
            sImageLoader = new ImageLoader(sImageQueue, sImageCache);
        }
        return sImageLoader;
    }

    static void flush() {
        if (sContext == null)
            return;
        sImageQueue = Volley.newRequestQueue(sContext, new HurlStack() {
            @Override
            protected HttpURLConnection createConnection(URL url) throws IOException {
                HttpURLConnection connection = super.createConnection(url);
                connection.setRequestProperty("Cookie", BUApi.getSessionCookie());
                return connection;
            }
        });
        sImageLoader = new ImageLoader(sImageQueue, sImageCache);
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
