package martin.app.bitunion;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.json.JSONException;
import org.json.JSONObject;

import martin.app.bitunion.util.BUAppUtils;
import martin.app.bitunion.util.PostMethod;
import martin.app.bitunion.util.BUAppUtils.Result;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.view.MenuItem;

/**
 * Activity which displays a login screen to the user, offering registration as
 * well.
 */
public class LoginActivity extends Activity {

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mLoginTask = null;

    // Values for username and password at the time of the login attempt.
    String mUsername;
    String mPassword;
    String mSession = "";
    int mNetType;

    // UI references.
    private EditText mUsernameView;
    private EditText mPasswordView;
    private View mLoginFormView;
    private View mLoginStatusView;
    private TextView mLoginStatusMessageView;
    private RadioGroup netGroup;
    private ProgressDialog progressDialog = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        getActionBar().setTitle("登录");
        setupActionBar();

        readConfig();
        // Set up the default login form.
        mUsernameView = (EditText) findViewById(R.id.username);
        mUsernameView.setText(mUsername);
        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setText(mPassword);

        netGroup = (RadioGroup) findViewById(R.id.radioGroup1);
        netGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup arg0, int arg1) {
                if (arg1 == R.id.radio_in) {
                    mNetType = BUAppUtils.BITNET;
                } else if (arg1 == R.id.radio_out) {
                    mNetType = BUAppUtils.OUTNET;
                }
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
//		mLoginStatusView = findViewById(R.id.login_status);
//		mLoginStatusMessageView = (TextView) findViewById(R.id.login_status_message);
        progressDialog = new ProgressDialog(this, R.style.ProgressDialog);
        progressDialog.setMessage("登录中...");

        findViewById(R.id.sign_in_button).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        attemptLogin();
                    }
                });
    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void setupActionBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            // Show the Up button in the action bar.
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();

                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.login, menu);
        return true;
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    public void attemptLogin() {
        if (mLoginTask != null) {
            return;
        }

        // Reset errors.
        mUsernameView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        mUsername = mUsernameView.getText().toString();
        mPassword = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password.
        if (TextUtils.isEmpty(mPassword)) {
            mPasswordView.setError(getString(R.string.error_field_required));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid username.
        if (TextUtils.isEmpty(mUsername)) {
            mUsernameView.setError(getString(R.string.error_field_required));
            focusView = mUsernameView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
//			mLoginStatusMessageView.setText(R.string.login_progress_signing_in);
            showProgress(true);
            mLoginTask = new UserLoginTask();
            mLoginTask.execute((Void) null);
        }
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        if (show)
            progressDialog.show();
        else
            progressDialog.dismiss();
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Result> {

        PostMethod postMethod = new PostMethod();

        @Override
        protected Result doInBackground(Void... params) {
            JSONObject postReq = new JSONObject();
            try {
                postReq.put("action", "login");
                postReq.put("username", URLEncoder.encode(mUsername, "utf-8"));
                postReq.put("password", mPassword);
                return postMethod.sendPost(
                        BUAppUtils.getUrl(MainActivity.settings.mNetType,
                                BUAppUtils.REQ_LOGGING), postReq);
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return Result.UNKNOWN;
        }

        @Override
        protected void onPostExecute(final Result result) {
            mLoginTask = null;
            showProgress(false);

            switch (result) {
                default:
                    return;
                case FAILURE:
                    Toast.makeText(LoginActivity.this, BUAppUtils.LOGINFAIL, Toast.LENGTH_SHORT)
                            .show();
                    return;
                case NETWRONG:
                    Toast.makeText(LoginActivity.this, BUAppUtils.NETWRONG, Toast.LENGTH_SHORT)
                            .show();
                    return;
                case SUCCESS:
            }
            saveConfig();
            try {
                mSession = postMethod.jsonResponse.getString("session");
                Intent intent = new Intent();
                intent.putExtra("session", mSession);
                setResult(BUAppUtils.MAIN_RESULT, intent);
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            finish();
        }

        @Override
        protected void onCancelled() {
            mLoginTask = null;
            showProgress(false);
        }
    }

    public void saveConfig() {
        SharedPreferences config = getSharedPreferences("config", MODE_PRIVATE);
        SharedPreferences.Editor editor = config.edit();
        editor.putInt("nettype", mNetType);
        editor.putString("username", mUsername);
        editor.putString("password", mPassword);
        editor.commit();
    }

    public void readConfig() {
        SharedPreferences config = getSharedPreferences("config", MODE_PRIVATE);
        mNetType = config.getInt("nettype", BUAppUtils.OUTNET);
        mUsername = config.getString("username", null);
        mPassword = config.getString("password", null);
    }
}
