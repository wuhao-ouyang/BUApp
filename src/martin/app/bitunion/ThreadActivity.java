package martin.app.bitunion;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import martin.app.bitunion.DisplayActivity.MyOnPageChangeListener;
import martin.app.bitunion.DisplayActivity.ReadPageTask;
import martin.app.bitunion.DisplayActivity.UserLoginTask;
import martin.app.bitunion.fragment.ForumFragment;
import martin.app.bitunion.fragment.ThreadFragment;
import martin.app.bitunion.util.BUAppUtils;
import martin.app.bitunion.util.BUPost;
import martin.app.bitunion.util.BUThread;
import martin.app.bitunion.util.PostMethod;
import martin.app.bitunion.util.BUAppUtils.Result;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.NavUtils;
import android.support.v4.view.PagerTitleStrip;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.View.OnTouchListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author Strider_oy
 * 
 */
public class ThreadActivity extends FragmentActivity {

	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a
	 * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
	 * will keep every loaded fragment in memory. If this becomes too memory
	 * intensive, it may be best to switch to a
	 * {@link android.support.v4.app.FragmentStatePagerAdapter}.
	 */
	ThreadPagerAdapter mThreadAdapter;

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	ViewPager mViewPager;
	PagerTitleStrip mPagerTitleStrip;
	View mReadingStatus;
	LayoutInflater inflater = null;
	ProgressDialog progressDialog = null;
	LinearLayout replyContainer = null;
	EditText replyMessage = null;
	/**
	 * Marking if current page is refreshing, used to notify the activity to
	 * update its view after having new data ready.
	 */
	boolean refreshingCurrentPage = false;

	String threadId;
	String threadName;

	/**
	 * Posts list of current thread, including all pages even not been
	 * initialized in ViewPager.
	 */
	SparseArray<ArrayList<BUPost>> postList = new SparseArray<ArrayList<BUPost>>(); // 所有回复列表
	/**
	 * Flag indicates whether the page is requesting or not
	 */
	SparseBooleanArray tReqFlags = new SparseBooleanArray(); // 是否正在读取该页回复列表
	int lastpage, replies; // 当前帖子总页数，总回复数
	int currentpage = 0; // 当前所在页数
	/**
	 * Maximum refresh attempts of session
	 */
	int refreshCnt = 2; // 刷新session最大次数
	/**
	 * Flag indicates if session refreshing task is running
	 */
	boolean refreshFlag = false; // session是否正在刷新

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_thread);

		Intent intent = getIntent();
		threadId = intent.getStringExtra("tid");
		threadName = intent.getStringExtra("subject");
		replies = Integer.parseInt(intent.getStringExtra("replies")) + 1;
		if (replies % BUAppUtils.POSTS_PER_PAGE == 0)
			lastpage = replies / BUAppUtils.POSTS_PER_PAGE - 1;
		else
			lastpage = replies / BUAppUtils.POSTS_PER_PAGE;
		Log.v("ThreadActivity", "lastpage>>>>>" + lastpage);

		// Show the Up button in the action bar.
		getActionBar().setTitle(threadName);
		getActionBar().setDisplayShowHomeEnabled(false);
		
		// Get reply View container and hide for current
		replyContainer = (LinearLayout) findViewById(R.id.reply_layout);
		replyContainer.setVisibility(View.GONE);
		replyMessage = (EditText) replyContainer.findViewById(R.id.reply_message);
		ImageButton replySubmit = (ImageButton) replyContainer.findViewById(R.id.reply_submit);
		replySubmit.setOnClickListener(new MyReplySubmitListener());

		// Create the adapter that will return a fragment for each of the three
		// primary sections of the app.
		mThreadAdapter = new ThreadPagerAdapter(getSupportFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.viewpager_thread);
		mPagerTitleStrip = (PagerTitleStrip) findViewById(R.id.pager_title_strip_thread);
		mPagerTitleStrip.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
		mViewPager.setAdapter(mThreadAdapter);
		mViewPager.setOnPageChangeListener(new MyOnPageChangeListener());
		mViewPager.setOnTouchListener(new MyOnTouchListener());

		// Progress dialog indicating reading process
		progressDialog = new ProgressDialog(this, R.style.ProgressDialog);
		progressDialog.setMessage("读取中...");
		progressDialog.show();

//		if (intent.getBooleanExtra("new", false)) {
//			currentpage = lastpage;
//			readThreadPage(currentpage);
//			readThreadPage(currentpage - 1);
//		} else {
		readThreadPage(0);
		readThreadPage(1);
//		}

	}

	/**
	 * Open a new thread to requesting data from server
	 * @param page	The page you want to request
	 */
	public void readThreadPage(int page) {
		if (page <= lastpage && page >= 0)
			new ReadThreadPageTask().execute(page);
	}

	/**
	 * Check if there are new posts, if positive, ask for new data and refresh
	 * current page
	 */
	private void refreshCurrentPage() {
		progressDialog.setMessage("刷新中...");
		progressDialog.show();
		// 请求当前帖子总楼数
		new RefreshingTask().execute();
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
			refreshCurrentPage();
			return true;
		case R.id.action_post:
			if (!replyContainer.isShown())
				replyContainer.setVisibility(View.VISIBLE);
			else
				replyContainer.setVisibility(View.GONE);
//			Log.v("ThreadActivity", "action_post>>"+replyContainer.isShown());
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void onBackPressed() {
		if (replyContainer.isShown()) {
			replyContainer.setVisibility(View.GONE);
			return;
		} else
			super.onBackPressed();
	}

	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 */
	public class ThreadPagerAdapter extends FragmentPagerAdapter {

		SparseArray<ThreadFragment> registeredFragments = new SparseArray<ThreadFragment>();

		public ThreadPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			if (registeredFragments.get(position) == null)
				registeredFragments.put(position, new ThreadFragment());
			Bundle args = new Bundle();
			ArrayList<String> singlepagelist = new ArrayList<String>();
			if (postList.get(position) != null)
				for (BUPost post : postList.get(position))
					singlepagelist.add(post.toString());
			args.putStringArrayList("singlepagelist", singlepagelist);
			args.putInt(ThreadFragment.ARG_PAGE_NUMBER, position);
			registeredFragments.get(position).setArguments(args);
			return registeredFragments.get(position);
		}

		@Override
		public int getCount() {
			if (postList.size() == 0)
				return 0;
			else
				return postList.keyAt(postList.size() - 1) + 1;
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

		public ThreadFragment getFragment(int position) {
			return registeredFragments.get(position);
		}
	}

	class MyOnTouchListener implements OnTouchListener {

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
					if (postList.get(position - 2) == null
							|| postList.get(position - 2).isEmpty())
						if (!tReqFlags.get(position - 2)) {
							readThreadPage(position - 2);
						}
				case 1:
					if (postList.get(position - 1) == null
							|| postList.get(position - 1).isEmpty())
						if (!tReqFlags.get(position - 1)) {
							readThreadPage(position - 1);
						}
				case 0:
				}
				switch (lastpage - position) {
				default:
					if (postList.get(position + 2) == null
							|| postList.get(position + 2).isEmpty())
						if (!tReqFlags.get(position + 2)) {
							readThreadPage(position + 2);
						}
				case 1: // 当前页面是倒数第二页
					if (postList.get(position + 1) == null
							|| postList.get(position + 1).isEmpty())
						if (!tReqFlags.get(position + 1)) {
							readThreadPage(position + 1);
						}
				case 0: // 当前页是最后一页
				}
			} else if (state == ViewPager.SCROLL_STATE_IDLE)
				currentpage = position;
		}

		@Override
		public void onPageScrolled(int pos, float per, int arg2) {
			position = pos;
			percent = per;
		}

		@Override
		public void onPageSelected(int arg0) {
		}
	}

	/**
	 * Read content of certain page from server. After reading, data will be put
	 * into list.
	 */
	private class ReadThreadPageTask extends AsyncTask<Integer, Void, Result> {

		PostMethod postMethod = new PostMethod();
		JSONArray pageContent = new JSONArray();
		int page;

		@Override
		protected Result doInBackground(Integer... params) {

			this.page = params[0];
			int postsRemain;
			if (this.page != lastpage)
				postsRemain = BUAppUtils.POSTS_PER_PAGE;
			else
				postsRemain = replies % BUAppUtils.POSTS_PER_PAGE;
			int from = params[0] * BUAppUtils.POSTS_PER_PAGE;
			int to;
			Result netStat = Result.SUCCESS;
			tReqFlags.put(this.page, true);

			while (postsRemain > 0 && netStat == Result.SUCCESS) {
				JSONObject postReq = new JSONObject();
				try {
					if (postsRemain > 20) {
						to = from + 20;
					} else {
						to = from + postsRemain;
					}
					postReq.put("action", "post");
					postReq.put("username", URLEncoder.encode(
							MainActivity.network.mUsername, "utf-8"));
					postReq.put("session", MainActivity.network.mSession);
					postReq.put("tid", threadId);
					postReq.put("from", from);
					postReq.put("to", to);
					postMethod.setNetType(MainActivity.network.mNetType);
					netStat = postMethod.sendPost(postMethod.REQ_POST, postReq);
					if (netStat == Result.SUCCESS_EMPTY)
						break;
					if (postMethod.jsonResponse != null)
						pageContent = BUAppUtils.mergeJSONArray(pageContent,
								postMethod.jsonResponse
										.getJSONArray("postlist"));
					postsRemain = postsRemain - 20;
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
				if (refreshCnt > 0 && !refreshFlag) {
					new UserLoginTask(page).execute();
				}
				break;
			case NETWRONG:
				showToast(BUAppUtils.NETWRONG);
				break;
			case SUCCESS_EMPTY:
				Log.v("ThreadActivity", "success empty");
				break;
			case SUCCESS:
//				Log.v("ThreadActivity", "raw jsonArray>>" + pageContent);
				postList.put(this.page, BUAppUtils.jsonToPostlist(pageContent));
				Log.v("ThreadActivity", "Page loaded>>" + this.page);
				Log.v("ThreadActivity", "Post length>>" + pageContent.length());
				mThreadAdapter.notifyDataSetChanged();
//				mThreadAdapter.getFragment(this.page).update(postList.get(this.page));
				if (refreshingCurrentPage == true) {
					mThreadAdapter.getFragment(currentpage).update(
							postList.get(currentpage));
					refreshingCurrentPage = false;
				}
				if (progressDialog.isShowing())
					progressDialog.dismiss();
			}
			// showProgress(false);
			tReqFlags.put(this.page, false);
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
			tReqFlags.put(this.page, false);
		}
	}

	/**
	 * Represents an asynchronous login/registration task used to authenticate
	 * the user.
	 */
	private class UserLoginTask extends AsyncTask<Void, Void, Result> {

		PostMethod postMethod = new PostMethod();
		int pageRequested = 0;

		public UserLoginTask(int page) {
			this.pageRequested = page;
		}

		@Override
		protected Result doInBackground(Void... params) {
			// 标记session正在刷新
			refreshFlag = true;
			JSONObject postReq = new JSONObject();
			try {
				postReq.put("action", "login");
				postReq.put("username", URLEncoder.encode(
						MainActivity.network.mUsername, "utf-8"));
				postReq.put("password", MainActivity.network.mPassword);
				postMethod.setNetType(MainActivity.network.mNetType);
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
				MainActivity.network.mSession = postMethod.jsonResponse
						.getString("session");
			} catch (JSONException e) {
				e.printStackTrace();
			}
			// Rest session refresh counter
			refreshCnt = 2;
			// Retry after session refreshed
			readThreadPage(pageRequested);
		}

	}
	
	private class RefreshingTask extends AsyncTask<Void, Void, Result> {

		PostMethod postMethod = new PostMethod();
		int pageRequested = currentpage;

		@Override
		protected Result doInBackground(Void... params) {
			JSONObject postReq = new JSONObject();
			try {
				postReq.put("username", URLEncoder.encode(
						MainActivity.network.mUsername, "utf-8"));
				postReq.put("session", MainActivity.network.mSession);
				postReq.put("tid", threadId);
				postMethod.setNetType(MainActivity.network.mNetType);
				return postMethod.sendPost(postMethod.REQ_FID_TID_SUM, postReq);
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
			int currReplies = replies;
			if (postMethod.jsonResponse != null)
			try {
				currReplies = Integer.parseInt(postMethod.jsonResponse.getString("tid_sum")) + 1;
			} catch (JSONException e) {
				e.printStackTrace();
			}
			if (currReplies != replies) {
				// update current page data
				replies = currReplies;
				readThreadPage(currentpage);
				refreshingCurrentPage = true;
				Log.v("displayActivity", "refreshCurrentPage");
			} else
				progressDialog.dismiss();
		}

	}
	
	private class MyReplySubmitListener implements OnClickListener{

		@Override
		public void onClick(View v) {
			String message = replyMessage.getText().toString() + "\n[i]from [b]BUApp Android[b][/i]";
			if (message != null && !message.isEmpty()){
				Log.v("ThreadActivity", "Reply sumitted>>" + message);
				new NewPostTask(message).execute();
				showToast(BUAppUtils.POSTEXECUTING);}
			else
				showToast("回复不能为空");
		}
		
	}
	
	private class NewPostTask extends AsyncTask<Void, Void, Result> {

		PostMethod postMethod = new PostMethod();
		String message = "";
		
		public NewPostTask(String m) {
			message = m;
		}

		@Override
		protected Result doInBackground(Void... params) {
			JSONObject postReq = new JSONObject();
			try {
				postReq.put("action", "newreply");
				postReq.put("username", URLEncoder.encode(
						MainActivity.network.mUsername, "utf-8"));
				postReq.put("session", MainActivity.network.mSession);
				postReq.put("tid", threadId);
				postReq.put("message", URLEncoder.encode(message, "utf-8"));
				postReq.put("attachment", 0);
				postMethod.setNetType(MainActivity.network.mNetType);
				return postMethod.sendPost(postMethod.NEWPOST, postReq);
			} catch (JSONException e) {
				e.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(final Result result) {
			switch (result) {
			default:
				return;
			case FAILURE:
				showToast(BUAppUtils.POSTFAILURE);
				return;
			case NETWRONG:
				showToast(BUAppUtils.NETWRONG);
				return;
			case UNKNOWN:
				return;
			case SUCCESS_EMPTY:
				showToast(BUAppUtils.POSTSUCCESS);
				replyMessage.setText("");
			}
		}

	}

	private void showToast(String text) {
		Toast.makeText(ThreadActivity.this, text, Toast.LENGTH_SHORT).show();
	}

}
