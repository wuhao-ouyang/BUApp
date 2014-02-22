package martin.app.bitunion;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import martin.app.bitunion.fragment.ForumFragment;
import martin.app.bitunion.fragment.ThreadFragment;
import martin.app.bitunion.util.BUAppUtils;
import martin.app.bitunion.util.BUThread;
import martin.app.bitunion.util.PostMethod;
import martin.app.bitunion.util.BUAppUtils.Result;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.PagerTitleStrip;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.Toast;

public class DisplayActivity extends FragmentActivity {

	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a
	 * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
	 * will keep every loaded fragment in memory. If this becomes too memory
	 * intensive, it may be best to switch to a
	 * {@link android.support.v4.app.FragmentStatePagerAdapter}.
	 */
	ThreadsPagerAdapter mPagerAdapter;
	// MyPagerAdapter mPagerAdapter;

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	ViewPager mViewPager;
	PagerTitleStrip mPagerTitleStrip;
	View mReadingStatus;
	LayoutInflater inflater = null;
	ProgressDialog progressDialog = null;

	// ReadPageTask mReadPageTask;

	int forumId;
	String forumName;
	String session;

	SparseArray<ArrayList<BUThread>> pageList = new SparseArray<ArrayList<BUThread>>(); // 所有帖子列表
	// SparseArray<ForumFragment> fragmentList = new
	// SparseArray<ForumFragment>();
	SparseBooleanArray pReqFlags = new SparseBooleanArray(); // 是否正在读取该页帖子列表
	Deque<Integer> reqDeck = new ArrayDeque<Integer>();
	Result netStatus = null; // 网络连接返回结果
	int currentpage = 0;
	int refreshCnt = 2; // 刷新session最大次数
	boolean refreshFlag = false; // 是否正在刷新session
	boolean refreshingCurrentPage = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_display);

		Intent intent = getIntent();
		forumId = intent.getIntExtra("fid", 27);
		forumName = intent.getStringExtra("name");
		session = MainActivity.settings.mSession;

		// Show the Up button in the action bar.
		getActionBar().setTitle(forumName.replace("-- ", ""));
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setDisplayShowHomeEnabled(false);

		// mPagerAdapter = new MyPagerAdapter();
		mPagerAdapter = new ThreadsPagerAdapter(getSupportFragmentManager());

		inflater = LayoutInflater.from(DisplayActivity.this);
		mReadingStatus = inflater.inflate(R.layout.processing_display, null);

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.viewpager);
		mPagerTitleStrip = (PagerTitleStrip) findViewById(R.id.pager_title_strip);
		mPagerTitleStrip.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
		// mPagerTabStrip = (PagerTabStrip) findViewById(R.id.pager_tab_strip);
		mViewPager.setAdapter(mPagerAdapter);
		mViewPager.setOnPageChangeListener(new MyOnPageChangeListener());
		mViewPager.setOnTouchListener(new MyOnTouchListener());
		
		progressDialog = new ProgressDialog(this, R.style.ProgressDialog);
		progressDialog.setMessage("读取中...");
		progressDialog.show();
		
//		showProgress(true);
		
		readPage(0);
		readPage(1);
//		currenpage = 2;
//		if (mViewPager.getCurrentItem() != currenpage)
//			mViewPager.setCurrentItem(currenpage);

	}

	public void readPage(int page) {
		if (!pReqFlags.get(page)) {
			pReqFlags.put(page, true);
			new ReadPageTask().execute(page);
		}
	}

	private void refreshCurrentPage() {
		progressDialog.setMessage("刷新中...");
		progressDialog.show();
		readPage(currentpage);
		refreshingCurrentPage = true;
		Log.v("displayActivity", "refreshCurrentPage");
		// update current page view
//		mViewPager.addView(mPagerAdapter.getFragment(currenpage).getView(), currenpage, null);
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
			return true;
		case R.id.action_refresh:
			refreshCurrentPage();
			return true;
		case R.id.action_newthread:
			Intent intent = new Intent(DisplayActivity.this, NewthreadActivity.class);
			intent.putExtra("action", "newthread");
			intent.putExtra("forumname", forumName);
			intent.putExtra("fid", forumId);
			startActivity(intent);
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * A {@link FragmentStatePagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 */
	public class ThreadsPagerAdapter extends FragmentStatePagerAdapter {
		
		SparseArray<ForumFragment> registeredFragments = new SparseArray<ForumFragment>();

		public ThreadsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
//			Log.v("adapter", "getItem>>"+position);
			if (registeredFragments.get(position) == null)
				registeredFragments.put(position, new ForumFragment());
			Bundle args = new Bundle();
			ArrayList<String> threadlist = new ArrayList<String>();
			if (pageList.get(position) != null)
				for (BUThread thread : pageList.get(position))
					threadlist.add(thread.toString());
			args.putStringArrayList("threadlist", threadlist);
			args.putInt(ForumFragment.ARG_PAGE_NUMBER, position);
			args.putInt("fid", forumId);
			registeredFragments.get(position).setArguments(args);
			return registeredFragments.get(position);
		}

		@Override
		public int getCount() {
			return pageList.size();
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
		public ForumFragment getFragment(int position){
			return registeredFragments.get(position);
		}
//		@Override
//		public int getItemPosition(Object object) {
//			ForumFragment fragment = (ForumFragment) object;
//			 int position = registeredFragments.indexOfValue(fragment);
//			if (position >= 0 )
//				return position;
//			else
//				return POSITION_NONE;
//		}
	}
	
	class MyOnTouchListener implements OnTouchListener{

		double lastx = -1;
		long lastswipetime = 0;
		boolean swipetrig = false;
		
		@Override
		public boolean onTouch(View v, MotionEvent motion) {
			if (currentpage == 0)
				switch (motion.getAction()) {
				case MotionEvent.ACTION_MOVE:
					int dpMoved = 0;
					if (lastx != -1)
						dpMoved = BUAppUtils.px2dip(getApplication(),
								(float) (motion.getX() - lastx));
					lastx = motion.getX();
					if (dpMoved > 24)
						swipetrig = true;
					break;
				case MotionEvent.ACTION_UP:
					lastx = -1;
					if (swipetrig) {
						if ((System.currentTimeMillis() - lastswipetime) >= BUAppUtils.EXIT_WAIT_TIME) {
							showToast("再次右滑返回");
							lastswipetime = System.currentTimeMillis();
						} else
							finish();
						swipetrig = false;
					}
					break;
				default:
				}
			return false;
		}
	}

	class MyOnPageChangeListener implements OnPageChangeListener {

		private int position;
		private float percent;

		@Override
		public void onPageScrollStateChanged(int state) {
			// Log.v("onPageScrollStateChanged", "--state-->>>" + state);
			if (state == ViewPager.SCROLL_STATE_DRAGGING) {
				switch (position) {
				default:
					if (pageList.get(position - 2) == null
							|| pageList.get(position - 2).isEmpty())
						if (!pReqFlags.get(position - 2)) {
							readPage(position - 2);
						}
				case 1:
					if (pageList.get(position - 1) == null
							|| pageList.get(position - 1).isEmpty())
						if (!pReqFlags.get(position - 1)) {
							readPage(position - 1);
						}
				case 0:
				}
				if (pageList.get(position + 2) == null
						|| pageList.get(position + 2).isEmpty())
					if (!pReqFlags.get(position + 2)) {
						readPage(position + 2);
					}
				if (pageList.get(position + 1) == null
						|| pageList.get(position + 1).isEmpty())
					if (!pReqFlags.get(position + 1)) {
						readPage(position + 1);
					}
			} else if (state == ViewPager.SCROLL_STATE_IDLE)
				currentpage = position;
		}

		@Override
		public void onPageScrolled(int pos, float per, int arg2) {
//			 Log.v("onPageScrolled", "--position-->>>" + pos);
//			 Log.v("onPageScrolled", "--percent-->>>" + percent);
//			 Log.v("onPageScrolled", "--arg2-->>>" + arg2);

			position = pos;
			percent = per;
		}

		@Override
		public void onPageSelected(int arg0) {
			// TODO Auto-generated method stub
			// Log.v("onPageSelected", "--arg0-->>>" + arg0);
		}
	}

	/**
	 * @author Martin Read content of certain page from server. After reading,
	 *         data will be put into list.
	 */
	public class ReadPageTask extends AsyncTask<Integer, Void, Result> {

		PostMethod postMethod = new PostMethod();
		JSONArray pageContent = new JSONArray();
		int page;

		@Override
		protected Result doInBackground(Integer... params) {

			int threadsRemain = BUAppUtils.THREADS_PER_PAGE;
			int from = params[0] * BUAppUtils.THREADS_PER_PAGE;
			int to;
			Result netStat = Result.SUCCESS;
			this.page = params[0];

			while (threadsRemain > 0 && netStat == Result.SUCCESS) {
				JSONObject postReq = new JSONObject();
				try {
					if (threadsRemain >= 20) {
						to = from + 20;
					} else {
						to = from + threadsRemain;
					}
					postReq.put("action", "thread");
					postReq.put("username", URLEncoder.encode(
							MainActivity.settings.mUsername, "utf-8"));
					postReq.put("session", MainActivity.settings.mSession);
					postReq.put("fid", forumId);
					postReq.put("from", from);
					postReq.put("to", to);
					postMethod.setNetType(MainActivity.settings.mNetType);
					netStat = postMethod.sendPost(postMethod.REQ_THREAD,
							postReq);
					if (netStat != Result.SUCCESS)
						return netStat;
						pageContent = BUAppUtils.mergeJSONArray(pageContent,
								postMethod.jsonResponse.getJSONArray("threadlist"));
					threadsRemain = threadsRemain - 20;
					from = from + 20;

				} catch (JSONException e) {
					e.printStackTrace();
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			}
			return netStat;
		}

		@Override
		protected void onPostExecute(Result result) {
			switch (result) {
			default:
				break;
			case FAILURE:
				// 如果读取失败尝试刷新session
				if (refreshCnt > 0 && !refreshFlag) {
					new UserLoginTask(this.page).execute();
				}
				break;
			case NETWRONG:
				showToast(BUAppUtils.NETWRONG);
				break;
			case SUCCESS:
				pageList.put(this.page,
						BUAppUtils.jsonToThreadlist(pageContent));
				mPagerAdapter.notifyDataSetChanged();
				if (refreshingCurrentPage == true){
					mPagerAdapter.getFragment(currentpage).update(pageList.get(currentpage));
					refreshingCurrentPage = false;
					}
				if (progressDialog.isShowing())
					progressDialog.dismiss();
			}
			pReqFlags.put(this.page, false);
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
			pReqFlags.put(this.page, false);
		}
	}

	/**
	 * Represents an asynchronous login/registration task used to authenticate
	 * the user.
	 */
	public class UserLoginTask extends AsyncTask<Void, Void, Result> {

		PostMethod postMethod = new PostMethod();
		int pageRequested = 0;
		
		public UserLoginTask(int page) {
			pageRequested = page;
		}

		@Override
		protected Result doInBackground(Void... params) {
			// 标记session正在刷新
			refreshFlag = true;
			JSONObject postReq = new JSONObject();
			try {
				postReq.put("action", "login");
				postReq.put("username", URLEncoder.encode(
						MainActivity.settings.mUsername, "utf-8"));
				postReq.put("password", MainActivity.settings.mPassword);
				postMethod.setNetType(MainActivity.settings.mNetType);
				return postMethod.sendPost(postMethod.REQ_LOGGING, postReq);
			} catch (JSONException e) {
				e.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(final Result result) {
			// 标记session刷新完毕
			refreshFlag = false;
			switch (result) {
			default:
				return;
			case FAILURE:
				// 再次尝试刷新session并且重置刷新次数计数器
				if (refreshCnt > 0 && !refreshFlag) {
					refreshCnt -= 1;
					new UserLoginTask(pageRequested).execute();
				} else
					showToast(BUAppUtils.LOGINFAIL);
				return;
			case NETWRONG:
				showToast(BUAppUtils.NETWRONG);
				return;
			case UNKNOWN:
				return;
			case SUCCESS:
			}

			try {
				MainActivity.settings.mSession = postMethod.jsonResponse
						.getString("session");
			} catch (JSONException e) {
				e.printStackTrace();
			}
			// Rest session refresh counter
			refreshCnt = 2;
			readPage(pageRequested);
			Log.i("DisplayActivity", "session refreshed");
		}

	}
	
	private void showToast(String text) {
		Toast.makeText(DisplayActivity.this, text, Toast.LENGTH_SHORT).show();
	}
}
