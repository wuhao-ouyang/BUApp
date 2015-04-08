package martin.app.bitunion;

import org.json.JSONObject;

import martin.app.bitunion.util.BUApiHelper;
import martin.app.bitunion.util.BUAppUtils.Result;
import martin.app.bitunion.model.BUUserInfo;
import martin.app.bitunion.util.HtmlImageGetter;
import martin.app.bitunion.util.PostMethod;
import martin.app.bitunion.util.VolleyImageLoaderFactory;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.app.ProgressDialog;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.NetworkImageView;

public class MyinfoActivity extends ActionBarActivity implements DialogInterface.OnClickListener {

    private NetworkImageView mAvatar;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_myinfo);

        // Show the Up button in the action bar.
        setupActionBar();

        if (savedInstanceState != null && !savedInstanceState.isEmpty())
            return;

        mAvatar = (NetworkImageView) findViewById(R.id.myinfo_avatar);
        mUsername = (TextView) findViewById(R.id.myinfo_username);
        mGroup = (TextView) findViewById(R.id.myinfo_group);
        mCredit = (TextView) findViewById(R.id.myinfo_credit);
        mThreadnum = (TextView) findViewById(R.id.myinfo_threadnum);
        mPostnum = (TextView) findViewById(R.id.myinfo_postnum);
        mRegdate = (TextView) findViewById(R.id.myinfo_regdate);
        mLastactive = (TextView) findViewById(R.id.myinfo_lastvisit);
        mEmail = (TextView) findViewById(R.id.myinfo_email);
        mSignt = (TextView) findViewById(R.id.myinfo_signature);

        progressDialog = new ProgressDialog(this, R.style.ProgressDialog);

        if (BUApiHelper.isUserLoggedin()) {
            readUserInfo();
        } else {
            BUApiHelper.tryLogin(new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    if (BUApiHelper.getResult(response) == Result.SUCCESS)
                        readUserInfo();
                    else
                        showToast(getString(R.string.login_error));
                }
            }, BUApiHelper.sErrorListener);
        }
    }

    private void setInfoContent(BUUserInfo info) {
        mAvatar.setImageUrl(BUApiHelper.getImageAbsoluteUrl(info.getAvatar()), VolleyImageLoaderFactory.getImageLoader(getApplicationContext()));
        mUsername.setText(info.getUsername());
        mGroup.setText("用户组：" + info.getStatus());
        mCredit.setText("积分：" + info.getCredit());
        mThreadnum.setText("主题数：" + info.getThreadnum());
        mPostnum.setText("发帖数：" + info.getPostnum());
        mRegdate.setText("注册日期：" + info.getRegdate());
        mLastactive.setText("上次登录：" + info.getLastvisit());
        mEmail.setText("E-mail：" + info.getEmail());
        mSignt.setText(Html.fromHtml("签名：<br>" + info.getSignature(), new HtmlImageGetter(this, mSignt), null));
        mSignt.setMovementMethod(LinkMovementMethod.getInstance());
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
                if (BUApiHelper.isUserLoggedin())

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

    private void readUserInfo() {
        progressDialog.show();
        BUApiHelper.getUserProfile(null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                if (BUApiHelper.getResult(response) == Result.SUCCESS && !response.isNull("memberinfo")){
                    if (progressDialog.isShowing())
                        progressDialog.dismiss();
                    BUUserInfo info = new BUUserInfo(response.optJSONObject("memberinfo"));
                    setInfoContent(info);
                } else
                    showToast("Server Error: " + response.toString());

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (progressDialog.isShowing())
                    progressDialog.dismiss();
                showToast(getString(R.string.network_unknown));
            }
        });
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

    private void showToast(String text) {
        Toast.makeText(MyinfoActivity.this, text, Toast.LENGTH_SHORT).show();
    }

}
