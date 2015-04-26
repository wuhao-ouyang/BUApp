package martin.app.bitunion.util;

import android.os.AsyncTask;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.apache.tika.Tika;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

public class MultipartAsynTask extends AsyncTask<Void, Integer, Boolean> {
    private static final String TAG = BUApi.class.getSimpleName();

    private final String mPath;
    private final Map<String, String> mParams;
    private final File mAttachFile;
    private final UploadProgressListener mProgressListener;
    private final Response.Listener<JSONObject> mReponseListener;
    private final Response.ErrorListener mErrorListener;

    private NetworkResponse mResponse;
    private VolleyError mError;

    public MultipartAsynTask(String path, Map<String, String> params, File attachment, UploadProgressListener progressListener, Response.Listener<JSONObject> responseListener, Response.ErrorListener errorListener) {
        mPath = path;
        mParams = params;
        mAttachFile = attachment;
        mProgressListener = progressListener;
        mReponseListener = responseListener;
        mErrorListener = errorListener;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        HttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(mPath);

        JSONObject postReq = new JSONObject();
        try {
            for (Map.Entry<String, String> entry : mParams.entrySet()) {
                if (entry.getValue() == null)
                    continue;
                postReq.put(entry.getKey(), URLEncoder.encode(entry.getValue(), HTTP.UTF_8));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        final long totalSize = mAttachFile.length();
        MultipartEntity reqEntity = new MultipartEntity() {
            @Override
            public void writeTo(final OutputStream outstream) throws IOException {
                super.writeTo(new WatchableOutputStream(outstream, new ProgressListener() {
                    @Override
                    public void transferred(long num) {
                        publishProgress((int) ((num / (float) totalSize) * 100));
                    }
                }));
            }

        };
        try {
            reqEntity.addPart("json", new StringBody(postReq.toString()));
            reqEntity.addPart("attach", new FileBody(mAttachFile, new Tika().detect(mAttachFile)));
            httpPost.setEntity(reqEntity);
            HttpResponse response = httpClient.execute(httpPost);
            // Read data, the response data must not be too large
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            byte[] data = new byte[1024];
            int len = 0;
            InputStream is = response.getEntity().getContent();
            while ((len = is.read(data)) != -1)
                outputStream.write(data, 0, len);
            String responseData = new String(outputStream.toByteArray());
            // Get headers
            Map<String, String> headers = new HashMap<String, String>();
            for (Header header : response.getAllHeaders())
                headers.put(header.getName(), header.getValue());
            mResponse = new NetworkResponse(response.getStatusLine().getStatusCode(), responseData, headers);
            Log.v(TAG, mPath + " >> " + responseData);
            return true;
        } catch (UnsupportedEncodingException e) {
            mError = new VolleyError(e);
            return false;
        } catch (ClientProtocolException e) {
            mError = new VolleyError(e);
            return false;
        } catch (IOException e) {
            mError = new VolleyError(e);
            return false;
        }
    }

    @Override
    protected void onProgressUpdate(Integer... progress) {
        if (mProgressListener != null)
            mProgressListener.onProgressUpdate(progress[0]);
    }

    @Override
    protected void onPostExecute(Boolean result) {
        if (result) {
            if (mResponse.statusCode == 200) {
                try {
                    JSONObject response = new JSONObject(mResponse.data);
                    mReponseListener.onResponse(response);
                } catch (JSONException e) {
                    mErrorListener.onErrorResponse(new VolleyError(e));
                }
            } else {
                mError = new VolleyError(mResponse.statusCode + ": " + mPath);
                Log.e(TAG, "Failed uploading attachment.", mError);
                mErrorListener.onErrorResponse(mError);
            }
        } else {
            Log.e(TAG, "Failed uploading attachment.", mError);
            mErrorListener.onErrorResponse(mError);
        }
    }

    interface ProgressListener {
        void transferred(long num);
    }

    private static class NetworkResponse {
        final int statusCode;
        final String data;
        final Map<String ,String> headers;
        NetworkResponse(int statusCode, String data, Map<String ,String> headers) {
            this.statusCode = statusCode;
            this.data = data;
            this.headers = headers;
        }
    }

    private static class WatchableOutputStream extends FilterOutputStream {

        private final ProgressListener listener;
        private long transferred;

        public WatchableOutputStream(final OutputStream out, final ProgressListener listener) {
            super(out);
            this.listener = listener;
            this.transferred = 0;
        }

        public void write(byte[] b, int off, int len) throws IOException {
            out.write(b, off, len);
            this.transferred += len;
            this.listener.transferred(this.transferred);
        }

        public void write(int b) throws IOException {
            out.write(b);
            this.transferred++;
            this.listener.transferred(this.transferred);
        }
    }
}
