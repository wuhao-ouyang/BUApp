package martin.app.bitunion;

import android.content.Intent;
import android.os.Bundle;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;
import martin.app.bitunion.fragment.ForumFragment;
import martin.app.bitunion.util.CommonIntents;
import martin.app.bitunion.util.ToastUtil;
import martin.app.bitunion.util.Utils;
import martin.app.bitunion.widget.SwipeDetector;

public class DisplayActivity extends BaseContentActivity {

    private ThreadsPagerAdapter mPagerAdapter;
    private int mTotalPage;
    // MyPagerAdapter mPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;
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
            forumName = forumName.replace("--", "");
        }

        // Show the Up button in the action bar.
        getSupportActionBar().setTitle(String.format("%s %d", forumName, currentpage+1));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(false);

        // Pre read 3 pages
        mTotalPage = 3;
        mPagerAdapter = new ThreadsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        // mPagerTabStrip = (PagerTabStrip) findViewById(R.id.pager_tab_strip);
        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.addOnPageChangeListener(new MyOnPageChangeListener());
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
            if (position < mTotalPage) {
                args.putInt(ForumFragment.ARG_PAGE, position);
                args.putInt(ForumFragment.ARG_FID, forumId);
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
            return mTotalPage;
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
                if ((System.currentTimeMillis() - lastswipetime) >= Utils.EXIT_WAIT_TIME) {
                    ToastUtil.showToast(R.string.swipe_right_go_back);
                    lastswipetime = System.currentTimeMillis();
                } else
                    finish();
            }
        }
    }

    private class MyOnPageChangeListener implements ViewPager.OnPageChangeListener {

        @Override
        public void onPageScrollStateChanged(int state) {
        }

        @Override
        public void onPageScrolled(int pos, float per, int arg2) {
        }

        @Override
        public void onPageSelected(int position) {
            currentpage = position;
            getSupportActionBar().setTitle(String.format("%s %d", forumName, currentpage+1));
            boolean added = false;
            while (mTotalPage - position < 2) {
                mTotalPage++;added = true;
            }
            if (added) {
                mPagerAdapter.notifyDataSetChanged();
            }
        }
    }
}
