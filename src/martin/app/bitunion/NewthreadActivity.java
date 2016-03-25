package martin.app.bitunion;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import martin.app.bitunion.util.BUApi;
import martin.app.bitunion.util.CommonIntents;
import martin.app.bitunion.util.Devices;
import martin.app.bitunion.util.ToastUtil;
import martin.app.bitunion.util.UploadProgressListener;
import martin.app.bitunion.util.Utils;

public class NewthreadActivity extends BaseContentActivity implements View.OnClickListener, TextWatcher, UploadProgressListener {
    public static final String ACTION_NEW_POST = "NewthreadActivity.ACTION_NEW_POST";
    public static final String ACTION_NEW_THREAD = "NewthreadActivity.ACTION_NEW_THREAD";

    private static final int REQCODE_IMAGE = 13;
    private static final int REQCODE_PHOTO = 17;
    private static final int REQCODE_FILE = 19;

    private EditText mSubET;
    private EditText mMsgET;
    private MenuItem mSendIc;
    private ViewGroup mAttachLyt;
    private TextView mAttachTV;
    private ProgressBar mProgress;
    private View mClearBtn;
    private View mImageBtn;
    private View mPhotoBtn;
    private View mFileBtn;

    private String title;
    private String action;
    private int fid = 0;
    private int tid = 0;

    private File mAttachFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newthread);

        // Init views and buttons
        mSubET = (EditText) findViewById(R.id.newthread_subject);
        mMsgET = (EditText) findViewById(R.id.newthread_message);
        mAttachLyt = (ViewGroup) findViewById(R.id.lyt_attach_descr);
        mAttachTV = (TextView) findViewById(R.id.txtVw_attach_descr);
        mProgress = (ProgressBar) findViewById(R.id.progressBar);
        mClearBtn = findViewById(R.id.newthread_clear);
        mImageBtn = findViewById(R.id.newthread_image);
        mPhotoBtn = findViewById(R.id.newthread_photo);
        mFileBtn = findViewById(R.id.newthread_file);

        // Parse parameters sent by parent activity
        Intent intent = getIntent();
        action = intent.getStringExtra(CommonIntents.EXTRA_ACTION);
        Log.v("NewthreadActivity", "action >> " + action);
        if (action.equals(ACTION_NEW_THREAD)) {
            fid = intent.getIntExtra(CommonIntents.EXTRA_FID, 0);
            title = intent.getStringExtra(CommonIntents.EXTRA_FORUM_NAME) + " - 发新话题";
        } else if (action.equals(ACTION_NEW_POST)) {
            mSubET.setEnabled(false);
            tid = intent.getIntExtra(CommonIntents.EXTRA_TID, 0);
            String m = intent.getStringExtra(CommonIntents.EXTRA_MESSAGE);
            mMsgET.setText(m);
            mMsgET.setSelection(mMsgET.getText().toString().length());
            title = "tid=" + tid + " - 高级回复";
        }
        // Show the Up button in the action bar.
        setupActionBar();

        // Set up listeners
        mSubET.addTextChangedListener(this);
        mMsgET.addTextChangedListener(this);
        mClearBtn.setOnClickListener(this);
        mImageBtn.setOnClickListener(this);
        mPhotoBtn.setOnClickListener(this);
        mFileBtn.setOnClickListener(this);
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void afterTextChanged(Editable s) {
        if (mSendIc == null)
            return;
        if (ACTION_NEW_POST.equals(action)) {
            mSendIc.setEnabled(!mMsgET.getText().toString().trim().isEmpty());
        } else if (ACTION_NEW_THREAD.equals(action)) {
            mSendIc.setEnabled(!mSubET.getText().toString().isEmpty() && !mMsgET.getText().toString().trim().isEmpty());
        }
    }

    private void sendMessage(String subject, String message) {
        if (message == null || message.length() < 5) {
            mMsgET.setError("内容长度不能小于5");
            return;
        }
        if (BUApp.settings.showSignature)
            message += getString(R.string.buapp_client_postfix).replace("$device_name", Devices.getDeviceName());
        mSendIc.setEnabled(false);
        ToastUtil.showToast(R.string.message_sending);
        BUApi.postNewThread(fid, subject, message, mAttachFile, this, mResponseListener, mErrorListener);
    }

    private void sendMessage(String message) {
        if (BUApp.settings.showSignature)
            message += getString(R.string.buapp_client_postfix).replace("$device_name", Devices.getDeviceName());
        mSendIc.setEnabled(false);
        ToastUtil.showToast(R.string.message_sending);
        BUApi.postNewPost(tid, message, mAttachFile, this, mResponseListener, mErrorListener);
    }

    @Override
    public void onProgressUpdate(int progress) {
        mProgress.setProgress(progress);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK)
            return;
        switch (requestCode) {
            case REQCODE_IMAGE:
                String path = Utils.getRealPathFromUri(this, data.getData());
                mAttachFile = new File(path);
                if (mAttachFile == null) {
                    ToastUtil.showToast(R.string.image_attach_fail);
                    break;
                } else {
                    ToastUtil.showToast(R.string.image_attached);
                }
                showAttachDetail();
                break;

            case REQCODE_FILE:
                String fpath = Utils.getRealPathFromUri(this, data.getData());
                mAttachFile = new File(fpath);
                if (mAttachFile == null) {
                    ToastUtil.showToast(R.string.file_attach_fail);
                    break;
                } else {
                    ToastUtil.showToast(R.string.file_attached);
                }
                showAttachDetail();
                break;

            case REQCODE_PHOTO:
                if (mAttachFile == null) {
                    ToastUtil.showToast(R.string.image_attach_fail);
                    break;
                } else {
                    ToastUtil.showToast(R.string.image_attached);
                }
                showAttachDetail();
                break;
        }
    }

    private void showAttachDetail() {
        StringBuilder attBuilder = new StringBuilder();
        attBuilder.append(mAttachFile.getName());
        attBuilder.append("\t");
        attBuilder.append(Utils.getReadableFileSize(mAttachFile.length()));
        mAttachTV.setText(attBuilder);
        mProgress.setProgress(0);
        mAttachLyt.setVisibility(View.VISIBLE);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.newthread_clear:
                mAttachTV.setText("");
                mAttachLyt.setVisibility(View.GONE);
                mAttachFile = null;
                break;

            case R.id.newthread_image:
                Intent imgIntent = new Intent(Intent.ACTION_GET_CONTENT);
                imgIntent.setType("image/*");
                imgIntent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                if (imgIntent.resolveActivity(getPackageManager()) != null)
                    startActivityForResult(imgIntent, REQCODE_IMAGE);
                break;

            case R.id.newthread_photo:
                StringBuilder photoName = new StringBuilder(getString(R.string.app_name));
                photoName.append('_');
                photoName.append(new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()));
                File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                try {
                    mAttachFile = File.createTempFile(photoName.toString(), ".jpg", storageDir);
                    Intent photoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    photoIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mAttachFile));
                    startActivityForResult(photoIntent, REQCODE_PHOTO);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;

            case R.id.newthread_file:
                Intent fileIntent = new Intent(Intent.ACTION_GET_CONTENT);
                fileIntent.setType("*/*");
                fileIntent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                if (fileIntent.resolveActivity(getPackageManager()) != null)
                    startActivityForResult(fileIntent, REQCODE_FILE);
                break;
        }
    }

    private void setupActionBar() {
        getSupportActionBar().setTitle(title);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.newthread, menu);
        mSendIc = menu.findItem(R.id.action_send);
        mSendIc.setTitle(R.string.menu_action_post);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.action_send:
                if (ACTION_NEW_THREAD.equals(action))
                    sendMessage(mSubET.getText().toString(), mMsgET.getText().toString());
                else
                    sendMessage(mMsgET.getText().toString());
        }
        return true;
    }

    private Response.Listener<JSONObject> mResponseListener = new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject response) {
            if (BUApi.getResult(response) == BUApi.Result.SUCCESS) {
                ToastUtil.showToast(R.string.message_sent_success);
                finish();
            } else {
                ToastUtil.showToast(R.string.message_sent_fail);
            }
        }
    };

    private Response.ErrorListener mErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError volleyError) {
            ToastUtil.showToast(R.string.network_unknown);
        }
    };
}
