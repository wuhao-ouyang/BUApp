package martin.app.bitunion;

import java.util.ArrayList;
import java.util.List;

import martin.app.bitunion.fragment.ForumFragment;
import martin.app.bitunion.util.BUAppUtils;
import martin.app.bitunion.util.CommonIntents;
import martin.app.bitunion.widget.SwipeDetector;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
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
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.Toast;

public class DisplayActivity extends ActionBarActivity {

    private ThreadsPagerAdapter mPagerAdapter;
    private List<Integer> mPageList;
    // MyPagerAdapter mPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;
    private PagerTitleStrip mPagerTitleStrip;
    private int currentpage = 0;

    // ReadPageTask mReadPageTask;

    private int forumId;
    private String forumName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display);

        Intent intent = getIntent();
        forumId = intent.getIntExtra(CommonIntents.EXTRA_FID, 27);
        forumName = intent.getStringExtra(CommonIntents.EXTRA_FORUM_NAME);

        if (savedInstanceState != null && !savedInstanceState.isEmpty()) {
            forumId = savedInstanceState.getInt("fid");
            forumName = savedInstanceState.getString("name");
        }

        // Show the Up button in the action bar.
        getSupportActionBar().setTitle(forumName.replace("-- ", ""));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(false);

        // Pre read 2 pages
        mPageList = new ArrayList<Integer>();
        mPageList.add(0);
        mPageList.add(0);
        mPagerAdapter = new ThreadsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        mPagerTitleStrip = (PagerTitleStrip) findViewById(R.id.pager_title_strip);
        mPagerTitleStrip.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        mPagerTitleStrip.setBackgroundResource(R.color.blue_dark);
        // mPagerTabStrip = (PagerTabStrip) findViewById(R.id.pager_tab_strip);
        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.setOnPageChangeListener(new MyOnPageChangeListener());
        int trigger = getResources().getDimensionPixelSize(R.dimen.swipe_trigger_limit);
        mViewPager.setOnTouchListener(new SwipeDetector(trigger, new MySwipeListener()));

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // TODO states needs checked
        outState.putInt("fid", forumId);
        outState.putString("name", forumName);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.display, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.action_refresh:
                mPagerAdapter.notifyRefresh(currentpage);
                break;
            case R.id.action_newthread:
                Intent intent = new Intent(DisplayActivity.this, NewthreadActivity.class);
                intent.putExtra(CommonIntents.EXTRA_ACTION, NewthreadActivity.ACTION_NEW_THREAD);
                intent.putExtra(CommonIntents.EXTRA_FORUM_NAME, forumName);
                intent.putExtra(CommonIntents.EXTRA_FID, forumId);
                startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A {@link FragmentStatePagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    private class ThreadsPagerAdapter extends FragmentStatePagerAdapter {

        private SparseArray<ForumFragment> registeredFragments = new SparseArray<ForumFragment>();

        public ThreadsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
//			Log.v("adapter", "getItem>>"+position);
            Bundle args = new Bundle();
            if (mPageList.get(position) != null) {
                args.putInt(ForumFragment.ARG_PAGE_NUMBER, position);
                args.putInt("fid", forumId);
            }
            ForumFragment frag = registeredFragments.get(position);
            if (frag == null) {
                frag = new ForumFragment();
                frag.setArguments(args);
                registeredFragments.put(position, frag);
            } else if (!frag.isUpdating())
                frag.onRefresh();
            return frag;
        }

        @Override
        public int getCount() {
            return mPageList.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return Integer.toString(position + 1);
        }
        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            ForumFragment fragment = (ForumFragment) super.instantiateItem(container, position);
            registeredFragments.put(position, fragment);
            return fragment;
        }
        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            registeredFragments.remove(position);
            super.destroyItem(container, position, object);
        }

        public void notifyRefresh(int page) {
            ForumFragment frag = registeredFragments.get(page);
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
                if ((System.currentTimeMillis() - lastswipetime) >= BUAppUtils.EXIT_WAIT_TIME) {
                    showToast("再次右滑返回");
                    lastswipetime = System.currentTimeMillis();
                } else
                    finish();
            }
        }
    }

    private class MyOnPageChangeListener implements OnPageChangeListener {

        @Override
        public void onPageScrollStateChanged(int state) {
        }

        @Override
        public void onPageScrolled(int pos, float per, int arg2) {
        }

        @Override
        public void onPageSelected(int position) {
            currentpage = position;
            while (position > mPageList.size() - 2)
                mPageList.add(0);
            mPagerAdapter.notifyDataSetChanged();
        }
    }

    private void showToast(String text) {
        Toast.makeText(DisplayActivity.this, text, Toast.LENGTH_SHORT).show();
    }
}
