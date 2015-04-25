package martin.app.bitunion;

import java.util.ArrayList;

import org.json.JSONObject;

import martin.app.bitunion.fragment.ThreadFragment;
import martin.app.bitunion.fragment.UserInfoDialogFragment;
import martin.app.bitunion.util.BUApi;
import martin.app.bitunion.util.Devices;
import martin.app.bitunion.util.Settings;
import martin.app.bitunion.util.ToastUtil;
import martin.app.bitunion.util.Utils;
import martin.app.bitunion.model.BUPost;
import martin.app.bitunion.util.CommonIntents;
import martin.app.bitunion.widget.SwipeDetector;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerTitleStrip;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

import com.android.volley.Response;
import com.android.volley.VolleyError;

public class ThreadActivity extends ActionBarActivity implements View.OnClickListener, ThreadFragment.ThreadContentListener {

    private ThreadPagerAdapter mThreadAdapter;

    private ViewPager mViewPager;
    private PagerTitleStrip mPagerTitleStrip;
    private ViewGroup replyContainer = null;
    private EditText replyMessage = null;
    private View mReplyBtn;

    private int threadId;
    private String threadName;

    private ArrayList<Integer> postList = new ArrayList<Integer>(); // 所有回复列表
    private int lastpage, replies = 1; // 当前帖子总页数，总回复数
    private int currentpage = 0; // 当前所在页数

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thread);

        Intent intent = getIntent();
        threadId = intent.getIntExtra(CommonIntents.EXTRA_TID, 0);
        threadName = intent.getStringExtra(CommonIntents.EXTRA_THREAD_NAME);
        replies = intent.getIntExtra(CommonIntents.EXTRA_REPIES, 1);
        onEndReached();

        if (savedInstanceState != null && !savedInstanceState.isEmpty()) {
            threadId = savedInstanceState.getInt("tid");
            threadName = savedInstanceState.getString("subject");
            replies = savedInstanceState.getInt("replies");
        }
        calculateTotalPage();
        Log.v("ThreadActivity", "lastpage>>>>>" + lastpage);

        // Setup the action bar.
        getSupportActionBar().setTitle(threadName);
        getSupportActionBar().setDisplayShowHomeEnabled(false);

        // Get reply View container and hide for current
        replyContainer = (ViewGroup) findViewById(R.id.reply_layout);
        replyContainer.setVisibility(View.GONE);
        replyMessage = (EditText) replyContainer.findViewById(R.id.reply_message);
        // Button calls reply window to front
        View replySubmit = replyContainer.findViewById(R.id.reply_submit);
        replySubmit.setOnClickListener(this);
        mReplyBtn = findViewById(R.id.imgVw_reply_btn);
        mReplyBtn.setOnClickListener(this);
        ImageButton advreply = (ImageButton) replyContainer.findViewById(R.id.reply_advanced);
        advreply.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ThreadActivity.this, NewthreadActivity.class);
                intent.putExtra(CommonIntents.EXTRA_ACTION, NewthreadActivity.ACTION_NEW_POST);
                intent.putExtra(CommonIntents.EXTRA_TID, threadId);
                intent.putExtra(CommonIntents.EXTRA_MESSAGE, replyMessage.getText().toString());
                startActivity(intent);

            }
        });

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the application.
        mThreadAdapter = new ThreadPagerAdapter(getSupportFragmentManager());

        // Setup the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.viewpager_thread);
        mPagerTitleStrip = (PagerTitleStrip) findViewById(R.id.pager_title_strip_thread);
        mPagerTitleStrip.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        mViewPager.setAdapter(mThreadAdapter);
        mViewPager.setOnPageChangeListener(new MyOnPageChangeListener());
        int trigger = getResources().getDimensionPixelSize(R.dimen.swipe_trigger_limit);
        mViewPager.setOnTouchListener(new SwipeDetector(trigger, new MySwipeListener()));

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // TODO Data needs to be stored
        outState.putInt("tid", threadId);
        outState.putString("subject", threadName);
        outState.putInt("replies", replies);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.thread, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_refresh:
                mThreadAdapter.notifyRefresh(currentpage);
                return true;
            case R.id.action_reply:
                toggleReplyBox(!isReplyBoxShowing());
                return true;
            case R.id.action_sharelink:
                StringBuilder sb = new StringBuilder(threadName);
                sb.append('\n');
                sb.append(BUApi.getRootUrl());
                sb.append("/viewthread.php?tid=");
                sb.append(threadId);

                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData.Item cliptext = new ClipData.Item(sb.toString());
                String[] mime = new String[1];
                mime[0] = "text/plain";
                ClipData clip = new ClipData("帖子链接", mime, cliptext);
                clipboard.setPrimaryClip(clip);
                ToastUtil.showToast(R.string.thread_url_copied);
                return true;
        }
        return false;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imgVw_reply_btn:
                mReplyBtn.setEnabled(false);
                break;
            case R.id.reply_submit:
                final String message = replyMessage.getText().toString();
                if (!message.isEmpty()) {
                    final DialogInterface.OnClickListener clickListener = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (which == DialogInterface.BUTTON_POSITIVE) {
                                ToastUtil.showToast(R.string.message_sending);
                                Log.v("", "Reply submitted>>" + message);
                                StringBuilder finalMsg = new StringBuilder(message);
                                if (BUApp.settings.showSignature)
                                    finalMsg.append(getString(R.string.buapp_client_postfix).replace("$device_name", Devices.getDeviceName()));
                                BUApi.postNewPost(threadId, finalMsg.toString(), new Response.Listener<JSONObject>() {
                                    @Override
                                    public void onResponse(JSONObject jsonObject) {
                                        if (BUApi.getResult(jsonObject) == BUApi.Result.SUCCESS) {
                                            ToastUtil.showToast(R.string.message_sent_success);
                                            replyMessage.setText("");
                                        } else {
                                            // TODO need to handle error
                                        }
                                    }
                                }, new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError volleyError) {
                                        ToastUtil.showToast(R.string.network_unknown);
                                    }
                                });
                            } else {

                            }
                        }
                    };
                    new AlertDialog.Builder(ThreadActivity.this)
                            .setTitle(R.string.send_message_title)
                            .setMessage(R.string.send_message_message)
                            .setPositiveButton(R.string.dialog_button_confirm, clickListener)
                            .setNegativeButton(R.string.dialog_button_cancel, clickListener)
                            .create().show();
                } else
                    ToastUtil.showToast(R.string.message_cant_be_empty);
                break;
        }
    }

    private boolean isReplyBoxShowing() {
        return replyContainer.getVisibility() == View.VISIBLE;
    }

    private void toggleReplyBox(boolean show) {
        if (show) {
            replyContainer.setVisibility(View.VISIBLE);
        } else {
            replyContainer.setVisibility(View.GONE);
        }
    }

    @Override
    public void onQuoteClick(BUPost post) {
        if (replyContainer.getVisibility() == View.GONE)
            replyContainer.setVisibility(View.VISIBLE);
        replyMessage.setText(replyMessage.getText().toString() + post.toQuote());
        replyMessage.setSelection(replyMessage.getText().toString().length());
    }

    @Override
    public void onUserClick(int uid) {
        UserInfoDialogFragment infoDialog = new UserInfoDialogFragment();
        Bundle args = new Bundle();
        args.putInt(CommonIntents.EXTRA_UID, uid);
        infoDialog.setArguments(args);
        infoDialog.show(getSupportFragmentManager(), "Userinfo-" + uid);
    }

    @Override
    public void onSubjectUpdated(@Nullable BUPost subject) {
        getSupportActionBar().setTitle(subject.getSubject());
    }

    @Override
    public void onEndReached() {
        BUApi.checkTotalPosts(threadId, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                if (BUApi.getResult(response) == BUApi.Result.SUCCESS) {
                    replies = response.optInt("tid_sum", replies);
                    calculateTotalPage();
                }
            }
        }, BUApi.sErrorListener);
    }

    private void calculateTotalPage() {
        if (replies % Settings.POSTS_PER_PAGE == 0)
            lastpage = replies / Settings.POSTS_PER_PAGE - 1;
        else
            lastpage = replies / Settings.POSTS_PER_PAGE;
        while (postList.size() <= lastpage)
            postList.add(0);
        if (mThreadAdapter != null)
            mThreadAdapter.notifyDataSetChanged();
    }

    @Override
    public void onBackPressed() {
        if (isReplyBoxShowing()) {
            toggleReplyBox(false);
            return;
        } else
            super.onBackPressed();
    }

    public class ThreadPagerAdapter extends FragmentStatePagerAdapter {

        private SparseArray<ThreadFragment> registeredFragments = new SparseArray<ThreadFragment>();

        public ThreadPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            ThreadFragment frag = registeredFragments.get(position);
            if (frag == null) {
                frag = new ThreadFragment();
                Bundle args = new Bundle();
                args.putInt(ThreadFragment.ARG_THREAD_ID, threadId);
                args.putInt(ThreadFragment.ARG_PAGE_NUMBER, position);
                frag.setArguments(args);
                registeredFragments.put(position, frag);
                Log.d("PagerAdapter", "Fragment create>>" + position);
            } else if (!frag.isUpdating()){
                frag.onRefresh();
            }
            frag.setThreadContentListener(ThreadActivity.this);
            return frag;
        }

        @Override
        public int getCount() {
            return postList.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return Integer.toString(position + 1);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            ThreadFragment fragment = (ThreadFragment) super.instantiateItem(
                    container, position);
            registeredFragments.put(position, fragment);
            return fragment;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
			registeredFragments.remove(position);
            super.destroyItem(container, position, object);
        }

        public void notifyRefresh(int page) {
            ThreadFragment frag = registeredFragments.get(page);
            if (frag != null && !frag.isUpdating())
                frag.onRefresh();
        }

    }

    private long lastswipetime = 0;
    /**
     * Listener to swipe activity. Double one-direction swiping will kill
     * current activity thus lead to upper level of this application.
     */
    private class MySwipeListener implements SwipeDetector.SwipeListener {

        @Override
        public void onSwiped(int swipeAction) {
            if (swipeAction == SwipeDetector.SWIPE_RIGHT && currentpage == 0) {
                if ((System.currentTimeMillis() - lastswipetime) >= Utils.EXIT_WAIT_TIME) {
                    ToastUtil.showToast(R.string.swipe_right_go_back);
                    lastswipetime = System.currentTimeMillis();
                } else
                    finish();
            }
        }
    }

    /**
     * Listen to the {@link ViewPager}, pre-load pages will be useful next to
     * current view.
     */
    private class MyOnPageChangeListener implements OnPageChangeListener {

        @Override
        public void onPageScrollStateChanged(int state) {
        }

        @Override
        public void onPageScrolled(int pos, float per, int arg2) {
        }

        @Override
        public void onPageSelected(int pos) {
            currentpage = pos;
        }
    }
}
