package martin.app.bitunion;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import org.json.JSONException;
import org.json.JSONObject;

import martin.app.bitunion.util.BUAppSettings;
import martin.app.bitunion.util.BUAppUtils;
import martin.app.bitunion.util.BUAppUtils.Result;
import martin.app.bitunion.model.BUForum;
import martin.app.bitunion.util.PostMethod;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

    public static int SCREENWIDTH;
    public static int SCREENHEIGHT;
    public static float PIXDENSITY;


    // 论坛列表视图
    ExpandableListView listView;
    // 单组论坛列表数据
    ArrayList<BUForum> forumList = new ArrayList<BUForum>();
    // 所有论坛列表数据
    ArrayList<ArrayList<BUForum>> fArrayList = new ArrayList<ArrayList<BUForum>>();
    // 分组数据
    ArrayList<String> groupList;
    ForumListAdapter adapter;

    // 静态变量在整个应用中传递网络连接参数，包括session, username, password信息
    public static BUAppSettings settings = new BUAppSettings();
    // 上次按返回键的时间
    long touchTime = 0;

    private UserLoginTask mLoginTask = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        setContentView(R.layout.activity_main);

        getActionBar().setTitle("北理FTP联盟");

        Point size = new Point();
        WindowManager w = getWindowManager();

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)    {
            w.getDefaultDisplay().getSize(size);
            SCREENWIDTH = size.x;
            SCREENHEIGHT = size.y;
        }else{
            Display d = w.getDefaultDisplay();
            SCREENWIDTH = d.getWidth();
            SCREENHEIGHT = d.getHeight();
        }
        PIXDENSITY = getResources().getDisplayMetrics().densityDpi;


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

        readConfig();
        if (settings.mUsername != null && !settings.mUsername.isEmpty()) {
            mLoginTask = new UserLoginTask();
            mLoginTask.execute((Void) null);
        }
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    private class UserLoginTask extends AsyncTask<Void, Void, Result> {

        PostMethod postMethod = new PostMethod();

        @Override
        protected Result doInBackground(Void... params) {

            JSONObject postReq = new JSONObject();
            try {
                postReq.put("action", "login");
                postReq.put("username",
                        URLEncoder.encode(settings.mUsername, "utf-8"));
                postReq.put("password", settings.mPassword);
                return postMethod.sendPost(BUAppUtils.getUrl(settings.mNetType, BUAppUtils.REQ_LOGGING), postReq);
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            return null;
        }

        // 处理登录结果并弹出toast显示
        @Override
        protected void onPostExecute(final Result result) {
            mLoginTask = null;

            switch (result) {
                default:
                    return;
                case FAILURE:
                    showToast(BUAppUtils.LOGINFAIL);
                    return;
                case NETWRONG:
                    showToast(BUAppUtils.NETWRONG);
                    return;
                case UNKNOWN:
                    return;
                case SUCCESS:
            }
            showToast(BUAppUtils.USERNAME + " " + settings.mUsername + " "
                    + BUAppUtils.LOGINSUCCESS);
            try {
                settings.mSession = postMethod.jsonResponse.getString("session");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            // finish();
        }

        @Override
        protected void onCancelled() {
            mLoginTask = null;
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        // 主要为login_activity结束后更新数据用
        Log.v("MainActivity", "Cookie>>" + settings.mSession);
    }

    @Override
    protected void onStop() {
        SharedPreferences config = getSharedPreferences("config", MODE_PRIVATE);
        SharedPreferences.Editor editor = config.edit();
        editor.putInt("nettype", settings.mNetType);
        editor.commit();
        super.onStop();
    }

    private void readConfig() {
        SharedPreferences config = getSharedPreferences("config", MODE_PRIVATE);
        settings.mNetType = config.getInt("nettype", BUAppUtils.OUTNET);
        settings.mUsername = config.getString("username", null);
        settings.mPassword = config.getString("password", null);
        settings.titletextsize = config.getInt("titletextsize", (PIXDENSITY > DisplayMetrics.DENSITY_HIGH)? 14 : 12);
        settings.contenttextsize = config.getInt("contenttextsize", (PIXDENSITY > DisplayMetrics.DENSITY_HIGH)? 14 : 12);
        settings.showsigature = config.getBoolean("showsigature", true);
        settings.showimage = config.getBoolean("showimage", true);
        settings.referenceat = config.getBoolean("referenceat", false);
        settings.setNetType(settings.mNetType);
        Log.i("MainActivity", "readConfig>>Settings loaded!");
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
                if (settings.mUsername == null || settings.mUsername.isEmpty()) {
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

    // 得到login_activity返回的cookies
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == BUAppUtils.MAIN_RESULT
                && requestCode == BUAppUtils.MAIN_REQ) {
            readConfig();
            settings.mSession = data.getStringExtra("session");
            showToast(BUAppUtils.USERNAME + " " + settings.mUsername + " "
                    + BUAppUtils.LOGINSUCCESS);
        }
    }

    // ExpandableListView的数据接口
    private class ForumListAdapter extends BaseExpandableListAdapter {

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            return fArrayList.get(groupPosition).get(childPosition);
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition,
                                 boolean isLastChild, View convertView, ViewGroup parent) {
            TextView textView = new TextView(MainActivity.this);
            textView.setBackgroundColor(getResources().getColor(
                    R.color.blue_light));
            if (fArrayList.get(groupPosition).get(childPosition).getName().contains("--"))
                textView.setTextSize(settings.titletextsize + 2);
            else
                textView.setTextSize(settings.titletextsize + 2 + 4);
            textView.setPadding(60, 10, 0, 10);
            textView.setText(fArrayList.get(groupPosition).get(childPosition)
                    .getName());
            textView.setTag(fArrayList.get(groupPosition).get(childPosition));
            // Log.v("martin", Integer.toString(textView.getId()));
            // 触摸点击反馈，按下时变色，上下滑动或松开则恢复
            textView.setOnTouchListener(new OnTouchListener() {

                double y;

                @Override
                public boolean onTouch(View v, MotionEvent motion) {
                    if (motion.getAction() == MotionEvent.ACTION_DOWN) {
                        v.setBackgroundColor(getResources().getColor(
                                R.color.blue_view_selected));
                        y = motion.getY();
                    } else if (motion.getAction() == MotionEvent.ACTION_UP
                            || motion.getAction() == MotionEvent.ACTION_CANCEL) {
                        v.setBackgroundColor(getResources().getColor(
                                R.color.blue_light));
                        return false;
                    } else if (motion.getAction() == MotionEvent.ACTION_MOVE) {
                        double disMoved;
                        disMoved = Math.abs(y - motion.getY());
                        if (disMoved > 5) {
                            v.setBackgroundColor(getResources().getColor(
                                    R.color.blue_light));
                        }
                    }
                    return false;
                }
            });
            // 注册OnClick事件，触摸点击转至DisplayActivity
            textView.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (settings.mUsername != null && settings.mPassword != null) {
                        if ( ((BUForum) v.getTag()).getFid() == -1){
                            // TODO 最新帖子
                            return;}
                        if ( ((BUForum) v.getTag()).getFid() == -2){
                            // TODO 收藏夹
                            return;}
                        Intent intent = new Intent(MainActivity.this,
                                DisplayActivity.class);
                        intent.putExtra("fid", ((BUForum) v.getTag()).getFid());
                        intent.putExtra("name",
                                ((BUForum) v.getTag()).getName());
                        startActivityForResult(intent, BUAppUtils.MAIN_REQ);
                    } else showToast("请先登录");
                }
            });
            return textView;
        }

        @Override
        public int getChildrenCount(int groupPosition) {
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
            final TextView textView = new TextView(MainActivity.this);
            textView.setBackgroundColor(getResources().getColor(
                    R.color.blue_dark));
            textView.setTextSize(settings.titletextsize + 2 + 4 + 5);
            textView.setPadding(60, 10, 0, 10);
            textView.setText(groupList.get(groupPosition));
            return textView;
        }
        @Override
        public boolean hasStableIds() {
            return false;
        }
        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return false;
        }

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
