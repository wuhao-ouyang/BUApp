package martin.app.bitunion.fragment;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import martin.app.bitunion.BUApp;
import martin.app.bitunion.ImageViewerActivity;
import martin.app.bitunion.R;
import martin.app.bitunion.ThreadActivity;
import martin.app.bitunion.model.BUPost;
import martin.app.bitunion.util.BUApi;
import martin.app.bitunion.util.CommonIntents;
import martin.app.bitunion.util.DataParser;
import martin.app.bitunion.util.Settings;
import martin.app.bitunion.util.Utils;
import martin.app.bitunion.widget.ObservableWebView;
import martin.app.bitunion.widget.WebListAdapter;

/**
 * A dummy fragment representing a section of the app, but that simply displays
 * dummy text.
 */
public class ThreadFragment extends Fragment implements Updateable, ObservableWebView.OnScrollChangedCallback {
    private static final String TAG = ThreadFragment.class.getSimpleName();

    public static final String ARG_THREAD_ID = "ThreadFragment.tid";
    public static final String ARG_PAGE_NUMBER = "ThreadFragment.page";

    private static String COLOR_BG_DARK;
    private static String COLOR_BG_LIGHT;

    private int POS_OFFSET;
    private int mTid;
    private int mPageNum;
    private ArrayList<BUPost> postlist = new ArrayList<BUPost>();
    private PostListAdapter mAdapter = new PostListAdapter();

    private SwipeRefreshLayout mRefreshLayout;
    private ObservableWebView mPageWebView = null;
    private ProgressBar mSpinner;

    private int mReqCount;
    private long mLastTs;
    private int mLastY;
    private static final long REFRESH_LIMIT = 30 * 1000l;

    private ThreadContentListener mThreadContentListener;

    public interface ThreadContentListener {
        void onQuoteClick(@NonNull BUPost post);
        void onUserClick(int uid);
        void onSubjectUpdated(@Nullable BUPost subject);
        void onEndReached();
        void onScroll(boolean down);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("offset", POS_OFFSET);
        outState.putInt("tid", mTid);
        outState.putInt("page_num", mPageNum);
        outState.putParcelableArrayList("post_list", postlist);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            POS_OFFSET = savedInstanceState.getInt("offset");
            mTid = savedInstanceState.getInt("tid");
            mPageNum = savedInstanceState.getInt("page_num");
            postlist = savedInstanceState.getParcelableArrayList("post_list");
        } else {
            mTid = getArguments().getInt(ARG_THREAD_ID);
            mPageNum = getArguments().getInt(ARG_PAGE_NUMBER);
            POS_OFFSET = mPageNum * Settings.POSTS_PER_PAGE + 1;
        }

        Resources res = getResources();
        COLOR_BG_DARK = Integer.toHexString(res.getColor(R.color.blue_light) & 0x00ffffff);
        COLOR_BG_LIGHT = Integer.toHexString(res.getColor(R.color.blue_text_bg_light) & 0x00ffffff);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_display_posts, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.lyt_refresh_frame);
        mRefreshLayout.setOnRefreshListener(this);
        mPageWebView = (ObservableWebView) view.findViewById(R.id.webView_posts);
        mPageWebView.setOnScrollChangedCallback(this);
        mSpinner = (ProgressBar) view.findViewById(R.id.progressBar);
        if (postlist == null || postlist.isEmpty()) {
            mPageWebView.setVisibility(View.GONE);
            mSpinner.setVisibility(View.VISIBLE);
            onRefresh();
        } else {
            mPageWebView.setVisibility(View.VISIBLE);
            mSpinner.setVisibility(View.GONE);
        }

        mPageWebView.setWebViewClient(new ThreadWebViewClient());
        mPageWebView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
        mPageWebView.getSettings().setJavaScriptEnabled(true);
        mPageWebView.addJavascriptInterface(new JSInterface(new JSHandler(getActivity())), JSInterface.class.getSimpleName());
        mPageWebView.setAdapter(mAdapter);
        // This is just intended for reloading, adapter seems have some problem with access to local assets
        String htmlcode = createHtmlCode();
        mPageWebView.loadDataWithBaseURL(BUApi.getRootUrl(), htmlcode, "text/html", "utf-8", null);
        Log.i(TAG, "WebView created!>>" + mPageNum + ", Posts >>" + postlist.size());
    }

    @Override
    public void onRefresh() {
        if (isUpdating())
            return;
        mRefreshLayout.setRefreshing(true);
        mReqCount = 0;
        int from = mPageNum * Settings.POSTS_PER_PAGE;
        int to = (mPageNum + 1) * Settings.POSTS_PER_PAGE;
        final ArrayList<BUPost> posts = new ArrayList<BUPost>(Settings.POSTS_PER_PAGE);
        while (from < to) {
            mReqCount++;
            BUApi.readPostList(mTid, from, from + 20, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    mReqCount--;
                    if (BUApi.getResult(response) != BUApi.Result.SUCCESS) {
                        Toast.makeText(BUApp.getInstance(), response.toString(), Toast.LENGTH_SHORT).show();
                    } else {
                        ArrayList<BUPost> tempList = DataParser.parsePostlist(response);
                        if (tempList != null)
                            posts.addAll(tempList);
                    }
                    if (!isUpdating()) {
                        postlist = posts;
                        notifyUpdated();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    mReqCount--;
                    notifyUpdated();
                    Toast.makeText(BUApp.getInstance(), R.string.network_unknown, Toast.LENGTH_SHORT).show();
                }
            });
            from += 20;
        }
    }

    @Override
    public boolean isUpdating() {
        return mReqCount != 0;
    }

    @Override
    public void notifyUpdated() {
        mLastTs = System.currentTimeMillis();
        mRefreshLayout.setRefreshing(false);
        mSpinner.setVisibility(View.GONE);
        mPageWebView.setVisibility(View.VISIBLE);
        mAdapter.notifyDataSetChanged();
//        String htmlcode = createHtmlCode();
//        mPageWebView.loadDataWithBaseURL(BUApi.getRootUrl(), htmlcode, "text/html", "utf-8", null);
        Log.v(TAG, "fragment " + this.mPageNum + " updated");

        if (mThreadContentListener != null && mPageNum == 0 && postlist.size() > 0) {
            mThreadContentListener.onSubjectUpdated(postlist.get(0));
        }
    }

    @Override
    public void onScroll(WebView view, int l, int t) {
        if (t == 0)
            mRefreshLayout.setEnabled(true);
        else
            mRefreshLayout.setEnabled(false);
        int trigger = (int)(view.getContentHeight() * view.getScale()) - view.getHeight()-1;
        if (postlist.size() < Settings.POSTS_PER_PAGE && t >= trigger && System.currentTimeMillis() - mLastTs >= REFRESH_LIMIT) {
            mThreadContentListener.onEndReached();
            Log.i(TAG, "fetch more >> " + mPageNum);
            onRefresh();
        }
        mThreadContentListener.onScroll(t - mLastY > 0);
        mLastY = t;
    }

    private String createHtmlCode() {
        // Get background color from resources and

        StringBuilder content = new StringBuilder("<!DOCTYPE ><html><head><title></title>" +
                "<style type=\"text/css\">" +
                "img{max-width:100%; width:auto; height:auto;}" +
                "body{margin:0px; padding:0px; background-color:#" + COLOR_BG_LIGHT + "; color:#284264; font-size:" + BUApp.settings.contenttextsize + "px;}" +
                "div.tdiv{background-color:#" + COLOR_BG_DARK + "; padding:2px 5px; font-size:" + BUApp.settings.contenttextsize + "px;}" +
                "div.mdiv{padding:8px; word-break:break-all;}" +
                "</style><script type='text/javascript'>" +
                JSInterface.javaScript() +
                "</script></head><body>");

        int len = postlist.size();
        for (int i = 0; i < len; i++) {
            BUPost postItem = postlist.get(i);
            content.append(postItem.getHtmlLayout(POS_OFFSET + i));
        }
        content.append("</body></html>");
        return content.toString();
    }

    public void setThreadContentListener(ThreadContentListener l) {
        mThreadContentListener = l;
    }

    /**
     * Custom {@link WebViewClient} to handle url clicks
     * Will intercept bitunion thread url and redirect to {@link ThreadActivity}
     * For image links will try go to {@link ImageViewerActivity}
     */
    private static class ThreadWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            boolean isHandled = false;
            Context context = view.getContext();
            Uri uri = Uri.parse(url);

            String tid = "";
            String path = "";
            if (Utils.isBitUnionUrl(url)) {
                path = uri.getPath();
                Log.d("WebViewClient", "bit path >> " + path);
                if (path.equalsIgnoreCase("/viewthread.php")) {
                    if (uri.getQueryParameterNames().contains("tid")) {
                        tid = uri.getQueryParameter("tid");
                        isHandled = true;
                    }
                } else {
                    Pattern p = Pattern.compile("^/thread-([0-9]+)-.*\\.html$");
                    Matcher m = p.matcher(path);
                    if (m.find()) {
                        tid = m.group(1);
                        isHandled = true;
                    }
                }
            }
            if (isHandled) {
                try {
                    Intent i = new Intent(context, ThreadActivity.class);
                    i.putExtra(CommonIntents.EXTRA_TID, Integer.parseInt(tid));
                    context.startActivity(i);
                } catch (NumberFormatException e) {
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                Log.d("WebViewClient", "url >> " + url);
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.addCategory(Intent.CATEGORY_BROWSABLE);
                i.setData(uri);
                try {
                    context.startActivity(i);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(context, "Cannot find a browser app!", Toast.LENGTH_SHORT).show();
                }
            }
            return true;
        }
    }

    private class PostListAdapter extends WebListAdapter {
        @Override
        public String getBaseUrl() {
            return BUApi.getRootUrl();
        }

        @Override
        public String getCSS() {
            return "img{max-width:100%; width:auto; height:auto;}" +
                    "body{margin:0px; padding:0px; background-color:#" + COLOR_BG_LIGHT + "; color:#284264; font-size:" + BUApp.settings.contenttextsize + "px;}" +
                    "div.tdiv{background-color:#" + COLOR_BG_DARK + "; padding:2px 5px; font-size:" + BUApp.settings.contenttextsize + "px;}" +
                    "div.mdiv{padding:8px; word-break:break-all;}";
        }

        @Override
        public String getJavaScript() {
            return JSInterface.javaScript();
        }

        @Override
        public String getHtml(int pos) {
            return postlist.get(pos).getHtmlLayout(POS_OFFSET + pos);
        }

        @Override
        public int getCount() {
            return postlist.size();
        }
    }

    private class JSHandler extends Handler {
        private final Context context;

        private JSHandler(Context context) {
            this.context = context;
        }

        public void handleMessage(android.os.Message msg) {
            if (msg.what == 0) {
                BUPost post = postlist.get(msg.arg1 - POS_OFFSET);
                if (mThreadContentListener != null)
                    mThreadContentListener.onQuoteClick(post);
            }
            if (msg.what == 1) {
                if (mThreadContentListener != null)
                    mThreadContentListener.onUserClick(msg.arg1);
            }
        }
    }

    private static class JSInterface {

        private JSHandler handler;

        private JSInterface(JSHandler h) {
            handler = h;
        }

        private static final String javaScript() {
            return "function referenceOnClick(num){" +
                    JSInterface.class.getSimpleName() + ".referenceOnClick(num);}" +
                    "function authorOnClick(uid){" +
                    JSInterface.class.getSimpleName() + ".authorOnClick(uid);}" +
                    "function imageOnClick(url){" +
                    JSInterface.class.getSimpleName() + ".imageOnClick(url);}";
        }

        @JavascriptInterface
        public void referenceOnClick(int count) {
            handler.obtainMessage();
            Message msg = handler.obtainMessage();
            msg.what = 0;
            msg.arg1 = count;
            handler.sendMessage(msg);
            Log.v("JavascriptInterface", "Ref Count>>" + count);
        }

        @JavascriptInterface
        public void authorOnClick(int uid) {
            handler.obtainMessage();
            Message msg = handler.obtainMessage();
            msg.what = 1;
            msg.arg1 = uid;
            handler.sendMessage(msg);
            Log.i("JavascriptInterface", "Author ID>>" + uid);
        }

        @JavascriptInterface
        public void imageOnClick(String url) {
            Log.i("JavascriptInterface", "image>>" + url);
            Intent i = new Intent(handler.context, ImageViewerActivity.class);
            i.putExtra(CommonIntents.EXTRA_IMAGE_URL, url);
            handler.context.startActivity(i);
        }
    }
}