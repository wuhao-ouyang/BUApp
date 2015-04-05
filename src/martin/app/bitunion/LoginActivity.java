package martin.app.bitunion;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.json.JSONException;
import org.json.JSONObject;

import martin.app.bitunion.util.BUApiHelper;
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
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.view.MenuItem;

import com.android.volley.Response;
import com.android.volley.VolleyError;

/**
 * Activity which displays a login screen to the user, offering registration as
 * well.
 */
public class LoginActivity extends ActionBarActivity {

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

        getSupportActionBar().setTitle("登录");
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
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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
            BUApiHelper.tryLogin(new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    switch (BUApiHelper.getResult(response)) {
                        case SUCCESS:
                        case SUCCESS_EMPTY:
                            saveConfig();
                            mSession = response.optString("session");
                            Intent intent = new Intent();
                            intent.putExtra("session", mSession);
                            setResult(RESULT_OK, intent);
                            finish();
                        default:
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                }
            });
        }
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    private void showProgress(final boolean show) {
        if (show)
            progressDialog.show();
        else
            progressDialog.dismiss();
    }

    public void saveConfig() {
        BUApplication.settings.mUsername = mUsername;
        BUApplication.settings.mPassword = mPassword;
        BUApplication.settings.mNetType = mNetType;
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
