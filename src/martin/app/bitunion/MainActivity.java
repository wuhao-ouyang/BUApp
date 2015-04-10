package martin.app.bitunion;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.transition.ChangeBounds;
import android.transition.ChangeClipBounds;
import android.transition.ChangeTransform;
import android.transition.Scene;
import android.transition.TransitionManager;
import android.transition.TransitionSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.h6ah4i.android.widget.advrecyclerview.animator.GeneralItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.animator.RefactoredDefaultItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.expandable.RecyclerViewExpandableItemManager;
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractExpandableItemAdapter;
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractExpandableItemViewHolder;
import com.h6ah4i.android.widget.advrecyclerview.utils.WrapperAdapterUtils;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;

import martin.app.bitunion.model.BUForum;
import martin.app.bitunion.util.BUApiHelper;
import martin.app.bitunion.util.BUAppUtils;
import martin.app.bitunion.util.CommonIntents;
import martin.app.bitunion.util.ToastUtil;

public class MainActivity extends ActionBarActivity {

    // Actionbar menu instance
    private Menu mOptionMenu;
    // 论坛列表视图
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.Adapter mWrappedAdapter;
    private RecyclerViewExpandableItemManager mRecyclerViewExpandableItemManager;
    // 单组论坛列表数据
    private ArrayList<BUForum> forumList = new ArrayList<BUForum>();
    // 所有论坛列表数据
    private ArrayList<ArrayList<BUForum>> fArrayList = new ArrayList<ArrayList<BUForum>>();
    // 分组数据
    private String[] groupList;

    // 上次按返回键的时间
    private long touchTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().setTitle("北理FTP联盟");

        // ExpandableListView的分组信息
        groupList = getResources().getStringArray(R.array.forum_group);

        // 读取论坛列表信息
        String[] forumNames = getResources().getStringArray(R.array.forums);
        int[] forumFids = getResources().getIntArray(R.array.fids);
        int[] forumTypes = getResources().getIntArray(R.array.types);
        for (int i = 0; i < forumNames.length; i++)
            forumList.add(new BUForum(forumNames[i], forumFids[i], forumTypes[i]));
        // 转换论坛列表信息为二维数组，方便ListViewAdapter读入
        for (int i = 0; i < groupList.length; i++) {
            ArrayList<BUForum> forums = new ArrayList<BUForum>();
            for (BUForum forum : forumList)
                if (i == forum.getType())
                    forums.add(forum);
            fArrayList.add(forums);
        }

        mRecyclerView = (RecyclerView) this.findViewById(R.id.listview);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerViewExpandableItemManager = new RecyclerViewExpandableItemManager(null);
        ForumListAdapter itemAdapter = new ForumListAdapter();
        mAdapter = itemAdapter;
        mWrappedAdapter = mRecyclerViewExpandableItemManager.createWrappedAdapter(itemAdapter);

        final GeneralItemAnimator animator = new RefactoredDefaultItemAnimator();
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mWrappedAdapter);  // requires *wrapped* adapter
        mRecyclerView.setItemAnimator(animator);
        mRecyclerView.setHasFixedSize(false);
        mRecyclerViewExpandableItemManager.attachRecyclerView(mRecyclerView);

        setupUser();
    }

    @Override
    protected void onDestroy() {
        if (mRecyclerViewExpandableItemManager != null) {
            mRecyclerViewExpandableItemManager.release();
            mRecyclerViewExpandableItemManager = null;
        }

        if (mRecyclerView != null) {
            mRecyclerView.setItemAnimator(null);
            mRecyclerView.setAdapter(null);
            mRecyclerView = null;
        }

        if (mWrappedAdapter != null) {
            WrapperAdapterUtils.releaseAll(mWrappedAdapter);
            mWrappedAdapter = null;
        }
        mAdapter = null;
        mLayoutManager = null;
        super.onDestroy();
    }

    private void setupUser() {

        BUApiHelper.tryLogin(new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                switch (BUApiHelper.getResult(response)) {
                    case FAILURE:
                        ToastUtil.showToast(R.string.login_fail);
                        break;
                    case SUCCESS:
                        ToastUtil.showToast(R.string.login_success);
                        updateOptionMenu();
                        break;
                    case UNKNOWN:
                        ToastUtil.showToast(R.string.network_unknown);
                        break;
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                ToastUtil.showToast(R.string.network_unknown);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        mOptionMenu = menu;
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    private void updateOptionMenu() {
        MenuItem loginItem = mOptionMenu.findItem(R.id.action_login);
        if (BUApiHelper.isUserLoggedin())
            loginItem.setTitle(R.string.menu_action_user);
        else
            loginItem.setTitle(R.string.menu_action_login);
    }

    /**
     * Menu click events
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.action_login:
                if (!BUApiHelper.isUserLoggedin()) {
                    Intent intent = new Intent(this, LoginActivity.class);
                    startActivityForResult(intent, BUAppUtils.MAIN_REQ);
                } else {
                    Intent intent = new Intent(this, MyinfoActivity.class);
                    startActivity(intent);
                }
                break;
            case R.id.action_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                break;
            default:}
        return true;
        // return super.onOptionsItemSelected(item);
    }

    private class ForumListAdapter extends AbstractExpandableItemAdapter<GroupViewHolder, ChildViewHolder>  {

        private ForumListAdapter() {
            setHasStableIds(true);
        }

        @Override
        public int getGroupCount() {
            return groupList.length;
        }

        @Override
        public int getChildCount(int groupPosition) {
            return fArrayList.get(groupPosition).size();
        }

        @Override
        public long getGroupId(int groupPos) {
            return groupPos;
        }

        @Override
        public long getChildId(int groupPos, int childPos) {
            return childPos;
        }

        @Override
        public int getGroupItemViewType(int i) {
            return 0;
        }

        @Override
        public int getChildItemViewType(int i, int i1) {
            return 0;
        }

        @Override
        public GroupViewHolder onCreateGroupViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_forum_group, parent, false);
            return new GroupViewHolder(view);
        }

        @Override
        public ChildViewHolder onCreateChildViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_forum_title, parent, false);
            return new ChildViewHolder(view);
        }

        @Override
        public void onBindGroupViewHolder(GroupViewHolder groupVH, int groupPosition, int viewType) {
            groupVH.groupName.setText(groupList[groupPosition]);
            groupVH.itemView.setClickable(true);
        }

        @Override
        public void onBindChildViewHolder(ChildViewHolder childViewHolder, int groupPos, int childPos, int viewType) {
            if (fArrayList.get(groupPos).get(childPos).getName().contains("--"))
                childViewHolder.childTitle.setTextSize(BUApplication.settings.titletextsize + 2);
            else
                childViewHolder.childTitle.setTextSize(BUApplication.settings.titletextsize + 2 + 4);
            childViewHolder.childTitle.setBackgroundResource(R.drawable.ripple_main_row_click);
            childViewHolder.childTitle.setText(fArrayList.get(groupPos).get(childPos).getName());
            final BUForum forum = fArrayList.get(groupPos).get(childPos);
            // 注册OnClick事件，触摸点击转至DisplayActivity
            childViewHolder.childTitle.setOnClickListener(new OnClickListener() {

                @Override
                @SuppressWarnings("NewApi")
                public void onClick(View v) {
                    if (BUApiHelper.isUserLoggedin()) {
                        if (forum.getFid() == -1) {
                            // TODO 最新帖子
                            ToastUtil.showToast("功能暂时无法使用");
                            return;
                        }
                        if (forum.getFid() == -2) {
                            // TODO 收藏夹
                            ToastUtil.showToast("功能暂时无法使用");
                            return;
                        }
                        Intent intent = new Intent(MainActivity.this, DisplayActivity.class);
                        intent.putExtra(CommonIntents.EXTRA_FID, forum.getFid());
                        intent.putExtra(CommonIntents.EXTRA_FORUM_NAME, forum.getName());
                        startActivity(intent);
                    } else
                        ToastUtil.showToast("请先登录");
                }
            });
        }

        @Override
        public boolean onCheckCanExpandOrCollapseGroup(GroupViewHolder groupVH, int groupPosition, int x, int y, boolean expand) {
            groupVH.indicator.setImageResource(expand ? R.drawable.ic_expand_more_grey600_48dp:R.drawable.ic_expand_less_grey600_48dp);
            return true;
        }
    }

    private static class GroupViewHolder extends AbstractExpandableItemViewHolder {
        ImageView indicator;
        TextView groupName;

        private GroupViewHolder(View itemView) {
            super(itemView);
            indicator = (ImageView) itemView.findViewById(R.id.imgVw_group_expand_indicator);
            groupName = (TextView) itemView.findViewById(R.id.txtVw_group_title);
            groupName.setTextSize(BUApplication.settings.titletextsize + 2 + 4 + 5);
        }
    }

    private static class ChildViewHolder extends AbstractExpandableItemViewHolder {
        TextView childTitle;

        private ChildViewHolder(View itemView) {
            super(itemView);
            childTitle = (TextView) itemView.findViewById(R.id.txtVw_forum_title);
        }
    }

    @Override
    public void onBackPressed() {
        long currentTime = System.currentTimeMillis();
        if((currentTime-touchTime)>= BUAppUtils.EXIT_WAIT_TIME) {
            ToastUtil.showToast("再按一次退出程序");
            touchTime = currentTime;
        }else {
            super.onBackPressed();
        }
    }

}
