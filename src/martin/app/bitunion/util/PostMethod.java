package martin.app.bitunion.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import martin.app.bitunion.util.BUAppUtils.Result;

public class PostMethod {

    public JSONObject jsonResponse = null;

    public PostMethod() {
        // TODO Auto-generated constructor stub
    }

    public Result sendPost(String path, JSONObject jsonRequest) {
        try {
            if (path.contains("/bu_newpost.php")){
                HttpParams httpParams = new BasicHttpParams();
                HttpConnectionParams.setConnectionTimeout(httpParams, 5000);
                HttpConnectionParams.setSoTimeout(httpParams, 8000);
                HttpClient httpclient = new DefaultHttpClient(httpParams);
                HttpPost httppost = new HttpPost(path);
                MultipartEntityBuilder reqEntityBuilder = MultipartEntityBuilder.create();
                reqEntityBuilder.addTextBody("json", jsonRequest.toString());

                httppost.setEntity(reqEntityBuilder.build());
                HttpResponse response = httpclient.execute(httppost);

                if (response.getStatusLine().getStatusCode() == 200){
                    String serverResponse = getServerResponse(response.getEntity().getContent());
                    jsonResponse = new JSONObject(serverResponse);
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
            urlConnection.setConnectTimeout(5000);
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
                this.jsonResponse = new JSONObject(getServerResponse(urlConnection.getInputStream()));
                String result = jsonResponse.getString("result");
                if (result.equals("success"))
                    if (jsonResponse.length() <= 1)
                        return Result.SUCCESS_EMPTY;
                    else
                        return Result.SUCCESS;
                else if (result.equals("fail"))
                    return Result.FAILURE;
            } else {
                try {
                    jsonResponse = new JSONObject(getServerResponse(urlConnection.getInputStream()));
                } catch (JSONException e) {
                }
                return Result.NETWRONG;
            }
        } catch (IOException e) {
            Log.e("PostMethod", "IOException while post", e);
            return Result.NETWRONG;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return Result.UNKNOWN;
    }

    public String getServerResponse(InputStream inputStream)
            throws UnsupportedEncodingException {
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
                e.printStackTrace();
                Log.e("PostMethod", "getServerResponse(): Transform error!");
            }
        }
        return result;
    }

}
