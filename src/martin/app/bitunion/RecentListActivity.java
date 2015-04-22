package martin.app.bitunion;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.OnScrollListener;
import android.transition.TransitionManager;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import martin.app.bitunion.model.RecentThread;
import martin.app.bitunion.util.BUApi;
import martin.app.bitunion.util.CommonIntents;
import martin.app.bitunion.util.ToastUtil;


public class RecentListActivity extends ActionBarActivity implements SwipeRefreshLayout.OnRefreshListener {
    private static final String TAG = RecentListActivity.class.getSimpleName();

    private List<RecentThread> mThreadList;
    private SparseBooleanArray mExpandTable;

    private View mSpinner;
    private SwipeRefreshLayout mRefreshLyt;
    private RecyclerView mRecyclerVw;
    private RecentListAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recent_list);

        if (savedInstanceState != null) {

        } else {
            mThreadList = new ArrayList<RecentThread>();
            mExpandTable = new SparseBooleanArray();
        }
        getSupportActionBar().setTitle(R.string.title_activity_recent_list);

        mSpinner = findViewById(R.id.progressBar);
        mRefreshLyt = (SwipeRefreshLayout) findViewById(R.id.lyt_refresh_frame);
        mRecyclerVw = (RecyclerView) findViewById(R.id.listview);
        mRecyclerVw.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new RecentListAdapter();
        mRecyclerVw.setAdapter(mAdapter);
        mRecyclerVw.setOnScrollListener(mScrollListener);

        showLoading(true);
        onRefresh();
    }

    private void showLoading(boolean loading) {
        mSpinner.setVisibility(loading ? View.VISIBLE:View.GONE);
        mRecyclerVw.setVisibility(loading ? View.GONE:View.VISIBLE);
    }

    @Override
    public void onRefresh() {
        mRefreshLyt.setRefreshing(true);
        BUApi.readHomeThreads(new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                if (BUApi.getResult(response) != BUApi.Result.SUCCESS) {
                    ToastUtil.showToast(response.toString());
                } else {
                    JSONArray newlist = response.optJSONArray("newlist");
                    for (int i = 0; i < newlist.length(); i++)
                        try {
                            mThreadList.add(new RecentThread(newlist.getJSONObject(i)));
                            mExpandTable.put(i, false);
                        } catch (JSONException e) {
                            continue;
                        }
                }
                mAdapter.notifyDataSetChanged();
                showLoading(false);
                mRefreshLyt.setRefreshing(false);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                ToastUtil.showToast(R.string.network_unknown);
                showLoading(false);
                mRefreshLyt.setRefreshing(false);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_recent_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.action_refresh:
                onRefresh();
                break;
        }
        return true;
    }

    private RecyclerView.OnScrollListener mScrollListener = new OnScrollListener() {
        private int totalY;
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            totalY += dy;
            if (totalY != 0)
                mRefreshLyt.setEnabled(false);
            else
                mRefreshLyt.setEnabled(true);
        }
    };

    private class RecentListAdapter extends RecyclerView.Adapter<VH> {

        @Override
        public VH onCreateViewHolder(ViewGroup parent, int viewType) {
            View row = LayoutInflater.from(parent.getContext()).inflate(R.layout.recent_thread_item, parent, false);
            return new VH(row);
        }

        @Override
        public void onBindViewHolder(final VH holder, final int position) {
            final RecentThread item = mThreadList.get(position);
            if (position % 2 == 0)
                holder.itemView.setBackgroundResource(R.drawable.ripple_text_bg_dark);
            else
                holder.itemView.setBackgroundResource(R.drawable.ripple_text_bg_light);
            holder.title.setText(item.title);
            holder.forum.setText(item.forum);
            holder.author.setText(item.author);
            holder.lastAuthor.setText("Last by " + item.lastAuthor);
            holder.lastTime.setText(item.lastTime);
            holder.lastMessage.setText(item.lastReply);

            boolean isExpanded = mExpandTable.get(position);
            holder.setExpanded(isExpanded);

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(v.getContext(), ThreadActivity.class);
                    intent.putExtra(CommonIntents.EXTRA_TID, item.tid);
                    intent.putExtra(CommonIntents.EXTRA_THREAD_NAME, item.title);
                    intent.putExtra(CommonIntents.EXTRA_REPIES, item.replies + 1);
                    v.getContext().startActivity(intent);
                }
            });
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    boolean expand = !mExpandTable.get(position);
                    mExpandTable.put(position, expand);
                    if (!expand)
                        holder.flipLayout(holder.basicLyt, holder.detailLyt);
                    else
                        holder.flipLayout(holder.detailLyt, holder.basicLyt);
                    return true;
                }
            });
        }

        @Override
        public int getItemCount() {
            return mThreadList.size();
        }
    }

    private static class VH extends RecyclerView.ViewHolder {
        ViewGroup basicLyt;
        ViewGroup detailLyt;

        TextView title;
        TextView forum;
        TextView author;

        TextView lastAuthor;
        TextView lastTime;
        TextView lastMessage;

        VH(View itemView) {
            super(itemView);
            basicLyt = (ViewGroup) itemView.findViewById(R.id.lyt_basic_info);
            detailLyt = (ViewGroup) itemView.findViewById(R.id.lyt_detail_info);
            title = (TextView) itemView.findViewById(R.id.txtVw_thread_title);
            forum = (TextView) itemView.findViewById(R.id.txtVw_forum_title);
            author = (TextView) itemView.findViewById(R.id.txtVw_author_name);
            lastAuthor = (TextView) itemView.findViewById(R.id.txtVw_last_author);
            lastTime = (TextView) itemView.findViewById(R.id.txtVw_last_time);
            lastMessage = (TextView) itemView.findViewById(R.id.txtVw_last_message);
        }

        private void setExpanded(boolean expand) {
            basicLyt.setVisibility(expand ? View.GONE : View.VISIBLE);
            detailLyt.setVisibility(expand ? View.VISIBLE : View.GONE);
            basicLyt.setRotationX(expand ? 90f : 0f);
            detailLyt.setRotationX(expand ? 0f : 90f);
            basicLyt.setAlpha(1f);
            detailLyt.setAlpha(1f);
        }

        private void flipLayout(final View tobeIn, final View tobeOut) {
            AnimatorSet set = new AnimatorSet();
            set.setDuration(500l);
            AnimatorSet out = new AnimatorSet();
            out.setDuration(250l);
            out.play(ObjectAnimator.ofFloat(tobeOut, View.ROTATION_X, 0f, 90f))
                    .with(ObjectAnimator.ofFloat(tobeOut, View.ALPHA, 1f, 0f));
            AnimatorSet in = new AnimatorSet();
            in.setDuration(250l);
            in.play(ObjectAnimator.ofFloat(tobeIn, View.ROTATION_X, 90f, 0f))
                    .with(ObjectAnimator.ofFloat(tobeIn, View.ALPHA, 0f, 1f));
            set.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    super.onAnimationStart(animation);
                    tobeIn.setVisibility(View.VISIBLE);
                    tobeOut.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    tobeIn.setVisibility(View.VISIBLE);
                    tobeOut.setVisibility(View.GONE);
                }
            });
            set.play(out).before(in);
            set.start();

        }
    }
}
