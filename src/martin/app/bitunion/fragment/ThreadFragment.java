package martin.app.bitunion.fragment;

import java.util.ArrayList;
import java.util.HashMap;
import martin.app.bitunion.MainActivity;
import martin.app.bitunion.ThreadActivity;
import martin.app.bitunion.util.BUAppUtils;
import martin.app.bitunion.util.BUPost;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;

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
	private ArrayList<BUPost> postlist = new ArrayList<BUPost>();
	private HashMap<String, Drawable> imgCache = new HashMap<String, Drawable>();
	WebView singlepageView = null;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		MainActivity.settings.setNetType(MainActivity.settings.mNetType);

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
		
		singlepageView = new WebView(getActivity());
		String content = createHtmlCode();
		singlepageView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
		singlepageView.getSettings().setJavaScriptEnabled(true);
		singlepageView.addJavascriptInterface(new JSInterface(getActivity()), "JSInterface");
		singlepageView.loadDataWithBaseURL("file:///android_res/drawable/", content, "text/html", "utf-8", null);
		Log.i("ThreadFragment", "WebView created!>>" + PAGENUM);
		return singlepageView;
	}
	
	private String createHtmlCode(){
		String content = "<!DOCTYPE ><html><head><title></title>" +
				"<style type=\"text/css\">" +
				"img{max-width: 100%; width:auto; height: auto;}" +
				"body{background-color: #D8E2EF; color: #284264;font-size:" + MainActivity.settings.contenttextsize +"px;}" +
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
		postlist = content;
		String htmlcode = createHtmlCode();
		singlepageView.loadDataWithBaseURL("file:///android_res/drawable/", htmlcode, "text/html", "utf-8", null);
		Log.v("ThreadFragment", "fragment>>" + this.PAGENUM + "<<updated");
	}

}