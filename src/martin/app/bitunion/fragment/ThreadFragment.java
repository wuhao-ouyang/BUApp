package martin.app.bitunion.fragment;

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

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.Html.ImageGetter;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
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
public class ThreadFragment extends Fragment {
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

	public ThreadFragment() {

	}

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
		if (postlist != null && !postlist.isEmpty())
			postlist.clear();
		for (String s : list)
			try {
				postlist.add(new BUPost(new JSONObject(s)));
			} catch (JSONException e) {
				e.printStackTrace();
			}

		ListView dummyListView = new ListView(getActivity());
		mAdapter = new MyListAdapter(getActivity(),
				R.layout.singlepostitem, postlist);
		dummyListView.setAdapter(mAdapter);
		return dummyListView;
	}

	@Override
	public void onViewStateRestored(Bundle savedInstanceState) {
		super.onViewStateRestored(null);
	}
	
	public void update(ArrayList<BUPost> content) {
		mAdapter.clear();
		mAdapter.updateList(content);
		mAdapter.notifyDataSetChanged();
		Log.v("fragment", "fragment>>" + this.PAGENUM);
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
			TextView dateView = (TextView) view.findViewById(R.id.post_date);
			titleView.setText("#" + Integer.toString(POS_OFFSET + position)
					+ "\t" + postItem.getAuthor());
			dateView.setText(postItem.getDateline());
			// Initiate content view container
			LinearLayout contentLayout = (LinearLayout) view
					.findViewById(R.id.post_content_container);
			// Initiate content view
			TextView contentView = (TextView) view
					.findViewById(R.id.post_content);
			TextView subjectView = (TextView) view
					.findViewById(R.id.post_subject);
//			contentView.setText(Html.fromHtml(postItem.getMessage()));
			contentView.setText(Html.fromHtml(
					postItem.getMessage(),
					new HtmlImageGetter(view), null));
			contentView.setMovementMethod(LinkMovementMethod.getInstance());
			// Initiate quote view
			int padding = BUAppUtils.dip2px(getContext(), 2);
			if (quotes != null && !quotes.isEmpty())
				for (BUQuote quote : quotes) {
					TextView quoteView = new TextView(getContext());
					quoteView.setBackgroundResource(R.drawable.border_dash);
					quoteView.setTextColor(getResources().getColor(
							R.color.blue_text));
					quoteView.setPadding(padding, padding, padding, padding);
					quoteView.setTextIsSelectable(true);
					quoteView.setText(Html.fromHtml(quote.toString()));
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

	public class HtmlImageGetter implements Html.ImageGetter {

		private TextView htmlTextView;
		private Drawable defaultDrawable;
		private View container;

		public HtmlImageGetter(View view) {
			htmlTextView = (TextView) view.findViewById(R.id.post_content);
			container = view;
			defaultDrawable = getResources().getDrawable(R.drawable.ic_action_picture);
		}

		@Override
		public Drawable getDrawable(String imgUrl) {
			// Get MD5 of imgUrl
			String imgKey = null;
			try {
				MessageDigest m = MessageDigest.getInstance("MD5");
				m.reset();
				m.update(imgUrl.getBytes());
				byte[] digest = m.digest();
				BigInteger bigInt = new BigInteger(1, digest);
				imgKey = bigInt.toString(16);
				while (imgKey.length() < 32)
					imgKey = "0" + imgKey;
				Log.v("image", imgUrl + ">>>" + imgKey);
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			}

			// Check if image is in cache
			if (imgCache.get(imgKey) != null)
				return imgCache.get(imgKey);
			
			// 检查是否为本地表情文件
			Pattern p = Pattern.compile("\\.\\./images/(smilies|bz)/(.+?)\\.gif");
			Matcher m = p.matcher(imgUrl);
			if (m.find()) {
				int resourceId = getResources().getIdentifier(
						m.group(1) + "_" + m.group(2), "drawable",
						getActivity().getPackageName());
//				Log.v("ThreadFragment", "resource name>>" + m.group(1) + "_" + m.group(2));
				if (resourceId != 0) {
					Drawable drawable = getResources().getDrawable(resourceId);
					drawable.setBounds(0, 0, drawable.getIntrinsicHeight(),
							drawable.getIntrinsicWidth());
					Log.v("ThreadFragment",
							"getDrawable>>emotion find>>" + m.group(1) + "/" + m.group(2));
					// imgCache.put(imgKey, drawable);
					return drawable;
				}
			}

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
				if (inps == null)
					return getResources().getDrawable(
							R.drawable.ic_action_picture);
				Drawable drawable = Drawable.createFromStream(inps, imgKey);
				Bitmap bitmap = BitmapFactory.decodeStream(inps);
				return drawable;
			}

			@Override
			protected void onPostExecute(Drawable result) {
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
				float dpi = MainActivity.PIXDENSITY;
				float scalingFactor = (float) htmlTextView.getMeasuredWidth()
						/ drawable.getIntrinsicWidth();
				if (drawable.getIntrinsicWidth() <= 30) {
					drawable.setBounds(
							0,
							BUAppUtils.dip2px(
									getActivity(),
									(float) (-drawable.getIntrinsicHeight() * 1.5 - 2)),
							BUAppUtils.dip2px(
									getActivity(),
									(float) (drawable.getIntrinsicWidth() * 1.5)),
							BUAppUtils.dip2px(getActivity(), -2));
					setBounds(
							0,
							BUAppUtils.dip2px(
									getActivity(),
									(float) (-drawable.getIntrinsicHeight() * 1.5)),
							BUAppUtils.dip2px(
									getActivity(),
									(float) (drawable.getIntrinsicWidth() * 1.5)),
							BUAppUtils.dip2px(getActivity(), 0));
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

	// public class MyImageGetter implements ImageGetter {
	//
	// public int getResourceID(String name) {
	// Field field;
	// try {
	// field = R.drawable.class.getField(name);
	// return Integer.parseInt(field.get(null).toString());
	// } catch (NoSuchFieldException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// } catch (NumberFormatException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// } catch (IllegalAccessException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// } catch (IllegalArgumentException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// return 0;
	// }
	//
	// @Override
	// public Drawable getDrawable(String imgpath) {
	// String source = imgpath;
	// Drawable drawable = null;
	// Log.v("image", source);
	// // // 如果是表情图片

	// // if (m.find()){
	// // drawable= getResources().getDrawable(R.drawable.shifty);
	// //
	// // return drawable;
	// // }
	// MainActivity.network.setNetType(MainActivity.network.mNetType);
	// source = source.replace("..", MainActivity.network.ROOTURL);
	// Log.v("image", source);
	// try {
	// InputStream is = new DefaultHttpClient().execute(new
	// HttpGet(source)).getEntity().getContent();
	// Bitmap bitmap = BitmapFactory.decodeStream(is);
	// drawable = new BitmapDrawable(getResources(), bitmap);
	// //setBounds(0, 0, bm.getWidth(), bm.getHeight());
	// drawable.setBounds(0, 0, 200, 300);
	// } catch (Exception e) {e.printStackTrace();}
	// return drawable;
	// }
	// }
	//
	// private class ReadImageTask extends AsyncTask<Void, Void, InputStream>{
	//
	// InputStream inputStream = null;
	// String path;
	//
	// public ReadImageTask(String source) {
	// this.path = source;
	// }
	//
	// @Override
	// protected InputStream doInBackground(Void... params) {
	// // TODO Auto-generated method stub
	// try {
	// inputStream = BUAppUtils.getImageVewInputStream(this.path);
	// } catch (IOException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// return null;
	// }
	// }

}