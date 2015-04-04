package martin.app.bitunion;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import martin.app.bitunion.fragment.ConfirmDialogFragment;
import martin.app.bitunion.fragment.ConfirmDialogFragment.ConfirmDialogListener;
import martin.app.bitunion.fragment.ThreadFragment;
import martin.app.bitunion.fragment.UserInfoDialogFragment;
import martin.app.bitunion.util.BUAppUtils;
import martin.app.bitunion.util.BUPost;
import martin.app.bitunion.util.PostMethod;
import martin.app.bitunion.util.BUAppUtils.Result;

import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
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
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.View.OnTouchListener;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

/**
 * @author Strider_oy
 * 
 */
public class ThreadActivity extends FragmentActivity implements ConfirmDialogListener {

	
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
		
		if (savedInstanceState != null && !savedInstanceState.isEmpty()) {
			threadId = savedInstanceState.getString("tid");
			threadName = savedInstanceState.getString("subject");
			replies = savedInstanceState.getInt("replies");
		}
		
		if (replies % BUAppUtils.POSTS_PER_PAGE == 0)
			lastpage = replies / BUAppUtils.POSTS_PER_PAGE - 1;
		else
			lastpage = replies / BUAppUtils.POSTS_PER_PAGE;
		Log.v("ThreadActivity", "lastpage>>>>>" + lastpage);

		// Setup the action bar.
		getActionBar().setTitle(threadName);
		getActionBar().setDisplayShowHomeEnabled(false);

		// Get reply View container and hide for current
		replyContainer = (LinearLayout) findViewById(R.id.reply_layout);
		replyContainer.setVisibility(View.GONE);
		replyMessage = (EditText) replyContainer
				.findViewById(R.id.reply_message);
		// Button calls reply window to front
		ImageButton replySubmit = (ImageButton) replyContainer
				.findViewById(R.id.reply_submit);
		replySubmit.setOnClickListener(new MyReplySubmitListener());
		ImageButton advreply = (ImageButton) replyContainer.findViewById(R.id.reply_advanced);
		advreply.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(ThreadActivity.this, NewthreadActivity.class);
				intent.putExtra("action", "newpost");
				intent.putExtra("tid", threadId);
				intent.putExtra("message", replyMessage.getText().toString());
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
		mViewPager.setOnTouchListener(new MyOnTouchListener());

		// Progress dialog indicating reading process
		progressDialog = new ProgressDialog(this, R.style.ProgressDialog);
		progressDialog.setMessage("读取中...");
		progressDialog.show();

		// Read first two page.
		readThreadPage(0);
		readThreadPage(1);

	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		// TODO Data needs to be stored
		outState.putString("tid", threadId);
		outState.putString("subject", threadName);
		outState.putInt("replies", replies);
	}

	/**
	 * Open a new thread to requesting data from server
	 * 
	 * @param page
	 *            Which page to request
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
			break;
		case R.id.action_refresh:
			refreshCurrentPage();
			break;
		case R.id.action_post:
			if (!replyContainer.isShown())
				replyContainer.setVisibility(View.VISIBLE);
			else
				replyContainer.setVisibility(View.GONE);
			break;
		case R.id.action_sharelink:
			StringBuilder sb = new StringBuilder(threadName);
			sb.append('\n');
			sb.append(MainActivity.settings.ROOTURL);
			sb.append("/viewthread.php?tid=");
			sb.append(threadId);
			
			ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
			ClipData.Item cliptext = new ClipData.Item(sb.toString());
			String[] mime = new String[1];
			mime[0] = "text/plain";
			ClipData clip = new ClipData("帖子链接", mime, cliptext);
			clipboard.setPrimaryClip(clip);
			showToast("帖子链接已经复制进剪切板");
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	public void setQuoteText(BUPost quotePost) {
		if (!replyContainer.isShown())
			replyContainer.setVisibility(View.VISIBLE);
		replyMessage.setText(replyMessage.getText().toString() + quotePost.toQuote());
		replyMessage.setSelection(replyMessage.getText().toString().length());	// 设置光标到文本末尾
	}
	
	public void displayUserInfo(int uid) {
		UserInfoDialogFragment infoDialog = new UserInfoDialogFragment();
		Bundle args = new Bundle();
		args.putInt("uid", uid);
		infoDialog.setArguments(args);
		infoDialog.show(getSupportFragmentManager(), "Userinfo-" + uid);
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
	 * A {@link FragmentStatePagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 */
	public class ThreadPagerAdapter extends FragmentStatePagerAdapter {

		SparseArray<ThreadFragment> registeredFragments = new SparseArray<ThreadFragment>();

		public ThreadPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			if (registeredFragments.get(position) == null){
				registeredFragments.put(position, new ThreadFragment());
			Bundle args = new Bundle();
			ArrayList<String> singlepagelist = new ArrayList<String>();
			if (postList.get(position) != null)
				for (BUPost post : postList.get(position))
					singlepagelist.add(post.toString());
			args.putStringArrayList("singlepagelist", singlepagelist);
			args.putInt(ThreadFragment.ARG_PAGE_NUMBER, position);
			registeredFragments.get(position).setArguments(args);
			Log.d("PagerAdapter", "Fragment create>>" + position);}
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
//			registeredFragments.remove(position);
			super.destroyItem(container, position, object);
		}

		public ThreadFragment getFragment(int position) {
			return registeredFragments.get(position);
		}
		
	}

	/**
	 * Listener to swipe activity. Double one-direction swiping will kill
	 * current activity thus lead to upper level of this application.
	 * @author Strider_oy
	 */
	class MyOnTouchListener implements OnTouchListener {

		double lastx = -1;
		long lastswipetimeright = 0;
		long lastswipetimeleft = 0;
		boolean rightswipetrig = false;
		boolean leftswipetrig = false;

		@Override
		public boolean onTouch(View v, MotionEvent motion) {
			if (currentpage == 0 || currentpage == lastpage)
				switch (motion.getAction()) {
				case MotionEvent.ACTION_MOVE:
					int dpMoved = 0;
					if (lastx != -1)
						dpMoved = BUAppUtils.px2dip(getApplication(),
								(float) (motion.getX() - lastx));
					lastx = motion.getX();
//					Log.i("TouchEvent", "dpmoved>>" + dpMoved);
					if (dpMoved > 24 && currentpage == 0)
						rightswipetrig = true;
					else if (dpMoved < -24 && currentpage == lastpage)
						leftswipetrig = true;
					break;
				case MotionEvent.ACTION_UP:
					lastx = -1;
					if (rightswipetrig) {
						if ((System.currentTimeMillis() - lastswipetimeright) >= BUAppUtils.EXIT_WAIT_TIME) {
							showToast("再次右滑返回");
							lastswipetimeright = System.currentTimeMillis();
							lastswipetimeleft = 0;
						} else
							finish();
						rightswipetrig = false;
					}
					if (leftswipetrig) {
						if ((System.currentTimeMillis() - lastswipetimeleft) >= BUAppUtils.EXIT_WAIT_TIME) {
							showToast("再次左滑返回");
							lastswipetimeleft = System.currentTimeMillis();
							lastswipetimeright = 0;
						} else
							finish();
						leftswipetrig = false;
					}
					break;
				default:
				}
			return false;
		}
	}

	/**
	 * Listen to the {@link ViewPager}, pre-load pages will be useful next to
	 * current view.
	 * 
	 * @author Strider_oy
	 */
	class MyOnPageChangeListener implements OnPageChangeListener {

		private int position;

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
				postsRemain = replies % (BUAppUtils.POSTS_PER_PAGE+1);
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
							MainActivity.settings.mUsername, "utf-8"));
					postReq.put("session", MainActivity.settings.mSession);
					postReq.put("tid", threadId);
					postReq.put("from", from);
					postReq.put("to", to);
					Log.d("Request", "Replies>> " + replies+ " from >> " + from + " to >>" + to);
					netStat = postMethod.sendPost(BUAppUtils.getUrl(MainActivity.settings.mNetType, BUAppUtils.REQ_POST), postReq);
					if (netStat != Result.SUCCESS)
						return netStat;
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
//				 Log.v("ThreadActivity", "raw jsonArray>>" + pageContent);
				postList.put(this.page, BUAppUtils.jsonToPostlist(pageContent, this.page));
				Log.v("ThreadActivity", "Page loaded>>" + this.page);
				Log.v("ThreadActivity", "Post length>>" + pageContent.length());
				mThreadAdapter.notifyDataSetChanged();
				// mThreadAdapter.getFragment(this.page).update(postList.get(this.page));
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
						MainActivity.settings.mUsername, "utf-8"));
				postReq.put("password", MainActivity.settings.mPassword);
				return postMethod.sendPost(BUAppUtils.getUrl(MainActivity.settings.mNetType, BUAppUtils.REQ_LOGGING), postReq);
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
			Log.i("DisplayActivity", "session refreshed");
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
						MainActivity.settings.mUsername, "utf-8"));
				postReq.put("session", MainActivity.settings.mSession);
				postReq.put("tid", threadId);
				return postMethod.sendPost(BUAppUtils.getUrl(MainActivity.settings.mNetType, BUAppUtils.REQ_FID_TID_SUM), postReq);
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
					currReplies = Integer.parseInt(postMethod.jsonResponse
							.getString("tid_sum")) + 1;
				} catch (JSONException e) {
					e.printStackTrace();
				}
				// update current page data
				replies = currReplies;
				lastpage = (replies -1) / BUAppUtils.POSTS_PER_PAGE;
				readThreadPage(currentpage);
				refreshingCurrentPage = true;
				Log.v("displayActivity", "refreshCurrentPage");
		}

	}

	private class MyReplySubmitListener implements OnClickListener {

		ConfirmDialogFragment mAlertFragment;
		
		@Override
		public void onClick(View v) {
			String message = replyMessage.getText().toString();
			if (message != null && !message.isEmpty()) {
				mAlertFragment = new ConfirmDialogFragment();
				Bundle args = new Bundle();
				args.putString("title", "发送消息");
				args.putString("message", "确认要发送吗？");
				args.putString("reply", message);
				mAlertFragment.setArguments(args);
				mAlertFragment.show(getSupportFragmentManager(), "LogoutAlert");
			} else
				showToast("回复不能为空");
		}
	}
	
	@Override
	public void onDialogPositiveClick(DialogFragment dialog, String message) {
		Log.i("MyReplySubmitListener", "Reply sumitted>>" + message);
		if (MainActivity.settings.showsigature)
			message += BUAppUtils.CLIENTMESSAGETAG;
		new NewPostTask(message).execute();
	}
	@Override
	public void onDialogPositiveClick(DialogFragment dialog) {}

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
						MainActivity.settings.mUsername, "utf-8"));
				postReq.put("session", MainActivity.settings.mSession);
				postReq.put("tid", threadId);
				postReq.put("message", URLEncoder.encode(message, "utf-8"));
				postReq.put("attachment", 0);
				return postMethod.sendPost(BUAppUtils.getUrl(MainActivity.settings.mNetType, BUAppUtils.NEWPOST), postReq);
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

	private Toast toast = null;
	private void showToast(String text) {
		if (toast != null)
			toast.cancel();
		toast = Toast.makeText(ThreadActivity.this, text, Toast.LENGTH_SHORT);
		toast.show();
	}

}
