package martin.app.bitunion;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.transition.ChangeBounds;
import android.transition.ChangeTransform;
import android.transition.TransitionManager;
import android.transition.TransitionSet;
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

        mRecyclerView = (RecyclerView) this.findViewById(R.id.listview);

        // ExpandableListView的分组信息
        groupList = getResources().getStringArray(R.array.forum_group);

        // 读取论坛列表信息
        String[] forumNames = getResources().getStringArray(R.array.forums);
        int[] forumFids = getResources().getIntArray(R.array.fids);
        int[] forumTypes = getResources().getIntArray(R.array.types);
        for (int i = 0; i < forumNames.length; i++) {
            forumList.add(new BUForum(forumNames[i], forumFids[i],
                    forumTypes[i]));
        }
        // 转换论坛列表信息为二维数组，方便ListViewAdapter读入
        for (int i = 0; i < groupList.length; i++) {
            ArrayList<BUForum> forums = new ArrayList<BUForum>();
            for (BUForum forum : forumList) {
                if (i == forum.getType()) {
                    forums.add(forum);
                }
            }
            fArrayList.add(forums);
        }
        // Log.v("martin", fArrayList.get(0).get(0).getName());
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(new ForumListAdapter());

        setupUser();
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

    private class ForumListAdapter extends RecyclerView.Adapter<ViewHolder>  {

        private View getChildView(int groupPosition, int childPosition) {
            TextView childTitle = new TextView(MainActivity.this);
            childTitle.setBackgroundResource(R.drawable.ripple_main_row_click);
            if (fArrayList.get(groupPosition).get(childPosition).getName().contains("--"))
                childTitle.setTextSize(BUApplication.settings.titletextsize + 2);
            else
                childTitle.setTextSize(BUApplication.settings.titletextsize + 2 + 4);
            childTitle.setPadding(60, 10, 0, 10);
            childTitle.setText(fArrayList.get(groupPosition).get(childPosition).getName());
            final BUForum forum = fArrayList.get(groupPosition).get(childPosition);
            // 注册OnClick事件，触摸点击转至DisplayActivity
            childTitle.setOnClickListener(new OnClickListener() {

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
            return childTitle;
        }

        private int getChildrenCount(int groupPosition) {
            return fArrayList.get(groupPosition).size();
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_forum_group, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            holder.groupName.setTextSize(BUApplication.settings.titletextsize + 2 + 4 + 5);
            holder.groupName.setText(groupList[position]);
            // Inflate child views
            if (holder.childsContainer.getChildCount() == 0) {
                for (int i = 0; i < getChildrenCount(position); i++)
                    holder.childsContainer.addView(getChildView(position, i));
            }
        }

        @Override
        public int getItemCount() {
            return groupList.length;
        }

    }

    private static class ViewHolder extends RecyclerView.ViewHolder implements OnClickListener {
        boolean isExpanded;

        ImageView indicator;
        TextView groupName;
        ViewGroup childsContainer;

        public ViewHolder(View itemView) {
            super(itemView);
            indicator = (ImageView) itemView.findViewById(R.id.imgVw_group_expand_indicator);
            groupName = (TextView) itemView.findViewById(R.id.txtVw_group_title);
            childsContainer = (ViewGroup) itemView.findViewById(R.id.lyt_child_container);

            isExpanded = false;
            indicator.setImageResource(R.drawable.ic_expand_more_grey600_48dp);
            childsContainer.setVisibility(View.GONE);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//                TransitionSet transition = new TransitionSet();
//                transition.addTransition(new ChangeBounds());
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
//                    transition.addTransition(new ChangeTransform());
                TransitionManager.beginDelayedTransition((ViewGroup) itemView.getParent());
            }
            isExpanded = !isExpanded;
            indicator.setImageResource(isExpanded ? R.drawable.ic_expand_less_grey600_48dp : R.drawable.ic_expand_more_grey600_48dp);
            childsContainer.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
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
