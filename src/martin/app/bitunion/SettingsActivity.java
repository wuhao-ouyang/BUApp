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

import java.io.IOException;

import martin.app.bitunion.util.Constants;
import martin.app.bitunion.util.Utils;

public class SettingsActivity extends ActionBarActivity {

    private TextView titleTextView;
    private SeekBar titleSeekBar;
    private TextView contentTextView;
    private SeekBar contentSeekBar;
    private Switch nettypeSwitch;
    private CheckBox showsig;
    private CheckBox showimg;
    private CheckBox referat;

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

        titleTextView = (TextView) findViewById(R.id.setting_titletextview);
        titleSeekBar = (SeekBar) findViewById(R.id.seekbar_titletextsize);
        contentTextView = (TextView) findViewById(R.id.setting_contenttextview);
        contentSeekBar = (SeekBar) findViewById(R.id.seekbar_contenttextsize);
        showsig = (CheckBox) findViewById(R.id.setting_showsig);
        showimg = (CheckBox) findViewById(R.id.setting_showimg);
        referat = (CheckBox) findViewById(R.id.setting_refat);
        nettypeSwitch = (Switch) findViewById(R.id.netswitch);

        nettypeSwitch.setChecked(BUApplication.settings.netType == 1);
        titleSeekBar
                .setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                    }

                    @Override
                    public void onProgressChanged(SeekBar seekBar,
                                                  int progress, boolean fromUser) {
                        BUApplication.settings.titletextsize = progress + 10;
                        titleTextView.setText("帖子标题字体大小" +
                                BUApplication.settings.titletextsize + "\t(推荐" + RECOMMENDTITESIZE + ")");
                        titleTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, BUApplication.settings.titletextsize);
                    }
                });
        titleSeekBar.setProgress(BUApplication.settings.titletextsize - 10);
        // contentTextView.setText("帖子内容文本大小" + contenttextsize + "\t(推荐"+
        // RECOMMENDCONTENTSIZE +")");
        // contentTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP,
        // contenttextsize);
        contentSeekBar
                .setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                    }

                    @Override
                    public void onProgressChanged(SeekBar seekBar,
                                                  int progress, boolean fromUser) {
                        BUApplication.settings.contenttextsize = progress + 10;
                        contentTextView.setText("帖子内容字体大小" + BUApplication.settings.contenttextsize
                                + "\t(推荐" + RECOMMENDCONTENTSIZE + ")");
                        contentTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP,
                                BUApplication.settings.contenttextsize);
                    }
                });
        contentSeekBar.setProgress(BUApplication.settings.contenttextsize - 10);
        nettypeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                Log.i("SettingsActivity", "外网模式>>" + isChecked);
                BUApplication.settings.netType = isChecked ? Constants.OUTNET : Constants.BITNET;
            }
        });

        showsig.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                Log.i("SettingsActivity", "显示签名>>" + isChecked);
                BUApplication.settings.showSignature = isChecked;
            }
        });
        showsig.setChecked(BUApplication.settings.showSignature);
        showimg.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                Log.i("SettingsActivity", "显示图片>>" + isChecked);
                BUApplication.settings.showImage = isChecked;
            }
        });
        showimg.setChecked(BUApplication.settings.showImage);
        referat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                Log.i("SettingsActivity", "引用At>>" + isChecked);
                BUApplication.settings.useReferAt = isChecked;
            }
        });
        referat.setChecked(BUApplication.settings.useReferAt);
    }

    protected void onDestroy() {
        BUApplication.settings.writePreference(this);
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
