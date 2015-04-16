package martin.app.bitunion;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.crittercism.app.Crittercism;

import java.io.IOException;

import martin.app.bitunion.util.Constants;
import martin.app.bitunion.util.Utils;

public class SettingsActivity extends ActionBarActivity implements CompoundButton.OnCheckedChangeListener, SeekBar.OnSeekBarChangeListener {

    private TextView titleTextView;
    private SeekBar titleSeekBar;
    private TextView contentTextView;
    private SeekBar contentSeekBar;
    private Switch nettypeSwitch;
    private CheckBox showsig;
    private CheckBox showimg;
    private CheckBox referat;
    private CheckBox sendStat;

    private int RECOMMENDTITESIZE;
    private int RECOMMENDCONTENTSIZE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        // Show the Up button in the action bar.
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        float dpi = getResources().getDisplayMetrics().densityDpi;
        if (dpi > DisplayMetrics.DENSITY_HIGH) {
            RECOMMENDTITESIZE = 14;
            RECOMMENDCONTENTSIZE = 14;
        } else {
            RECOMMENDTITESIZE = 12;
            RECOMMENDCONTENTSIZE = 12;
        }
        TextView mVersion = (TextView) findViewById(R.id.settings_version);
        try {
            mVersion.setText(mVersion.getText().toString() + getPackageManager().getPackageInfo(getPackageName(), 0).versionName);
        } catch (NameNotFoundException e) {
            Log.e("SettingsActivity", "Version name not found!");
            e.printStackTrace();
        }

        nettypeSwitch = (Switch) findViewById(R.id.netswitch);
        titleTextView = (TextView) findViewById(R.id.setting_titletextview);
        titleSeekBar = (SeekBar) findViewById(R.id.seekbar_titletextsize);
        contentTextView = (TextView) findViewById(R.id.setting_contenttextview);
        contentSeekBar = (SeekBar) findViewById(R.id.seekbar_contenttextsize);
        showsig = (CheckBox) findViewById(R.id.setting_showsig);
        showimg = (CheckBox) findViewById(R.id.setting_showimg);
        referat = (CheckBox) findViewById(R.id.setting_refat);
        sendStat = (CheckBox) findViewById(R.id.setting_sendStat);

        nettypeSwitch.setOnCheckedChangeListener(this);
        nettypeSwitch.setChecked(BUApp.settings.netType == 1);

        titleSeekBar.setOnSeekBarChangeListener(this);
        titleSeekBar.setProgress(BUApp.settings.titletextsize - 10);
        contentSeekBar.setOnSeekBarChangeListener(this);
        contentSeekBar.setProgress(BUApp.settings.contenttextsize - 10);

        showsig.setOnCheckedChangeListener(this);
        showsig.setChecked(BUApp.settings.showSignature);
        showimg.setOnCheckedChangeListener(this);
        showimg.setChecked(BUApp.settings.showImage);
        referat.setOnCheckedChangeListener(this);
        referat.setChecked(BUApp.settings.useReferAt);
        sendStat.setOnCheckedChangeListener(this);
        sendStat.setChecked(BUApp.settings.sendStat);
    }

    protected void onDestroy() {
        Crittercism.setOptOutStatus(BUApp.settings.sendStat);
        BUApp.settings.writePreference(this);
        Log.i("SettingActivity", "Config Saved");
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.settings, menu);
        return true;
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        switch (seekBar.getId()) {
            case R.id.seekbar_contenttextsize:
                BUApp.settings.contenttextsize = progress + 10;
                contentTextView.setText("帖子内容字体大小" + BUApp.settings.contenttextsize + "\t(推荐" + RECOMMENDCONTENTSIZE + ")");
                contentTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, BUApp.settings.contenttextsize);
                break;
            case R.id.seekbar_titletextsize:
                BUApp.settings.titletextsize = progress + 10;
                titleTextView.setText("帖子标题字体大小" + BUApp.settings.titletextsize + "\t(推荐" + RECOMMENDTITESIZE + ")");
                titleTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, BUApp.settings.titletextsize);
                break;
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.netswitch:
                Log.i("SettingsActivity", "外网模式>>" + isChecked);
                BUApp.settings.netType = isChecked ? Constants.OUTNET : Constants.BITNET;
                break;
            case R.id.setting_showsig:
                Log.i("SettingsActivity", "显示签名>>" + isChecked);
                BUApp.settings.showSignature = isChecked;
                break;
            case R.id.setting_showimg:
                Log.i("SettingsActivity", "显示图片>>" + isChecked);
                BUApp.settings.showImage = isChecked;
                break;
            case R.id.setting_refat:
                Log.i("SettingsActivity", "引用At>>" + isChecked);
                BUApp.settings.useReferAt = isChecked;
                break;
            case R.id.setting_sendStat:
                Log.i("SettingsActivity", "发送Crittercism>>" + isChecked);
                BUApp.settings.sendStat = isChecked;
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
            case R.id.action_info:
                String message = null;
                try {
                    message = Utils.readTextFromInputStream(getAssets().open("about.txt"));
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
                new AlertDialog.Builder(this)
                        .setMessage(message)
                        .setNegativeButton("我要评价！", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent i = new Intent(Intent.ACTION_VIEW,
                                        Uri.parse("https://play.google.com/store/apps/details?id=martin.app.bitunion"));
                                startActivity(i);
                            }
                        })
                        .setPositiveButton("确定", null)
                        .setTitle("关于BUApp").create().show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
