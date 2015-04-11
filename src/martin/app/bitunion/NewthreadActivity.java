package martin.app.bitunion;

import org.json.JSONObject;

import martin.app.bitunion.util.BUApi;
import martin.app.bitunion.util.CommonIntents;
import martin.app.bitunion.util.Utils.Result;

import android.os.Bundle;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;

public class NewthreadActivity extends ActionBarActivity {
    public static final String ACTION_NEW_POST = "NewthreadActivity.ACTION_NEW_POST";
    public static final String ACTION_NEW_THREAD = "NewthreadActivity.ACTION_NEW_THREAD";

    private EditText subjectbox;
    private EditText messagebox;
    private Button clearButton;
    private Button sendButton;
    private String title;
    private String action;
    private int fid = 0;
    private int tid = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newthread);

        subjectbox = (EditText) findViewById(R.id.newthread_subject);
        messagebox = (EditText) findViewById(R.id.newthread_message);

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
                if (action.equals(ACTION_NEW_THREAD))
                    sendMessage(subjectbox.getText().toString(), messagebox.getText().toString());
                else
                    sendMessage(messagebox.getText().toString());
            }
        });

        // Parse parameters sent by parent activity
        Intent intent = getIntent();
        action = intent.getStringExtra(CommonIntents.EXTRA_ACTION);
        Log.v("NewthreadActivity", "action>>" + action);
        if (action.equals(ACTION_NEW_THREAD)){
            fid = intent.getIntExtra(CommonIntents.EXTRA_FID, 0);
            title = intent.getStringExtra(CommonIntents.EXTRA_FORUM_NAME) + " - 发新话题";
        }
        if (action.equals(ACTION_NEW_POST)){
            subjectbox.setEnabled(false);
            tid = intent.getIntExtra(CommonIntents.EXTRA_TID, 0);
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
        if (BUApplication.settings.showSignature)
            message += getString(R.string.buapp_client_postfix);
        BUApi.postNewThread(fid, subject, message, mResponseListener, mErrorListener);
    }

    private void sendMessage(String message) {
        if (message == null || message.isEmpty()) {
            messagebox.setError("回复不能为空");
            return;
        }
        if (BUApplication.settings.showSignature)
            message += getString(R.string.buapp_client_postfix);
        BUApi.postNewPost(tid, message, mResponseListener, mErrorListener);
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

    private Response.Listener<JSONObject> mResponseListener = new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject response) {
            if (BUApi.getResult(response) == Result.SUCCESS) {
                Toast.makeText(getApplicationContext(), R.string.message_sent_success, Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(getApplicationContext(), R.string.message_sent_fail, Toast.LENGTH_SHORT).show();
            }
        }
    };

    private Response.ErrorListener mErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError volleyError) {
            Toast.makeText(getApplicationContext(), R.string.network_unknown, Toast.LENGTH_SHORT).show();
        }
    };
}
