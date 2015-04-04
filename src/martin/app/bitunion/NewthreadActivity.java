package martin.app.bitunion;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.json.JSONException;
import org.json.JSONObject;

import martin.app.bitunion.util.BUAppUtils;
import martin.app.bitunion.util.PostMethod;
import martin.app.bitunion.util.BUAppUtils.Result;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.support.v4.app.NavUtils;

public class NewthreadActivity extends ActionBarActivity {

    private EditText subjectbox;
    private EditText messagebox;
    private Button clearButton;
    private Button sendButton;
    private String title;
    private String action;
    private int fid = 0;
    private String tid = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newthread);

        subjectbox = (EditText) findViewById(R.id.newthread_subject);
        messagebox = (EditText) findViewById(R.id.newthread_message);
        subjectbox.setOnFocusChangeListener(new MyOnFocusChangeListener());
        messagebox.setOnFocusChangeListener(new MyOnFocusChangeListener());

        clearButton = (Button) findViewById(R.id.newthread_clear);
        sendButton = (Button) findViewById(R.id.newthread_send);
        clearButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                subjectbox.setText("");
                messagebox.setText("");
            }
        });
        sendButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (action.equals("newthread"))
                    sendMessage(subjectbox.getText().toString(), messagebox.getText().toString());
                else
                    sendMessage(messagebox.getText().toString());
            }
        });

        // Parse parameters sent by parent activity
        Intent intent = getIntent();
        action = intent.getStringExtra("action");
        Log.v("NewthreadActivity", "action>>" + action);
        if (action.equals("newthread")){
            fid = intent.getIntExtra("fid", 0);
            title = intent.getStringExtra("forumname") + " - 发新话题";
        }
        if (action.equals("newpost")){
            subjectbox.setFocusable(false);
            subjectbox.setBackgroundResource(R.drawable.edittext_background_disable);
            tid = intent.getStringExtra("tid");
            String m = intent.getStringExtra("message");
            messagebox.setText(m);
            messagebox.setSelection(messagebox.getText().toString().length());
            title = "tid=" + tid + " - 高级回复";
        }
        // Show the Up button in the action bar.
        setupActionBar();


    }

    private void sendMessage(String subject, String message){
        if (subject == null || subject.isEmpty()) {
            subjectbox.setError("主题不能为空");
            return;
        }
        if (message == null || message.isEmpty()) {
            messagebox.setError("回复不能为空");
            return;
        }
        if (message.length() < 5) {
            messagebox.setError("内容长度不能小于5");
            return;
        }
        if (MainActivity.settings.showsigature)
            message += BUAppUtils.CLIENTMESSAGETAG;
        new NewContentTask(subject, message, fid).execute();
    }

    private void sendMessage(String message) {
        if (message == null || message.isEmpty()) {
            messagebox.setError("回复不能为空");
            return;
        }
        if (MainActivity.settings.showsigature)
            message += BUAppUtils.CLIENTMESSAGETAG;
        new NewContentTask(message, tid).execute();
    }

    class MyOnFocusChangeListener implements View.OnFocusChangeListener{

        @Override
        public void onFocusChange(View v, boolean focus) {
            if (focus)
                ((EditText) v).setBackgroundResource(R.drawable.edittext_background_selected);
            else
                ((EditText) v).setBackgroundResource(R.drawable.edittext_background);
        }

    }

    /**
     * Set up the {@link android.app.ActionBar}.
     */
    private void setupActionBar() {

        getSupportActionBar().setTitle(title);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.newthread, menu);
        return true;
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

    private class NewContentTask extends AsyncTask<Void, Void, Result> {

        PostMethod postMethod = new PostMethod();
        String subject = "";
        String message = "";
        String tid = null;
        int fid = 0;

        public NewContentTask(String m, String tid) {
            message = m;
            this.tid = tid;
        }

        public NewContentTask(String s, String m, int fid) {
            subject = s;
            message = m;
            this.fid = fid;
        }

        @Override
        protected Result doInBackground(Void... params) {
            JSONObject postReq = new JSONObject();
            try {
                if (tid != null && !tid.isEmpty()) {
                    postReq.put("action", "newreply");
                    postReq.put("username", URLEncoder.encode(
                            MainActivity.settings.mUsername, "utf-8"));
                    postReq.put("session", MainActivity.settings.mSession);
                    postReq.put("tid", tid);
                    postReq.put("message", URLEncoder.encode(message, "utf-8"));
                    postReq.put("attachment", 0);
                    return postMethod.sendPost(BUAppUtils.getUrl(MainActivity.settings.mNetType, BUAppUtils.NEWPOST), postReq);
                }
                if (fid != 0){
                    postReq.put("action", "newthread");
                    postReq.put("username", URLEncoder.encode(
                            MainActivity.settings.mUsername, "utf-8"));
                    postReq.put("session", MainActivity.settings.mSession);
                    postReq.put("fid", fid);
                    postReq.put("subject", URLEncoder.encode(subject, "utf-8"));
                    postReq.put("message", URLEncoder.encode(message, "utf-8"));
                    postReq.put("attachment", 0);
                    return postMethod.sendPost(BUAppUtils.getUrl(MainActivity.settings.mNetType, BUAppUtils.NEWTHREAD), postReq);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(final Result result) {
            switch (result) {
                default:
                    return;
                case FAILURE:
                    showToast(BUAppUtils.POSTFAILURE);
                    return;
                case NETWRONG:
                    showToast(BUAppUtils.NETWRONG);
                    return;
                case UNKNOWN:
                    return;
                case SUCCESS_EMPTY:
                    if (action.equals("newpost"))
                        showToast(BUAppUtils.POSTSUCCESS);
                    if (action.equals("newthread"))
                        showToast("发送成功，刷新查看新帖");
                    finish();
            }
        }

    }

    private void showToast(String text) {
        Toast.makeText(NewthreadActivity.this, text, Toast.LENGTH_SHORT).show();
    }

}
