package martin.app.bitunion.widget;

import android.database.DataSetObservable;
import android.database.DataSetObserver;

/**
 * Mock adapter use similar pattern of {@link android.widget.Adapter} and javascript to inflate html layout with data.
 * Thus the webview content can be updated dynamically without reloading the whole page.
 * The data must implement {@link HtmlListItem} to get it actual functioning.
 * @see ObservableWebView
 */
public abstract class WebListAdapter {
    public interface HtmlListItem {
        String getHtmlLayout(int pos);
    }

    DataSetObservable mObservable = new DataSetObservable();

    public void registerDataSetObserver(DataSetObserver observer) {
        mObservable.registerObserver(observer);
    }

    public void unregisterDataSetObserver(DataSetObserver observer) {
        mObservable.unregisterObserver(observer);
    }

    public void notifyDataSetChanged() {
        mObservable.notifyChanged();
    }

    public void notifyDataSetInvalidated() {
        mObservable.notifyInvalidated();
    }

    public abstract String getBaseUrl();

    public abstract String getCSS();

    public abstract String getJavaScript();

    public abstract String getHtml(int pos);

    public abstract int getCount();

}
