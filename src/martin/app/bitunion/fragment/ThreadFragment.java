package martin.app.bitunion.fragment;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import martin.app.bitunion.DisplayActivity;
import martin.app.bitunion.MainActivity;
import martin.app.bitunion.R;
import martin.app.bitunion.ThreadActivity;
import martin.app.bitunion.util.BUAppUtils;
import martin.app.bitunion.util.BUAppUtils.Result;
import martin.app.bitunion.util.BUForum;
import martin.app.bitunion.util.BUPost;
import martin.app.bitunion.util.BUQuote;
import martin.app.bitunion.util.BUThread;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.Html.ImageGetter;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.TextView;

/**
 * A dummy fragment representing a section of the app, but that simply displays
 * dummy text.
 */
@SuppressLint({ "JavascriptInterface", "SetJavaScriptEnabled" })
public class ThreadFragment extends Fragment {
	
	ThreadFragment mFragment;
	
	/**
	 * The fragment argument representing the section number for this fragment.
	 */
	public static final String ARG_PAGE_NUMBER = "page";
	private int POS_OFFSET;
	private int PAGENUM;
	private LayoutInflater inflater;
	private MyListAdapter mAdapter;
	private ArrayList<BUPost> postlist = new ArrayList<BUPost>();
	private HashMap<String, Drawable> imgCache = new HashMap<String, Drawable>();
	WebView singlepageView = null;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		MainActivity.network.setNetType(MainActivity.network.mNetType);
		this.inflater = inflater;
		View rootView = inflater.inflate(R.layout.fragment_thread_dummy,
				container, false);
		if (getArguments() == null) {
			Log.v("Fragment", "no arguments");
			return rootView;
		}
		PAGENUM = getArguments().getInt(ThreadFragment.ARG_PAGE_NUMBER);
		POS_OFFSET = PAGENUM * BUAppUtils.POSTS_PER_PAGE + 1;
		ArrayList<String> list = getArguments().getStringArrayList(
				"singlepagelist");
		if (postlist == null || postlist.isEmpty())
			for (String s : list)
				try {
					postlist.add(new BUPost(new JSONObject(s), list.indexOf(s)
							+ POS_OFFSET));
				} catch (JSONException e) {
					e.printStackTrace();
				}

//		ListView dummyListView = new ListView(getActivity());
//		mAdapter = new MyListAdapter(getActivity(),
//				R.layout.singlepostitem, postlist);
//		dummyListView.setAdapter(mAdapter);
//		return dummyListView
		
		singlepageView = new WebView(getActivity());
		String content = createHtmlCode();
		singlepageView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
		singlepageView.getSettings().setJavaScriptEnabled(true);
		singlepageView.addJavascriptInterface(new JSInterface(getActivity()), "JSInterface");
		singlepageView.loadDataWithBaseURL("file:///android_res/drawable/", content, "text/html", "utf-8", null);
		return singlepageView;
	}
	
	private String createHtmlCode(){
		String content = "<!DOCTYPE ><html><head><title></title>" +
				"<style type=\"text/css\">" +
				"img{max-width: 100%; width:auto; height: auto;}" +
				"body{background-color: #D8E2EF; color: #284264;font-size:14px;}" +
				"</style><script type='text/javascript'>" +
				"function referenceOnClick(num){" +
				"JSInterface.referenceOnClick(num);}" +
				"function authorOnClick(uid){" +
				"JSInterface.authorOnClick(uid);}" +
				"</script></head><body>";

		for (BUPost postItem : postlist){
			content += postItem.getHtmlLayout();
		}
		content += "</body></html>";
		return content;
	}
	
	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			if (msg.what == 0) {
				BUPost post = postlist.get(msg.arg1 - 1);
				((ThreadActivity) ThreadFragment.this.getActivity())
						.setQuoteText(post);
			}
			if (msg.what == 1) {
				((ThreadActivity) ThreadFragment.this.getActivity())
						.displayUserInfo(msg.arg1);
			}
		}
	};
	
	private class JSInterface {
		
		Context mContext;
		
		JSInterface(Context c) {
			mContext = c;
		}
		@JavascriptInterface
		public void referenceOnClick(int count){
			handler.obtainMessage();
			Message msg = handler.obtainMessage();
			msg.what = 0;
			msg.arg1 = count;
			handler.sendMessage(msg);
			Log.v("JavascriptInterface", "Ref Count>>" + count);
		}
		
		@JavascriptInterface
		public void authorOnClick(int uid){
			handler.obtainMessage();
			Message msg = handler.obtainMessage();
			msg.what = 1;
			msg.arg1 = uid;
			handler.sendMessage(msg);
			Log.i("JavascriptInterface", "Author ID>>" + uid);
		}
	}
	
	public void update(ArrayList<BUPost> content) {
//		mAdapter.clear();
//		mAdapter.updateList(content);
//		mAdapter.notifyDataSetChanged();
		postlist = content;
		String htmlcode = createHtmlCode();
		singlepageView.loadDataWithBaseURL("file:///android_res/drawable/", htmlcode, "text/html", "utf-8", null);
		Log.v("ThreadFragment", "fragment>>" + this.PAGENUM + "<<updated");
	}

	class MyListAdapter extends ArrayAdapter<BUPost> {

		List<BUPost> list = new ArrayList<BUPost>();
		SparseArray<View> viewList = new SparseArray<View>();

		public MyListAdapter(Context context, int resource,
				ArrayList<BUPost> arrayList) {
			super(context, resource, arrayList);
			this.list = arrayList;
		}
		
		public void updateList(ArrayList<BUPost> list){
			this.list = list;
		}

		@Override
		public int getCount() {
			return this.list.size();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (viewList.get(position) != null)
				return viewList.get(position);
			// Initiate data and View container
			BUPost postItem = list.get(position);
			ArrayList<BUQuote> quotes = postItem.getQuote();
			ArrayList<TextView> quoteViews = new ArrayList<TextView>(
					quotes.size());

			View view = inflater.inflate(R.layout.singlepostitem, null);
			viewList.put(position, view);
			TextView titleView = (TextView) view.findViewById(R.id.post_title);
			TextView refTextView = (TextView) view.findViewById(R.id.post_ref_button);
			TextView authorinfoView = (TextView) view.findViewById(R.id.post_viewauthor_button);
			TextView dateView = (TextView) view.findViewById(R.id.post_date);
			titleView.setText("#" + Integer.toString(POS_OFFSET + position)
					+ "\t" + postItem.getAuthor());
			refTextView.setText(Html.fromHtml(" <u>引用</u>  "));
			refTextView.setOnClickListener(new RefOnClickListener(postItem));
			authorinfoView.setText(Html.fromHtml("<u>查看</u>"));	// 点击查看用户详情
			authorinfoView.setOnClickListener(new AuthorOnClickLisstener(postItem.getAuthor()));
			dateView.setText(postItem.getDateline());
			// Initiate content view container
			LinearLayout contentLayout = (LinearLayout) view
					.findViewById(R.id.post_content_container);
			// Initiate content view
			WebView contentView = (WebView) view
					.findViewById(R.id.post_content);
			TextView subjectView = (TextView) view
					.findViewById(R.id.post_subject);
			// Set up TextViews in content container
//			contentView.setText(Html.fromHtml(postItem.getMessage()));
//			contentView.setText(Html.fromHtml(
//					postItem.getMessage(),
//					new HtmlImageGetter(contentView), null));
//			contentView.setMovementMethod(LinkMovementMethod.getInstance());
			contentView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
			contentView.setScrollBarStyle(0);
			contentView.setBackgroundColor(getResources().getColor(R.color.blue_text_bg_light));
			contentView.loadDataWithBaseURL(null, postItem.getMessage(), null, "utf-8", null);
			// Initiate quote view
			int padding = BUAppUtils.dip2px(getContext(), 2);
			if (quotes != null && !quotes.isEmpty())
				for (BUQuote quote : quotes) {
					TextView quoteView = new TextView(getContext());
					quoteView.setBackgroundResource(R.drawable.border_dash);
					quoteView.setTextColor(getResources().getColor(
							R.color.blue_text));
					quoteView.setLinkTextColor(getResources().getColor(R.color.blue_text_link));
					quoteView.setPadding(padding, padding, padding, padding);
					LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
					llp.setMargins(5, 5, 5, 5);
					quoteView.setLayoutParams(llp);
					quoteView.setTextIsSelectable(true);
					quoteView.setText(Html.fromHtml(quote.toString(), new HtmlImageGetter(quoteView), null));
					quoteViews.add(quoteView);
				}
			
			contentLayout.removeAllViews(); // Remove template views
			if (postItem.getSubject()!=null){
				subjectView.setText(Html.fromHtml(postItem.getSubject()));
				contentLayout.addView(subjectView);}
			if (quotes != null && !quotes.isEmpty())
				for (TextView quoteView : quoteViews) {
					contentLayout.addView(quoteView);
				}
			contentLayout.addView(contentView);
			return view;
		}

		public void updateView(int position, View view) {
			viewList.put(position, view);
		}
	}
	
	private class AuthorOnClickLisstener implements View.OnClickListener {

		String author = "";
		
		public AuthorOnClickLisstener(String authorname){
			author = authorname;
		}
		
		@Override
		public void onClick(View v) {
			//TODO Show author info of selected post
		}
	}
	
	private class RefOnClickListener implements View.OnClickListener {

		BUPost post;
		
		public RefOnClickListener(BUPost p){
			post = p;
		}

		@Override
		public void onClick(View v) {
			((ThreadActivity) ThreadFragment.this.getActivity()).setQuoteText(post);
		}
		
	}
	
	public class HtmlImageGetter implements Html.ImageGetter {

		private TextView htmlTextView;
		private Drawable defaultDrawable;

		public HtmlImageGetter(TextView view) {
			htmlTextView = view;
//			container = view;
			defaultDrawable = getResources().getDrawable(R.drawable.ic_action_picture);
		}

		@Override
		public Drawable getDrawable(String imgUrl) {
			// 检查是否为本地表情文件
			Pattern p = Pattern
					.compile("\\.\\./images/(smilies|bz)/(.+?)\\.gif");
			Matcher m = p.matcher(imgUrl);
			if (m.find()) {
				int resourceId = getResources().getIdentifier(
						m.group(1) + "_" + m.group(2), "drawable",
						getActivity().getPackageName());
				// Log.v("ThreadFragment", "resource name>>" + m.group(1) + "_"
				// + m.group(2));
				if (resourceId != 0) {
					Drawable drawable = getResources().getDrawable(resourceId);
					drawable.setBounds(0, 0, drawable.getIntrinsicHeight(),
							drawable.getIntrinsicWidth());
					Log.v("ThreadFragment",
							"getDrawable>>emotion find>>" + m.group(1) + "/"
									+ m.group(2));
					// imgCache.put(imgKey, drawable);
					return drawable;
				}
			}

			// Get MD5 of imgUrl
			String imgKey = null;
			try {
				MessageDigest md = MessageDigest.getInstance("MD5");
				md.reset();
				md.update(imgUrl.getBytes());
				byte[] digest = md.digest();
				BigInteger bigInt = new BigInteger(1, digest);
				imgKey = bigInt.toString(16);
				while (imgKey.length() < 32)
					imgKey = "0" + imgKey;
				Log.v("ThreadFragment", imgUrl + ">>>" + imgKey);
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			}

			// Check if image is in cache
			if (imgCache.get(imgKey) != null)
				return imgCache.get(imgKey);

			imgUrl = imgUrl.replace("..", MainActivity.network.ROOTURL);
			URLDrawable urlDrawable = new URLDrawable(defaultDrawable);
			new AsyncThread(urlDrawable).execute(imgKey, imgUrl);
			return urlDrawable;
		}

		private class AsyncThread extends AsyncTask<String, Integer, Drawable> {
			private String imgKey;
			private URLDrawable drawable;

			public AsyncThread(URLDrawable drawable) {
				this.drawable = drawable;
			}

			@Override
			protected Drawable doInBackground(String... strings) {
				imgKey = strings[0];
				InputStream inps = null;
				try {
					inps = BUAppUtils.getImageVewInputStream(strings[1]);
				} catch (IOException e) {
					e.printStackTrace();
				}
				if (inps == null) {
					if (getActivity() == null)
						return null;
					return getResources().getDrawable(
							R.drawable.ic_action_picture);
				}
//				Drawable drawable = Drawable.createFromStream(inps, imgKey);
				Bitmap bm = BitmapFactory.decodeStream(inps);
				BitmapDrawable drawable = new BitmapDrawable(bm);
				Bitmap bmScaled;
				if (bm.getWidth() > 2048 || bm.getHeight() > 2048) {
					float ratio = (float) bm.getWidth() / bm.getHeight();
					if (ratio > 1)
						bmScaled = Bitmap.createScaledBitmap(bm, 2048,
								(int) (2048 / ratio), false);
					else
						bmScaled = Bitmap.createScaledBitmap(bm,
								(int) (2048 * ratio), 2048, false);
					drawable = new BitmapDrawable(getResources(), bmScaled);
				}
				return drawable;
			}

			@Override
			protected void onPostExecute(Drawable result) {
				if (getActivity() == null)
					return;
				imgCache.put(imgKey, drawable);
				drawable.setDrawable(result);
				htmlTextView.setText(htmlTextView.getText());
			}
		}

		public class URLDrawable extends BitmapDrawable {

			private Drawable drawable;

			public URLDrawable(Drawable defaultDraw) {
				setDrawable(defaultDraw);
			}

			private void setDrawable(Drawable ndrawable) {
				drawable = ndrawable;
				float scalingFactor = (float) htmlTextView.getMeasuredWidth()
						/ drawable.getIntrinsicWidth();
				if (drawable.getIntrinsicWidth() < 100) {
					drawable.setBounds(0, 0, drawable.getIntrinsicWidth() * 2,
							drawable.getIntrinsicHeight() * 2);
					setBounds(0, 0, drawable.getIntrinsicWidth() * 2,
							drawable.getIntrinsicHeight() * 2);
				} else {
					drawable.setBounds(
							0,
							0,
							htmlTextView.getMeasuredWidth(),
							(int) (drawable.getIntrinsicHeight() * scalingFactor));
					setBounds(
							0,
							0,
							htmlTextView.getMeasuredWidth(),
							(int) (drawable.getIntrinsicHeight() * scalingFactor));
					Log.v("ThreadActivity", "width>>" + drawable.getIntrinsicWidth() + "<<");
					Log.v("ThreadActivity", "height>>" + drawable.getIntrinsicHeight() + "<<");
					Log.v("ThreadActivity", "Scaled width>>" + htmlTextView.getMeasuredWidth() + "<<");
					Log.v("ThreadActivity", "Scaled height>>" + (int) (drawable.getIntrinsicHeight() * scalingFactor) + "<<");
				}
//				setBounds(0, 0, htmlTextView.getMeasuredWidth(),
//						(int) (drawable.getIntrinsicHeight() * scalingFactor));
//				htmlTextView.setHeight(htmlTextView.getMeasuredHeight()
//						+ drawable.getIntrinsicHeight() * 2);
			}

			@Override
			public void draw(Canvas canvas) {
				drawable.draw(canvas);
			}
		}
	}

}