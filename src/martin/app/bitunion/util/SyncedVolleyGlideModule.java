package martin.app.bitunion.util;

import android.content.Context;

import com.bumptech.glide.Glide;
import com.bumptech.glide.integration.volley.VolleyGlideModule;
import com.bumptech.glide.load.model.GlideUrl;

import java.io.InputStream;

public class SyncedVolleyGlideModule extends VolleyGlideModule {
    @Override
    public void registerComponents(Context context, Glide glide) {
        glide.getRegistry().append(GlideUrl.class, InputStream.class, new SyncedVolleyUrlLoader.Factory(context));
    }
}
