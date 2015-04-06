package martin.app.bitunion;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import org.json.JSONException;
import org.json.JSONObject;

import martin.app.bitunion.util.BUApiHelper;
import martin.app.bitunion.util.BUAppUtils.Result;
import martin.app.bitunion.util.BUAppUtils;
import martin.app.bitunion.model.BUUserInfo;
import martin.app.bitunion.util.HtmlImageGetter;
import martin.app.bitunion.util.PostMethod;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v4.app.DialogFragment;

import com.android.volley.Response;
import com.android.volley.VolleyError;

public class MyinfoActivity extends ActionBarActivity implements DialogInterface.OnClickListener {

    private ImageView mAvatar;
    private TextView mUsername;
    private TextView mGroup;
    private TextView mCredit;
    private TextView mThreadnum;
    private TextView mPostnum;
    private TextView mRegdate;
    private TextView mLastactive;
    private TextView mEmail;
    private TextView mSignt;

    private ProgressDialog progressDialog = null;

    private static Drawable imageDrawable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_myinfo);

        // Show the Up button in the action bar.
        setupActionBar();

        if (savedInstanceState != null && !savedInstanceState.isEmpty())
            return;

        imageDrawable = getResources()
                .getDrawable(R.drawable.ic_action_picture);

        mAvatar = (ImageView) findViewById(R.id.myinfo_avatar);
        mUsername = (TextView) findViewById(R.id.myinfo_username);
        mGroup = (TextView) findViewById(R.id.myinfo_group);
        mCredit = (TextView) findViewById(R.id.myinfo_credit);
        mThreadnum = (TextView) findViewById(R.id.myinfo_threadnum);
        mPostnum = (TextView) findViewById(R.id.myinfo_postnum);
        mRegdate = (TextView) findViewById(R.id.myinfo_regdate);
        mLastactive = (TextView) findViewById(R.id.myinfo_lastvisit);
        mEmail = (TextView) findViewById(R.id.myinfo_email);
        mSignt = (TextView) findViewById(R.id.myinfo_signature);

        if (BUApplication.settings.mSession != null
                && !BUApplication.settings.mSession.isEmpty()) {
            progressDialog = new ProgressDialog(this, R.style.ProgressDialog);
            progressDialog.show();
            new MyinfoReadTask().execute();
        } else
            showToast("请等待登录后重新尝试");
    }

    private void setInfoContent(BUUserInfo info) {
        new GetAvatarTask(mAvatar, info.getAvatar()).execute();
        mUsername.setText(info.getUsername());
        mGroup.setText("用户组：" + info.getStatus());
        mCredit.setText("积分：" + info.getCredit());
        mThreadnum.setText("主题数：" + info.getThreadnum());
        mPostnum.setText("发帖数：" + info.getPostnum());
        mRegdate.setText("注册日期：" + info.getRegdate());
        mLastactive.setText("上次登录：" + info.getLastvisit());
        mEmail.setText("E-mail：" + info.getEmail());
        mSignt.setText(Html.fromHtml("签名：<br>" + info.getSignature(),
                new HtmlImageGetter(this, mSignt), null));
        mSignt.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private void updateImage() {
        mAvatar.setImageDrawable(imageDrawable);
    }

    class GetAvatarTask extends AsyncTask<Void, Void, Result> {

        String path;
        ImageView mView;

        public GetAvatarTask(ImageView v, String url) {
            mView = v;
            BUApplication.settings.setNetType(BUApplication.settings.mNetType);
            path = url;
            path = path.replaceAll("(http://)?(www|v6|kiss|out).bitunion.org",
                    BUApplication.settings.ROOTURL);
            path = path.replaceAll("^images/", BUApplication.settings.ROOTURL
                    + "/images/");
            path = path.replaceAll("^attachments/",
                    BUApplication.settings.ROOTURL + "/attachments/");
            Log.v("MyinfoActivity", "GetAvatarTask>>" + path);
        }

        @Override
        protected Result doInBackground(Void... arg0) {

            InputStream is = null;
            try {
                is = BUAppUtils.getImageVewInputStream(path);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return null;
            }
            Drawable drawable = Drawable.createFromStream(is, path);
            if (drawable == null)
                drawable = getResources().getDrawable(R.drawable.noavatar);
            float asratio = (float) drawable.getIntrinsicWidth()
                    / drawable.getIntrinsicHeight();
            if (asratio > 1)
                drawable.setBounds(0, 0, mAvatar.getWidth(),
                        (int) (mAvatar.getWidth() / asratio));
            else
                drawable.setBounds(0, 0, (int) (mAvatar.getHeight() * asratio),
                        mAvatar.getHeight());
            imageDrawable = drawable;
            return null;
        }

        @Override
        protected void onPostExecute(Result result) {
            updateImage();
            super.onPostExecute(result);
        }

    }

    class MyinfoReadTask extends AsyncTask<Void, Void, Result> {

        PostMethod postMethod = new PostMethod();

        @Override
        protected Result doInBackground(Void... params) {
            JSONObject postReq = new JSONObject();
            try {
                postReq.put("action", "profile");
                postReq.put("username", URLEncoder.encode(
                        BUApplication.settings.mUsername, "utf-8"));
                postReq.put("session", BUApplication.settings.mSession);
                postReq.put("queryusername", URLEncoder.encode(
                        BUApplication.settings.mUsername, "utf-8"));
                return postMethod.sendPost(
                        BUAppUtils.getUrl(BUApplication.settings.mNetType,
                                BUAppUtils.REQ_PROFILE), postReq);
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Result result) {
            switch (result) {
                default:
                    return;
                case FAILURE:
                    new UserLoginTask().execute();
                    return;
                case NETWRONG:
                    showToast(BUAppUtils.NETWRONG);
                    return;
                case UNKNOWN:
                    return;
                case SUCCESS:
            }
            try {
                BUUserInfo info = new BUUserInfo(
                        postMethod.jsonResponse.getJSONObject("memberinfo"));
                setInfoContent(info);
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            progressDialog.dismiss();
        }

    }

    /**
     * Set up the {@link android.app.ActionBar}.
     */
    private void setupActionBar() {
        getSupportActionBar().setTitle("我的联盟");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.myinfo, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_refresh:
                if (BUApplication.settings.mSession == null
                        || BUApplication.settings.mSession.isEmpty()) {
                    new UserLoginTask().execute();
                    progressDialog.show();
                } else
                    new MyinfoReadTask().execute();
                return true;
            case R.id.action_logout:
                AlertDialog dialog = new AlertDialog.Builder(this)
                        .setTitle(R.string.logout_confirm_title)
                        .setMessage(R.string.logout_confirm_message)
                        .setPositiveButton(R.string.logout_confirm_button, this)
                        .setNegativeButton(R.string.logout_cancel_button, this)
                        .create();
                dialog.show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (which == DialogInterface.BUTTON_POSITIVE)
            BUApiHelper.logoutUser(new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    if (BUApiHelper.getResult(response) == Result.SUCCESS) {
                        BUApiHelper.clearUser();
                        finish();
                    } else {
                        Toast.makeText(getApplicationContext(), R.string.logout_error, Toast.LENGTH_SHORT).show();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(getApplicationContext(), R.string.network_unknown, Toast.LENGTH_SHORT).show();
                }
            });
        else
            dialog.dismiss();
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    private class UserLoginTask extends AsyncTask<Void, Void, Result> {

        PostMethod postMethod = new PostMethod();

        @Override
        protected Result doInBackground(Void... params) {

            JSONObject postReq = new JSONObject();
            try {
                postReq.put("action", "login");
                postReq.put("username", URLEncoder.encode(
                        BUApplication.settings.mUsername, "utf-8"));
                postReq.put("password", BUApplication.settings.mPassword);
                return postMethod.sendPost(BUAppUtils.getUrl(BUApplication.settings.mNetType, BUAppUtils.REQ_LOGGING), postReq);
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            return null;
        }

        // 处理登录结果并弹出toast显示
        @Override
        protected void onPostExecute(final Result result) {

            switch (result) {
                default:
                    return;
                case FAILURE:
                    showToast(BUAppUtils.LOGINFAIL);
                    return;
                case NETWRONG:
                    showToast(BUAppUtils.NETWRONG);
                    return;
                case UNKNOWN:
                    return;
                case SUCCESS:
            }
            try {
                BUApplication.settings.mSession = postMethod.jsonResponse
                        .getString("session");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            new MyinfoReadTask().execute();
        }
    }

    private void showToast(String text) {
        Toast.makeText(MyinfoActivity.this, text, Toast.LENGTH_SHORT).show();
    }

}
