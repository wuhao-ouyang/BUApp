package martin.app.bitunion;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

import com.idunnololz.widgets.AnimatedExpandableListView;

import java.util.ArrayList;

import martin.app.bitunion.model.BUForum;
import martin.app.bitunion.util.BUApiHelper;
import martin.app.bitunion.util.BUAppUtils;

public class MainActivity extends ActionBarActivity {

    // 论坛列表视图
    private ExpandableListView listView;
    // 单组论坛列表数据
    private ArrayList<BUForum> forumList = new ArrayList<BUForum>();
    // 所有论坛列表数据
    private ArrayList<ArrayList<BUForum>> fArrayList = new ArrayList<ArrayList<BUForum>>();
    // 分组数据
    private ArrayList<String> groupList;
    private ForumListAdapter adapter;

    // 上次按返回键的时间
    private long touchTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().setTitle("北理FTP联盟");

        listView = (ExpandableListView) this.findViewById(R.id.listview);

        // ExpandableListView的分组信息
        groupList = new ArrayList<String>();
        groupList.add(0, "系统管理区");
        groupList.add(1, "直通理工区");
        groupList.add(2, "技术讨论区");
        groupList.add(3, "苦中作乐区");
        groupList.add(4, "时尚生活区");
        groupList.add(5, "其他功能");

        // 读取论坛列表信息
        String[] forumNames = getResources().getStringArray(R.array.forums);
        int[] forumFids = getResources().getIntArray(R.array.fids);
        int[] forumTypes = getResources().getIntArray(R.array.types);
        for (int i = 0; i < forumNames.length; i++) {
            forumList.add(new BUForum(forumNames[i], forumFids[i],
                    forumTypes[i]));
        }
        // 转换论坛列表信息为二维数组，方便ListViewAdapter读入
        for (int i = 0; i < groupList.size(); i++) {
            ArrayList<BUForum> forums = new ArrayList<BUForum>();
            for (BUForum forum : forumList) {
                if (i == forum.getType()) {
                    forums.add(forum);
                }
            }
            fArrayList.add(forums);
        }
        // Log.v("martin", fArrayList.get(0).get(0).getName());
        listView.setAdapter(new ForumListAdapter());

        BUApiHelper.tryLogin();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    // 登录按钮跳转至login_activity
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

    // ExpandableListView的数据接口
    private class ForumListAdapter extends AnimatedExpandableListView.AnimatedExpandableListAdapter {

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            return fArrayList.get(groupPosition).get(childPosition);
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        @Override
        public View getRealChildView(int groupPosition, int childPosition,
                                 boolean isLastChild, View convertView, ViewGroup parent) {
            if (convertView == null) {
                ChildHolder holder = new ChildHolder();
                holder.title = new TextView(MainActivity.this);
                convertView = holder.title;
                convertView.setTag(holder);
            }
            ChildHolder holder = (ChildHolder) convertView.getTag();
            holder.title.setBackgroundResource(R.drawable.ripple_main_row_click);
            if (fArrayList.get(groupPosition).get(childPosition).getName().contains("--"))
                holder.title.setTextSize(BUApplication.settings.titletextsize + 2);
            else
                holder.title.setTextSize(BUApplication.settings.titletextsize + 2 + 4);
            holder.title.setPadding(60, 10, 0, 10);
            holder.title.setText(fArrayList.get(groupPosition).get(childPosition).getName());
            final BUForum forum = fArrayList.get(groupPosition).get(childPosition);
            // 注册OnClick事件，触摸点击转至DisplayActivity
            holder.title.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (BUApiHelper.isUserLoggedin()) {
                        if (forum.getFid() == -1){
                            // TODO 最新帖子
                            return;}
                        if (forum.getFid() == -2){
                            // TODO 收藏夹
                            return;}
                        Intent intent = new Intent(MainActivity.this,
                                DisplayActivity.class);
                        intent.putExtra("fid", forum.getFid());
                        intent.putExtra("name", forum.getName());
                        startActivityForResult(intent, BUAppUtils.MAIN_REQ);
                    } else showToast("请先登录");
                }
            });
            return convertView;
        }

        @Override
        public int getRealChildrenCount(int groupPosition) {
            return fArrayList.get(groupPosition).size();
        }

        @Override
        public Object getGroup(int groupPosition) {
            return groupList.get(groupPosition);
        }

        @Override
        public int getGroupCount() {
            return groupList.size();
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded,
                                 View convertView, ViewGroup parent) {
            if (convertView == null) {
                GroupHolder holder = new GroupHolder();
                holder.title = new TextView(MainActivity.this);
                convertView = holder.title;
                convertView.setTag(holder);
            }
            GroupHolder holder = (GroupHolder) convertView.getTag();
            holder.title.setBackgroundColor(getResources().getColor(R.color.blue_dark));
            holder.title.setTextSize(BUApplication.settings.titletextsize + 2 + 4 + 5);
            holder.title.setPadding(60, 10, 0, 10);
            holder.title.setText(groupList.get(groupPosition));
            return convertView;
        }
        @Override
        public boolean hasStableIds() {
            return true;
        }
        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }

    }

    private static class GroupHolder {
        TextView title;
    }

    private static class ChildHolder {
        TextView title;
    }

    @Override
    public void onBackPressed() {
        long currentTime = System.currentTimeMillis();
        if((currentTime-touchTime)>= BUAppUtils.EXIT_WAIT_TIME) {
            showToast("再按一次退出程序");
            touchTime = currentTime;
        }else {
            super.onBackPressed();
        }
    }

    private void showToast(String text) {
        Toast.makeText(MainActivity.this, text, Toast.LENGTH_SHORT).show();
    }

}
