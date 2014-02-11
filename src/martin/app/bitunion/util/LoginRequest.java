package martin.app.bitunion.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import martin.app.bitunion.util.BUAppUtils.Result;
import martin.app.bitunion.util.PostMethod;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.util.Log;

public class LoginRequest {

	String mUsername;
	String mPassword;
	int mNettype;
	public JSONObject response = null;
	Result res = null;
	
	private UserLoginTask mLoginTask;
	
	
	public LoginRequest(String username, String password, int nettype) {
		mUsername = username;
		mPassword = password;
		mNettype = nettype;
		try {
			Log.v("martin", "session>>>>>>>"+(this.response).getString("session"));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public Result loginReq(){
		mLoginTask = new UserLoginTask();
		mLoginTask.execute((Void) null);
		this.response = mLoginTask.jsonRes;
		try {
			Log.v("martin", "session>>>>>>>"+(this.response).getString("session"));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.res = mLoginTask.taskRes;
		mLoginTask = null;
		return this.res;
	}

	/**
	 * Represents an asynchronous login/registration task used to authenticate
	 * the user.
	 */
	public class UserLoginTask extends AsyncTask<Void, Void, Result> {

		PostMethod postMethod = new PostMethod();
		JSONObject jsonRes = new JSONObject();
		Result taskRes = null;

		@Override
		protected Result doInBackground(Void... params) {
			// TODO: attempt authentication against a network service.
			JSONObject postReq = new JSONObject();
			try {
				postReq.put("action", "login");
				postReq.put("username", URLEncoder.encode(
						mUsername, "utf-8"));
				postReq.put("password", mPassword);
				postMethod.setNetType(mNettype);
				return postMethod.sendPost(postMethod.REQ_LOGGING, postReq);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(final Result result) {
			
			this.jsonRes = postMethod.jsonResponse;
			this.taskRes = result;
		
		}

	}

}
