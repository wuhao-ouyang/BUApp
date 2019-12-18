package martin.app.bitunion;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.Menu;
import android.view.MenuItem;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import martin.app.bitunion.util.CommonIntents;
import martin.app.bitunion.util.ToastUtil;
import martin.app.bitunion.widget.PhotoView;

public class ImageViewerActivity extends BaseContentActivity {

    private String mImageUrl;

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        outState.putString("image_url", mImageUrl);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);
        getSupportActionBar().setShowHideAnimationEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_white_24dp);

        if (savedInstanceState != null)
            mImageUrl = savedInstanceState.getString("image_url");
        else
            mImageUrl = getIntent().getStringExtra(CommonIntents.EXTRA_IMAGE_URL);

        final PhotoView photoView = (PhotoView) findViewById(R.id.photoVw_image);
        photoView.setMaxInitialScaleFactor(1);
        photoView.enableImageTransforms(true);

        Glide.with(this).load(mImageUrl)
                .into(new SimpleTarget<Drawable>() {
                    @Override
                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                        photoView.bindDrawable(resource);
                        // Show gif
                        if (resource instanceof GifDrawable) {
                            ((GifDrawable) resource).setLoopCount(GifDrawable.LOOP_FOREVER);
                            ((GifDrawable) resource).start();
                        }
                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.image_viewer, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_sharelink:
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData.Item cliptext = new ClipData.Item(mImageUrl);
                String[] mime = new String[1];
                mime[0] = "text/plain";
                ClipData clip = new ClipData("图片链接", mime, cliptext);
                clipboard.setPrimaryClip(clip);
                ToastUtil.showToast(R.string.image_url_copied);
                return true;
        }
        return false;
    }
}