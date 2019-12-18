package martin.app.bitunion.util;

import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.integration.volley.VolleyRequestFactory;
import com.bumptech.glide.integration.volley.VolleyRequestFuture;
import com.bumptech.glide.integration.volley.VolleyStreamFetcher;
import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.load.model.ModelLoaderFactory;
import com.bumptech.glide.load.model.MultiModelLoaderFactory;
import com.bumptech.glide.signature.ObjectKey;

import java.io.InputStream;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * A simple model loader for fetching media over http/https using Volley.
 */
public class SyncedVolleyUrlLoader implements ModelLoader<GlideUrl, InputStream> {

    /**
     * The default factory for {@link SyncedVolleyUrlLoader}s.
     */
    public static class Factory implements ModelLoaderFactory<GlideUrl, InputStream> {
        private static RequestQueue internalQueue;
        private RequestQueue requestQueue;
        private final VolleyRequestFactory requestFactory;

        private static RequestQueue getInternalQueue(Context context) {
            if (internalQueue == null) {
                synchronized (Factory.class) {
                    if (internalQueue == null) {
                        internalQueue = Volley.newRequestQueue(context);
                    }
                }
            }
            return internalQueue;
        }

        /**
         * Constructor for a new Factory that runs requests using a static singleton request queue.
         */
        public Factory(Context context) {
            this(getInternalQueue(context));
        }

        /**
         * Constructor for a new Factory that runs requests using the given {@link RequestQueue}.
         */
        public Factory(RequestQueue requestQueue) {
            this(requestQueue, VolleyStreamFetcher.DEFAULT_REQUEST_FACTORY);
        }

        /**
         * Constructor for a new Factory with a custom Volley request factory that runs requests
         * using the given {@link RequestQueue}.
         */
        public Factory(RequestQueue requestQueue, VolleyRequestFactory requestFactory) {
            this.requestFactory = requestFactory;
            this.requestQueue = requestQueue;
        }

        @NonNull
        @Override
        public ModelLoader<GlideUrl, InputStream> build(@NonNull MultiModelLoaderFactory multiFactory) {
            return new SyncedVolleyUrlLoader(requestQueue, requestFactory);
        }

        @Override
        public void teardown() {
            // Do nothing, this instance doesn't own the request queue.
        }
    }

    private final RequestQueue requestQueue;
    private final VolleyRequestFactory requestFactory;

    public SyncedVolleyUrlLoader(RequestQueue requestQueue) {
        this(requestQueue, VolleyStreamFetcher.DEFAULT_REQUEST_FACTORY);
    }

    public SyncedVolleyUrlLoader(RequestQueue requestQueue, VolleyRequestFactory requestFactory) {
        this.requestQueue = requestQueue;
        this.requestFactory = requestFactory;
    }

    @Nullable
    @Override
    public LoadData<InputStream> buildLoadData(@NonNull GlideUrl url, int width, int height, @NonNull Options options) {
        return new LoadData<InputStream>(new ObjectKey(url), new VolleyStreamFetcher(
            requestQueue, url, new VolleyRequestFuture<InputStream>(), requestFactory));
    }

    @Override
    public boolean handles(@NonNull GlideUrl glideUrl) {
        return true;
    }
}
