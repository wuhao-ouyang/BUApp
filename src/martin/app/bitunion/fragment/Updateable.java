package martin.app.bitunion.fragment;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public interface Updateable extends SwipeRefreshLayout.OnRefreshListener {
    @Override
    void onRefresh();

    boolean isUpdating();

    void notifyUpdated();
}
