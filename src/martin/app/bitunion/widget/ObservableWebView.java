package martin.app.bitunion.widget;

import android.content.Context;
import android.database.DataSetObserver;
import android.util.AttributeSet;
import android.util.Log;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import martin.app.bitunion.BUApp;

public class ObservableWebView extends WebView {
    private WebListAdapter mAdapter;
    private DataSetObserver mDataSetObserver;

    private class AdapterDataSetObserver extends DataSetObserver {
        @Override
        public void onChanged() {
            refreshContent();
        }

        @Override
        public void onInvalidated() {
            reloadContent();
        }
    }

    private OnScrollChangedCallback mOnScrollChangedCallback;

    public ObservableWebView(final Context context) {
        super(context);
    }

    public ObservableWebView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }

    public ObservableWebView(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onScrollChanged(final int l, final int t, final int oldl, final int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        if (mOnScrollChangedCallback != null) mOnScrollChangedCallback.onScroll(this, l, t);
    }

    public OnScrollChangedCallback getOnScrollChangedCallback() {
        return mOnScrollChangedCallback;
    }

    public void setOnScrollChangedCallback(final OnScrollChangedCallback onScrollChangedCallback) {
        mOnScrollChangedCallback = onScrollChangedCallback;
    }

    /**
     * Impliment in the activity/fragment/view that you want to listen to the webview
     */
    public interface OnScrollChangedCallback {
        /**
         * Intercept callback of {@link WebView#onScrollChanged(int, int, int, int)}
         * @param l Current horizontal scroll origin
         * @param t Current vertical scroll origin
         */
        void onScroll(WebView view, int l, int t);
    }

    /**
     * Bind {@link WebListAdapter} to this webview
     * @param adapter
     */
    public void setAdapter(WebListAdapter adapter) {
        if (mAdapter == adapter)
            return;
        if (mAdapter != null && mDataSetObserver != null)
            mAdapter.unregisterDataSetObserver(mDataSetObserver);
        mAdapter = adapter;
        if (mAdapter != null) {
            mDataSetObserver = new AdapterDataSetObserver();
            mAdapter.registerDataSetObserver(mDataSetObserver);
        }
//        reloadContent();
    }

    private void reloadContent() {
        if (mAdapter == null)
            return;
        // Set console debugger
        setWebChromeClient(new WebChromeClient());
        StringBuilder content = new StringBuilder("<!DOCTYPE ><html><head><title></title>" +
                "<style type=\"text/css\">" +
                (mAdapter == null ? "" : mAdapter.getCSS()) +
                "</style><script type='text/javascript'>" +
                (mAdapter == null ? "" : mAdapter.getJavaScript()) +
                "</script></head><body>");
        loadDataWithBaseURL(mAdapter.getBaseUrl(), content.toString(), "text/html", "utf-8", null);
//        refreshContent();
    }

    private void refreshContent() {
        int len = mAdapter.getCount();
        for (int i = 0; i < len; i++)
            runJavascriptUpdate(i, mAdapter.getHtml(i).replace("'", "\\'"));
        Log.i("Javascript", "content refreshed >> " + len);
    }

    private static final String HTML_ROW_CLASS = "ContentRow";

    private void runJavascriptUpdate(int pos, String rowHtml) {
        StringBuilder jsBuilder = new StringBuilder("javascript:(function myfunction(){");
        jsBuilder.append("var rows = document.body.getElementsByClassName('" + HTML_ROW_CLASS + "');\n" +
                "if (rows[" + pos + "] == null){" +
                "newrow = document.createElement('div');" +
                "newrow.setAttribute('class', '" + HTML_ROW_CLASS + "');" +
                "newrow.innerHTML = '" + rowHtml + "';" +
                "document.body.appendChild(newrow);" +
//                "console.log('row: "+pos+" inserted!');" +
                "}else{" +
                "rows[" + pos + "].innerHTML = '" + rowHtml + "';" +
                "console.log('row: " + pos + " updated!');" +
                "}})()");
//        loadDataWithBaseURL(mAdapter.getBaseUrl(), jsBuilder.toString(), "text/javascript", "utf-8", null);
        loadUrl(jsBuilder.toString());
    }

}