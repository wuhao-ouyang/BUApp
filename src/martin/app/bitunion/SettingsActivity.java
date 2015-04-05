package martin.app.bitunion;

import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
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
import android.support.v4.app.NavUtils;

public class SettingsActivity extends ActionBarActivity {

    private TextView titleTextView;
    private SeekBar titleSeekBar;
    private TextView contentTextView;
    private SeekBar contentSeekBar;
    private Switch nettypeSwitch;
    private CheckBox showsig;
    private CheckBox showimg;
    private CheckBox referat;

    private int titletextsize;
    private int contenttextsize;
    private boolean sig;
    private boolean img;
    private boolean ref;
    private int RECOMMENDTITESIZE;
    private int RECOMMENDCONTENTSIZE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        // Show the Up button in the action bar.
        setupActionBar();

        if (BUApplication.PIXDENSITY > DisplayMetrics.DENSITY_HIGH) {
            RECOMMENDTITESIZE = 14;
            RECOMMENDCONTENTSIZE = 14;
        } else {
            RECOMMENDTITESIZE = 12;
            RECOMMENDCONTENTSIZE = 12;
        }
        titletextsize = BUApplication.settings.titletextsize;
        contenttextsize = BUApplication.settings.contenttextsize;
        sig = BUApplication.settings.showsigature;
        img = BUApplication.settings.showimage;
        ref = BUApplication.settings.referenceat;

        TextView mVersion = (TextView) findViewById(R.id.settings_version);
        try {
            mVersion.setText(mVersion.getText().toString() +  getPackageManager().getPackageInfo(getPackageName(), 0).versionName);
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

        nettypeSwitch.setChecked(BUApplication.settings.mNetType == 1);
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
                        titletextsize = progress + 10;
                        titleTextView.setText("帖子标题字体大小" + titletextsize
                                + "\t(推荐" + RECOMMENDTITESIZE + ")");
                        titleTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP,
                                titletextsize);
                    }
                });
        titleSeekBar.setProgress(titletextsize - 10);
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
                        contenttextsize = progress + 10;
                        contentTextView.setText("帖子内容字体大小" + contenttextsize
                                + "\t(推荐" + RECOMMENDCONTENTSIZE + ")");
                        contentTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP,
                                contenttextsize);
                    }
                });
        contentSeekBar.setProgress(contenttextsize - 10);
        nettypeSwitch
                .setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                    @Override
                    public void onCheckedChanged(CompoundButton buttonView,
                                                 boolean isChecked) {
                        Log.i("SettingsActivity", "外网模式>>" + isChecked);
                        BUApplication.settings.mNetType = isChecked ? 1 : 0;
                    }
                });

        showsig.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                Log.i("SettingsActivity", "显示签名>>" + isChecked);
                sig = isChecked;
            }
        });
        showsig.setChecked(sig);
        showimg.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                Log.i("SettingsActivity", "显示图片>>" + isChecked);
                img = isChecked;
            }
        });
        showimg.setChecked(img);
        referat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                Log.i("SettingsActivity", "引用At>>" + isChecked);
                ref = isChecked;
            }
        });
        referat.setChecked(ref);
    }

    protected void onDestroy() {
        BUApplication.settings.titletextsize = titletextsize;
        BUApplication.settings.contenttextsize = contenttextsize;
        BUApplication.settings.showsigature = sig;
        BUApplication.settings.showimage = img;
        BUApplication.settings.referenceat = ref;
        SharedPreferences config = getSharedPreferences("config", MODE_PRIVATE);
        SharedPreferences.Editor editor = config.edit();
        editor.putInt("titletextsize", titletextsize);
        editor.putInt("contenttextsize", contenttextsize);
        editor.putBoolean("showsigature", sig);
        editor.putBoolean("showimage", img);
        editor.putBoolean("referenceat", ref);
        editor.commit();
        Log.i("SettingActivity", "Config Saved>>" + titletextsize + ">>"
                + contenttextsize);
        super.onDestroy();
    }

    /**
     * Set up the {@link android.app.ActionBar}.
     */
    private void setupActionBar() {

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

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
                new AlertDialog.Builder(this)
                        .setMessage(Html.fromHtml("作者：Martin<br>E-mail：martin_oy@qq.com"))
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
