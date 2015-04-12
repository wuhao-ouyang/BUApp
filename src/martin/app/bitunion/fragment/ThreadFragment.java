package martin.app.bitunion.fragment;

import java.util.ArrayList;

import martin.app.bitunion.BUApplication;
import martin.app.bitunion.ImageViewerActivity;
import martin.app.bitunion.R;
import martin.app.bitunion.util.BUApi;
import martin.app.bitunion.util.CommonIntents;
import martin.app.bitunion.util.Settings;
import martin.app.bitunion.model.BUPost;
import martin.app.bitunion.util.DataParser;
import martin.app.bitunion.widget.ObservableWebView;

import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;

/**
 * A dummy fragment representing a section of the app, but that simply displays
 * dummy text.
 */
@SuppressLint({ "JavascriptInterface", "SetJavaScriptEnabled" })
public class ThreadFragment extends Fragment implements Updateable, ObservableWebView.OnScrollChangedCallback  {
    private static final String TAG = ThreadFragment.class.getSimpleName();

    public static final String ARG_THREAD_ID = "ThreadFragment.tid";
    public static final String ARG_PAGE_NUMBER = "ThreadFragment.page";

    private int POS_OFFSET;
    private int mTid;
    private int mPageNum;
    private ArrayList<BUPost> postlist = new ArrayList<BUPost>();

    private SwipeRefreshLayout mRefreshLayout;
    private ObservableWebView mPageWebView = null;
    private ProgressBar mSpinner;

    private int mReqCount;

    private PostActionListener mPostActionListener;

    public interface PostActionListener {
        void onQuoteClick(BUPost post);
        void onUserClick(int uid);
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
        mRefreshLayout.setEnabled(false);
        mPageWebView = (ObservableWebView) view.findViewById(R.id.webView_posts);
        mPageWebView.setOnScrollChangedCallback(this);
        mSpinner = (ProgressBar) view.findViewById(R.id.progressBar);
        if (postlist == null || postlist.isEmpty()) {
            mPageWebView.setVisibility(View.GONE);
            mSpinner.setVisibility(View.VISIBLE);
        } else {
            mPageWebView.setVisibility(View.VISIBLE);
            mSpinner.setVisibility(View.GONE);
        }

        String content = createHtmlCode();
        mPageWebView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
        mPageWebView.getSettings().setJavaScriptEnabled(true);
        mPageWebView.addJavascriptInterface(new JSInterface(new JSHandler(getActivity())), JSInterface.class.getSimpleName());
        mPageWebView.loadDataWithBaseURL("file:///android_asset/", content, "text/html", "utf-8", null);
        Log.i(TAG, "WebView created!>>" + mPageNum + ", Posts >>" + postlist.size());

        onRefresh();
    }

    @Override
    public void onRefresh() {
        if (isUpdating())
            return;
        mReqCount = 0;
        int from = mPageNum* Settings.POSTS_PER_PAGE;
        int to = (mPageNum+1)* Settings.POSTS_PER_PAGE;
        final ArrayList<BUPost> posts = new ArrayList<BUPost>(Settings.POSTS_PER_PAGE);
        while (from < to) {
            mReqCount++;
            BUApi.readPostList(mTid, from, from + 20, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    mReqCount--;
                    if (BUApi.getResult(response) != BUApi.Result.SUCCESS) {
                        Toast.makeText(BUApplication.getInstance(), response.toString(), Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(BUApplication.getInstance(), R.string.network_unknown, Toast.LENGTH_SHORT).show();
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
        mRefreshLayout.setRefreshing(false);
        mSpinner.setVisibility(View.GONE);
        mPageWebView.setVisibility(View.VISIBLE);

        String htmlcode = createHtmlCode();
        mPageWebView.loadDataWithBaseURL("file:///android_asset/", htmlcode, "text/html", "utf-8", null);
        Log.v(TAG, "fragment " + this.mPageNum + " updated");
    }

    @Override
    public void onScroll(WebView view, int l, int t) {
        if (view.getScrollY() == 0)
            mRefreshLayout.setEnabled(true);
        else
            mRefreshLayout.setEnabled(false);
    }

    private String createHtmlCode(){
        StringBuilder content = new StringBuilder("<!DOCTYPE ><html><head><title></title>" +
                "<style type=\"text/css\">" +
                "img{max-width: 100%; width:auto; height: auto;}" +
                "body{background-color: #D8E2EF; color: #284264;font-size:" + BUApplication.settings.contenttextsize +"px;}" +
                "</style><script type='text/javascript'>" +
                JSInterface.javaScript() +
                "</script></head><body>");

        int len = postlist.size();
        for (int i = 0; i < len; i++){
            BUPost postItem = postlist.get(i);
            content.append(postItem.getHtmlLayout(POS_OFFSET + i));
        }
        content.append("</body></html>");
        return content.toString();
    }

    public void setPostActionListener(PostActionListener l) {
        mPostActionListener = l;
    }

    private class JSHandler extends Handler {
        private final Context context;

        private JSHandler(Context context) {
            this.context = context;
        }

        public void handleMessage(android.os.Message msg) {
            if (msg.what == 0) {
                BUPost post = postlist.get(msg.arg1 - POS_OFFSET);
                if (mPostActionListener != null)
                    mPostActionListener.onQuoteClick(post);
            }
            if (msg.what == 1) {
                if (mPostActionListener != null)
                    mPostActionListener.onUserClick(msg.arg1);
            }
        }
    };

    private static class JSInterface {

        private JSHandler handler;

        private JSInterface(JSHandler h) {
            handler = h;
        }

        private static final String javaScript() {
            return "function referenceOnClick(num){" +
                    JSInterface.class.getSimpleName()+".referenceOnClick(num);}" +
                    "function authorOnClick(uid){" +
                    JSInterface.class.getSimpleName()+".authorOnClick(uid);}" +
                    "function imageOnClick(url){" +
                    JSInterface.class.getSimpleName()+".imageOnClick(url);}";
        }

        @JavascriptInterface
        public void referenceOnClick(int count){
            handler.obtainMessage();
            Message msg = handler.obtainMessage();
            msg.what = 0;
            msg.arg1 = count;
            handler.sendMessage(msg);
            Log.v("JavascriptInterface", "Ref Count>>" + count);
        }

        @JavascriptInterface
        public void authorOnClick(int uid){
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