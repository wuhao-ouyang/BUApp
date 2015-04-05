package martin.app.bitunion.util;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

import martin.app.bitunion.model.BUPost;
import martin.app.bitunion.model.BUThread;

public class DataParser {

    public static ArrayList<BUThread> jsonToThreadlist(JSONArray array) {
        ArrayList<BUThread> list = new ArrayList<BUThread>();
        for (int i = 0; i < array.length(); i++)
            try {
                list.add(new BUThread(array.getJSONObject(i)));
            } catch (JSONException e) {
                Log.e("JSONError", "Error>>\n" + array.toString());
                e.printStackTrace();
            }
        // Log.v("page", "array parsed");
        return list;
    }

    public static ArrayList<BUPost> jsonToPostlist(JSONArray array) {
        ArrayList<BUPost> list = new ArrayList<BUPost>();
        for (int i = 0; i < array.length(); i++)
            try {
                list.add(new BUPost(array.getJSONObject(i)));
            } catch (JSONException e) {
                Log.e("JSONError", "Error>>\n" + array.toString());
                e.printStackTrace();
            }
        // Log.v("page", "array parsed");
        return list;
    }
}
