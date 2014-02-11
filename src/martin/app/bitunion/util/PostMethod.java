package martin.app.bitunion.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import martin.app.bitunion.util.BUAppUtils.Result;

public class PostMethod {

	public String ROOTURL, BASEURL;
	public String REQ_LOGGING, REQ_FORUM, REQ_THREAD, REQ_PROFILE, REQ_POST,
			NEWPOST, NEWTHREAD, REQ_FID_TID_SUM;

	public JSONObject jsonResponse = null;

	public PostMethod() {
		// TODO Auto-generated constructor stub
	}

	public Result sendPost(String path, JSONObject jsonRequest) {
		try {
			if (path == NEWPOST){
				HttpParams httpParams = new BasicHttpParams();
				HttpConnectionParams.setConnectionTimeout(httpParams, 8000);
				HttpConnectionParams.setSoTimeout(httpParams, 10000);
				HttpClient httpclient = new DefaultHttpClient(httpParams);
				HttpPost httppost = new HttpPost(path);
				MultipartEntityBuilder reqEntityBuilder = MultipartEntityBuilder.create();
				reqEntityBuilder.addTextBody("json", jsonRequest.toString());
				
				httppost.setEntity(reqEntityBuilder.build());
				HttpResponse response = httpclient.execute(httppost);
				
				if (response.getStatusLine().getStatusCode() == 200){
					String serverResponse = getServerResponse(response.getEntity().getContent());
					jsonResponse = new JSONObject(serverResponse);
					Log.v("PostMethod", "serverResponse>>" + jsonResponse.toString());
					if (jsonResponse.getString("result").equals("success"))
						return Result.SUCCESS_EMPTY;
					else
						return Result.FAILURE;
					}
				else 
					return Result.NETWRONG;
			}
			
			URL url = new URL(path);
			HttpURLConnection urlConnection = (HttpURLConnection) url
					.openConnection();
			urlConnection.setConnectTimeout(10000);
			urlConnection.setDoInput(true);
			urlConnection.setDoOutput(true);
			byte[] postdata = jsonRequest.toString().getBytes();
			urlConnection.setRequestProperty("Content-Type",
					"application/x-www-form-urlencoded");
			urlConnection.setRequestProperty("Content-Length",
					String.valueOf(postdata.length));
			OutputStream outputStream = urlConnection.getOutputStream();
			outputStream.write(postdata);
			if (urlConnection.getResponseCode() == 200) {
				this.jsonResponse = new JSONObject(
						getServerResponse(urlConnection.getInputStream()));
				String result = jsonResponse.getString("result");
				if (result.equals("success"))
					if (jsonRequest.length() <= 1)
						return Result.SUCCESS_EMPTY;
					else
						return Result.SUCCESS;
				else if (result.equals("fail"))
					return Result.FAILURE;
			} else {
				return Result.NETWRONG;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return Result.UNKNOWN;
	}

	public String getServerResponse(InputStream inputStream)
			throws UnsupportedEncodingException {
		// TODO Auto-generated method stub
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		byte[] data = new byte[1024];
		int len = 0;
		String result = "";
		if (inputStream != null) {
			try {
				while ((len = inputStream.read(data)) != -1) {
					outputStream.write(data, 0, len);
				}
				result = new String(outputStream.toByteArray());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return result;
	}

	public void setNetType(int net) {
		if (net == BUAppUtils.BITNET) {
			ROOTURL = "http://www.bitunion.org";
		} else if (net == BUAppUtils.OUTNET) {
			ROOTURL = "http://out.bitunion.org";
		}
		BASEURL = ROOTURL + "/open_api";
		REQ_LOGGING = BASEURL + "/bu_logging.php";
		REQ_FORUM = BASEURL + "/bu_forum.php";
		REQ_THREAD = BASEURL + "/bu_thread.php";
		REQ_PROFILE = BASEURL + "/bu_profile.php";
		REQ_POST = BASEURL + "/bu_post.php";
		REQ_FID_TID_SUM = BASEURL + "/bu_fid_tid.php";
		NEWPOST = BASEURL + "/bu_newpost.php";
		NEWTHREAD = BASEURL + "/bu_newpost.php";
	}

}
