package martin.app.bitunion.util;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import martin.app.bitunion.model.BUPost;
import martin.app.bitunion.model.BUThread;

public class DataParser {
    private static final String TAG = DataParser.class.getSimpleName();

    public static ArrayList<BUThread> parseThreadlist(JSONObject rawJson) {
        JSONArray array = rawJson.optJSONArray("threadlist");
        if (array == null)
            return null;
        ArrayList<BUThread> list = new ArrayList<BUThread>();
        for (int i = 0; i < array.length(); i++) {
            JSONObject threadObj = array.optJSONObject(i);
            if (threadObj != null)
                try {
                    list.add(new BUThread(threadObj));
                } catch (JSONException e) {
                    Log.w(TAG, "Failed parsing thread >> " + threadObj.toString(), e);
                    continue;
                }
        }
        return list;
    }

    public static ArrayList<BUPost> parsePostlist(JSONObject rawJson) {
        JSONArray array = rawJson.optJSONArray("postlist");
        if (array == null)
            return null;
        ArrayList<BUPost> list = new ArrayList<BUPost>();
        for (int i = 0; i < array.length(); i++) {
            JSONObject postObj = array.optJSONObject(i);
            if (postObj != null)
                try {
                    list.add(new BUPost(postObj));
                } catch (JSONException e) {
                    Log.w(TAG, "Failed parsing post >> " + postObj.toString(), e);
                    continue;
                }
        }
        return list;
    }
}
