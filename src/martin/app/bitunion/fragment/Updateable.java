package martin.app.bitunion.fragment;

import android.support.v4.widget.SwipeRefreshLayout;

import java.util.List;

import martin.app.bitunion.model.BUContent;

public interface Updateable extends SwipeRefreshLayout.OnRefreshListener {
    @Override
    void onRefresh();
    boolean isUpdating();
    void notifyUpdated();
}
