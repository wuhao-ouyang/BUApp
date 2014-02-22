package martin.app.bitunion.fragment;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import martin.app.bitunion.DisplayActivity;
import martin.app.bitunion.MainActivity;
import martin.app.bitunion.R;
import martin.app.bitunion.ThreadActivity;
import martin.app.bitunion.DisplayActivity.UserLoginTask;
import martin.app.bitunion.util.BUAppUtils;
import martin.app.bitunion.util.BUForum;
import martin.app.bitunion.util.BUThread;
import martin.app.bitunion.util.PostMethod;
import martin.app.bitunion.util.BUAppUtils.Result;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * A dummy fragment representing a section of the app, but that simply displays
 * dummy text.
 */
public class ForumFragment extends Fragment {
	/**
	 * The fragment argument representing the section number for this fragment.
	 */
	public static final String ARG_PAGE_NUMBER = "page";
	private LayoutInflater inflater;
	private ArrayList<BUThread> threadlist = new ArrayList<BUThread>();
	private int PAGENUM, FORUMID;
	private MyListAdapter mAdapter;
	private View mForumView;

	public ForumFragment() {

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		this.inflater = inflater;
		mForumView = inflater.inflate(R.layout.fragment_display_dummy, container, false);
//		PAGENUM = getArguments().getInt("page");
//		FORUMID = getArguments().getInt("fid");
		ArrayList<String> list = getArguments()
				.getStringArrayList("threadlist");
		if (threadlist == null || threadlist.isEmpty())
			for (String s : list)
				try {
					threadlist.add(new BUThread(new JSONObject(s)));
				} catch (JSONException e) {
					e.printStackTrace();
				}

		ListView dummyListView = (ListView) mForumView.findViewById(R.id.forum_listview);
		mAdapter = new MyListAdapter(getActivity(), R.layout.singlethreaditem,
				threadlist);
		dummyListView.setAdapter(mAdapter);
		// new ReadPageTask().execute(pagenum);
		return mForumView;
	}

	@Override
	public void onViewStateRestored(Bundle savedInstanceState) {
		super.onViewStateRestored(null);
	}

	public void update(ArrayList<BUThread> content) {
		mAdapter.updateList(content);
		mAdapter.notifyDataSetChanged();
		Log.v("fragment", "fragment>>" + this.PAGENUM);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}

	class MyListAdapter extends ArrayAdapter<BUThread> {

		List<BUThread> list = new ArrayList<BUThread>();

		public MyListAdapter(Context context, int resource,
				ArrayList<BUThread> arrayList) {
			super(context, resource, arrayList);
			this.list = arrayList;
		}
		
		public void updateList(ArrayList<BUThread> list){
			this.list = list;
		}
		
		@Override
		public int getCount() {
			return this.list.size();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = convertView;
			if (view == null)
				view = inflater.inflate(R.layout.singlethreaditem, null);
			TextView subjView = (TextView) view
					.findViewById(R.id.thread_subject);
			TextView addinfoView = (TextView) view
					.findViewById(R.id.additional_info);
			TextView repliesView = (TextView) view.findViewById(R.id.thread_replies);
			TextView viewsView = (TextView) view.findViewById(R.id.thread_views);
//			TextView newPostTag = (TextView) view.findViewById(R.id.tag_new_post);
			if ((position % 2) == 1)
				view.setBackgroundColor(getResources().getColor(
						R.color.blue_text_bg_light));
			else
				view.setBackgroundColor(getResources().getColor(
						R.color.blue_text_bg_dark));
			// TextView textView = new TextView(DisplayActivity.this);
			BUThread threadItem = list.get(position);
			subjView.setText(threadItem.getSubject());
			subjView.setTextSize(TypedValue.COMPLEX_UNIT_SP, MainActivity.settings.titletextsize);
			addinfoView.setText(threadItem.getAuthor());
			addinfoView.setTextSize(TypedValue.COMPLEX_UNIT_SP, MainActivity.settings.titletextsize - 2);
			repliesView.setText(threadItem.getReplies());
			repliesView.setTextSize(TypedValue.COMPLEX_UNIT_SP, MainActivity.settings.titletextsize - 2);
			viewsView.setText(threadItem.getViews());
			viewsView.setTextSize(TypedValue.COMPLEX_UNIT_SP, MainActivity.settings.titletextsize - 2);
			view.setTag(threadItem);
			view.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					Intent intent = new Intent(getContext(),
							ThreadActivity.class);
					intent.putExtra("tid", ((BUThread) v.getTag()).getTid());
					intent.putExtra("subject",
							((BUThread) v.getTag()).getSubject());
					intent.putExtra("replies",
							((BUThread) v.getTag()).getReplies());
					intent.putExtra("new", false);
					startActivityForResult(intent, BUAppUtils.MAIN_REQ);
				}
			});
//			newPostTag.setTag(threadItem);
//			newPostTag.setOnClickListener(new OnClickListener() {
//				
//				@Override
//				public void onClick(View v) {
//					Intent intent = new Intent(getContext(),
//							ThreadActivity.class);
//					intent.putExtra("tid", ((BUThread) v.getTag()).getTid());
//					intent.putExtra("subject",
//							((BUThread) v.getTag()).getSubject());
//					intent.putExtra("replies",
//							((BUThread) v.getTag()).getReplies());
//					intent.putExtra("new", true);
//					startActivityForResult(intent, BUAppUtils.MAIN_REQ);
//				}
//			});

			return view;
		}
	}

	private void showToast(String text) {
		Toast.makeText(getActivity(), text, Toast.LENGTH_SHORT).show();
	}
}