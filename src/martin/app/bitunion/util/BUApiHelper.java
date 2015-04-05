package martin.app.bitunion.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import martin.app.bitunion.BUApplication;
import martin.app.bitunion.R;

public class BUApiHelper {

    private static final String TAG = BUApiHelper.class.getSimpleName();

    private static String mUsername;
    private static String mPassword;
    private static String mSession;
    private static String rooturl;
    private static String baseurl;
    private static int mNetType;

    private static RequestQueue mApiQueue;

    /**
     * Login current user with response listener
     */
    public static void tryLogin(Response.Listener<JSONObject> responseListener,
                                Response.ErrorListener errorListener) {
        if (mUsername == null || mPassword == null)
            return;
        String path = baseurl + "/bu_logging.php";
        Map<String, String> params = new HashMap<String, String>();
        params.put("action", "login");
        params.put("username", mUsername);
        params.put("password", mPassword);
        sendRequest(path, params, responseListener, errorListener);
    }

    /**
     * Simple version of {@link BUApiHelper#tryLogin()}
     */
    public static void tryLogin() {
        tryLogin(new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                switch (getResult(response)) {
                    case FAILURE:
                        Toast.makeText(BUApplication.getInstance(), BUAppUtils.LOGINFAIL, Toast.LENGTH_SHORT).show();
                        break;
                    case SUCCESS:
                        Toast.makeText(BUApplication.getInstance(), BUAppUtils.USERNAME + " " + mUsername + " "
                                + BUAppUtils.LOGINSUCCESS, Toast.LENGTH_SHORT).show();
                        mSession = response.optString("session");
                        BUApplication.settings.mSession = response.optString("session");
                        break;
                    case UNKNOWN:
                        Toast.makeText(BUApplication.getInstance(), R.string.network_unknown, Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(BUApplication.getInstance(), BUAppUtils.NETWRONG, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Get threads of forum
     * @param fid Forum id
     * @param from from index
     * @param to to index
     * @param responseListener response listener
     * @param errorListener error listener
     */
    public static void readThreads(int fid, int from, int to,
                                   Response.Listener<JSONObject> responseListener,
                                   Response.ErrorListener errorListener) {
        if (from < 0 || to < 0 || from > to)
            return;
        String path = baseurl + "/bu_thread.php";
        Map<String, String> params = new HashMap<String, String>();
        params.put("action", "thread");
        params.put("username", mUsername);
        params.put("session", mSession);
        params.put("fid", Integer.toString(fid));
        params.put("from", Integer.toString(from));
        params.put("to", Integer.toString(to));
        sendRequest(path, params, responseListener, errorListener);
    }

    public static void readPostList(int tid, int from, int to,
                                    Response.Listener<JSONObject> responseListener,
                                    Response.ErrorListener errorListener) {
        if (from < 0 || to < 0 || from > to)
            return;
        String path = baseurl + "/bu_post.php";
        Map<String, String> params = new HashMap<String, String>();
        params.put("action", "post");
        params.put("username", mUsername);
        params.put("session", mSession);
        params.put("tid", Integer.toString(tid));
        params.put("from", Integer.toString(from));
        params.put("to", Integer.toString(to));
        sendRequest(path, params, responseListener, errorListener);
    }

    public static void init(Context context) {
        SharedPreferences config = context.getSharedPreferences("config", Context.MODE_PRIVATE);
        mNetType = config.getInt("nettype", BUAppUtils.OUTNET);
        mUsername = config.getString("username", null);
        mPassword = config.getString("password", null);
        setNetType(mNetType);

        mApiQueue = Volley.newRequestQueue(context);
    }

    public static void setNetType(int net) {
        mNetType = net;
        if (net == BUAppUtils.BITNET)
            rooturl = "http://www.bitunion.org";
        else if (net == BUAppUtils.OUTNET)
            rooturl = "http://out.bitunion.org";
        baseurl = rooturl + "/open_api";
    }

    private static void sendRequest(final String path, Map<String, String> params,
                                    Response.Listener<JSONObject> responseListener,
                                    Response.ErrorListener errorListener) {
        JSONObject postReq = new JSONObject();
        try {
            for (Map.Entry<String, String> entry : params.entrySet())
                postReq.put(entry.getKey(), URLEncoder.encode(entry.getValue(), HTTP.UTF_8));
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "BUILD " + path + " >> " + postReq.toString());
        mApiQueue.add(new JsonObjectRequest(Request.Method.POST, path, postReq, responseListener, errorListener) {
            @Override
            protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
                Log.d(TAG, path + " >> " + new String(response.data));
                return super.parseNetworkResponse(response);
            }

            @Override
            protected VolleyError parseNetworkError(VolleyError volleyError) {
                Log.d(TAG, path + " >> " + new String(volleyError.networkResponse.data), volleyError);
                return super.parseNetworkError(volleyError);
            }
        });
    }

    public static BUAppUtils.Result getResult(JSONObject response) {
        if ("fail".equals(response.optString("result")))
            return BUAppUtils.Result.FAILURE;
        if ("success".equals(response.optString("result")))
            return BUAppUtils.Result.SUCCESS;
        return BUAppUtils.Result.UNKNOWN;
    }

//    private static String getUrl(int net, int urlType){
//        String ROOTURL, BASEURL;
//
//        ROOTURL = (net == BUAppUtils.BITNET) ? "http://www.bitunion.org" : "http://out.bitunion.org";
//        BASEURL = ROOTURL + "/open_api";
//        if (urlType == REQ_LOGGING)
//            return BASEURL + "/bu_logging.php";
//        if (urlType == REQ_FORUM)
//            return BASEURL + "/bu_forum.php";
//        if (urlType == REQ_THREAD)
//            return BASEURL + "/bu_thread.php";
//        if (urlType == REQ_PROFILE)
//            return BASEURL + "/bu_profile.php";
//        if (urlType == REQ_POST)
//            return BASEURL + "/bu_post.php";
//        if (urlType == REQ_FID_TID_SUM)
//            return BASEURL + "/bu_fid_tid.php";
//        if (urlType == NEWPOST)
//            return BASEURL + "/bu_newpost.php";
//        if (urlType == NEWTHREAD)
//            return BASEURL + "/bu_newpost.php";
//        Log.e("BUAppUtils", "getUrl Error!");
//        return "";
//
//    }

}
