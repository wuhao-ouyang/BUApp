package martin.app.bitunion.util;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import martin.app.bitunion.model.BUPost;
import martin.app.bitunion.model.BUThread;

public class DataParser {

    public static ArrayList<BUThread> parseThreadlist(JSONObject rawJson) {
        JSONArray array = rawJson.optJSONArray("threadlist");
        if (array == null)
            return null;
        ArrayList<BUThread> list = new ArrayList<BUThread>();
        for (int i = 0; i < array.length(); i++)
            try {
                list.add(new BUThread(array.getJSONObject(i)));
            } catch (JSONException e) {
                Log.w("JSONError", "Error>>\n" + array.toString(), e);
            }
        return list;
    }

    public static ArrayList<BUPost> parsePostlist(JSONObject rawJson) {
        JSONArray array = rawJson.optJSONArray("postlist");
        if (array == null)
            return null;
        ArrayList<BUPost> list = new ArrayList<BUPost>();
        for (int i = 0; i < array.length(); i++)
            try {
                list.add(new BUPost(array.getJSONObject(i)));
            } catch (JSONException e) {
                Log.w("JSONError", "Error>>\n" + array.toString(), e);
            }
        return list;
    }
}
